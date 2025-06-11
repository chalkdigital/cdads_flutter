package com.chalkdigital.interstitial.ads;

import com.chalkdigital.ads.CDAdErrorCode;

import static com.chalkdigital.interstitial.ads.CDAdInterstitial.InterstitialAdListener;

public class DefaultInterstitialAdListener implements InterstitialAdListener {
    @Override
    public void onInterstitialLoaded(CDAdInterstitial interstitial) { }
    @Override
    public void onInterstitialFailed(CDAdInterstitial interstitial, CDAdErrorCode errorCode) { }
    @Override
    public void onInterstitialShown(CDAdInterstitial interstitial) { }
    @Override
    public void onInterstitialClicked(CDAdInterstitial interstitial) { }
    @Override
    public void onInterstitialDismissed(CDAdInterstitial interstitial) { }
    @Override
    public void onInterstitialAdRequest(CDAdInterstitial interstitial) {

    }
}
