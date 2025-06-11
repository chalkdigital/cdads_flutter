package com.chalkdigital.interstitial.ads;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import static com.chalkdigital.common.DataKeys.CLICKTHROUGH_URL_KEY;
import static com.chalkdigital.common.DataKeys.HTML_RESPONSE_BODY_KEY;
import static com.chalkdigital.common.DataKeys.REDIRECT_URL_KEY;
import static com.chalkdigital.common.DataKeys.SCROLLABLE_KEY;

public class HtmlInterstitial extends ResponseBodyInterstitial {
    private String mHtmlData;
    private boolean mIsScrollable;
    private String mRedirectUrl;
    private String mClickthroughUrl;

    @Override
    protected void extractExtras(Map<String, String> serverExtras) {
        mHtmlData = serverExtras.get(HTML_RESPONSE_BODY_KEY);
        mIsScrollable = Boolean.valueOf(serverExtras.get(SCROLLABLE_KEY));
        mRedirectUrl = serverExtras.get(REDIRECT_URL_KEY);
        mClickthroughUrl = serverExtras.get(CLICKTHROUGH_URL_KEY);
    }

    @Override
    protected void preRenderHtml(CustomEventInterstitialListener customEventInterstitialListener,
                                 @NonNull Map<String, String> serverExtras) {
        CDAdActivity.preRenderHtml(this, mContext, mAdReport, customEventInterstitialListener, mHtmlData,
                mIsScrollable, mRedirectUrl, mClickthroughUrl, mBroadcastIdentifier, serverExtras);
    }

    @Override
    public void showInterstitial() {
        CDAdActivity.start(mContext, mHtmlData, mAdReport, mIsScrollable,
                mRedirectUrl, mClickthroughUrl,
                mBroadcastIdentifier, mServerExtras);
    }
}
