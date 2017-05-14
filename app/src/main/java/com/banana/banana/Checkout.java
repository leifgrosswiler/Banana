package com.banana.banana;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static com.banana.banana.EditReceipt.DATAMODELS_ID;

public class Checkout extends AppCompatActivity {

    public static final int request =  18;
    ListView listView;
    ArrayList<OrderOld> dataModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        Intent intentReceive = getIntent();
        dataModels = (ArrayList<OrderOld>) intentReceive.getSerializableExtra(DATAMODELS_ID);

        // LISTVIEW
        listView = (ListView) findViewById(R.id.checkOutList);
        EditReceiptListAdapter adapter = new EditReceiptListAdapter(dataModels, getApplicationContext());
        listView.setAdapter(adapter);

        // GMAIL BUTTON
        Button gmail = (Button) findViewById(R.id.gmail);
        gmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentGmail = new Intent(Checkout.this, MainActivity.class);
                startActivity(intentGmail);
            }
        });

        // DONE BUTTON
        Button done = (Button) findViewById(R.id.checkOutfinish);
        done.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intentDone = new Intent(Checkout.this, SelectItems.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(DATAMODELS_ID, dataModels);
                intentDone.putExtras(bundle);
                startActivityForResult(intentDone, request);
            }
        });
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case (request) : {

                String name = null;
                boolean[] selected = null;
                if (resultCode == Activity.RESULT_OK) {
                    name = (String) data.getStringExtra(SelectItems.NAME_ID);
                    selected = data.getBooleanArrayExtra(SelectItems.SELECTED_ID);
                }

                ((MyList) getApplication()).printList();

                for (int i = 0; i < listView.getCount(); i++) {
                    View v = listView.getChildAt(i);
                    TextView identifier = (TextView) v.findViewById(R.id.identifier);
                    if (name != null && selected[i] == true) {
                        identifier.setText(name);
                    }
                }

                break;

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
