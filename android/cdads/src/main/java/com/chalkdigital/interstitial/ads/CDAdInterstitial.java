package com.chalkdigital.interstitial.ads;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.chalkdigital.ads.CDAdErrorCode;
import com.chalkdigital.ads.CDAdView;
import com.chalkdigital.ads.AdTypeTranslator;
import com.chalkdigital.common.CDAdDeviceInfo;
import com.chalkdigital.common.CDAdGeoInfo;
import com.chalkdigital.common.CDAdRequest;
import com.chalkdigital.common.CDAdSize;
import com.chalkdigital.common.CDAdsUtils;
import com.chalkdigital.common.AdFormat;
import com.chalkdigital.common.DataKeys;
import com.chalkdigital.common.Preconditions;
import com.chalkdigital.common.VisibleForTesting;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.DeviceUtils;
import com.chalkdigital.common.util.Utils;
import com.chalkdigital.interstitial.ads.factories.CustomEventInterstitialAdapterFactory;
import com.chalkdigital.mediation.CDMediationAdRequest;
import com.chalkdigital.nativeads.VideoConfiguration;
import com.chalkdigital.network.response.AdResponse;
import com.chalkdigital.network.response.Event;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.chalkdigital.ads.CDAdErrorCode.ADAPTER_NOT_FOUND;
import static com.chalkdigital.ads.CDAdErrorCode.EXPIRED;
import static com.chalkdigital.common.Constants.AD_EXPIRATION_DELAY;
import static com.chalkdigital.interstitial.ads.CDAdInterstitial.InterstitialState.DESTROYED;
import static com.chalkdigital.interstitial.ads.CDAdInterstitial.InterstitialState.IDLE;
import static com.chalkdigital.interstitial.ads.CDAdInterstitial.InterstitialState.LOADING;
import static com.chalkdigital.interstitial.ads.CDAdInterstitial.InterstitialState.READY;
import static com.chalkdigital.interstitial.ads.CDAdInterstitial.InterstitialState.SHOWING;

public class CDAdInterstitial implements CustomEventInterstitialAdapter.CustomEventInterstitialAdapterListener {
    private AtomicBoolean mIsDismissed;
    private String mRequestId;

    @VisibleForTesting
    enum InterstitialState {
        /**
         * Waiting to something to happen. There is no interstitial currently loaded.
         */
        IDLE,

        /**
         * Loading an interstitial.
         */
        LOADING,

        /**
         * Loaded and ready to be shown.
         */
        READY,

        /**
         * The interstitial is showing.
         */
        SHOWING,

        /**
         * No longer able to accept events as the internal InterstitialView has been destroyed.
         */
        DESTROYED
    }

    @NonNull
    protected CDAdInterstitialView mInterstitialView;
    @Nullable
    protected CustomEventInterstitialAdapter mCustomEventInterstitialAdapter;
    @Nullable
    private InterstitialAdListener mInterstitialAdListener;
    @Nullable
    private CDAdVideoInterstitial.InterstitialVideoAdListener mInterstitialVideoAdListener;
    @NonNull
    protected Activity mActivity;
    @NonNull
    private Handler mHandler;
    @NonNull
    private final Runnable mAdExpiration;
    @NonNull
    private volatile InterstitialState mCurrentInterstitialState;

    private boolean locationAutoUpdateEnabled;

    private Integer refreshInterval;

    private boolean shouldSkipStateCheck;

    protected boolean mTesting;
    protected String mBundleIdentifier;
    protected String mCategory;
    protected CDAdGeoInfo mCDAdGeoInfo;
    private CDAdSize cdAdSize;

    public CDAdSize getCdAdSize() {
        return cdAdSize;
    }

    public void setCdAdSize(CDAdSize.CDAdSizeConstant cdAdSize) {
        this.cdAdSize = CDAdSize.getSizeFromCDSizeConstant(cdAdSize);
    }

    @NonNull
    CDAdRequest mCDAdRequest;

    protected boolean isVideoAd;

    public interface InterstitialAdListener {

        /**
         * This method is called when an ad request is initiated on CDAdInterstitial object.
         * @param interstitial reference of interstitial on which ad request is initiated.
         */
        void onInterstitialAdRequest(CDAdInterstitial interstitial);

