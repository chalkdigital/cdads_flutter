package com.chalkdigital.interstitial.ads;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.chalkdigital.ads.CDAdErrorCode;
import com.chalkdigital.ads.BaseBroadcastReceiver;
import com.chalkdigital.ads.Interstitial;
import com.chalkdigital.ads.WebViewCacheService;
import com.chalkdigital.common.AdReport;
import com.chalkdigital.common.DataKeys;
import com.chalkdigital.common.ExternalViewabilitySessionManager;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.Utils;
import com.chalkdigital.interstitial.ads.factories.HtmlInterstitialWebViewFactory;
import com.chalkdigital.network.response.TypeParser;
import com.chalkdigital.network.retrofit.CDAdParams;

import java.util.HashMap;
import java.util.Map;

import static com.chalkdigital.ads.HtmlWebViewClient.CDADS_FAIL_LOAD;
import static com.chalkdigital.ads.HtmlWebViewClient.CDADS_FINISH_LOAD;
import static com.chalkdigital.common.DataKeys.AD_EVENTS_KEY;
import static com.chalkdigital.common.DataKeys.AD_REPORT_KEY;
import static com.chalkdigital.common.DataKeys.AD_SERVER_EXTRAS_KEY;
import static com.chalkdigital.common.DataKeys.BROADCAST_IDENTIFIER_KEY;
import static com.chalkdigital.common.DataKeys.CLICKTHROUGH_URL_KEY;
import static com.chalkdigital.common.DataKeys.CLICK_ACTION;
import static com.chalkdigital.common.DataKeys.HTML_RESPONSE_BODY_KEY;
import static com.chalkdigital.common.DataKeys.REDIRECT_URL_KEY;
import static com.chalkdigital.common.DataKeys.SCROLLABLE_KEY;
import static com.chalkdigital.common.IntentActions.ACTION_INTERSTITIAL_CLICK;
import static com.chalkdigital.common.IntentActions.ACTION_INTERSTITIAL_DISMISS;
import static com.chalkdigital.common.IntentActions.ACTION_INTERSTITIAL_FAIL;
import static com.chalkdigital.common.IntentActions.ACTION_INTERSTITIAL_SHOW;
import static com.chalkdigital.common.util.JavaScriptWebViewCallbacks.WEB_VIEW_DID_APPEAR;
import static com.chalkdigital.common.util.JavaScriptWebViewCallbacks.WEB_VIEW_DID_CLOSE;
import static com.chalkdigital.interstitial.ads.CustomEventInterstitial.CustomEventInterstitialListener;

public class CDAdActivity extends BaseInterstitialActivity {
    @Nullable
    private HtmlInterstitialWebView mHtmlInterstitialWebView;
    @Nullable
    private ExternalViewabilitySessionManager mExternalViewabilitySessionManager;

