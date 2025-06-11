package com.chalkdigital.network.response;

/**
 * Created by arungupta on 21/09/17.
 */

public class IPGeoLocationRequestResponse extends BaseResponse {
    private IPGeoLocationResponse response;

    public IPGeoLocationResponse getResponse() {
        return response;
    }

    public void setResponse(IPGeoLocationResponse response) {
        this.response = response;
    }
}
