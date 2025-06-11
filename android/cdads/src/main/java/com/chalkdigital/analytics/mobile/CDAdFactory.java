package com.chalkdigital.analytics.mobile;

import android.webkit.WebView;

public class CDAdFactory {

    public static CDAdFactory cdAdFactory = new CDAdFactory();


    private static void setCDAdFactory(final CDAdFactory cdAdFactory) {
        CDAdFactory.cdAdFactory = cdAdFactory;
    }

    public static CDAdFactory create(){
        if (cdAdFactory==null){
            cdAdFactory = new CDAdFactory();
        }
        return cdAdFactory;
    }

    public CDAdWebAdTracker createWebAdTracker(WebView webView, CDAdAnalyticsListener cdAdAnalyticsListener){
        return new CDAdWebAdTracker(webView, cdAdAnalyticsListener);
    }

}
