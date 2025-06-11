package com.chalkdigital.network.response;

import java.util.ArrayList;

/**
 * Created by arungupta on 03/01/17.
 */

public class Address {
    private ArrayList<AddressComponent> address_components;

    public ArrayList<AddressComponent> getAddress_components() {
        return address_components;
    }

    public void setAddress_components(ArrayList<AddressComponent> address_components) {
        this.address_components = address_components;
    }
}
