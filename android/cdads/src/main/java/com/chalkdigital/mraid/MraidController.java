package com.chalkdigital.mraid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.chalkdigital.ads.CDAdVideoPlayerActivity;
import com.chalkdigital.ads.WebViewCacheService;
import com.chalkdigital.ads.util.WebViews;
import com.chalkdigital.common.AdReport;
import com.chalkdigital.common.CloseableLayout;
import com.chalkdigital.common.CloseableLayout.ClosePosition;
import com.chalkdigital.common.CloseableLayout.OnCloseListener;
import com.chalkdigital.common.ExternalViewabilitySessionManager;
import com.chalkdigital.common.Preconditions;
import com.chalkdigital.common.UrlAction;
import com.chalkdigital.common.UrlHandler;
import com.chalkdigital.common.VisibleForTesting;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.DeviceUtils;
import com.chalkdigital.common.util.Dips;
import com.chalkdigital.common.util.Utils;
import com.chalkdigital.common.util.Views;
import com.chalkdigital.mraid.MraidBridge.MraidBridgeListener;
import com.chalkdigital.mraid.MraidBridge.MraidWebView;

import java.lang.ref.WeakReference;
import java.net.URI;

import static android.content.pm.ActivityInfo.CONFIG_ORIENTATION;
import static android.content.pm.ActivityInfo.CONFIG_SCREEN_SIZE;
import static com.chalkdigital.common.util.Utils.bitMaskContainsFlag;

public class MraidController {
    private final AdReport mAdReport;

    public interface MraidListener {
        void onLoaded(View view);
        void onFailedToLoad();
        void onExpand();
        void onOpen();
        void onClose();
    }

    public interface UseCustomCloseListener {
        public void useCustomCloseChanged(boolean useCustomClose);
    }

    public interface MraidWebViewCacheListener {
        void onReady(final MraidWebView webView, final ExternalViewabilitySessionManager viewabilityManager);
    }

    /**
     * Holds a weak reference to the activity if the context that is passed in is an activity.
     * While this field is never null, the reference could become null. This reference starts out
     * null if the passed-in context is not an activity.
     */
    @NonNull private final WeakReference<Activity> mWeakActivity;
    @NonNull private final Context mContext;
    @NonNull private final PlacementType mPlacementType;

    // An ad container, which contains the ad web view in default state, but is empty when expanded.
    @NonNull private final FrameLayout mDefaultAdContainer;

    // Ad ad container which contains the ad view in expanded state.
    @NonNull private final CloseableLayout mCloseableAdContainer;

    // Root view, where we'll add the expanded ad
    @Nullable private ViewGroup mRootView;

    // Helper classes for updating screen values
    @NonNull private final ScreenMetricsWaiter mScreenMetricsWaiter;
    @NonNull private final MraidScreenMetrics mScreenMetrics;

    // Current view state
    @NonNull private ViewState mViewState = ViewState.LOADING;

    // Listeners
    @Nullable private MraidListener mMraidListener;
    @Nullable private UseCustomCloseListener mOnCloseButtonListener;
    @Nullable private MraidWebViewDebugListener mDebugListener;

    // The WebView which will display the ad. "Two part" creatives, loaded via handleExpand(URL)
    // are shown in a separate web view
    @Nullable private MraidWebView mMraidWebView;
    @Nullable private MraidWebView mTwoPartWebView;

    // A bridge to handle all interactions with the WebView HTML and Javascript.
    @NonNull private final MraidBridge mMraidBridge;
    @NonNull private final MraidBridge mTwoPartBridge;

    @NonNull private OrientationBroadcastReceiver mOrientationBroadcastReceiver =
            new OrientationBroadcastReceiver();

    // Stores the requested orientation for the Activity to which this controller's view belongs.
    // This is needed to restore the Activity's requested orientation in the event that the view
    // itself requires an orientation lock.
    @Nullable private Integer mOriginalActivityOrientation;

    private boolean mAllowOrientationChange = true;
    private MraidOrientation mForceOrientation = MraidOrientation.NONE;

    private final MraidNativeCommandHandler mMraidNativeCommandHandler;

    private boolean mIsPaused;
    private String mClickAction;

    public MraidController(@NonNull Context context, @Nullable AdReport adReport,
            @NonNull PlacementType placementType, String clickAction) {
        this(context, adReport, placementType,
                new MraidBridge(adReport, placementType),
                new MraidBridge(adReport, PlacementType.INTERSTITIAL),
                new ScreenMetricsWaiter(), clickAction);
    }

