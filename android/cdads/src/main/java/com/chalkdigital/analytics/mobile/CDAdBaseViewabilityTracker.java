package com.chalkdigital.analytics.mobile;

import android.graphics.Rect;
import android.view.View;

import com.chalkdigital.common.CDAdConstants;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CDAdBaseViewabilityTracker {
    final private static float CDViewabilitySuccessVisibilityRatio = 0.5f;
    protected long mViewVisibilityStartTime;
    protected List<View> mNonBlockingOverlayViews;
    protected View view;
    protected Rect windowFrame;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private CDAdAnalytics.CDAdVisibleArea mCDAdVisibleArea;
    private CDAdAnalytics.CDAdVisibleArea mChangedCDAdVisibleArea;
    private CDAdAnalyticsListener mCDAdAnalyticsListener;

    public CDAdBaseViewabilityTracker(final View view, CDAdAnalyticsListener cdAdAnalyticsListener) {
        this.view = view;
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                trackVisibility();
            }
        };
        mCDAdVisibleArea = CDAdAnalytics.CDAdVisibleArea.CDAdNotVisible;
        mChangedCDAdVisibleArea = CDAdAnalytics.CDAdVisibleArea.CDAdNotVisible;
        mCDAdAnalyticsListener = cdAdAnalyticsListener;
    }


    protected void trackVisibility(){
        if (view.hasWindowFocus() && view.getVisibility()==View.VISIBLE){
            Rect r = new Rect();
            boolean isVisible = view.getGlobalVisibleRect(r);
            CDAdAnalytics.CDAdVisibleArea cdAdVisibleArea = CDAdAnalytics.CDAdVisibleArea.CDAdNotVisible;
            if (isVisible){
                float visibleArea = r.height()*r.width();
                float adArea = view.getHeight()*view.getWidth();
                double visibleRatio = (visibleArea/adArea*100.0)/25.0;
                switch ((int)visibleRatio){
                    case 0:
                        cdAdVisibleArea = CDAdAnalytics.CDAdVisibleArea.CDAdNotVisible;
                        break;
                    case 1:
                        cdAdVisibleArea = CDAdAnalytics.CDAdVisibleArea.CDAdOneQuarterVisible;
                        break;
                    case 2:
                        cdAdVisibleArea = CDAdAnalytics.CDAdVisibleArea.CDAdHalfVisible;
                        break;
                    case 3:
                        cdAdVisibleArea = CDAdAnalytics.CDAdVisibleArea.CDAdThreeQuarterVisible;
                        break;
                    case 4:
                        cdAdVisibleArea = CDAdAnalytics.CDAdVisibleArea.CDAdFullVisible;
                        break;
                }
                if (cdAdVisibleArea == mChangedCDAdVisibleArea && mChangedCDAdVisibleArea!=mCDAdVisibleArea && cdAdVisibleArea != CDAdAnalytics.CDAdVisibleArea.CDAdNotVisible){
                    mCDAdVisibleArea = mChangedCDAdVisibleArea;
                    postViewabilityEvent(cdAdVisibleArea);
                    if (mCDAdVisibleArea == CDAdAnalytics.CDAdVisibleArea.CDAdFullVisible)
                        stopTracking();
                }else{
                    mChangedCDAdVisibleArea = cdAdVisibleArea;
                }
            }
        }
    }

    protected void postViewabilityEvent(CDAdAnalytics.CDAdVisibleArea cdAdVisibleArea){
        if (mCDAdAnalyticsListener!=null)
            mCDAdAnalyticsListener.cdAdViewabilityChanged(view, cdAdVisibleArea);
    }

    protected float visibleAreaNotOverlappedByAnyOtherViewWithAreaRatio(float visibleAreaRatio){
        return 0.0f;
    }

    protected long getViewabilitySuccessIntervalMillis(){
        return 1000;
    }

    protected float getViewabilitySuccessVisibilityRatio(){
        return CDAdConstants.CDAdViewabilitySuccessVisibilityRatio;
    }

    public void startTracking(){
        mTimer.scheduleAtFixedRate(mTimerTask, 0, (long) getViewabilitySuccessIntervalMillis());
        if (mCDAdAnalyticsListener!=null)
            mCDAdAnalyticsListener.cdAdAnalyticsStarted(view);
    }

    public void stopTracking(){
        mTimer.purge();
        mTimer.cancel();
        if (mCDAdAnalyticsListener!=null)
            mCDAdAnalyticsListener.cdAdAnalyticsStopped(view);
    }


}
