package com.chalkdigital.interstitial.ads;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.chalkdigital.ads.CDAdErrorCode;
import com.chalkdigital.ads.WebViewCacheService;
import com.chalkdigital.common.AdReport;
import com.chalkdigital.common.CDAdConstants;
import com.chalkdigital.common.CDAdSize;
import com.chalkdigital.common.Constants;
import com.chalkdigital.common.DataKeys;
import com.chalkdigital.common.Preconditions;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.Reflection;
import com.chalkdigital.common.util.Utils;
import com.chalkdigital.interstitial.ads.CustomEventInterstitial.CustomEventInterstitialListener;
import com.chalkdigital.interstitial.ads.factories.CustomEventInterstitialFactory;
import com.chalkdigital.mediation.CDMediationAdRequest;
import com.chalkdigital.mediation.MediationConstants;
import com.chalkdigital.network.TrackingRequest;
import com.chalkdigital.network.response.Event;
import com.chalkdigital.network.response.TypeParser;

import java.util.HashMap;
import java.util.Map;

import static com.chalkdigital.ads.CDAdErrorCode.ADAPTER_NOT_FOUND;
import static com.chalkdigital.ads.CDAdErrorCode.NETWORK_TIMEOUT;
import static com.chalkdigital.ads.CDAdErrorCode.UNSPECIFIED;

public class CustomEventInterstitialAdapter implements CustomEventInterstitialListener {
    public static final int DEFAULT_INTERSTITIAL_TIMEOUT_DELAY = Constants.SIXTY_SECONDS_MILLIS;

    private final CDAdInterstitial mCDAdInterstitial;
    private boolean mInvalidated;
    private CustomEventInterstitialAdapterListener mCustomEventInterstitialAdapterListener;
    private CustomEventInterstitial mCustomEventInterstitial;
    private Context mContext;
    private HashMap<String, String> mServerExtras;
    private final Handler mHandler;
    private final Runnable mTimeout;
    private final CDMediationAdRequest mMediationAdRequest;
    private long mBroadcastIdentifier;
    private String mClassName;
    private Runnable mEventTimeout;
    private Event[] mEvents;
    private int mEventIndex;
    private String mBaseSdkEventUrl;
    private int[] mEventCheckArray;
    private CDAdSize mRequestedAdSize;

    public CustomEventInterstitialAdapter(@NonNull final CDAdInterstitial cdAdInterstitial,
                                          @NonNull final String className, long broadcastIdentifier, final Event[] events, final CDMediationAdRequest mediationAdRequest,
                                          @NonNull CDAdSize requestedAdSize, @NonNull final Map<String, String> serverExtras) {
        Preconditions.checkNotNull(serverExtras);
        mHandler = new Handler();
        mClassName = className;
        mEvents = events;
        mMediationAdRequest = mediationAdRequest;
        mCDAdInterstitial = cdAdInterstitial;
        mBroadcastIdentifier = broadcastIdentifier;
        mContext = mCDAdInterstitial.getActivity();
        mRequestedAdSize = requestedAdSize;
        mTimeout = new Runnable() {
            @Override
            public void run() {
                CDAdLog.d("Third-party network timed out.");
                onInterstitialFailed(NETWORK_TIMEOUT);
                invalidate();
            }
        };

        mEventTimeout = new Runnable() {
            @Override
            public void run() {
                if (mCustomEventInterstitial != null) {

                    // Custom event classes can be developed by any third party and may not be tested.
                    // We catch all exceptions here to prevent crashes from untested code.
                    try {
                        mCustomEventInterstitial.onInvalidate();
                    } catch (Exception e) {
                        Utils.logStackTrace(e);
                        CDAdLog.d("Invalidating a custom event interstitial threw an exception.", e);
                    }
                }
                mEventIndex++;
                if (mEventIndex<events.length){
                    loadInterstitial();
                }else{
                    onInterstitialFailed(CDAdErrorCode.NO_FILL);
                    invalidate();
                }
            }
        };


        mServerExtras = new HashMap<String, String>(serverExtras);
        if (mServerExtras!=null && mServerExtras.keySet().contains(DataKeys.BASE_SDK_EVENT_URL)){
            String baseUrl = TypeParser.parseString(mServerExtras.get(DataKeys.BASE_SDK_EVENT_URL), "");
            if (baseUrl.length()>0)
                mBaseSdkEventUrl = baseUrl;
        }
    }

