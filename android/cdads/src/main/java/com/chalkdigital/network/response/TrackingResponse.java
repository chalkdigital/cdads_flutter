package com.chalkdigital.network.response;

import java.util.HashMap;

/**
 * Created by arungupta on 03/01/17.
 */

public class TrackingResponse{

    private long ADfetchInterval;
    private long TrackingInterval;
    private float DistanceFilter;
    private int MinBGTime;
    private int AcceptableAccuracy;
    private int ReverseGeocodeDistanceFilter;
    private int MaxLocationManagerRunningInterval;
    private long AdLocationExpiryInterval;
    private int sdkkeyid;
    private String defaultAdServerUrl;
    private HashMap<String, String> countrySpecificAdUrl;

    public String getDefaultAdServerUrl() {
        return defaultAdServerUrl;
    }

    public void setDefaultAdServerUrl(String defaultAdServerUrl) {
        this.defaultAdServerUrl = defaultAdServerUrl;
    }

    public HashMap<String, String> getCountrySpecificAdUrl() {
        return countrySpecificAdUrl;
    }

    public void setCountrySpecificAdUrl(HashMap<String, String> countrySpecificAdUrl) {
        this.countrySpecificAdUrl = countrySpecificAdUrl;
    }

    public long getADfetchInterval() {
        return ADfetchInterval;
    }

    public void setADfetchInterval(long ADfetchInterval) {
        this.ADfetchInterval = ADfetchInterval;
    }

    public long getTrackingInterval() {
        return TrackingInterval;
    }

    public void setTrackingInterval(long trackingInterval) {
        TrackingInterval = trackingInterval;
    }

    public float getDistanceFilter() {
        return DistanceFilter;
    }

    public void setDistanceFilter(float distanceFilter) {
        DistanceFilter = distanceFilter;
    }

    public int getMinBGTime() {
        return MinBGTime;
    }

    public void setMinBGTime(int minBGTime) {
        MinBGTime = minBGTime;
    }

    public int getAcceptableAccuracy() {
        return AcceptableAccuracy;
    }

    public void setAcceptableAccuracy(int acceptableAccuracy) {
        AcceptableAccuracy = acceptableAccuracy;
    }

    public int getReverseGeocodeDistanceFilter() {
        return ReverseGeocodeDistanceFilter;
    }

    public void setReverseGeocodeDistanceFilter(int reverseGeocodeDistanceFilter) {
        ReverseGeocodeDistanceFilter = reverseGeocodeDistanceFilter;
    }

    public int getMaxLocationManagerRunningInterval() {
        return MaxLocationManagerRunningInterval;
    }

    public void setMaxLocationManagerRunningInterval(int maxLocationManagerRunningInterval) {
        MaxLocationManagerRunningInterval = maxLocationManagerRunningInterval;
    }

    public long getAdLocationExpiryInterval() {
        return AdLocationExpiryInterval;
    }

    public void setAdLocationExpiryInterval(long adLocationExpiryInterval) {
        AdLocationExpiryInterval = adLocationExpiryInterval;
    }

    public int getSdkkeyid() {
        return sdkkeyid;
    }

    public void setSdkkeyid(int sdkkeyid) {
        this.sdkkeyid = sdkkeyid;
    }

}
