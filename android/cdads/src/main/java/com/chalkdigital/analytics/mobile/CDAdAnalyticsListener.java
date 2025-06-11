package com.chalkdigital.analytics.mobile;

import android.view.View;

public interface CDAdAnalyticsListener {

    public void cdAdViewabilityChanged(View view, CDAdAnalytics.CDAdVisibleArea cdAdVisibleArea);
    public void cdAdAnalyticsStarted(View view);
    public void cdAdAnalyticsStopped(View view);

}
