package com.chalkdigital.banner.ads;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.chalkdigital.ads.CDAdErrorCode;
import com.chalkdigital.ads.BaseHtmlWebView;
import com.chalkdigital.ads.HtmlWebViewClient;
import com.chalkdigital.ads.HtmlWebViewListener;
import com.chalkdigital.common.AdReport;
import com.chalkdigital.common.logging.CDAdLog;

import static com.chalkdigital.banner.ads.CustomEventBanner.CustomEventBannerListener;

public class HtmlBannerWebView extends BaseHtmlWebView {
    public static final String EXTRA_AD_CLICK_DATA = "com.chalkdigital.intent.extra.AD_CLICK_DATA";

    public HtmlBannerWebView(Context context, AdReport adReport) {
        super(context, adReport);
    }

    public void init(CustomEventBannerListener customEventBannerListener, boolean isScrollable, String redirectUrl, String clickthroughUrl, String dspCreativeId, String clickAction) {
        super.init(isScrollable);
        setWebViewClient(new HtmlWebViewClient(new HtmlBannerWebViewListener(customEventBannerListener), this, clickthroughUrl, redirectUrl, dspCreativeId, clickAction));
        setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(final WebView view, final String url, final String message,
                                     final JsResult result) {
                CDAdLog.d(message);
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public boolean onConsoleMessage(@NonNull final ConsoleMessage consoleMessage) {
                CDAdLog.d(consoleMessage.message());
                return super.onConsoleMessage(consoleMessage);
            }

            @Override
            public void onShowCustomView(final View view, final CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
            }
        });
    }

    static class HtmlBannerWebViewListener implements HtmlWebViewListener {
        private final CustomEventBannerListener mCustomEventBannerListener;

        public HtmlBannerWebViewListener(CustomEventBannerListener customEventBannerListener) {
            mCustomEventBannerListener = customEventBannerListener;
        }

        @Override
        public void onLoaded(BaseHtmlWebView htmlWebView) {
            mCustomEventBannerListener.onBannerLoaded(htmlWebView);
        }

        @Override
        public void onFailed(CDAdErrorCode errorCode) {
            mCustomEventBannerListener.onBannerFailed(errorCode);
        }

        @Override
        public void onClicked() {
            mCustomEventBannerListener.onBannerClicked();
        }

        @Override
        public void onCollapsed() {
            mCustomEventBannerListener.onBannerCollapsed();
        }

    }
}
