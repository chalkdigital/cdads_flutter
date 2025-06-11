package com.chalkdigital.network;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.chalkdigital.common.event.BaseEvent;
import com.chalkdigital.network.AdRequest.Listener;
import com.chalkdigital.network.retrofit.CDAdCallback;
import com.chalkdigital.network.retrofit.CDAdRetrofit;
import com.chalkdigital.network.retrofit.CDAdService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class TrackingRequest {

    // Retrying may cause duplicate impressions
    private static final int ZERO_RETRIES = 0;

//    public interface Listener extends Response.ErrorListener {
//        void onResponse(@NonNull String url);
//    }

//    @Nullable private final TrackingRequest.Listener mListener;

    private TrackingRequest(@NonNull final String url, @Nullable final Listener listener) {
    }

    public static void makeVastTrackingHttpRequest(){
    }

    public static void makeTrackingHttpRequest(@Nullable final Iterable<String> urls,
            @Nullable final Context context,
            @Nullable final Listener listener,
            final BaseEvent.Name name) {
        if (urls == null || context == null) {
            return;
        }

        String serverUrl = null;
        try {
            serverUrl = context.getResources().getText(context.getResources().getIdentifier("SERVER_URL", "string", context.getPackageName())).toString();
        } catch (Resources.NotFoundException e) {

        }

        if (serverUrl == null)
            return;

        for (final String url : urls) {
            if (TextUtils.isEmpty(url)) {
                continue;
            }

            CDAdService.performCall(CDAdRetrofit.getsharedSDKRequestAPIInstance(serverUrl+"/"), "logEvent", Object.class, new Class[]{String.class}, new Object[]{url}, new CDAdCallback<Object>(null, 0) {
                @Override
                public void onNetworkResponse(final Call<Object> call, final Response<Object> response, final Object object, final int apitype) {

                }

                @Override
                public void onNetworkFailure(final Call<Object> call, final Throwable t, final Object object, final int apitype) {

                }
            });
        }
    }

    public static void makeTrackingHttpRequest(@Nullable final String url,
            @Nullable final Context context) {
        makeTrackingHttpRequest(url, context, null, null);
    }

    public static void makeTrackingHttpRequest(@Nullable final String url,
            @Nullable final Context context, @Nullable Listener listener) {
        makeTrackingHttpRequest(url, context, listener, null);
    }

    public static void makeTrackingHttpRequest(@Nullable final String url,
            @Nullable final Context context, final BaseEvent.Name name) {
        makeTrackingHttpRequest(url, context, null, name);
    }

    public static void makeTrackingHttpRequest(@Nullable final String url,
            @Nullable final Context context,
            @Nullable Listener listener,
            final BaseEvent.Name name) {
        if (url != null) {
            makeTrackingHttpRequest(Arrays.asList(url), context, listener, name);
        }
    }

    public static void makeTrackingHttpRequest(@Nullable final Iterable<String> urls,
            @Nullable final Context context) {
        makeTrackingHttpRequest(urls, context, null, null);
    }

    public static void makeTrackingHttpRequest(@Nullable final Iterable<String> urls,
            @Nullable final Context context,
            final BaseEvent.Name name) {
        makeTrackingHttpRequest(urls, context, null, name);
    }
}
