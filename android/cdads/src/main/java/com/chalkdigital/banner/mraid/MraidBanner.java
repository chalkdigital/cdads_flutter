package com.chalkdigital.banner.mraid;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;

import com.chalkdigital.ads.AdViewController;
import com.chalkdigital.ads.factories.MraidControllerFactory;
import com.chalkdigital.banner.ads.CustomEventBanner;
import com.chalkdigital.common.CDAdConstants;
import com.chalkdigital.common.CDAdSize;
import com.chalkdigital.common.ExternalViewabilitySessionManager;
import com.chalkdigital.common.VisibleForTesting;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.Utils;
import com.chalkdigital.mediation.CDMediationAdRequest;
import com.chalkdigital.mediation.MediationConstants;
import com.chalkdigital.mraid.MraidBridge;
import com.chalkdigital.mraid.MraidController;
import com.chalkdigital.mraid.MraidController.MraidListener;
import com.chalkdigital.mraid.MraidWebViewClient;
import com.chalkdigital.mraid.MraidWebViewDebugListener;
import com.chalkdigital.mraid.PlacementType;
import com.chalkdigital.network.response.TypeParser;

import java.util.HashMap;
import java.util.Map;

import static com.chalkdigital.ads.CDAdErrorCode.MRAID_LOAD_ERROR;
import static com.chalkdigital.common.DataKeys.CLICK_ACTION;
import static com.chalkdigital.common.DataKeys.HTML_RESPONSE_BODY_KEY;
import static com.chalkdigital.common.util.JavaScriptWebViewCallbacks.WEB_VIEW_DID_APPEAR;

class MraidBanner extends CustomEventBanner {
    @Nullable
    private MraidController mMraidController;
    @Nullable
    private CustomEventBannerListener mBannerListener;
    @Nullable
    private MraidWebViewDebugListener mDebugListener;
    private Map<String, Object> mParams;
    @Nullable
    private ExternalViewabilitySessionManager mExternalViewabilitySessionManager;

