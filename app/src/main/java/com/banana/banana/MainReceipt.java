package com.banana.banana;

import android.content.Intent;
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

import com.google.api.gbase.client.Tax;

import java.util.ArrayList;
import java.util.List;

import static com.banana.banana.Checkout.request;
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

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        recView = (RecyclerView) findViewById(R.id.rec_list);
        recView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ReceiptAdapter(OrderData.getListData(), this, (MyList) getApplication());
        recView.setAdapter(adapter);

        //Get the total price and set
        double total = OrderData.getTotal();
        totalPrice = (TextView)findViewById(R.id.totalView);
        totalPrice.setText("Total: " + total);

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
//                adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
//                adapter.notifyItemRangeChanged(viewHolder.getAdapterPosition(), );
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

        // add new user to spinner
//        Intent newIntent = getIntent();
//        String name = newIntent.getStringExtra(PickItems.NEW_ID);
//        if (name != null) {
//            categories.add(name);
//        }
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
        totalPrice.setText("Total: " + total);
        System.out.println("TRYING TO UPDATE THE TOTAL. PLZ PLZ CHANGE: " + total);
    }

//    private void payItems(String name){
//        for (int i = 0; i < OrderData.size(); i++){
//            View thisView = recView.getChildAt(i);
//            if (MyList.getTracker(name)[i])
//                ((TextView) thisView.findViewById(R.id.payer)).setText(name);
//        }
//    }

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
//                Intent intentDone = new Intent(MainReceipt.this, SelectItems.class);
//                startActivityForResult(intentDone, request);

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
//                Set<String> users = ((MyList) getApplication()).getUsers();
//                System.out.println(users.toString());
//                for (String user : users){
//                    if (!categories.contains(user)){
//                        spinnerAdapter.add(user);
//                    }
//                }
//                spinnerAdapter.notifyDataSetChanged();
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

}
