package com.chalkdigital.spark;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.chalkdigital.ads.BaseBroadcastReceiver;
import com.chalkdigital.ads.VastVideoCtaButtonWidget;
import com.chalkdigital.ads.VastVideoSkipButtonWidget;
import com.chalkdigital.ads.WebViewCacheService;
import com.chalkdigital.ads.resource.DrawableConstants;
import com.chalkdigital.common.ExternalViewabilitySessionManager;
import com.chalkdigital.common.IntentActions;
import com.chalkdigital.common.Preconditions;
import com.chalkdigital.common.UrlAction;
import com.chalkdigital.common.UrlHandler;
import com.chalkdigital.common.VisibleForTesting;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.DeviceUtils;
import com.chalkdigital.common.util.Dips;
import com.chalkdigital.common.util.Drawables;
import com.chalkdigital.common.util.Utils;
import com.chalkdigital.common.util.Views;

import java.lang.ref.WeakReference;
import java.net.URI;

import static android.content.pm.ActivityInfo.CONFIG_ORIENTATION;
import static android.content.pm.ActivityInfo.CONFIG_SCREEN_SIZE;
import static com.chalkdigital.common.util.Utils.bitMaskContainsFlag;

public class SparkVideoViewController {


    public interface SparkListener {
        void onLoaded(View view);

        void onFailedToLoad();

        void onExpand();

        void onOpen();

        void onClose();

        void onPlaying();

        void onPlay();

        void onError(String errorDesc);

        void onStarted();

        void onReady();

        void onEnded();

        void onCancelled();

        void onTimeout();

        void onPause();

        void onResume();

        void onFirstQuatile();

        void onMidpoint();

        void onThirdQuartile();

        void onMuted();

        void onVolumeChanged();

        void onStateChanged(final Integer currentState);

        void onImpression();

        void onSkipped();
    }

    public interface SparkWebViewCacheListener {
        void onReady(final SparkBridge.SparkWebView webView, final ExternalViewabilitySessionManager viewabilityManager);
    }

    public interface UseCustomCloseListener {
        public void useCustomCloseChanged(boolean useCustomClose);
    }


    /**
     * Holds a weak reference to the activity if the context that is passed in is an activity.
     * While this field is never null, the reference could become null. This reference starts out
     * null if the passed-in context is not an activity.
     */
    @NonNull
    private final WeakReference<Activity> mWeakActivity;
    @NonNull
    private final Context mContext;

    @NonNull
    private final RelativeLayout mDefaultAdContainer;

    // Current view state
    @NonNull
    private com.chalkdigital.spark.ViewState mViewState = ViewState.LOADING;

    // Listeners
    @Nullable
    private SparkVideoViewController.SparkListener mSparkListener;
    @Nullable
    private SparkWebViewDebugListener mDebugListener;

    // The WebView which will display the ad. "Two part" creatives, loaded via handleExpand(URL)
    // are shown in a separate web view
    @Nullable
    private SparkBridge.SparkWebView mSparkWebView;

    // A bridge to handle all interactions with the WebView HTML and Javascript.
    @NonNull
    private final SparkBridge mSparkBridge;
    @NonNull
    private final SparkScreenMetrics mSparkScreenMetrics;
    @NonNull
    private final PlacementType mPlacementType;
    @Nullable private Long mBroadcastIdentifier;

    @NonNull
    private SparkVideoViewController.OrientationBroadcastReceiver mOrientationBroadcastReceiver =
            new SparkVideoViewController.OrientationBroadcastReceiver();

    // Stores the requested orientation for the Activity to which this controller's view belongs.
    // This is needed to restore the Activity's requested orientation in the event that the view
    // itself requires an orientation lock.
    @Nullable
    private Integer mOriginalActivityOrientation;

    private boolean mAllowOrientationChange = true;
    private SparkOrientation mForceOrientation = SparkOrientation.NONE;

    private final SparkNativeCommandHandler mSparkNativeCommandHandler;


