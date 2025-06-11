package com.chalkdigital.network.response;

import java.util.ArrayList;

/**
 * Created by arungupta on 03/01/17.
 */

public class ReverseGeocodeResponse {

    private ArrayList<Address> results;

    public ArrayList<Address> getResults() {
        return results;
    }

    public void setResults(ArrayList<Address> results) {
        this.results = results;
    }
}
