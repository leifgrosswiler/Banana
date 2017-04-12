package com.banana.banana;

import android.app.Application;
import java.util.HashMap;
import java.util.List;

/**
 * Created by abbyvansoest on 4/11/17.
 */

public class MyList extends Application {

    private HashMap<String, List<String> > split = new HashMap<>();

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
}


