package com.chalkdigital.nativeads;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Surface;
import android.view.TextureView;

import com.chalkdigital.ads.RepeatingHandlerRunnable;
import com.chalkdigital.common.Preconditions;
import com.chalkdigital.common.VisibleForTesting;
import com.chalkdigital.common.event.CDAdEvents;
import com.chalkdigital.common.event.BaseEvent;
import com.chalkdigital.common.event.Event;
import com.chalkdigital.common.event.EventDetails;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.Utils;
import com.chalkdigital.nativeads.NativeVideoController.NativeVideoProgressRunnable.ProgressListener;
import com.chalkdigital.nativeads.VisibilityTracker.VisibilityChecker;

import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlaybackException;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.LoadControl;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.exoplayer.Renderer;
import androidx.media3.common.Timeline;
import androidx.media3.exoplayer.audio.MediaCodecAudioRenderer;
import androidx.media3.extractor.Extractor;
import androidx.media3.extractor.ExtractorsFactory;
import androidx.media3.extractor.mp4.Mp4Extractor;
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector;
import androidx.media3.exoplayer.source.TrackGroupArray;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.trackselection.TrackSelectionArray;
import androidx.media3.exoplayer.trackselection.TrackSelector;
import androidx.media3.datasource.DataSource;
import androidx.media3.exoplayer.upstream.DefaultAllocator;
import androidx.media3.exoplayer.video.MediaCodecVideoRenderer;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrapper class around the {@link ExoPlayer} to provide a nice interface into the player along
 * with some helper methods. This class is not thread safe.
 */
public class NativeVideoController implements Player.Listener, OnAudioFocusChangeListener {

    public interface Listener {
        void onStateChanged(boolean playWhenReady, int playbackState);
        void onError(Exception e);
    }

    @NonNull private final static Map<Long, NativeVideoController> sManagerMap =
            new HashMap<Long, NativeVideoController>(4);

    public static final int STATE_READY = ExoPlayer.STATE_READY;
    public static final int STATE_BUFFERING = ExoPlayer.STATE_BUFFERING;
    public static final int STATE_IDLE = ExoPlayer.STATE_IDLE;
    public static final int STATE_ENDED = ExoPlayer.STATE_ENDED;
    public static final int STATE_CLEARED = ExoPlayer.STATE_ENDED + 1;

    public static final long RESUME_FINISHED_THRESHOLD = 750L;

    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024; // 64 kB
    private static final int BUFFER_SEGMENT_COUNT = 32; // 64 kB * 32 ~= 2 MB

    @NonNull private final Context mContext;
    @NonNull private final Handler mHandler;
    @NonNull private final CDAdExoPlayerFactory mCDAdExoPlayerFactory;
    @NonNull private NativeVideoProgressRunnable mNativeVideoProgressRunnable;
    @NonNull private AudioManager mAudioManager;

    @Nullable private Listener mListener;
    @Nullable private OnAudioFocusChangeListener mOnAudioFocusChangeListener;
    @Nullable private Surface mSurface;
    @Nullable private TextureView mTextureView;
    @Nullable private WeakReference<Object> mOwnerRef;
    @Nullable private volatile ExoPlayer mExoPlayer;
    @Nullable private BitmapDrawable mFinalFrame;
    @Nullable private MediaCodecAudioRenderer mAudioRenderer;
    @Nullable private MediaCodecVideoRenderer mVideoRenderer;
    @Nullable private EventDetails mEventDetails;

    private boolean mPlayWhenReady;
    private boolean mAudioEnabled;
    private boolean mAppAudioEnabled;
    private int mPreviousExoPlayerState = ExoPlayer.STATE_IDLE;
    private boolean mExoPlayerStateStartedFromIdle = true;

    /**
     * Create a new {@link NativeVideoController} for this id with the given parameters.
     * Any existing entry with the same id is removed.
     *
     * @param id the unique id of the native video ad
     * @return an initialized {@link NativeVideoController}
     */
    @NonNull
    public static NativeVideoController createForId(final long id,
            @NonNull final Context context,
            @NonNull final List<VisibilityTrackingEvent> visibilityTrackingEvents,
            @Nullable final EventDetails eventDetails) {
        NativeVideoController nvc = new NativeVideoController(context, visibilityTrackingEvents, eventDetails);
        sManagerMap.put(id, nvc);
        return nvc;
    }

