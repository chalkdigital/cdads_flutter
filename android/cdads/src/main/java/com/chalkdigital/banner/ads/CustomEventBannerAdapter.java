package com.chalkdigital.banner.ads;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.chalkdigital.ads.CDAdErrorCode;
import com.chalkdigital.ads.CDAdView;
import com.chalkdigital.banner.ads.CustomEventBanner.CustomEventBannerListener;
import com.chalkdigital.banner.ads.factories.CustomEventBannerFactory;
import com.chalkdigital.common.AdReport;
import com.chalkdigital.common.CDAdConstants;
import com.chalkdigital.common.CDAdSize;
import com.chalkdigital.common.Constants;
import com.chalkdigital.common.DataKeys;
import com.chalkdigital.common.Preconditions;
import com.chalkdigital.common.VisibleForTesting;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.Reflection;
import com.chalkdigital.common.util.ReflectionTarget;
import com.chalkdigital.common.util.Utils;
import com.chalkdigital.mediation.CDMediationAdRequest;
import com.chalkdigital.mediation.MediationConstants;
import com.chalkdigital.network.TrackingRequest;
import com.chalkdigital.network.response.Event;
import com.chalkdigital.network.response.TypeParser;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static com.chalkdigital.ads.CDAdErrorCode.ADAPTER_NOT_FOUND;
import static com.chalkdigital.ads.CDAdErrorCode.NETWORK_TIMEOUT;
import static com.chalkdigital.ads.CDAdErrorCode.UNSPECIFIED;

public class CustomEventBannerAdapter implements CustomEventBannerListener {
    public static final int DEFAULT_BANNER_TIMEOUT_DELAY = Constants.SIXTY_SECONDS_MILLIS;
    private boolean mInvalidated;
    private CDAdView mCDAdView;
    private Context mContext;
    private CustomEventBanner mCustomEventBanner;
    private Map<String, String> mServerExtras;

    private final Handler mHandler;
    private final Runnable mTimeout;
    private int mEventIndex;
    private Runnable mEventTimeout;
    private boolean mStoredAutorefresh;
    private int[] mEventCheckArray;

    private CDAdSize mRequestedAdSize;
    private CDMediationAdRequest mCdMediationAdRequest;
    private String mClassName;
    private String mBaseSdkEventUrl;

    private int mImpressionMinVisibleDips = Integer.MIN_VALUE;
    private int mImpressionMinVisibleMs = Integer.MIN_VALUE;
    private boolean mIsVisibilityImpressionTrackingEnabled = false;
    @Nullable
    private BannerVisibilityTracker mVisibilityTracker;
    private Event[] mEvents;

    public CustomEventBannerAdapter(@NonNull CDAdView cdAdView,
            @NonNull String className,
            @NonNull final Event[] events,
            long broadcastIdentifier,
            @NonNull CDAdSize requestedAdSize,
            @NonNull CDMediationAdRequest cdMediationAdRequest,
            @NonNull Map<String, String> serverExtras) {
        Preconditions.checkNotNull(serverExtras);
        mEvents = events;
        mClassName = className;
        mRequestedAdSize = requestedAdSize;
        mCdMediationAdRequest = cdMediationAdRequest;
        mEventIndex = 0;
        mHandler = new Handler();
        mCDAdView = cdAdView;
        mContext = cdAdView.getContext();
        mTimeout = new Runnable() {
            @Override
            public void run() {
                CDAdLog.d("Ad timed out.");
                onBannerFailed(NETWORK_TIMEOUT);
                invalidate();
            }
        };

        mEventTimeout = new Runnable() {
            @Override
            public void run() {
                if (mCustomEventBanner != null) {
                    // Custom event classes can be developed by any third party and may not be tested.
                    // We catch all exceptions here to prevent crashes from untested code.
                    try {
                        mCustomEventBanner.onInvalidate();
                    } catch (Exception e) {
                        Utils.logStackTrace(e);
                        CDAdLog.d("Invalidating a custom event banner threw an exception", e);
                    }
                }
                mEventIndex++;
                if (mEventIndex<events.length){
                    loadAd();
                }else{
                    onBannerFailed(CDAdErrorCode.NO_FILL);
                    invalidate();
                }
            }
        };

        // Attempt to load the JSON extras into mServerExtras.
        mServerExtras = new TreeMap<String, String>(serverExtras);

        if (mServerExtras!=null && mServerExtras.keySet().contains(DataKeys.BASE_SDK_EVENT_URL)){
            String baseUrl = TypeParser.parseString(mServerExtras.get(DataKeys.BASE_SDK_EVENT_URL), "");
            if (baseUrl.length()>0)
                mBaseSdkEventUrl = baseUrl;
        }
        // Parse banner impression tracking headers to determine if we are in visibility experiment
        parseBannerImpressionTrackingHeaders();

    }

