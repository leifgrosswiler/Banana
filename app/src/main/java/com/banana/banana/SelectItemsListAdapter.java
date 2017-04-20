package com.banana.banana;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by andrewjayzhou on 4/10/17.
 */

public class SelectItemsListAdapter extends ArrayAdapter implements View.OnClickListener{

    private ArrayList<OrderOld> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView txtItem;
        TextView txtPrice;
        CheckBox checkBox;
    }

    public SelectItemsListAdapter(ArrayList<OrderOld> data, Context context) {

        super(context, R.layout.row_select_items, data);
        this.dataSet = data;
        this.mContext=context;
    }

    @Override
    public void onClick(View v) {

        int position=(Integer) v.getTag();
        Object object= getItem(position);
        OrderOld dataModel=(OrderOld) object;

        switch (v.getId())
        {
            case R.id.price:
                break;
        }
    }

    private int lastPosition = -1;
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        OrderOld dataModel = (OrderOld) getItem(position);
        final ViewHolder viewHolder;

        final View result;

        if (convertView == null){
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_select_items, parent, false);
            viewHolder.txtItem = (TextView) convertView.findViewById(R.id.list_item_select);
            viewHolder.txtPrice = (TextView) convertView.findViewById(R.id.price_select);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);


            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

//        Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
//        result.startAnimation(animation);
//        lastPosition = position;

        viewHolder.txtItem.setText(dataModel.getItem());
        viewHolder.txtPrice.setText(dataModel.getPrice());
        viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                final boolean isChecked = viewHolder.checkBox.isChecked();
                // Do something here.
            }
        });

//        viewHolder.info.setOnClickListener(this);
//        viewHolder.info.setTag(position);

        return convertView;
    }
}
