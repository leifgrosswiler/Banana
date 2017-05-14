package com.banana.banana;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import static com.banana.banana.MainReceipt.ITEM_ID;
import static com.banana.banana.MainReceipt.PRICE_ID;
import static com.banana.banana.MainReceipt.P_ID;

/**
 * Created by andrewjayzhou on 4/14/17.
 */

public class ReceiptAdapter extends RecyclerView.Adapter<ReceiptAdapter.ReceiptHolder> {


    public static final int DEFAULT = 0;
    public static final int USER = 1;
    public static final int PICK_ITEMS = 2;

    private List<Order> listData;
    private LayoutInflater inflater;
    private int mode = DEFAULT;
    private MyList app;
    private String user;

    @Override
    public ReceiptHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new ReceiptHolder(view);
    }

    @Override
    public void onBindViewHolder(ReceiptHolder holder, int position) {
        Order item = listData.get(position);
        String food = item.getItem();
        if (food.length() > 20){
            food = food.substring(0,19) + "...";
        }
        holder.item.setText(food);
        holder.price.setText(item.getPrice());

        holder.payer.setText("");
        for (String person : MyList.getAllUsers()) {
            if (MyList.getTracker(person)[position])
                if (holder.payer.getText() == "")
                    holder.payer.setText(person);
                else
                    holder.payer.setText(holder.payer.getText() + ", " + person);
        }

        // set background color in PickItems
        if (mode == PICK_ITEMS){
            if (MyList.getTracker(user)[position]){
                holder.card.setCardBackgroundColor(Color.parseColor("#FF8A65"));
                holder.payer.setTextColor(Color.parseColor("#FFFFFF"));
            }
        }
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public final static int[] indexes = new int[100];
    public static int[] getIndexCounts() {
        return indexes;
    }

    //    public class ReceiptHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public class ReceiptHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView item;
        private TextView price;
        private View container;
        private CardView card;
        private TextView payer;

        public ReceiptHolder(View itemView) {
            super(itemView);

            item = (TextView) itemView.findViewById(R.id.list_item);
            price = (TextView) itemView.findViewById(R.id.price);
            container = itemView.findViewById(R.id.cont_item_root);
            card = (CardView) itemView.findViewById(R.id.card);
            payer = (TextView) itemView.findViewById(R.id.payer);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            String user = app.getUser();

            if (mode == DEFAULT) {
                int p = getLayoutPosition();

                Order item = (Order) OrderData.getListData().get(p);

                Intent intent = new Intent(view.getContext(), EditSpecifics.class);

                intent.putExtra(ITEM_ID, item.getItem());
                intent.putExtra(PRICE_ID, item.getPrice());
                intent.putExtra(P_ID, p);

                view.getContext().startActivity(intent);
            } else if (mode == PICK_ITEMS){

                // count number of people that have picked p??
                int p = getLayoutPosition();
                Order item = (Order) OrderData.getListData().get(p);

                boolean[] tracker = MyList.getTracker(user);

                if (!tracker[p]) {
                    MyList.isSelected(user, p, true);
                    card.setCardBackgroundColor(Color.parseColor("#FF8A65"));
                    payer.setTextColor(Color.parseColor("#FFFFFF"));
                    System.out.println("1");
                    item.addUser();
                    int num = item.getNumPpl();
                    System.out.println("NUMBER     "+ num);
                }
                else {
                    MyList.isSelected(user, p, false);
                    card.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
                    payer.setTextColor(Color.parseColor("#FF8A65"));
                    item.rmUser();
                    int num = item.getNumPpl();
                    System.out.println("NUMBER     "+ num);
                }
            }
        }
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setUser(String user) {this.user = user; }

    public ReceiptAdapter(List<Order> listData, Context c, MyList application) {
        inflater = LayoutInflater.from(c);
        this.listData = listData;
        this.app = application;
    }

}

