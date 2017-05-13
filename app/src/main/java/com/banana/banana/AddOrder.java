package com.banana.banana;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static com.banana.banana.R.menu.app_bar2_menu;

public class AddOrder extends AppCompatActivity {

    private EditText addName;
    private EditText addPrice;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_order);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((TextView) toolbar.findViewById(R.id.name)).setText("ADD NEW ITEM");
        invalidateOptionsMenu();
        addName = (EditText) findViewById(R.id.addName);
        addPrice = (EditText) findViewById(R.id.addPrice);
        addName.setText("", EditText.BufferType.EDITABLE);
        addPrice.setText("", EditText.BufferType.EDITABLE);


        // return back to EditReceipt
//        Button done = (Button) findViewById(R.id.addDone);
//        done.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//                Intent intentDone = new Intent(AddOrder.this, com.banana.banana.MainReceipt.class);
//
//                // temporary way to update original array list
//                String newName = addName.getText().toString();
//                String newPrice = addPrice.getText().toString();
//
//                if (EditReceipt.food == null) OrderData.add(newName, newPrice, 0);
//                else OrderData.add(newName, newPrice, com.banana.banana.EditReceipt.food.length + 1);
//
//
//                startActivity(intentDone);
//                finish();
//            }
//        });

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
                Intent intentDone = new Intent(AddOrder.this, com.banana.banana.MainReceipt.class);
                // temporary way to update original array list
                String newName = addName.getText().toString();
                String newPrice = addPrice.getText().toString();

                if (!EditSpecifics.isValid(newPrice)) {
                    Toast.makeText(getApplicationContext(), "Not a price...", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (EditReceipt.food == null) OrderData.add(newName, newPrice, 0);
                else
                    OrderData.add(newName, newPrice, com.banana.banana.EditReceipt.food.length + 1);
                startActivity(intentDone);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