        /**
         * This method is called when an ad is loaded for an CDAdInterstitial object.
         * @param interstitial reference of interstitial for which ad is loaded.
         */
        void onInterstitialLoaded(CDAdInterstitial interstitial);

        /**
         * This method is called when an ad request is failed for an CDAdInterstitial object.
         * @param interstitial reference of interstitial for which ad is failed.
         * @param errorCode reason of ad request failure.
         */
        void onInterstitialFailed(CDAdInterstitial interstitial, CDAdErrorCode errorCode);

        /**
         * This method is called when an ad is shown using an CDAdInterstitial object.
         * @param interstitial reference of interstitial on which an ad is shown.
         */
        void onInterstitialShown(CDAdInterstitial interstitial);

        /**
         * This method is called when an ad is clicked.
         * @param interstitial reference of interstitial on which ad is clicked.
         */
        void onInterstitialClicked(CDAdInterstitial interstitial);

        /**
         * This method is called when an ad is dismissed.
         * @param interstitial reference of interstitial on which ad is dismissed.
         */
        void onInterstitialDismissed(CDAdInterstitial interstitial);

    }

    /**
     * Enable testing mode for interstitial.
     * <p/>
     *
     * @param testing boolean value to enable testing mode.
     */
    public void setTesting(final boolean testing) {
        mTesting = testing;
    }

    /**
     * Set Bundle Identifier for an interstitial.
     * Note : This will work only when testing ads are requested.
     * <p/>
     *
     * @param bundleIdentifier The bundle identifier of application. Its default value would be the bundle identifier of the app in which you are using CDAdInterstitial.
     */

    public void setBundleIdentifier(@NonNull  final String bundleIdentifier) {
        mBundleIdentifier = bundleIdentifier;
    }

    /**
     * Set category for an interstitial.
     * <p/>
     *
     * @param category The IAB category of application.
     */
    public void setCategory(@NonNull final String category) {
        mCategory = category;
    }

    /**
     * Set CDAdGeoInfo for an interstitial.
     * <p/>
     *
     * @param CDAdGeoInfo The CDAdGeoInfo object to set ad location. Use this only if automatic location updates are disabled.
     */
    public void setCDAdGeoInfo(@NonNull final CDAdGeoInfo CDAdGeoInfo) {
        mCDAdGeoInfo = CDAdGeoInfo;
    }

    /**
     * Creates a new interstitial to be used as an ad.
     * <p/>
     *
     * @param activity The activity context which would be used to render this interstitial.
     */
    public CDAdInterstitial(@NonNull final Activity activity) {
//        CDAdLog.d("REFERENCE : "+Integer.toHexString(System.identityHashCode(this)), "new CDAdInterstitial created");
//        if (CDAdLog.getSdkHandlerLevel() == Level.ALL) {
//            StringBuilder sb = new StringBuilder();
//            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
//            for (StackTraceElement element : stackTraceElements) {
//                sb.append(element.toString());
//                sb.append("\n");
//            }
//            CDAdLog.d(sb.toString());
//            Throwable throwable = new Throwable("CDAdInterstitial instantiated");
//            throwable.setStackTrace(Thread.currentThread().getStackTrace());
//            Utils.logStackTrace(throwable);
//        }
        setTesting(false);
        try {
            setBundleIdentifier(activity.getResources().getText(activity.getResources().getIdentifier("CDADS_BUNDLE_ID", "string", activity.getPackageName())).toString());
        } catch (Exception e) {
            setBundleIdentifier("");
        }
        try {
            setCategory(activity.getResources().getText(activity.getResources().getIdentifier("CDADS_CAT", "string", activity.getPackageName())).toString());
        } catch (Exception e) {
            setCategory("");
        }
        setCDAdGeoInfo(null);
        mActivity = activity;

        mInterstitialView = new CDAdInterstitialView(mActivity);
        mInterstitialView.setAdAutoRefreshEnabled(false);
        mCurrentInterstitialState = IDLE;

        mHandler = new Handler();
        mAdExpiration = new Runnable() {
            @Override
            public void run() {
                CDAdLog.d("Expiring unused Interstitial ad.");
                attemptStateTransition(IDLE, true);
                if (!SHOWING.equals(mCurrentInterstitialState) &&
                        !DESTROYED.equals(mCurrentInterstitialState)) {
                    // double-check the state in case the runnable fires right after the state
                    // transition but before it's cancelled
                    mInterstitialView.adFailed(EXPIRED);
                }
            }
        };
    }

