package com.chalkdigital.nativeads;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.chalkdigital.ads.CDAdErrorCode;
import com.chalkdigital.ads.WebAdRequestBodyGenerator;
import com.chalkdigital.common.CDAdActions;
import com.chalkdigital.common.CDAdConnectivityChangeReceiver;
import com.chalkdigital.common.CDAdConstants;
import com.chalkdigital.common.CDAdIPGeolocationManager;
import com.chalkdigital.common.CDAdLocationManager;
import com.chalkdigital.common.CDAdRequest;
import com.chalkdigital.common.CDAdSize;
import com.chalkdigital.common.CDAdsUtils;
import com.chalkdigital.common.DataKeys;
import com.chalkdigital.common.Preconditions;
import com.chalkdigital.common.VisibleForTesting;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.DeviceUtils;
import com.chalkdigital.common.util.ManifestUtils;
import com.chalkdigital.common.util.Utils;
import com.chalkdigital.network.CDAdNetworkError;
import com.chalkdigital.network.AdRequest;
import com.chalkdigital.network.response.AdResponse;
import com.chalkdigital.network.response.NetworkResponse;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import retrofit2.Response;

import static com.chalkdigital.common.GpsHelper.fetchAdvertisingInfoAsync;
import static com.chalkdigital.nativeads.CustomEventNative.CustomEventNativeListener;
import static com.chalkdigital.nativeads.NativeErrorCode.CONNECTION_ERROR;
import static com.chalkdigital.nativeads.NativeErrorCode.EMPTY_AD_RESPONSE;
import static com.chalkdigital.nativeads.NativeErrorCode.INVALID_REQUEST;
import static com.chalkdigital.nativeads.NativeErrorCode.NATIVE_RENDERER_CONFIGURATION_ERROR;
import static com.chalkdigital.nativeads.NativeErrorCode.SERVER_ERROR_RESPONSE_CODE;
import static com.chalkdigital.nativeads.NativeErrorCode.UNSPECIFIED;

public class CDAdNative {

    private static final String TAG = "CDAdNative";
    private boolean isWaitingForLocationUpdate;
    private boolean isWaitingForIP;
    private boolean shouldSkipIp;
    private boolean shouldSkipLocation;
    private boolean mAtLeastOneAdLoaded;
    private long mLastAdRequestTime;
    private CDAdBroadcastReceiver mCDAdBroadcastReceiver;
    private boolean mAdWasLoaded;
    private Handler mHandler;
    private Runnable mRefreshRunnable;
    private Handler mLocationHandler;
    private Runnable mLocationRunnable;
    static final int MAX_REFRESH_TIME = 60; // 10 minutes
    static final int MIN_REFRESH_TIME = 10; // 10 minutes

    private CDAdSize.CDAdSizeConstant mCDAdSizeConstant;


    /**
     * Native Ad Listener. This listener is used to get native ad load and fail callbacks.
     */
    public interface CDAdNativeAdListener {

        void onNativeLoad(final NativeAd nativeAd);
        void onNativeVideoEnded(final BaseNativeAd nativeAd);
        void onNativeFail(final NativeErrorCode errorCode);
        void onNativeClicked(final BaseNativeAd nativeAd);
    }

    static final CDAdNativeAdListener EMPTY_NETWORK_LISTENER =
            new CDAdNativeAdListener() {
        @Override
        public void onNativeLoad(@NonNull final NativeAd nativeAd) {
            // If this listener is invoked, it means that CDAdNative instance has been destroyed
            // so destroy any leftover incoming NativeAds
            nativeAd.destroy();
        }
        @Override
        public void onNativeFail(final NativeErrorCode errorCode) {
        }

                @Override
                public void onNativeVideoEnded(final BaseNativeAd nativeAd) {

                }

                @Override
                public void onNativeClicked(final BaseNativeAd errorCode) {

                }
            };

