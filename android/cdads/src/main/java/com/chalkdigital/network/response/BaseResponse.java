package com.chalkdigital.network.response;

/**
 * Created by arungupta on 28/12/16.
 */

public class BaseResponse {
    private int ecode;
    private String edesc;
    private int status;


    public int getEcode() {
        return ecode;
    }

    public void setEcode(int ecode) {
        this.ecode = ecode;
    }

    public String getEdesc() {
        return edesc;
    }

    public void setEdesc(String edesc) {
        this.edesc = edesc;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    public BaseResponse() {
    }

}
