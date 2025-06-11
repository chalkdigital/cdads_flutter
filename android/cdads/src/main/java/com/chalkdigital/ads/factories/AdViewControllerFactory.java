package com.chalkdigital.ads.factories;

import android.content.Context;

import com.chalkdigital.ads.CDAdView;
import com.chalkdigital.ads.AdViewController;

public class AdViewControllerFactory {
    protected static AdViewControllerFactory instance = new AdViewControllerFactory();

    @Deprecated // for testing
    public static void setInstance(AdViewControllerFactory factory) {
        instance = factory;
    }

    public static AdViewController create(Context context, CDAdView cdAdView) {
        return instance.internalCreate(context, cdAdView);
    }

    protected AdViewController internalCreate(Context context, CDAdView cdAdView) {
        return new AdViewController(context, cdAdView);
    }
}
