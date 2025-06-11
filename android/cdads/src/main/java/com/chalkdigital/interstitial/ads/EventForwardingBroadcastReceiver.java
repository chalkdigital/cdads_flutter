package com.chalkdigital.interstitial.ads;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;

import com.chalkdigital.ads.CDAdErrorCode;
import com.chalkdigital.ads.BaseBroadcastReceiver;
import com.chalkdigital.common.DataKeys;
import com.chalkdigital.common.IntentActions;

import static com.chalkdigital.ads.CDAdErrorCode.NETWORK_INVALID_STATE;
import static com.chalkdigital.ads.CDAdErrorCode.UNSPECIFIED;
import static com.chalkdigital.interstitial.ads.CustomEventInterstitial.CustomEventInterstitialListener;

public class EventForwardingBroadcastReceiver extends BaseBroadcastReceiver {
    private final CustomEventInterstitialListener mCustomEventInterstitialListener;


    private static IntentFilter sIntentFilter;


    public EventForwardingBroadcastReceiver(CustomEventInterstitialListener customEventInterstitialListener, final long broadcastIdentifier) {
        super(broadcastIdentifier);
        mCustomEventInterstitialListener = customEventInterstitialListener;
        getIntentFilter();
    }

    @NonNull
    public IntentFilter getIntentFilter() {
        if (sIntentFilter == null) {
            sIntentFilter = new IntentFilter();
            sIntentFilter.addAction(IntentActions.ACTION_INTERSTITIAL_FAIL);
            sIntentFilter.addAction(IntentActions.ACTION_INTERSTITIAL_SHOW);
            sIntentFilter.addAction(IntentActions.ACTION_INTERSTITIAL_DISMISS);
            sIntentFilter.addAction(IntentActions.ACTION_INTERSTITIAL_CLICK);
            sIntentFilter.addAction(IntentActions.ACTION_INTERSTITIAL_VIDEO_COMPLETE);
        }
        return sIntentFilter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mCustomEventInterstitialListener == null) {
            return;
        }

        if (!shouldConsumeBroadcast(intent)) {
            return;
        }

        final String action = intent.getAction();
        if (IntentActions.ACTION_INTERSTITIAL_FAIL.equals(action)) {
            CDAdErrorCode errorcode = UNSPECIFIED;
            UNSPECIFIED.setMessage(intent.getStringExtra(DataKeys.BROADCAST_INFO_KEY));
            mCustomEventInterstitialListener.onInterstitialFailed(intent.getStringExtra(DataKeys.BROADCAST_INFO_KEY)!=null?errorcode:NETWORK_INVALID_STATE);
        } else if (IntentActions.ACTION_INTERSTITIAL_SHOW.equals(action)) {
            mCustomEventInterstitialListener.onInterstitialShown();
        } else if (IntentActions.ACTION_INTERSTITIAL_DISMISS.equals(action)) {
            mCustomEventInterstitialListener.onInterstitialDismissed();
            unregister(this);
        } else if (IntentActions.ACTION_INTERSTITIAL_CLICK.equals(action)) {
            mCustomEventInterstitialListener.onInterstitialClicked();
        }else if (IntentActions.ACTION_INTERSTITIAL_VIDEO_COMPLETE.equals(action)) {
            mCustomEventInterstitialListener.onInterstitialVideoEnded();
        }

    }
}
