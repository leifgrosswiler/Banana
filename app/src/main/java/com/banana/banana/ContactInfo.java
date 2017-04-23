package com.banana.banana;

import android.app.Application;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by andrewjayzhou on 4/23/17.
 */

public class ContactInfo extends Application {

    private HashMap<String, String> info = new HashMap<>();
    // True = number
    private HashMap<String, Boolean> numberOrEmail = new HashMap<>();

    public HashMap<String, String> getList() {
        return info;
    }
    public void add(String name, String method, Boolean isNumeber) {
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

}
