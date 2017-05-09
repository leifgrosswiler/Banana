package com.banana.banana;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Path;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.banana.banana.MainReceipt.categories;
import static com.banana.banana.MainReceipt.spinnerAdapter;

public class SelectItems extends AppCompatActivity {

    public static final String NAME_ID = "com.banana.nameID";
    public static final String SELECTED_ID = "com.banana.selectedID";
    public static final String PickItems_ID = "com.banana.pickID";
    ListView listView;
    ArrayList<OrderOld> dataModels;

    //Contacts stuff
    private ListView contNames;
    public static List<CoolList> everything;
    private static String person;
    private static Intent i;

    private ArrayAdapter<CoolList> adapter;
    private SearchView sv;
//    private ArrayList<String> newNames = new ArrayList<>();


    // Request code for READ_CONTACTS. It can be any number > 0.
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_items);

        // Find the list view
        this.contNames = (ListView) findViewById(R.id.contNames);

        sv = (SearchView) findViewById(R.id.search);

        // Read and show the contacts
        showContacts();

        // Click on contact so it shows up in text view
        contNames.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if(!((MyList) getApplication()).contains(adapter.getItem(position).get(0))) {
                    System.out.println(v.toString());
                    if (adapter.getItem(position).get(1) != null) {
                        System.out.println("Email adding");
                        ((MyList) getApplication()).add(adapter.getItem(position).get(0), adapter.getItem(position).get(1), false);
                    } else {
                        System.out.println("Number adding");
                        ((MyList) getApplication()).add(adapter.getItem(position).get(0), adapter.getItem(position).get(2), true);
                    }
                    MyList.addUser(adapter.getItem(position).get(0), OrderData.size());

                    // go to pick items screen
                    i = new Intent(SelectItems.this, PickItems.class);
                    i.putExtra(PickItems_ID, adapter.getItem(position).get(0));
//                    startActivity(i);

                    person = adapter.getItem(position).get(0);
                    PopupMenu popup = new PopupMenu(SelectItems.this, v);
                    //Inflating the Popup using xml file
                    MenuInflater MI = popup.getMenuInflater();
                    MI.inflate(R.menu.popup, popup.getMenu());

                    if (adapter.getItem(position).get(1) != null) {
                        popup.getMenu().add(adapter.getItem(position).get(1));
                    }
                    if (adapter.getItem(position).size() > 2) {
                        popup.getMenu().add(adapter.getItem(position).get(2));
                    }

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            Toast.makeText(
                                    SelectItems.this,
                                    "You Clicked : " + item.getTitle(),
                                    Toast.LENGTH_SHORT
                            ).show();
                            System.out.println(person);
//                            if (item.getTitle().toString().contains("@"))
//                                ((MyList) getApplication()).add(person, item.getTitle().toString(), false);
//                            else
//                                ((MyList) getApplication()).add(person, item.getTitle().toString(), true);
//                            Intent i = new Intent(SelectItems.this, PickItems.class);
//                            i.putExtra(PickItems_ID, item.getTitle().toString());
                            startActivity(i);
                            return true;
                        }
                    });

                    popup.show(); //showing popup menu

                    System.out.println(((MyList) getApplication()).getUsers());
                }
                // TODO: print toaster message for user already exist in else
                System.out.println(adapter.getItem(position));

            }
        });

        // Return to parent
        Button finish = (Button) findViewById(R.id.finish);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            //getContactNames();
            adapter = new ArrayAdapter<CoolList>(this, android.R.layout.simple_selectable_list_item, everything);
            contNames.setAdapter(adapter);

            // implement search function
            sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextChange(String newText) {
                    adapter.getFilter().filter(newText);

                    return false;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

            });
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
                Toast.makeText(this, "Until you grant the permission, we cannot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Read the name of all the contacts.
     *
     * @return a list of names.
     */
//    private void getContactNames() {
//        List<CoolList> all = new ArrayList<>();
//
//        // Get the ContentResolver
//        ContentResolver cr = getContentResolver();
//        // Get the Cursor of all the contacts
//        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
//
//        // Move the cursor to first. Also check whether the cursor is empty or not.
//        if (cursor.moveToFirst()) {
//            // Iterate through the cursor
//            int i = 0;
//            do {
//                // Get the contacts name
//                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
//                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
//
//                // Get the contacts email
//                Cursor emailCursor = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
//                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{ id }, null);
//
//                Cursor phoneCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
//                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{ id }, null);
//
//                //Only adds contact if an email address or phone numbers is associated with it
//                if (phoneCursor.moveToNext()) {
//                    all.add(new CoolList());
//                    all.get(i).add(name);
//                    String number = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
//                    if (emailCursor.moveToNext()) {
//                        String email = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
//                        all.get(i).add(email);
//                        all.get(i).add(number);
//                    } else {
//                        all.get(i).add(null);
//                        all.get(i).add(number);
//                    }
//                    i++;
//                } else if (emailCursor.moveToNext()) {
//                    all.add(new CoolList());
//                    all.get(i).add(name);
//                    String email = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
//                    all.get(i).add(email);
//                    i++;
//                }
//            } while (cursor.moveToNext());
//        }
//        // Close the cursor
//        cursor.close();
//
//        everything = all;
//    }

    // Specific String List built for this class to use in the list view (uses specific toString method)
//    public class CoolList extends ArrayList {
//
//        ArrayList<String> alist;
//
//        public CoolList() {
//            alist = new ArrayList<String>();
//        }
//
//        // Special toString function for this class
//        @Override
//        public String toString() {
//            String s = "";
//            for (int i = 0; i < alist.size(); i++) {
//                // Doesn't add if the item is null (this could only be the email address currently)
//                if (alist.get(i) != null)
//                    s = s + alist.get(i) + "\n";
//            }
//            return s;
//        }
//
//        public void add(String e) {
//            alist.add(e);
//        }
//
//        @Override
//        public String get(int ind) {
//            return alist.get(ind);
//        }
//
//    }

}

