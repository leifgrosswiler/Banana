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
        if (alist.size() > 0)
            return alist.get(0);
        else
            return "";
    }

    public void add(String e) {
        alist.add(e);
    }

    @Override
    public String get(int ind) {
        return alist.get(ind);
    }

    @Override
    public int size() {
        return alist.size();
    }

}