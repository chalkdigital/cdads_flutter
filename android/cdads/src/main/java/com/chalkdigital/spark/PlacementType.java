package com.chalkdigital.spark;

import java.util.Locale;

public enum PlacementType {
    INLINE,
    INTERSTITIAL;

    String toJavascriptString() {
        return toString().toLowerCase(Locale.US);
    }
}