    /**
     * Check Automatic location updates for an interstitial.
     * <p/>
     *
     * @return  locationAutoUpdateEnabled boolean value which indicates automatic location updates for interstitial enabled or not.
     *
     *
     */
    public boolean isLocationAutoUpdateEnabled() {
        return locationAutoUpdateEnabled;
    }

    /**
     * Enable/Disable Automatic location updates for an interstitial.
     * <p/>
     *
     * @param  locationAutoUpdateEnabled boolean value to set automatic location updates for interstitial.
     *
     *
     */
    public void setLocationAutoUpdateEnabled(boolean locationAutoUpdateEnabled) {
        this.locationAutoUpdateEnabled = locationAutoUpdateEnabled;
        if (mInterstitialView!=null)
            mInterstitialView.setLocationAutoUpdateEnabled(locationAutoUpdateEnabled);
    }


    private boolean attemptStateTransition(@NonNull final InterstitialState endState) {
        return attemptStateTransition(endState, false);
    }

    /**
     * Attempts to transition to the new state. All state transitions should go through this method.
     * Other methods should not be modifying mCurrentInterstitialState.
     *
     * @param endState     The desired end state.
     * @param force Whether or not this is part of a force transition. Force transitions
     *                     can happen from IDLE, LOADING, or READY. It will ignore
     *                     the currently loading or loaded ad and attempt to load another.
     * @return {@code true} if a state change happened, {@code false} if no state change happened.
     */
    @VisibleForTesting
    synchronized boolean attemptStateTransition(@NonNull final InterstitialState endState,
            boolean force) {
        Preconditions.checkNotNull(endState);

        final InterstitialState startState = mCurrentInterstitialState;

        /**
         * There are 50 potential cases. Any combination that is a no op will not be enumerated
         * and returns false. The usual case goes IDLE -> LOADING -> READY -> SHOWING -> IDLE. At
         * most points, having the force refresh flag into IDLE resets CDAdInterstitial and clears
         * the interstitial adapter. This cannot happen while an interstitial is showing. Also,
         * CDAdInterstitial can be destroyed arbitrarily, and once this is destroyed, it no longer
         * can perform any state transitions.
         */
        switch (startState) {
            case IDLE:
                switch(endState) {
                    case LOADING:
                        // Going from IDLE to LOADING is the usual load case
                        invalidateInterstitialAdapter();
                        mCurrentInterstitialState = LOADING;
                        if (force) {
                            // Force-load means a pub-initiated force refresh.
                            mInterstitialView.forceRefresh();
                        } else {
                            // Otherwise, do a normal load
                            mInterstitialView.loadRequest(mCDAdRequest);
                        }
                        return true;
                    case SHOWING:
                        CDAdLog.d("No interstitial loading or loaded.");
                        return false;
                    case DESTROYED:
                        setInterstitialStateDestroyed();
                        return true;
                    default:
                        return false;
                }
            case LOADING:
                switch (endState) {
                    case IDLE:
                        // Being forced back into idle while loading resets CDAdInterstitial while
                        // not forced just means the load failed. Either way, it should reset the
                        // state back into IDLE.
                        invalidateInterstitialAdapter();
                        mCurrentInterstitialState = IDLE;
                        return true;
                    case LOADING:
                        if (!force) {
                            // Cannot load more than one interstitial at a time
                            CDAdLog.d("Already loading an interstitial.");
                        }
                        return false;
                    case READY:
                        // This is the usual load finished transition
                        mCurrentInterstitialState = READY;
                        // Expire CDAd ads to synchronize with CDAd Ad Server tracking window
                        if (AdTypeTranslator.CustomEventType
                                .isCDAdSpecific(mInterstitialView.getCustomEventClassName())) {
                            mHandler.postDelayed(mAdExpiration, AD_EXPIRATION_DELAY);
                        }
                        return true;
                    case SHOWING:
                        CDAdLog.d("Interstitial is not ready to be shown yet.");
                        return false;
                    case DESTROYED:
                        setInterstitialStateDestroyed();
                        return true;
                    default:
                        return false;
                }
            case READY:
                switch (endState) {
                    case IDLE:
                        if (force) {
                            // This happens on a force refresh or an ad expiration
                            invalidateInterstitialAdapter();
                            mCurrentInterstitialState = IDLE;
                            return true;
                        }
                        return false;
                    case LOADING:
                        // This is to prevent loading another interstitial while one is loaded.
                        CDAdLog.d("Interstitial already loaded. Not loading another.");
                        // Let the ad listener know that there's already an ad loaded
                        if (!isVideoAd){
                            if (mInterstitialAdListener != null) {
                                CDAdLog.d("REQUEST ID : "+mRequestId+" ,REFERENCE : "+Integer.toHexString(System.identityHashCode(this)), "onInterstitialLoaded");
                                mInterstitialAdListener.onInterstitialLoaded(this);
                            }
                        }else{
                            if (mInterstitialVideoAdListener !=null) {
                                CDAdLog.d("REQUEST ID : "+mRequestId+" ,REFERENCE : "+Integer.toHexString(System.identityHashCode(this)), "onInterstitialVideoLoaded");
                                mInterstitialVideoAdListener.onInterstitialVideoLoaded((CDAdVideoInterstitial) CDAdInterstitial.this);
                            }
                        }

                        return false;
                    case SHOWING:
                        // This is the usual transition from ready to showing
                        showCustomEventInterstitial();
                        mCurrentInterstitialState = SHOWING;
                        mHandler.removeCallbacks(mAdExpiration);
                        return true;
                    case DESTROYED:
                        setInterstitialStateDestroyed();
                        return true;
                    default:
                        return false;
                }
            case SHOWING:
                switch(endState) {
                    case IDLE:
                        if (force) {
                            CDAdLog.d("Cannot force refresh while showing an interstitial.");
                            return false;
                        }
                        // This is the usual transition when done showing this interstitial
                        invalidateInterstitialAdapter();
                        mCurrentInterstitialState = IDLE;
                        return true;
                    case LOADING:
                        if (!force) {
                            CDAdLog.d("Interstitial already showing. Not loading another.");
                        }
                        return false;
                    case SHOWING:
                        CDAdLog.d("Already showing an interstitial. Cannot show it again.");
                        return false;
                    case DESTROYED:
                        setInterstitialStateDestroyed();
                        return true;
                    default:
                        return false;
                }
            case DESTROYED:
                // Once destroyed, CDAdInterstitial is no longer functional.
                CDAdLog.d("CDAdInterstitial destroyed. Ignoring all requests.");
                return false;
            default:
                return false;
        }
    }