    // Highly recommended to be an Activity since 3rd party networks need it
    @NonNull private final WeakReference<Context> mContext;
    @NonNull private final String mAdUnitId;
    @NonNull private CDAdNativeAdListener mCDAdNativeAdListener;

    // For small sets TreeMap, takes up less memory than HashMap
    @NonNull private Map<String, Object> mLocalExtras = new TreeMap<String, Object>();
    @NonNull private final AdRequest.Listener mAdListener;
    @Nullable private AdRequest mNativeRequest;
    @NonNull AdRendererRegistry mAdRendererRegistry;
    @NonNull private CDAdRequest mCDAdRequest;

    public CDAdNative(@NonNull final Context context,
                       @NonNull final CDAdNativeAdListener cdAdNativeAdListener) {
        this(context, null, new AdRendererRegistry(), cdAdNativeAdListener);
    }

    @VisibleForTesting
    public CDAdNative(@NonNull final Context context,
                       @NonNull final String adUnitId,
                       @NonNull AdRendererRegistry adRendererRegistry,
                       @NonNull final CDAdNativeAdListener cdAdNativeAdListener) {
        mHandler = new Handler();

        Preconditions.checkNotNull(context, "context may not be null.");
//        Preconditions.checkNotNull(adUnitId, "AdUnitId may not be null.");
        Preconditions.checkNotNull(adRendererRegistry, "AdRendererRegistry may not be null.");
        Preconditions.checkNotNull(cdAdNativeAdListener, "CDAdNativeAdListener may not be null.");

        mCDAdSizeConstant = CDAdSize.CDAdSizeConstant.CDAdSize300X250;

        ManifestUtils.checkNativeActivitiesDeclared(context);

        mContext = new WeakReference<Context>(context);
        mAdUnitId = adUnitId;
        mCDAdNativeAdListener = cdAdNativeAdListener;
        mAdRendererRegistry = adRendererRegistry;
        setLastAdRequestTime();
        mAdListener = new AdRequest.Listener() {

            @Override
            public void onAdRequestSuccess(Response response, Object object, int apitype) {
                try {
                    NetworkResponse networkResponse = (NetworkResponse) response.body();
                    networkResponse.parseResponse();
                    AdResponse adResponse = mNativeRequest.parseNetworkResponse(networkResponse);
                    setLastAdRequestTime();
                    onAdLoadSuccess(adResponse);
                } catch (Throwable t) {
                        Utils.logStackTrace(t);
                    CDAdLog.d(t.toString());
                    setLastAdRequestTime();
                    onAdLoadError(new CDAdNetworkError(response, t, CDAdNetworkError.Reason.BAD_BODY));
                }
            }

            @Override
            public void onAdRequestFailure(CDAdNetworkError error, Object object, int apitype) {
                onAdLoadError(error);
//                AdResponse adResponse = mActiveRequest.parseNetworkResponse(new Gson().fromJson("{\"cur\":\"USD\",\"id\":\"299453af-29bc-44a6-807d-8b586fcc7f82\",\"seatbid\":[{\"seat\":\"2\",\"bid\":[{\"crid\":\"3913\",\"h\":250,\"adm\":\"<script src=\\\"http:\\/\\/bs.serving-sys.com\\/BurstingPipe\\/adServer.bs?cn=rsb&c=28&pli=23634867&PluID=0&w=300&h=250&ord=1519892359421&ucm=true&mb=1\\\"><\\/script><noscript><a href=\\\"http:\\/\\/ec2-54-166-238-105.compute-1.amazonaws.com\\/xp\\/evt?pp=zkGviXgrlGrkYXYbRIgod5R8wy4JcEU4KE8tzSwabJR4rJWPI7cKy4GJss3exPYTRtErw5o2rm8iI35eFQgMnrqMWxCpF2kkadiMrblMjtnzfTtNJC8eNVrPXGneeh5iaRNn1My8aHqCiibjBboRRk5ATf4MkELpKN8cDxIEvDTNT9U3P8w2PI15TrNnwPHwxJHF60G79Fm2YN9Qznj7ZDZs7n1QGdIZMvw3wLPOa4iY2mjatHmtDeae7wbjmh0Ip6JrBvQJFUwqdUNp9r7TkPkPFrVvWQrVgNVTHQdYDpN1LmDcG0crgwPhypB6GQpEsbjaj5Hzwdfv1eHvQzHrZVw3nPR6vXhrohrxTQh7ouuT6E9kge98G6PXh4wrZYaWSGxtpNidOLwR6AujLZX3QSpdTdBWJAzxZaV6lRYHDlvd4S5lwW7ZcmVX9Fufmuvh4qi8iZSXzy2EONfagbORn0n3C6v3yNYTddhtLBJvPj3V4z4KELSWiBxU0OH3Z47lmaWII4OqnWG2uGVvLptbXMq06QrZvT4LLgUWu7RlY0VVfPlVBgYxrcdSmoNNhOrmx5O8ROoqit7f820Jg4z2nBVdewYC59Lkitbm0YFtzZZCqQz8ZUEAyrnuIGfJwm6xMIqoQirh4YEekd8tANHdB41NS4OFM4222GPynGHy0gM7xazEGTrdMLh0YE7BwxCYctbvONEZhjNFv2HPPFEQQsnajVrWFw73Fly6k0ovQFD5AlaLrGxAxt7lUWIrofEhussVV4v3ot1jKghPxTLH0Xraz5Ljjc13JU3UqKupOEzPYqUwBFgebkQzdZZGSOrIuMa1J__rd=HS5qP2fQCbbrUcdl9ZciP8CFpXX2U1pTd8VdpjnX9qcpKBWUESvNCCsd7vERQIyoJPxNhLhCu0XkVePhJzcusVPMXiocuHvtitLUEeJwNGpgQ42WiphSEeBJiw9mK1VM5E2lpFwG4LaTOQS4fwHkKiC7RMczxmllhqHtMRAWCvxMg3ZthcOfMWimM2bWLKlr__\\\" target=\\\"_blank\\\"><img src=\\\"http:\\/\\/bs.serving-sys.com\\/BurstingPipe\\/adServer.bs?cn=bsr&FlightID=23634867&Page=&PluID=0&Pos=1929237889&mb=1\\\" border=0 width=300 height=250><\\/a><\\/noscript><img src=\\\"http:\\/\\/ec2-54-166-238-105.compute-1.amazonaws.com\\/xp\\/evt?pp=4rirIQILvKyFGxfokEfAmHqvlnSMq0oKRPIcQtvpfTnTyADNQSDdypFqxGLbh6sDJUfYlyE41oVChqeBsURC45x5NI4aJVqtqjP4xWRgO8tVc7W0TBEf6MLp5sNxrTyspplI7g75aqhk4EaCNRsMbCCtq2cI53lNZ9veNEeSQFpiYyQpCx8yyz7vHz0T4QNlgZG2M7Gsp93hcRl38DPDxDGJshTyQ9AN4844pRKyIaslnZBTstuiWdNUTNqU6a0l0kORfYt5NwTNFKyN6SqDVeVehdznf2elxA1qSuVWOU3tlbjceYIiS3jMzDx35VamsdCRqlaHyNkjwoufGqZ6etucopB4bjskLhkBH3zqdfaSWeNsZeNzr3FMAk5gisitUaUFDNdjZ5j4YbBHgpI2HN033YudHnMplFHjDENDVTs0INaBWY1wxCQDBXgAPufUPFtr7uIs16S1nlgyHl8piqXrDaTb1ptcsnkRIX1VoUM8OWseAc6hOjg2lXsQulZ32bIMbrkO9CxP18vrzaMTObCheAtTBjfLqT10E8nqeAVcPzieABFffrqKJ38t3eVT7Z7TGbjHQrCnozrh4HQbY0cHZOu8mi5SQOdhqKsI92ag7Rz7zUazX7PWKWQgIcYhivag97FR7fQIYFNy0h1nKNuRFJVdMB9a2BvIa7qrmo8WVFVTZswJIWVyx9NSlUpMTkwlEAApjYsNzQAelHgByJFglV9FM8s2MFzNTwSkoTt9P27zLWndCKdjlJ3jnr3J8xCRAN6DVAsDPF6jOf2uNP2fYo3wNkAdTcfMxxYq1xkksuWCvTVjLBJdF7axeumX4FF2KK8YnEw5oMW5VjJ7uaO7X6G__\\\" width=\\\"1\\\" height=\\\"1\\\" \\/>\",\"adid\":\"1222_3913\",\"adomain\":[\"redfin.com\"],\"price\":0.1,\"w\":300,\"iurl\":\"https:\\/\\/daf37cpxaja7f.cloudfront.net\\/c1222\\/creative_url_15181802673800_aaa.png\",\"cat\":[\"IAB10\"],\"id\":\"cefd1a0b-3413-4c9d-8a6f-9ddebefae62a_1\",\"attr\":[],\"impid\":\"1\",\"cid\":\"1222\"}]}],\"bidid\":\"cefd1a0b-3413-4c9d-8a6f-9ddebefae62a\"}", NetworkResponse.class));
//                onAdLoadSuccess(adResponse);
                onAdLoadError(error);
            }
        };

        // warm up cache for google play services info
        fetchAdvertisingInfoAsync(context, null);
    }

