package com.chalkdigital.common;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;

import com.chalkdigital.analytics.mobile.CDAdAnalytics;
import com.chalkdigital.analytics.mobile.CDAdAnalyticsListener;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.Reflection;
import com.chalkdigital.common.util.Utils;
import com.chalkdigital.network.retrofit.CDAdCallback;
import com.chalkdigital.network.retrofit.CDAdRetrofit;
import com.chalkdigital.network.retrofit.CDAdService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Response;

// API documentation: https://drive.google.com/drive/folders/0B8U0thTyT1GGLUlweWRVMXk1Qlk
class CDAdViewabilitySession implements ExternalViewabilitySession, CDAdAnalyticsListener{
    private static final String CDAD_FACTORY_PATH = "com.chalkdigital.analytics.mobile.CDAdFactory";
    private static final String CDAD_OPTIONS_PATH = "com.chalkdigital.analytics.mobile.CDAdOptions";
    private static final String CDAD_ANALYTICS_PATH = "com.chalkdigital.analytics.mobile.CDAdAnalytics";
    private static final String CDAD_AD_EVENT_PATH = "com.chalkdigital.analytics.mobile.CDAdAdEvent";
    private static final String CDAD_AD_EVENT_TYPE_PATH = "com.chalkdigital.analytics.mobile.CDAdAdEventType";
    private static final String CDAD_REACTIVE_VIDEO_TRACKER_PLUGIN_PATH = "com.chalkdigital.analytics.mobile.ReactiveVideoTrackerPlugin";
    private static final String CDAD_PLUGIN_PATH = "com.chalkdigital.analytics.mobile.CDAdPlugin";

    private static final String PARTNER_CODE_KEY = "partnerCode";
    // CDAd's partner identifier with CDAd. Partner code is normally parsed from the video
    // viewability tracking URL, but in case of error, this default value is used instead.
    private static final String DEFAULT_PARTNER_CODE = "cdadsinapphtmvideo468906546585";
    private static final String CDAD_KEY = "cdAd";
    private static final String CDAD_VAST_IDS_KEY = "zCDAdVASTIDs";

    private static Boolean sIsViewabilityEnabledViaReflection;
    private static boolean sIsVendorDisabled;
    private static boolean sWasInitialized = false;

    private static final Map<String, String> QUERY_PARAM_MAPPING = new HashMap<String, String>();
    static {
        QUERY_PARAM_MAPPING.put("cdAdClientLevel1", "level1");
        QUERY_PARAM_MAPPING.put("cdAdClientLevel2", "level2");
        QUERY_PARAM_MAPPING.put("cdAdClientLevel3", "level3");
        QUERY_PARAM_MAPPING.put("cdAdClientLevel4", "level4");
        QUERY_PARAM_MAPPING.put("cdAdClientSlicer1", "slicer1");
        QUERY_PARAM_MAPPING.put("cdAdClientSlicer2", "slicer2");
    }

    @Nullable private Object mCDAdWebAdTracker;
    @Nullable private Object mCDAdVideoTracker;
    @NonNull private Map<String, String> mAdIds = new HashMap<String, String>();
    private HashMap<String, String[]> mEvents;
    private boolean mWasVideoPrepared;
    private Context mContext;

    static boolean isEnabled() {
        return !sIsVendorDisabled && isViewabilityEnabledViaReflection();
    }

    static void disable() {
        sIsVendorDisabled = true;
    }

    private static boolean isViewabilityEnabledViaReflection() {
        if (sIsViewabilityEnabledViaReflection == null) {
            sIsViewabilityEnabledViaReflection = Reflection.classFound(CDAD_FACTORY_PATH);
            CDAdLog.d("CDAdViewability is "
                    + (sIsViewabilityEnabledViaReflection ? "" : "un")
                    + "available via reflection.");
        }

        return sIsViewabilityEnabledViaReflection;
    }

    @Override
    @NonNull
    public String getName() {
        return "CDAd";
    }

    public CDAdViewabilitySession() {

    }

