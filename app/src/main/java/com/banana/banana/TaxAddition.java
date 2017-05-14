package com.banana.banana;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.HashMap;
import java.util.Set;

import static com.banana.banana.R.menu.app_bar2_menu;


public class TaxAddition extends AppCompatActivity  {

    private EditText editTax;
    private EditText editTip;
    private TextView subtotalS;
    private TextView taxS;
    private TextView totalS;
    private boolean taxByPercent = true;
    private ToggleButton taxToggle;
    private Toolbar toolbar;
    private ExpandableListDataPump pump;

    int position;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tax);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((TextView) toolbar.findViewById(R.id.name)).setText("TAX AND TIP");

        // set views
        editTax = (EditText) findViewById(R.id.editTax);
        editTip = (EditText) findViewById(R.id.editTip);
        subtotalS = (TextView) findViewById(R.id.subTotal);
        taxS = (TextView) findViewById(R.id.tax);
        totalS = (TextView) findViewById(R.id.total);
        taxToggle = (ToggleButton) findViewById(R.id.toggleButton);

        editTax.setText("", TextView.BufferType.EDITABLE);
        editTip.setText("", TextView.BufferType.EDITABLE);
        subtotalS.setText("$" + OrderData.getTotal());

        if (taxToggle.isChecked()) {
            editTax.setHint("Tax (%)");
            taxByPercent = true;
        }
        else {
            editTax.setHint("Tax ($)");
            taxByPercent = false;
        }

        taxToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editTax.setHint("Tax (%)");
                    taxByPercent = true;
                } else {
                    editTax.setHint("Tax ($)");
                    taxByPercent = false;
                }
                if (editTax.getText().toString().isEmpty())
                    updateTotal(true);
                else
                    updateTotal(false);
            }
        });


        Set<String> names = ((MyList) getApplication()).getUsers();
        pump = new ExpandableListDataPump(this, names);
        editTax.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateTotal(false);
            }
        });

    }

    private void updateTotal(boolean noToast) {
        double tax;
        double total = OrderData.getTotal();
        String taxStr = editTax.getText().toString();
        if (!isValid(taxStr)) {
            if (!noToast)
                Toast.makeText(getApplicationContext(),"Invalid Tax Value!", Toast.LENGTH_SHORT).show();
            taxStr = "0";
        }
        if (taxStr.isEmpty()) taxStr = "0";
        if (taxByPercent)
            tax = ((double)Math.round(Double.parseDouble(taxStr)*total))/100;
        else
            tax = ((double)Math.round(Double.parseDouble(taxStr)*100))/100;
        taxS.setText("$" + tax);
        totalS.setText("$" + (tax + total));
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        startActivity(new Intent(this, MainReceipt.class));
    }

    public boolean isValid(String str) {
        if (str.equals("") || str.equals(".")) return false;
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
                pump.getData();
                pump.rmTipTax();

                HashMap<String, Double> finalPrices = pump.getTotalPrices();
                //double total = pump.getTotal();
                double total = OrderData.getTotal();
                String taxStr = editTax.getText().toString();
                if (!isValid(taxStr)) {
                    Toast.makeText(getApplicationContext(),"Invalid Tax Value!", Toast.LENGTH_SHORT).show();
                    taxStr = "0";
                }
                if (taxStr.isEmpty()) taxStr = "0";

                double tax;
                if (taxByPercent)
                    tax = ((double)Math.round(Double.parseDouble(taxStr)*total))/100;
                else
                    tax = ((double)Math.round(Double.parseDouble(taxStr)*100))/100;

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

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}