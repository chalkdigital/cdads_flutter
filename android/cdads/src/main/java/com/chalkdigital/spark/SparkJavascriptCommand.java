package com.chalkdigital.spark;

import android.support.annotation.NonNull;

public enum SparkJavascriptCommand {

    CLOSE("close"),
    STARTED("started"),
    COMPLETED("completed"),
    ALLADSCOMPLETED("allAdsCompleted"),
    LOADED("loaded"),
    PAUSED("paused"),
    RESUMED("resumed"),
    ERROR("error"),
    CLICK("click"),
    IMPRESSION("impression"),
    FIRSTQUARTILE("firstQuartile"),
    MIDPOINT("midPoint"),
    THIRDQUARTILE("thirdQuartile"),
    VOLUMECHANGED("volumeChanged"),
    SKIPPED("skipped"),
    MUTE("mute"),
    UNSPECIFIED("");

    @NonNull private final String mJavascriptString;

    SparkJavascriptCommand(@NonNull String javascriptString) {
        mJavascriptString = javascriptString;
    }

    static SparkJavascriptCommand fromJavascriptString(@NonNull String string) {
        for (SparkJavascriptCommand command : SparkJavascriptCommand.values()) {
            if (command.mJavascriptString.equals(string)) {
                return command;
            }
        }

        return UNSPECIFIED;
    }

    String toJavascriptString() {
        return mJavascriptString;
    }

    boolean requiresClick(@NonNull PlacementType placementType) {
        return false;
    }
}
