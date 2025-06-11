package com.chalkdigital.spark;

import android.content.pm.ActivityInfo;

enum SparkOrientation {

    PORTRAIT(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
    LANDSCAPE(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE),
    NONE(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

    private final int mActivityInfoOrientation;

    SparkOrientation(final int activityInfoOrientation) {
        mActivityInfoOrientation = activityInfoOrientation;
    }

    int getActivityInfoOrientation() {
        return mActivityInfoOrientation;
    }
}
