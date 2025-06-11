package com.chalkdigital.network.retrofit;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by arungupta on 21/06/16.
 */
public interface CDAdCallbackListener {

    public void onNetworkRequestSuccess(Call call, Response response, Object object, int apitype);

    public void onNetworkRequestFailure(Call call, Throwable t, Object object, int apitype);
}
