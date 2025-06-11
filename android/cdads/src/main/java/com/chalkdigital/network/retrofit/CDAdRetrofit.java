package com.chalkdigital.network.retrofit;

import com.chalkdigital.common.CDAdConstants;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by arungupta on 20/06/16.
 */
public class CDAdRetrofit {
    private static Retrofit sharedSDKRequestRetrofit;
    private static Retrofit sharedDebugRequestRetrofit;

    public static Retrofit getsharedSDKRequestAPIInstance(String baseUrl){
        return getsharedSDKRequestAPIInstance(baseUrl, null);
    }

    public static Retrofit getsharedSDKRequestAPIInstance(String baseUrl, File cacheDir){
        if (sharedSDKRequestRetrofit == null) {
            sharedSDKRequestRetrofit = CDAdRetrofit.createSharedInstance(baseUrl);
        }
        return sharedSDKRequestRetrofit;
    }

    public static Retrofit getsharedDebugRequestAPIInstance(String baseUrl){
        if (sharedDebugRequestRetrofit == null) {
            sharedDebugRequestRetrofit = CDAdRetrofit.createSharedInstance(baseUrl);
        }
        return sharedDebugRequestRetrofit;
    }


    public static Retrofit createInstance(String baseUrl, final Map<String, String> requestHeaders){
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.readTimeout(CDAdConstants.CONSTANT_READ_TIMEOUT_INTERVAL, TimeUnit.SECONDS);
        builder.connectTimeout(CDAdConstants.CONSTANT_CONNECTION_TIMEOUT_INTERVAL, TimeUnit.SECONDS);
        if (requestHeaders!=null){
            builder.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Interceptor.Chain chain) throws IOException {
                    Request.Builder builder = chain.request().newBuilder();
                    for (String key: requestHeaders.keySet()) {
                        builder.addHeader(key, requestHeaders.get(key));
                    }
                    builder.method(chain.request().method(), chain.request().body());
                    Request request = builder.build();
                    return chain.proceed(request);
                }
            });
        }
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        builder.addNetworkInterceptor(interceptor);
        return new Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create()).client(builder.build()).build();
    }

    public static Retrofit createSharedInstance(String baseUrl, File cacheDir){

        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
            builder.readTimeout(CDAdConstants.CONSTANT_READ_TIMEOUT_INTERVAL, TimeUnit.SECONDS);
            builder.connectTimeout(CDAdConstants.CONSTANT_CONNECTION_TIMEOUT_INTERVAL, TimeUnit.SECONDS);
        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Request.Builder builder = chain.request().newBuilder();
                Map<String, String> requestHeaders = getHeaders();
                for (String key: requestHeaders.keySet()) {
                    builder.addHeader(key, requestHeaders.get(key));
                }
                builder.method(chain.request().method(), chain.request().body());
                Request request = builder.build();
                return chain.proceed(request);
            }
        });
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addNetworkInterceptor(interceptor);
            if (cacheDir!=null){
                int cacheSize = 10 * 1024 * 1024; // 10 MB
                Cache cache = new Cache(cacheDir, cacheSize);
                builder.cache(cache);
            }
            return new Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create()).client(builder.build()).build();
    }

    public static Retrofit createSharedInstance(String baseUrl){
        return createSharedInstance(baseUrl, null);
    }

    public static Retrofit createAdRequestAPIInstance(String baseUrl){
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.readTimeout(CDAdConstants.CONSTANT_READ_TIMEOUT_INTERVAL, TimeUnit.SECONDS);
        builder.connectTimeout(CDAdConstants.CONSTANT_CONNECTION_TIMEOUT_INTERVAL, TimeUnit.SECONDS);
        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Request.Builder builder = chain.request().newBuilder();
                Map<String, String> requestHeaders = getHeaders();
                for (String key: requestHeaders.keySet()) {
                    builder.addHeader(key, requestHeaders.get(key));
                }
                builder.method(chain.request().method(), chain.request().body());
                Request request = builder.build();
                return chain.proceed(request);
            }
        });
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        builder.addNetworkInterceptor(interceptor);
        return new Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(StringConverterFactory.create()).client(builder.build()).build();
    }

    public static Map<String, String> getHeaders() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Device-OS", "Android");
        map.put("Content-Type", "application/json");
        return map;
    }


}
