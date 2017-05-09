package com.banana.banana;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import static com.banana.banana.EditReceipt.ITEM_ID;
import static com.banana.banana.EditReceipt.PRICE_ID;
import static com.banana.banana.R.menu.app_bar2_menu;

public class EditSpecifics extends AppCompatActivity {

    private EditText editName;
    private EditText editPrice;
    int position;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_specifics);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        TextView title = (TextView) findViewById(R.id.name);
        title.setText("EDIT ITEM");


        // get intent
        Intent intent = getIntent();
        String item = intent.getStringExtra(ITEM_ID);
        String price = intent.getStringExtra(PRICE_ID);
        position = intent.getIntExtra(MainReceipt.P_ID, -1);

        // set views
        editName = (EditText) findViewById(R.id.editName);
        editPrice = (EditText) findViewById(R.id.editPrice);
        editName.setText(item, TextView.BufferType.EDITABLE);
        editPrice.setText(price, TextView.BufferType.EDITABLE);
    }

        // button
//        final Button done =(Button) findViewById(R.id.editDone);
//        done.setOnClickListener(new View.OnClickListener() {
//
////            @Override
////            public void onClick(View view) {
////                updateInfo(done);
////            }
////        });
//


//
//    public void updateInfo(Button done){
//        String newItem = editName.getText().toString();
//        String newPrice = editPrice.getText().toString();
//
//        Order updated = new Order(newItem, newPrice);
//        try {
//            OrderData.set(position, updated);
//        } catch(Exception e) {
//            System.out.println("position is fucked up");
//        }
//
//
//        Intent intentUpdate = new Intent(EditSpecifics.this, MainReceipt.class);
//        startActivity(intentUpdate);
//    }

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
                String newItem = editName.getText().toString();
                String newPrice = editPrice.getText().toString();

                Order updated = new Order(newItem, newPrice);
                try {
                    OrderData.set(position, updated);
                } catch(Exception e) {
                    System.out.println("position is fucked up");
                }


                Intent intentUpdate = new Intent(EditSpecifics.this, MainReceipt.class);
                startActivity(intentUpdate);

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

