package com.chalkdigital.banner.ads;

import android.content.Context;
import android.view.View;

import com.chalkdigital.ads.CDAdErrorCode;
import com.chalkdigital.common.CDAdSize;
import com.chalkdigital.mediation.CDMediationAdRequest;

import java.util.HashMap;
import java.util.Map;

/*
 * CustomEventBanner is a base class for custom events that support banners. By implementing
 * subclasses of CustomEventBanner, you can enable the CDAd SDK to natively support a wider
 * variety of third-party ad networks, or execute any of your application code on demand.
 * 
 * At runtime, the CDAd SDK will find and instantiate a CustomEventBanner subclass as needed
 * and invoke its loadAd() method.
 */
public abstract class CustomEventBanner {
    
    /*
     * When the CDAd SDK receives a response indicating it should load a custom event, it will send
     * this message to your custom event class. Your implementation of this method can either load
     * a banner ad from a third-party ad network, or execute any application code. It must also
     * notify the provided CustomEventBanner.Listener Object of certain lifecycle events.
     * 
     * The localExtras parameter is a Map containing additional custom data that is set within
     * your application by calling CDAdView.setLocalExtras(Map<String, Object>). Note that the
     * localExtras Map is a copy of the Map supplied to setLocalExtras().
     * 
     * The serverExtras parameter is a Map containing additional custom data configurable on the
     * CDAd website that you want to associate with a given custom event request. This data may be
     * used to pass dynamic information, such as publisher IDs, without changes in application code.
     */
    protected abstract void loadBanner(Context context,
                                       CustomEventBannerListener customEventBannerListener, Map<String, Object> params,
                                       final CDAdSize adSize, final CDMediationAdRequest mediationAdRequest, final Map<String, String> serverExtras);
    
    /*
     * Called when a Custom Event is being invalidated or destroyed. Perform any final cleanup here.
     */
    protected abstract void onInvalidate();

    /*
     * Fire MPX impression trackers and 3rd-party impression trackers from JS.
     */
    protected void trackMpxAndThirdPartyImpressions() {
    }

    protected abstract String getId();

    public interface CustomEventBannerListener {
        /*
         * Your custom event subclass must call this method when it successfully loads an ad and
         * needs to display the provided View. Failure to do so will disrupt the mediation waterfall
         * and cause future ad requests to stall.
         */
        void onBannerLoaded(View bannerView);
        
        /*
         * Your custom event subclass must call this method when it fails to load an ad.
         * Failure to do so will disrupt the mediation waterfall and cause future ad requests to
         * stall.
         */
        void onBannerFailed(CDAdErrorCode errorCode);

        /*
         * This method is for internal use only. You may ignore it.
         */
        void onBannerExpanded();

        /*
         * This method is for internal use only. You may ignore it.
         */
        void onBannerCollapsed();

        /*
         * Your custom event subclass should call this method when a user taps on a banner ad.
         * This method is optional.
         */
        void onBannerClicked();

        /*
         * This is an alias for onBannerClicked().
         * Your custom event subclass should call this method if the ad will cause the user to leave
         * the application (e.g. for the Play Store or browser). This method is optional.
         */
        void onLeaveApplication();

        void onBannerDisplayed(View bannerView);

        void onBannerDismissed();
    }
}
