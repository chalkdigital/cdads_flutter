package com.chalkdigital.spark;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.RequiresApi;

import com.chalkdigital.ads.BaseWebView;
import com.chalkdigital.BuildConfig;
import com.chalkdigital.ads.ViewGestureDetector;
import com.chalkdigital.common.CloseableLayout;
import com.chalkdigital.common.VisibleForTesting;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.Utils;
import com.chalkdigital.network.retrofit.CDAdParams;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class SparkBridge {

    public interface SparkBridgeListener {
        void onStarted();

        void onTimeout();

        void onReady();

        void onPlaying();

        void onPlay();

        void onPause();

        void onResume();

        void onFirstQuatile();

        void onMidpoint();

        void onThirdQuartile();

        void onError(String errorDesc);

        void onClose();

        void onEnded();

        void onMuted();

        void onVolumeChanged();

        void onStateChanged(Integer currentState);

        void onImpression();

        void onCancelled();

        void onSkipped();

        void onPageLoaded();

        void onPageFailedToLoad();

        void onVisibilityChanged(boolean isVisible);

        boolean onJsAlert(@NonNull String message, @NonNull JsResult result);

        boolean onConsoleMessage(@NonNull ConsoleMessage consoleMessage);

        void onSetOrientationProperties(boolean allowOrientationChange, SparkOrientation
                forceOrientation) throws SparkCommandException;

        void onOpen(URI uri);
    }

    @NonNull private final PlacementType mPlacementType;

    @NonNull private final SparkNativeCommandHandler mSparkNativeCommandHandler;

    @Nullable
    private SparkBridge.SparkBridgeListener mSparkBridgeListener;

    @Nullable private SparkBridge.SparkWebView mSparkWebView;

    private boolean mIsClicked;

    private boolean mHasLoaded;

    private String mClickUrl;

    SparkBridge(@NonNull PlacementType placementType) {
        this(placementType, new SparkNativeCommandHandler());
    }

    @VisibleForTesting
    SparkBridge(@NonNull PlacementType placementType, @NonNull SparkNativeCommandHandler sparkNativeCommandHandler) {
        mPlacementType = placementType;
        mSparkNativeCommandHandler = sparkNativeCommandHandler;
    }

    void setSparkBridgeListener(@Nullable SparkBridge.SparkBridgeListener listener) {
        mSparkBridgeListener = listener;
    }

    void attachView(@NonNull SparkBridge.SparkWebView sparkWebView) {
        mSparkWebView = sparkWebView;
        mSparkWebView.getSettings().setJavaScriptEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (mPlacementType == PlacementType.INTERSTITIAL) {
                sparkWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
            }
        }

        mSparkWebView.setScrollContainer(false);
        mSparkWebView.setVerticalScrollBarEnabled(false);
        mSparkWebView.setHorizontalScrollBarEnabled(false);
        mSparkWebView.setBackgroundColor(Color.BLACK);
        mSparkWebView.setWebViewClient(mSparkWebViewClient);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        }
//        Utils.setDesktopMode(mSparkWebView, true);
        mSparkWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(final WebView view, final String url, final String message,
                                     final JsResult result) {
                if (mSparkBridgeListener != null) {
                    return mSparkBridgeListener.onJsAlert(message, result);
                }
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public boolean onConsoleMessage(@NonNull final ConsoleMessage consoleMessage) {
                if (mSparkBridgeListener != null) {
                    return mSparkBridgeListener.onConsoleMessage(consoleMessage);
                }
                return super.onConsoleMessage(consoleMessage);
            }

            @Override
            public void onShowCustomView(final View view, final CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
            }
        });

        final ViewGestureDetector gestureDetector = new ViewGestureDetector(
                mSparkWebView.getContext(), mSparkWebView);
        gestureDetector.setUserClickListener(new ViewGestureDetector.UserClickListener() {
            @Override
            public void onUserClick() {
                mIsClicked = true;
            }

            @Override
            public void onResetUserClick() {
                mIsClicked = false;
            }

            @Override
            public boolean wasClicked() {
                return mIsClicked;
            }
        });