    @Override
    @Nullable
    public Boolean initialize(@NonNull final Context context) {
        Preconditions.checkNotNull(context);
        mContext = context;
        if (!isEnabled()) {
            return null;
        }

        if (sWasInitialized) {
            return true;
        }

        final Application application;
        if (context instanceof Activity) {
            application = ((Activity) context).getApplication();
        } else {
            try {
                application = (Application) context.getApplicationContext();
            } catch (ClassCastException e) {
                        Utils.logStackTrace(e);
                CDAdLog.d("Unable to initialize CDAd, error obtaining application context.");
                return false;
            }
        }

        // Pre-reflection code:
        // final CDAdOptions options = new CDAdOptions();
        // options.disableAdIdCollection = true;
        // CDAdAnalytics.getInstance().start(options, application);

        try {
            Object cdAdOptions = Reflection.instantiateClassWithEmptyConstructor(CDAD_OPTIONS_PATH,
                    Object.class);

            cdAdOptions.getClass().getField("disableAdIdCollection")
                    .setBoolean(cdAdOptions, true);

            Object cdAdAnalytics = new Reflection.MethodBuilder(null, "getInstance")
                    .setStatic(CDAD_ANALYTICS_PATH)
                    .execute();

            new Reflection.MethodBuilder(cdAdAnalytics, "start")
                    .addParam(CDAD_OPTIONS_PATH, cdAdOptions)
                    .addParam(Application.class, application)
                    .execute();

            sWasInitialized = true;
            return true;
        } catch (Exception e) {
                        Utils.logStackTrace(e);
            CDAdLog.d("Unable to initialize CDAd: " + e.getMessage());
            return false;
        }
    }

    @Override
    @Nullable
    public Boolean invalidate() {
        if (!isEnabled()) {
            return null;
        }

        mCDAdWebAdTracker = null;
        mCDAdVideoTracker = null;
        mAdIds.clear();

        return true;
    }

    @Override
    @Nullable
    public Boolean createDisplaySession(@NonNull final Context context,
                                        @NonNull final WebView webView, boolean isDeferred) {
        Preconditions.checkNotNull(context);

        if (!isEnabled()) {
            return null;
        }

        // Pre-reflection code:
        // mCDAdWebAdTracker = CDAdFactory.create().createWebAdTracker(webView);
        // if (!isDeferred) {
        //     mCDAdWebAdTracker.startTracking();
        // }

        try {
            Object cdAdFactory = new Reflection.MethodBuilder(null, "create")
                    .setStatic(CDAD_FACTORY_PATH)
                    .execute();

            mCDAdWebAdTracker = new Reflection.MethodBuilder(cdAdFactory, "createWebAdTracker")
                    .addParam(WebView.class, webView)
                    .addParam(CDAdAnalyticsListener.class, this)
                    .execute();

            // If we're not dealing with a deferred session, start tracking now
            if (!isDeferred) {
//                new Reflection.MethodBuilder(mCDAdWebAdTracker, "startTracking").execute();
            }

            return true;
        } catch (Exception e) {
                        Utils.logStackTrace(e);
            CDAdLog.d("Unable to execute CDAd start display session: "
                    + e.getMessage());
            return false;
        }
    }

    @Override
    @Nullable
    public Boolean startDeferredDisplaySession(@NonNull final Activity activity) {
        if (!isEnabled()) {
            return null;
        }

        if (mCDAdWebAdTracker == null) {
            CDAdLog.d("CDAdWebAdTracker unexpectedly null.");
            return false;
        }

        // Pre-reflection code:
        // mCDAdWebAdTracker.startTracking();

        try {
//            new Reflection.MethodBuilder(mCDAdWebAdTracker, "startTracking").execute();

            return true;
        } catch (Exception e) {
                        Utils.logStackTrace(e);
            CDAdLog.d("Unable to record deferred display session for CDAd: " + e.getMessage());
            return false;
        }
    }

