package com.chalkdigital.network;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import retrofit2.Response;

public class CDAdNetworkError extends Throwable {
    public enum Reason {
        WARMING_UP,
        NO_FILL,
        BAD_HEADER_DATA,
        BAD_BODY,
        TRACKING_FAILURE,
        UNSPECIFIED
    }

    @NonNull private final Reason mReason;
    @Nullable private final Integer mRefreshTime;
    @Nullable public final Response networkResponse;

    public CDAdNetworkError(@NonNull Reason reason) {
        networkResponse = null;
        mReason = reason;
        mRefreshTime = null;
    }



    public CDAdNetworkError(@NonNull Response networkResponse, @NonNull Reason reason) {
        this.networkResponse = networkResponse;
        mReason = reason;
        mRefreshTime = null;
    }

    public CDAdNetworkError(@NonNull Throwable cause, @NonNull Reason reason) {
        super(cause);
        networkResponse = null;
        mReason = reason;
        mRefreshTime = null;
    }

    public CDAdNetworkError(@NonNull String message, @NonNull Reason reason) {
        this(message, reason, null);
    }

    public CDAdNetworkError(@NonNull String message, @NonNull Throwable cause, @NonNull Reason reason) {
        super(message, cause);
        networkResponse = null;
        mReason = reason;
        mRefreshTime = null;
    }

    public CDAdNetworkError(@NonNull Response networkResponse, @NonNull Throwable cause, @NonNull Reason reason) {
        this.networkResponse = networkResponse;
        mReason = reason;
        mRefreshTime = null;
    }

    public CDAdNetworkError(@NonNull String message, @NonNull Reason reason,
            @Nullable Integer refreshTime) {
        super(message);
        networkResponse = null;
        mReason = reason;
        mRefreshTime = refreshTime;
    }

    @NonNull
    public Reason getReason() {
        return mReason;
    }

    @Nullable
    public Integer getRefreshTime() {
        return mRefreshTime;
    }
}
