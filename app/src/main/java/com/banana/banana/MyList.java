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

    public HashMap<String, List<String>> getList() {
        return split;
    }
    public void addPair(String name, List<String> stuff) {
        split.put(name, stuff);
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

    public static boolean[] getTracker(String name){
        return tracker.get(name);
    }

    // TODO: test this
//    public static void removeItem(int p){
//        for (String name : tracker.keySet()) {
//            boolean[] tmp = tracker.get(name);
//            boolean[] tmp2 = new boolean[tmp.length-1];
//            for (int i = 0; i < tmp2.length; i++){
//                if (i < p)
//                    tmp2[i] = tmp[i];
//                else
//                    tmp2[i] = tmp[i+1];
//            }
//            tracker.put(name, tmp2);
//        }
//
//    }

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



}


