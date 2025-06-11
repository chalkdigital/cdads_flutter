package com.chalkdigital.interstitial.ads.factories;

import com.chalkdigital.common.AdReport;
import com.chalkdigital.common.CDAdSize;
import com.chalkdigital.interstitial.ads.CDAdInterstitial;
import com.chalkdigital.interstitial.ads.CustomEventInterstitialAdapter;
import com.chalkdigital.mediation.CDMediationAdRequest;
import com.chalkdigital.network.response.Event;

import java.util.HashMap;
import java.util.Map;

public class CustomEventInterstitialAdapterFactory {
    protected static CustomEventInterstitialAdapterFactory instance = new CustomEventInterstitialAdapterFactory();

    @Deprecated // for testing
    public static void setInstance(CustomEventInterstitialAdapterFactory factory) {
        instance = factory;
    }

    public static CustomEventInterstitialAdapter create(CDAdInterstitial cdAdInterstitial, String className, Map<String, String> serverExtras, long broadcastIdentifier, CDMediationAdRequest cdMediationAdRequest, CDAdSize cdAdSize, Event[] events) {
        return instance.internalCreate(cdAdInterstitial, className, serverExtras, broadcastIdentifier, cdMediationAdRequest, cdAdSize, events);
    }

    protected CustomEventInterstitialAdapter internalCreate(CDAdInterstitial cdAdInterstitial, String className, Map<String, String> serverExtras, long broadcastIdentifier, CDMediationAdRequest cdMediationAdRequest, CDAdSize cdAdSize, Event[] events) {
        return new CustomEventInterstitialAdapter(cdAdInterstitial, className, broadcastIdentifier, events, cdMediationAdRequest, cdAdSize, serverExtras);
    }
}
