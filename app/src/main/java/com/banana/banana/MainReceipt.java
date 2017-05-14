package com.banana.banana;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static com.banana.banana.Checkout.request;
import static com.banana.banana.OpenCamera.cropFile;
import static com.banana.banana.OpenCamera.photoFile;
import static com.banana.banana.R.menu.app_bar_menu;

public class MainReceipt extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    public static final String ITEM_ID = "com.banana.itemID";
    public static final String PRICE_ID = "com.banana.priceID";
    public static final String SPINNER_ID = "com.banana.spinnerID";
    public static final String P_ID = "com.banana.pID";
    private static TextView totalPrice;

    public Spinner spinner;

    private RecyclerView recView;
    public static ReceiptAdapter adapter;
    private Toolbar toolbar;
    public static List<String> categories;
    private static TextView orderTotal;
    public static ArrayAdapter<String> spinnerAdapter;


    @Override
    // TODO: incorporate mylist
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_receipt);

        PackageManager pm = this.getPackageManager();
        int hasPerm = pm.checkPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                this.getPackageName());
        if (hasPerm == PackageManager.PERMISSION_GRANTED) {
            if (cropFile != null && photoFile != null) {
                if (cropFile.exists() || photoFile.exists()) {
                    cropFile.delete();
                    photoFile.delete();
                }
            }
        }

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        recView = (RecyclerView) findViewById(R.id.rec_list);
        recView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ReceiptAdapter(OrderData.getListData(), this, (MyList) getApplication());
        recView.setAdapter(adapter);

        //Get the total price and set
        double total = OrderData.getTotal();
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        String formattedPrice;
        try {
            formattedPrice = formatter.format(total);
        }
        catch (java.lang.NumberFormatException e) {
            formattedPrice = "$0.00";
        }
        System.out.println("FORMATTED PRICE IS: " + formattedPrice);
        totalPrice = (TextView)findViewById(R.id.totalView);
        totalPrice.setText("Total: " + formattedPrice);

        // floating action button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentTax = new Intent(MainReceipt.this, TaxAddition.class);
                startActivity(intentTax);
            }
        });


        // swipe to delete
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                OrderData.delete(viewHolder.getAdapterPosition());
            }

        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recView);

        // Spinner
        // TODO: remove hardcorded cateogires
        spinner = (Spinner) findViewById(R.id.spinner);
        categories = new ArrayList<String>();
        categories.add("Select Payer: ");
        spinnerAdapter = new ArrayAdapter<String>(MainReceipt.this,
                android.R.layout.simple_list_item_1, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);


        // add users to spinner
        for (String person : MyList.getAllUsers()){
            categories.add(person);
        }

        spinner.setAdapter(spinnerAdapter);


        // spinner onClick
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // Spinner OnClick
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String user = spinner.getSelectedItem().toString();
                if (user != "Select Payer: ") {
                    Intent spinnerIntent = new Intent(MainReceipt.this, PickItems.class);
                    spinnerIntent.putExtra(SelectItems.PickItems_ID, user);
                    MainReceipt.this.startActivity(spinnerIntent);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });



    }

    public static void updateTotal() {
        double total = OrderData.getTotal();
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        String formattedPrice;
        try {
            formattedPrice = formatter.format(total);
        }
        catch (java.lang.NumberFormatException e) {
            formattedPrice = "$0.00";
        }
        System.out.println("FORMATTED PRICE IS: " + formattedPrice);
        totalPrice.setText("Total: " + formattedPrice);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(app_bar_menu, menu);
        return true;
    }

    // App Bar Icons OnClick
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_user:
                View vItem = findViewById(R.id.add_user);
                PopupMenu popup = new PopupMenu(this, vItem);
                popup.getMenuInflater().inflate(R.menu.pop_menu1, popup.getMenu());


                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.addNew) {
                            Intent intentNew = new Intent(MainReceipt.this, AddNewContact.class);
                            startActivity(intentNew);
                        } else {
                            Intent intentDone = new Intent(MainReceipt.this, SelectItems.class);
                            startActivityForResult(intentDone, request);
                        }
                        return true;
                    }
                });

                popup.show();

                return true;
            case R.id.add_item:
                Intent addOrder = new Intent(this, AddOrder.class);
                startActivity(addOrder);
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String item = adapterView.getItemAtPosition(i).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        OrderData.DeleteWholeOrder();
        ((MyList) getApplication()).Restart();
        startActivity(new Intent(this, OpenCamera.class));
        recView.setAdapter(null);
        finish();

    }

    @Override
    public void onResume() {
        super.onResume();

        // Variables used for checking and requestng permissions
        boolean shouldStay = false;
        List<String> permissionRequests = new ArrayList<>();
        int pCount = 0;

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
            pCount++;
        }

        // Check camera permissions
        int hasPermCam = pm.checkPermission(
                Manifest.permission.CAMERA,
                this.getPackageName());
        if (hasPermCam != PackageManager.PERMISSION_GRANTED) {
            permissionRequests.add(Manifest.permission.CAMERA);
            shouldStay = true;
            pCount++;
        }

        // Check SMS permissions
        int hasPermSMS = pm.checkPermission(
                Manifest.permission.SEND_SMS,
                this.getPackageName());
        if (hasPermSMS != PackageManager.PERMISSION_GRANTED) {
            permissionRequests.add(Manifest.permission.SEND_SMS);
            shouldStay = true;
            pCount++;
        }

        // Check storage permissions
        int hasPermStore = pm.checkPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                this.getPackageName());
        if (hasPermStore != PackageManager.PERMISSION_GRANTED) {
            permissionRequests.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            shouldStay = true;
            pCount++;
        }

        // Check phone number accessibility permissions
        int hasPermNumb = pm.checkPermission(
                Manifest.permission.READ_PHONE_STATE,
                this.getPackageName());
        if (hasPermNumb != PackageManager.PERMISSION_GRANTED) {
            permissionRequests.add(Manifest.permission.READ_PHONE_STATE);
            shouldStay = true;
            pCount++;
        }

        if (shouldStay) {
            Intent i = new Intent(this, StartScreen.class);
            startActivity(i);
        }
    }


}
