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
import android.view.WindowManager;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.chalkdigital.ads.CDAdErrorCode;
import com.chalkdigital.ads.BaseBroadcastReceiver;
import com.chalkdigital.ads.BaseWebView;
import com.chalkdigital.ads.Interstitial;
import com.chalkdigital.ads.WebViewCacheService;
import com.chalkdigital.common.AdReport;
import com.chalkdigital.common.DataKeys;
import com.chalkdigital.common.ExternalViewabilitySessionManager;
import com.chalkdigital.common.Preconditions;
import com.chalkdigital.common.VisibleForTesting;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.Utils;
import com.chalkdigital.interstitial.ads.CustomEventInterstitial.CustomEventInterstitialListener;
import com.chalkdigital.mraid.MraidBridge;
import com.chalkdigital.mraid.MraidController;
import com.chalkdigital.mraid.MraidController.MraidListener;
import com.chalkdigital.mraid.MraidController.UseCustomCloseListener;
import com.chalkdigital.mraid.MraidWebViewClient;
import com.chalkdigital.mraid.MraidWebViewDebugListener;
import com.chalkdigital.mraid.PlacementType;
import com.chalkdigital.network.response.TypeParser;
import com.chalkdigital.network.retrofit.CDAdParams;

import java.util.HashMap;

import static com.chalkdigital.ads.HtmlWebViewClient.CDADS_FAIL_LOAD;
import static com.chalkdigital.common.DataKeys.AD_EVENTS_KEY;
import static com.chalkdigital.common.DataKeys.AD_REPORT_KEY;
import static com.chalkdigital.common.DataKeys.AD_SERVER_EXTRAS_KEY;
import static com.chalkdigital.common.DataKeys.BROADCAST_IDENTIFIER_KEY;
import static com.chalkdigital.common.DataKeys.CLICK_ACTION;
import static com.chalkdigital.common.DataKeys.FORCE_CLOSE_BUTTON;
import static com.chalkdigital.common.DataKeys.HTML_RESPONSE_BODY_KEY;
import static com.chalkdigital.common.IntentActions.ACTION_INTERSTITIAL_CLICK;
import static com.chalkdigital.common.IntentActions.ACTION_INTERSTITIAL_DISMISS;
import static com.chalkdigital.common.IntentActions.ACTION_INTERSTITIAL_FAIL;
import static com.chalkdigital.common.IntentActions.ACTION_INTERSTITIAL_SHOW;
import static com.chalkdigital.common.util.JavaScriptWebViewCallbacks.WEB_VIEW_DID_APPEAR;
import static com.chalkdigital.common.util.JavaScriptWebViewCallbacks.WEB_VIEW_DID_CLOSE;

public class MraidActivity extends BaseInterstitialActivity {
    @Nullable
    private MraidController mMraidController;
    @Nullable
    private MraidWebViewDebugListener mDebugListener;
    @Nullable
    private ExternalViewabilitySessionManager mExternalViewabilitySessionManager;

    public static void preRenderHtml(@NonNull final Interstitial mraidInterstitial,
            @NonNull final Context context,
            @NonNull final CustomEventInterstitialListener customEventInterstitialListener,
            @Nullable final String htmlData,
            @NonNull final Long broadcastIdentifier,
            @NonNull HashMap<String, String> serverExtras) {
        Preconditions.checkNotNull(mraidInterstitial);
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(customEventInterstitialListener);
        Preconditions.checkNotNull(broadcastIdentifier);

        preRenderHtml(mraidInterstitial, context, customEventInterstitialListener, htmlData,
                new MraidBridge.MraidWebView(context), broadcastIdentifier, serverExtras);
    }

