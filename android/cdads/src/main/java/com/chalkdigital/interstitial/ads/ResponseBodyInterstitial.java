package com.chalkdigital.interstitial.ads;

import android.content.Context;
import android.support.annotation.NonNull;

import com.chalkdigital.common.AdReport;
import com.chalkdigital.common.CDAdSize;
import com.chalkdigital.common.ExternalViewabilitySessionManager;
import com.chalkdigital.common.util.Utils;
import com.chalkdigital.mediation.CDMediationAdRequest;
import com.chalkdigital.mediation.MediationConstants;
import com.chalkdigital.network.response.TypeParser;

import java.util.HashMap;
import java.util.Map;

import static com.chalkdigital.ads.CDAdErrorCode.NETWORK_INVALID_STATE;
import static com.chalkdigital.common.DataKeys.HTML_RESPONSE_BODY_KEY;

public abstract class ResponseBodyInterstitial extends CustomEventInterstitial {
    private EventForwardingBroadcastReceiver mBroadcastReceiver;
    protected Context mContext;
    protected AdReport mAdReport;
    protected long mBroadcastIdentifier;
    protected ExternalViewabilitySessionManager mExternalViewabilitySessionManager;
    protected HashMap<String, String> mServerExtras;
    protected Map<String, Object> mParams;

    abstract protected void extractExtras(Map<String, String> serverExtras);
    abstract protected void preRenderHtml(CustomEventInterstitialListener customEventInterstitialListener, Map<String, String> serverExtras);
    public abstract void showInterstitial();

    @Override
    protected void loadInterstitial(
            Context context,
            CustomEventInterstitialListener customEventInterstitialListener,
            final Map<String, Object> params, final CDMediationAdRequest mediationAdRequest, final CDAdSize cdAdSize, final
            HashMap<String, String> serverExtras) {

        mParams = params;
        mContext = context;
        mServerExtras = serverExtras;
        if (extrasAreValid(serverExtras)) {
            extractExtras(serverExtras);
        } else {
            customEventInterstitialListener.onInterstitialFailed(NETWORK_INVALID_STATE);
            return;
        }

        mBroadcastIdentifier = Utils.generateUniqueId();
        mBroadcastReceiver = new EventForwardingBroadcastReceiver(customEventInterstitialListener,
                mBroadcastIdentifier);
        mBroadcastReceiver.register(mBroadcastReceiver, context);

        preRenderHtml(customEventInterstitialListener, serverExtras);
    }

    @Override
    public void onInvalidate() {
        if (mBroadcastReceiver != null) {
            mBroadcastReceiver.unregister(mBroadcastReceiver);
        }
    }

    private boolean extrasAreValid(Map<String,String> serverExtras) {
        return serverExtras.containsKey(HTML_RESPONSE_BODY_KEY);
    }

    @Override
    protected String getId() {
        if (mParams!=null && mParams.keySet().contains(MediationConstants.SDK_ID))
            return TypeParser.parseString(mParams.get(MediationConstants.SDK_ID), "");
        return "";
    }
}
