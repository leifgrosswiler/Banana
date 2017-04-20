package com.banana.banana;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import static com.banana.banana.MainReceipt.ITEM_ID;
import static com.banana.banana.MainReceipt.PRICE_ID;

/**
 * Created by andrewjayzhou on 4/14/17.
 */

public class ReceiptAdapter extends RecyclerView.Adapter<ReceiptAdapter.ReceiptHolder> {


    public static final int DEFAULT = 0;
    public static final int USER = 1;

    private List<Order> listData;
    private LayoutInflater inflater;
    private int mode = DEFAULT;


    @Override
    public ReceiptHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new ReceiptHolder(view);
    }

    @Override
    public void onBindViewHolder(ReceiptHolder holder, int position) {
        Order item = listData.get(position);
        holder.item.setText(item.getItem());
        holder.price.setText(item.getPrice());
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    //    public class ReceiptHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public class ReceiptHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView item;
        private TextView price;
        private View container;
        private View avatar;

        public ReceiptHolder(View itemView) {
            super(itemView);

            item = (TextView) itemView.findViewById(R.id.list_item);
            price = (TextView) itemView.findViewById(R.id.price);
            container = itemView.findViewById(R.id.cont_item_root);
            avatar = itemView.findViewById(R.id.avatar);
            avatar.setVisibility(View.GONE);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            if (mode == DEFAULT) {
                int p = getLayoutPosition();
                Order item = (Order) OrderData.getListData().get(p);

                Intent intent = new Intent(view.getContext(), EditSpecifics.class);

                intent.putExtra(item.getItem(), ITEM_ID);
                intent.putExtra(item.getPrice(), PRICE_ID);

                view.getContext().startActivity(intent);
            } else {
                int p = getLayoutPosition();
                if (view.isSelected()) {
                    view.setSelected(false);
                    avatar.setVisibility(View.GONE);
                    Tracker.isSelected("Andrew", p, false);

                }
                else {
                    view.setSelected(true);
                    avatar.setVisibility(View.VISIBLE);
                    Tracker.isSelected("Andrew", p, true);
                    System.out.println(Tracker.getTracker("Andrew")[p]);
                }
            }
        }


    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public ReceiptAdapter(List<Order> listData, Context c) {
        inflater = LayoutInflater.from(c);
        this.listData = listData;
    }

}