    /**
     * Registers an ad renderer for rendering a specific native ad format.
     * Note that if multiple ad renderers support a specific native ad format, the first
     * one registered will be used.
     */
    public void registerAdRenderer(CDAdAdRenderer CDAdAdRenderer) {
        mAdRendererRegistry.registerAdRenderer(CDAdAdRenderer);
    }

    public void destroy() {
        mContext.clear();

        if (mNativeRequest != null) {
            mNativeRequest.cancel();
            mNativeRequest = null;
        }
        mCDAdNativeAdListener = EMPTY_NETWORK_LISTENER;
    }

    public void setLocalExtras(@Nullable final Map<String, Object> localExtras) {
        if (localExtras == null) {
            mLocalExtras = new TreeMap<String, Object>();
        } else {
            mLocalExtras = new TreeMap<String, Object>(localExtras);
        }
    }

    public void makeRequest() {
        makeRequest((CDAdRequest)null);
    }

    public void makeRequest(@Nullable CDAdRequest cdAdRequest) {
        try {
            if (!CDAdsUtils.initialised)
            {
                CDAdLog.e(CDAdErrorCode.CDADS_NOT_INITIALIZED.toString());
                if (mCDAdNativeAdListener!=null)
                    mCDAdNativeAdListener.onNativeFail(NativeErrorCode.CDADS_NOT_INITIALIZED);
                return;
            }
            if (cdAdRequest==null)
                cdAdRequest = (new CDAdRequest.Builder()).build(mContext.get());
            if (cdAdRequest.videoConfiguration == null)
                cdAdRequest.videoConfiguration = (new VideoConfiguration.Builder()).build(mContext.get());
            makeRequest(cdAdRequest, null);
        } catch (Throwable throwable) {
            Utils.logStackTrace(throwable);
        }
    }

