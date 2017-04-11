package com.banana.banana;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class SelectItems extends AppCompatActivity {

    public static final String NAME_ID = "com.banana.nameID";
    public static final String SELECTED_ID = "com.banana.selectedID";
    ListView listView;
    ArrayList<Order> dataModels;

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

                // create boolean[] to store positions of checked items
                View v;
                CheckBox cb;
                boolean[] checked = new boolean[listView.getCount()];
                for (int i = 0; i < listView.getCount(); i++){
                    v = listView.getChildAt(i);
                    cb = (CheckBox) v.findViewById(R.id.checkBox);
                    if(cb.isChecked()){
                        checked[i] = true;
                    }
                    else{
                        checked[i] = false;
                    }
                }
                Intent resultIntent = new Intent();

                resultIntent.putExtra(SELECTED_ID, checked);
                resultIntent.putExtra(NAME_ID, name);
                setResult(Activity.RESULT_OK,resultIntent);
                finish();

            }
        });
    }
}

