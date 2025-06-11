package com.chalkdigital.analytics.mobile;

import android.app.Application;

public class CDAdAnalytics {

    public enum CDAdVisibleArea{
        CDAdNotVisible,
        CDAdOneQuarterVisible,
        CDAdHalfVisible,
        CDAdThreeQuarterVisible,
        CDAdFullVisible;
    };

    protected static CDAdAnalytics cdAdAnalytics = new CDAdAnalytics();
    private Application mApplication;
    private CDAdOptions mCDAdOptions;

    public static void setCDAdAnalytics(final CDAdAnalytics cdAdAnalytics) {
        CDAdAnalytics.cdAdAnalytics = cdAdAnalytics;
    }

    public static CDAdAnalytics getInstance(){
        if (cdAdAnalytics==null){
            cdAdAnalytics = new CDAdAnalytics();
        }
        return cdAdAnalytics;
    }

    public void start(CDAdOptions cdAdOptions, Application application){
        mApplication = application;
        mCDAdOptions = cdAdOptions;
    }
}
