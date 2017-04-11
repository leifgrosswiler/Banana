package com.banana.banana;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by andrewjayzhou on 4/10/17.
 */

public class Order implements Parcelable {
    String item;
    String price;

    public Order(String item, String price) {
        this.item = item;
        this.price = price;
    }

    public String getItem() {
        return item;
    }

    public String getPrice() {
        return price;
    }

    // The following methods that are required for using Parcelable
    private Order(Parcel in) {
        // This order must match the order in writeToParcel()
        item = in.readString();
        price = in.readString();
        // Continue doing this for the rest of your member data
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(item);
        out.writeString(price);
    }

    // Just cut and paste this for now
    public int describeContents() {
        return 0;
    }

    // Just cut and paste this for now
    public static final Parcelable.Creator<Order> CREATOR = new Parcelable.Creator<Order>() {
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }

        public Order[] newArray(int size) {
            return new Order[size];
        }
    };
}

