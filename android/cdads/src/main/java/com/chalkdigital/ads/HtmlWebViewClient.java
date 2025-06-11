package com.chalkdigital.ads;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.support.annotation.NonNull;
import android.webkit.MimeTypeMap;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.chalkdigital.common.UrlAction;
import com.chalkdigital.common.UrlHandler;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.Intents;
import com.chalkdigital.common.util.Utils;
import com.chalkdigital.exceptions.IntentNotResolvableException;
import com.chalkdigital.network.retrofit.CDAdParams;

import java.util.EnumSet;

import static com.chalkdigital.ads.CDAdErrorCode.UNSPECIFIED;

public class HtmlWebViewClient extends WebViewClient {
    public static final String CDADS_FINISH_LOAD = "chalkdigital://finishLoad";
    public static final String CDADS_FAIL_LOAD = "chalkdigital://failLoad";

    private final EnumSet<UrlAction> SUPPORTED_URL_ACTIONS = EnumSet.of(
            UrlAction.HANDLE_CDAD_SCHEME,
            UrlAction.IGNORE_ABOUT_SCHEME,
            UrlAction.HANDLE_PHONE_SCHEME,
            UrlAction.OPEN_APP_MARKET,
            UrlAction.OPEN_NATIVE_BROWSER,
            UrlAction.OPEN_IN_APP_BROWSER,
            UrlAction.HANDLE_SHARE_TWEET,
            UrlAction.FOLLOW_DEEP_LINK_WITH_FALLBACK,
            UrlAction.FOLLOW_DEEP_LINK);

    private final Context mContext;
    private final String mDspCreativeId;
    private final HtmlWebViewListener mHtmlWebViewListener;
    private final BaseHtmlWebView mHtmlWebView;
    private final String mClickthroughUrl;
    private final String mRedirectUrl;
    private final String mclickAction;

    public HtmlWebViewClient(HtmlWebViewListener htmlWebViewListener,
            BaseHtmlWebView htmlWebView, String clickthrough,
            String redirect, String dspCreativeId, final String clickAction) {
        mHtmlWebViewListener = htmlWebViewListener;
        mHtmlWebView = htmlWebView;
        mClickthroughUrl = clickthrough;
        mRedirectUrl = redirect;
        mDspCreativeId = dspCreativeId;
        mContext = htmlWebView.getContext();
        mclickAction = clickAction;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {

        if(url.toLowerCase().contains("/favicon.ico")) {
            try {
                return new WebResourceResponse("image/png", null, null);
            } catch (Exception e) {
                CDAdLog.d("Failed to respond for : "+url);
            }
        }
        else if (url.toLowerCase().contains(CDAdParams.webViewBaseUrl)){
            try {
                return new WebResourceResponse("text/html", null, null);
            } catch (Exception e) {
                CDAdLog.d("Failed to respond for : "+url);
            }
        }

//        if (URLUtil.isValidUrl(url)){
//            try {
//                OkHttpClient httpClient = new OkHttpClient();
//                Request request = new Request.Builder()
//                        .url(url.trim())
//                        .addHeader("x-forwarded-for", "107.77.161.3") //add headers
//                        .build();
//                Response response = httpClient.newCall(request).execute();
//                return new WebResourceResponse(
//                        getMimeType(url), // set content-type
//                        response.header("content-encoding", "utf-8"),
//                        response.body().byteStream()
//                );
//            }  catch (IOException e) {
//                return null;
//            }
//        }
        return super.shouldInterceptRequest(view, url);
    }

    //get mime type by url
    public String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            if (extension.equals("js")) {
                return "text/javascript";
            }
            else if (extension.equals("woff")) {
                return "application/font-woff";
            }
            else if (extension.equals("woff2")) {
                return "application/font-woff2";
            }
            else if (extension.equals("ttf")) {
                return "application/x-font-ttf";
            }
            else if (extension.equals("eot")) {
                return "application/vnd.ms-fontobject";
            }
            else if (extension.equals("svg")) {
                return "image/svg+xml";
            }
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    @Override
    public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
        new UrlHandler.Builder()
                .withDspCreativeId(mDspCreativeId)
                .withSupportedUrlActions(SUPPORTED_URL_ACTIONS)
                .withResultActions(new UrlHandler.ResultActions() {
                    @Override
                    public void urlHandlingSucceeded(@NonNull String url,
                            @NonNull UrlAction urlAction) {
                        if (mHtmlWebView.wasClicked()) {
                            mHtmlWebViewListener.onClicked();
                            mHtmlWebView.onResetUserClick();
                        }
                    }

                    @Override
                    public void urlHandlingFailed(@NonNull String url,
                            @NonNull UrlAction lastFailedUrlAction) {
                    }
                })
                .withCDAdSchemeListener(new UrlHandler.CDAdSchemeListener() {
                    @Override
                    public void onFinishLoad() {
                        mHtmlWebViewListener.onLoaded(mHtmlWebView);
                    }

                    @Override
                    public void onClose() {
                        mHtmlWebViewListener.onCollapsed();
                    }

                    @Override
                    public void onFailLoad() {
                        mHtmlWebView.stopLoading();
                        mHtmlWebViewListener.onFailed(UNSPECIFIED);
                    }
                })
                .build().handleUrl(mContext, url, mHtmlWebView.wasClicked(), mclickAction);
        return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        // If the URL being loaded shares the redirectUrl prefix, open it in the browser.
        CDAdLog.d(url);
        if (mRedirectUrl != null && url.startsWith(mRedirectUrl)) {
            view.stopLoading();
            if (mHtmlWebView.wasClicked()) {
                try {
                    Intents.showCDAdBrowserForUrl(mContext, Uri.parse(url), mDspCreativeId);
                } catch (IntentNotResolvableException e) {
                        Utils.logStackTrace(e);
                    CDAdLog.d(e.getMessage());
                }
            } else {
                CDAdLog.d("Attempted to redirect without user interaction");
            }
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        mHtmlWebViewListener.onLoaded(mHtmlWebView);
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP && !request.getUrl().toString().contains(CDAdParams.webViewBaseUrl)){
            mHtmlWebView.stopLoading();
            mHtmlWebViewListener.onFailed(UNSPECIFIED);
        }
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
        mHtmlWebView.stopLoading();
        mHtmlWebViewListener.onFailed(UNSPECIFIED);
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        super.onReceivedSslError(view, handler, error);
        mHtmlWebView.stopLoading();
        mHtmlWebViewListener.onFailed(UNSPECIFIED);
    }



}
