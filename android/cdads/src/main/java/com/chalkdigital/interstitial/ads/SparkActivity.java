package com.chalkdigital.interstitial.ads;

import android.app.Activity;
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
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.chalkdigital.ads.CDAdErrorCode;
import com.chalkdigital.ads.BaseBroadcastReceiver;
import com.chalkdigital.ads.BaseWebView;
import com.chalkdigital.ads.Interstitial;
import com.chalkdigital.ads.WebViewCacheService;
import com.chalkdigital.common.AdReport;
import com.chalkdigital.common.DataKeys;
import com.chalkdigital.common.ExternalViewabilitySession;
import com.chalkdigital.common.ExternalViewabilitySessionManager;
import com.chalkdigital.common.Preconditions;
import com.chalkdigital.common.VisibleForTesting;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.Utils;
import com.chalkdigital.interstitial.ads.CustomEventInterstitial.CustomEventInterstitialListener;
import com.chalkdigital.network.response.TypeParser;
import com.chalkdigital.network.retrofit.CDAdParams;
import com.chalkdigital.spark.PlacementType;
import com.chalkdigital.spark.SparkBridge;
import com.chalkdigital.spark.SparkVideoViewController;
import com.chalkdigital.spark.SparkVideoViewController.SparkListener;
import com.chalkdigital.spark.SparkWebViewClient;
import com.chalkdigital.spark.SparkWebViewDebugListener;

import java.io.BufferedInputStream;
import java.util.HashMap;
import java.util.Map;

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
import static com.chalkdigital.common.IntentActions.ACTION_INTERSTITIAL_VIDEO_COMPLETE;
import static com.chalkdigital.common.util.JavaScriptWebViewCallbacks.WEB_VIEW_DID_CLOSE;

public class SparkActivity extends BaseInterstitialActivity {
    @Nullable
    private SparkVideoViewController mSparkVideoViewController;
    @Nullable
    private SparkWebViewDebugListener mDebugListener;
    @Nullable
    private ExternalViewabilitySessionManager mExternalViewabilitySessionManager;

    public static void preRenderHtml(@NonNull final Interstitial sparkInterstitial,
            @NonNull final Context context,
            @NonNull final CustomEventInterstitialListener customEventInterstitialListener,
            @Nullable final String htmlData,
            @NonNull final Long broadcastIdentifier,
            @NonNull HashMap<String, String> serverExtras) {
        Preconditions.checkNotNull(sparkInterstitial);
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(customEventInterstitialListener);
        Preconditions.checkNotNull(broadcastIdentifier);

        preRenderHtml(sparkInterstitial, context, customEventInterstitialListener, htmlData,
                new SparkBridge.SparkWebView(context), broadcastIdentifier, serverExtras);
    }

    @VisibleForTesting
    static void preRenderHtml(@NonNull final Interstitial sparkInterstitial,
            @NonNull final Context context,
            @NonNull final CustomEventInterstitialListener customEventInterstitialListener,
            @Nullable final String htmlData,
            @NonNull final BaseWebView sparkWebView,
            @NonNull final Long broadcastIdentifier,
            @NonNull HashMap<String, String> serverExtras) {
        Preconditions.checkNotNull(sparkInterstitial);
        Preconditions.checkNotNull(customEventInterstitialListener);
        Preconditions.checkNotNull(sparkWebView);
        Preconditions.checkNotNull(broadcastIdentifier);



        sparkWebView.enablePlugins(false);
        sparkWebView.enableJavascriptCaching();
        sparkWebView.setInitialScale(Utils.getInterstitialScale(context, serverExtras.get(DataKeys.WIDTH), serverExtras.get(DataKeys.HEIGHT), serverExtras.get(DataKeys.STRETCH), TypeParser.parseInteger(serverExtras.get(DataKeys.CREATIVE_ORIENTATION_KEY), ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)));
        sparkWebView.setWebViewClient(new SparkWebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (CDADS_FAIL_LOAD.equals(url)) {
                    customEventInterstitialListener.onInterstitialFailed(
                            CDAdErrorCode.SPARK_LOAD_ERROR);
                }
                return true;
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {

                if(request.getUrl().getPath()!=null){
                    if(request.getUrl().getPath().endsWith("/favicon.ico")) {
                        try {
                            return new WebResourceResponse("image/png", null, new BufferedInputStream(view.getContext().getAssets().open("empty_favicon.ico")));
                        } catch (Exception e) {

                        }
                    }
                    else if (request.getUrl().getPath().toLowerCase().contains(CDAdParams.webViewBaseUrl)){
                        try {
                            return new WebResourceResponse("text/html", null, null);
                        } catch (Exception e) {
                            CDAdLog.d("Failed to respond for : "+request.getUrl().toString());
                        }
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
                            CDAdErrorCode.SPARK_LOAD_ERROR);
                }
            }
        });

