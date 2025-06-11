package com.chalkdigital.interstitial.ads;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.chalkdigital.ads.CDAdErrorCode;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.nativeads.VideoConfiguration;

import java.util.Map;

public class CDAdVideoInterstitial extends CDAdInterstitial {

    public interface InterstitialVideoAdListener {

        /**
         * This method is called when an ad request is initiated on CDAdVideoInterstitial object.
         * @param videoInterstitial reference of interstitial on which ad request is initiated.
         */
        void onInterstitialVideoAdRequest(CDAdVideoInterstitial videoInterstitial);

        /**
         * This method is called when an ad is loaded for an CDAdVideoInterstitial object.
         * @param videoInterstitial reference of interstitial for which ad is loaded.
         */
        void onInterstitialVideoLoaded(CDAdVideoInterstitial videoInterstitial);

        /**
         * This method is called when an ad request is failed for an CDAdVideoInterstitial object.
         * @param videoInterstitial reference of interstitial for which ad is failed.
         * @param errorCode reason of ad request failure.
         */
        void onInterstitialVideoFailed(CDAdVideoInterstitial videoInterstitial, CDAdErrorCode errorCode);

        /**
         * This method is called when an ad is shown using an CDAdVideoInterstitial object.
         * @param videoInterstitial reference of interstitial on which an ad is shown.
         */
        void onInterstitialVideoShown(CDAdVideoInterstitial videoInterstitial);

        /**
         * This method is called when an ad is clicked.
         * @param videoInterstitial reference of interstitial on which ad is clicked.
         */
        void onInterstitialVideoClicked(CDAdVideoInterstitial videoInterstitial);

        /**
         * This method is called when an ad is dismissed.
         * @param videoInterstitial reference of interstitial on which ad is dismissed.
         */
        void onInterstitialVideoDismissed(CDAdVideoInterstitial videoInterstitial);

        /**
         * This method is called when a video ad is ended.
         * @param videoInterstitial reference of interstitial on which ad is ended.
         */
        void onInterstitialVideoEnded(CDAdVideoInterstitial videoInterstitial);
    }

    @Nullable
    public InterstitialVideoAdListener getInterstitialVideoAdListener() {
        return super.getInterstitialVideoAdListener();
    }

    public void setInterstitialVideoAdListener(@Nullable final InterstitialVideoAdListener interstitialVideoAdListener) {
        super.setInterstitialVideoAdListener(interstitialVideoAdListener);
    }

    @Override
    public void setInterstitialAdListener(@Nullable final InterstitialAdListener listener) {
        CDAdLog.e("This is an interstitial video ad. Please use setInterstitialVideoAdListener instead of setInterstitialAdListener.");
    }

    @Nullable
    @Override
    public InterstitialAdListener getInterstitialAdListener() {
        return null;
    }

    /**
     * Request an interstitial video ad.
     * <p/>
     *
     * @param context activity context.
     * @param videoConfiguration video configuration for requesting video ad. If videoConfiguration is null then request will use default video configuration
     * @param targetingParams targeting parameters passed as key value pair.
     * @param partnerId partner id of publisher.
     * @param placementId placement id of this ad.
     *
     *
     */
    public void requestInterstitialVideo(Context context, VideoConfiguration videoConfiguration, Map<String, String> targetingParams, String partnerId, String placementId){
        internalRequestNewAd(targetingParams, (videoConfiguration==null)?(new VideoConfiguration.Builder().build(mActivity.getApplicationContext())):videoConfiguration, partnerId, placementId);
    }

    /**
     * Creates a new video interstitial to be used as an ad.
     * <p/>
     *
     * @param activity The activity context which would be used to render this interstitial.
     */
    public CDAdVideoInterstitial(@NonNull final Activity activity) {
        super(activity);
        super.isVideoAd = true;
    }
}
