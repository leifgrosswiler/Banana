package com.banana.banana;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SelectItems extends AppCompatActivity {

    public static final String NAME_ID = "com.banana.nameID";
    public static final String SELECTED_ID = "com.banana.selectedID";
    ListView listView;
    ArrayList<Order> dataModels;

    HashMap<String, List<String> > split = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_items);

        Intent intentReceive = getIntent();
        //String tmp = (String) intentReceive.getStringExtra(EditRecepit.DATAMODELS_ID);
        dataModels = (ArrayList<Order>) intentReceive.getSerializableExtra(EditReceipt.DATAMODELS_ID);

        // LISTVIEW
        listView = (ListView) findViewById(R.id.checkList);
        SelectItemsListAdapter adapter = new SelectItemsListAdapter(dataModels, getApplicationContext());
        listView.setAdapter(adapter);

        // Return to parent
        Button finish = (Button) findViewById(R.id.finish);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get user's name
                EditText tv = (EditText) findViewById(R.id.userName);
                String name = tv.getText().toString();

                EditText tv2 = (EditText) findViewById(R.id.userEmail);
                String email = tv2.getText().toString();

                ArrayList<String> emailAndItems = new ArrayList<>(Arrays.asList(email));

                // create boolean[] to store positions of checked items
                View v;
                CheckBox cb;
                boolean[] checked = new boolean[listView.getCount()];
                for (int i = 0; i < listView.getCount(); i++) {
                    v = listView.getChildAt(i);
                    cb = (CheckBox) v.findViewById(R.id.checkBox);
                    if(cb.isChecked()) {
                        checked[i] = true;
                        String selected = ((TextView) v.findViewById(R.id.list_item_select)).getText().toString();
                        String price = ((TextView) v.findViewById(R.id.price_select)).getText().toString();
                        //System.out.println(selected + " " + price);
                        emailAndItems.add(selected);
                        emailAndItems.add(price);
                    }
                    else {
                        checked[i] = false;
                    }
                }

                split.put(name, emailAndItems);

                for (String person : split.keySet()) {
                    System.out.println(person + " " + split.get(person));
                }
                System.out.print(split.size());

                Intent resultIntent = new Intent();

                resultIntent.putExtra(SELECTED_ID, checked);
                resultIntent.putExtra(NAME_ID, name);
                setResult(Activity.RESULT_OK,resultIntent);
                finish();

            }
        });
    }
}