    void loadInterstitial() {

        mEventCheckArray = new int[]{0,0,0,0,0,0,0,0,0,0};
        Map<String, Object> eventParams = null;
        String eventClassName = mClassName;
        int timeout = getTimeoutDelayMilliseconds();
        if (mEvents.length>0){
            Event event = mEvents[mEventIndex];
            if (event.getEventData() != null){

                if (mClassName !=null && mClassName.contains("Interstitial")) {
                    if (mCDAdInterstitial.mCDAdRequest.rewarded && mEvents[mEventIndex].getEventData().getAndroid_Rewarded() != null) {
                        eventParams = mEvents[mEventIndex].getEventData().getAndroid_Rewarded();
                        eventClassName = (String) eventParams.get("class");
                    }
                    else if (mEvents[mEventIndex].getEventData().getAndroid_Interstitial() != null) {
                        eventParams = mEvents[mEventIndex].getEventData().getAndroid_Interstitial();
                        eventClassName = (String) eventParams.get("class");
                    }
                }
                if (eventParams!=null){
                    eventParams.remove("class");
                }
            }
            else if (mServerExtras==null || !mServerExtras.keySet().contains(DataKeys.HTML_RESPONSE_BODY_KEY) || mServerExtras.get(DataKeys.HTML_RESPONSE_BODY_KEY)==null || mServerExtras.get(DataKeys.HTML_RESPONSE_BODY_KEY).length()==0) {
                onInterstitialFailed(CDAdErrorCode.NO_FILL);
                return;
            }
            if (event!=null) {
                timeout = event.getTimeout() * 1000;
                if (eventParams!=null)
                    eventParams.put(MediationConstants.SDK_ID, event.getId());
            }
            if (mCDAdInterstitial.mCDAdRequest.rewarded) {
                mServerExtras.put(DataKeys.SKIP_ENABLE, "false");
                mServerExtras.put(DataKeys.FORCE_CLOSE_BUTTON, "false");
            }
        }

        if (!Reflection.classFound(eventClassName)){
            if (mEventIndex<mEvents.length-1){
                mHandler.post(mEventTimeout);
            }else{
                onInterstitialFailed(CDAdErrorCode.ADAPTER_CONFIGURATION_ERROR);
            }
            return;
        }

        CDAdLog.d("Attempting to invoke custom event: " + eventClassName);
        try {
            mCustomEventInterstitial = CustomEventInterstitialFactory.create(eventClassName);
        } catch (Exception exception) {
            Utils.logStackTrace(exception);
            CDAdLog.d("Couldn't locate or instantiate custom event: " + eventClassName + ".");
            mCDAdInterstitial.onCustomEventInterstitialFailed(ADAPTER_NOT_FOUND);
            return;
        }

        if (isInvalidated()) {
            return;
        }

        if (mCustomEventInterstitial == null){
            if (mEventIndex<mEvents.length-1){
                mHandler.post(mEventTimeout);
            }else{
                onInterstitialFailed(CDAdErrorCode.ADAPTER_NOT_FOUND);
            }
        }

        mHandler.postDelayed(mEventTimeout, timeout);
        mHandler.postDelayed(mTimeout, getTimeoutDelayMilliseconds());


        // Custom event classes can be developed by any third party and may not be tested.
        // We catch all exceptions here to prevent crashes from untested code.
        try {
            mCustomEventInterstitial.loadInterstitial(mContext, this, eventParams, mMediationAdRequest, mRequestedAdSize, mServerExtras);
            trackEvent(CDAdConstants.EVENT_REQUESTS);
        } catch (Exception e) {
            Utils.logStackTrace(e);
            CDAdLog.d("Loading a custom event interstitial threw an exception.", e);
            if (mEventIndex<mEvents.length-1){
                mHandler.post(mEventTimeout);
            }else{
                onInterstitialFailed(CDAdErrorCode.ADAPTER_CONFIGURATION_ERROR);
            }
        }
    }


    void showInterstitial() {
        if (isInvalidated() || mCustomEventInterstitial == null) {
            return;
        }

        // Custom event classes can be developed by any third party and may not be tested.
        // We catch all exceptions here to prevent crashes from untested code.
        try {
            mCustomEventInterstitial.showInterstitial();
        } catch (Exception e) {
                        Utils.logStackTrace(e);
            CDAdLog.d("Showing a custom event interstitial threw an exception.", e);
            onInterstitialFailed(CDAdErrorCode.INTERNAL_ERROR);
        }
    }

