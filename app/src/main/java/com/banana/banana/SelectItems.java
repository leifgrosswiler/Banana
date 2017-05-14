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

    //Contacts stuff
    private ListView contNames;
    public static List<CoolList> everything;
    private static String person;
    private static Intent i;

    private ArrayAdapter<CoolList> adapter;
    private SearchView sv;


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

                    // go to pick items screen
                    i = new Intent(SelectItems.this, PickItems.class);
                    i.putExtra(PickItems_ID, adapter.getItem(position).get(0));

                    person = adapter.getItem(position).get(0);
                    PopupMenu popup = new PopupMenu(SelectItems.this, v);
                    // Inflating the Popup using xml file
                    MenuInflater MI = popup.getMenuInflater();
                    MI.inflate(R.menu.popup, popup.getMenu());

                    if (adapter.getItem(position).get(1) != null) {
                        popup.getMenu().add(adapter.getItem(position).get(1));
                    }
                    if (adapter.getItem(position).size() > 2) {
                        if (adapter.getItem(position).get(2) != null)
                            popup.getMenu().add(adapter.getItem(position).get(2));
                    }

                    // linking popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            Toast.makeText(
                                    SelectItems.this,
                                    "You Clicked : " + item.getTitle(),
                                    Toast.LENGTH_SHORT
                            ).show();
                            System.out.println(person);
                            if (item.getTitle().toString().contains("@"))
                                ((MyList) getApplication()).add(person, item.getTitle().toString(), false);
                            else
                                ((MyList) getApplication()).add(person, item.getTitle().toString(), true);
                            MyList.addUser(person, OrderData.size());
                            startActivity(i);
                            return true;
                        }
                    });

                    popup.show(); // showing popup menu

                    System.out.println(((MyList) getApplication()).getUsers());
                }
                System.out.println(adapter.getItem(position));

            }
        });

    }

    // Show the contacts in the ListView.
    private void showContacts() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            // After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            // Android version is lesser than 6.0 or the permission is already granted
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
                Toast.makeText(this, "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Checks if permissions have changed
    @Override
    public void onResume() {
        super.onResume();

        // Variables used for checking and requestng permissions
        boolean shouldStay = false;
        List<String> permissionRequests = new ArrayList<>();

        // Figure out all of the current permssions granted
        // Request permissions if necessary
        PackageManager pm = this.getPackageManager();

        // Check contacts permission
        int hasPermCont = pm.checkPermission(
                Manifest.permission.READ_CONTACTS,
                this.getPackageName());
        if (hasPermCont != PackageManager.PERMISSION_GRANTED) {
            permissionRequests.add(Manifest.permission.READ_CONTACTS);
            shouldStay = true;
        }

        // Check camera permissions
        int hasPermCam = pm.checkPermission(
                Manifest.permission.CAMERA,
                this.getPackageName());
        if (hasPermCam != PackageManager.PERMISSION_GRANTED) {
            permissionRequests.add(Manifest.permission.CAMERA);
            shouldStay = true;
        }

        // Check SMS permissions
        int hasPermSMS = pm.checkPermission(
                Manifest.permission.SEND_SMS,
                this.getPackageName());
        if (hasPermSMS != PackageManager.PERMISSION_GRANTED) {
            permissionRequests.add(Manifest.permission.SEND_SMS);
            shouldStay = true;
        }

        // Check storage permissions
        int hasPermStore = pm.checkPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                this.getPackageName());
        if (hasPermStore != PackageManager.PERMISSION_GRANTED) {
            permissionRequests.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            shouldStay = true;
        }

        // Check phone number accessibility permissions
        int hasPermNumb = pm.checkPermission(
                Manifest.permission.READ_PHONE_STATE,
                this.getPackageName());
        if (hasPermNumb != PackageManager.PERMISSION_GRANTED) {
            permissionRequests.add(Manifest.permission.READ_PHONE_STATE);
            shouldStay = true;
        }

        if (shouldStay) {
            Intent i = new Intent(this, StartScreen.class);
            startActivity(i);
        }
    }

}