    public void makeRequest(@Nullable final CDAdRequest cdAdRequest,
            @Nullable Integer sequenceNumber) {
        mCDAdRequest = cdAdRequest;
        mRefreshRunnable = new Runnable() {
            public void run() {
                requestNativeAd(cdAdRequest);
            }
        };
        final Context context = getContextOrDestroy();
        if (context == null) {
            return;
        }


        loadNativeAd(cdAdRequest, sequenceNumber);
    }

    private void loadNativeAd(
            @Nullable final CDAdRequest cdAdRequest,
            @Nullable final Integer sequenceNumber) {
        mLastAdRequestTime = 0;
        final Context context = getContextOrDestroy();
        if (context == null) {
            return;
        }

        if (mCDAdBroadcastReceiver == null)
            mCDAdBroadcastReceiver = new CDAdBroadcastReceiver();

        if (!mCDAdBroadcastReceiver.isBroadcastReceiverRegistered){
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(CDAdActions.LOCATION_CHANGED);
            intentFilter.addAction(CDAdActions.LOCATION_ERROR);
            intentFilter.addAction(CDAdActions.IP_RECEIVED);
            intentFilter.addAction(CDAdActions.IP_ERROR);
            intentFilter.addAction(CDAdActions.CDAdNotifyNetworkReachabilityChanged);
            if (mContext.get()!=null)
                LocalBroadcastManager.getInstance(mContext.get()).registerReceiver(mCDAdBroadcastReceiver, intentFilter);
            mCDAdBroadcastReceiver.isBroadcastReceiverRegistered = true;
        }

        mAdWasLoaded = true;
        if (mCDAdRequest == null || mNativeRequest!=null)
            return;

        if (System.currentTimeMillis() - mLastAdRequestTime <30000) {
            return;
        }

        if (!DeviceUtils.isNetworkAvailable(mContext.get())) {
            CDAdLog.d("Can't load an ad because there is no network connectivity. Waiting for network conection");
            mCDAdNativeAdListener.onNativeFail(CONNECTION_ERROR);
            return;
        }

        if (cdAdRequest.locationAutoUpdateEnabled && CDAdLocationManager.shouldWaitToProceedForAdRequest(mContext.get()) && !shouldSkipLocation){
            CDAdLog.d("Can't load an ad because location is not available. Waiting for location");
            initializeLocationHandler(context);
            isWaitingForLocationUpdate = true;
            return;
        }


        if (CDAdsUtils.isIsGeoIpLocationEnabled() && CDAdIPGeolocationManager.shouldWaitForIP(mContext.get()) && !shouldSkipIp){
            isWaitingForIP = true;
            return;
        }
        shouldSkipIp = false;
        shouldSkipLocation = false;

        if (mCDAdBroadcastReceiver!=null && mCDAdBroadcastReceiver.isBroadcastReceiverRegistered && mContext.get()!=null){
            LocalBroadcastManager.getInstance(mContext.get()).unregisterReceiver(mCDAdBroadcastReceiver);
            mCDAdBroadcastReceiver.isBroadcastReceiverRegistered = false;
        }

        requestNativeAd(cdAdRequest);
    }

