package com.chalkdigital.network.response;

/**
 * Created by arungupta on 28/12/16.
 */

public class TrackingRequestResponse extends BaseResponse {

    private TrackingResponse response;

    public TrackingResponse getResponse() {
        return response;
    }

    public void setResponse(TrackingResponse response) {
        this.response = response;
    }
}
