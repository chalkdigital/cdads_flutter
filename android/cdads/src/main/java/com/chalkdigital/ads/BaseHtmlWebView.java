package com.chalkdigital.ads;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;

import com.chalkdigital.common.AdReport;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.network.retrofit.CDAdParams;

import static com.chalkdigital.ads.ViewGestureDetector.UserClickListener;

public class BaseHtmlWebView extends BaseWebView implements UserClickListener {
    private final ViewGestureDetector mViewGestureDetector;
    private boolean mClicked;

    public BaseHtmlWebView(Context context, AdReport adReport) {
        super(context);

        disableScrollingAndZoom();
        getSettings().setJavaScriptEnabled(true);
        getSettings().setAllowFileAccessFromFileURLs(true); //Maybe you don't need this rule
        getSettings().setAllowUniversalAccessFromFileURLs(true);

        mViewGestureDetector = new ViewGestureDetector(context, this, adReport);
        mViewGestureDetector.setUserClickListener(this);

        enablePlugins(true);
        setBackgroundColor(Color.TRANSPARENT);
    }

    public void init(boolean isScrollable) {
        initializeOnTouchListener(isScrollable);
    }

    @Override
    public void loadUrl(@Nullable final String url) {
        if (url == null) {
            return;
        }

        if (url.startsWith("javascript:")) {
            super.loadUrl(url);
            return;
        }

        CDAdLog.d("Loading url: " + url);
    }

    @Override
    public void stopLoading() {
        if (mIsDestroyed) {
            CDAdLog.w(BaseHtmlWebView.class.getSimpleName() + "#stopLoading() called after destroy()");
            return;
        }

        final WebSettings webSettings = getSettings();
        if (webSettings == null) {
            CDAdLog.w(BaseHtmlWebView.class.getSimpleName() + "#getSettings() returned null");
            return;
        }

        webSettings.setJavaScriptEnabled(false);
        super.stopLoading();
        webSettings.setJavaScriptEnabled(true);
    }

    private void disableScrollingAndZoom() {
        setHorizontalScrollBarEnabled(false);
        setHorizontalScrollbarOverlay(false);
        setVerticalScrollBarEnabled(false);
        setVerticalScrollbarOverlay(false);
        getSettings().setSupportZoom(false);
    }

    public void loadHtmlResponse(String htmlResponse) {
        loadDataWithBaseURL(CDAdParams.webViewBaseUrl, htmlResponse,
                "text/html", "utf-8", null);
    }

    void initializeOnTouchListener(final boolean isScrollable) {
        setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                mViewGestureDetector.sendTouchEvent(event);
                return false;
            }
        });
    }

    @Override
    public void onUserClick() {
        mClicked = true;
    }

    @Override
    public void onResetUserClick() {
        mClicked = false;
    }

    @Override
    public boolean wasClicked() {
        return mClicked;
    }
}
