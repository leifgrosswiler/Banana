package com.banana.banana;

import android.app.Activity;

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

        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();

        for (String s : userSet) {

            int totalprice = 0;

            List<String> user = new ArrayList<>();
            List<Order> orders = ((MyList) activity.getApplication()).getUserOrders(s);
            for (Order order : orders) {
                StringBuilder sb = new StringBuilder("");
                sb.append("\n" + order.getItem() + "\t$" + order.getPricePP(MyList.numBuyers(order.getItem())));
                totalprice += Double.parseDouble(order.getPricePP(MyList.numBuyers(order.getItem())));
                user.add(sb.toString());

                user.add("Total:\t$" + totalprice);
                expandableListDetail.put(s, user);
            }
        }

        return expandableListDetail;
    }
}
