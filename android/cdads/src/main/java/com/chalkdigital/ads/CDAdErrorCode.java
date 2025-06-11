package com.chalkdigital.ads;

public enum CDAdErrorCode {
    NO_FILL("No ads found."),
    WARMUP("Ad unit is warming up. Try again in a few minutes."),
    SERVER_ERROR("Unable to connect to CDAd adserver."),
    IP_ERROR("Unable to fetch public IP."),
    LOCATION_ERROR("Unable to fetch location."),
    INTERNAL_ERROR("Unable to serve ad due to invalid internal state."),
    CANCELLED("Ad request was cancelled."),
    NO_CONNECTION("No internet connection detected."),

    /** see {@link com.chalkdigital.common.Constants#AD_EXPIRATION_DELAY } */
    EXPIRED("Ad expired since it was not shown within 4 hours."),

    ADAPTER_NOT_FOUND("Unable to find Native Network or Custom Event adapter."),
    ADAPTER_CONFIGURATION_ERROR("Native Network or Custom Event adapter was configured incorrectly."),
    NETWORK_TIMEOUT("Third-party network failed to respond in a timely manner."),
    NETWORK_NO_FILL("Third-party network failed to provide an ad."),
    NETWORK_INVALID_STATE("Third-party network failed due to invalid internal state."),
    MRAID_LOAD_ERROR("Error loading MRAID ad."),
    SPARK_LOAD_ERROR("Error loading VPAID ad."),
    VPAID_SOURCE_URL_ERROR("Vpaid source url error."),
    VIDEO_CACHE_ERROR("Error creating a cache to store downloaded videos."),
    VIDEO_DOWNLOAD_ERROR("Error downloading video."),

    VIDEO_NOT_AVAILABLE("No video loaded for ad unit."),
    VIDEO_PLAYBACK_ERROR("Error playing a video."),

    REWARDED_CURRENCIES_PARSING_ERROR("Error parsing rewarded currencies JSON header."),
    REWARD_NOT_SELECTED("Reward not selected for rewarded ad."),

    UNSPECIFIED("Unspecified error."),
    CDADS_PARTNER_ID_NOT_CONFIGURED("Partner id not defined in strings.xml."),
    CDADS_SERVER_URL_NOT_CONFIGURED("Server url not defined in strings.xml."),
    CDADS_SERVER_PATH_NOT_CONFIGURED("Server path not defined in strings.xml."),
    CDADS_CAT_NOT_CONFIGURED("Application category not defined in strings.xml."),
    CDADS_NOT_INITIALIZED("CDADS sdk not initialized, please initialize CDAdsUtils.initialize() in application class");

    private String message;

    private CDAdErrorCode(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return this.message;
    }

    public void setMessage(String info){
        this.message = info;
    }
}
