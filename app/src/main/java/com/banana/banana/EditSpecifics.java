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

import static com.banana.banana.EditReceipt.ITEM_ID;
import static com.banana.banana.EditReceipt.PRICE_ID;
import static com.banana.banana.R.menu.app_bar2_menu;

public class EditSpecifics extends AppCompatActivity {

    private EditText editName;
    private EditText editPrice;
    int position;
    private Toolbar toolbar;
    private String price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_specifics);
        System.out.println("---------------------------starting----------------------");
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((TextView) toolbar.findViewById(R.id.name)).setText("Edit Order");

        // get intent
        Intent intent = getIntent();
        String item = intent.getStringExtra(ITEM_ID);
        price = intent.getStringExtra(PRICE_ID);

        position = intent.getIntExtra(MainReceipt.P_ID, -1);
        // set views
        editName = (EditText) findViewById(R.id.editName);
        editPrice = (EditText) findViewById(R.id.editPrice);
        editName.setText(item, TextView.BufferType.EDITABLE);
        editPrice.setText(price, TextView.BufferType.EDITABLE);


        // button
//        final Button done = (Button) findViewById(R.id.editDone);
//        if (isValid(price)) done.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//                updateInfo(done, price);
//            }
//        });

    }

//    public void updateInfo(Button done, String price){
    public void updateInfo(){
        String newItem = editName.getText().toString();
        String newPrice = editPrice.getText().toString();

        if (!isValid(newPrice)) {
            Toast.makeText(getApplicationContext(),"Not a price...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newItem.isEmpty() || newItem.trim().isEmpty()) {
            Toast.makeText(getApplicationContext(),"Order needs a name...", Toast.LENGTH_SHORT).show();
            return;
        }

        Order updated = new Order(newItem, newPrice, position,OrderData.getAt(position).getNumPpl());
        try {
            OrderData.set(position, updated);
        } catch(Exception e) {
            System.out.println("position is incorrect");
        }

        Intent intentUpdate = new Intent(EditSpecifics.this, MainReceipt.class);
        startActivity(intentUpdate);
    }

    public static boolean isValid(String str) {
        if (str.equals(""))
            return false;
        for (char c : str.toCharArray()) {
            // if any c is not a digit
            if (!Character.isDigit(c)) {
                // and not a proper punctuation mark, return false
                if (c != '.' && c != ',') return false;
            }
        }
        // otherwise return true
        return true;
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
                updateInfo();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

