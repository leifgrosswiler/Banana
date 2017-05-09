package com.banana.banana;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

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

        TextView title = (TextView) findViewById(R.id.name);
        title.setText("ADD A NEW ITEM");

        addName = (EditText) findViewById(R.id.addName);
        addPrice = (EditText) findViewById(R.id.addPrice);
        addName.setText("", EditText.BufferType.EDITABLE);
        addPrice.setText("", EditText.BufferType.EDITABLE);


//        // return back to EditReceipt
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
////                String newItems[] = new String[com.banana.banana.EditReceipt.food.length + 1];
////                String newPrices[] = new String[com.banana.banana.EditReceipt.price.length + 1];
////                for(int i = 0; i < com.banana.banana.EditReceipt.food.length; i++){
////                    newItems[i] = com.banana.banana.EditReceipt.food[i];
////                    newPrices[i] = com.banana.banana.EditReceipt.price[i];
////                }
////                newItems[newItems.length - 1] = newName;
////                newPrices[newItems.length - 1] = newPrice;
////                com.banana.banana.EditReceipt.food = newItems;
////                com.banana.banana.EditReceipt.price = newPrices;
//
//                OrderData.add(newName, newPrice);
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
                Intent intent = new Intent(AddOrder.this, com.banana.banana.MainReceipt.class);
                String newName = addName.getText().toString();
                String newPrice = addPrice.getText().toString();
                OrderData.add(newName, newPrice);
//                intent.putExtra(NEW_ID, name);
                startActivity(intent);

            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
