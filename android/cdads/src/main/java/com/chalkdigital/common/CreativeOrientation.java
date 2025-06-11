package com.chalkdigital.common;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Represents the orientation returned for CDAd ads from the CDAd ad server.
 */
public enum CreativeOrientation {
    PORTRAIT, LANDSCAPE, DEVICE, VIEW, UNDEFINED;

    @NonNull
    public static CreativeOrientation fromString(@Nullable String orientation) {
        if ("l".equalsIgnoreCase(orientation)) {
            return LANDSCAPE;
        }

        if ("p".equalsIgnoreCase(orientation)) {
            return PORTRAIT;
        }

        if ("d".equalsIgnoreCase(orientation)) {
            return DEVICE;
        }



        return UNDEFINED;
    }
}
