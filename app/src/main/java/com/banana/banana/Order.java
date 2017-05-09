package com.banana.banana;

import java.text.DecimalFormat;

/**
 * Created by andrewjayzhou on 4/14/17.
 */

public class Order {

    private String item;
    private String price;
    //private ArrayList<String> users;


    public Order(String item, String price) {
        this.item = item;
        this.price = price;
//        users = new ArrayList<>();
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getItem() {
        return item;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPrice() {
        return price;
    }

    public String getPricePP(int num) {
        double intPrice = Double.parseDouble(price);
        double pricePP = intPrice/(double)num;
        return Double.toString(pricePP);
    }


}
