package com.chalkdigital.model;

import java.util.Map;

public class EventData {
    private Map<String, Object> Android_Interstitial;
    private Map<String, Object> Android_Banner;
    private Map<String, Object> Android_Rewarded;

    public Map<String, Object> getAndroid_Interstitial() {
        return Android_Interstitial;
    }

    public void setAndroid_Interstitial(Map<String, Object> android_Interstitial) {
        Android_Interstitial = android_Interstitial;
    }

    public Map<String, Object> getAndroid_Banner() {
        return Android_Banner;
    }

    public void setAndroid_Banner(Map<String, Object> android_Banner) {
        Android_Banner = android_Banner;
    }

    public Map<String, Object> getAndroid_Rewarded() {
        return Android_Rewarded;
    }

    public void setAndroid_Rewarded(Map<String, Object> android_Rewarded) {
        Android_Rewarded = android_Rewarded;
    }
}
