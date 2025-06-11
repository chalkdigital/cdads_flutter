package com.chalkdigital.interstitial.mraid;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.chalkdigital.common.CDAdConstants;
import com.chalkdigital.interstitial.ads.MraidActivity;
import com.chalkdigital.interstitial.ads.ResponseBodyInterstitial;
import com.chalkdigital.mraid.MraidWebViewClient;

import java.util.HashMap;
import java.util.Map;

import static com.chalkdigital.common.DataKeys.HTML_RESPONSE_BODY_KEY;

class MraidInterstitial extends ResponseBodyInterstitial {
    @Nullable
    protected String mHtmlData;

    @Override
    protected void extractExtras(Map<String, String> serverExtras) {
        mHtmlData = serverExtras.get(HTML_RESPONSE_BODY_KEY);
        mHtmlData = mHtmlData.contains(MraidWebViewClient.MRAID_JS)?mHtmlData:CDAdConstants.mraidJSPrefix +mHtmlData;
    }

    @Override
    protected void preRenderHtml(@NonNull CustomEventInterstitialListener
            customEventInterstitialListener, Map<String, String> serverExtras) {
        MraidActivity.preRenderHtml(this, mContext, customEventInterstitialListener, mHtmlData,
                mBroadcastIdentifier, mServerExtras);
    }

    @Override
    public void showInterstitial() {
        MraidActivity.start(mContext, mAdReport, mHtmlData, mBroadcastIdentifier, mServerExtras);
    }
}
