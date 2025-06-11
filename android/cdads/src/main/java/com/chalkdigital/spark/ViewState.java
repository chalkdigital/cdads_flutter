package com.chalkdigital.spark;

import java.util.Locale;

public enum ViewState {
    LOADING,
    DEFAULT,
    PLAYING,
    PAUSED,
    LOADED,
    COMPLETED,
    ALLCOMPLETED,
    CLOSED,
    RESIZED,
    EXPANDED,
    HIDDEN;

    public String toJavascriptString() {
        return toString().toLowerCase(Locale.US);
    }
}
