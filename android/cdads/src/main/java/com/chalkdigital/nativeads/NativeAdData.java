package com.chalkdigital.nativeads;

import android.support.annotation.NonNull;

/**
 * An object that represents placed ads in a {@link CDAdStreamAdPlacer}
 */
class NativeAdData {
    @NonNull private final String adUnitId;
    @NonNull private final CDAdAdRenderer adRenderer;
    @NonNull private final NativeAd adResponse;

    NativeAdData(@NonNull final String adUnitId,
            @NonNull final CDAdAdRenderer adRenderer,
            @NonNull final NativeAd adResponse) {
        this.adUnitId = adUnitId;
        this.adRenderer = adRenderer;
        this.adResponse = adResponse;
    }

    @NonNull
    String getAdUnitId() {
        return adUnitId;
    }

    @NonNull
    CDAdAdRenderer getAdRenderer() {
        return adRenderer;
    }

    @NonNull
    NativeAd getAd() {
        return adResponse;
    }
}
