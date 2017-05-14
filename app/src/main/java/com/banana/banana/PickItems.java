package com.banana.banana;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static com.banana.banana.R.menu.app_bar2_menu;


public class PickItems extends AppCompatActivity {

    public static final String NEW_ID = "com.banana.banana.NEW";

    private static RecyclerView recView;
    public static ReceiptAdapter adapter;
    private Toolbar toolbar;
    private String name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_items);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        recView = (RecyclerView) findViewById(R.id.rec_list);
        recView.setLayoutManager(new LinearLayoutManager(this));

        Intent i = getIntent();
        name = i.getStringExtra(SelectItems.PickItems_ID);

        adapter = new ReceiptAdapter(OrderData.getListData(), this, (MyList) getApplication());
        adapter.setMode(ReceiptAdapter.PICK_ITEMS);
        adapter.setUser(name);
        recView.setAdapter(adapter);


        // pull intent, set header
        ((MyList) getApplication()).setUser(name);
        TextView header = (TextView) findViewById(R.id.name);
        header.setText(name);
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
                Intent intent = new Intent(PickItems.this, MainReceipt.class);
                startActivity(intent);

            default:
                return super.onOptionsItemSelected(item);
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