    public static void start(Context context, String htmlData, AdReport adReport,
                             boolean isScrollable, String redirectUrl, String clickthroughUrl,
                             long broadcastIdentifier, final HashMap<String, String> serverExtras) {
        Intent intent = createIntent(context, htmlData, adReport, isScrollable,
                redirectUrl, clickthroughUrl, broadcastIdentifier, serverExtras);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException anfe) {
            Log.d("CDAdActivity", "CDAdActivity not found - did you declare it in AndroidManifest.xml?");
        }
    }

    static Intent createIntent(Context context,
                               String htmlData, AdReport adReport, boolean isScrollable, String redirectUrl,
                               String clickthroughUrl, long broadcastIdentifier, final HashMap<String, String> serverExtras) {
        Intent intent = new Intent(context, CDAdActivity.class);
        intent.putExtra(HTML_RESPONSE_BODY_KEY, htmlData);
        intent.putExtra(SCROLLABLE_KEY, isScrollable);
        intent.putExtra(CLICKTHROUGH_URL_KEY, clickthroughUrl);
        intent.putExtra(REDIRECT_URL_KEY, redirectUrl);
        intent.putExtra(BROADCAST_IDENTIFIER_KEY, broadcastIdentifier);
        intent.putExtra(AD_REPORT_KEY, adReport);
        intent.putExtra(CLICK_ACTION, serverExtras.get(CLICK_ACTION));
        intent.putExtra(AD_SERVER_EXTRAS_KEY, serverExtras);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

   public static void preRenderHtml(final Interstitial baseInterstitial,
            final Context context,
            final AdReport adReport,
            final CustomEventInterstitialListener customEventInterstitialListener,
            final String htmlData,
            final boolean isScrollable,
            final String redirectUrl,
            final String clickthroughUrl,
            final long broadcastIdentifier,
            final Map<String, String> serverExtras) {
        final HtmlInterstitialWebView htmlInterstitialWebView = HtmlInterstitialWebViewFactory.create(
                context.getApplicationContext(), adReport, customEventInterstitialListener,
                isScrollable, redirectUrl, clickthroughUrl, serverExtras.get(DataKeys.CLICK_ACTION));
        htmlInterstitialWebView.enablePlugins(false);
        htmlInterstitialWebView.enableJavascriptCaching();
        htmlInterstitialWebView.setInitialScale(Utils.getInterstitialScale(context, serverExtras.get(DataKeys.WIDTH), serverExtras.get(DataKeys.HEIGHT), serverExtras.get(DataKeys.STRETCH), TypeParser.parseInteger(serverExtras.get(DataKeys.CREATIVE_ORIENTATION_KEY), ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)));

        htmlInterstitialWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (CDADS_FINISH_LOAD.equals(url)) {
                    customEventInterstitialListener.onInterstitialLoaded();
                } else if (CDADS_FAIL_LOAD.equals(url)) {
                    customEventInterstitialListener.onInterstitialFailed(null);
                }

                return true;
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


            @Override
            public void onPageFinished(final WebView view, final String url) {
                customEventInterstitialListener.onInterstitialLoaded();
            }

            @Override
            public void onReceivedError(final WebView view, final int errorCode,
                                        final String description,
                                        final String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP && !failingUrl.contains(CDAdParams.webViewBaseUrl)) {
                    customEventInterstitialListener.onInterstitialFailed(
                            CDAdErrorCode.INTERNAL_ERROR);
                }
            }
        });

        final ExternalViewabilitySessionManager externalViewabilitySessionManager =
                new ExternalViewabilitySessionManager(context);
        externalViewabilitySessionManager.createDisplaySession(context, htmlInterstitialWebView, true);

        htmlInterstitialWebView.loadHtmlResponse(htmlData);
        WebViewCacheService.storeWebViewConfig(broadcastIdentifier, baseInterstitial,
                htmlInterstitialWebView, externalViewabilitySessionManager);
    }

    @Override
    public View getAdView(@NonNull final HashMap<String, String[]> events) {
        Intent intent = getIntent();
        boolean isScrollable = intent.getBooleanExtra(SCROLLABLE_KEY, false);
        String redirectUrl = intent.getStringExtra(REDIRECT_URL_KEY);
        String clickthroughUrl = intent.getStringExtra(CLICKTHROUGH_URL_KEY);
        String htmlResponse = intent.getStringExtra(HTML_RESPONSE_BODY_KEY);
        String clickAction = intent.getStringExtra(CLICK_ACTION);

        final Long broadcastIdentifier = getBroadcastIdentifier();
        if (broadcastIdentifier != null) {
            // If a cache hit happens, the content is already loaded; therefore, this re-initializes
            // the WebView with a new {@link BroadcastingInterstitialListener}, enables plugins,
            // and fires the impression tracker.
            final WebViewCacheService.Config config =
                    WebViewCacheService.popWebViewConfig(broadcastIdentifier);
            if (config != null && config.getWebView() instanceof HtmlInterstitialWebView) {
                mHtmlInterstitialWebView = (HtmlInterstitialWebView) config.getWebView();
                mHtmlInterstitialWebView.init(new BroadcastingInterstitialListener(), isScrollable,
                        redirectUrl, clickthroughUrl, mAdReport != null ? mAdReport.getDspCreativeId(): null, clickAction);
                mHtmlInterstitialWebView.enablePlugins(true);
                mHtmlInterstitialWebView.loadUrl(WEB_VIEW_DID_APPEAR.getUrl());

                mExternalViewabilitySessionManager = config.getViewabilityManager();

                return mHtmlInterstitialWebView;
            }
        }

        CDAdLog.d("WebView cache miss. Recreating the WebView.");
        mHtmlInterstitialWebView = HtmlInterstitialWebViewFactory.create(getApplicationContext(),
                mAdReport, new BroadcastingInterstitialListener(), isScrollable, redirectUrl, clickthroughUrl, clickAction);
        
        mExternalViewabilitySessionManager = new ExternalViewabilitySessionManager(this);
        mExternalViewabilitySessionManager.createDisplaySession(this, mHtmlInterstitialWebView, true);
        mHtmlInterstitialWebView.loadHtmlResponse(htmlResponse);
        return mHtmlInterstitialWebView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mExternalViewabilitySessionManager != null) {
            mExternalViewabilitySessionManager.startDeferredDisplaySession(this);
        }
        if (getBroadcastIdentifier() != null) {
            BaseBroadcastReceiver.broadcastAction(this, getBroadcastIdentifier(), ACTION_INTERSTITIAL_SHOW);
        }
    }

    @Override
    protected void onDestroy() {
        if (mExternalViewabilitySessionManager != null) {
            mExternalViewabilitySessionManager.endDisplaySession();
            mExternalViewabilitySessionManager = null;
        }
        if (mHtmlInterstitialWebView != null) {
            mHtmlInterstitialWebView.loadUrl(WEB_VIEW_DID_CLOSE.getUrl());
            mHtmlInterstitialWebView.destroy();
        }
        if (getBroadcastIdentifier() != null) {
            BaseBroadcastReceiver.broadcastAction(getApplicationContext(), getBroadcastIdentifier(), ACTION_INTERSTITIAL_DISMISS);
        }
        super.onDestroy();
    }

    class BroadcastingInterstitialListener implements CustomEventInterstitialListener {
        @Override
        public void onInterstitialLoaded() {
            if (mHtmlInterstitialWebView != null) {
                mHtmlInterstitialWebView.loadUrl(WEB_VIEW_DID_APPEAR.getUrl());
            }
        }

        @Override
        public void onInterstitialFailed(CDAdErrorCode errorCode) {
            if (getBroadcastIdentifier()!=null)
                BaseBroadcastReceiver.broadcastAction(CDAdActivity.this, getBroadcastIdentifier(), ACTION_INTERSTITIAL_FAIL);
            finish();
        }

        @Override
        public void onInterstitialShown() {
        }

        @Override
        public void onInterstitialClicked() {
            if (getBroadcastIdentifier()!=null)
                BaseBroadcastReceiver.broadcastAction(CDAdActivity.this, getBroadcastIdentifier(), ACTION_INTERSTITIAL_CLICK);
        }

        @Override
        public void onLeaveApplication() {
        }

        @Override
        public void onInterstitialDismissed() {
        }

        @Override
        public void onInterstitialVideoEnded() {

        }
    }
}
