package com.chalkdigital.banner.ads.factories;

import android.content.Context;

import com.chalkdigital.banner.ads.HtmlBannerWebView;
import com.chalkdigital.common.AdReport;

import static com.chalkdigital.banner.ads.CustomEventBanner.CustomEventBannerListener;

public class HtmlBannerWebViewFactory {
    protected static HtmlBannerWebViewFactory instance = new HtmlBannerWebViewFactory();

    public static HtmlBannerWebView create(
            Context context,
            AdReport adReport,
            CustomEventBannerListener customEventBannerListener,
            boolean isScrollable,
            String redirectUrl,
            String clickthroughUrl,
            String clickAction) {
        return instance.internalCreate(context, adReport, customEventBannerListener, isScrollable, redirectUrl, clickthroughUrl, clickAction);
    }

    public HtmlBannerWebView internalCreate(
            Context context,
            AdReport adReport,
            CustomEventBannerListener customEventBannerListener,
            boolean isScrollable,
            String redirectUrl,
            String clickthroughUrl,
            String clickAction) {
        HtmlBannerWebView htmlBannerWebView = new HtmlBannerWebView(context, adReport);
        htmlBannerWebView.init(customEventBannerListener, isScrollable, redirectUrl, clickthroughUrl, null, clickAction);
        return htmlBannerWebView;
    }

    @Deprecated // for testing
    public static void setInstance(HtmlBannerWebViewFactory factory) {
        instance = factory;
    }
}

