package com.chalkdigital.ads.factories;

import android.content.Context;

import com.chalkdigital.ads.CDAdView;
import com.chalkdigital.common.VisibleForTesting;

public class CDAdViewFactory {
    protected static CDAdViewFactory instance = new CDAdViewFactory();

    @VisibleForTesting
    @Deprecated
    public static void setInstance(CDAdViewFactory factory) {
        instance = factory;
    }

    public static CDAdView create(Context context) {
        return instance.internalCreate(context);
    }

    protected CDAdView internalCreate(Context context) {
        return new CDAdView(context);
    }
}
