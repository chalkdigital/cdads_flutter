package com.chalkdigital.nativeads;

import android.support.annotation.NonNull;

public enum NativeErrorCode {
    EMPTY_AD_RESPONSE("Server returned empty response."),
    INVALID_RESPONSE("Unable to parse response from server."),
    IMAGE_DOWNLOAD_FAILURE("Unable to download images associated with ad."),
    INVALID_REQUEST("Invalid nativs ad request."),
    UNEXPECTED_RESPONSE_CODE("Received unexpected response code from server."),
    SERVER_ERROR_RESPONSE_CODE("Server returned erroneous response code."),
    CONNECTION_ERROR("Network is unavailable."),
    UNSPECIFIED("Unspecified error occurred."),
    VPAID_SOURCE_ERROR("Vpaid source error occurred."),

    NETWORK_INVALID_REQUEST("Third-party network received invalid request."),
    NETWORK_TIMEOUT("Third-party network failed to respond in a timely manner."),
    NETWORK_NO_FILL("Third-party network failed to provide an ad."),
    NETWORK_INVALID_STATE("Third-party network failed due to invalid internal state."),

    NATIVE_RENDERER_CONFIGURATION_ERROR("A required renderer was not registered for the CustomEventNative."),
    NATIVE_ADAPTER_CONFIGURATION_ERROR("CustomEventNative was configured incorrectly."),
    NATIVE_ADAPTER_NOT_FOUND("Unable to find CustomEventNative."),
    CDADS_NOT_INITIALIZED("CDADS sdk not initialized, please initialize CDAdsUtils.initialize() in application class.");

    private String message;

    private NativeErrorCode(String message) {
        this.message = message;
    }

    @NonNull
    @Override
    public final String toString() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }
}
