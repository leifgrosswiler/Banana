package com.banana.banana;

import android.app.Activity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by abbyvansoest on 5/3/17.
 */

public class ExpandableListDataPump {

    public Activity activity;
    private HashMap<String,Double> totalPrices = new HashMap<>();
    private HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();


    public ExpandableListDataPump(Activity act, Set<String> userSet) {

        this.activity = act;
        this.expandableListDetail = new HashMap<>();

        DecimalFormat df = new DecimalFormat("###.##");

        for (String s : userSet) {

            double totalprice = 0;

            List<String> user = new ArrayList<>();
            List<Order> orders = ((MyList) activity.getApplication()).getUserOrders(s);
            for (Order order : orders) {
                StringBuilder sb = new StringBuilder("");
                int pos = order.getPos();
                int count = ReceiptAdapter.indexes[pos];
                Double pp = Double.parseDouble(order.getPricePP());
                String priceper = df.format(pp);
                sb.append("\n" + order.getItem() + "\t$" + priceper);
                totalprice += Double.parseDouble(priceper);
                user.add(sb.toString());
            }
            totalPrices.put(s,totalprice);
            String tp = df.format(totalprice);
            user.add("\nTotal:\t$" + tp);
            expandableListDetail.put(s, user);
        }
    }

    public HashMap<String, List<String>> getData() {
        return expandableListDetail;
    }

    public String getTotal(String user) {
        double totalprice = 0;
        DecimalFormat df = new DecimalFormat("###.##");
        List<Order> orders = ((MyList) activity.getApplication()).getUserOrders(user);
        for (Order order : orders) {
            StringBuilder sb = new StringBuilder("");
            Double pp = Double.parseDouble(order.getPricePP());
            String priceper = df.format(pp);
            totalprice += Double.parseDouble(priceper);
        }
        return df.format(totalprice);
    }

    public void addTipTax(String tip, double tax, HashMap<String,Double> perc) {

        DecimalFormat df = new DecimalFormat("###.##");
        for (String user : MyList.getAllUsers()) {
            double p = perc.get(user);
            double tipprop = p* Integer.parseInt(tip);
            double taxprop = p*tax;

            List<String> userOrder = expandableListDetail.get(user);
            expandableListDetail.remove(user);
            // remove total entry
            for (String s : userOrder) {
                if (s.contains("Total:")) {
                    userOrder.remove(s);
                }
            }
            userOrder.add("Tip: $"+df.format(tipprop));
            userOrder.add("Tax: $"+df.format(taxprop));

            double total = totalPrices.get(user);
            total += tipprop+taxprop;
            totalPrices.remove(user);
            totalPrices.put(user,total);

            userOrder.add("Total: $"+df.format(total));

            expandableListDetail.put(user,userOrder);
        }

        ((MyList) activity.getApplication()).putPumpData(expandableListDetail);
        ((MyList) activity.getApplication()).putUserTotals(totalPrices);
    }


    public HashMap<String,Double> getTotalPrices() {
        return totalPrices;
    }

    public double getTotal() {
        double total = 0.0;
        for (Double t : totalPrices.values()) total += t;
        return total;
    }
}