    void requestNativeAd(@Nullable final CDAdRequest cdAdRequest) {
        final Context context = getContextOrDestroy();
        if (context == null) {
            return;
        }

        if (cdAdRequest == null) {
            mCDAdNativeAdListener.onNativeFail(INVALID_REQUEST);
            return;
        }
        mNativeRequest = new AdRequest(AdResponse.CDAdType.CDAdTypeVideo, new WebAdRequestBodyGenerator(mContext.get(), false).bodyWithParams(getParams(cdAdRequest), false, mContext.get()), mContext.get(), mAdListener, null, 0, "adRequest", NetworkResponse.class);
        mNativeRequest.execute();
    }

    protected Map<String, Object> getParams(@NonNull CDAdRequest cdAdRequest){
        HashMap<String, Object> map = cdAdRequest.getParams();
        CDAdSize size = CDAdSize.getSizeFromCDSizeConstant(mCDAdSizeConstant);
        map.put(DataKeys.HEIGHT, size.getHeight());
        map.put(DataKeys.WIDTH, size.getWidth());
        map.put(DataKeys.VIDEO_CONF, cdAdRequest.videoConfiguration);
        map.put(DataKeys.AD_TYPE, AdResponse.CDAdType.getValue(AdResponse.CDAdType.CDAdTypeVideo));
        return map;
    }

