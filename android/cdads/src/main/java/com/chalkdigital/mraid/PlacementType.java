package com.chalkdigital.mraid;

import java.util.Locale;

public enum PlacementType {
    INLINE,
    INTERSTITIAL;

    String toJavascriptString() {
        return toString().toLowerCase(Locale.US);
    }
}