    /**
     * Sets CDAdInterstitial to be destroyed. This should only be called by attemptStateTransition.
     */
    private void setInterstitialStateDestroyed() {
        invalidateInterstitialAdapter();
        mInterstitialView.setCDAdViewListener(null);
        mInterstitialView.destroy();
        mHandler.removeCallbacks(mAdExpiration);
        mCurrentInterstitialState = DESTROYED;
    }

//    public void setAdAutoRefreshEnabled(boolean adAutoRefreshEnabled){
//        if (mInterstitialView != null) {
//            mInterstitialView.setAdAutoRefreshEnabled(adAutoRefreshEnabled);
//        }
//    }



    /**
     * Request an interstitial ad.
     * <p/>
     *
     * @param context activity context.
     * @param targetingParams targeting parameters passed as key value pair.
     * @param partnerId partner id of publisher.
     * @param placementId placement id of this ad.
     *
     *
     */
    public void requestNewAd(Context context, Map<String, String> targetingParams, String partnerId, String placementId) {
        internalRequestNewAd(targetingParams, null, partnerId, placementId);
    }

    protected void internalRequestNewAd(Map<String, String> targetingParams, VideoConfiguration videoConfiguration, String partnerId, String placementId){
        try {
            if (mCurrentInterstitialState == InterstitialState.IDLE){
                mRequestId = UUID.randomUUID().toString();
                mIsDismissed = new AtomicBoolean(false);
            }
            if (!CDAdsUtils.initialised)
            {
                CDAdLog.e(CDAdErrorCode.CDADS_NOT_INITIALIZED.toString());
                if (mInterstitialAdListener!=null) {
                    CDAdLog.d("REQUEST ID : "+mRequestId+" ,REFERENCE : "+Integer.toHexString(System.identityHashCode(this)), "onInterstitialFailed");
                    mInterstitialAdListener.onInterstitialFailed(this, CDAdErrorCode.CDADS_NOT_INITIALIZED);
                }
                if (mInterstitialVideoAdListener!=null) {
                    CDAdLog.d("REQUEST ID : "+mRequestId+" ,REFERENCE : "+Integer.toHexString(System.identityHashCode(this)), "onInterstitialVideoFailed");
                    mInterstitialVideoAdListener.onInterstitialVideoFailed((CDAdVideoInterstitial) this, CDAdErrorCode.CDADS_NOT_INITIALIZED);
                }
                return;

            }
            CDAdRequest cdAdRequest = new CDAdRequest.Builder().build(mActivity.getApplicationContext());
            if(targetingParams!=null) {
                Set<String> keys = targetingParams.keySet();
                if (keys.contains("gender")) {
                    String value = targetingParams.get("gender");
                    if (value != null) {
                        if (value.equals("male")) {
                            cdAdRequest.targetingGender = "M";
                        }
                        if (value.equals("female")) {
                            cdAdRequest.targetingGender = "F";
                        }
                        if (value.equals("others")) {
                            cdAdRequest.targetingGender = "O";
                        }
                    }
                }
                if (keys.contains("age") && targetingParams.get("age") != null)
                    cdAdRequest.targetingAge = targetingParams.get("age");
                if (keys.contains("education") && targetingParams.get("education") != null)
                    cdAdRequest.targetingEducation = targetingParams.get("education");
                if (keys.contains("language") && targetingParams.get("language") != null)
                    cdAdRequest.targetingLanguage = targetingParams.get("language");
                if (keys.contains("income") && targetingParams.get("income") != null)
                    try {
                        cdAdRequest.targetingIncome = Integer.parseInt(targetingParams.get("income"));
                    } catch (NumberFormatException e) {
                        cdAdRequest.targetingIncome = 0;
                    }
                if (keys.contains("userId") && targetingParams.get("userId") != null)
                    cdAdRequest.userId = targetingParams.get("userId");
                if (keys.contains("keyword") && targetingParams.get("keyword") != null)
                    cdAdRequest.keyword = targetingParams.get("keyword");
            }

            cdAdRequest.partnerId = partnerId;
            cdAdRequest.placementId = placementId;
            cdAdRequest.onlySecureImpressionsAllowed = false;
            cdAdRequest.testing = mTesting;
            cdAdRequest.bundleId = mBundleIdentifier;
            cdAdRequest.cat = mCategory;
            cdAdRequest.geoInfo = mCDAdGeoInfo;
            mInterstitialView.setLocationAutoUpdateEnabled(locationAutoUpdateEnabled);
            cdAdRequest.locationAutoUpdateEnabled = locationAutoUpdateEnabled;
            cdAdRequest.videoConfiguration = videoConfiguration;
            loadRequest(cdAdRequest);

        } catch (Throwable throwable) {
            Utils.logStackTrace(throwable);
        }

    }