    @Override
    @Nullable
    public Boolean endDisplaySession() {
        if (!isEnabled()) {
            return null;
        }

        if (mCDAdWebAdTracker == null) {
            CDAdLog.d("CDAd WebAdTracker unexpectedly null.");
            return false;
        }

        // Pre-reflection code:
        // mCDAdWebAdTracker.stopTracking();

        try {
//            new Reflection.MethodBuilder(mCDAdWebAdTracker, "stopTracking").execute();

            return true;
        } catch (Exception e) {
                        Utils.logStackTrace(e);
            CDAdLog.d("Unable to execute CDAd end session: " + e.getMessage());
        }

        return false;
    }

    @Override
    @Nullable
    public Boolean createVideoSession(@NonNull final Activity activity, @NonNull final View view,
                                      @NonNull final Set<String> buyerResources,
                                      @NonNull final Map<String, String> videoViewabilityTrackers) {
//        Preconditions.checkNotNull(activity);
//        Preconditions.checkNotNull(view);
//        Preconditions.checkNotNull(buyerResources);
//        Preconditions.checkNotNull(videoViewabilityTrackers);
//
//        if (!isEnabled()) {
//            return null;
//        }
//
//        updateAdIdsFromUrlStringAndBuyerResources(videoViewabilityTrackers.get(CDAD_KEY),
//                buyerResources);
//
//        String partnerCode = mAdIds.get(PARTNER_CODE_KEY);
//        if (TextUtils.isEmpty(partnerCode)) {
//            CDAdLog.d("partnerCode was empty when starting CDAd video session");
//            return false;
//        }
//
//        // Pre-reflection code:
//        // CDAdPlugin cdAdPlugin = new ReactiveVideoTrackerPlugin(partnerCode);
//        // mCDAdVideoTracker = CDAdFactory.create().createCustomTracker(cdAdPlugin);
//
//        try {
//            final Object cdAdPlugin = Reflection.instantiateClassWithConstructor(
//                    CDAD_REACTIVE_VIDEO_TRACKER_PLUGIN_PATH, Object.class,
//                    new Class[]{String.class}, new Object[]{partnerCode});
//
//            final Object cdAdFactory = new Reflection.MethodBuilder(null, "create")
//                    .setStatic(CDAD_FACTORY_PATH)
//                    .execute();
//
//            mCDAdVideoTracker = new Reflection.MethodBuilder(cdAdFactory, "createCustomTracker")
//                    .addParam(CDAD_PLUGIN_PATH, cdAdPlugin)
//                    .execute();

            return true;
//        } catch (Exception e) {
//                        Utils.logStackTrace(e);
//            CDAdLog.d("Unable to execute CDAd start video session: " + e.getMessage());
//            return false;
//        }
    }

    @Override
    @Nullable
    public Boolean registerVideoObstructions(@NonNull final List<View> views) {
//        Preconditions.checkNotNull(views);
//
//        if (!isEnabled()) {
//            return null;
//        }
//
//        // unimplemented by CDAd
        return true;
    }

    @Override
    @Nullable
    public Boolean onVideoPrepared(@NonNull final View playerView, final Integer duration, final Integer volume) {
//        Preconditions.checkNotNull(playerView);
//
//        if (!isEnabled()) {
//            return null;
//        }
//
//        if (mCDAdVideoTracker == null) {
//            CDAdLog.d("CDAd VideoAdTracker unexpectedly null.");
//            return false;
//        }
//
//        if (mWasVideoPrepared) {
//            return false;
//        }
//
//        // Pre-reflection code:
//        // mCDAdVideoTracker.trackVideoAd(mAdIds, duration, playerView);
//
//        try {
//            new Reflection.MethodBuilder(mCDAdVideoTracker, "trackVideoAd")
//                    .addParam(Map.class, mAdIds)
//                    .addParam(Integer.class, duration)
//                    .addParam(View.class, playerView)
//                    .execute();
//            mWasVideoPrepared = true;
//            return true;
//        } catch (Exception e) {
//                        Utils.logStackTrace(e);
//            CDAdLog.d("Unable to execute CDAd onVideoPrepared: " + e.getMessage());
//            return false;
//        }
        return true;
    }

