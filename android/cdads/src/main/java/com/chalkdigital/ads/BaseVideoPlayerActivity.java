package com.chalkdigital.ads;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import com.chalkdigital.common.Constants;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.Utils;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class BaseVideoPlayerActivity extends Activity {
    public static final String VIDEO_CLASS_EXTRAS_KEY = "video_view_class_name";
    public static final String VIDEO_URL = "video_url";

    public static void startMraid(final Context context, final String videoUrl) {
        final Intent intentVideoPlayerActivity = createIntentMraid(context, videoUrl);
        try {
            context.startActivity(intentVideoPlayerActivity);
        } catch (ActivityNotFoundException e) {
                        Utils.logStackTrace(e);
            CDAdLog.d("Activity CDAdVideoPlayerActivity not found. Did you declare it in your AndroidManifest.xml?");
        }
    }

    static Intent createIntentMraid(final Context context,
            final String videoUrl) {
        final Intent intentVideoPlayerActivity = new Intent(context, CDAdVideoPlayerActivity.class);
        intentVideoPlayerActivity.setFlags(FLAG_ACTIVITY_NEW_TASK);
        intentVideoPlayerActivity.putExtra(VIDEO_CLASS_EXTRAS_KEY, "mraid");
        intentVideoPlayerActivity.putExtra(VIDEO_URL, videoUrl);
        return intentVideoPlayerActivity;
    }


    public static void startNativeVideo(final Context context, final long nativeVideoId) {
        final Intent intentVideoPlayerActivity = createIntentNativeVideo(context, nativeVideoId);
        try {
            context.startActivity(intentVideoPlayerActivity);
        } catch (ActivityNotFoundException e) {
                        Utils.logStackTrace(e);
            CDAdLog.d("Activity CDAdVideoPlayerActivity not found. Did you declare it in your AndroidManifest.xml?");
        }
    }

    public static Intent createIntentNativeVideo(final Context context, final long nativeVideoId) {
        final Intent intentVideoPlayerActivity = new Intent(context, CDAdVideoPlayerActivity.class);
        intentVideoPlayerActivity.setFlags(FLAG_ACTIVITY_NEW_TASK);
        intentVideoPlayerActivity.putExtra(VIDEO_CLASS_EXTRAS_KEY, "native");
        intentVideoPlayerActivity.putExtra(Constants.NATIVE_VIDEO_ID, nativeVideoId);
        return intentVideoPlayerActivity;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // VideoViews may never release audio focus, leaking the activity. See
        // https://code.google.com/p/android/issues/detail?id=152173.
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            am.abandonAudioFocus(null);
        }
    }
}