    @ReflectionTarget
    void loadAd() {
        mEventCheckArray = new int[]{0,0,0,0,0,0,0,0,0,0};
        Map<String, Object> eventParams = null;
        String eventClassName = mClassName;
        int timeout = getTimeoutDelayMilliseconds();
        if (mEvents.length>0){
            Event event = mEvents[mEventIndex];
            if (event.getEventData() != null){
                if (mClassName !=null && !mClassName.contains("Interstitial") && mEvents[mEventIndex].getEventData().getAndroid_Banner()!=null) {
                    eventParams = mEvents[mEventIndex].getEventData().getAndroid_Banner();
                    eventClassName = (String) eventParams.get("class");
                }
                if (eventParams!=null){
                    eventParams.remove("class");
                }
            }
            else if (mServerExtras==null || !mServerExtras.keySet().contains(DataKeys.HTML_RESPONSE_BODY_KEY) || mServerExtras.get(DataKeys.HTML_RESPONSE_BODY_KEY)==null || mServerExtras.get(DataKeys.HTML_RESPONSE_BODY_KEY).length()==0) {
                onBannerFailed(CDAdErrorCode.NO_FILL);
                return;
            }
            if (event!=null) {
                timeout = event.getTimeout() * 1000;
                if (eventParams!=null)
                    eventParams.put(MediationConstants.SDK_ID, event.getId());
            }
        }

        if (!Reflection.classFound(eventClassName)){
            if (mEventIndex<mEvents.length-1){
                mHandler.post(mEventTimeout);
            }else{
                onBannerFailed(CDAdErrorCode.ADAPTER_CONFIGURATION_ERROR);
            }
            return;
        }

        CDAdLog.d("Attempting to invoke custom event: " + eventClassName);
        try {
            mCustomEventBanner = CustomEventBannerFactory.create(eventClassName);
        } catch (Exception exception) {
            Utils.logStackTrace(exception);
            CDAdLog.d("Couldn't locate or instantiate custom event: " + eventClassName + ".");
            mCDAdView.loadFailUrl(ADAPTER_NOT_FOUND);
            return;
        }

        if (isInvalidated()) {
            return;
        }

        if (mCustomEventBanner == null){
            if (mEventIndex<mEvents.length-1){
                mHandler.post(mEventTimeout);
            }else{
                onBannerFailed(CDAdErrorCode.ADAPTER_NOT_FOUND);
            }
        }

        mHandler.postDelayed(mEventTimeout, timeout);

        mHandler.postDelayed(mTimeout, getTimeoutDelayMilliseconds());

        // Custom event classes can be developed by any third party and may not be tested.
        // We catch all exceptions here to prevent crashes from untested code.
        try {
            mCustomEventBanner.loadBanner(mContext, this, eventParams, mRequestedAdSize, mCdMediationAdRequest, mServerExtras);
            trackEvent(CDAdConstants.EVENT_REQUESTS);
        } catch (Exception e) {
            Utils.logStackTrace(e);
            CDAdLog.d("Loading a custom event banner threw an exception.", e);
            if (mEventIndex<mEvents.length-1){
                mHandler.post(mEventTimeout);
            }else{
                onBannerFailed(CDAdErrorCode.INTERNAL_ERROR);
            }
        }
    }

    @ReflectionTarget
    void invalidate() {
        if (mCustomEventBanner != null) {
            // Custom event classes can be developed by any third party and may not be tested.
            // We catch all exceptions here to prevent crashes from untested code.
            try {
                mCustomEventBanner.onInvalidate();
            } catch (Exception e) {
                        Utils.logStackTrace(e);
                CDAdLog.d("Invalidating a custom event banner threw an exception", e);
            }
        }
        if (mVisibilityTracker != null) {
            try {
                mVisibilityTracker.destroy();
            } catch (Exception e) {
                        Utils.logStackTrace(e);
                CDAdLog.d("Destroying a banner visibility tracker threw an exception", e);
            }
        }
        mContext = null;
        mCustomEventBanner = null;
        mServerExtras = null;
        mInvalidated = true;
    }

    boolean isInvalidated() {
        return mInvalidated;
    }

    @Deprecated
    @VisibleForTesting
    int getImpressionMinVisibleDips() {
        return mImpressionMinVisibleDips;
    }

    @Deprecated
    @VisibleForTesting
    int getImpressionMinVisibleMs() {
        return mImpressionMinVisibleMs;
    }

    @Deprecated
    @VisibleForTesting
    boolean isVisibilityImpressionTrackingEnabled() {
        return mIsVisibilityImpressionTrackingEnabled;
    }

    @Nullable
    @Deprecated
    @VisibleForTesting
    BannerVisibilityTracker getVisibilityTracker() {
        return mVisibilityTracker;
    }

    private void cancelTimeout() {
        mHandler.removeCallbacks(mTimeout);
        mHandler.removeCallbacks(mEventTimeout);
    }

    private int getTimeoutDelayMilliseconds() {
        return DEFAULT_BANNER_TIMEOUT_DELAY;
    }

