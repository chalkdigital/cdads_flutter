package com.chalkdigital.network.retrofit;

import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.Utils;
import com.chalkdigital.network.retrofit.service.CDAdApi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by arungupta on 22/06/16.
 */
public class CDAdService {
    public static <T> Call<T> performRequest(Retrofit retrofit,
                                       String declaredMethodName, final Class<T> returnObjectType, Class[] classArgs , Object[] valueArgs, final CDAdCallbackListener callbackListener,
                                       final Object object, final int apitype){
        Call<T> call = null;
        try {
            final CDAdApi api = retrofit.create(CDAdApi.class);
            Class[] cArg = new Class[0];
            Method declaredMethod = CDAdApi.class.getDeclaredMethod(declaredMethodName, classArgs);
            call = (Call<T>) declaredMethod.invoke(api, valueArgs);
            CDAdCallback<T> callback = new CDAdCallback<T>(object, apitype) {

                @Override
                public void onNetworkResponse(Call<T> call, Response<T> response, Object object, int apitype) {
                    CDAdLog.d("retrofit service", "cdAd_service_success");
                    callbackListener.onNetworkRequestSuccess(call, response, object, apitype);
                }

                @Override
                public void onNetworkFailure(Call<T> call, Throwable t, Object object, int apitype) {
                    CDAdLog.d("retrofit service", "cdAd_service_failure");
                    callbackListener.onNetworkRequestFailure(call, t, object, apitype);
                }
            };
            call.enqueue(callback);
        } catch (NoSuchMethodException e) {
                        Utils.logStackTrace(e);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
                        Utils.logStackTrace(e);
            e.printStackTrace();
        } catch (InvocationTargetException e) {
                        Utils.logStackTrace(e);
            e.printStackTrace();
        } finally {
        }
        return call;
    }

    public static <T> Call<T> performCall(Retrofit retrofit,
                                             String declaredMethodName, final Class<T> returnObjectType, Class[] classArgs , Object[] valueArgs,
                                             final CDAdCallback<T> callback){
        Call<T> call = null;
        try {
            final CDAdApi api = retrofit.create(CDAdApi.class);
            Class[] cArg = new Class[0];
            Method declaredMethod = CDAdApi.class.getDeclaredMethod(declaredMethodName, classArgs);
            call = (Call<T>) declaredMethod.invoke(api, valueArgs);
            call.enqueue(callback);
        } catch (NoSuchMethodException e) {
                        Utils.logStackTrace(e);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
                        Utils.logStackTrace(e);
            e.printStackTrace();
        } catch (InvocationTargetException e) {
                        Utils.logStackTrace(e);
            e.printStackTrace();
        } finally {
        }
        return call;
    }

}