        final Context context1 = sparkWebView.getContext();
        final ExternalViewabilitySessionManager externalViewabilitySessionManager =
                new ExternalViewabilitySessionManager(context1);
        externalViewabilitySessionManager.createVideoSession((Activity) context, sparkWebView, serverExtras);
        Utils.setDesktopMode(sparkWebView, true);
//        sparkWebView.loadUrl("https://chalkiosapp.s3.amazonaws.com/vpaid/vpaid_2.0/android_web_page_test.html");
        sparkWebView.loadDataWithBaseURL(CDAdParams.webViewBaseUrl,
                htmlData, "text/html", "UTF-8", null);
        WebViewCacheService.storeWebViewConfig(broadcastIdentifier, sparkInterstitial, sparkWebView, externalViewabilitySessionManager);
    }

    public static void start(@NonNull Context context, @Nullable AdReport adreport, @Nullable String htmlData, long broadcastIdentifier, @NonNull HashMap<String, String> serverExtras) {
        Intent intent = createIntent(context, adreport, htmlData, broadcastIdentifier, serverExtras);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException exception) {
                        Utils.logStackTrace(exception);
            Log.d("SparkInterstitial", "SparkActivity.class not found. Did you declare SparkActivity in your manifest?");
        }
    }

    @VisibleForTesting
    protected static Intent createIntent(@NonNull Context context, @Nullable AdReport adReport,
                                         @Nullable String htmlData, long broadcastIdentifier, @NonNull HashMap<String, String> serverExtras) {
        Intent intent = new Intent(context, SparkActivity.class);
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

        mSparkVideoViewController = new SparkVideoViewController(
                this,null, PlacementType.INTERSTITIAL, null, getIntent().getStringExtra(CLICK_ACTION));

        mSparkVideoViewController.setDebugListener(mDebugListener);
        mSparkVideoViewController.setSparkListener(new SparkListener() {
            @Override
            public void onLoaded(View view) {
                // This is only done for the interstitial. Banners have a different mechanism
                // for tracking third party impressions.
//                mSparkVideoViewController.loadJavascript(WEB_VIEW_DID_APPEAR.getJavascript());
            }

            @Override
            public void onFailedToLoad() {
                CDAdLog.d("SparkActivity failed to load. Finishing the activity");
                if (getBroadcastIdentifier() != null) {
                    BaseBroadcastReceiver.broadcastAction(SparkActivity.this, getBroadcastIdentifier(),
                            ACTION_INTERSTITIAL_FAIL);
                }
                postViewabilityEvent(ExternalViewabilitySession.VideoEvent.RECORD_AD_ERROR, null, null, mSparkVideoViewController.isMuted()?0:1, null);
                finish();
            }

            public void onClose() {
                mSparkVideoViewController.loadJavascript(WEB_VIEW_DID_CLOSE.getJavascript());
                postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_STOPPED, null, null, mSparkVideoViewController.isMuted()?0:1, null);
                finish();
            }

            @Override
            public void onExpand() {
                postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_PLAYER_STATE_CHNAGED, null, null, mSparkVideoViewController.isMuted()?0:1, null);
            }

            @Override
            public void onOpen() {
                if (getBroadcastIdentifier()!= null) {
                    BaseBroadcastReceiver.broadcastAction(SparkActivity.this, getBroadcastIdentifier(),
                            ACTION_INTERSTITIAL_CLICK);
                }
                postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_CLICK_THRU, null, null, mSparkVideoViewController.isMuted()?0:1, null);
            }

            @Override
            public void onPlaying() {
                postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_PLAYING, null, null, mSparkVideoViewController.isMuted()?0:1, null);
            }

            @Override
            public void onPlay() {

            }

            @Override
            public void onError(String errorDesc) {
                if (getBroadcastIdentifier()!= null) {
                    BaseBroadcastReceiver.broadcastAction(SparkActivity.this, getBroadcastIdentifier(),
                            ACTION_INTERSTITIAL_FAIL, errorDesc);
                }
                postViewabilityEvent(ExternalViewabilitySession.VideoEvent.RECORD_AD_ERROR, null, null, mSparkVideoViewController.isMuted()?0:1, errorDesc);
            }

            @Override
            public void onStarted() {
                postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_STARTED, 1, null, mSparkVideoViewController.isMuted()?0:1, null);
            }

            @Override
            public void onReady() {
                postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_LOADED, null, null, mSparkVideoViewController.isMuted()?0:1, null);
            }

            @Override
            public void onEnded() {
                if (getBroadcastIdentifier()!= null) {
                    BaseBroadcastReceiver.broadcastAction(SparkActivity.this, getBroadcastIdentifier(),
                            ACTION_INTERSTITIAL_VIDEO_COMPLETE);
                }

                postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_COMPLETE, null, null, mSparkVideoViewController.isMuted()?0:1, null);
            }

            @Override
            public void onCancelled() {
                postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_SKIPPED, null, null, mSparkVideoViewController.isMuted()?0:1, null);

            }

            @Override
            public void onTimeout() {
                postViewabilityEvent(ExternalViewabilitySession.VideoEvent.RECORD_AD_ERROR, null, null, mSparkVideoViewController.isMuted()?0:1, "Ad Timeout");
            }

            @Override
            public void onPause() {
                postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_PAUSED, null, null, mSparkVideoViewController.isMuted()?0:1, null);
            }

            @Override
            public void onResume() {
                postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_RESUME, null, null, mSparkVideoViewController.isMuted()?0:1, null);
            }

            @Override
            public void onFirstQuatile() {
                postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_VIDEO_FIRST_QUARTILE, null, null, mSparkVideoViewController.isMuted()?0:1, null);
            }

            @Override
            public void onMidpoint() {
                postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_VIDEO_MIDPOINT, null, null, mSparkVideoViewController.isMuted()?0:1, null);
            }

            @Override
            public void onThirdQuartile() {
                postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_VIDEO_THIRD_QUARTILE, null, null, mSparkVideoViewController.isMuted()?0:1, null);
            }

            @Override
            public void onMuted() {
                postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_VOLUME_CHANGED, null, null, 0, null);
            }

            @Override
            public void onVolumeChanged() {
                postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_VOLUME_CHANGED, null, null, mSparkVideoViewController.isMuted()?0:1, null);
            }

            @Override
            public void onStateChanged(final Integer currentState) {
                postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_PLAYER_STATE_CHNAGED  , null, null, mSparkVideoViewController.isMuted()?0:1, null);
            }

            @Override
            public void onImpression() {
                postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_IMPRESSED, null, null, mSparkVideoViewController.isMuted()?0:1, null);
            }

            @Override
            public void onSkipped() {
                postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_SKIPPED, null, null, mSparkVideoViewController.isMuted()?0:1, null);
            }
        });