    private void parseBannerImpressionTrackingHeaders() {
        final String impressionMinVisibleDipsString =
                mServerExtras.get(DataKeys.BANNER_IMPRESSION_MIN_VISIBLE_DIPS);
        final String impressionMinVisibleMsString =
                mServerExtras.get(DataKeys.BANNER_IMPRESSION_MIN_VISIBLE_MS);

        if (!TextUtils.isEmpty(impressionMinVisibleDipsString)
                && !TextUtils.isEmpty(impressionMinVisibleMsString)) {
            try {
                mImpressionMinVisibleDips = Integer.parseInt(impressionMinVisibleDipsString);
            } catch (NumberFormatException e) {
                        Utils.logStackTrace(e);
                CDAdLog.d("Cannot parse integer from header "
                        + DataKeys.BANNER_IMPRESSION_MIN_VISIBLE_DIPS);
            }

            try {
                mImpressionMinVisibleMs = Integer.parseInt(impressionMinVisibleMsString);
            } catch (NumberFormatException e) {
                        Utils.logStackTrace(e);
                CDAdLog.d("Cannot parse integer from header "
                        + DataKeys.BANNER_IMPRESSION_MIN_VISIBLE_MS);
            }

            if (mImpressionMinVisibleDips > 0 && mImpressionMinVisibleMs >= 0) {
                    mIsVisibilityImpressionTrackingEnabled = true;
            }
        }
    }

    /*
     * CustomEventBanner.Listener implementation
     */
    @Override
    public void onBannerLoaded(View bannerView) {
        if (isInvalidated()) {
            return;
        }

        cancelTimeout();

        if (mCDAdView != null) {
            mCDAdView.nativeAdLoaded();

            // If visibility impression tracking is enabled for banners, fire all impression
            // tracking URLs (AdServer, MPX, 3rd-party) for both HTML and MRAID banner types when
            // visibility conditions are met.
            //
            // Else, retain old behavior of firing AdServer impression tracking URL if and only if
            // banner is not HTML.
            if (mIsVisibilityImpressionTrackingEnabled) {
                // Set up visibility tracker and listener if in experiment
                mVisibilityTracker = new BannerVisibilityTracker(mContext, mCDAdView, bannerView,
                        mImpressionMinVisibleDips, mImpressionMinVisibleMs);
                mVisibilityTracker.setBannerVisibilityTrackerListener(
                        new BannerVisibilityTracker.BannerVisibilityTrackerListener() {
                    @Override
                    public void onVisibilityChanged() {
                        mCDAdView.trackNativeImpression();
                        if (mCustomEventBanner != null) {
                            mCustomEventBanner.trackMpxAndThirdPartyImpressions();
                        }
                    }
                });
            }

            mCDAdView.setAdContentView(bannerView);

            // Old behavior
            if (!mIsVisibilityImpressionTrackingEnabled) {
                if (!(bannerView instanceof HtmlBannerWebView)) {
                    mCDAdView.trackNativeImpression();
                }
            }
        }
        trackEvent(CDAdConstants.EVENT_LOAD);
    }

    @Override
    public void onBannerFailed(CDAdErrorCode errorCode) {
        if (isInvalidated()) {
            return;
        }

        if (mCDAdView != null) {
            if (errorCode == null) {
                errorCode = UNSPECIFIED;
            }
            if (mEventIndex<mEvents.length-1){
                mHandler.post(mEventTimeout);
            }else{
                cancelTimeout();
                mCDAdView.loadFailUrl(errorCode);
            }
        }
    }

    @Override
    public void onBannerExpanded() {
        if (isInvalidated()) {
            return;
        }

        mStoredAutorefresh = mCDAdView.getAutorefreshEnabled();
        mCDAdView.setLocationAutoUpdateEnabled(false);
        mCDAdView.adPresentedOverlay();
    }

    @Override
    public void onBannerCollapsed() {
        if (isInvalidated()) {
            return;
        }

        mCDAdView.setLocationAutoUpdateEnabled(mStoredAutorefresh);
        mCDAdView.adClosed();
    }

    @Override
    public void onBannerClicked() {
        if (isInvalidated()) {
            return;
        }

        if (mCDAdView != null) {
            mCDAdView.registerClick();
        }

        trackEvent(CDAdConstants.EVENT_CLICK);
    }

    @Override
    public void onLeaveApplication() {
        onBannerClicked();
    }

    @Override
    public void onBannerDisplayed(View bannerView) {
        trackEvent(CDAdConstants.EVENT_IMPRESSION);
    }

    @Override
    public void onBannerDismissed() {

    }

    public void trackEvent(int event){
        if (mEventCheckArray[event]==0 && mBaseSdkEventUrl!=null) {
            TrackingRequest.makeTrackingHttpRequest(mBaseSdkEventUrl.replace(DataKeys.EVENT_ID_PLACEHOLDER, event + "").replace(DataKeys.SDK_ID_PLACEHOLDER, mCustomEventBanner.getId()), mContext);
            mEventCheckArray[event] = 1;
        }
    }
}
