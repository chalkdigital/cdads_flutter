package com.chalkdigital.network.response;

/**
 * Created by arungupta on 21/09/17.
 */

public class IPGeoLocationResponse {
    private String city;
    private String countryCode;
    private String regionName;
    private String postalCode;
    private String latitude;
    private String countryName;
    private String longitude;
    private String clientIp;

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(final String clientIp) {
        this.clientIp = clientIp;
    }

    public String getCity() {
        return nullCheck(city);
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountryCode() {
        return nullCheck(countryCode);
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getRegionName() {
        return nullCheck(regionName);
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getPostalCode() {
        return nullCheck(postalCode);
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getLatitude() {
        return nullCheck(latitude);
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getCountryName() {
        return nullCheck(countryName);
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getLongitude() {
        return nullCheck(longitude);
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    private String nullCheck(String value){
        if (value == null)
            return "";
        else
            return value;
    }
}
