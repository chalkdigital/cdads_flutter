package com.chalkdigital.nativeads;

import android.support.annotation.NonNull;

import com.chalkdigital.nativeads.CDAdNativeAdPositioning.CDAdClientPositioning;

/**
 * Allows asynchronously requesting positioning information.
 */
interface PositioningSource {

    interface PositioningListener {
        void onLoad(@NonNull CDAdClientPositioning positioning);

        void onFailed();
    }

    void loadPositions(@NonNull String adUnitId, @NonNull PositioningListener listener);

}
