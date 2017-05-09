package com.banana.banana;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import static com.banana.banana.EditReceipt.ITEM_ID;
import static com.banana.banana.EditReceipt.PRICE_ID;

public class TaxAddition extends AppCompatActivity {

    private EditText editTax;
    private EditText editTip;
    int position;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tax);


        // set views
        editTax = (EditText) findViewById(R.id.editTax);
        editTip = (EditText) findViewById(R.id.editTip);
        editTax.setText("15", TextView.BufferType.EDITABLE);
        editTip.setText("20", TextView.BufferType.EDITABLE);

        // button
        final Button done =(Button) findViewById(R.id.editDone);
        done.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
               Intent finalIntent = new Intent(TaxAddition.this, MainActivity.class);
                startActivity(finalIntent);
            }
        });

    }


}

