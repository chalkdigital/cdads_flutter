package com.chalkdigital.common;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.chalkdigital.ads.BaseWebView;
import com.chalkdigital.ads.util.WebViews;
import com.chalkdigital.common.event.CDAdEvents;
import com.chalkdigital.common.event.Event;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.chalkdigital.common.event.BaseEvent.Category;
import static com.chalkdigital.common.event.BaseEvent.Name;
import static com.chalkdigital.common.event.BaseEvent.SamplingRate;
import static com.chalkdigital.common.util.Drawables.BACKGROUND;
import static com.chalkdigital.common.util.Drawables.CLOSE;
import static com.chalkdigital.common.util.Drawables.REFRESH;
import static com.chalkdigital.common.util.Drawables.UNLEFT_ARROW;
import static com.chalkdigital.common.util.Drawables.UNRIGHT_ARROW;

public class CDAdBrowser extends Activity {
    public static final String DESTINATION_URL_KEY = "URL";
    public static final String DSP_CREATIVE_ID = "cdads-dsp-creative-id";
    public static final int CDAds_BROWSER_REQUEST_CODE = 1;
    private static final int INNER_LAYOUT_ID = 1;

    private WebView mWebView;
    private ImageButton mBackButton;
    private ImageButton mForwardButton;
    private ImageButton mRefreshButton;
    private ImageButton mCloseButton;

    private DoubleTimeTracker dwellTimeTracker;
    private String mDspCreativeId;

    @NonNull
    public ImageButton getBackButton() {
        return mBackButton;
    }

    @NonNull
    public ImageButton getCloseButton() {
        return mCloseButton;
    }

    @NonNull
    public ImageButton getForwardButton() {
        return mForwardButton;
    }

    @NonNull
    public ImageButton getRefreshButton() {
        return mRefreshButton;
    }

    @NonNull
    public WebView getWebView() {
        return mWebView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setResult(Activity.RESULT_OK);

        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);

        setContentView(getCDAdBrowserView());

        dwellTimeTracker = new DoubleTimeTracker();

        initializeWebView();
        initializeButtons();
        enableCookies();
    }

    private void initializeWebView() {
        WebSettings webSettings = mWebView.getSettings();

        webSettings.setJavaScriptEnabled(true);

        /**
         * Pinch to zoom is apparently not enabled by default on all devices, so
         * declare zoom support explicitly.
         * https://stackoverflow.com/questions/5125851/enable-disable-zoom-in-android-webview
         */
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setUseWideViewPort(true);

        mDspCreativeId = getIntent().getStringExtra(DSP_CREATIVE_ID);

        mWebView.loadUrl(getIntent().getStringExtra(DESTINATION_URL_KEY));

        mWebView.setWebViewClient(new BrowserWebViewClient(this));

        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView webView, int progress) {
                setTitle("Loading...");
                setProgress(progress * 100);
                if (progress == 100) {
                    setTitle(webView.getUrl());
                }
            }
        });
    }

    private void initializeButtons() {
        mBackButton.setBackgroundColor(Color.TRANSPARENT);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mWebView.canGoBack()) {
                    mWebView.goBack();
                }
            }
        });

        mForwardButton.setBackgroundColor(Color.TRANSPARENT);
        mForwardButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mWebView.canGoForward()) {
                    mWebView.goForward();
                }
            }
        });

        mRefreshButton.setBackgroundColor(Color.TRANSPARENT);
        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mWebView.reload();
            }
        });

        mCloseButton.setBackgroundColor(Color.TRANSPARENT);
        mCloseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CDAdBrowser.this.finish();
            }
        });
    }

    private void enableCookies() {
        CookieSyncManager.createInstance(this);
        CookieSyncManager.getInstance().startSync();
    }

    @Override
    protected void onPause() {
        super.onPause();
        CookieSyncManager.getInstance().stopSync();
        WebViews.onPause(mWebView, isFinishing());
        // Pause dwell time counting.
        dwellTimeTracker.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CookieSyncManager.getInstance().startSync();
        mWebView.onResume();

        dwellTimeTracker.start();
    }

    @Override
    public void finish() {
        // ZoomButtonController adds buttons to the window's decorview. If they're still visible
        // when finish() is called, they need to be removed or a Window object will be leaked.
        ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
        decorView.removeAllViews();
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.destroy();
        mWebView = null;
        // Log dwell time value.
        CDAdEvents.log(new Event.Builder(Name.AD_DWELL_TIME,
                Category.AD_INTERACTIONS,
                SamplingRate.AD_INTERACTIONS.getSamplingRate())
                .withDspCreativeId(mDspCreativeId)
                .withPerformanceDurationMs(dwellTimeTracker.getInterval())
                .build());
    }

    @SuppressWarnings("ResourceType") // Using XML resources causes issues in Unity
    private View getCDAdBrowserView() {
        LinearLayout cdAdBrowserView = new LinearLayout(this);
        LinearLayout.LayoutParams browserLayoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        cdAdBrowserView.setLayoutParams(browserLayoutParams);
        cdAdBrowserView.setOrientation(LinearLayout.VERTICAL);

        RelativeLayout outerLayout = new RelativeLayout(this);
        LinearLayout.LayoutParams outerLayoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        outerLayout.setLayoutParams(outerLayoutParams);
        cdAdBrowserView.addView(outerLayout);

        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setId(INNER_LAYOUT_ID);
        RelativeLayout.LayoutParams innerLayoutParams = new RelativeLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        innerLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        innerLayout.setLayoutParams(innerLayoutParams);
        innerLayout.setBackgroundDrawable(BACKGROUND.createDrawable(this));
        outerLayout.addView(innerLayout);

        mBackButton = getButton(UNLEFT_ARROW.createDrawable(this));
        mForwardButton = getButton(UNRIGHT_ARROW.createDrawable(this));
        mRefreshButton = getButton(REFRESH.createDrawable(this));
        mCloseButton = getButton(CLOSE.createDrawable(this));

        innerLayout.addView(mBackButton);
        innerLayout.addView(mForwardButton);
        innerLayout.addView(mRefreshButton);
        innerLayout.addView(mCloseButton);

        mWebView = new BaseWebView(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.ABOVE, INNER_LAYOUT_ID);
        mWebView.setLayoutParams(layoutParams);
        outerLayout.addView(mWebView);

        return cdAdBrowserView;
    }

    private ImageButton getButton(final Drawable drawable) {
        ImageButton imageButton = new ImageButton(this);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, 1f);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        imageButton.setLayoutParams(layoutParams);

        imageButton.setImageDrawable(drawable);

        return imageButton;
    }

    @Deprecated
    @VisibleForTesting
    void setWebView(WebView webView) {
        mWebView = webView;
    }
}