    private void onAdLoadSuccess(@NonNull final AdResponse response) {
        final Context context = getContextOrDestroy();
        if (context == null) {
            return;
        }
        final CustomEventNativeListener customEventNativeListener =
                new CustomEventNativeListener() {
                    @Override
                    public void onNativeAdLoaded(@NonNull final BaseNativeAd nativeAd) {
                        final Context context = getContextOrDestroy();
                        if (context == null) {
                            return;
                        }

                        CDAdAdRenderer renderer = mAdRendererRegistry.getRendererForAd(nativeAd);
                        if (renderer == null) {
                            onNativeAdFailed(NATIVE_RENDERER_CONFIGURATION_ERROR);
                            return;
                        }

                        mCDAdNativeAdListener.onNativeLoad(new NativeAd(context,
                                        response.getImpressionTrackingUrl(),
                                        response.getClickTrackingUrl(),
                                        mAdUnitId,
                                        nativeAd,
                                        renderer)

                        );
                        setLastAdRequestTime();
                    }

                    @Override
                    public void onNativeAdFailed(final NativeErrorCode errorCode) {
                        CDAdLog.v(String.format("Native Ad failed to load with error: %s.", errorCode));
                        loadNativeAd(mCDAdRequest,0);
                        setLastAdRequestTime();
                    }

                    @Override
                    public void onNativeAdVideoEnded(@NonNull final BaseNativeAd nativeAd) {
                        mCDAdNativeAdListener.onNativeVideoEnded(nativeAd);
                    }

                    @Override
                    public void onNativeAdClicked(final BaseNativeAd nativeAd) {
                        mCDAdNativeAdListener.onNativeClicked(nativeAd);
                    }
                };

        CustomEventNativeAdapter.loadNativeAd(
                context,
                mLocalExtras,
                response,
                customEventNativeListener
        );
    }

    void onAdLoadError(final CDAdNetworkError cdAdNetworkError) {
        NativeErrorCode nativeErrorCode = getErrorCodeFromNetworkError(cdAdNetworkError, mContext.get());
        onAdLoadError(nativeErrorCode);
    }

    void onAdLoadError(final NativeErrorCode nativeErrorCode) {
        mCDAdNativeAdListener.onNativeFail(nativeErrorCode);
    }



    static NativeErrorCode getErrorCodeFromNetworkError(@NonNull final CDAdNetworkError error,
                                                       @Nullable final Context context) {
        final Response networkResponse = error.networkResponse;

        // For CDAdNetworkErrors, networkResponse is null.
        if (networkResponse!=null) {
            switch (error.getReason()) {
                case WARMING_UP:
                    return EMPTY_AD_RESPONSE;
                case NO_FILL:
                    return EMPTY_AD_RESPONSE;
                default:
                    return UNSPECIFIED;
            }
        }

        if (networkResponse == null) {
            if (!DeviceUtils.isNetworkAvailable(context)) {
                return CONNECTION_ERROR;
            }
            return UNSPECIFIED;
        }

        if (error.networkResponse.code() >= 400) {
            return SERVER_ERROR_RESPONSE_CODE;
        }

        return UNSPECIFIED;
    }

    @VisibleForTesting
    @Nullable
    Context getContextOrDestroy() {
        final Context context = mContext.get();
        if (context == null) {
            destroy();
            CDAdLog.d("Weak reference to Context in CDAdNative became null. This instance" +
                    " of CDAdNative is destroyed and No more requests will be processed.");
        }
        return context;
    }

    @VisibleForTesting
    @Deprecated
    @NonNull
    CDAdNativeAdListener getCDAdNativeAdListener() {
        return mCDAdNativeAdListener;
    }

    public CDAdSize.CDAdSizeConstant getCDAdSizeConstant() {
        return mCDAdSizeConstant;
    }

    public void setCDAdSizeConstant(final CDAdSize.CDAdSizeConstant CDAdSizeConstant) {
        mCDAdSizeConstant = CDAdSizeConstant;
    }

