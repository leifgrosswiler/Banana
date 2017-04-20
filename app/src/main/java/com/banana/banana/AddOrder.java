package com.banana.banana;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AddOrder extends AppCompatActivity {

    private EditText addName;
    private EditText addPrice;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_order);

        addName = (EditText) findViewById(R.id.addName);
        addPrice = (EditText) findViewById(R.id.addPrice);
        addName.setText("", EditText.BufferType.EDITABLE);
        addPrice.setText("", EditText.BufferType.EDITABLE);


        // return back to EditReceipt
        Button done = (Button) findViewById(R.id.addDone);
        done.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intentDone = new Intent(AddOrder.this, com.banana.banana.EditReceipt.class);

                // temporary way to update original array list
                String newName = addName.getText().toString();
                String newPrice = addPrice.getText().toString();
//                String newItems[] = new String[com.banana.banana.EditReceipt.food.length + 1];
//                String newPrices[] = new String[com.banana.banana.EditReceipt.price.length + 1];
//                for(int i = 0; i < com.banana.banana.EditReceipt.food.length; i++){
//                    newItems[i] = com.banana.banana.EditReceipt.food[i];
//                    newPrices[i] = com.banana.banana.EditReceipt.price[i];
//                }
//                newItems[newItems.length - 1] = newName;
//                newPrices[newItems.length - 1] = newPrice;
//                com.banana.banana.EditReceipt.food = newItems;
//                com.banana.banana.EditReceipt.price = newPrices;



                startActivity(intentDone);
                finish();
            }
        });

    }

}
