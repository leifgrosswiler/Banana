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

    public ExpandableListDataPump(Activity act) {
        this.activity = act;
    }

    public HashMap<String, List<String>> getData(Set<String> userSet) {

        DecimalFormat df = new DecimalFormat("###.##");

        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();

        for (String s : userSet) {

            double totalprice = 0;

            List<String> user = new ArrayList<>();
            List<Order> orders = ((MyList) activity.getApplication()).getUserOrders(s);
            for (Order order : orders) {
                StringBuilder sb = new StringBuilder("");
                double pp = Double.parseDouble(order.getPricePP(MyList.numBuyers(order.getItem())));
                String priceper = df.format(pp);
                sb.append("\n" + order.getItem() + "\t$" + priceper);
                totalprice += Double.parseDouble(priceper);
                user.add(sb.toString());

                String tp = df.format(totalprice);
                user.add("Total:\t$" + tp);
                expandableListDetail.put(s, user);
            }
        }

        return expandableListDetail;
    }
}