    @Override
    @Nullable
    public Boolean recordVideoEvent(@NonNull final VideoEvent event, final Integer duration, final Integer playHeadMillis, final Integer volume) {
//        Preconditions.checkNotNull(event);
//
//        if (!isEnabled()) {
//            return null;
//        }
//
//        if (mCDAdVideoTracker == null) {
//            CDAdLog.d("CDAd VideoAdTracker unexpectedly null.");
//            return false;
//        }
//
//        try {
//            switch (event) {
//                case AD_STARTED:
//                case AD_STOPPED:
//                case AD_PAUSED:
//                case AD_PLAYING:
//                case AD_SKIPPED:
//                case AD_VIDEO_FIRST_QUARTILE:
//                case AD_VIDEO_MIDPOINT:
//                case AD_VIDEO_THIRD_QUARTILE:
//                case AD_COMPLETE:
//                    handleVideoEventReflection(event, playheadMillis);
//                    return true;
//
//                case AD_LOADED:
//                case AD_IMPRESSED:
//                case AD_CLICK_THRU:
//                case RECORD_AD_ERROR:
//                    // unimplemented
//                    return null;
//
//                default:
//                    CDAdLog.d("Unexpected video event: " + event.getAvidMethodName());
//                    return false;
//            }
//        } catch (Exception e) {
//                        Utils.logStackTrace(e);
//            CDAdLog.d("Video event " + event.getCDAdEnumName() + " failed. "
//                    + e.getMessage());
//            return false;
//        }
        return true;
    }

    @Override
    @Nullable
    public Boolean endVideoSession() {
//        if (!isEnabled()) {
//            return null;
//        }
//
//        if (mCDAdVideoTracker == null) {
//            CDAdLog.d("CDAd VideoAdTracker unexpectedly null.");
//            return false;
//        }
//
//        // Pre-reflection code:
//        // mCDAdVideoTracker.stopTracking();
//
//        try {
//            new Reflection.MethodBuilder(mCDAdVideoTracker, "stopTracking").execute();
//
//            return true;
//        } catch (Exception e) {
//                        Utils.logStackTrace(e);
//            CDAdLog.d("Unable to execute CDAd end video session: " + e.getMessage());
//            return false;
//        }
        return  true;
    }

    /**
     * Generates the adIds map from the video viewability tracking URL and any additional buyer tag
     * resources.
     *
     * @param urlString Used to gather partnerCode and relevant level/slicer information.
     * Example: https://z.cdAdads.com.chalkdigitalppdisplay698212075271/cdAdad.js#cdAdClientLevel1=appname&cdAdClientLevel2=adunit&cdAdClientLevel3=creativetype&cdAdClientSlicer1=adformat
     *
     * @param buyerResources CDAd buyer-tag impression pixels.
     *
     * Example output adIds map:
     * {
     *     "level1": "appname",
     *     "level2": "adunit",
     *     "level3": "creativetype",
     *     "slicer1": "adformat",
     *     "partnerCode": "cdadsappdisplay698212075271",
     *     "zCDAdVASTIDs": "<ViewableImpression id="${BUYER_AD_SERVER_MACRO[S]}"><![CDATA[https://px.cdAdads.com/pixel.gif?cdAdPartnerCode=${CDAD_PARTNER_CODE}]]</ViewableImpression>"
     * }
     */
    private void updateAdIdsFromUrlStringAndBuyerResources(@Nullable final String urlString,
                                                           @Nullable final Set<String> buyerResources) {
        mAdIds.clear();
        mAdIds.put(PARTNER_CODE_KEY, DEFAULT_PARTNER_CODE);
        mAdIds.put(CDAD_VAST_IDS_KEY, TextUtils.join(";", buyerResources));

        if (TextUtils.isEmpty(urlString)) {
            return;
        }

        final Uri uri = Uri.parse(urlString);

        final List<String> pathSegments = uri.getPathSegments();
        // If a partnerCode is parsed from the viewability tracking URL, prefer to use that.
        // Otherwise fallback to the CDAd default that was already added to the map.
        if (pathSegments.size() > 0 && !TextUtils.isEmpty(pathSegments.get(0))) {
            mAdIds.put(PARTNER_CODE_KEY, pathSegments.get(0));
        }

        final String fragment = uri.getFragment();
        if (!TextUtils.isEmpty(fragment)) {
            for (final String fragmentPairs : fragment.split("&")) {
                final String[] fragmentPair = fragmentPairs.split("=");
                if (fragmentPair.length < 2) {
                    continue;
                }

                final String fragmentKey = fragmentPair[0];
                final String fragmentValue = fragmentPair[1];
                if (TextUtils.isEmpty(fragmentKey) || TextUtils.isEmpty(fragmentValue)) {
                    continue;
                }

                if (QUERY_PARAM_MAPPING.containsKey(fragmentKey)) {
                    mAdIds.put(QUERY_PARAM_MAPPING.get(fragmentKey), fragmentValue);
                }
            }
        }
    }