//        // Needed because the Activity provides the close button, not the controller. This
//        // gets called if the creative calls spark.useCustomClose.
//        mSparkVideoViewController.setUseCustomCloseListener(new UseCustomCloseListener() {
//            public void useCustomCloseChanged(boolean useCustomClose) {
//                if (useCustomClose && !getIntent().getBooleanExtra(DataKeys.FORCE_CLOSE_BUTTON, false)) {
//                    hideInterstitialCloseButton();
//                } else {
//                    showInterstitialCloseButton();
//                }
//            }
//        });

        mSparkVideoViewController.fillContent(getBroadcastIdentifier(), htmlData,
                new SparkVideoViewController.SparkWebViewCacheListener() {
                    @Override
                    public void onReady(@NonNull final SparkBridge.SparkWebView webView,
                            @Nullable final ExternalViewabilitySessionManager viewabilityManager) {
                        if (viewabilityManager != null) {
                            mExternalViewabilitySessionManager = viewabilityManager;
                        } else {
                            mExternalViewabilitySessionManager = new ExternalViewabilitySessionManager(SparkActivity.this);
                            mExternalViewabilitySessionManager.createVideoSession(SparkActivity.this, webView, (Map<String, String>) getIntent().getSerializableExtra(AD_SERVER_EXTRAS_KEY));
                        }
                    }
                });

        return mSparkVideoViewController.getAdContainer();
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
        if (mSparkVideoViewController != null) {
            mSparkVideoViewController.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSparkVideoViewController != null) {
            mSparkVideoViewController.resume();
        }
    }

    @Override
    protected void onDestroy() {
        if (mExternalViewabilitySessionManager != null) {
            mExternalViewabilitySessionManager.endDisplaySession();
            mExternalViewabilitySessionManager = null;
        }
        if (mSparkVideoViewController != null) {
            mSparkVideoViewController.destroy();
        }

        if (getBroadcastIdentifier()!= null) {
            BaseBroadcastReceiver.broadcastAction(this, getBroadcastIdentifier(), ACTION_INTERSTITIAL_DISMISS);
        }
        super.onDestroy();
    }

    @VisibleForTesting
    public void setDebugListener(@Nullable SparkWebViewDebugListener debugListener) {
        mDebugListener = debugListener;
        if (mSparkVideoViewController != null) {
            mSparkVideoViewController.setDebugListener(debugListener);
        }
    }

    private void postViewabilityEvent(ExternalViewabilitySession.VideoEvent event, Integer duration, Integer playHeadMillis, int volume, String message){
        if (mExternalViewabilitySessionManager != null) {
            mExternalViewabilitySessionManager.recordVideoEvent(event, duration, playHeadMillis, volume);
        }
    }
}
