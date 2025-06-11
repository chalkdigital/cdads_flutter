package com.chalkdigital.common;

import android.location.Location;
import android.location.LocationManager;

/**
 * Created by arungupta on 27/12/17.
 */

public final class CDAdLocation {
    private String provider;
    private float accuracy;
    private double latitude;
    private double longitude;
    private double altitude;
    private long time;
    private long dwellTime;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public CDAdLocation(Location l) {
        this.accuracy = l.getAccuracy();
        this.latitude = l.getLatitude();
        this.longitude = l.getLongitude();
        this.time = l.getTime();
        this.provider = l.getProvider();
        this.altitude = l.getAltitude();
    }

    public CDAdLocation() {
    }

    public Location toLocation(){
        Location l = new Location(LocationManager.PASSIVE_PROVIDER);
        l.setLatitude(this.latitude);
        l.setLongitude(this.longitude);
        l.setTime(this.time);
        l.setAccuracy(this.accuracy);
        l.setAltitude(this.altitude);
        l.setProvider(this.provider);
        return l;
    }

    public float distanceTo(Location dest) {
        return toLocation().distanceTo(dest);
    }

    public float distanceTo(CDAdLocation dest) {
        return toLocation().distanceTo(dest.toLocation());
    }

    public long getDwellTime() {
        return dwellTime;
    }

    public void setDwellTime(long dwellTime) {
        this.dwellTime = dwellTime;
    }
}
