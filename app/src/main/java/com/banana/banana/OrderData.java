package com.banana.banana;

import java.util.ArrayList;
import java.util.List;

import static com.banana.banana.OpenCamera.parseResult;

/**
 * Created by andrewjayzhou on 4/14/17.
 */

public class OrderData {

    private static String[] food;
    private static String[] price;
    private static List<Order> data = new ArrayList<>();


    private static int length;
    public static List<Order> getListData() { return data; }
    public static int size() {
        return data.size();
    }
    public static Order getAt(int p){return data.get(p); };
    public static void set(int p, Order updated){ data.set(p, updated); MainReceipt.updateTotal();}

    public static void delete(int p) {

        data.remove(p);
        MainReceipt.adapter.notifyItemRemoved(p);

        MyList.notifyTrackerOrderDeleted(p);
        MainReceipt.updateTotal();
    }
    public static void setFoodAndPrice() {
        ArrayList<String> tempFood = new ArrayList<>();
        ArrayList<String> tempPrice = new ArrayList<>();

        for(List<String> parsedLine : parseResult) {
            String tmp = parsedLine.get(0);
            if (tmp.length() > 20){
                tmp = tmp.substring(0,19) + "...";
            }
            tempFood.add(tmp);
            tempPrice.add(parsedLine.get(2));
        }
        food = new String[tempFood.size()];
        price = new String[tempPrice.size()];
        food = tempFood.toArray(food);
        price = tempPrice.toArray(price);

        for (int i = 0; i < food.length; i++) {

            Order order = new Order(food[i], price[i],i,0);
            data.add(order);
        }

        System.out.println("ATTENTION: " + data.size());

    }
    public static void add(String item, String price, int p){
        int tmp = data.size();
        Order order = new Order(item, price, p,0);
        data.add(order);

        // update trackers
        MyList.notifyTrackerNewOrder();

        MainReceipt.adapter.notifyItemInserted(tmp);
        MainReceipt.updateTotal();
    }
    public static void DeleteWholeOrder() {
        data = new ArrayList<>();
        food = null;
        price = null;
        MainReceipt.updateTotal();
    }
    public static double getTotal() {
        double total = 0;
        for(Order order : data) {
            total += Double.parseDouble(order.getPrice());
        }
        total = ((double) Math.round(total * 100)) / 100;
        return total;
    }
}
