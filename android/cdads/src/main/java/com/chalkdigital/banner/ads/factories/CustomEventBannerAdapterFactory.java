package com.chalkdigital.banner.ads.factories;

import android.support.annotation.NonNull;

import com.chalkdigital.ads.CDAdView;
import com.chalkdigital.banner.ads.CustomEventBannerAdapter;
import com.chalkdigital.common.AdReport;
import com.chalkdigital.common.CDAdSize;
import com.chalkdigital.mediation.CDMediationAdRequest;
import com.chalkdigital.network.response.Event;

import java.util.HashMap;
import java.util.Map;

public class CustomEventBannerAdapterFactory {
    protected static CustomEventBannerAdapterFactory instance = new CustomEventBannerAdapterFactory();

    @Deprecated // for testing
    public static void setInstance(CustomEventBannerAdapterFactory factory) {
        instance = factory;
    }

    public static CustomEventBannerAdapter create(@NonNull CDAdView advAdView,
                                                  @NonNull String className,
                                                  @NonNull Map<String, String> serverExtras,
                                                  long broadcastIdentifier,
                                                  CDAdSize cdAdSize,
                                                  CDMediationAdRequest advMediationAdRequest, @NonNull final Event[] events) {
        return instance.internalCreate(advAdView, className, serverExtras, broadcastIdentifier, cdAdSize, advMediationAdRequest, events);
    }

    protected CustomEventBannerAdapter internalCreate(@NonNull CDAdView advAdView,
                                                      @NonNull String className,
                                                      @NonNull Map<String, String> serverExtras,
                                                      long broadcastIdentifier,
                                                      CDAdSize cdAdSize,
                                                      CDMediationAdRequest advMediationAdRequest, @NonNull final Event[] events) {
        return new CustomEventBannerAdapter(advAdView, className, events, broadcastIdentifier, cdAdSize, advMediationAdRequest, serverExtras);
    }
}
