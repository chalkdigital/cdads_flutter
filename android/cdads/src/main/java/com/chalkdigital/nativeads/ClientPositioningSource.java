package com.chalkdigital.nativeads;

import android.os.Handler;
import android.support.annotation.NonNull;

import com.chalkdigital.nativeads.CDAdNativeAdPositioning.CDAdClientPositioning;

/**
 * Returns a preset client positioning object.
 */
class ClientPositioningSource implements PositioningSource {
    @NonNull private final Handler mHandler = new Handler();
    @NonNull private final CDAdClientPositioning mPositioning;

    ClientPositioningSource(@NonNull CDAdClientPositioning positioning) {
        mPositioning = CDAdNativeAdPositioning.clone(positioning);
    }

    @Override
    public void loadPositions(@NonNull final String adUnitId,
            @NonNull final PositioningListener listener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                listener.onLoad(mPositioning);
            }
        });
    }
}
