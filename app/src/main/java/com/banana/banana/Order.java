package com.banana.banana;

import java.text.DecimalFormat;

/**
 * Created by andrewjayzhou on 4/14/17.
 */

public class Order {

    private String item;
    private String price;
    private int p;
    private int ppl;
    //private ArrayList<String> users;


    public Order(String item, String price, int p, int ppl) {
        this.item = item;
        this.price = price;
        this.p = p;
        this.ppl = ppl;
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

    public String getPricePP() {
        double intPrice = Double.parseDouble(price);
        double pricePP = intPrice/(double)ppl;
        return Double.toString(pricePP);
    }

    public int getPos() {return this.p;}

    public int getNumPpl() {return this.ppl;}

    public void addUser() {this.ppl = this.ppl + 1;}
    public void rmUser() {this.ppl = this.ppl - 1;}

}
