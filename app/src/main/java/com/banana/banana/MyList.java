package com.banana.banana;

import android.app.Application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by abbyvansoest on 4/11/17.
 */

public class MyList extends Application {

    private HashMap<String, List<String> > split = new HashMap<>();
    private String currentUser = "Master";
    private int dataLength;
    private HashMap<String,Integer> items = new HashMap<>();

    public HashMap<String, List<String>> getList() {
        return split;
    }

    // Restarts myList when going back to camera
    public void Restart() {
        split = new HashMap<>();
        currentUser = "Master";
        items = new HashMap<>();
        info = new HashMap<>();
        numberOrEmail = new HashMap<>();
        tracker = new HashMap<>();
        itemTracker = new HashMap<>();
        savePump = null;
        userTotals = null;
    }

    public void addPair(String name, List<String> stuff) {

        split.put(name, stuff);
        for (String menu : stuff) {
            if (items.containsKey(menu)) {
                int val = items.get(menu);
                items.put(menu,val+1);
            }
            else items.put(menu,1);
        }
    }

    public void printList() {
        for (String person : split.keySet()) {
            System.out.println(person + " " + split.get(person));
        }
        System.out.print(split.size());
    }


    // **Contacts Info**

    private HashMap<String, String> info = new HashMap<>();
    // True = number
    private HashMap<String, Boolean> numberOrEmail = new HashMap<>();

    public HashMap<String, String> getContactsList() {
        return info;
    }
    public void add(String name, String method, Boolean isNumeber) {
        System.out.println("Do adding");
        info.put(name, method);
        numberOrEmail.put(name, isNumeber);
    }

    public boolean isNumber(String name){
        return numberOrEmail.get(name);
    }

    public String getMethod(String name){
        return info.get(name);
    }

    public boolean contains(String name){
        return info.containsKey(name);
    }

    public Set<String> getUsers(){
        return info.keySet();
    }

    public void setUser(String name) {
        currentUser = name;
    }

    public String getUser() {
        return currentUser;
    }

    private static HashMap<String, Integer> itemTracker = new HashMap<>();
    public static void addBuyer(String item) {
        if (itemTracker.containsKey(item)) {
            int val = itemTracker.get(item);
            itemTracker.put(item, val+1);
        }
        else itemTracker.put(item, 1);
    }

    public static void removeBuyer(String item) {
        if (itemTracker.containsKey(item)) {
            int val = itemTracker.get(item);
            itemTracker.put(item, val-1);
        }
        else System.out.println("Error: item not in menu");
    }

    public static int numBuyers(String item) {
        return itemTracker.get(item);
    }

    // **Tracker**

    private static HashMap<String, boolean[]> tracker = new HashMap<>();
    //private static int length = OrderData.size();


    public static void addUser(String name, int length){
        boolean[] tmp = new boolean[length];
        for (int i = 0; i < length; i++){
            tmp[i] = false;
        }
        tracker.put(name, tmp);
        System.out.println("THIS IS THE REAL LENGTH:" + length);
    }

    public static void isSelected(String name, int p, boolean value){
        try {
            tracker.get(name)[p] = value;
        } catch (Exception e){
            System.out.println("shitt" + tracker.get(name).length);
        }

    }

    public static int numSelected(int p) {
        int count = 0;
        for (String name : tracker.keySet()) {
            if (tracker.get(name)[p]) count++;
        }
        return count;
    }

    public static boolean[] getTracker(String name){
        return tracker.get(name);
    }
    public static Set<String> getAllUsers() { return tracker.keySet(); }


    // Get Orders of Users
    public static ArrayList<Order> getUserOrders(String name){
        ArrayList<Order> ret = new ArrayList<>();
        boolean[] tmp = getTracker(name);
        for (int i = 0; i < tmp.length; i++){
            if (tmp[i])
                ret.add(OrderData.getAt(i));
        }

        return ret;
    }

    public static void notifyTrackerNewOrder(){
        for (String person : MyList.getAllUsers()) {
            boolean[] original = MyList.getTracker(person);
            boolean[] updated = new boolean[original.length + 1];
            for (int i = 0; i < original.length; i++){
                updated[i] = original[i];
            }
            updated[updated.length-1] = false;
            tracker.put(person, updated);
        }
    }

    public static void notifyTrackerOrderDeleted(int p){
        for (String person : MyList.getAllUsers()) {
            boolean[] original = MyList.getTracker(person);
            boolean[] updated = new boolean[original.length - 1];
            for (int i = 0; i < updated.length; i++){
                if (i < p)
                    updated[i] = original[i];
                else{
                    updated[i] = original[i+1];
                }
            }
            tracker.put(person, updated);
        }
    }

    private static HashMap<String, List<String>> savePump;

    public static void putPumpData(HashMap<String, List<String>> pump) {savePump = pump;}
    public static HashMap<String, List<String>> getPumpData() {return savePump;}

    private static HashMap<String, Double> userTotals;
    public static void putUserTotals(HashMap<String, Double> map) {userTotals = map;}
    public static Double getUserTotal(String name) {return userTotals.get(name);}

}