    @Override
    protected void loadBanner(
            final Context context,
            CustomEventBannerListener customEventBannerListener, Map<String, Object> params, final CDAdSize adSize, final CDMediationAdRequest mediationAdRequest,
            Map<String, String> serverExtras) {

        mParams = params;
        mBannerListener = customEventBannerListener;

        String htmlData;
        if (extrasAreValid(serverExtras)) {
            htmlData = serverExtras.get(HTML_RESPONSE_BODY_KEY);
            htmlData = htmlData.contains(MraidWebViewClient.MRAID_JS)?htmlData:CDAdConstants.mraidJSPrefix +htmlData;
//            htmlData = "<script src=\"mraid.js\"></script>\n" +
//                    "<script type=\"text/javascript\">\n" +
//                    "function doReadyCheck()\n" +
//                    "{\n" +
//                    " if (mraid.getState() == 'loading')\n" +
//                    " {\n" +
//                    " //Wait until mraid library is ready and loaded so listen for ready event\n" +
//                    " mraid.addEventListener(\"ready\", mraidIsReady);\n" +
//                    " }\n" +
//                    " else\n" +
//                    " {\n" +
//                    " showMyAd();\n" +
//                    " }\n" +
//                    "}\n" +
//                    "function showMyAd()\n" +
//                    "{\n" +
//                    " //Add mraid related event listeners here e.g\n" +
//                    " //mraid.addEventListener(\"stateChange\", stateChangeHandler);\n" +
//                    " //mraid.addEventListener(\"sizeChange\", sizeChangeHandler);\n" +
//                    " //mraid.addEventListener(\"viewableChange\", viewableChangeHandler);\n" +
//                    " //mraid.addEventListener(\"error\", errorHandler);\n" +
//                    " //you can add the rest of the Javascript code related to your Ad here e.g\n" +
//                    " var adContainer = document.querySelector('#imageContainer');\n" +
//                    " addEvent(\"click\", adContainer , function (e) {\n" +
//                    " e.preventDefault();\n" +
//                    " mraid.expand();\n" +
//                    " return false;\n" +
//                    " });\n" +
//                    "}\n" +
//                    "function mraidIsReady()\n" +
//                    "{\n" +
//                    " //Remove the ready event listener\n" +
//                    " mraid.removeEventListener(\"ready\", mraidIsReady);\n" +
//                    "\n" +
//                    " showMyAd();\n" +
//                    "}\n" +
//                    "function addEvent(evnt, elem, func) {\n" +
//                    " if (elem.addEventListener) { // W3C DOM\n" +
//                    " elem.addEventListener(evnt, func, false);\n" +
//                    " } else if (elem.attachEvent) { // IE DOM\n" +
//                    " elem.attachEvent(\"on\" + evnt, func);\n" +
//                    " } else { // No much to do\n" +
//                    " elem[evnt] = func;\n" +
//                    " }\n" +
//                    "}\n" +
//                    "doReadyCheck();\n" +
//                    "</script>\n" +
//                    "<style>\n" +
//                    "    body {\n" +
//                    "        margin:0px;\n" +
//                    "    }\n" +
//                    "</style>\n" +
//                    "<div id=\"imageContainer\">\n" +
//                    " <img src=\"http://www.bannerinspiration.com/assets/uploads/r_27_f.jpg\" width=\"300\" height=\"250\"/>\n" +
//                    "</div>";

        } else {
            mBannerListener.onBannerFailed(MRAID_LOAD_ERROR);
            return;
        }

        try {
//            AdReport adReport = (AdReport) localExtras.get(AD_REPORT_KEY);
            mMraidController = MraidControllerFactory.create(
                    context, null, PlacementType.INLINE, serverExtras.get(CLICK_ACTION));
        } catch (ClassCastException e) {
                        Utils.logStackTrace(e);
            CDAdLog.w("MRAID banner creating failed:", e);
            mBannerListener.onBannerFailed(MRAID_LOAD_ERROR);
            return;
        }

        mDebugListener = new MraidWebViewDebugListener() {
            @Override
            public boolean onJsAlert(@NonNull String message, @NonNull JsResult result) {
                CDAdLog.i("JSAlert", message+" "+result.toString());
                return false;
            }

            @Override
            public boolean onConsoleMessage(@NonNull ConsoleMessage consoleMessage) {
                CDAdLog.i("consoleMessage", consoleMessage.message());
                return false;
            }
        };
        mMraidController.setDebugListener(mDebugListener);
        mMraidController.setMraidListener(new MraidListener() {
            @Override
            public void onLoaded(View view) {
                // Honoring the server dimensions forces the WebView to be the size of the banner
                AdViewController.setShouldHonorServerDimensions(view);
                mBannerListener.onBannerLoaded(view);
            }

            @Override
            public void onFailedToLoad() {
                mBannerListener.onBannerFailed(MRAID_LOAD_ERROR);
            }

            @Override
            public void onExpand() {
                mBannerListener.onBannerExpanded();
                mBannerListener.onBannerClicked();
            }

            @Override
            public void onOpen() {
                mBannerListener.onBannerClicked();
            }

            @Override
            public void onClose() {
                mBannerListener.onBannerCollapsed();
            }
        });

        mMraidController.fillContent(null, htmlData, new MraidController.MraidWebViewCacheListener() {
            @Override
            public void onReady(final @NonNull MraidBridge.MraidWebView webView,
                    final @Nullable ExternalViewabilitySessionManager viewabilityManager) {
                webView.getSettings().setJavaScriptEnabled(true);
                mExternalViewabilitySessionManager = new ExternalViewabilitySessionManager(context);
                mExternalViewabilitySessionManager.createDisplaySession(context, webView);
            }
        });
    }

    @Override
    protected void onInvalidate() {
        if (mExternalViewabilitySessionManager != null) {
            mExternalViewabilitySessionManager.endDisplaySession();
            mExternalViewabilitySessionManager = null;
        }
        if (mMraidController != null) {
            mMraidController.setMraidListener(null);
            mMraidController.destroy();
        }
    }

    @Override
    protected void trackMpxAndThirdPartyImpressions() {
        mMraidController.loadJavascript(WEB_VIEW_DID_APPEAR.getJavascript());
    }

    private boolean extrasAreValid(@NonNull final Map<String, String> serverExtras) {
        return serverExtras.containsKey(HTML_RESPONSE_BODY_KEY);
    }

    @VisibleForTesting
    public void setDebugListener(@Nullable MraidWebViewDebugListener debugListener) {
        mDebugListener = debugListener;
        if (mMraidController != null) {
            mMraidController.setDebugListener(debugListener);
        }
    }

    @Override
    protected String getId() {
        if (mParams!=null && mParams.keySet().contains(MediationConstants.SDK_ID))
            return TypeParser.parseString(mParams.get(MediationConstants.SDK_ID), "");
        return "";
    }
}