    private boolean handleVideoEventReflection(@NonNull VideoEvent videoEvent,
                                               final Integer playHeadMillis) throws Exception {
//        if (videoEvent.getCDAdEnumName() == null) {
//            return false;
//        }
//
//        // Pre-reflection code:
//        // CDAdAdEvent event = new CDAdAdEventType(<cdAdEventType>, playhead);
//        // mCDAdVideoTracker.dispatchEvent(event);
//
//        final Class<?> clazz = Class.forName(CDAD_AD_EVENT_TYPE_PATH);
//        final Enum<?> adEventTypeEnum = Enum.valueOf(clazz.asSubclass(Enum.class),
//                videoEvent.getCDAdEnumName());
//
//        final Object cdAdEvent = Reflection.instantiateClassWithConstructor(
//                CDAD_AD_EVENT_PATH, Object.class, new Class[]{clazz, Integer.class},
//                new Object[]{adEventTypeEnum, playheadMillis});
//
//        new Reflection.MethodBuilder(mCDAdVideoTracker, "dispatchEvent")
//                .addParam(CDAD_AD_EVENT_PATH, cdAdEvent)
//                .execute();

        return true;
    }

    @Override
    public void cdAdViewabilityChanged(final View view, final CDAdAnalytics.CDAdVisibleArea cdAdVisibleArea) {
        CDAdLog.d("CDAdAnalytics", cdAdVisibleArea.toString());
        if (mEvents!=null){
            String eventName = "";
            switch (cdAdVisibleArea){
                case CDAdOneQuarterVisible:
                    eventName = "view_25";
                    break;
                case CDAdHalfVisible:
                    eventName = "view_50";
                    break;
                case CDAdThreeQuarterVisible:
                    eventName = "view_75";
                    break;
                case CDAdFullVisible:
                    eventName = "view_100";
                    break;
            }

            if (mEvents!=null && mEvents.size()>0){
                String url = null;
                url = mContext.getResources().getText(mContext.getResources().getIdentifier("SERVER_URL", "string", mContext.getPackageName())).toString();
                if (url == null){
                    CDAdLog.i("Server Url Missing, Stopping Viewablity Event");
                    return;
                }
                for (String event:mEvents.keySet()) {
                    if (event.equals(eventName) && mEvents.get(event)!=null){
                        for (String s:mEvents.get(event)) {
                            CDAdService.performCall(CDAdRetrofit.getsharedSDKRequestAPIInstance(url+"/"), "logEvent", Object.class, new Class[]{String.class}, new Object[]{s}, new CDAdCallback<Object>(null, 0) {
                                @Override
                                public void onNetworkResponse(final Call<Object> call, final Response<Object> response, final Object object, final int apitype) {

                                }

                                @Override
                                public void onNetworkFailure(final Call<Object> call, final Throwable t, final Object object, final int apitype) {

                                }
                            });

                        }
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void cdAdAnalyticsStarted(final View view) {

    }

    @Override
    public void cdAdAnalyticsStopped(final View view) {

    }
}
