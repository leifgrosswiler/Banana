package com.banana.banana;

import java.util.ArrayList;
import java.util.List;

import static com.banana.banana.OpenCamera.parseResult;

/**
 * Created by andrewjayzhou on 4/14/17.
 */

public class OrderData {

    private static String[] food;// = {"Pepperoni and Sausage Pizza", "Pasta", "Ice Cream Sandwitches", "Cheerios with Honey Oats", "Chicken Nuggets", "Tomato Soup", "Tomato Soup", "Tomato Soup", "Tomato Soup", "Tomato Soup"};
    private static String[] price;// = {"$15.74", "$1300.02", "$14.00", "$5.99", "$2.33", "$0.99", "$0.99", "$0.99", "$0.99", "$0.99","$0.99"};
    private static List<Order> data = new ArrayList<>();

    public static List<Order> getListData() { return data; }

    public static void delete(int p) {
        data.remove(p);
    }
    public static int size() {return food.length;}

    public static void setFoodAndPrice() {
        ArrayList<String> tempFood = new ArrayList<>();
        ArrayList<String> tempPrice = new ArrayList<>();

        for(List<String> parsedLine : parseResult) {
            tempFood.add(parsedLine.get(1));
            tempPrice.add(parsedLine.get(2));
        }
        food = new String[tempFood.size()];
        price = new String[tempPrice.size()];
        food = tempFood.toArray(food);
        price = tempPrice.toArray(price);

        for (int i = 0; i < food.length; i++) {

            Order order = new Order(food[i], price[i]);
            data.add(order);
        }
    }

    public static void add(String item, String price){
        Order order = new Order(item, price);
        data.add(order);
        MainReceipt.adapter.notifyDataSetChanged();
    }

}
