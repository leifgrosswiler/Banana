package com.banana.banana;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.banana.banana.Checkout.request;
import static com.banana.banana.R.menu.app_bar_menu;


public class MainReceipt extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    public static final String ITEM_ID = "com.banana.itemID";
    public static final String PRICE_ID = "com.banana.priceID";
    public static final String NEW_ID = "com.banana.newID";

    public Spinner spinner;
    public static Tracker tracker;

    private RecyclerView recView;
    private ReceiptAdapter adapter;
    private Toolbar toolbar;
    public static List<String> categories;
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

        adapter = new ReceiptAdapter(OrderData.getListData(), this);
        recView.setAdapter(adapter);

        // floating action button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentGmail = new Intent(MainReceipt.this, MainActivity.class);
                startActivity(intentGmail);
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
                adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
//                adapter.notifyItemRangeChanged(viewHolder.getAdapterPosition(), );
            }

        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recView);

        // Spinner

        spinner = (Spinner) findViewById(R.id.spinner);
        categories = new ArrayList<String>();
        categories.add("Master");
        categories.add("Andrew");
        spinnerAdapter = new ArrayAdapter<String>(MainReceipt.this,
                android.R.layout.simple_list_item_1, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String user = spinner.getSelectedItem().toString();
                Toast.makeText(MainReceipt.this, user, Toast.LENGTH_SHORT).show();
                // TODO: replace this hard code
                Tracker.addUser("Andrew");
                System.out.println("THE ARRAY HAS SIZE " + Tracker.getTracker("Andrew").length);
                if (user == "Andrew") {
                    adapter.setMode(ReceiptAdapter.USER);
                    boolean[] andrewTracker = Tracker.getTracker("Andrew");
                    System.out.print(andrewTracker.length);
                    for(int p = 0; p < recView.getChildCount(); p++) {
                        System.out.println("Main: " + Tracker.getTracker("Andrew")[p]);
                        // TODO: FIX THIS BUG
                        try {
                            recView.getChildAt(p).setSelected(andrewTracker[p]);
                        } catch (Exception e) {
                            System.out.println("WTF" + andrewTracker.length);
                        }
                    }
                }
                else {
                    adapter.setMode(ReceiptAdapter.DEFAULT);
                    // change all background color back to normal in Master
                    for(int p = 0; p < recView.getChildCount(); p++) {
                        System.out.println("Master: " + Tracker.getTracker("Andrew")[p]);
                        recView.getChildAt(p).setSelected(false);
                    }

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_user:
                adapter.setMode(1);
                Intent intentDone = new Intent(MainReceipt.this, SelectItems.class);
                startActivityForResult(intentDone, request);
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
}
