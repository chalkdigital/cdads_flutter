package com.chalkdigital.network.response;

import com.chalkdigital.common.DataKeys;

import java.util.Map;

public class EventData {
    private Map<String, Object> Android_Interstitial;
    private Map<String, Object> Android_Banner;
    private Map<String, Object> Android_Rewarded;

    public EventData(Map<String, Object> map) {
        if (map!=null && map instanceof Map){
            if (map.keySet().contains(DataKeys.ANDROID_BANNER)) {
                Object bannerParams = map.get(DataKeys.ANDROID_BANNER);
                if (bannerParams!=null && bannerParams instanceof Map)
                    setAndroid_Banner((Map<String, Object>) bannerParams);
            }
            if (map.keySet().contains(DataKeys.ANDROID_INTERSTITIAL)) {
                Object interstitialParams = map.get(DataKeys.ANDROID_INTERSTITIAL);
                if (interstitialParams!=null && interstitialParams instanceof Map)
                    setAndroid_Interstitial((Map<String, Object>) interstitialParams);
            }
            if (map.keySet().contains(DataKeys.ANDROID_REWARDED)) {
                Object rewardedParams = map.get(DataKeys.ANDROID_REWARDED);
                if (rewardedParams!=null && rewardedParams instanceof Map)
                    setAndroid_Rewarded((Map<String, Object>) rewardedParams);
            }
        }
    }

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
