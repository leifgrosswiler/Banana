package com.banana.banana;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

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
//                intent.putExtra(NEW_ID, name);
                startActivity(intent);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

