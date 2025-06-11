package com.chalkdigital.network.retrofit;

import com.chalkdigital.common.logging.CDAdLog;

import retrofit2.Call;
import retrofit2.Response;


public abstract class RetryableCallback<T> extends CDAdCallback<T> {

    private int totalRetries = 3;
    private final Call<T> call;
    private int retryCount = 0;

    public RetryableCallback(Call<T> call, int totalRetries, Object refObject, int apiType) {
        super(refObject, apiType);
        this.call = call;
        this.totalRetries = totalRetries;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (!isCallSuccess(response)){
            if (retryCount++ < totalRetries) {
                CDAdLog.v("Retrying API Call -  (" + retryCount + " / " + totalRetries + ")");
                retry();
            } else
                super.onResponse(call, response);
        }else{
            super.onResponse(call, response);
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        CDAdLog.e(t.getMessage());
        if (retryCount++ < totalRetries) {
            CDAdLog.v("Retrying API Call -  (" + retryCount + " / " + totalRetries + ")");
            retry();
        } else
            super.onFailure(call, t);
    }


    private void retry() {
        call.clone().enqueue(this);
    }

}
