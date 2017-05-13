package com.banana.banana;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.banana.banana.SelectItems.everything;

/**
 * Created by Jacob on 5/9/17.
 */

public class StartScreen extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_start);

        // Set status bar to white
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.WHITE);

        setThingsUp();
    }

    private void setThingsUp() {

        boolean shouldStay = false;
        List<String> permissionRequests = new ArrayList<>();
        int pCount = 0;

        // Figure out all of the current permssions granted
        PackageManager pm = this.getPackageManager();
        int hasPermCont = pm.checkPermission(
                Manifest.permission.READ_CONTACTS,
                this.getPackageName());
        if (hasPermCont != PackageManager.PERMISSION_GRANTED) {
            permissionRequests.add(Manifest.permission.READ_CONTACTS);
            shouldStay = true;
            pCount++;
        } else if (everything == null) {
            getContactNames();
        }
        int hasPermCam = pm.checkPermission(
                Manifest.permission.CAMERA,
                this.getPackageName());
        if (hasPermCam != PackageManager.PERMISSION_GRANTED) {
            permissionRequests.add(Manifest.permission.CAMERA);
            shouldStay = true;
            pCount++;
        }
        int hasPermSMS = pm.checkPermission(
                Manifest.permission.SEND_SMS,
                this.getPackageName());
        if (hasPermSMS != PackageManager.PERMISSION_GRANTED) {
            permissionRequests.add(Manifest.permission.SEND_SMS);
            shouldStay = true;
            pCount++;
        }
        int hasPermStore = pm.checkPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                this.getPackageName());
        if (hasPermStore != PackageManager.PERMISSION_GRANTED) {
            permissionRequests.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            shouldStay = true;
            pCount++;
        }
        int hasPermNumb = pm.checkPermission(
                Manifest.permission.READ_PHONE_STATE,
                this.getPackageName());
        if (hasPermNumb != PackageManager.PERMISSION_GRANTED) {
            permissionRequests.add(Manifest.permission.READ_PHONE_STATE);
            shouldStay = true;
            pCount++;
        }

        if (shouldStay) {
            // If not all permission granted, request them
            String[] permissionsArray = new String[pCount];
            ActivityCompat.requestPermissions(this,
                    permissionRequests.toArray(permissionsArray),
                    MY_PERMISSIONS_REQUEST);
        } else {
            // If all necessary permissions granted, move to main app
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                    Intent mainIntent = new Intent(StartScreen.this, OpenCamera.class);
                    StartScreen.this.startActivity(mainIntent);
                    StartScreen.this.finish();
                }
            }, 2000);
        }
    }

    private void getContactNames() {

        List<CoolList> all = new ArrayList<>();
        Map<String, ArrayList<String>> tempAll = new HashMap<>();

        String[] EMAIL_PROJECTION = new String[] {
                ContactsContract.CommonDataKinds.Email.CONTACT_ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Email.DATA
        };

        String[] PHONE_PROJECTION = new String[] {
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.DATA
        };

        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, EMAIL_PROJECTION, null, null, null);
        Cursor phoneCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PHONE_PROJECTION, null, null, null);

        if (cursor != null && phoneCursor != null) {
            try {
                final int displayNameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                final int emailIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
                final int displayNameIndexNum = phoneCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                final int phoneIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA);

                String displayName, address, displayNameNum, number;

                while (cursor.moveToNext()) {
                    ArrayList<String> curContact = new ArrayList<>();
                    displayName = cursor.getString(displayNameIndex);
                    address = cursor.getString(emailIndex);
                    curContact.add(0, address);
                    curContact.add(1, null);
                    tempAll.put(displayName, curContact);
                }

                while (phoneCursor.moveToNext()) {
                    displayNameNum = phoneCursor.getString(displayNameIndexNum);
                    number = phoneCursor.getString(phoneIndex);
                    ArrayList<String> curContact = tempAll.get(displayNameNum);

                    if (curContact != null) {
                        curContact.add(1, number);
                        tempAll.put(displayNameNum, curContact);
                    }
                    else {
                        curContact = new ArrayList<>();
                        curContact.add(0, null);
                        curContact.add(1, number);
                        tempAll.put(displayNameNum, curContact);
                    }
                }

            } finally {
                cursor.close();
                phoneCursor.close();
            }
        }

        for(String key : tempAll.keySet()) {
            CoolList curContact = new CoolList();
            String curEmail = tempAll.get(key).get(0);
            String curPhone = tempAll.get(key).get(1);
            curContact.add(key);
            curContact.add(curEmail); // email
            curContact.add(curPhone); // number
            all.add(curContact);
        }

        everything = all;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST) {
            // Restart after requesting permissions to see if they were granted
            setThingsUp();
        }
    }

}
