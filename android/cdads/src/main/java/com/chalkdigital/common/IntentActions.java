package com.chalkdigital.common;

/**
 * IntentActions are used by a {@link com.chalkdigital.ads.BaseBroadcastReceiver}
 * to relay information about the current state of a custom event activity.
 */
public class IntentActions {
    public static final String ACTION_INTERSTITIAL_FAIL = "com.chalkdigital.action.interstitial.fail";
    public static final String ACTION_INTERSTITIAL_SHOW = "com.chalkdigital.action.interstitial.show";
    public static final String ACTION_INTERSTITIAL_DISMISS = "com.chalkdigital.action.interstitial.dismiss";
    public static final String ACTION_INTERSTITIAL_CLICK = "com.chalkdigital.action.interstitial.click";
    public static final String ACTION_INTERSTITIAL_VIDEO_COMPLETE = "com.chalkdigital.action.interstitial.video.complete";

    public static final String ACTION_REWARDED_VIDEO_COMPLETE = "com.chalkdigital.action.rewardedvideo.complete";
    public static final String ACTION_REWARDED_PLAYABLE_COMPLETE = "com.chalkdigital.action.rewardedplayable.complete";
    private IntentActions() {}
}
