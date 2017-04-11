package com.banana.banana;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
        String price = intent.getStringExtra(PRICE_ID);
        position = (int) intent.getIntExtra(EditReceipt.POSITION_ID, -1);

        // set views
        editName = (EditText) findViewById(R.id.editName);
        editPrice = (EditText) findViewById(R.id.editPrice);
        editName.setText(item, TextView.BufferType.EDITABLE);
        editPrice.setText(price, TextView.BufferType.EDITABLE);

        // button
        final Button done =(Button) findViewById(R.id.editDone);
        done.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                updateInfo(done);
            }
        });

    }

    public void updateInfo(Button done){
        String newItem = editName.getText().toString();
        String newPrice = editPrice.getText().toString();
        EditReceipt.food[position] = newItem;
        EditReceipt.price[position] = newPrice;

        Intent intentUpdate = new Intent(EditSpecifics.this, EditReceipt.class);
        startActivity(intentUpdate);
    }
}