    private class CDAdBroadcastReceiver extends BroadcastReceiver {
        boolean isBroadcastReceiverRegistered;
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case CDAdActions.LOCATION_CHANGED:
                    if (isWaitingForLocationUpdate){
                        synchronized (CDAdNative.this){
                            isWaitingForLocationUpdate = false;
                            clearLocationHandler();
                            reloadAdIfStillWaiting();
                        }
                    }
                    break;
                case CDAdActions.LOCATION_ERROR:
                    synchronized (CDAdNative.this){
                        isWaitingForLocationUpdate = false;
                        shouldSkipLocation = true;
                        clearLocationHandler();
                        reloadAdIfStillWaiting();
                    }
                    break;
                case CDAdActions.CDAdNotifyNetworkReachabilityChanged:
                    if (intent.getBooleanExtra(CDAdConnectivityChangeReceiver.IS_NETWORK_CONNECTED, false)) {
                        synchronized (CDAdNative.this){
                            reloadAdIfStillWaiting();
                        }
                    }
//                    else if(mShouldAllowAutoRefresh){
//                        CDAdLog.i("CDAdView", "Ad Request stopped, Network not reachable");
//                        cancelRefreshTimer();
//                    }
                    break;
                case CDAdActions.IP_RECEIVED:
                    synchronized (CDAdNative.this){
                        isWaitingForIP = false;
                        reloadAdIfStillWaiting();
                    }
                    break;
                case CDAdActions.IP_ERROR:
                    synchronized (CDAdNative.this){
                        isWaitingForIP = false;
                        shouldSkipIp = true;
                        reloadAdIfStillWaiting();
                    }
                    break;

            }
        }
    }

    private void reloadAdIfStillWaiting(){
        if (mAdWasLoaded){
            if (!mAtLeastOneAdLoaded)
                scheduleRefreshTimerIfEnabled();
        }
    }

    void scheduleRefreshTimerIfEnabled() {
        cancelRefreshTimer();
        if (!mAtLeastOneAdLoaded) {
            int mRefreshTime = 30;
            int nextAdInterval = (int) ((System.currentTimeMillis() - mLastAdRequestTime)/1000);
            if (nextAdInterval > mRefreshTime )
                nextAdInterval = 0;
            else if (nextAdInterval < mRefreshTime){
                nextAdInterval = mRefreshTime - nextAdInterval;
            }
            mHandler.postDelayed(mRefreshRunnable,
                    Math.min(MAX_REFRESH_TIME,
                            nextAdInterval)*1000);
        }
    }

    private void setLastAdRequestTime(){
        mLastAdRequestTime = System.currentTimeMillis();
    }

    private void cancelRefreshTimer() {
        mHandler.removeCallbacks(mRefreshRunnable);
    }


    private synchronized void initializeLocationHandler(final Context context){
        CDAdLog.d(TAG, "initializeLocationHandler");
        if (mLocationHandler == null){
            CDAdLog.d(TAG, "initializedLocationHandler");
            mLocationHandler = new Handler();
            mLocationRunnable = new Runnable() {
                @Override
                public void run() {
                    CDAdLog.d(TAG, "locationHandlerTimeout");
                    onLocationServiceTimeout(context);
//                stopUpdatingLocation(true);
                }
            };
        }
        mLocationHandler.postDelayed(mLocationRunnable, CDAdConstants.CDAdLocationServiceTimeoutInterval*1000);
    }

    private synchronized void clearLocationHandler(){
        CDAdLog.d(TAG, "clearLocationHandler");
        if (mLocationHandler !=null){
            CDAdLog.d(TAG, "clearedLocationHandler");
            if (mLocationRunnable !=null) {
                mLocationHandler.removeCallbacks(mLocationRunnable);
                mLocationRunnable = null;
            }
            mLocationHandler = null;
        }
    }

    private void onLocationServiceTimeout(Context context){
        isWaitingForLocationUpdate = false;
        shouldSkipLocation = true;
        reloadAdIfStillWaiting();
    }
}
