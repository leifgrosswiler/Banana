package com.banana.banana;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static com.banana.banana.EditReceipt.ITEM_ID;
import static com.banana.banana.EditReceipt.PRICE_ID;

public class EditSpecifics extends AppCompatActivity {

    private EditText editName;
    private EditText editPrice;
    int position;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_specifics);

        // get intent
        Intent intent = getIntent();
        String item = intent.getStringExtra(ITEM_ID);
        final String price = intent.getStringExtra(PRICE_ID);

        position = intent.getIntExtra(MainReceipt.P_ID, -1);
        // set views
        editName = (EditText) findViewById(R.id.editName);
        editPrice = (EditText) findViewById(R.id.editPrice);
        editName.setText(item, TextView.BufferType.EDITABLE);
        editPrice.setText(price, TextView.BufferType.EDITABLE);

        // button
        final Button done = (Button) findViewById(R.id.editDone);
        if (isValid(price)) done.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    updateInfo(done, price);
                }
            });

    }

    public void updateInfo(Button done, String price){
        String newItem = editName.getText().toString();
        String newPrice = editPrice.getText().toString();

        if (!isValid(newPrice)) {
            Toast.makeText(getApplicationContext(),"Not a price...", Toast.LENGTH_LONG).show();
            return;
        }

        Order updated = new Order(newItem, newPrice);
        try {
            OrderData.set(position, updated);
        } catch(Exception e) {
            System.out.println("position is fucked up");
        }

        Intent intentUpdate = new Intent(EditSpecifics.this, MainReceipt.class);
        startActivity(intentUpdate);
    }

    public boolean isValid(String str) {

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
}