    private void loadRequest(@NonNull CDAdRequest cdAdRequest) {
        mCDAdRequest = cdAdRequest;
        shouldSkipStateCheck = true;
        attemptStateTransition(LOADING);
    }

    /**
     * Show an interstitial ad once it is loaded.
     *<p/>
     * It is cdised to confirm that ad is loaded using isReady() before calling this method.
     */
    public boolean show() {
        try {
            return attemptStateTransition(SHOWING);
        } catch (Throwable throwable) {
            Utils.logStackTrace(throwable);
        }
        return false;
    }

    /**
     * Force refresh an interstitial ad, this will discard the current ad request and attempt a fresh request for interstitial.
     */
    public void forceRefresh() {
        shouldSkipStateCheck = true;
        attemptStateTransition(IDLE, true);
        attemptStateTransition(LOADING, true);
    }

    /**
     * Check if ad is ready to display.
     */
    public boolean isReady() {
        return mCurrentInterstitialState == READY;
    }

    /**
     * Check if this interstitial is destroyed. A new instance is required to fetch ad once an interstitial is destroyed.
     */
    boolean isDestroyed() {
        return mCurrentInterstitialState == DESTROYED;
    }


    public Integer getAdTimeoutDelay() {
        return mInterstitialView.getAdTimeoutDelay();
    }

    @NonNull
    CDAdInterstitialView getCDAdInterstitialView() {
        return mInterstitialView;
    }

    private void showCustomEventInterstitial() {
        if (mCustomEventInterstitialAdapter != null) {
            mCustomEventInterstitialAdapter.showInterstitial();
        }
    }

