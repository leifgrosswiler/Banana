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
                Intent intentDone = new Intent(AddOrder.this, com.banana.banana.MainReceipt.class);

                // temporary way to update original array list
                String newName = addName.getText().toString();
                String newPrice = addPrice.getText().toString();

                if (EditReceipt.food == null) OrderData.add(newName, newPrice, 0);
                else OrderData.add(newName, newPrice, com.banana.banana.EditReceipt.food.length + 1);


                startActivity(intentDone);
                finish();
            }
        });

    }

}
