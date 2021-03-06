package com.banana.banana;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends Activity
        implements EasyPermissions.PermissionCallbacks {

    // Credential request stuff
    GoogleAccountCredential mCredential;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {GmailScopes.MAIL_GOOGLE_COM};

    // Permission request stuff
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private PackageManager pm;

    // Text messaging stuff
    private static String myPhoneNo;
    private Button mSendRequests;

    // Organization/list stuff
    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle;
    HashMap<String, List<String>> expandableListDetail;

    // Create the final activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up texting
        TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        myPhoneNo =  tm.getLine1Number();

        // Check SMS Permissions in beginning
        pm = this.getPackageManager();
        int hasPerm = pm.checkPermission(
                Manifest.permission.SEND_SMS,
                this.getPackageName());
        if (hasPerm != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_REQUEST_SEND_SMS);
        }

        // Set up the message sending button
        mSendRequests = (Button) findViewById(R.id.callApi);
        mSendRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSendRequests.setEnabled(false);
                PerformTextOperation();
                PerformEmailOperation();
                Toast.makeText(getApplicationContext(), "Requests Sent",
                        Toast.LENGTH_LONG).show();
                mSendRequests.setEnabled(true);
            }
        });

        // Set up the overview list
        expandableListView = (ExpandableListView) findViewById(R.id.finalList);
        expandableListDetail = ((MyList) getApplication()).getPumpData();
        expandableListTitle = new ArrayList<String>(expandableListDetail.keySet());
        expandableListAdapter = new ExpandableListAdapter(this, expandableListTitle, expandableListDetail);
        expandableListView.setAdapter(expandableListAdapter);

        int count = expandableListAdapter.getGroupCount();
        for ( int i = 0; i < count; i++ )
            expandableListView.expandGroup(i);

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                Toast.makeText(
                        getApplicationContext(),
                        expandableListTitle.get(groupPosition)
                                + " -> "
                                + expandableListDetail.get(
                                expandableListTitle.get(groupPosition)).get(
                                childPosition), Toast.LENGTH_SHORT
                ).show();
                return false;
            }
        });

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
    }

    // Function for finding text contacts and sending messages
    private void PerformTextOperation() {
        Set<String> names = ((MyList) getApplication()).getUsers();
        List<Order> list;

        // Check for SMS permissions
        int hasPerm = pm.checkPermission(
                Manifest.permission.SEND_SMS,
                this.getPackageName());
        CheckBox useVenmo = (CheckBox)findViewById(R.id.useVenmo);
        if (hasPerm == PackageManager.PERMISSION_GRANTED) {
            // Search through and find text Contacts, then send texts
            for (String name : names) {
                if (((MyList) getApplication()).isNumber(name)) {
                    String phoneNo = ((MyList) getApplication()).getMethod(name);
                    list = ((MyList) getApplication()).getUserOrders(name);
                    if (list.size() == 0)
                        continue;
                    // Set up message to be sent
                    String header = ("You owe me for:");
                    SmsManager sms = SmsManager.getDefault();
                    sms.sendTextMessage(phoneNo, null, header.toString(), null, null);
                    for (Order order : list) {
                        String body = order.getItem().toString() + ": $" + order.getPrice().toString();
                        sms.sendTextMessage(phoneNo, null, body, null, null);
                    }
                    Double amt = ((MyList) getApplication()).getUserTotal(name);
                    String total = Double.toString(amt);
                    // Create Venmo link and send it users selected so
                    String link = "https://venmo.com/?txn=pay&audience=friends&recipients="
                        + myPhoneNo + "&amount=" + total + "&note=Banana+Payment";
                    if (useVenmo.isChecked())
                        sms.sendTextMessage(phoneNo, null, link, null, null);
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "Need SMS Permissions.",
                    Toast.LENGTH_LONG).show();
        }
    }

    // Attempt to call the API, after verifying that all the preconditions are satisfied.
    private void PerformEmailOperation() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            Toast.makeText(getApplicationContext(), "No network connection available.",
                    Toast.LENGTH_LONG).show();
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }


    // Attempts to set the account used with the API credentials
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        // Check permissions for getting accounts
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                PerformEmailOperation();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Different cases for activity requests
        switch (requestCode) {
            // needed google play services
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "This app requires Google Play Services",
                            Toast.LENGTH_LONG).show();
                } else {
                    // Create and send the email
                    PerformEmailOperation();
                }
                break;
            // needed to choose an account
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        // Create and send an email
                        PerformEmailOperation();
                    }
                }
                break;
            // needed to request Gmail authorization
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    PerformEmailOperation();
                }
                break;
        }
    }

    // Respond to permission request.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    // Checks whether the device currently has a network connection.
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    // Check that Google Play services APK is installed and up to date.
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

     // Attempt to resolve a missing, out-of-date, invalid or disabled Google Play Services
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    // Display an error dialog showing that Google Play Services is missing
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }


    // An asynchronous task that handles the Gmail API call
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.gmail.Gmail mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.gmail.Gmail.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Gmail API Android Quickstart")
                    .build();
        }

        // Background task to call Gmail API.
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                CreateAndSendEmail(mService);
                return null;
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            //Do Nothing
        }

        @Override
        protected void onPostExecute(List<String> output) {
            //Do Nothing
        }

        // if the user backs out of sending the message
        @Override
        protected void onCancelled() {
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    Toast.makeText(getApplicationContext(), "The following error occurred: \n"
                            + mLastError, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Request Cancelled",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    // Create a MimeMessage using the parameters provided.
    public static MimeMessage createEmail(String to,
                                          String from,
                                          String subject,
                                          String bodyText)
            throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);
        return email;
    }

    // Create a message from an email.
    public static com.google.api.services.gmail.model.Message createMessageWithEmail(MimeMessage emailContent)
            throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        com.google.api.services.gmail.model.Message message = new com.google.api.services.gmail.model.Message();
        message.setRaw(encodedEmail);
        return message;
    }

    // Send an email from the user's mailbox to its recipient.
    public static com.google.api.services.gmail.model.Message sendMessage(Gmail service,
                                                                          String userId,
                                                                          MimeMessage emailContent)
            throws MessagingException, IOException {
        com.google.api.services.gmail.model.Message message = createMessageWithEmail(emailContent);
        message = service.users().messages().send(userId, message).execute();
        return message;
    }

    // Function responsible for constructing each email to be sent
    private void CreateAndSendEmail(Gmail service) throws MessagingException {

        Set<String> names = ((MyList) getApplication()).getUsers();
        List<Order> list;
        CheckBox useVenmo = (CheckBox)findViewById(R.id.useVenmo);

        // Search through names and create and send emails
        for (String name : names) {
            // Checks to make sure it's an email
            if (!((MyList) getApplication()).isNumber(name)) {
                list = ((MyList) getApplication()).getUserOrders(name);
                if (list.size() == 0) {
                    // if no items given to this user, no message is sent
                    continue;
                }

                // Creates actual email
                String email = ((MyList) getApplication()).getMethod(name);
                StringBuilder body = new StringBuilder();
                body.append("You owe me for:\n");
                for (Order order : list)
                    body.append(order.getItem() + ": $" + order.getPrice() + "\n");
                String subject = "Payment from your group receipt";

                Double amt = ((MyList) getApplication()).getUserTotal(name);
                amt = ((double)Math.round(amt * 100)) / 100;
                String total = Double.toString(amt);
                String link = "https://venmo.com/?txn=pay&audience=friends&recipients="
                        + myPhoneNo + "&amount=" + total + "&note=Banana+Payment";
                if (useVenmo.isChecked())
                    body.append(link + "\n");
                MimeMessage mimeMessage = createEmail(email, mCredential.getSelectedAccountName(), subject, body.toString());

                // Attempts to send the above created email
                try {
                    sendMessage(service, "me", mimeMessage);
                } catch (UserRecoverableAuthIOException e) {
                    startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                } catch (Exception e) {
                    System.out.println("Exception caught: " + e.toString());
                }
            }
        }
    }
}