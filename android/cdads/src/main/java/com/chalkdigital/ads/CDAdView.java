package com.chalkdigital.ads;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.chalkdigital.ads.factories.AdViewControllerFactory;
import com.chalkdigital.common.CDAdGeoInfo;
import com.chalkdigital.common.CDAdRequest;
import com.chalkdigital.common.CDAdSize;
import com.chalkdigital.common.CDAdsUtils;
import com.chalkdigital.common.AdFormat;
import com.chalkdigital.common.AdReport;
import com.chalkdigital.common.DataKeys;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.ManifestUtils;
import com.chalkdigital.common.util.Reflection;
import com.chalkdigital.common.util.Utils;
import com.chalkdigital.common.util.Visibility;
import com.chalkdigital.mediation.CDMediationAdRequest;
import com.chalkdigital.network.response.AdResponse;
import com.chalkdigital.network.response.Event;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.chalkdigital.R;

import static com.chalkdigital.ads.CDAdErrorCode.ADAPTER_NOT_FOUND;

public class CDAdView extends FrameLayout {

    private CDAdSize.CDAdSizeConstant cdAdSize;
    private Integer refreshInterval;
    private boolean locationAutoUpdateEnabled;
    static final int DEFAULT_REFRESH_TIME = 30;  // 30seconds
    protected boolean mTesting;
    protected String mBundleIdentifier;
    protected String mCategory;
    protected CDAdGeoInfo mCDAdGeoInfo;


    public interface CDAdViewListener {
        /**
         * This method is called when an ad request is initiated on CDAdView object.
         * @param banner reference of banner on which ad request is initiated.
         */
        void onBannerAdRequest(CDAdView banner);

        /**
         * This method is called when an ad is loaded for an CDAdView object.
         * @param banner reference of banner for which ad is loaded.
         */
        void onBannerLoaded(CDAdView banner);

        /**
         * This method is called when an ad request is failed for an CDAdView object.
         * @param banner reference of banner for which ad is failed.
         * @param errorCode reason of ad request failure.
         */
        void onBannerFailed(CDAdView banner, CDAdErrorCode errorCode);

        /**
         * This method is called when an ad is clicked on CDAdView object.
         * @param banner reference of banner on which ad is clicked.
         */
        void onBannerClicked(CDAdView banner);

        /**
         * This method is called when an ad is expanded on CDAdView object. This callback will be fired for MRAID ads only.
         * @param banner reference of banner on which ad is expanded.
         */
        void onBannerExpanded(CDAdView banner);

        /**
         * This method is called when an ad is collapsed on CDAdView object. This callback will be fired for MRAID ads only.
         * @param banner reference of banner on which ad is collapsed.
         */
        void onBannerCollapsed(CDAdView banner);
    }

    /**
     * Enable testing mode for banner.
     * <p/>
     *
     * @param testing boolean value to enable testing mode.
     */
    public void setTesting(final boolean testing) {
        mTesting = testing;
    }

    /**
     * Set Bundle Identifier for a banner.
     * Note : This will work only when testing ads are requested.
     * <p/>
     *
     * @param bundleIdentifier The bundle identifier of application. Its default value would be the bundle identifier of the app in which you are using CDAdView.
     */
    public void setBundleIdentifier(@NonNull final String bundleIdentifier) {
        mBundleIdentifier = bundleIdentifier;
    }

    /**
     * Set category for a banner.
     * <p/>
     *
     * @param category The IAB category of application.
     */
    public void setCategory(@NonNull final String category) {
        mCategory = category;
    }

    /**
     * Set CDAdGeoInfo for a banner.
     * <p/>
     *
     * @param CDAdGeoInfo The CDAdGeoInfo object to set ad location. Use this only if automatic location updates are disabled.
     */
    public void setCDAdGeoInfo(@NonNull final CDAdGeoInfo CDAdGeoInfo) {
        mCDAdGeoInfo = CDAdGeoInfo;
    }

    public boolean canReload(){
        return true;
    }

