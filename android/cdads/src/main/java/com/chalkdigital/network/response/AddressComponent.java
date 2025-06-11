package com.chalkdigital.network.response;

import java.util.ArrayList;

/**
 * Created by arungupta on 03/01/17.
 */

public class AddressComponent {
    private ArrayList<String> types;
    private String short_name;
    private String long_name;

    public ArrayList<String> getTypes() {
        return types;
    }

    public void setTypes(ArrayList<String> types) {
        this.types = types;
    }

    public String getShort_name() {
        return short_name;
    }

    public void setShort_name(String short_name) {
        this.short_name = short_name;
    }

    public String getLong_name() {
        return long_name;
    }

    public void setLong_name(String long_name) {
        this.long_name = long_name;
    }
}
