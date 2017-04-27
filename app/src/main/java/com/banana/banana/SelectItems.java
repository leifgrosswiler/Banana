package com.banana.banana;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.banana.banana.MainReceipt.categories;
import static com.banana.banana.MainReceipt.spinnerAdapter;

public class SelectItems extends AppCompatActivity {

    public static final String NAME_ID = "com.banana.nameID";
    public static final String SELECTED_ID = "com.banana.selectedID";
    ListView listView;
    ArrayList<OrderOld> dataModels;

    //Contacts stuff
    private ListView contNames;
    private List<String> conts;
    private List<String> addrs;
    private List<String> phNums;
    private List<String> both;
//    private ArrayList<String> newNames = new ArrayList<>();


    // Request code for READ_CONTACTS. It can be any number > 0.
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_items);

//        Intent intentReceive = getIntent();
//        //String tmp = (String) intentReceive.getStringExtra(EditRecepit.DATAMODELS_ID);
//        dataModels = (ArrayList<OrderOld>) intentReceive.getSerializableExtra(EditReceipt.DATAMODELS_ID);

//        // LISTVIEW
//        listView = (ListView) findViewById(R.id.checkList);
//        SelectItemsListAdapter adapter = new SelectItemsListAdapter(dataModels, getApplicationContext());
//        listView.setAdapter(adapter);

        // Find the list view
        this.contNames = (ListView) findViewById(R.id.contNames);

        // Read and show the contacts
        showContacts();

        // Array for keeping track of names and emails
        final String[] cont = {"", ""};

        // Click on contact so it shows up in text view
        contNames.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if(!((MyList) getApplication()).contains(conts.get(position))) {
                    if (addrs.get(position).length() != 0) {
//                    cont[0] = addrs.get(position);
                        System.out.println("Email adding");
                        ((MyList) getApplication()).add(conts.get(position), addrs.get(position), false);
                        MyList.addUser(conts.get(position), OrderData.size());
                    } else {
//                    cont[0] = phNums.get(position);
                        System.out.println("Number adding");
                        ((MyList) getApplication()).add(conts.get(position), phNums.get(position), true);
                        MyList.addUser(conts.get(position), OrderData.size());
                    }
//                cont[1] = conts.get(position);
//                    newNames.add(conts.get(position));
                    System.out.println(((MyList) getApplication()).getUsers());
                }
                System.out.println(conts.get(position));
            }
        });

        // Return to parent
        Button finish = (Button) findViewById(R.id.finish);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get user's name

//                String email = cont[0];
//                String name = cont[1];


//
//                ArrayList<String> emailAndItems = new ArrayList<>(Arrays.asList(email));
//
//                // create boolean[] to store positions of checked items
//                View v;
//                CheckBox cb;
//                boolean[] checked = new boolean[listView.getCount()];
//                for (int i = 0; i < listView.getCount(); i++) {
//                    v = listView.getChildAt(i);
//                    cb = (CheckBox) v.findViewById(R.id.checkBox);
//                    if(cb.isChecked()) {
//                        checked[i] = true;
//                        String selected = ((TextView) v.findViewById(R.id.list_item_select)).getText().toString();
//                        String price = ((TextView) v.findViewById(R.id.price_select)).getText().toString();
//                        //System.out.println(selected + " " + price);
//                        emailAndItems.add(selected);
//                        emailAndItems.add(price);
//                    }
//                    else {
//                        checked[i] = false;
//                    }
//                }
//
//                ((MyList) getApplication()).addPair(name, emailAndItems);

                Intent resultIntent = new Intent();
//                resultIntent.putExtra(SELECTED_ID, checked);
//                resultIntent.putExtra(NAME_ID, name);
                setResult(Activity.RESULT_OK,resultIntent);
                Set<String> users = ((MyList) getApplication()).getUsers();
//                System.out.println(users.toString());
                for (String user : users){
                    if (!categories.contains(user)){
                        spinnerAdapter.add(user);
                    }
                }
                spinnerAdapter.notifyDataSetChanged();
                finish();

            }
        });
    }

    /**
     * Show the contacts in the ListView.
     */
    private void showContacts() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            // Android version is lesser than 6.0 or the permission is already granted.
            getContactNames();
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_selectable_list_item, both);
            contNames.setAdapter(adapter);
        }
    }

    //Tries to get permission to read contacts from the user (only version 6.0 and above)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                showContacts();
            } else {
                Toast.makeText(this, "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Read the name of all the contacts.
     *
     * @return a list of names.
     */
    private void getContactNames() {
        List<String> contacts = new ArrayList<>();
        List<String> emails = new ArrayList<>();
        List<String> numbers = new ArrayList<>();
        List<String> together = new ArrayList<>();

        // Get the ContentResolver
        ContentResolver cr = getContentResolver();
        // Get the Cursor of all the contacts
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        // Move the cursor to first. Also check whether the cursor is empty or not.
        if (cursor.moveToFirst()) {
            // Iterate through the cursor
            do {
                // Get the contacts name
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                // Get the contacts email
                Cursor emailCursor = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{ id }, null);

                Cursor phoneCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{ id }, null);

                //Only adds contact if an email address or phone numbers is associated with it
                if (phoneCursor.moveToNext()) {
                    String number = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
                    if (emailCursor.moveToNext()) {
                        String email = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                        together.add(name + "\n" + number + "\n" + email);
                        emails.add(email);
                    } else {
                        together.add(name + "\n" + number);
                        emails.add("");
                    }
                    contacts.add(name);
                    numbers.add(number);
                } else if (emailCursor.moveToNext()) {
                    String email = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    together.add(name + "\n" + email);
                    contacts.add(name);
                    numbers.add("");
                    emails.add(email);
                }
            } while (cursor.moveToNext());
        }
        // Close the cursor
        cursor.close();

        //List with all contact names
        conts = contacts;
        //List with all contact email addresses
        addrs = emails;
        //List with all contact phone numbers
        phNums = numbers;
        //List with both of the above for all contacts
        both = together;
    }

}

