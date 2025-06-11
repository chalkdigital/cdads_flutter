package com.chalkdigital.network.response;

public class SSBBaseResponse {

    private String resultCode;
    private String resultMess;

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMess() {
        return resultMess;
    }

    public void setResultMess(String resultMess) {
        this.resultMess = resultMess;
    }
}
