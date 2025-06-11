package com.chalkdigital.interstitial.ads.factories;

import android.content.Context;

import com.chalkdigital.common.AdReport;
import com.chalkdigital.interstitial.ads.HtmlInterstitialWebView;

import static com.chalkdigital.interstitial.ads.CustomEventInterstitial.CustomEventInterstitialListener;

public class HtmlInterstitialWebViewFactory {
    protected static HtmlInterstitialWebViewFactory instance = new HtmlInterstitialWebViewFactory();

    public static HtmlInterstitialWebView create(
            Context context,
            AdReport adReport,
            CustomEventInterstitialListener customEventInterstitialListener,
            boolean isScrollable,
            String redirectUrl,
            String clickthroughUrl,
            String clickAction) {
        return instance.internalCreate(context, adReport, customEventInterstitialListener, isScrollable, redirectUrl, clickthroughUrl, clickAction);
    }

    public HtmlInterstitialWebView internalCreate(
            Context context,
            AdReport adReport,
            CustomEventInterstitialListener customEventInterstitialListener,
            boolean isScrollable,
            String redirectUrl,
            String clickthroughUrl,
            String clickAction) {
        HtmlInterstitialWebView htmlInterstitialWebView = new HtmlInterstitialWebView(context, adReport);
        htmlInterstitialWebView.init(customEventInterstitialListener, isScrollable, redirectUrl, clickthroughUrl, null, clickAction);
        return htmlInterstitialWebView;
    }

    @Deprecated // for testing
    public static void setInstance(HtmlInterstitialWebViewFactory factory) {
        instance = factory;
    }
}
