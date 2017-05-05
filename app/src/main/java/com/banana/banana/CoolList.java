package com.banana.banana;

import java.util.ArrayList;

/**
 * Created by Jacob on 5/4/17.
 */

public class CoolList extends ArrayList {

    ArrayList<String> alist;

    public CoolList() {
        alist = new ArrayList<String>();
    }

    // Special toString function for this class
    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < alist.size(); i++) {
            // Doesn't add if the item is null (this could only be the email address currently)
            if (alist.get(i) != null)
                s = s + alist.get(i) + "\n";
        }
        return s;
    }

    public void add(String e) {
        alist.add(e);
    }

    @Override
    public String get(int ind) {
        return alist.get(ind);
    }

}