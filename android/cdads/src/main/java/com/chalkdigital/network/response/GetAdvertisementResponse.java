package com.chalkdigital.network.response;

import com.chalkdigital.model.Advertisement;

import java.util.ArrayList;

public class GetAdvertisementResponse extends SSBBaseResponse {

    private ArrayList<Advertisement> ads;

    public ArrayList<Advertisement> getAds() {
        return ads;
    }

    public void setAds(ArrayList<Advertisement> ads) {
        this.ads = ads;
    }
}
