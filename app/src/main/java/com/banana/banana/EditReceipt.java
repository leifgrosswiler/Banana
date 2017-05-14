package com.banana.banana;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import static com.banana.banana.OpenCamera.parseResult;


public class EditReceipt extends AppCompatActivity {

    public static final String ITEM_ID = "com.banana.itemID";
    public static final String PRICE_ID = "com.banana.priceID";
    public static final String POSITION_ID = "com.banana.positionID";
    public static final String DATAMODELS_ID = "com.banana.dataModelsID";
    public static final String SOURCE = "com.banana.source";
    // inputs
    public static String food[];// = {"Pepperoni and Sausage Pizza", "Pasta", "Ice Cream Sandwitches", "Cheerios with Honey Oats", "Chicken Nuggets", "Tomato Soup"};
    public static String price[];// = {"$15.74", "$1300.02", "$14.00", "$5.99", "$2.33", "$0.99"};


    ListView listView;
    ArrayList<OrderOld> dataModels;
    private static EditReceiptListAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_receipt);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        // LISTVIEW
        listView=(ListView)findViewById(android.R.id.list);
        dataModels= new ArrayList<>();
        for (int i = 0; i < food.length; i++) {
            dataModels.add(new OrderOld(food[i], price[i]));
        }
        adapter = new EditReceiptListAdapter(dataModels, getApplicationContext());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                OrderOld dataModel= dataModels.get(position);
                editSpecifics(dataModel, position);
            }
        });

        // ADD BUTTON
        Button add = (Button) findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intentAdd = new Intent(EditReceipt.this, AddOrder.class);
                startActivity(intentAdd);
                finish();
            }
        });

        // DONE BUTTON
        Button done = (Button) findViewById(R.id.done_edit);
        done.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intentDone = new Intent(EditReceipt.this, Checkout.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(DATAMODELS_ID, dataModels);
                intentDone.putExtras(bundle);
                startActivity(intentDone);
            }
        });

    }

    public void editSpecifics(OrderOld dataModel, int position){
        Intent intentEdit = new Intent(EditReceipt.this, EditSpecifics.class);
        intentEdit.putExtra(ITEM_ID, dataModel.getItem());
        intentEdit.putExtra(PRICE_ID, dataModel.getPrice());
        intentEdit.putExtra(POSITION_ID, position);
        startActivity(intentEdit);
        finish();
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