    private void invalidateInterstitialAdapter() {
        if (mCustomEventInterstitialAdapter != null) {
            mCustomEventInterstitialAdapter.invalidate();
            mCustomEventInterstitialAdapter = null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @NonNull
    public Activity getActivity() {
        return mActivity;
    }

    public void destroy() {
        attemptStateTransition(DESTROYED);
    }

    public void setInterstitialAdListener(@Nullable final InterstitialAdListener listener) {
        mInterstitialAdListener = listener;
    }

    @Nullable
    public InterstitialAdListener getInterstitialAdListener() {
        return mInterstitialAdListener;
    }


    @Nullable
    protected CDAdVideoInterstitial.InterstitialVideoAdListener getInterstitialVideoAdListener() {
        return mInterstitialVideoAdListener;
    }

    protected void setInterstitialVideoAdListener(@Nullable final CDAdVideoInterstitial.InterstitialVideoAdListener interstitialVideoAdListener) {
        mInterstitialVideoAdListener = interstitialVideoAdListener;
    }

    /*
     * Implements CustomEventInterstitialAdapter.CustomEventInterstitialListener
     * Note: All callbacks should be no-ops if the interstitial has been destroyed
     */

    @Override
    public void onCustomEventInterstitialLoaded() {
        if (isDestroyed()) {
            return;
        }

        attemptStateTransition(READY);

        if (!isVideoAd){
            if (mInterstitialAdListener != null) {
                CDAdLog.d("REQUEST ID : "+mRequestId+" ,REFERENCE : "+Integer.toHexString(System.identityHashCode(this)), "onInterstitialLoaded");
                mInterstitialAdListener.onInterstitialLoaded(this);
            }
        }else{
            if (mInterstitialVideoAdListener !=null)
                CDAdLog.d("REQUEST ID : "+mRequestId+" ,REFERENCE : "+Integer.toHexString(System.identityHashCode(this)), "onInterstitialVideoLoaded");
                mInterstitialVideoAdListener.onInterstitialVideoLoaded((CDAdVideoInterstitial) CDAdInterstitial.this);
        }
    }

    @Override
    public void onCustomEventInterstitialFailed(@NonNull final CDAdErrorCode errorCode) {
        if (isDestroyed()) {
            return;
        }

        if (!mInterstitialView.loadFailUrl(errorCode)) {
            attemptStateTransition(IDLE);
        }
    }

    @Override
    public void onCustomEventInterstitialShown() {
        if (isDestroyed()) {
            return;
        }

        mInterstitialView.trackImpression();

        if (!isVideoAd){
            if (mInterstitialAdListener != null) {
                CDAdLog.d("REQUEST ID : "+mRequestId+" ,REFERENCE : "+Integer.toHexString(System.identityHashCode(this)), "onInterstitialShown");
                mInterstitialAdListener.onInterstitialShown(this);
            }
        }else{
            if (mInterstitialVideoAdListener !=null) {
                CDAdLog.d("REQUEST ID : "+mRequestId+" ,REFERENCE : "+Integer.toHexString(System.identityHashCode(this)), "onInterstitialVideoShown");
                mInterstitialVideoAdListener.onInterstitialVideoShown((CDAdVideoInterstitial) CDAdInterstitial.this);
            }
        }

    }

    @Override
    public void onCustomEventInterstitialClicked() {
        if (isDestroyed()) {
            return;
        }

        mInterstitialView.registerClick();

        if (!isVideoAd){
            if (mInterstitialAdListener != null) {
                CDAdLog.d("REQUEST ID : "+mRequestId+" ,REFERENCE : "+Integer.toHexString(System.identityHashCode(this)), "onInterstitialClicked");
                mInterstitialAdListener.onInterstitialClicked(this);
            }
        }else{
            if (mInterstitialVideoAdListener !=null) {
                CDAdLog.d("REQUEST ID : "+mRequestId+" ,REFERENCE : "+Integer.toHexString(System.identityHashCode(this)), "onInterstitialVideoClicked");
                mInterstitialVideoAdListener.onInterstitialVideoClicked((CDAdVideoInterstitial) CDAdInterstitial.this);
            }
        }
    }

    @Override
    public void onCustomEventInterstitialVideoEnded() {
        if (isDestroyed()) {
            return;
        }

        if (isVideoAd){
            if (mInterstitialVideoAdListener != null) {
                CDAdLog.d("REQUEST ID : "+mRequestId+" ,REFERENCE : "+Integer.toHexString(System.identityHashCode(this)), "onInterstitialVideoEnded");
                mInterstitialVideoAdListener.onInterstitialVideoEnded((CDAdVideoInterstitial) this);
            }
        }
    }

    @Override
    public void onCustomEventInterstitialDismissed() {
        boolean execute = false;
        synchronized (mIsDismissed) {
            if (!mIsDismissed.get()){
                mIsDismissed.set(true);
                execute = true;
            }
        }

        if (!execute){
            return;
        }

        if (isDestroyed()) {
            return;
        }

        attemptStateTransition(IDLE);

        if (!isVideoAd){
            if (mInterstitialAdListener != null) {
                CDAdLog.d("REQUEST ID : "+mRequestId+" ,REFERENCE : "+Integer.toHexString(System.identityHashCode(this)), "onInterstitialDismissed");
                mInterstitialAdListener.onInterstitialDismissed(this);
            }
        }else{
            if (mInterstitialVideoAdListener !=null) {
                CDAdLog.d("REQUEST ID : "+mRequestId+" ,REFERENCE : "+Integer.toHexString(System.identityHashCode(this)), "onInterstitialVideoDismissed");
                mInterstitialVideoAdListener.onInterstitialVideoDismissed((CDAdVideoInterstitial) CDAdInterstitial.this);
            }
        }

//        if (mInterstitialView!=null)
//            mInterstitialView.destroy();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public class CDAdInterstitialView extends CDAdView {
        private CDAdRequest mCDAdRequest;
        public CDAdInterstitialView(Context context) {
            super(context);
            setLocationAutoUpdateEnabled(false);
        }

        @Override
        public boolean canReload(){
//            if (shouldSkipStateCheck){
//                shouldSkipStateCheck = false;
//                return true;
//            }else{
//                return attemptStateTransition(LOADING);
//            }
            return true;
        }

        @Nullable
        String getCustomEventClassName() {
            return mAdViewController.getCustomEventClassName();
        }

        @Override
        public AdFormat getAdFormat() {
            return AdFormat.INTERSTITIAL;
        }

        @Override
        protected void loadCustomEvent(String customEventClassName, Map<String, String> serverExtras, CDMediationAdRequest cdMediationAdRequest, @NonNull final Event[] events) {
            if (mAdViewController == null) {
                return;
            }

            if (TextUtils.isEmpty(customEventClassName)) {
                CDAdLog.d("Couldn't invoke custom event because the server did not specify one.");
                loadFailUrl(ADAPTER_NOT_FOUND);
                return;
            }

            if (mCustomEventInterstitialAdapter != null) {
                mCustomEventInterstitialAdapter.invalidate();
            }

            CDAdLog.d("Loading custom event interstitial adapter.");

            mCustomEventInterstitialAdapter = CustomEventInterstitialAdapterFactory.create(
                    CDAdInterstitial.this,
                    customEventClassName,
                    serverExtras,
                    mAdViewController.getBroadcastIdentifier(),cdMediationAdRequest, cdAdSize, events);
            mCustomEventInterstitialAdapter.setAdapterListener(CDAdInterstitial.this);
            mCustomEventInterstitialAdapter.loadInterstitial();
        }

        protected void trackImpression() {
            CDAdLog.d("Tracking impression for interstitial.");
            if (mAdViewController != null) mAdViewController.trackImpression();
        }

        @Override
        protected void adFailed(CDAdErrorCode errorCode) {
            attemptStateTransition(IDLE);

            if (!isVideoAd){
                if (mInterstitialAdListener != null) {
                    CDAdLog.d("REQUEST ID : "+mRequestId+" ,REFERENCE : "+Integer.toHexString(System.identityHashCode(CDAdInterstitial.this)), "onInterstitialFailed");
                    mInterstitialAdListener.onInterstitialFailed(CDAdInterstitial.this, errorCode);
                }
            }else{
                if (mInterstitialVideoAdListener !=null) {
                    CDAdLog.d("REQUEST ID : "+mRequestId+" ,REFERENCE : "+Integer.toHexString(System.identityHashCode(CDAdInterstitial.this)), "onInterstitialVideoFailed");
                    mInterstitialVideoAdListener.onInterstitialVideoFailed((CDAdVideoInterstitial) CDAdInterstitial.this, errorCode);
                }
            }
        }

        @Override
        public Map<String, Object> getParams(@NonNull CDAdRequest cdAdRequest){
            HashMap<String, Object> map = cdAdRequest.getParams();
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            CDAdDeviceInfo cdAdDeviceInfo = CDAdDeviceInfo.deviceInfo(mContext);
            if (cdAdSize == null){
                if (cdAdDeviceInfo.getH()<1024){
                    map.put(DataKeys.HEIGHT, 480);
                    map.put(DataKeys.WIDTH, 320);
                    cdAdSize = CDAdSize.getSizeFromCDSizeConstant(CDAdSize.CDAdSizeConstant.CDAdSize320X480);
                }else {
                    map.put(DataKeys.HEIGHT, 1024);
                    map.put(DataKeys.WIDTH, 768);
                    cdAdSize = CDAdSize.getSizeFromCDSizeConstant(CDAdSize.CDAdSizeConstant.CDAdSize768X1024);
                }
            }
            else{
                map.put(DataKeys.HEIGHT, cdAdSize.getHeight());
                map.put(DataKeys.WIDTH, cdAdSize.getWidth());
            }
            int orientation = DeviceUtils.getScreenOrientation(mActivity.getApplicationContext());
            if (ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE == orientation || ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE == orientation){
                Object temp = map.get(DataKeys.HEIGHT);
                map.put(DataKeys.HEIGHT, map.get(DataKeys.WIDTH));
                map.put(DataKeys.WIDTH, temp);
            }

            map.put(DataKeys.AD_TYPE, AdResponse.CDAdType.getValue(this.getCDADType()));
            if (this.getCDADType()== AdResponse.CDAdType.CDAdTypeInterstitialVideo)
                map.put(DataKeys.VIDEO_CONF, cdAdRequest.videoConfiguration);
            return map;
        }

        @Override
        public AdResponse.CDAdType getCDADType(){
            return (mCDAdRequest.videoConfiguration !=null?AdResponse.CDAdType.CDAdTypeInterstitialVideo:AdResponse.CDAdType.CDAdTypeInterstitial);
        }

        @Override
        protected void loadRequest(@NonNull CDAdRequest cdAdRequest) {
            mCDAdRequest = cdAdRequest;
            if (!isVideoAd){
                if (mInterstitialAdListener !=null) {
                    CDAdLog.d("REQUEST ID : "+mRequestId+" ,REFERENCE : "+Integer.toHexString(System.identityHashCode(CDAdInterstitial.this)), "onInterstitialAdRequest");
                    mInterstitialAdListener.onInterstitialAdRequest(CDAdInterstitial.this);
                }
            }else{
                if (mInterstitialVideoAdListener !=null) {
                    CDAdLog.d("REQUEST ID : "+mRequestId+" ,REFERENCE : "+Integer.toHexString(System.identityHashCode(CDAdInterstitial.this)), "onInterstitialVideoAdRequest");
                    mInterstitialVideoAdListener.onInterstitialVideoAdRequest((CDAdVideoInterstitial) CDAdInterstitial.this);
                }
            }
            
            if (mAdViewController != null) {
                mAdViewController.loadRequest(cdAdRequest);
            }
        }
    }

    @VisibleForTesting
    @Deprecated
    void setHandler(@NonNull final Handler handler) {
        mHandler = handler;
    }

    @VisibleForTesting
    @Deprecated
    void setInterstitialView(@NonNull CDAdInterstitialView interstitialView) {
        mInterstitialView = interstitialView;
    }

    @VisibleForTesting
    @Deprecated
    void setCurrentInterstitialState(@NonNull final InterstitialState interstitialState) {
        mCurrentInterstitialState = interstitialState;
    }

    @VisibleForTesting
    @Deprecated
    @NonNull
    InterstitialState getCurrentInterstitialState() {
        return mCurrentInterstitialState;
    }

    @VisibleForTesting
    @Deprecated
    void setCustomEventInterstitialAdapter(@NonNull final CustomEventInterstitialAdapter
            customEventInterstitialAdapter) {
        mCustomEventInterstitialAdapter = customEventInterstitialAdapter;
    }

    @Override
    public void onCustomEventRewardsUnlocked(Map<Object, Object> map) {

    }
}