    /**
     * Check automatic location updates for a banner request.
     * <p/>
     *
     * @return locationAutoUpdateEnabled boolean value true if automatic location updates are on.
     */
    public boolean isLocationAutoUpdateEnabled() {
        return locationAutoUpdateEnabled;
    }

    /**
     * Set automatic location updates for a banner.
     * <p/>
     *
     * @param locationAutoUpdateEnabled set this boolean value true if you want ad request to fetch location automatically which fetching ad request.
     */
    public void setLocationAutoUpdateEnabled(boolean locationAutoUpdateEnabled) {
        this.locationAutoUpdateEnabled = locationAutoUpdateEnabled;
    }

    /**
     * Set automatic ad refresh for a banner.
     * <p/>
     *
     * @param adAutoRefreshEnabled set this boolean value true if you want banner to fetch ad automatically after predefined refreshInterval .
     */
    public void setAdAutoRefreshEnabled(boolean adAutoRefreshEnabled){
        if (mAdViewController != null) {
            mAdViewController.setAdAutoRefreshEnabled(adAutoRefreshEnabled);
        }
    }

    /**
     * Get refresh interval for a banner.
     * <p/>
     *
     * @return refreshInterval Integer value which specifies the time interval after which banner will refresh ad automatically. .
     */
    public Integer getRefreshInterval() {
        return refreshInterval;
    }

    /**
     * Set refresh interval for a banner.
     * <p/>
     *
     * @param refreshInterval Integer value which specifies the time interval in seconds after which banner will refresh ad automatically.
     *                        Maximum value 60 seconds
     *                        Minimum value 10 seconds
     */
    public void setRefreshInterval(Integer refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    /**
     * Get CDAdSize.CDAdSizeConstant for a banner.
     * <p/>
     *
     * @return cdAdSize CDAdSize.CDAdSizeConstant for a banner.
     */
    public CDAdSize.CDAdSizeConstant getCDAdSize() {
        return cdAdSize;
    }

    /**
     * Set CDAdSize.CDAdSizeConstant for a banner.
     * <p/>
     *
     * @param cdAdSize CDAdSize.CDAdSizeConstant for a banner. Its default value is CDAdSize320X50.
     */
    public void setCDAdSize(CDAdSize.CDAdSizeConstant cdAdSize) {
        this.cdAdSize = cdAdSize;
    }

    private static final String CUSTOM_EVENT_BANNER_ADAPTER_FACTORY =
            "com.chalkdigital.banner.ads.factories.CustomEventBannerAdapterFactory";

    @Nullable
    protected AdViewController mAdViewController;
    // mCustomEventBannerAdapter must be a CustomEventBannerAdapter
    protected Object mCustomEventBannerAdapter;

    protected Context mContext;
    private int mScreenVisibility;
    private BroadcastReceiver mScreenStateReceiver;

    private CDAdViewListener mCDAdViewListener;

    public CDAdView(Context context) {
        this(context, null);
    }

    public CDAdView(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public CDAdView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CDAdView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        initialise(context, attrs, defStyleAttr);
    }

    private void initialise(Context context, AttributeSet attrs, int defStyleAttr){
        setTesting(false);
        try {
            setBundleIdentifier(getResources().getText(getResources().getIdentifier("CDADS_BUNDLE_ID", "string", context.getPackageName())).toString());
        } catch (Exception e) {
            setBundleIdentifier("");
        }
        try {
            setCategory(getResources().getText(getResources().getIdentifier("CDADS_CAT", "string", context.getPackageName())).toString());
        } catch (Exception e) {
            setCategory("");
        }
        setCDAdGeoInfo(null);

        refreshInterval = DEFAULT_REFRESH_TIME;
        ManifestUtils.checkWebViewActivitiesDeclared(context);

        mContext = context;
        mScreenVisibility = getVisibility();

        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);

        int adSize = 0;
        if (attrs!=null){
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.CDADBannerViewStyle, defStyleAttr, 0);
            try {
                adSize = ta.getInteger(R.styleable.CDADBannerViewStyle_cdAdSize, 0);
            } finally {
                ta.recycle();
            }
        }
        switch (adSize){
            case 0:
                cdAdSize = CDAdSize.CDAdSizeConstant.CDAdSize320X50;
                break;
            case 1:
                cdAdSize = CDAdSize.CDAdSizeConstant.CDAdSize300X50;
                break;
            case 2:
                cdAdSize = CDAdSize.CDAdSizeConstant.CDAdSize300X250;
                break;
            case 3:
                cdAdSize = CDAdSize.CDAdSizeConstant.CDAdSize320X100;
                break;
            case 4:
                cdAdSize = CDAdSize.CDAdSizeConstant.CDAdSize728X90;
                break;
            case 5:
                cdAdSize = CDAdSize.CDAdSizeConstant.CDAdSize728X250;
                break;
            case 6:
                cdAdSize = CDAdSize.CDAdSizeConstant.CDAdSize320X480;
                break;
            case 7:
                cdAdSize = CDAdSize.CDAdSizeConstant.CDAdSize768X1024;
                break;
        }
        setClipToPadding(false);
        setClipChildren(false);
//        try {
//            // There is a rare bug in Froyo/2.2 where creation of a WebView causes a
//            // NullPointerException. (https://code.google.com/p/android/issues/detail?id=10789)
//            // It happens when the WebView can't access the local file store to make a cache file.
//            // Here, we'll work around it by trying to create a file store and then just go inert
//            // if it's not accessible.
//            if (WebViewDatabase.getInstance(context) == null) {
//                CDAdLog.e("Disabling CDAd. Local cache file is inaccessible so CDAd will " +
//                        "fail if we try to create a WebView. Details of this Android bug found at:" +
//                        "https://code.google.com/p/android/issues/detail?id=10789");
//                return;
//            }
//        } catch (Exception e) {
//                        Utils.logStackTrace(e);
//            // If anything goes wrong here, it's most likely due to not having a WebView at all.
//            // This happens when Android updates WebView.
//            CDAdLog.e("Disabling CDAd due to no WebView, or it's being updated", e);
//            return;
//        }

