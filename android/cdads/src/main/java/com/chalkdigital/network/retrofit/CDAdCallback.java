package com.chalkdigital.network.retrofit;

import com.chalkdigital.common.util.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public abstract class CDAdCallback<T> implements Callback<T> {

    private final Object refObject;
    private final int apiType;

    public CDAdCallback(Object refObject, int apiType) {
        this.refObject = refObject;
        this.apiType = apiType;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        try {
            onNetworkResponse(call, response, refObject, apiType);
        } catch (Throwable throwable) {
            Utils.logStackTrace(throwable);
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        try {
            onNetworkFailure(call, t, refObject, apiType);
        } catch (Throwable throwable) {
            Utils.logStackTrace(throwable);
        }
    }

    public static boolean isCallSuccess(Response response) {
        int code = response.code();
        return (code >= 200 && code < 400 && response.body()!=null);
    }

    public abstract void onNetworkResponse(Call<T> call, Response<T> response, Object object, int apitype);

    public abstract void onNetworkFailure(Call<T> call, Throwable t, Object object, int apitype);
}
