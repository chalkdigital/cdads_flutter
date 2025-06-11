package com.chalkdigital.common;

/**
 * Created by arungupta on 07/12/16.
 */

public class CDAdGeoInfo {

    private Float lat;
    private Float lon;
    private Integer type;
    private String countryCode;
    private String region;
    private String city;
    private String zip;
    private String accuracy;
    private long time;
    private String metro;

    public CDAdGeoInfo() {
        super();
        this.lat = null;
        this.lon = null;
        this.type = CDAdConstants.CDAdLocTypeUnavailable;
        this.countryCode = null;
        this.region = null;
        this.city = null;
        this.zip = null;
        this.accuracy = null;
        this.metro = null;
        this.time = System.currentTimeMillis();
    }

    /**
     * Get time at which this location is received in milliseconds.
     * @return long value of timestamp
     */
    public long getTime() {
        return time;
    }

    /**
     * Set time at which this location is received in milliseconds.
     */
    public void setTime(long time) {
        this.time = time;
    }

    /**
     * Get latitude of location.
     * @return float value of latitude
     */
    public Float getLat() {
        return lat;
    }

    /**
     * Set latitude of location.
     */
    public void setLat(Float lat) {
        this.lat = lat;
    }

    /**
     * Get longitude of location.
     * @return float value of longitude
     */
    public Float getLon() {
        return lon;
    }

    /**
     * Set longitude of location.
     */
    public void setLon(Float lon) {
        this.lon = lon;
    }

    /**
     * Get location type.
     * @return Integer value 1 for device, 2 for IP and 3 for user provided location
     */
    public Integer getType() {
        return type;
    }

    /**
     * Set location type.
     * @param type value 1 for device, 2 for IP and 3 for user provided location
     */
    public void setType(Integer type) {
        this.type = type;
    }

    /**
     * Get country code (Alpha-3 format)
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * Set country code (Alpha-3 format)
     */
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * Get region
     */
    public String getRegion() {
        return region;
    }

    /**
     * Set region
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * Get city name
     */
    public String getCity() {
        return city;
    }

    /**
     * Set city name
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Get zip code
     */
    public String getZip() {
        return zip;
    }

    /**
     * Set zip code
     */
    public void setZip(String zip) {
        this.zip = zip;
    }

    /**
     * Get horizontal accuracy of location
     */
    public String getAccuracy() {
        return accuracy;
    }

    /**
     * Set horizontal accuracy of location
     */
    public void setAccuracy(String accuracy) {
        this.accuracy = accuracy;
    }

    /**
     * Get metro code
     */
    public String getMetro() {
        return metro;
    }

    /**
     * Set metro code
     */
    public void setMetro(final String metro) {
        this.metro = metro;
    }
}