    private boolean mIsMuted;
    private boolean mEnded;

    @NonNull
    private VastVideoCtaButtonWidget mCtaButtonWidget;
    @NonNull
    private VastVideoSkipButtonWidget mSkipButtonWidget;
    @NonNull
    private ImageView mPlayButton;
    @NonNull
    private ImageView mPauseButton;
    @NonNull
    private ImageView mMuteButton;

    @Nullable private Drawable mMutedDrawable;
    @Nullable private Drawable mUnmutedDrawable;

    // Root view, where we'll add the expanded ad
    @Nullable
    private ViewGroup mRootView;

    // Helper classes for updating screen values
    @NonNull
    private final SparkVideoViewController.ScreenMetricsWaiter mScreenMetricsWaiter;
    @NonNull private View.OnTouchListener mClickThroughListener;
    @NonNull private View.OnTouchListener mCloseOnTouchListener;
    @NonNull private View.OnClickListener mPlayListener;
    @NonNull private View.OnClickListener mMuteListener;
    @NonNull private View.OnClickListener mPauseListener;

    // Measurements
    private int mControlSizePx;
    private int mMuteSizePx;
    private int mPaddingPx;
    private String mClickAction;

    private static final int MUTE_SIZE_DIPS = 36;
    private static final int CONTROL_SIZE_DIPS = 40;
    private static final int PINNER_PADDING_DIPS = 10;


