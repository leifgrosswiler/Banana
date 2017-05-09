package com.banana.banana;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static com.banana.banana.EditReceipt.ITEM_ID;
import static com.banana.banana.EditReceipt.PRICE_ID;

public class TaxAddition extends AppCompatActivity  {

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
        editTax.setText("", TextView.BufferType.EDITABLE);
        editTip.setText("", TextView.BufferType.EDITABLE);


        // button
        final Button done =(Button) findViewById(R.id.editDone);
        Set<String> names = ((MyList) getApplication()).getUsers();
        final ExpandableListDataPump pump = new ExpandableListDataPump(this, names);
        done.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // get list of final prices and total price

                pump.getData();
                HashMap<String, Double> finalPrices = pump.getTotalPrices();
                double total = pump.getTotal();

                double tax = Double.parseDouble(editTax.getText().toString())/100.*total;

                // get list of perc
                // add tip tax proportionately to each
                HashMap<String, Double> perc = new HashMap<String, Double>();
                for (String name : finalPrices.keySet()) {
                    perc.put(name, finalPrices.get(name)/total);
                }

                pump.addTipTax(editTip.getText().toString(), tax, perc);

                Intent finalIntent = new Intent(TaxAddition.this, MainActivity.class);
                startActivity(finalIntent);

            }
        });

    }

}