    @NonNull
    @VisibleForTesting
    public static NativeVideoController createForId(final long id,
            @NonNull final Context context,
            @NonNull final NativeVideoProgressRunnable nativeVideoProgressRunnable,
            @NonNull final CDAdExoPlayerFactory cdAdExoPlayerFactory,
            @Nullable final EventDetails eventDetails,
            @NonNull final AudioManager audioManager) {
        NativeVideoController nvc = new NativeVideoController(context,
                nativeVideoProgressRunnable, cdAdExoPlayerFactory, eventDetails, audioManager);
        sManagerMap.put(id, nvc);
        return nvc;
    }

    @VisibleForTesting
    public static void setForId(final long id,
            @NonNull final NativeVideoController nativeVideoController) {
        sManagerMap.put(id, nativeVideoController);
    }

    @Nullable
    public static NativeVideoController getForId(final long id) {
        return sManagerMap.get(id);
    }

    @Nullable
    public static NativeVideoController remove(final long id) {
        return sManagerMap.remove(id);
    }

    private NativeVideoController(@NonNull final Context context,
            @NonNull final List<VisibilityTrackingEvent> visibilityTrackingEvents,
            @Nullable final EventDetails eventDetails) {
        this(context,
                new NativeVideoProgressRunnable(context,
                        new Handler(Looper.getMainLooper()),
                        visibilityTrackingEvents),
                new CDAdExoPlayerFactory(),
                eventDetails, 
                (AudioManager) context.getSystemService(Context.AUDIO_SERVICE));
    }

    private NativeVideoController(@NonNull final Context context,
            @NonNull final NativeVideoProgressRunnable nativeVideoProgressRunnable,
            @NonNull final CDAdExoPlayerFactory cdAdExoPlayerFactory,
            @Nullable final EventDetails eventDetails,
            @NonNull final AudioManager audioManager) {
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(cdAdExoPlayerFactory);
        Preconditions.checkNotNull(audioManager);

        mContext = context.getApplicationContext();
        mHandler = new Handler(Looper.getMainLooper());
        mNativeVideoProgressRunnable = nativeVideoProgressRunnable;
        mCDAdExoPlayerFactory = cdAdExoPlayerFactory;
        mEventDetails = eventDetails;
        mAudioManager = audioManager;
    }

    public void setListener(@Nullable final Listener listener) {
        mListener = listener;
    }

    public void setProgressListener(@Nullable final ProgressListener progressListener) {
        mNativeVideoProgressRunnable.setProgressListener(progressListener);
    }

    public void setOnAudioFocusChangeListener(@Nullable final OnAudioFocusChangeListener onAudioFocusChangeListener) {
        mOnAudioFocusChangeListener = onAudioFocusChangeListener;
    }

    public void setPlayWhenReady(final boolean playWhenReady) {
        if (mPlayWhenReady == playWhenReady) {
            return;
        }

        mPlayWhenReady = playWhenReady;
        setExoPlayWhenReady();
    }

    public int getPlaybackState() {
        if (mExoPlayer == null) {
            return STATE_CLEARED;
        }

        return mExoPlayer.getPlaybackState();
    }

    public void setAudioEnabled(final boolean audioEnabled) {
        mAudioEnabled = audioEnabled;
        setExoAudio();
    }

    public void setAppAudioEnabled(final boolean audioEnabled) {
        if (mAppAudioEnabled == audioEnabled) {
            return;
        }
        mAppAudioEnabled = audioEnabled;

        if (mAppAudioEnabled) {
            mAudioManager.requestAudioFocus(this,
                    AudioManager.STREAM_MUSIC,
                    // Request permanent focus.
                    AudioManager.AUDIOFOCUS_GAIN);
        } else {
            mAudioManager.abandonAudioFocus(this);
        }
    }

    public void setAudioVolume(final float volume) {
        if (!mAudioEnabled) {
            return;
        }

        setExoAudio(volume);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (mOnAudioFocusChangeListener == null) {
            return;
        }

        mOnAudioFocusChangeListener.onAudioFocusChange(focusChange);
    }

