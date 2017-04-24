package com.banana.banana;

import android.app.Application;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by abbyvansoest on 4/11/17.
 */

public class MyList extends Application {

    private HashMap<String, List<String> > split = new HashMap<>();
    private String currentUser = "Master";

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

    // Below is stuff for contacts info

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

}


