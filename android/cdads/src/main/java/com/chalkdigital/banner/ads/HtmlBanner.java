package com.chalkdigital.banner.ads;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;

import com.chalkdigital.ads.AdViewController;
import com.chalkdigital.banner.ads.factories.HtmlBannerWebViewFactory;
import com.chalkdigital.common.AdReport;
import com.chalkdigital.common.CDAdSize;
import com.chalkdigital.common.DataKeys;
import com.chalkdigital.common.ExternalViewabilitySessionManager;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.mediation.CDMediationAdRequest;
import com.chalkdigital.mediation.MediationConstants;
import com.chalkdigital.network.response.TypeParser;

import java.util.HashMap;
import java.util.Map;

import static com.chalkdigital.ads.CDAdErrorCode.NETWORK_INVALID_STATE;
import static com.chalkdigital.common.util.JavaScriptWebViewCallbacks.WEB_VIEW_DID_APPEAR;

public class HtmlBanner extends CustomEventBanner {
    private HtmlBannerWebView mHtmlBannerWebView;
    private Map<String, Object> mParams;
    @Nullable
    private ExternalViewabilitySessionManager mExternalViewabilitySessionManager;

    @Override
    protected void loadBanner(
            Context context,
            CustomEventBannerListener customEventBannerListener, Map<String, Object> params, final CDAdSize adSize, final CDMediationAdRequest mediationAdRequest,
            Map<String, String> serverExtras) {

        mParams = params;
        String htmlData;
        String redirectUrl;
        String clickthroughUrl;
        Boolean isScrollable;
        AdReport adReport;
        if (extrasAreValid(serverExtras)) {
            htmlData = serverExtras.get(DataKeys.HTML_RESPONSE_BODY_KEY);
            redirectUrl = null;
            clickthroughUrl = null;
            isScrollable = Boolean.valueOf(serverExtras.get(DataKeys.SCROLLABLE_KEY));
        } else {
            customEventBannerListener.onBannerFailed(NETWORK_INVALID_STATE);
            return;
        }

        mHtmlBannerWebView = HtmlBannerWebViewFactory.create(context, null, customEventBannerListener, isScrollable, redirectUrl, clickthroughUrl, serverExtras.get(DataKeys.CLICK_ACTION));
        AdViewController.setShouldHonorServerDimensions(mHtmlBannerWebView);

        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            mExternalViewabilitySessionManager = new ExternalViewabilitySessionManager(activity);
            mExternalViewabilitySessionManager.createDisplaySession(activity, mHtmlBannerWebView);
        } else {
            CDAdLog.d("Unable to start viewability session for HTML banner: Context provided was not an Activity.");
        }

        mHtmlBannerWebView.loadHtmlResponse(htmlData);
    }

    @Override
    protected void onInvalidate() {
        if (mExternalViewabilitySessionManager != null) {
            mExternalViewabilitySessionManager.endDisplaySession();
            mExternalViewabilitySessionManager = null;
        }

        if (mHtmlBannerWebView != null) {
            mHtmlBannerWebView.destroy();
        }
    }

    @Override
    protected void trackMpxAndThirdPartyImpressions() {
        mHtmlBannerWebView.loadUrl(WEB_VIEW_DID_APPEAR.getUrl());
    }

    private boolean extrasAreValid(Map<String, String> serverExtras) {
        return serverExtras.containsKey(DataKeys.HTML_RESPONSE_BODY_KEY);
    }

    @Override
    protected String getId() {
        if (mParams!=null && mParams.keySet().contains(MediationConstants.SDK_ID))
            return TypeParser.parseString(mParams.get(MediationConstants.SDK_ID), "");
        return "";
    }
}
