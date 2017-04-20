package com.banana.banana;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrewjayzhou on 4/14/17.
 */

public class OrderData {

    private static String food[] = {"Pepperoni and Sausage Pizza", "Pasta", "Ice Cream Sandwitches", "Cheerios with Honey Oats", "Chicken Nuggets", "Tomato Soup", "Tomato Soup", "Tomato Soup", "Tomato Soup", "Tomato Soup"};
    private static String price[] = {"$15.74", "$1300.02", "$14.00", "$5.99", "$2.33", "$0.99", "$0.99", "$0.99", "$0.99", "$0.99","$0.99"};

    private static List<Order> data = new ArrayList<>();

    public static List<Order> getListData() {

        for (int i = 0; i < food.length; i++) {

            Order order = new Order(food[i], price[i]);
            data.add(order);
        }

        return data;
    }

    public static void delete(int p) {
        data.remove(p);
    }
    public static int size() {return food.length;}

}
