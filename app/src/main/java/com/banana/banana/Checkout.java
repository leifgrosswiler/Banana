package com.banana.banana;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import static com.banana.banana.EditReceipt.DATAMODELS_ID;

public class Checkout extends AppCompatActivity {

    public static final int request =  18;
    ListView listView;
    ArrayList<Order> dataModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        Intent intentReceive = getIntent();
        //String tmp = (String) intentReceive.getStringExtra(EditRecepit.DATAMODELS_ID);
        dataModels = (ArrayList<Order>) intentReceive.getSerializableExtra(DATAMODELS_ID);

        // LISTVIEW
        listView = (ListView) findViewById(R.id.checkOutList);
        EditReceiptListAdapter adapter = new EditReceiptListAdapter(dataModels, getApplicationContext());
        listView.setAdapter(adapter);

        // GMAIL BUTTON
        Button gmail = (Button) findViewById(R.id.gmail);
        gmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentGmail = new Intent(Checkout.this, MainActivity.class);
                startActivity(intentGmail);
            }
        });

        // DONE BUTTON
        Button done = (Button) findViewById(R.id.checkOutfinish);
        done.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intentDone = new Intent(Checkout.this, SelectItems.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(DATAMODELS_ID, dataModels);
                intentDone.putExtras(bundle);
                startActivityForResult(intentDone, request);
            }
        });
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case (request) : {

                String name = null;
                boolean[] selected = null;
                if (resultCode == Activity.RESULT_OK) {
                    name = (String) data.getStringExtra(SelectItems.NAME_ID);
                    selected = data.getBooleanArrayExtra(SelectItems.SELECTED_ID);
                }

                ((MyList) getApplication()).printList();

                for (int i = 0; i < listView.getCount(); i++) {
                    View v = listView.getChildAt(i);
                    TextView identifier = (TextView) v.findViewById(R.id.identifier);
                    if (name != null && selected[i] == true) {
                        identifier.setText(name);
                    }
                }

                break;

            }
        }
    }
}