    public SparkVideoViewController(final Context context,
                                    final Bundle intentExtras,
                                    @NonNull PlacementType placementType,
                                    final Bundle savedInstanceState, final String clickAction) {
        // No broadcast identifiers are used by SparkVideoViews.
        mIsMuted = true;
        mClickAction = clickAction;
        mContext = context.getApplicationContext();
        Preconditions.checkNotNull(mContext);
        if (context instanceof Activity) {
            mWeakActivity = new WeakReference<Activity>((Activity) context);
        } else {
            // Make sure mWeakActivity itself is never null, though the reference
            // it's pointing to could be null.
            mWeakActivity = new WeakReference<Activity>(null);
        }
        mPlacementType = placementType;
        mSparkBridge = new SparkBridge(placementType);
        mViewState = ViewState.LOADING;

        mDefaultAdContainer = new RelativeLayout(mContext);
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        mSparkScreenMetrics = new SparkScreenMetrics(context, displayMetrics.density);
        View dimmingView = new View(mContext);
        dimmingView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        mOrientationBroadcastReceiver.register(mContext);

        mSparkBridge.setSparkBridgeListener(mSparkBridgeListener);
        mSparkNativeCommandHandler = new SparkNativeCommandHandler();
        mScreenMetricsWaiter = new ScreenMetricsWaiter();
        mBroadcastIdentifier = 1l;

        if (placementType== PlacementType.INTERSTITIAL) {
            mClickThroughListener = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
//                        mExternalViewabilitySessionManager.recordVideoEvent(ExternalViewabilitySession.VideoEvent.AD_CLICK_THRU,
//                                getCurrentPosition());
//                        mIsClosing = true;
                        mSparkBridge.setClicked(true);
                        mSparkBridge.simulateClickAction();
                        if (mBroadcastIdentifier!=null)
                            broadcastAction(IntentActions.ACTION_INTERSTITIAL_CLICK);
                    }
                    return true;
                }
            };

            mCloseOnTouchListener = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    handleClose();
                    return true;
                }
            };

            mMuteListener = new View.OnClickListener(){

                @Override
                public void onClick(final View v) {
                    mIsMuted = !mIsMuted;
                    loadJavascript("muteAds("+(!mIsMuted?"1)":"0)"));
                    mMuteButton.setImageDrawable(mIsMuted?mMutedDrawable:mUnmutedDrawable);
                }
            };

            mPlayListener = new View.OnClickListener(){
                @Override
                public void onClick(final View v) {
                    if (mViewState == ViewState.PAUSED){
                        resume();
                    }else if (mViewState == ViewState.COMPLETED){
                        loadJavascript("start()");
                        setViewState(ViewState.PLAYING);
                    }


                }
            };

            mPauseListener = new View.OnClickListener(){
                @Override
                public void onClick(final View v) {
                    pause();
                }
            };
            setViewState(ViewState.PLAYING);
        }

    }

    public void pause(){
//        loadJavascript("pause()");
//        setViewState(ViewState.PAUSED);
    }

    public void resume(){
//        loadJavascript("resume()");
//        setViewState(ViewState.PLAYING);
    }

    void broadcastAction(final String action) {
        if (mBroadcastIdentifier != null) {
            BaseBroadcastReceiver.broadcastAction(mContext, mBroadcastIdentifier, action);
        } else {
            CDAdLog.w("Tried to broadcast a video event without a broadcast identifier to send to.");
        }
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final SparkBridge.SparkBridgeListener mSparkBridgeListener = new SparkBridge.SparkBridgeListener() {
        @Override
        public void onPageLoaded() {
            handlePageLoad();
        }

        @Override
        public void onPageFailedToLoad() {
            if (mSparkListener != null) {
                mSparkListener.onFailedToLoad();
            }
        }

        @Override
        public void onVisibilityChanged(final boolean isVisible) {
            // The bridge only receives visibility events if there is no 2 part covering it
            mSparkBridge.notifyViewability(isVisible);
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
        public void onSetOrientationProperties(final boolean allowOrientationChange,
                                               final SparkOrientation forceOrientation) throws SparkCommandException {
            handleSetOrientationProperties(allowOrientationChange, forceOrientation);
        }

        @Override
        public void onOpen(@NonNull final URI uri) {
            if (mSparkListener != null) {
                mSparkListener.onOpen();
            }
        }

        @Override
        public void onStarted() {
            if (mSparkListener != null) {
                mSparkListener.onStarted();
            }
        }

        @Override
        public void onReady() {
            if (mSparkListener != null) {
                mSparkListener.onReady();
            }
        }

        @Override
        public void onPlaying() {
            if (mSparkListener != null) {
                mSparkListener.onPlaying();
            }
            mSparkBridge.setClicked(false);
        }

        @Override
        public void onPlay() {
            mSparkListener.onPlay();
        }

        @Override
        public void onError(String errorDesc) {
            if (mSparkListener != null) {
                mSparkListener.onError(errorDesc);
            }
        }

        @Override
        public void onEnded() {
            if (mSparkListener != null) {
                mSparkListener.onEnded();
                setViewState(ViewState.COMPLETED);
            }
        }

        @Override
        public void onCancelled() {
            if (mSparkListener != null) {
                mSparkListener.onCancelled();
            }
        }

        @Override
        public void onTimeout() {
            if (mSparkListener != null) {
                mSparkListener.onTimeout();
            }
        }

        @Override
        public void onPause() {
            if (mSparkListener != null) {
                mSparkListener.onPause();
            }
        }

        @Override
        public void onResume() {
            if (mSparkListener != null) {
                mSparkListener.onResume();
            }
        }

        @Override
        public void onFirstQuatile() {
            if (mSparkListener != null) {
                mSparkListener.onFirstQuatile();
            }
        }

        @Override
        public void onMidpoint() {
            if (mSparkListener != null) {
                mSparkListener.onMidpoint();
            }
        }

        @Override
        public void onThirdQuartile() {
            if (mSparkListener != null) {
                mSparkListener.onThirdQuartile();
            }
        }

        @Override
        public void onMuted() {
            if (mSparkListener != null) {
                mSparkListener.onMuted();
            }
        }

        @Override
        public void onVolumeChanged() {
            if (mSparkListener != null) {
                mSparkListener.onVolumeChanged();
            }
        }

        @Override
        public void onStateChanged(final Integer currentState) {
            if (mSparkListener != null) {
                mSparkListener.onStateChanged(currentState);
            }
        }

        @Override
        public void onImpression() {
            if (mSparkListener != null) {
                mSparkListener.onImpression();
            }
        }

        @Override
        public void onSkipped() {
            if (mSparkListener !=null){
                mSparkListener.onSkipped();
            }
        }
    };

    @VisibleForTesting
    void handleSetOrientationProperties(final boolean allowOrientationChange,
                                        final SparkOrientation forceOrientation) throws SparkCommandException {
        if (!shouldAllowForceOrientation(forceOrientation)) {
            throw new SparkCommandException(
                    "Unable to force orientation to " + forceOrientation);
        }

        mAllowOrientationChange = allowOrientationChange;
        mForceOrientation = forceOrientation;

        applyOrientation();
    }

    /**
     * Attempts to handle cdadsnativebrowser links in the device browser, deep-links in the
     * corresponding application, and all other links in the CDAd in-app browser.
     */
    @VisibleForTesting
    void handleOpen(@NonNull final String url) {
        if (mSparkListener != null) {
            mSparkListener.onOpen();
        }

        UrlHandler.Builder builder = new UrlHandler.Builder();


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
    protected void handleClose() {
        if (mSparkWebView == null) {
            // Doesn't throw an exception because the ad has been destroyed
            return;
        }

        if (mViewState == com.chalkdigital.spark.ViewState.LOADING || mViewState == com.chalkdigital.spark.ViewState.HIDDEN) {
            return;
        }

        // Unlock the orientation before changing the view hierarchy.
        unApplyOrientation();

//        if (mViewState == com.chalkdigital.spark.ViewState.DEFAULT) {
            mDefaultAdContainer.setVisibility(View.INVISIBLE);
            setViewState(com.chalkdigital.spark.ViewState.HIDDEN);
//        }
    }

    @VisibleForTesting
    void handlePageLoad() {
        setViewState(com.chalkdigital.spark.ViewState.DEFAULT, new Runnable() {
            @Override
            public void run() {
                mSparkBridge.notifySupports(
                        mSparkNativeCommandHandler.isSmsAvailable(mContext),
                        mSparkNativeCommandHandler.isTelAvailable(mContext),
                        SparkNativeCommandHandler.isCalendarAvailable(mContext),
                        SparkNativeCommandHandler.isStorePictureSupported(mContext),
                        isInlineVideoAvailable());
                mSparkBridge.notifyViewability(mSparkBridge.isVisible());
            }
        });

        // Call onLoaded immediately. This causes the container to get added to the view hierarchy
        if (mSparkListener != null) {
            mSparkListener.onLoaded(mDefaultAdContainer);
        }
    }

    private boolean isInlineVideoAvailable() {
        final Activity activity = mWeakActivity.get();
        //noinspection SimplifiableIfStatement
        if (activity == null || getCurrentWebView() == null) {
            return false;
        }

        return mSparkNativeCommandHandler.isInlineVideoAvailable(activity, getCurrentWebView());
    }

    @Nullable
    public SparkBridge.SparkWebView getCurrentWebView() {
        return mSparkWebView;
    }

    private void setViewState(@NonNull com.chalkdigital.spark.ViewState viewState) {
        setViewState(viewState, null);
    }

    private void setViewState(@NonNull com.chalkdigital.spark.ViewState viewState, @Nullable Runnable successRunnable) {
        // Make sure this is a valid transition.
        CDAdLog.d("SPARK state set to " + viewState);
        final com.chalkdigital.spark.ViewState previousViewState = mViewState;
        mViewState = viewState;
        mSparkBridge.notifyViewState(viewState);

        if (mPauseButton!=null){
            if (mViewState == ViewState.PAUSED){
                mPauseButton.setVisibility(View.INVISIBLE);
                mPlayButton.setVisibility(View.VISIBLE);
            }else if(mViewState == ViewState.PLAYING){
                mPauseButton.setVisibility(View.INVISIBLE);
                mPlayButton.setVisibility(View.INVISIBLE);
            }else if(mViewState == ViewState.COMPLETED){
                mPauseButton.setVisibility(View.INVISIBLE);
                mPlayButton.setVisibility(View.VISIBLE);
            }
        }

        if (mSparkListener != null) {
            if (viewState == com.chalkdigital.spark.ViewState.DEFAULT) {
//                mSparkListener.onClose();
            } else if (viewState == com.chalkdigital.spark.ViewState.HIDDEN) {
                mSparkListener.onClose();
            }
        }

        updateScreenMetricsAsync(successRunnable);
    }

    @VisibleForTesting
    void handleShowVideo(@NonNull String videoUrl) {
//        CDAdVideoPlayerActivity.startSpark(mContext, videoUrl);
    }

    @VisibleForTesting
    void lockOrientation(final int screenOrientation) throws SparkCommandException {
        final Activity activity = mWeakActivity.get();
        if (activity == null || !shouldAllowForceOrientation(mForceOrientation)) {
            throw new SparkCommandException("Attempted to lock orientation to unsupported value: " +
                    mForceOrientation.name());
        }

        if (mOriginalActivityOrientation == null) {
            mOriginalActivityOrientation = activity.getRequestedOrientation();
        }

        activity.setRequestedOrientation(screenOrientation);
    }

    @VisibleForTesting
    void applyOrientation() throws SparkCommandException {
        if (mForceOrientation == SparkOrientation.NONE) {
            if (mAllowOrientationChange) {
                // If screen orientation can be changed, an orientation of NONE means that any
                // orientation lock should be removed
                unApplyOrientation();
            } else {
                final Activity activity = mWeakActivity.get();
                if (activity == null) {
                    throw new SparkCommandException("Unable to set SPARK expand orientation to " +
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
    boolean shouldAllowForceOrientation(final SparkOrientation newOrientation) {
        // NONE is the default and always allowed
        if (newOrientation == SparkOrientation.NONE) {
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
    class OrientationBroadcastReceiver extends BroadcastReceiver {
        @Nullable
        private Context mContext;

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

    void handleOrientationChange(int currentRotation) {
        updateScreenMetricsAsync(null);
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
                        mSparkScreenMetrics.setScreenSize(
                                displayMetrics.widthPixels, displayMetrics.heightPixels);

                        int[] location = new int[2];
                        View rootView = getRootView();
                        rootView.getLocationOnScreen(location);
                        mSparkScreenMetrics.setRootViewPosition(location[0], location[1],
                                rootView.getWidth(),
                                rootView.getHeight());

                        mDefaultAdContainer.getLocationOnScreen(location);
                        mSparkScreenMetrics.setDefaultAdPosition(location[0], location[1],
                                mDefaultAdContainer.getWidth(),
                                mDefaultAdContainer.getHeight());

                        currentWebView.getLocationOnScreen(location);
                        mSparkScreenMetrics.setCurrentAdPosition(location[0], location[1],
                                currentWebView.getWidth(),
                                currentWebView.getHeight());

                        // Always notify both bridges of the new metrics
                        mSparkBridge.notifyScreenMetrics(mSparkScreenMetrics);

                        if (successRunnable != null) {
                            successRunnable.run();
                        }
                    }
                });
    }


    @VisibleForTesting
    static class ScreenMetricsWaiter {
        static class WaitRequest {
            @NonNull
            private final View[] mViews;
            @NonNull
            private final Handler mHandler;
            @Nullable
            private Runnable mSuccessRunnable;
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
                        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
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

        @NonNull
        private final Handler mHandler = new Handler();
        @Nullable
        private SparkVideoViewController.ScreenMetricsWaiter.WaitRequest mLastWaitRequest;

        SparkVideoViewController.ScreenMetricsWaiter.WaitRequest waitFor(@NonNull View... views) {
            mLastWaitRequest = new SparkVideoViewController.ScreenMetricsWaiter.WaitRequest(mHandler, views);
            return mLastWaitRequest;
        }

        void cancelLastRequest() {
            if (mLastWaitRequest != null) {
                mLastWaitRequest.cancel();
                mLastWaitRequest = null;
            }
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

    @Deprecated
        // for testing
    void setRootView(RelativeLayout rootView) {
        mRootView = rootView;
    }

    public void setSparkListener(@Nullable SparkListener sparkListener) {
        mSparkListener = sparkListener;
    }

    public void setDebugListener(@Nullable SparkWebViewDebugListener debugListener) {
        mDebugListener = debugListener;
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

        // Remove the closeable ad container from the view hierarchy, if necessary
        Views.removeFromParent(mSparkWebView);

        // Calling destroy eliminates a memory leak on Gingerbread devices
        mSparkBridge.detach();
        if (mSparkWebView != null) {
            mSparkWebView.destroy();
            mSparkWebView = null;
        }
    }

    /**
     * Gets an SparkWebView and fills it with data. In the case that the SparkWebView is retrieved
     * from the cache, this also notifies that the ad has been loaded. If the broadcast identifier
     * is null or there is a cache miss, a new SparkWebView is created and is filled with htmlData.
     *
     * @param broadcastIdentifier The unique identifier of an interstitial. This can be null,
     *                            especially when there is no interstitial.
     * @param htmlData            The HTML of the ad. This will only be loaded if a cached WebView
     *                            is not found.
     * @param listener            Optional listener that (if non-null) is notified when an
     *                            SparkWebView is loaded from the cache or created.
     */
    public void fillContent(@Nullable final Long broadcastIdentifier,
                            @NonNull final String htmlData,
                            @Nullable final SparkWebViewCacheListener listener) {
        Preconditions.checkNotNull(htmlData, "htmlData cannot be null");

        final boolean cacheHit = hydrateSparkWebView(null, listener);
        Preconditions.NoThrow.checkNotNull(mSparkWebView, "mSparkWebView cannot be null");
        mSparkBridge.attachView(mSparkWebView);
        mDefaultAdContainer.addView(mSparkWebView,
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
//        if (mPlacementType == PlacementType.INTERSTITIAL)
//            addCtaButtonWidget(mContext);
        // If the WebView was retrieved from the cache, notify that the ad is already loaded.
        if (cacheHit) {
            handlePageLoad();
        } else {
            // Otherwise, load the content into the SparkWebView
            mSparkBridge.setContentHtml(htmlData);
        }
    }

    /**
     * Gets and sets the SparkWebView. Returns true if the SparkWebView was from the cache, and
     * false if a new one was created. If the broadcast identifier is {@code null}, then this
     * will always return false and create a new SparkWebView.
     *
     * @param broadcastIdentifier The unique identifier associated with the SparkWebView in the cache.
     * @param listener            Listener passed in from {@link #fillContent(Long, String, SparkWebViewCacheListener)}
     * @return {@code true} if there was a cache hit, {@code false} if a new SparkWebView was created.
     */
    private boolean hydrateSparkWebView(@Nullable final Long broadcastIdentifier,
                                        @Nullable final SparkWebViewCacheListener listener) {
        if (broadcastIdentifier != null) {
            final WebViewCacheService.Config config =
                    WebViewCacheService.popWebViewConfig(broadcastIdentifier);
            if (config != null && config.getWebView() instanceof SparkBridge.SparkWebView) {
                mSparkWebView = (SparkBridge.SparkWebView) config.getWebView();
                mSparkWebView.enablePlugins(true);

                if (listener != null) {
                    listener.onReady(mSparkWebView, config.getViewabilityManager());
                }
                return true;
            }
        }
        CDAdLog.d("WebView cache miss. Creating a new SparkWebView.");
        mSparkWebView = new SparkBridge.SparkWebView(mContext);

        if (listener != null) {
            listener.onReady(mSparkWebView, null);
        }
        return false;
    }

    /**
     * Loads a javascript URL. Useful for running callbacks, such as javascript:webviewDidClose()
     */
    public void loadJavascript(@NonNull String javascript) {
        mSparkBridge.injectJavaScript(javascript);
    }

    @NonNull
    public RelativeLayout getAdContainer() {
        return mDefaultAdContainer;
    }


    private void addCtaButtonWidget(@NonNull final Context context) {

        mCtaButtonWidget = new VastVideoCtaButtonWidget(context, mDefaultAdContainer.getId(), false,
                true);
        mSkipButtonWidget = new VastVideoSkipButtonWidget(context, mDefaultAdContainer.getId(), false,
                true);

        mDefaultAdContainer.addView(mCtaButtonWidget);
        mDefaultAdContainer.addView(mSkipButtonWidget);

        mCtaButtonWidget.setOnTouchListener(mClickThroughListener);
        mSkipButtonWidget.setOnTouchListener(mCloseOnTouchListener);

        mControlSizePx = Dips.asIntPixels(CONTROL_SIZE_DIPS, context);
        mMuteSizePx = Dips.asIntPixels(MUTE_SIZE_DIPS, context);
        mPaddingPx = Dips.asIntPixels(PINNER_PADDING_DIPS, context);
        mMutedDrawable = Drawables.NATIVE_MUTED.createDrawable(mContext);
        mUnmutedDrawable = Drawables.NATIVE_UNMUTED.createDrawable(mContext);
        final RelativeLayout.LayoutParams muteControlParams = new RelativeLayout.LayoutParams(mControlSizePx, mControlSizePx);
        muteControlParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        muteControlParams.addRule(RelativeLayout.ABOVE, mSkipButtonWidget.getId());
        final int margin = Dips.dipsToIntPixels(DrawableConstants.CtaButton.MARGIN_DIPS, context);
        muteControlParams.setMargins(margin-mPaddingPx, 0, 0, 0);
        mMuteButton = new ImageView(mContext);
        mMuteButton.setLayoutParams(muteControlParams);
        mMuteButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mMuteButton.setPadding(mPaddingPx, mPaddingPx, mPaddingPx, mPaddingPx);
        mMuteButton.setImageDrawable(mMutedDrawable);
        mDefaultAdContainer.addView(mMuteButton);


        final RelativeLayout.LayoutParams playButtonParams = new RelativeLayout.LayoutParams(mControlSizePx, mControlSizePx);
        playButtonParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        mPlayButton = new ImageView(mContext);
        mPlayButton.setLayoutParams(playButtonParams);
        mPlayButton.setImageDrawable(Drawables.NATIVE_PLAY.createDrawable(mContext));
        mPlayButton.setVisibility(View.INVISIBLE);
        mDefaultAdContainer.addView(mPlayButton);

        final RelativeLayout.LayoutParams pauseButtonParams = new RelativeLayout.LayoutParams(mControlSizePx, mControlSizePx);
        pauseButtonParams.addRule(RelativeLayout.ABOVE, mSkipButtonWidget.getId());
        pauseButtonParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        pauseButtonParams.setMargins(mControlSizePx+margin-mPaddingPx, 0, 0, 0);
        mPauseButton = new ImageView(mContext);
        mPauseButton.setLayoutParams(pauseButtonParams);
        mPauseButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mPauseButton.setPadding(mPaddingPx, mPaddingPx, mPaddingPx, mPaddingPx);
        mPauseButton.setImageDrawable(Drawables.NATIVE_PAUSE.createDrawable(mContext));
        mDefaultAdContainer.addView(mPauseButton);

        mMuteButton.setOnClickListener(mMuteListener);
        mPlayButton.setOnClickListener(mPlayListener);
        mPauseButton.setOnClickListener(mPauseListener);


    }

    public boolean isMuted() {
        return mIsMuted;
    }
}