    void invalidate() {
        if (mCustomEventInterstitial != null) {

            // Custom event classes can be developed by any third party and may not be tested.
            // We catch all exceptions here to prevent crashes from untested code.
            try {
                mCustomEventInterstitial.onInvalidate();
            } catch (Exception e) {
                        Utils.logStackTrace(e);
                CDAdLog.d("Invalidating a custom event interstitial threw an exception.", e);
            }
        }
        mCustomEventInterstitial = null;
        mContext = null;
        mServerExtras = null;
        mCustomEventInterstitialAdapterListener = null;
        final WebViewCacheService.Config config =
                WebViewCacheService.popWebViewConfig(mBroadcastIdentifier);
        if (config != null) {
            config.getWebView().destroy();
        }
        mInvalidated = true;
    }

    boolean isInvalidated() {
        return mInvalidated;
    }

    void setAdapterListener(CustomEventInterstitialAdapterListener listener) {
        mCustomEventInterstitialAdapterListener = listener;
    }

    private void cancelTimeout() {
        mHandler.removeCallbacks(mTimeout);
        mHandler.removeCallbacks(mEventTimeout);
    }

    private int getTimeoutDelayMilliseconds() {
        if (mCDAdInterstitial == null
                || mCDAdInterstitial.getAdTimeoutDelay() == null
                || mCDAdInterstitial.getAdTimeoutDelay() < 0) {
            return DEFAULT_INTERSTITIAL_TIMEOUT_DELAY;
        }

        return mCDAdInterstitial.getAdTimeoutDelay() * 1000;
    }

    interface CustomEventInterstitialAdapterListener {
        void onCustomEventInterstitialLoaded();
        void onCustomEventInterstitialFailed(CDAdErrorCode errorCode);
        void onCustomEventInterstitialShown();
        void onCustomEventInterstitialClicked();
        void onCustomEventInterstitialDismissed();
        void onCustomEventInterstitialVideoEnded();
        void onCustomEventRewardsUnlocked(Map<Object, Object> map);
    }

    /*
     * CustomEventInterstitial.Listener implementation
     */
    @Override
    public void onInterstitialLoaded() {
        if (isInvalidated()) {
            return;
        }

        cancelTimeout();

        if (mCustomEventInterstitialAdapterListener != null) {
            mCustomEventInterstitialAdapterListener.onCustomEventInterstitialLoaded();
        }
        trackEvent(CDAdConstants.EVENT_LOAD);
    }

    @Override
    public void onInterstitialFailed(CDAdErrorCode errorCode) {
        if (isInvalidated()) {
            return;
        }

        if (mCustomEventInterstitialAdapterListener != null) {
            if (errorCode == null) {
                errorCode = UNSPECIFIED;
            }
            if (mEventIndex<mEvents.length-1){
                mHandler.post(mEventTimeout);
            }else{
                cancelTimeout();
                mCustomEventInterstitialAdapterListener.onCustomEventInterstitialFailed(errorCode);
            }
        }
    }

    @Override
    public void onInterstitialShown() {
        if (isInvalidated()) {
            return;
        }

        if (mCustomEventInterstitialAdapterListener != null) {
            mCustomEventInterstitialAdapterListener.onCustomEventInterstitialShown();
        }
        trackEvent(CDAdConstants.EVENT_IMPRESSION);
    }

    @Override
    public void onInterstitialClicked() {
        if (isInvalidated()) {
            return;
        }

        if (mCustomEventInterstitialAdapterListener != null) {
            mCustomEventInterstitialAdapterListener.onCustomEventInterstitialClicked();
        }
        trackEvent(CDAdConstants.EVENT_CLICK);
    }

    @Override
    public void onLeaveApplication() {
        onInterstitialClicked();
    }

    @Override
    public void onInterstitialDismissed() {
        if (isInvalidated()) {
            return;
        }

        if (mCustomEventInterstitialAdapterListener != null) {
            mCustomEventInterstitialAdapterListener.onCustomEventInterstitialDismissed();
        }
    }

    @Override
    public void onInterstitialVideoEnded() {
        if (isInvalidated()) {
            return;
        }

        if (mCustomEventInterstitialAdapterListener != null) {
            mCustomEventInterstitialAdapterListener.onCustomEventInterstitialVideoEnded();
        }
        trackEvent(CDAdConstants.EVENT_COMPLETE);
    }

    @Deprecated
    void setCustomEventInterstitial(CustomEventInterstitial interstitial) {
        mCustomEventInterstitial = interstitial;
    }

    public void trackEvent(int event){
        if (mEventCheckArray[event]==0 && mBaseSdkEventUrl!=null) {
            TrackingRequest.makeTrackingHttpRequest(mBaseSdkEventUrl.replace(DataKeys.EVENT_ID_PLACEHOLDER, event + "").replace(DataKeys.SDK_ID_PLACEHOLDER, mCustomEventInterstitial.getId()), mContext);
            mEventCheckArray[event] = 1;
        }
    }
}
