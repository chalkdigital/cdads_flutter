package com.chalkdigital.interstitial.ads;

import android.content.Context;
import android.os.Handler;

import com.chalkdigital.ads.CDAdErrorCode;
import com.chalkdigital.ads.BaseHtmlWebView;
import com.chalkdigital.ads.HtmlWebViewClient;
import com.chalkdigital.ads.HtmlWebViewListener;
import com.chalkdigital.common.AdReport;

import static com.chalkdigital.interstitial.ads.CustomEventInterstitial.CustomEventInterstitialListener;

public class HtmlInterstitialWebView extends BaseHtmlWebView {
    private Handler mHandler;

    public HtmlInterstitialWebView(Context context, AdReport adReport) {
        super(context, adReport);

        mHandler = new Handler();
    }

    public void init(final CustomEventInterstitialListener customEventInterstitialListener, boolean isScrollable, String redirectUrl, String clickthroughUrl, String dspCreativeId, String clickAction) {
        super.init(isScrollable);

        HtmlInterstitialWebViewListener htmlInterstitialWebViewListener = new HtmlInterstitialWebViewListener(customEventInterstitialListener);
        HtmlWebViewClient htmlWebViewClient = new HtmlWebViewClient(htmlInterstitialWebViewListener, this, clickthroughUrl, redirectUrl, dspCreativeId, clickAction);
        setWebViewClient(htmlWebViewClient);
    }

    private void postHandlerRunnable(Runnable r) {
        mHandler.post(r);
    }

    static class HtmlInterstitialWebViewListener implements HtmlWebViewListener {
        private final CustomEventInterstitialListener mCustomEventInterstitialListener;

        public HtmlInterstitialWebViewListener(CustomEventInterstitialListener customEventInterstitialListener) {
            mCustomEventInterstitialListener = customEventInterstitialListener;
        }

        @Override
        public void onLoaded(BaseHtmlWebView mHtmlWebView) {
            mCustomEventInterstitialListener.onInterstitialLoaded();
        }

        @Override
        public void onFailed(CDAdErrorCode errorCode) {
            mCustomEventInterstitialListener.onInterstitialFailed(errorCode);
        }

        @Override
        public void onClicked() {
            mCustomEventInterstitialListener.onInterstitialClicked();
        }

        @Override
        public void onCollapsed() {
            // Ignored
        }
    }
}
