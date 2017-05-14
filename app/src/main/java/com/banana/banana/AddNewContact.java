package com.banana.banana;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.banana.banana.R.menu.app_bar2_menu;


public class AddNewContact extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_contact);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView header = (TextView) findViewById(R.id.name);
        header.setText("ADD NEW CONTACT");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(app_bar2_menu, menu);
        return true;
    }


    // App Bar Icons OnClick
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.check:
                EditText contactName = (EditText) findViewById(R.id.contactName);
                EditText contactInfo = (EditText) findViewById(R.id.contactInfo);
                String name = contactName.getText().toString();
                String info = contactInfo.getText().toString().trim();

                String emailCheck = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
                String phoneCheck = "^\\d{10}$";

                Pattern emailPattern = Pattern.compile(emailCheck, Pattern.CASE_INSENSITIVE);
                Pattern phonePattern = Pattern.compile(phoneCheck, Pattern.CASE_INSENSITIVE);

                Matcher emailMatcher = emailPattern.matcher(info);
                Matcher phoneMatcher = phonePattern.matcher(info);

                if (name.isEmpty() || name.trim().isEmpty()) {
                    Toast.makeText(AddNewContact.this, "Invalid Input", Toast.LENGTH_SHORT).show();
                }
                else if (emailMatcher.matches()){
                    ((MyList) getApplication()).add(name, info, false);
                    MyList.addUser(name, OrderData.size());

                    Intent intent = new Intent(AddNewContact.this, PickItems.class);
                    intent.putExtra(SelectItems.PickItems_ID, name);
                    startActivity(intent);
                }
                else if (phoneMatcher.matches()){
                    ((MyList) getApplication()).add(name, info, true);
                    MyList.addUser(name, OrderData.size());

                    Intent intent = new Intent(AddNewContact.this, PickItems.class);
                    intent.putExtra(SelectItems.PickItems_ID, name);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(AddNewContact.this, "Invalid Input", Toast.LENGTH_SHORT).show();
                }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

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
