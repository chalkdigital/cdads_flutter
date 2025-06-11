package com.chalkdigital.common;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.network.retrofit.CDAdParams;

import java.util.EnumSet;

import static com.chalkdigital.common.util.Drawables.LEFT_ARROW;
import static com.chalkdigital.common.util.Drawables.RIGHT_ARROW;
import static com.chalkdigital.common.util.Drawables.UNLEFT_ARROW;
import static com.chalkdigital.common.util.Drawables.UNRIGHT_ARROW;

class BrowserWebViewClient extends WebViewClient {

    private static final EnumSet<UrlAction> SUPPORTED_URL_ACTIONS = EnumSet.of(
            UrlAction.HANDLE_PHONE_SCHEME,
            UrlAction.OPEN_APP_MARKET,
            UrlAction.OPEN_IN_APP_BROWSER,
            UrlAction.HANDLE_SHARE_TWEET,
            UrlAction.FOLLOW_DEEP_LINK_WITH_FALLBACK,
            UrlAction.FOLLOW_DEEP_LINK
    );

    @NonNull
    private CDAdBrowser mCDAdBrowser;

    public BrowserWebViewClient(@NonNull final CDAdBrowser cdAdBrowser) {
        mCDAdBrowser = cdAdBrowser;
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description,
            String failingUrl) {
        CDAdLog.d("CDAdBrowser error: " + description);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }

        UrlHandler urlHandler = new UrlHandler.Builder()
                .withSupportedUrlActions(SUPPORTED_URL_ACTIONS)
                .withoutCDAdBrowser()
                .withResultActions(new UrlHandler.ResultActions() {
                    @Override
                    public void urlHandlingSucceeded(@NonNull String url,
                            @NonNull UrlAction urlAction) {
                        if (urlAction.equals(UrlAction.OPEN_IN_APP_BROWSER)) {
                            mCDAdBrowser.getWebView().loadUrl(url);
                        } else {
                            // UrlAction opened in external app, so close CDAdBrowser
                            mCDAdBrowser.finish();
                        }
                    }

                    @Override
                    public void urlHandlingFailed(@NonNull String url,
                            @NonNull UrlAction lastFailedUrlAction) {
                    }
                })
                .build();

        return urlHandler.handleResolvedUrl(mCDAdBrowser.getApplicationContext(), url,
                true, // = fromUserInteraction
                null, // = trackingUrls
                null);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        Drawable backImageDrawable = view.canGoBack()
                ? LEFT_ARROW.createDrawable(mCDAdBrowser)
                : UNLEFT_ARROW.createDrawable(mCDAdBrowser);
        mCDAdBrowser.getBackButton().setImageDrawable(backImageDrawable);

        Drawable forwardImageDrawable = view.canGoForward()
                ? RIGHT_ARROW.createDrawable(mCDAdBrowser)
                : UNRIGHT_ARROW.createDrawable(mCDAdBrowser);
        mCDAdBrowser.getForwardButton().setImageDrawable(forwardImageDrawable);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {

        if(url.toLowerCase().contains("/favicon.ico")) {
            try {
                return new WebResourceResponse("image/png", null, null);
            } catch (Exception e) {
            }
        }
        else if (url.toLowerCase().contains(CDAdParams.webViewBaseUrl)){
            try {
                return new WebResourceResponse("text/html", null, null);
            } catch (Exception e) {
                CDAdLog.d("Failed to respond for : "+url);
            }
        }

        return null;
    }

}
