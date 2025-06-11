package com.chalkdigital.ads;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.chalkdigital.common.AdFormat;
import com.chalkdigital.common.AdType;
import com.chalkdigital.common.util.ResponseHeader;

import java.util.Map;

import static com.chalkdigital.network.HeaderUtils.extractHeader;

public class AdTypeTranslator {
    public enum CustomEventType {
        // "Special" custom events that we let people choose in the UI.
        GOOGLE_PLAY_SERVICES_BANNER("admob_native_banner",
                "com.chalkdigital.ads.GooglePlayServicesBanner", false),
        GOOGLE_PLAY_SERVICES_INTERSTITIAL("admob_full_interstitial",
                "com.chalkdigital.ads.GooglePlayServicesInterstitial", false),
        MILLENNIAL_BANNER("millennial_native_banner",
                "com.chalkdigital.ads.MillennialBanner", false),
        MILLENNIAL_INTERSTITIAL("millennial_full_interstitial",
                "com.chalkdigital.ads.MillennialInterstitial", false),

        // CDAd-specific custom events.
        MRAID_BANNER("mraid_banner",
                "com.chalkdigital.banner.mraid.MraidBanner", true),
        MRAID_INTERSTITIAL("mraid_interstitial",
                "com.chalkdigital.interstitial.mraid.MraidInterstitial", true),
        HTML_BANNER("html_banner",
                "com.chalkdigital.banner.ads.HtmlBanner", true),
        HTML_INTERSTITIAL("html_interstitial",
                "com.chalkdigital.interstitial.ads.HtmlInterstitial", true),
        VAST_VIDEO_INTERSTITIAL("vast_interstitial",
                "com.chalkdigital.ads.VastVideoInterstitial", true),
        CDADS_NATIVE("chalkdigital_native",
                "com.chalkdigital.nativeads.CDAdCustomEventNative", true),
        CDADS_VIDEO_NATIVE("chalkdigital_video_native",
                "com.chalkdigital.nativeads.CDAdCustomEventVideoNative", true),
        CDADS_REWARDED_VIDEO("rewarded_video",
                "com.chalkdigital.ads.CDAdRewardedVideo", true),
        CDADS_REWARDED_PLAYABLE("rewarded_playable",
                "com.chalkdigital.ads.CDAdRewardedPlayable", true),

        UNSPECIFIED("", null, false);

        @NonNull
        private final String mKey;
        @Nullable
        private final String mClassName;
        private final boolean mIsCDAdSpecific;

        private CustomEventType(String key, String className, boolean isCDAdSpecific) {
            mKey = key;
            mClassName = className;
            mIsCDAdSpecific = isCDAdSpecific;
        }

        private static CustomEventType fromString(@Nullable final String key) {
            for (CustomEventType customEventType : values()) {
                if (customEventType.mKey.equals(key)) {
                    return customEventType;
                }
            }

            return UNSPECIFIED;
        }

        private static CustomEventType fromClassName(@Nullable final String className) {
            for (CustomEventType customEventType : values()) {
                if (customEventType.mClassName != null
                        && customEventType.mClassName.equals(className)) {
                    return customEventType;
                }
            }

            return UNSPECIFIED;
        }

        @Override
        public String toString() {
            return mClassName;
        }

        public static boolean isCDAdSpecific(@Nullable final String className) {
            return fromClassName(className).mIsCDAdSpecific;
        }
    }

    public static final String BANNER_SUFFIX = "_banner";
    public static final String INTERSTITIAL_SUFFIX = "_interstitial";

    static String getAdNetworkType(String adType, String fullAdType) {
        String adNetworkType = AdType.INTERSTITIAL.equals(adType) ? fullAdType : adType;
        return adNetworkType != null ? adNetworkType : "unknown";
    }

    public static String getCustomEventName(@NonNull AdFormat adFormat,
            @NonNull String adType,
            @Nullable String fullAdType,
            @NonNull Map<String, String> headers) {
        if (AdType.CUSTOM.equalsIgnoreCase(adType)) {
            return extractHeader(headers, ResponseHeader.CUSTOM_EVENT_NAME);
        } else if (AdType.STATIC_NATIVE.equalsIgnoreCase(adType)) {
            return CustomEventType.CDADS_NATIVE.toString();
        } else if (AdType.VIDEO_NATIVE.equalsIgnoreCase(adType)) {
            return CustomEventType.CDADS_VIDEO_NATIVE.toString();
        } else if (AdType.REWARDED_VIDEO.equalsIgnoreCase(adType)) {
            return CustomEventType.CDADS_REWARDED_VIDEO.toString();
        } else if (AdType.REWARDED_PLAYABLE.equalsIgnoreCase(adType)) {
            return CustomEventType.CDADS_REWARDED_PLAYABLE.toString();
        } else if (AdType.HTML.equalsIgnoreCase(adType) || AdType.MRAID.equalsIgnoreCase(adType)) {
            return (AdFormat.INTERSTITIAL.equals(adFormat)
                    ? CustomEventType.fromString(adType + INTERSTITIAL_SUFFIX)
                    : CustomEventType.fromString(adType + BANNER_SUFFIX)).toString();
        } else if (AdType.INTERSTITIAL.equalsIgnoreCase(adType)) {
            return CustomEventType.fromString(fullAdType + INTERSTITIAL_SUFFIX).toString();
        } else {
            return CustomEventType.fromString(adType + BANNER_SUFFIX).toString();
        }
    }
}