    @VisibleForTesting
    MraidController(@NonNull Context context, @Nullable AdReport adReport,
            @NonNull PlacementType placementType,
            @NonNull MraidBridge bridge, @NonNull MraidBridge twoPartBridge,
            @NonNull ScreenMetricsWaiter screenMetricsWaiter, String clickAction) {
        mContext = context.getApplicationContext();
        mClickAction = clickAction;
        Preconditions.checkNotNull(mContext);
        mAdReport = adReport;
        if (context instanceof Activity) {
            mWeakActivity = new WeakReference<Activity>((Activity) context);
        } else {
            // Make sure mWeakActivity itself is never null, though the reference
            // it's pointing to could be null.
            mWeakActivity = new WeakReference<Activity>(null);
        }

        mPlacementType = placementType;
        mMraidBridge = bridge;
        mTwoPartBridge = twoPartBridge;
        mScreenMetricsWaiter = screenMetricsWaiter;

        mViewState = ViewState.LOADING;

        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        mScreenMetrics = new MraidScreenMetrics(mContext, displayMetrics.density);
        mDefaultAdContainer = new FrameLayout(mContext);
        mCloseableAdContainer = new CloseableLayout(mContext);
        mCloseableAdContainer.setOnCloseListener(new OnCloseListener() {
            @Override
            public void onClose() {
                handleClose();
            }
        });

        View dimmingView = new View(mContext);
        dimmingView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mCloseableAdContainer.addView(dimmingView,
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        mOrientationBroadcastReceiver.register(mContext);

        mMraidBridge.setMraidBridgeListener(mMraidBridgeListener);
        mTwoPartBridge.setMraidBridgeListener(mTwoPartBridgeListener);
        mMraidNativeCommandHandler = new MraidNativeCommandHandler();
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final MraidBridgeListener mMraidBridgeListener = new MraidBridgeListener() {
        @Override
        public void onPageLoaded() {
            handlePageLoad();
        }

        @Override
        public void onPageFailedToLoad() {
            if (mMraidListener != null) {
                mMraidListener.onFailedToLoad();
            }
        }

        @Override
        public void onVisibilityChanged(final boolean isVisible) {
            // The bridge only receives visibility events if there is no 2 part covering it
            if (!mTwoPartBridge.isAttached()) {
                mMraidBridge.notifyViewability(isVisible);
            }
        }

        @Override
        public boolean onJsAlert(@NonNull final String message, @NonNull final JsResult result) {
            return handleJsAlert(message, result);
        }

        @Override
        public boolean onConsoleMessage(@NonNull final ConsoleMessage consoleMessage) {
            return handleConsoleMessage(consoleMessage);
        }

        @Override
        public void onClose() {
            handleClose();
        }

        @Override
        public void onResize(final int width, final int height, final int offsetX,
                final int offsetY, @NonNull final ClosePosition closePosition,
                final boolean allowOffscreen) throws MraidCommandException {
            handleResize(width, height, offsetX, offsetY, closePosition, allowOffscreen);
        }

        public void onExpand(@Nullable final URI uri, final boolean shouldUseCustomClose)
                throws MraidCommandException {
            handleExpand(uri, shouldUseCustomClose);
        }

        @Override
        public void onUseCustomClose(final boolean shouldUseCustomClose) {
            handleCustomClose(shouldUseCustomClose);
        }

        @Override
        public void onSetOrientationProperties(final boolean allowOrientationChange,
                final MraidOrientation forceOrientation) throws MraidCommandException {
            handleSetOrientationProperties(allowOrientationChange, forceOrientation);
        }

        @Override
        public void onOpen(@NonNull final URI uri) {
            handleOpen(uri.toString());
        }

        @Override
        public void onPlayVideo(@NonNull final URI uri) {
            handleShowVideo(uri.toString());
        }
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final MraidBridgeListener mTwoPartBridgeListener = new MraidBridgeListener() {
        @Override
        public void onPageLoaded() {
            handleTwoPartPageLoad();
        }

        @Override
        public void onPageFailedToLoad() {
            // no-op for two-part expandables. An expandable failing to load should not trigger failover.
        }

        @Override
        public void onVisibilityChanged(final boolean isVisible) {
            // The original web view must see the 2-part bridges visibility
            mMraidBridge.notifyViewability(isVisible);
            mTwoPartBridge.notifyViewability(isVisible);
        }

        @Override
        public boolean onJsAlert(@NonNull final String message, @NonNull final JsResult result) {
            return handleJsAlert(message, result);
        }

        @Override
        public boolean onConsoleMessage(@NonNull final ConsoleMessage consoleMessage) {
            return handleConsoleMessage(consoleMessage);
        }

        @Override
        public void onResize(final int width, final int height, final int offsetX,
                final int offsetY, @NonNull final ClosePosition closePosition,
                final boolean allowOffscreen) throws MraidCommandException {
            throw new MraidCommandException("Not allowed to resize from an expanded state");
        }

        @Override
        public void onExpand(@Nullable final URI uri, final boolean shouldUseCustomClose) {
            // The MRAID spec dictates that this is ignored rather than firing an error
        }

        @Override
        public void onClose() {
            handleClose();
        }

        @Override
        public void onUseCustomClose(final boolean shouldUseCustomClose) {
            handleCustomClose(shouldUseCustomClose);
        }

        @Override
        public void onSetOrientationProperties(final boolean allowOrientationChange,
                final MraidOrientation forceOrientation) throws MraidCommandException {
            handleSetOrientationProperties(allowOrientationChange, forceOrientation);
        }

        @Override
        public void onOpen(final URI uri) {
            handleOpen(uri.toString());
        }

        @Override
        public void onPlayVideo(@NonNull final URI uri) {
            handleShowVideo(uri.toString());
        }
    };

    public void setMraidListener(@Nullable MraidListener mraidListener) {
        mMraidListener = mraidListener;
    }

    public void setUseCustomCloseListener(@Nullable UseCustomCloseListener listener) {
        mOnCloseButtonListener = listener;
    }

    public void setDebugListener(@Nullable MraidWebViewDebugListener debugListener) {
        mDebugListener = debugListener;
    }

    /**
     * Gets an MraidWebView and fills it with data. In the case that the MraidWebView is retrieved
     * from the cache, this also notifies that the ad has been loaded. If the broadcast identifier
     * is null or there is a cache miss, a new MraidWebView is created and is filled with htmlData.
     * @param broadcastIdentifier The unique identifier of an interstitial. This can be null,
     *                            especially when there is no interstitial.
     * @param htmlData            The HTML of the ad. This will only be loaded if a cached WebView
     *                            is not found.
     * @param listener            Optional listener that (if non-null) is notified when an
     *                            MraidWebView is loaded from the cache or created.
     */
    public void fillContent(@Nullable final Long broadcastIdentifier,
            @NonNull final String htmlData,
            @Nullable final MraidWebViewCacheListener listener) {
        Preconditions.checkNotNull(htmlData, "htmlData cannot be null");

        final boolean cacheHit = hydrateMraidWebView(broadcastIdentifier, listener);
        Preconditions.NoThrow.checkNotNull(mMraidWebView, "mMraidWebView cannot be null");

        mMraidBridge.attachView(mMraidWebView);
        mDefaultAdContainer.addView(mMraidWebView,
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        // If the WebView was retrieved from the cache, notify that the ad is already loaded.
        if (cacheHit) {
            handlePageLoad();
        } else {
            // Otherwise, load the content into the MraidWebView
            mMraidBridge.setContentHtml(htmlData);
        }
    }

    /**
     * Gets and sets the MraidWebView. Returns true if the MraidWebView was from the cache, and
     * false if a new one was created. If the broadcast identifier is {@code null}, then this
     * will always return false and create a new MraidWebView.
     *
     * @param broadcastIdentifier The unique identifier associated with the MraidWebView in the cache.
     * @param listener            Listener passed in from {@link #fillContent(Long, String, MraidWebViewCacheListener)}
     * @return {@code true} if there was a cache hit, {@code false} if a new MraidWebView was created.
     */
    private boolean hydrateMraidWebView(@Nullable final Long broadcastIdentifier,
            @Nullable final MraidWebViewCacheListener listener) {
        if (broadcastIdentifier != null) {
            final WebViewCacheService.Config config =
                    WebViewCacheService.popWebViewConfig(broadcastIdentifier);
            if (config != null && config.getWebView() instanceof MraidWebView) {
                mMraidWebView = (MraidWebView) config.getWebView();
                mMraidWebView.enablePlugins(true);

                if (listener != null) {
                    listener.onReady(mMraidWebView, config.getViewabilityManager());
                }
                return true;
            }
        }
        CDAdLog.d("WebView cache miss. Creating a new MraidWebView.");
        mMraidWebView = new MraidWebView(mContext);

        if (listener != null) {
            listener.onReady(mMraidWebView, null);
        }
        return false;
    }

    // onPageLoaded gets fired once the html is loaded into the webView.
    private int getDisplayRotation() {
        WindowManager wm = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getRotation();
    }

    @VisibleForTesting
    boolean handleConsoleMessage(@NonNull final ConsoleMessage consoleMessage) {
        //noinspection SimplifiableIfStatement
        if (mDebugListener != null) {
            return mDebugListener.onConsoleMessage(consoleMessage);
        }
        return true;
    }

    @VisibleForTesting
    boolean handleJsAlert(@NonNull final String message, @NonNull final JsResult result) {
        if (mDebugListener != null) {
            return mDebugListener.onJsAlert(message, result);
        }
        result.confirm();
        return true;
    }

    @VisibleForTesting
    static class ScreenMetricsWaiter {
        static class WaitRequest {
            @NonNull private final View[] mViews;
            @NonNull private final Handler mHandler;
            @Nullable private Runnable mSuccessRunnable;
            int mWaitCount;

            private WaitRequest(@NonNull Handler handler, @NonNull final View[] views) {
                mHandler = handler;
                mViews = views;
            }

            private void countDown() {
                mWaitCount--;
                if (mWaitCount == 0 && mSuccessRunnable != null) {
                    mSuccessRunnable.run();
                    mSuccessRunnable = null;
                }
            }

            private final Runnable mWaitingRunnable = new Runnable() {
                @Override
                public void run() {
                    for (final View view : mViews) {
                        // Immediately count down for any views that already have a size
                        if (view.getHeight() > 0 || view.getWidth() > 0) {
                            countDown();
                            continue;
                        }

                        // For views that didn't have a size, listen (once) for a preDraw. Note
                        // that this doesn't leak because the ViewTreeObserver gets detached when
                        // the view is no longer part of the view hierarchy.
                        view.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                            @Override
                            public boolean onPreDraw() {
                                view.getViewTreeObserver().removeOnPreDrawListener(this);
                                countDown();
                                return true;
                            }
                        });
                    }
                }
            };

            void start(@NonNull Runnable successRunnable) {
                mSuccessRunnable = successRunnable;
                mWaitCount = mViews.length;
                mHandler.post(mWaitingRunnable);
            }

            void cancel() {
                mHandler.removeCallbacks(mWaitingRunnable);
                mSuccessRunnable = null;
            }
        }

        @NonNull private final Handler mHandler = new Handler();
        @Nullable private WaitRequest mLastWaitRequest;

        WaitRequest waitFor(@NonNull View... views) {
            mLastWaitRequest = new WaitRequest(mHandler, views);
            return mLastWaitRequest;
        }

        void cancelLastRequest() {
            if (mLastWaitRequest != null) {
                mLastWaitRequest.cancel();
                mLastWaitRequest = null;
            }
        }
    }

    @Nullable
    public MraidWebView getCurrentWebView() {
        return mTwoPartBridge.isAttached() ? mTwoPartWebView : mMraidWebView;
    }

    private boolean isInlineVideoAvailable() {
        final Activity activity = mWeakActivity.get();
        //noinspection SimplifiableIfStatement
        if (activity == null || getCurrentWebView() == null) {
            return false;
        }

        return mMraidNativeCommandHandler.isInlineVideoAvailable(activity, getCurrentWebView());
    }

    @VisibleForTesting
    void handlePageLoad() {
        setViewState(ViewState.DEFAULT, new Runnable() {
            @Override
            public void run() {
                mMraidBridge.notifySupports(
                        mMraidNativeCommandHandler.isSmsAvailable(mContext),
                        mMraidNativeCommandHandler.isTelAvailable(mContext),
                        MraidNativeCommandHandler.isCalendarAvailable(mContext),
                        MraidNativeCommandHandler.isStorePictureSupported(mContext),
                        isInlineVideoAvailable());
                mMraidBridge.notifyPlacementType(mPlacementType);
                mMraidBridge.notifyViewability(mMraidBridge.isVisible());
                mMraidBridge.notifyReady();
            }
        });

        // Call onLoaded immediately. This causes the container to get added to the view hierarchy
        if (mMraidListener != null) {
            mMraidListener.onLoaded(mDefaultAdContainer);
        }
    }

    @VisibleForTesting
    void handleTwoPartPageLoad() {
        updateScreenMetricsAsync(new Runnable() {
            @Override
            public void run() {
                mTwoPartBridge.notifySupports(
                        mMraidNativeCommandHandler.isSmsAvailable(mContext),
                        mMraidNativeCommandHandler.isTelAvailable(mContext),
                        mMraidNativeCommandHandler.isCalendarAvailable(mContext),
                        mMraidNativeCommandHandler.isStorePictureSupported(mContext),
                        isInlineVideoAvailable());
                mTwoPartBridge.notifyViewState(mViewState);
                mTwoPartBridge.notifyPlacementType(mPlacementType);
                mTwoPartBridge.notifyViewability(mTwoPartBridge.isVisible());
                mTwoPartBridge.notifyReady();
            }
        });
    }

    /**
     * Updates screen metrics, calling the successRunnable once they are available. The
     * successRunnable will always be called asynchronously, ie on the next main thread loop.
     */
    private void updateScreenMetricsAsync(@Nullable final Runnable successRunnable) {
        // Don't allow multiple metrics wait requests at once
        mScreenMetricsWaiter.cancelLastRequest();

        // Determine which web view should be used for the current ad position
        final View currentWebView = getCurrentWebView();
        if (currentWebView == null) {
            return;
        }

        // Wait for the next draw pass on the default ad container and current web view
        mScreenMetricsWaiter.waitFor(mDefaultAdContainer, currentWebView).start(
                new Runnable() {
                    @Override
                    public void run() {
                        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
                        mScreenMetrics.setScreenSize(
                                displayMetrics.widthPixels, displayMetrics.heightPixels);

                        int[] location = new int[2];
                        View rootView = getRootView();
                        rootView.getLocationOnScreen(location);
                        mScreenMetrics.setRootViewPosition(location[0], location[1],
                                rootView.getWidth(),
                                rootView.getHeight());

                        mDefaultAdContainer.getLocationOnScreen(location);
                        mScreenMetrics.setDefaultAdPosition(location[0], location[1],
                                mDefaultAdContainer.getWidth(),
                                mDefaultAdContainer.getHeight());

                        currentWebView.getLocationOnScreen(location);
                        mScreenMetrics.setCurrentAdPosition(location[0], location[1],
                                currentWebView.getWidth(),
                                currentWebView.getHeight());

                        // Always notify both bridges of the new metrics
                        mMraidBridge.notifyScreenMetrics(mScreenMetrics);
                        if (mTwoPartBridge.isAttached()) {
                            mTwoPartBridge.notifyScreenMetrics(mScreenMetrics);
                        }

                        if (successRunnable != null) {
                            successRunnable.run();
                        }
                    }
                });
    }

    void handleOrientationChange(int currentRotation) {
        updateScreenMetricsAsync(null);
    }

    public void pause(boolean isFinishing) {
        mIsPaused = true;

        // This causes an inline video to pause if there is one playing
        if (mMraidWebView != null) {
            WebViews.onPause(mMraidWebView, isFinishing);
        }
        if (mTwoPartWebView != null) {
            WebViews.onPause(mTwoPartWebView, isFinishing);
        }
    }

    public void resume() {
        mIsPaused = false;

        // This causes an inline video to resume if it was playing previously
        if (mMraidWebView != null) {
            mMraidWebView.onResume();
        }
        if (mTwoPartWebView != null) {
            mTwoPartWebView.onResume();
        }
    }

    public void destroy() {
        mScreenMetricsWaiter.cancelLastRequest();

        try {
            mOrientationBroadcastReceiver.unregister();
        } catch (IllegalArgumentException e) {
                        Utils.logStackTrace(e);
            if (!e.getMessage().contains("Receiver not registered")) {
                throw e;
            } // Else ignore this exception.
        }

        // Pause the controller to make sure the video gets stopped.
        if (!mIsPaused) {
            pause(true);
        }

        // Remove the closeable ad container from the view hierarchy, if necessary
        Views.removeFromParent(mCloseableAdContainer);

        // Calling destroy eliminates a memory leak on Gingerbread devices
        mMraidBridge.detach();
        if (mMraidWebView != null) {
            mMraidWebView.destroy();
            mMraidWebView = null;
        }
        mTwoPartBridge.detach();
        if (mTwoPartWebView != null) {
            mTwoPartWebView.destroy();
            mTwoPartWebView = null;
        }
    }

    private void setViewState(@NonNull ViewState viewState) {
        setViewState(viewState, null);
    }

    private void setViewState(@NonNull ViewState viewState, @Nullable Runnable successRunnable) {
        // Make sure this is a valid transition.
        CDAdLog.d("MRAID state set to " + viewState);
        final ViewState previousViewState = mViewState;
        mViewState = viewState;
        mMraidBridge.notifyViewState(viewState);

        // Changing state notifies the two part view, but only if it's loaded
        if (mTwoPartBridge.isLoaded()) {
            mTwoPartBridge.notifyViewState(viewState);
        }

        if (mMraidListener != null) {
            if (viewState == ViewState.EXPANDED) {
                mMraidListener.onExpand();
            } else if (previousViewState == ViewState.EXPANDED && viewState == ViewState.DEFAULT) {
                mMraidListener.onClose();
            } else if (viewState == ViewState.HIDDEN) {
                mMraidListener.onClose();
            }
        }

        updateScreenMetricsAsync(successRunnable);
    }

    int clampInt(int min, int target, int max) {
        return Math.max(min, Math.min(target, max));
    }

    @VisibleForTesting
    void handleResize(final int widthDips, final int heightDips, final int offsetXDips,
            final int offsetYDips, @NonNull final ClosePosition closePosition,
            final boolean allowOffscreen)
            throws MraidCommandException {
        if (mMraidWebView == null) {
            throw new MraidCommandException("Unable to resize after the WebView is destroyed");
        }

        // The spec says that there is no effect calling resize from loaded or hidden, but that
        // calling it from expanded should raise an error.
        if (mViewState == ViewState.LOADING
                || mViewState == ViewState.HIDDEN) {
            return;
        } else if (mViewState == ViewState.EXPANDED) {
            throw new MraidCommandException("Not allowed to resize from an already expanded ad");
        }

        if (mPlacementType == PlacementType.INTERSTITIAL) {
            throw new MraidCommandException("Not allowed to resize from an interstitial ad");
        }

        // Translate coordinates to px and get the resize rect
        int width = Dips.dipsToIntPixels(widthDips, mContext);
        int height = Dips.dipsToIntPixels(heightDips, mContext);
        int offsetX = Dips.dipsToIntPixels(offsetXDips, mContext);
        int offsetY = Dips.dipsToIntPixels(offsetYDips, mContext);
        int left = mScreenMetrics.getDefaultAdRect().left + offsetX;
        int top = mScreenMetrics.getDefaultAdRect().top + offsetY;
        Rect resizeRect = new Rect(left, top, left + width, top + height);

        if (!allowOffscreen) {
            // Require the entire ad to be on-screen.
            Rect bounds = mScreenMetrics.getRootViewRect();
            if (resizeRect.width() > bounds.width() || resizeRect.height() > bounds.height()) {
                throw new MraidCommandException("resizeProperties specified a size ("
                        + widthDips + ", " + heightDips + ") and offset ("
                        + offsetXDips + ", " + offsetYDips + ") that doesn't allow the ad to"
                        + " appear within the max allowed size ("
                        + mScreenMetrics.getRootViewRectDips().width() + ", "
                        + mScreenMetrics.getRootViewRectDips().height() + ")");
            }

            // Offset the resize rect so that it displays on the screen
            int newLeft = clampInt(bounds.left, resizeRect.left, bounds.right - resizeRect.width());
            int newTop = clampInt(bounds.top, resizeRect.top, bounds.bottom - resizeRect.height());
            resizeRect.offsetTo(newLeft, newTop);
        }

        // The entire close region must always be visible.
        Rect closeRect = new Rect();
        mCloseableAdContainer.applyCloseRegionBounds(closePosition, resizeRect, closeRect);
        if (!mScreenMetrics.getRootViewRect().contains(closeRect)) {
            throw new MraidCommandException("resizeProperties specified a size ("
                    + widthDips + ", " + heightDips + ") and offset ("
                    + offsetXDips + ", " + offsetYDips + ") that doesn't allow the close"
                    + " region to appear within the max allowed size ("
                    + mScreenMetrics.getRootViewRectDips().width() + ", "
                    + mScreenMetrics.getRootViewRectDips().height() + ")");
        }

        if (!resizeRect.contains(closeRect)) {
            throw new MraidCommandException("resizeProperties specified a size ("
                    + widthDips + ", " + height + ") and offset ("
                    + offsetXDips + ", " + offsetYDips + ") that don't allow the close region to appear "
                    + "within the resized ad.");
        }

        // Resized ads always rely on the creative's close button (as if useCustomClose were true)
        mCloseableAdContainer.setCloseVisible(false);
        mCloseableAdContainer.setClosePosition(closePosition);

        // Put the ad in the closeable container and resize it
        LayoutParams layoutParams = new LayoutParams(resizeRect.width(), resizeRect.height());
        layoutParams.leftMargin = resizeRect.left - mScreenMetrics.getRootViewRect().left;
        layoutParams.topMargin = resizeRect.top - mScreenMetrics.getRootViewRect().top;
        if (mViewState == ViewState.DEFAULT) {
            mDefaultAdContainer.removeView(mMraidWebView);
            mDefaultAdContainer.setVisibility(View.INVISIBLE);
            mCloseableAdContainer.addView(mMraidWebView,
                    new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            getAndMemoizeRootView().addView(mCloseableAdContainer, layoutParams);
        } else if (mViewState == ViewState.RESIZED) {
            mCloseableAdContainer.setLayoutParams(layoutParams);
        }
        mCloseableAdContainer.setClosePosition(closePosition);

        setViewState(ViewState.RESIZED);
    }

    void handleExpand(@Nullable URI uri, boolean shouldUseCustomClose)
            throws MraidCommandException {
        if (mMraidWebView == null) {
            throw new MraidCommandException("Unable to expand after the WebView is destroyed");
        }

        if (mPlacementType == PlacementType.INTERSTITIAL) {
            return;
        }

        if (mViewState != ViewState.DEFAULT && mViewState != ViewState.RESIZED) {
            return;
        }

        applyOrientation();

        // For two part expands, create a new web view
        boolean isTwoPart = (uri != null);
        if (isTwoPart) {
            // Of note: the two part ad will start off with its view state as LOADING, and will
            // transition to EXPANDED once the page is fully loaded
            mTwoPartWebView = new MraidWebView(mContext);
            mTwoPartBridge.attachView(mTwoPartWebView);

            // onPageLoaded gets fired once the html is loaded into the two part webView
            mTwoPartBridge.setContentUrl(uri.toString());
        }

        // Make sure the correct webView is in the closeable  container and make it full screen
        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        if (mViewState == ViewState.DEFAULT) {
            if (isTwoPart) {
                mCloseableAdContainer.addView(mTwoPartWebView, layoutParams);
            } else {
                mDefaultAdContainer.removeView(mMraidWebView);
                mDefaultAdContainer.setVisibility(View.INVISIBLE);
                mCloseableAdContainer.addView(mMraidWebView, layoutParams);
            }
            getAndMemoizeRootView().addView(mCloseableAdContainer,
                    new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        } else if (mViewState == ViewState.RESIZED) {
            if (isTwoPart) {
                // Move the ad back to the original container so that when we close the
                // resized ad, it will be in the correct place
                mCloseableAdContainer.removeView(mMraidWebView);
                mDefaultAdContainer.addView(mMraidWebView, layoutParams);
                mDefaultAdContainer.setVisibility(View.INVISIBLE);
                mCloseableAdContainer.addView(mTwoPartWebView, layoutParams);
            }
            // If we were resized and not 2 part, nothing to do.
        }
        mCloseableAdContainer.setLayoutParams(layoutParams);
        handleCustomClose(shouldUseCustomClose);

        // Update to expanded once we have new screen metrics. This won't update the two-part ad,
        // because it is not yet loaded.
        setViewState(ViewState.EXPANDED);
    }

    @VisibleForTesting
    protected void handleClose() {
        if (mMraidWebView == null) {
            // Doesn't throw an exception because the ad has been destroyed
            return;
        }

        if (mViewState == ViewState.LOADING || mViewState == ViewState.HIDDEN) {
            return;
        }

        // Unlock the orientation before changing the view hierarchy.
        if (mViewState == ViewState.EXPANDED || mPlacementType == PlacementType.INTERSTITIAL) {
            unApplyOrientation();
        }

        if (mViewState == ViewState.RESIZED || mViewState == ViewState.EXPANDED) {
            if (mTwoPartBridge.isAttached() && mTwoPartWebView != null) {
                // If we have a two part web view, simply remove it from the closeable container
                mCloseableAdContainer.removeView(mTwoPartWebView);
                mTwoPartBridge.detach();
            } else {
                // Move the web view from the closeable container back to the default container
                mCloseableAdContainer.removeView(mMraidWebView);
                mDefaultAdContainer.addView(mMraidWebView, new LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                mDefaultAdContainer.setVisibility(View.VISIBLE);
            }
            Views.removeFromParent(mCloseableAdContainer);

            // Set the view state to default
            setViewState(ViewState.DEFAULT);
        } else if (mViewState == ViewState.DEFAULT) {
            mDefaultAdContainer.setVisibility(View.INVISIBLE);
            setViewState(ViewState.HIDDEN);
        }
    }

    /*
     * Prefer this method over getAndMemoizeRootView() when the rootView is only being used for
     * screen size calculations (and not for adding/removing anything from the view hierarchy).
     * Having consistent return values is less important in the former case.
     */
    @NonNull
    private ViewGroup getRootView() {
        if (mRootView != null) {
            return mRootView;
        }

        final View bestRootView = Views.getTopmostView(mWeakActivity.get(),
                mDefaultAdContainer);
        return bestRootView instanceof ViewGroup
                ? (ViewGroup) bestRootView
                : mDefaultAdContainer;
    }

    @NonNull
    private ViewGroup getAndMemoizeRootView() {
        if (mRootView == null) {
            mRootView = getRootView();
        }

        return mRootView;
    }

    @VisibleForTesting
    void handleShowVideo(@NonNull String videoUrl) {
        CDAdVideoPlayerActivity.startMraid(mContext, videoUrl);
    }

    @VisibleForTesting
    void lockOrientation(final int screenOrientation) throws MraidCommandException {
        final Activity activity = mWeakActivity.get();
        if (activity == null || !shouldAllowForceOrientation(mForceOrientation)) {
            throw new MraidCommandException("Attempted to lock orientation to unsupported value: " +
                    mForceOrientation.name());
        }

        if (mOriginalActivityOrientation == null) {
            mOriginalActivityOrientation = activity.getRequestedOrientation();
        }

        activity.setRequestedOrientation(screenOrientation);
    }

    @VisibleForTesting
    void applyOrientation() throws MraidCommandException {
        if (mForceOrientation == MraidOrientation.NONE) {
            if (mAllowOrientationChange) {
                // If screen orientation can be changed, an orientation of NONE means that any
                // orientation lock should be removed
                unApplyOrientation();
            } else {
                final Activity activity = mWeakActivity.get();
                if (activity == null) {
                    throw new MraidCommandException("Unable to set MRAID expand orientation to " +
                            "'none'; expected passed in Activity Context.");
                }

                // If screen orientation cannot be changed and we can obtain the current
                // screen orientation, locking it to the current orientation is a best effort
                lockOrientation(DeviceUtils.getScreenOrientation(activity));
            }
        } else {
            // Otherwise, we have a valid, non-NONE orientation. Lock the screen based on this value
            lockOrientation(mForceOrientation.getActivityInfoOrientation());
        }
    }

    @VisibleForTesting
    void unApplyOrientation() {
        final Activity activity = mWeakActivity.get();
        if (activity != null && mOriginalActivityOrientation != null) {
            activity.setRequestedOrientation(mOriginalActivityOrientation);
        }
        mOriginalActivityOrientation = null;
    }

    @VisibleForTesting
    boolean shouldAllowForceOrientation(final MraidOrientation newOrientation) {
        // NONE is the default and always allowed
        if (newOrientation == MraidOrientation.NONE) {
            return true;
        }

        final Activity activity = mWeakActivity.get();
        // If we can't obtain an Activity, return false
        if (activity == null) {
            return false;
        }

        final ActivityInfo activityInfo;
        try {
            activityInfo = activity.getPackageManager().getActivityInfo(
                    new ComponentName(activity, activity.getClass()), 0);
        } catch (PackageManager.NameNotFoundException e) {
                        Utils.logStackTrace(e);
            return false;
        }

        // If an orientation is explicitly declared in the manifest, allow forcing this orientation
        final int activityOrientation = activityInfo.screenOrientation;
        if (activityOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            return activityOrientation == newOrientation.getActivityInfoOrientation();
        }

        // Make sure the config changes won't tear down the activity when moving to this orientation
        // The necessary configChanges must always include "orientation"
        boolean containsNecessaryConfigChanges =
                bitMaskContainsFlag(activityInfo.configChanges, CONFIG_ORIENTATION);

        // configChanges must also include "screenSize"
        containsNecessaryConfigChanges = containsNecessaryConfigChanges
                && bitMaskContainsFlag(activityInfo.configChanges, CONFIG_SCREEN_SIZE);

        return containsNecessaryConfigChanges;
    }

    @VisibleForTesting
    protected void handleCustomClose(boolean useCustomClose) {
        boolean wasUsingCustomClose = !mCloseableAdContainer.isCloseVisible();
        if (useCustomClose == wasUsingCustomClose) {
            return;
        }

        mCloseableAdContainer.setCloseVisible(!useCustomClose);
        if (mOnCloseButtonListener != null) {
            mOnCloseButtonListener.useCustomCloseChanged(useCustomClose);
        }
    }

    @NonNull
    public FrameLayout getAdContainer() {
        return mDefaultAdContainer;
    }

    /**
     * Loads a javascript URL. Useful for running callbacks, such as javascript:webviewDidClose()
     */
    public void loadJavascript(@NonNull String javascript) {
        mMraidBridge.injectJavaScript(javascript);
    }

    @VisibleForTesting
    class OrientationBroadcastReceiver extends BroadcastReceiver {
        @Nullable private Context mContext;

        // -1 until this gets set at least once
        private int mLastRotation = -1;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mContext == null) {
                return;
            }

            if (Intent.ACTION_CONFIGURATION_CHANGED.equals(intent.getAction())) {
                int orientation = getDisplayRotation();

                if (orientation != mLastRotation) {
                    mLastRotation = orientation;
                    handleOrientationChange(mLastRotation);
                }
            }
        }

        public void register(@NonNull final Context context) {
            Preconditions.checkNotNull(context);
            mContext = context.getApplicationContext();
            if (mContext != null) {
                mContext.registerReceiver(this,
                        new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
            }
        }

        public void unregister() {
            if (mContext != null) {
                mContext.unregisterReceiver(this);
                mContext = null;
            }
        }
    }

    @NonNull
    public Context getContext() {
        return mContext;
    }

    @VisibleForTesting
    void handleSetOrientationProperties(final boolean allowOrientationChange,
            final MraidOrientation forceOrientation) throws MraidCommandException {
        if (!shouldAllowForceOrientation(forceOrientation)) {
            throw new MraidCommandException(
                    "Unable to force orientation to " + forceOrientation);
        }

        mAllowOrientationChange = allowOrientationChange;
        mForceOrientation = forceOrientation;

        if (mViewState == ViewState.EXPANDED || mPlacementType == PlacementType.INTERSTITIAL) {
            applyOrientation();
        }
    }

    /**
     * Attempts to handle cdadsnativebrowser links in the device browser, deep-links in the
     * corresponding application, and all other links in the CDAd in-app browser.
     */
    @VisibleForTesting
    void handleOpen(@NonNull final String url) {
        if (mMraidListener != null) {
            mMraidListener.onOpen();
        }

        UrlHandler.Builder builder = new UrlHandler.Builder();

        if (mAdReport != null) {
            builder.withDspCreativeId(mAdReport.getDspCreativeId());
        }

        builder.withSupportedUrlActions(
                UrlAction.IGNORE_ABOUT_SCHEME,
                UrlAction.OPEN_NATIVE_BROWSER,
                UrlAction.OPEN_IN_APP_BROWSER,
                UrlAction.HANDLE_SHARE_TWEET,
                UrlAction.FOLLOW_DEEP_LINK_WITH_FALLBACK,
                UrlAction.FOLLOW_DEEP_LINK)
                .build().handleUrl(mContext, url, mClickAction);
    }

    @VisibleForTesting
    @Deprecated // for testing
    @NonNull
    ViewState getViewState() {
        return mViewState;
    }

    @VisibleForTesting
    @Deprecated // for testing
    void setViewStateForTesting(@NonNull ViewState viewState) {
        mViewState = viewState;
    }

    @VisibleForTesting
    @Deprecated // for testing
    @NonNull
    CloseableLayout getExpandedAdContainer() {
        return mCloseableAdContainer;
    }

    @VisibleForTesting
    @Deprecated // for testing
    void setRootView(FrameLayout rootView) {
        mRootView = rootView;
    }

    @VisibleForTesting
    @Deprecated // for testing
    void setRootViewSize(int width, int height) {
        mScreenMetrics.setRootViewPosition(0, 0, width, height);
    }

    @VisibleForTesting
    @Deprecated // for testing
    Integer getOriginalActivityOrientation() {
        return mOriginalActivityOrientation;
    }

    @VisibleForTesting
    @Deprecated // for testing
    boolean getAllowOrientationChange() {
        return mAllowOrientationChange;
    }

    @VisibleForTesting
    @Deprecated // for testing
    MraidOrientation getForceOrientation() {
        return mForceOrientation;
    }

    @VisibleForTesting
    @Deprecated // for testing
    void setOrientationBroadcastReceiver(OrientationBroadcastReceiver receiver) {
        mOrientationBroadcastReceiver = receiver;
    }

    @VisibleForTesting
    @Deprecated // for testing
    MraidWebView getMraidWebView() {
        return mMraidWebView;
    }

    @VisibleForTesting
    @Deprecated // for testing
    MraidWebView getTwoPartWebView() {
        return mTwoPartWebView;
    }
}