    @VisibleForTesting
    static void preRenderHtml(@NonNull final Interstitial mraidInterstitial,
            @NonNull final Context context,
            @NonNull final CustomEventInterstitialListener customEventInterstitialListener,
            @Nullable final String htmlData,
            @NonNull final BaseWebView mraidWebView,
            @NonNull final Long broadcastIdentifier,
            @NonNull HashMap<String, String> serverExtras) {
        Preconditions.checkNotNull(mraidInterstitial);
        Preconditions.checkNotNull(customEventInterstitialListener);
        Preconditions.checkNotNull(mraidWebView);
        Preconditions.checkNotNull(broadcastIdentifier);

        mraidWebView.enablePlugins(false);
        mraidWebView.enableJavascriptCaching();
        mraidWebView.setInitialScale(Utils.getInterstitialScale(context, serverExtras.get(DataKeys.WIDTH), serverExtras.get(DataKeys.HEIGHT), serverExtras.get(DataKeys.STRETCH), TypeParser.parseInteger(serverExtras.get(DataKeys.CREATIVE_ORIENTATION_KEY), ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)));

        mraidWebView.setWebViewClient(new MraidWebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (CDADS_FAIL_LOAD.equals(url)) {
                    customEventInterstitialListener.onInterstitialFailed(
                            CDAdErrorCode.MRAID_LOAD_ERROR);
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
                            CDAdErrorCode.MRAID_LOAD_ERROR);
                }
            }
        });

        final Context context1 = mraidWebView.getContext();
        final ExternalViewabilitySessionManager externalViewabilitySessionManager =
                new ExternalViewabilitySessionManager(context1);
        externalViewabilitySessionManager.createDisplaySession(context1, mraidWebView, true);

        mraidWebView.loadDataWithBaseURL(CDAdParams.webViewBaseUrl,
                htmlData, "text/html", "UTF-8", null);
        WebViewCacheService.storeWebViewConfig(broadcastIdentifier, mraidInterstitial, mraidWebView, externalViewabilitySessionManager);
    }

    public static void start(@NonNull Context context, @Nullable AdReport adreport, @Nullable String htmlData, long broadcastIdentifier, @NonNull HashMap<String, String> serverExtras) {
        Intent intent = createIntent(context, adreport, htmlData, broadcastIdentifier, serverExtras);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException exception) {
                        Utils.logStackTrace(exception);
            Log.d("MraidInterstitial", "SparkActivity.class not found. Did you declare SparkActivity in your manifest?");
        }
    }

    @VisibleForTesting
    protected static Intent createIntent(@NonNull Context context, @Nullable AdReport adReport,
                                         @Nullable String htmlData, long broadcastIdentifier, @NonNull HashMap<String, String> serverExtras) {
        Intent intent = new Intent(context, MraidActivity.class);
        intent.putExtra(HTML_RESPONSE_BODY_KEY, htmlData);
        intent.putExtra(BROADCAST_IDENTIFIER_KEY, broadcastIdentifier);
        intent.putExtra(AD_REPORT_KEY, adReport);
        intent.putExtra(CLICK_ACTION, serverExtras.get(CLICK_ACTION));
        intent.putExtra(AD_SERVER_EXTRAS_KEY, serverExtras);
        intent.putExtra(DataKeys.FORCE_CLOSE_BUTTON, (serverExtras.containsKey(FORCE_CLOSE_BUTTON) && serverExtras.get(FORCE_CLOSE_BUTTON).equals("true"))?true:false);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    public View getAdView(@NonNull final HashMap<String, String[]> events) {
        String htmlData = getIntent().getStringExtra(HTML_RESPONSE_BODY_KEY);
        if (htmlData == null) {
            CDAdLog.w("SparkActivity received a null HTML body. Finishing the activity.");
            finish();
            return new View(this);
        }

        mMraidController = new MraidController(
                this, mAdReport, PlacementType.INTERSTITIAL, getIntent().getStringExtra(CLICK_ACTION));

        mMraidController.setDebugListener(mDebugListener);
        mMraidController.setMraidListener(new MraidListener() {
            @Override
            public void onLoaded(View view) {
                // This is only done for the interstitial. Banners have a different mechanism
                // for tracking third party impressions.
                mMraidController.loadJavascript(WEB_VIEW_DID_APPEAR.getJavascript());
            }

            @Override
            public void onFailedToLoad() {
                CDAdLog.d("SparkActivity failed to load. Finishing the activity");
                if (getBroadcastIdentifier() != null) {
                    BaseBroadcastReceiver.broadcastAction(MraidActivity.this, getBroadcastIdentifier(),
                            ACTION_INTERSTITIAL_FAIL);
                }
                finish();
            }

            public void onClose() {
                mMraidController.loadJavascript(WEB_VIEW_DID_CLOSE.getJavascript());
                finish();
            }

            @Override
            public void onExpand() {
                // No-op. The interstitial is always expanded.
            }

            @Override
            public void onOpen() {
                if (getBroadcastIdentifier()!= null) {
                    BaseBroadcastReceiver.broadcastAction(MraidActivity.this, getBroadcastIdentifier(),
                            ACTION_INTERSTITIAL_CLICK);
                }
            }
        });

        // Needed because the Activity provides the close button, not the controller. This
        // gets called if the creative calls mraid.useCustomClose.
        mMraidController.setUseCustomCloseListener(new UseCustomCloseListener() {
            public void useCustomCloseChanged(boolean useCustomClose) {
                if (useCustomClose && !getIntent().getBooleanExtra(DataKeys.FORCE_CLOSE_BUTTON, false)) {
                    hideInterstitialCloseButton();
                } else {
                    showInterstitialCloseButton();
                }
            }
        });

        mMraidController.fillContent(getBroadcastIdentifier(), htmlData,
                new MraidController.MraidWebViewCacheListener() {
                    @Override
                    public void onReady(@NonNull final MraidBridge.MraidWebView webView,
                            @Nullable final ExternalViewabilitySessionManager viewabilityManager) {
                        if (viewabilityManager != null) {
                            mExternalViewabilitySessionManager = viewabilityManager;
                        } else {
                            mExternalViewabilitySessionManager = new ExternalViewabilitySessionManager(MraidActivity.this);
                            mExternalViewabilitySessionManager.createDisplaySession(MraidActivity.this, webView, true);
                        }
                    }
                });

        return mMraidController.getAdContainer();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mExternalViewabilitySessionManager != null) {
            mExternalViewabilitySessionManager.startDeferredDisplaySession(this);
        }
        if (getBroadcastIdentifier()!= null) {
            BaseBroadcastReceiver.broadcastAction(this, getBroadcastIdentifier(), ACTION_INTERSTITIAL_SHOW);
        }

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
    }

    @Override
    protected void onPause() {
        if (mMraidController != null) {
            mMraidController.pause(isFinishing());
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMraidController != null) {
            mMraidController.resume();
        }
    }

    @Override
    protected void onDestroy() {
        if (mExternalViewabilitySessionManager != null) {
            mExternalViewabilitySessionManager.endDisplaySession();
            mExternalViewabilitySessionManager = null;
        }
        if (mMraidController != null) {
            mMraidController.destroy();
        }

        if (getBroadcastIdentifier()!= null) {
            BaseBroadcastReceiver.broadcastAction(this, getBroadcastIdentifier(), ACTION_INTERSTITIAL_DISMISS);
        }
        super.onDestroy();
    }

    @VisibleForTesting
    public void setDebugListener(@Nullable MraidWebViewDebugListener debugListener) {
        mDebugListener = debugListener;
        if (mMraidController != null) {
            mMraidController.setDebugListener(debugListener);
        }
    }
}
