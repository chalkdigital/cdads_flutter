package com.chalkdigital.analytics.mobile;

import android.view.View;

import com.chalkdigital.common.CDAdConstants;

public class CDAdWebAdTracker extends CDAdBaseViewabilityTracker{

    public CDAdWebAdTracker(final View view, CDAdAnalyticsListener cdAdAnalyticsListener) {
        super(view, cdAdAnalyticsListener);
    }

    @Override
    protected long getViewabilitySuccessIntervalMillis() {
        return 1000;
    }

    @Override
    protected float getViewabilitySuccessVisibilityRatio() {
        return CDAdConstants.CDAdViewabilitySuccessVisibilityRatio;
    }



}