        mAdViewController = AdViewControllerFactory.create(context, this);
        registerScreenStateBroadcastReceiver();
    }

    private void registerScreenStateBroadcastReceiver() {
        mScreenStateReceiver = new BroadcastReceiver() {
            public void onReceive(final Context context, final Intent intent) {
                if (!Visibility.isScreenVisible(mScreenVisibility) || intent == null) {
                    return;
                }

                final String action = intent.getAction();

                if (Intent.ACTION_USER_PRESENT.equals(action)) {
                    setAdVisibility(View.VISIBLE);
                } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    setAdVisibility(View.GONE);
                }
            }
        };

        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        mContext.registerReceiver(mScreenStateReceiver, filter);
    }

    private void unregisterScreenStateBroadcastReceiver() {
        try {
            mContext.unregisterReceiver(mScreenStateReceiver);
        } catch (Exception IllegalArgumentException) {
            CDAdLog.d("Failed to unregister screen state broadcast receiver (never registered).");
        }
    }

    /**
     * Request a banner ad.
     * <p/>
     *
     * @param targetingParams targeting parameters passed as key value pair.
     * @param partnerId partner id of publisher.
     * @param placementId placement id of this ad.
     *
     *
     */
    public void requestNewAd(Map<String, String> targetingParams, String partnerId, String placementId){
        try {
            if (!CDAdsUtils.initialised)
            {
                CDAdLog.e(CDAdErrorCode.CDADS_NOT_INITIALIZED.toString());
                if (mCDAdViewListener!=null)
                    mCDAdViewListener.onBannerFailed(this, CDAdErrorCode.CDADS_NOT_INITIALIZED);
                return;
            }
            CDAdRequest cdAdRequest = new CDAdRequest.Builder().build(mContext);
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
            cdAdRequest.cat = mCategory;
            cdAdRequest.bundleId = mBundleIdentifier;
            cdAdRequest.geoInfo = mCDAdGeoInfo;

            cdAdRequest.locationAutoUpdateEnabled = locationAutoUpdateEnabled;
            loadRequest(cdAdRequest);
        } catch (Throwable throwable) {
            Utils.logStackTrace(throwable);
        }

    }

    protected void loadRequest(@NonNull CDAdRequest cdAdRequest){
        if (mCDAdViewListener !=null)
            mCDAdViewListener.onBannerAdRequest(this);

        if (mAdViewController != null) {
            mAdViewController.loadRequest(cdAdRequest);
        }
    }

    protected Map<String, Object> getParams(@NonNull CDAdRequest cdAdRequest){
        HashMap<String, Object> map = cdAdRequest.getParams();
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        CDAdSize adRequestSize = CDAdSize.getSizeFromCDSizeConstant(cdAdSize);
        map.put(DataKeys.HEIGHT, adRequestSize.getHeight());
        map.put(DataKeys.WIDTH, adRequestSize.getWidth());
        map.put(DataKeys.AD_TYPE, AdResponse.CDAdType.getValue(AdResponse.CDAdType.CDAdTypeBanner));
        return map;
    }

    /*
     * Tears down the ad view: no ads will be shown once this method executes. The parent
     * Activity's onDestroy implementation must include a call to this method.
     */
    public void destroy() {
        unregisterScreenStateBroadcastReceiver();
        removeAllViews();

        if (mAdViewController != null) {
            mAdViewController.cleanup();
            mAdViewController = null;
        }

        if (mCustomEventBannerAdapter != null) {
            invalidateAdapter();
            mCustomEventBannerAdapter = null;
        }
    }

    private void invalidateAdapter() {
        if (mCustomEventBannerAdapter != null) {
            try {
                new Reflection.MethodBuilder(mCustomEventBannerAdapter, "invalidate")
                        .setAccessible()
                        .execute();
            } catch (Exception e) {
                        Utils.logStackTrace(e);
                CDAdLog.e("Error invalidating adapter", e);
            }
        }
    }

    public Integer getAdTimeoutDelay() {
        return (mAdViewController != null) ? mAdViewController.getAdTimeoutDelay() : null;
    }

    public boolean loadFailUrl(@NonNull final CDAdErrorCode errorCode) {
        if (mAdViewController == null) {
            return false;
        }
        return mAdViewController.loadFailUrl(errorCode);
    }

    protected void loadCustomEvent(String customEventClassName, Map<String, String> serverExtras, CDMediationAdRequest cdMediationAdRequest, @NonNull final Event[] events) {
        if (mAdViewController == null) {
            return;
        }
        if (customEventClassName == null) {
            CDAdLog.d("Couldn't invoke custom event because the server did not specify one.");
            loadFailUrl(ADAPTER_NOT_FOUND);
            return;
        }

        if (mCustomEventBannerAdapter != null) {
            invalidateAdapter();
        }

        CDAdLog.d("Loading custom event adapter.");

        if (Reflection.classFound(CUSTOM_EVENT_BANNER_ADAPTER_FACTORY)) {
            try {
                final Class<?> adapterFactoryClass = Class.forName(CUSTOM_EVENT_BANNER_ADAPTER_FACTORY);
                mCustomEventBannerAdapter = new Reflection.MethodBuilder(null, "create")
                        .setStatic(adapterFactoryClass)
                        .addParam(CDAdView.class, this)
                        .addParam(String.class, customEventClassName)
                        .addParam(Map.class, serverExtras)
                        .addParam(long.class, mAdViewController.getBroadcastIdentifier())
                        .addParam(CDAdSize.class, CDAdSize.getSizeFromCDSizeConstant(cdAdSize))
                        .addParam(CDMediationAdRequest.class, cdMediationAdRequest)
                        .addParam(Event[].class, events)
                        .execute();
                new Reflection.MethodBuilder(mCustomEventBannerAdapter, "loadAd")
                        .setAccessible()
                        .execute();
            } catch (Exception e) {
                        Utils.logStackTrace(e);
                CDAdLog.e("Error loading custom event", e);
            }
        } else {
            CDAdLog.e("Could not load custom event -- missing banner module");
        }
    }

    public void registerClick() {
        if (mAdViewController != null) {
            mAdViewController.registerClick();

            // Let any listeners know that an ad was clicked
            adClicked();
        }
    }

    public void trackNativeImpression() {
        CDAdLog.d("Tracking impression for native adapter.");
        if (mAdViewController != null) mAdViewController.trackImpression();
    }

    @Override
    protected void onWindowVisibilityChanged(final int visibility) {
        // Ignore transitions between View.GONE and View.INVISIBLE
        if (Visibility.hasScreenVisibilityChanged(mScreenVisibility, visibility)) {
            mScreenVisibility = visibility;
            setAdVisibility(mScreenVisibility);
        }
    }

    private void setAdVisibility(final int visibility) {
        if (mAdViewController == null) {
            return;
        }

        if (Visibility.isScreenVisible(visibility)) {
            mAdViewController.resumeRefresh();
        } else {
            mAdViewController.pauseRefresh();
        }
    }

    protected void adLoaded() {
        CDAdLog.d("adLoaded");
        setVisibility(VISIBLE);
        if (mCDAdViewListener != null) {
            mCDAdViewListener.onBannerLoaded(this);
        }
    }

    protected void adFailed(CDAdErrorCode errorCode) {
        if (mCDAdViewListener != null) {
            mCDAdViewListener.onBannerFailed(this, errorCode);
        }
    }

    public void adPresentedOverlay() {
        if (mCDAdViewListener != null) {
            mCDAdViewListener.onBannerExpanded(this);
        }
    }

    public void adClosed() {
        if (mCDAdViewListener != null) {
            mCDAdViewListener.onBannerCollapsed(this);
        }
    }

    protected void adClicked() {
        if (mCDAdViewListener != null) {
            mCDAdViewListener.onBannerClicked(this);
        }
    }

    public void nativeAdLoaded() {
        if (mAdViewController != null) mAdViewController.scheduleRefreshTimerIfEnabled();
        adLoaded();
    }


    public Activity getActivity() {
        return (Activity) mContext;
    }

    /**
     * Set CDAdViewListener for a banner to receive ad related callbacks.
     * <p/>
     *
     * @param listener valid reference of CDAdViewListener.
     *
     */
    public void setCDAdViewListener(CDAdViewListener listener) {
        mCDAdViewListener = listener;
    }

    /**
     * Get CDAdViewListener for a banner to receive ad related callbacks.
     * <p/>
     *
     * @return reference of CDAdViewListener.
     *
     */
    public CDAdViewListener getCDAdViewListener() {
        return mCDAdViewListener;
    }

    public boolean getAutorefreshEnabled() {
        if (mAdViewController != null) return mAdViewController.isAutoRefreshEnabled();
        else {
            CDAdLog.d("Can't get autorefresh status for destroyed CDAdView. " +
                    "Returning false.");
            return false;
        }
    }

    public void setAdContentView(View view) {
        if (mAdViewController != null) mAdViewController.setAdContentView(view);
    }


    public AdResponse.CDAdType getCDADType(){
        return AdResponse.CDAdType.CDAdTypeBanner;
    }

    /**
     * Force refresh a banner ad, this will discard the current ad request and attempt a fresh request for banner.
     */
    public void forceRefresh() {
        if (mCustomEventBannerAdapter != null) {
            invalidateAdapter();
            mCustomEventBannerAdapter = null;
        }

        if (mAdViewController != null) {
            mAdViewController.forceRefresh();
        }
    }

    AdViewController getAdViewController() {
        return mAdViewController;
    }

    public AdFormat getAdFormat() {
        return AdFormat.BANNER;
    }

    /**
     * @deprecated As of release 4.4.0
     */
    @Deprecated
    public void setTimeout(int milliseconds) {
    }

    @Deprecated
    public String getResponseString() {
        return null;
    }

    @Deprecated
    public String getClickTrackingUrl() {
        return null;
    }


}
