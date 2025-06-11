package com.chalkdigital.nativeads;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code CustomEventNative} is a base class for custom events that support native ads. By
 * implementing subclasses of {@code CustomEventNative}, you can enable the CDAd SDK to support a
 * wider variety of third-party ad networks, or execute any of your application code on demand.
 *
 * At runtime, the CDAd SDK will find and instantiate a {@code CustomEventNative} subclass as
 * needed and invoke its {@link #loadNativeAd} method.
 */
public abstract class CustomEventNative {
    /**
     * When the CDAd SDK receives a response indicating it should load a custom event, it will send
     * this message to your custom event class. Your implementation of this method can either load a
     * native ad from a third-party ad network, or execute any application code. It must also notify
     * the provided {@link CustomEventNativeListener} Object of certain lifecycle events.
     *
     * @param context The context.
     * @param customEventNativeListener An Object that must be notified of certain lifecycle
     * events.
     * @param localExtras A Map containing additional custom data that is set within your
     * application by calling {@link CDAdNative#setLocalExtras(Map)}. Note that the
     * localExtras Map is a copy of the Map supplied to {@link CDAdNative#setLocalExtras(Map)}.
     * @param serverExtras A Map containing additional custom data configurable on the CDAd website
     * that you want to associate with a given custom event request. This data may be used to pass
     * dynamic information, such as publisher IDs, without changes in application code.
     */
    protected abstract void loadNativeAd(@NonNull final Context context,
            @NonNull final CustomEventNativeListener customEventNativeListener,
            @NonNull final Map<String, Object> localExtras,
            @NonNull final Map<String, String> serverExtras);

    public interface CustomEventNativeListener {
        /**
         * Your custom event subclass must call this method when it successfully loads a native ad.
         * Failure to do so will disrupt the mediation waterfall and cause future ad requests to
         * stall.
         *
         * @param nativeAd The ad that was successfully loaded.
         */
        void onNativeAdLoaded(BaseNativeAd nativeAd);

        /**
         * Your custom event subclass must call this method when it fails to load a native ad.
         * Failure to do so will disrupt the mediation waterfall and cause future ad requests to
         * stall.
         *
         * @param errorCode An enum value with the relevant error message.
         */
        void onNativeAdFailed(NativeErrorCode errorCode);

        /**
         * Your custom event subclass must call this method when it native video ends.
         * Failure to do so will disrupt the mediation waterfall and cause future ad requests to
         * stall.
         *
         * @param nativeAd The ad that was successfully loaded.
         */
        void onNativeAdVideoEnded(BaseNativeAd nativeAd);

        /**
         * Your custom event subclass must call this method when user clicks native ad.
         * Failure to do so will disrupt the mediation waterfall and cause future ad requests to
         * stall.
         *
         * @param nativeAd The ad that was successfully loaded.
         */
        void onNativeAdClicked(BaseNativeAd nativeAd);
    }
}
