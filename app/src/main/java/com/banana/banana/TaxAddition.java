package com.banana.banana;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.CharArrayReader;
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
    private TextView subtotal;
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
                pump.rmTipTax();

                HashMap<String, Double> finalPrices = pump.getTotalPrices();
                double total = pump.getTotal();

                String taxStr = editTax.getText().toString();
                if (!isValid(taxStr)) {
                    Toast.makeText(getApplicationContext(),"Invalid Tax Value!", Toast.LENGTH_SHORT).show();
                    taxStr = "0";
                }
                if (taxStr.isEmpty()) taxStr = "0";
                double tax = Double.parseDouble(taxStr)/100.*total;

                // get list of perc
                // add tip tax proportionately to each
                HashMap<String, Double> perc = new HashMap<String, Double>();
                for (String name : finalPrices.keySet()) {
                    if (total == 0) perc.put(name,0.0);
                    else perc.put(name, finalPrices.get(name)/total);
                }

                String tipStr = editTip.getText().toString();
                if (!isValid(tipStr)) {
                    Toast.makeText(getApplicationContext(),"Invalid Tip Value!", Toast.LENGTH_SHORT).show();
                    tipStr = "0";
                }
                if (tipStr.isEmpty()) tipStr = "0";

                if (!pump.tipTaxAdd) pump.addTipTax(tipStr, tax, perc);

                Intent finalIntent = new Intent(TaxAddition.this, MainActivity.class);
                if (isValid(editTip.getText().toString()) && isValid(editTax.getText().toString()))
                    startActivity(finalIntent);

            }
        });

    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        startActivity(new Intent(this, MainReceipt.class));
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