    public void setTextureView(@NonNull final TextureView textureView) {
        Preconditions.checkNotNull(textureView);

        mSurface = new Surface(textureView.getSurfaceTexture());
        mTextureView = textureView;
        mNativeVideoProgressRunnable.setTextureView(mTextureView);
        setExoSurface(mSurface);
    }

    /**
     * This method is called to prepare the controller for playback. Calling this method will
     * initiate video download.
     */
    public void prepare(@NonNull final Object owner) {
        Preconditions.checkNotNull(owner);

        mOwnerRef = new WeakReference<Object>(owner);
        clearExistingPlayer();
        preparePlayer();
        setExoSurface(mSurface);
    }

    /**
     * The controller will stop rendering to its surfacetexture when this method is called.
     */
    public void clear() {
        setPlayWhenReady(false);
        mSurface = null;
        clearExistingPlayer();
    }

    /**
     * Releases video player resources. After calling this, you must call prepare() again.
     */
    public void release(@NonNull final Object owner) {
        Preconditions.checkNotNull(owner);

        final Object object = mOwnerRef == null ? null : mOwnerRef.get();
        if (object == owner) {
            clearExistingPlayer();
        }
    }

    @Override
    public void onRepeatModeChanged(final int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(final boolean shuffleModeEnabled) {

    }


    @Override
    public void onPlayWhenReadyChanged(boolean playWhenReady, int newState) {
        if (newState == STATE_ENDED && mFinalFrame == null) {
            if (mExoPlayer == null || mSurface == null || mTextureView == null) {
                CDAdLog.w("onPlayerStateChanged called after view has been recycled.");
                return;
            }

            mFinalFrame = new BitmapDrawable(mContext.getResources(), mTextureView.getBitmap());
            mNativeVideoProgressRunnable.requestStop();
        }

        if (mPreviousExoPlayerState == ExoPlayer.STATE_READY && newState == ExoPlayer.STATE_BUFFERING) {
            CDAdEvents.log(Event.createEventFromDetails(
                    BaseEvent.Name.DOWNLOAD_BUFFERING,
                    BaseEvent.Category.NATIVE_VIDEO,
                    BaseEvent.SamplingRate.NATIVE_VIDEO,
                    mEventDetails));
        }

        if (mExoPlayerStateStartedFromIdle &&
                mPreviousExoPlayerState == ExoPlayer.STATE_BUFFERING &&
                newState == ExoPlayer.STATE_READY) {
            CDAdEvents.log(Event.createEventFromDetails(
                    BaseEvent.Name.DOWNLOAD_VIDEO_READY,
                    BaseEvent.Category.NATIVE_VIDEO,
                    BaseEvent.SamplingRate.NATIVE_VIDEO,
                    mEventDetails));
        }

        mPreviousExoPlayerState = newState;
        if (newState == ExoPlayer.STATE_READY) {
            mExoPlayerStateStartedFromIdle = false;
        } else if (newState == ExoPlayer.STATE_IDLE) {
            mExoPlayerStateStartedFromIdle = true;
        }

        if (mListener != null) {
            mListener.onStateChanged(playWhenReady, newState);
        }
    }

    public void seekTo(final long ms) {
        if (mExoPlayer == null) {
            return;
        }

        mExoPlayer.seekTo(ms);
        mNativeVideoProgressRunnable.seekTo(ms);
    }

    public long getCurrentPosition() {
        return mNativeVideoProgressRunnable.getCurrentPosition();
    }

    public long getDuration() {
        return mNativeVideoProgressRunnable.getDuration();
    }

    @Override
    public void onPlayerErrorChanged(@androidx.annotation.Nullable @Nullable PlaybackException error) {
                        Utils.logStackTrace(error);
        if (mListener == null) {
            return;
        }

        CDAdEvents.log(Event.createEventFromDetails(
                BaseEvent.Name.ERROR_DURING_PLAYBACK,
                BaseEvent.Category.NATIVE_VIDEO,
                BaseEvent.SamplingRate.NATIVE_VIDEO,
                mEventDetails));

        mListener.onError(error);
        mNativeVideoProgressRunnable.requestStop();
    }


    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {}

    /**
     * Handles forwarding the user to the specified click through url. Also, fires all unfired
     * impression trackers (which should already have been handled in the transition from in-stream
     * to full-screen. See
     * {@link CDAdCustomEventVideoNative.CDAdVideoNativeAd#render(MediaLayout)}
     */
    public void handleCtaClick(@NonNull final Context context) {
        triggerImpressionTrackers();

    }

    public boolean hasFinalFrame() {
        return mFinalFrame != null;
    }

    @Nullable
    public Drawable getFinalFrame() {
        return mFinalFrame;
    }

    void triggerImpressionTrackers() {
        mNativeVideoProgressRunnable.checkImpressionTrackers(true);
    }

    private void clearExistingPlayer() {
        if (mExoPlayer == null) {
            return;
        }

        setExoSurface(null);
        mExoPlayer.stop();
        mExoPlayer.release();
        mExoPlayer = null;
        mNativeVideoProgressRunnable.stop();
        mNativeVideoProgressRunnable.setExoPlayer(null);
    }

    private void preparePlayer() {
        if (mExoPlayer == null) {
//            mVideoRenderer = new MediaCodecVideoRenderer(mContext,
//                    MediaCodecSelector.DEFAULT, 0, mHandler, null, 10);
//            mAudioRenderer = new MediaCodecAudioRenderer(mContext, MediaCodecSelector.DEFAULT);
//            final DefaultAllocator allocator = new DefaultAllocator(true, BUFFER_SEGMENT_SIZE,
//                    BUFFER_SEGMENT_COUNT);
//
//            mExoPlayer = mCDAdExoPlayerFactory.newInstance(
//                    new Renderer[] {mVideoRenderer, mAudioRenderer},
//                    new DefaultTrackSelector(),
//                    new DefaultLoadControl(allocator));
//
//            mNativeVideoProgressRunnable.setExoPlayer(mExoPlayer);
//            mExoPlayer.addListener(this);
//
//            final DataSource.Factory dataSourceFactory = new DataSource.Factory() {
//                @Override
//                public DataSource createDataSource() {
//                    return new HttpDiskCompositeDataSource(mContext, "exo_demo", mEventDetails);
//                }
//            };
//
//            final ExtractorsFactory extractorsFactory = new ExtractorsFactory() {
//                @Override
//                public Extractor[] createExtractors() {
//                    return new Extractor[] {new Mp4Extractor()};
//                }
//            };

            mNativeVideoProgressRunnable.startRepeating(50);
        }

        setExoAudio();
        setExoPlayWhenReady();
    }

    private void setExoPlayWhenReady() {
        if (mExoPlayer == null) {
            return;
        }

        mExoPlayer.setPlayWhenReady(mPlayWhenReady);
    }

    private void setExoAudio() {
        setExoAudio(mAudioEnabled ? 1.0f : 0.0f);
    }

    private void setExoAudio(final float volume) {
        Preconditions.checkArgument(volume >= 0.0f && volume <= 1.0f);
        if (mExoPlayer == null) {
            return;
        }

//        mExoPlayer.sendMessages(
//                new ExoPlayer.ExoPlayerMessage(mAudioRenderer, C.MSG_SET_VOLUME, volume)
//        );
    }

    private void setExoSurface(@Nullable final Surface surface) {
        if (mExoPlayer == null) {
            return;
        }

//        mExoPlayer.sendMessages(
//            new ExoPlayer.ExoPlayerMessage(mVideoRenderer, C.MSG_SET_SURFACE, surface)
//        );
    }

    /**
     * Created purely for the purpose of mocking to ease testing.
     */
    @VisibleForTesting
    static class CDAdExoPlayerFactory {
        public ExoPlayer newInstance(@NonNull final Renderer[] renderers,
                @NonNull final TrackSelector trackSelector, @Nullable LoadControl loadControl) {
            return null;
//            return ExoPlayerFactory.newInstance(renderers, trackSelector, loadControl);
        }
    }

    static class VisibilityTrackingEvent {
        interface OnTrackedStrategy {
            void execute();
        }

        OnTrackedStrategy strategy;
        int minimumPercentageVisible;
        int totalRequiredPlayTimeMs;
        int totalQualifiedPlayCounter;
        boolean isTracked;
        Integer minimumVisiblePx;
    }

    static class NativeVideoProgressRunnable extends RepeatingHandlerRunnable {
        public interface ProgressListener {
            /**
             * Should send a number from 0 to 1000.
             *
             * @param progressTenthPercent tenth of a percentage of video progress
             */
            void updateProgress(int progressTenthPercent);
        }

        @NonNull private final Context mContext;
        @NonNull private final VisibilityChecker mVisibilityChecker;
        @NonNull private final List<VisibilityTrackingEvent> mVisibilityTrackingEvents;
        @Nullable private ExoPlayer mExoPlayer;
        @Nullable private TextureView mTextureView;
        @Nullable private ProgressListener mProgressListener;
        private long mCurrentPosition;
        private long mDuration;
        private boolean mStopRequested;

        NativeVideoProgressRunnable(@NonNull final Context context,
                @NonNull final Handler handler,
                @NonNull final List<VisibilityTrackingEvent> visibilityTrackingEvents) {
            this(context, handler, visibilityTrackingEvents, new VisibilityChecker());
        }

        @VisibleForTesting
        NativeVideoProgressRunnable(@NonNull final Context context,
                @NonNull final Handler handler,
                @NonNull final List<VisibilityTrackingEvent> visibilityTrackingEvents,
                @NonNull final VisibilityChecker visibilityChecker) {
            super(handler);
            Preconditions.checkNotNull(context);
            Preconditions.checkNotNull(handler);
            Preconditions.checkNotNull(visibilityTrackingEvents);

            mContext = context.getApplicationContext();
            mVisibilityTrackingEvents = visibilityTrackingEvents;
            mVisibilityChecker = visibilityChecker;
            mDuration = -1L; // Initialized to -1 so we can distinguish between "never started" and a zero-length video.
            mStopRequested = false;
        }

        void setExoPlayer(@Nullable final ExoPlayer exoPlayer) {
            mExoPlayer = exoPlayer;
        }

        void setTextureView(@Nullable final TextureView textureView) {
            mTextureView = textureView;
        }

        void setProgressListener(@Nullable final ProgressListener progressListener) {
            mProgressListener = progressListener;
        }

        void seekTo(long currentPosition) {
            mCurrentPosition = currentPosition;
        }

        long getCurrentPosition() {
            return mCurrentPosition;
        }

        long getDuration() {
            return mDuration;
        }

        void requestStop() {
            mStopRequested = true;
        }

        void checkImpressionTrackers(final boolean forceTrigger) {
            int trackedCount = 0;
            for (VisibilityTrackingEvent event : mVisibilityTrackingEvents) {
                if (event.isTracked) {
                    trackedCount++;
                    continue;
                }
                if (forceTrigger || mVisibilityChecker.isVisible(mTextureView, mTextureView,
                        event.minimumPercentageVisible, event.minimumVisiblePx)) {
                    event.totalQualifiedPlayCounter += mUpdateIntervalMillis;
                    if (forceTrigger ||
                            event.totalQualifiedPlayCounter >= event.totalRequiredPlayTimeMs) {
                        event.strategy.execute();
                        event.isTracked = true;
                        trackedCount++;
                    }
                }
            }
            if (trackedCount == mVisibilityTrackingEvents.size() && mStopRequested) {
                stop();
            }
        }

        @Override
        public void doWork() {
            if (mExoPlayer == null || !mExoPlayer.getPlayWhenReady()) {
                return;
            }

            mCurrentPosition = mExoPlayer.getCurrentPosition();
            mDuration = mExoPlayer.getDuration();

            checkImpressionTrackers(false);

            if (mProgressListener != null) {
                float tenthsOfPercentPlayed = ((float) mCurrentPosition / mDuration) * 1000;
                mProgressListener.updateProgress((int) tenthsOfPercentPlayed);
            }

        }

        @Deprecated
        @VisibleForTesting
        void setUpdateIntervalMillis(final long updateIntervalMillis) {
            mUpdateIntervalMillis = updateIntervalMillis;
        }
    }
}