//        mSparkWebView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(final View v, final MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_UP){
////                    mIsClicked = true;
////                    simulateClickAction();
//                    return true;
//                }
//                return false;
//            }
//        });

        mSparkWebView.setVisibilityChangedListener(new SparkBridge.SparkWebView.OnVisibilityChangedListener() {
            @Override
            public void onVisibilityChanged(final boolean isVisible) {
                if (mSparkBridgeListener != null) {
                    mSparkBridgeListener.onVisibilityChanged(isVisible);
                }
            }
        });
    }

    void simulateClickAction(){
        if (mIsClicked && mClickUrl !=null) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(mClickUrl));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                if (mSparkWebView == null) {
                    CDAdLog.d("WebView was detached. Unable to load a URL");
                }
                mSparkWebView.getContext().getApplicationContext().startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Utils.logStackTrace(e);
                CDAdLog.d("No activity found to handle this URL " + mClickUrl);
            }
        }
        if (mSparkBridgeListener!=null){
            mSparkBridgeListener.onOpen(null);
        }
    }

    void detach() {
        mSparkWebView = null;
    }

    public void setContentHtml(@NonNull String htmlData) {
        if (mSparkWebView == null) {
            CDAdLog.d("SPARK bridge called setContentHtml before WebView was attached");
            return;
        }

        mHasLoaded = false;
        mSparkWebView.loadDataWithBaseURL(CDAdParams.webViewBaseUrl,
                htmlData, "text/html", "UTF-8", null);
    }

    public void setContentUrl(String url) {
        if (mSparkWebView == null) {
            CDAdLog.d("SPARK bridge called setContentHtml while WebView was not attached");
            return;
        }

        mHasLoaded = false;
        mSparkWebView.loadUrl(url);
    }

    void injectJavaScript(@NonNull String javascript) {
        if (mSparkWebView == null) {
            CDAdLog.d("Attempted to inject Javascript into SPARK WebView while was not "
                    + "attached:\n\t" + javascript);
            return;
        }
        CDAdLog.v("Injecting Javascript into SPARK WebView:\n\t" + javascript);
        mSparkWebView.loadUrl("javascript:" + javascript);
    }

    private void fireErrorEvent(@NonNull SparkJavascriptCommand command, @NonNull String message) {

    }

    private void fireNativeCommandCompleteEvent(@NonNull SparkJavascriptCommand command) {

    }

    public static class SparkWebView extends BaseWebView {
        public interface OnVisibilityChangedListener {
            void onVisibilityChanged(boolean isVisible);
        }

        @Nullable private SparkBridge.SparkWebView.OnVisibilityChangedListener mOnVisibilityChangedListener;

        private boolean mIsVisible;

        public SparkWebView(Context context) {
            super(context);
            mIsVisible = getVisibility() == View.VISIBLE;
        }

        void setVisibilityChangedListener(@Nullable SparkBridge.SparkWebView.OnVisibilityChangedListener listener) {
            mOnVisibilityChangedListener = listener;
        }

        @Override
        protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
            super.onVisibilityChanged(changedView, visibility);
            boolean newIsVisible = (visibility == View.VISIBLE);
            if (newIsVisible != mIsVisible) {
                mIsVisible = newIsVisible;
                if (mOnVisibilityChangedListener != null) {
                    mOnVisibilityChangedListener.onVisibilityChanged(mIsVisible);
                }
            }
        }

        public boolean isVisible() {
            return mIsVisible;
        }
    }

    private final WebViewClient mSparkWebViewClient = new SparkWebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return handleShouldOverrideUrl(url);
        }

        @androidx.annotation.Nullable
        @Nullable
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

        @RequiresApi(21)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return handleShouldOverrideUrl(request.getUrl().toString());
        }

        @Override
        public void onPageFinished(@NonNull WebView view, @NonNull String url) {
            handlePageFinished();
        }

        @Override
        public void onReceivedError(@NonNull WebView view, int errorCode,
                                    @NonNull String description, @NonNull String failingUrl) {
            CDAdLog.d("Error: " + description);
            if (mSparkBridgeListener != null) {
                mSparkBridgeListener.onError(description);
            }
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public void onLoadResource(final WebView view, final String url) {
            super.onLoadResource(view, url);
        }

        @RequiresApi(21)
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            CDAdLog.d("Error: " + request.getUrl().toString());
            CDAdLog.d("Error: " + error.getDescription());
            if (mSparkBridgeListener != null) {
//                mSparkBridgeListener.onError(error.getDescription().toString());
            }
            super.onReceivedError(view, request, error);
        }
    };

    @VisibleForTesting
    boolean handleShouldOverrideUrl(@NonNull  String url) {
        System.out.println("handleShouldOverrideUrl : "+ url);
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            Utils.logStackTrace(e);
            CDAdLog.w("Invalid SPARK URL: " + url);
            fireErrorEvent(SparkJavascriptCommand.UNSPECIFIED, "Spark command sent an invalid URL");
            return true;
        }

        // Note that scheme will be null when we are passed a relative Uri
        String scheme = uri.getScheme();
        String host = uri.getHost();

        if ("chalkdigital".equals(scheme)) {
            if ("failLoad".equals(host)) {
                if (mPlacementType == PlacementType.INLINE && mSparkBridgeListener != null) {
                    mSparkBridgeListener.onPageFailedToLoad();
                }
            }
            return true;
        }

        if ("imaadv".equals(scheme)) {
            Map<String, String> params = new HashMap<String, String>();
            try {
                Uri parsedUri = Uri.parse(url);
                for (String parameterName : parsedUri.getQueryParameterNames()) {
                    params.put(parameterName, parsedUri.getQueryParameter(parameterName));
                }
                SparkJavascriptCommand command = SparkJavascriptCommand.fromJavascriptString(host);
                runCommand(command, params);
                fireNativeCommandCompleteEvent(command);
            } catch (Exception exception) {
                Utils.logStackTrace(exception);
                //fireErrorEvent(command, exception.getMessage());
            }
            return true;
        }

        // This block handles all other URLs, including sms://, tel://,
        // clicking a hyperlink, or setting window.location directly in Javascript. It checks for
        // clicked in order to avoid interfering with automatically browser redirects.
        return false;
    }

    @VisibleForTesting
    private void handlePageFinished() {
        // This can happen a second time if the ad does something that changes the window location,
        // such as a redirect, changing window.location in Javascript, or programmatically clicking
        // a hyperlink. Note that the handleShouldOverrideUrl method skips doing its own
        // processing if the user hasn't clicked the ad.
        if (mHasLoaded) {
            return;
        }

        mHasLoaded = true;
        if (mSparkBridgeListener != null) {
            mSparkBridgeListener.onPageLoaded();
        }
    }

    @VisibleForTesting
    void runCommand(@NonNull final SparkJavascriptCommand command,
                    @NonNull Map<String, String> params)
            throws SparkCommandException {
        if (command.requiresClick(mPlacementType) && !mIsClicked) {
            throw new SparkCommandException("Cannot execute this command unless the user clicks");
        }

        if (mSparkBridgeListener == null) {
            throw new SparkCommandException("Invalid state to execute this command");
        }

        if (mSparkWebView == null) {
            throw new SparkCommandException("The current WebView is being destroyed");
        }

        switch (command) {
            case CLOSE:
                mSparkBridgeListener.onClose();
                break;
//            case PLAYING:
//                mSparkBridgeListener.onPlaying();
//                break;
//            case PLAY:
//                mSparkBridgeListener.onPlay();
//                break;
            case CLICK:
                mIsClicked = true;
                simulateClickAction();
                break;
            case LOADED:
                mSparkBridgeListener.onReady();
                break;
            case ERROR:
            {
                if(mSparkBridgeListener!=null){
                    try {
                        mSparkBridgeListener.onError(URLDecoder.decode(params.get("desc"), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        mSparkBridgeListener.onError("");
                    }
                }
            }
                break;
//            case CANCELLED:
//                mSparkBridgeListener.onCancelled();
//                break;
            case STARTED:
                try {
                    mClickUrl = params.get("clickthroughurl");
                }
                catch (Exception e){

                }
                mSparkBridgeListener.onStarted();
                break;
//            case TIMEOUT:
//                mSparkBridgeListener.onTimeout();
//                break;
            case MUTE:
                mSparkBridgeListener.onMuted();
                break;
            case FIRSTQUARTILE:
                mSparkBridgeListener.onFirstQuatile();
                break;
            case MIDPOINT:
                mSparkBridgeListener.onMidpoint();
                break;
            case THIRDQUARTILE:
                mSparkBridgeListener.onThirdQuartile();
                break;
            case VOLUMECHANGED:
                mSparkBridgeListener.onVolumeChanged();
                break;
            case PAUSED:
                mSparkBridgeListener.onPause();
                break;
            case RESUMED:
                mSparkBridgeListener.onResume();
                break;
            case COMPLETED:
                mSparkBridgeListener.onEnded();
                break;
            case IMPRESSION:
                mSparkBridgeListener.onImpression();
                break;
            case SKIPPED:
                mSparkBridgeListener.onSkipped();
                break;
            case UNSPECIFIED:
                CDAdLog.d(command.name());
//                throw new SparkCommandException("Unspecified SPARK Javascript command");
        }
    }

    private CloseableLayout.ClosePosition parseClosePosition(@NonNull String text,
                                                             @NonNull CloseableLayout.ClosePosition defaultValue)
            throws SparkCommandException {
        if (TextUtils.isEmpty(text)) {
            return defaultValue;
        }

        if (text.equals("top-left")) {
            return CloseableLayout.ClosePosition.TOP_LEFT;
        } else if (text.equals("top-right")) {
            return CloseableLayout.ClosePosition.TOP_RIGHT;
        } else if (text.equals("center")) {
            return CloseableLayout.ClosePosition.CENTER;
        } else if (text.equals("bottom-left")) {
            return CloseableLayout.ClosePosition.BOTTOM_LEFT;
        } else if (text.equals("bottom-right")) {
            return CloseableLayout.ClosePosition.BOTTOM_RIGHT;
        } else if (text.equals("top-center")) {
            return CloseableLayout.ClosePosition.TOP_CENTER;
        } else if (text.equals("bottom-center")) {
            return CloseableLayout.ClosePosition.BOTTOM_CENTER;
        } else {
            throw new SparkCommandException("Invalid close position: " + text);
        }
    }

    private int parseSize(@NonNull String text) throws SparkCommandException {
        int result;
        try {
            result = Integer.parseInt(text, 10);
        } catch (NumberFormatException e) {
            Utils.logStackTrace(e);
            throw new SparkCommandException("Invalid numeric parameter: " + text);
        }
        return result;
    }

    private SparkOrientation parseOrientation(String text) throws SparkCommandException {
        if ("portrait".equals(text)) {
            return SparkOrientation.PORTRAIT;
        } else if ("landscape".equals(text)) {
            return SparkOrientation.LANDSCAPE;
        } else if ("none".equals(text)) {
            return SparkOrientation.NONE;
        } else {
            throw new SparkCommandException("Invalid orientation: " + text);
        }
    }

    private int checkRange(int value, int min, int max) throws SparkCommandException {
        if (value < min || value > max) {
            throw new SparkCommandException("Integer parameter out of range: " + value);
        }
        return value;
    }

    private boolean parseBoolean(
            @Nullable String text, boolean defaultValue) throws SparkCommandException {
        if (text == null) {
            return defaultValue;
        }
        return parseBoolean(text);
    }

    private boolean parseBoolean(final String text) throws SparkCommandException {
        if ("true".equals(text)) {
            return true;
        } else if ("false".equals(text)) {
            return false;
        }
        throw new SparkCommandException("Invalid boolean parameter: " + text);
    }

    @NonNull
    private URI parseURI(@Nullable String encodedText, URI defaultValue)
            throws SparkCommandException {
        if (encodedText == null) {
            return defaultValue;
        }
        return parseURI(encodedText);
    }

    @NonNull
    private URI parseURI(@Nullable String encodedText) throws SparkCommandException {
        if (encodedText == null) {
            throw new SparkCommandException("Parameter cannot be null");
        }
        try {
            return new URI(encodedText);
        } catch (URISyntaxException e) {
            Utils.logStackTrace(e);
            throw new SparkCommandException("Invalid URL parameter: " + encodedText);
        }
    }

    void notifyViewability(boolean isViewable) {

    }

    void notifyPlacementType(PlacementType placementType) {

    }

    void notifyViewState(ViewState state) {

    }

    void notifySupports(boolean sms, boolean telephone, boolean calendar,
                        boolean storePicture, boolean inlineVideo) {
//        injectJavaScript("sparkbridge.setSupports("
//                + sms + "," + telephone + "," + calendar + "," + storePicture + "," + inlineVideo
//                + ")");
    }

    @NonNull
    private String stringifyRect(Rect rect) {
        return rect.left + "," + rect.top + "," + rect.width() + "," + rect.height();
    }

    @NonNull
    private String stringifySize(Rect rect) {
        return rect.width() + "," + rect.height();
    }

    public void notifyScreenMetrics(@NonNull final SparkScreenMetrics screenMetrics) {
//        injectJavaScript("sparkbridge.setScreenSize("
//                + stringifySize(screenMetrics.getScreenRectDips())
//                + ");sparkbridge.setMaxSize("
//                + stringifySize(screenMetrics.getRootViewRectDips())
//                + ");sparkbridge.setCurrentPosition("
//                + stringifyRect(screenMetrics.getCurrentAdRectDips())
//                + ");sparkbridge.setDefaultPosition("
//                + stringifyRect(screenMetrics.getDefaultAdRectDips())
//                + ")");
//        injectJavaScript("sparkbridge.notifySizeChangeEvent("
//                + stringifySize(screenMetrics.getCurrentAdRectDips())
//                + ")");
    }

    void notifyReady() {

    }

    boolean isClicked() {
        return mIsClicked;
    }

    boolean isVisible() {
        return mSparkWebView != null && mSparkWebView.isVisible();
    }

    boolean isAttached() {
        return mSparkWebView != null;
    }

    boolean isLoaded() {
        return mHasLoaded;
    }

    @VisibleForTesting
    SparkBridge.SparkWebView getSparkWebView() {
        return mSparkWebView;
    }

    @VisibleForTesting
    void setClicked(boolean clicked) {
        mIsClicked = clicked;
    }
}
