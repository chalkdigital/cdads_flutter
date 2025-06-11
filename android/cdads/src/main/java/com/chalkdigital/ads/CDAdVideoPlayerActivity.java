package com.chalkdigital.ads;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.chalkdigital.common.DataKeys;
import com.chalkdigital.common.IntentActions;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.Intents;
import com.chalkdigital.common.util.Reflection;
import com.chalkdigital.common.util.Utils;
import com.chalkdigital.mraid.MraidVideoViewController;

import java.util.HashMap;

import static com.chalkdigital.ads.BaseBroadcastReceiver.broadcastAction;
import static com.chalkdigital.common.DataKeys.BROADCAST_IDENTIFIER_KEY;

public class CDAdVideoPlayerActivity extends BaseVideoPlayerActivity implements BaseVideoViewController.BaseVideoViewControllerListener {
    private static final String NATIVE_VIDEO_VIEW_CONTROLLER =
            "com.chalkdigital.nativeads.NativeVideoViewController";

    @Nullable private BaseVideoViewController mBaseVideoController;
    private Long mBroadcastIdentifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mBroadcastIdentifier = getBroadcastIdentifierFromIntent(getIntent());

        try {
            mBaseVideoController = createVideoViewController(savedInstanceState);
        } catch (IllegalStateException e) {
                        Utils.logStackTrace(e);
            // This can happen if the activity was started without valid intent extras. We leave
            // mBaseVideoController set to null, and finish the activity immediately.
            if (mBroadcastIdentifier!=null)
                broadcastAction(this, mBroadcastIdentifier, IntentActions.ACTION_INTERSTITIAL_FAIL);
            finish();
            return;
        }

        mBaseVideoController.onCreate();
    }

    @Override
    protected void onPause() {
        if (mBaseVideoController != null) {
            mBaseVideoController.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBaseVideoController != null) {
            mBaseVideoController.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        if (mBaseVideoController != null) {
            mBaseVideoController.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mBaseVideoController != null) {
            mBaseVideoController.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mBaseVideoController != null) {
            mBaseVideoController.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onBackPressed() {
        if (mBaseVideoController != null && mBaseVideoController.backButtonEnabled()) {
            super.onBackPressed();
            mBaseVideoController.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (mBaseVideoController != null) {
            mBaseVideoController.onActivityResult(requestCode, resultCode, data);
        }
    }

    private BaseVideoViewController createVideoViewController(Bundle savedInstanceState) throws IllegalStateException {
        String clazz = getIntent().getStringExtra(VIDEO_CLASS_EXTRAS_KEY);
        HashMap<String , String[]> events = (HashMap<String , String[]>)getIntent().getSerializableExtra(DataKeys.AD_EVENTS_KEY);

        if ("mraid".equals(clazz)) {
            return new MraidVideoViewController(this, getIntent().getExtras(), savedInstanceState, this);
        } else if ("native".equals(clazz)) {
            final Class[] constructorParameterClasses = { Context.class, Bundle.class, Bundle.class,
                    BaseVideoViewController.BaseVideoViewControllerListener.class };
            final Object[] constructorParameterValues =
                    { this, getIntent().getExtras(), savedInstanceState, this };

            if (!Reflection.classFound(NATIVE_VIDEO_VIEW_CONTROLLER)) {
                throw new IllegalStateException("Missing native video module");
            }

            try {
                return Reflection.instantiateClassWithConstructor(NATIVE_VIDEO_VIEW_CONTROLLER,
                        BaseVideoViewController.class,
                        constructorParameterClasses,
                        constructorParameterValues);
            } catch (Throwable throwable) {
                        Utils.logStackTrace(throwable);
                throw new IllegalStateException("Missing native video module");
            }
        } else {
            throw new IllegalStateException("Unsupported video type: " + clazz);
        }
    }

    /**
     * Implementation of BaseVideoViewControllerListener
     */

    @Override
    public void onSetContentView(final View view) {
        setContentView(view);
    }

    @Override
    public void onSetRequestedOrientation(final int requestedOrientation) {
        setRequestedOrientation(requestedOrientation);
    }

    @Override
    public void onFinish() {
        finish();
    }

    @Override
    public void onStartActivityForResult(final Class<? extends Activity> clazz,
            final int requestCode,
            final Bundle extras) {
        if (clazz == null) {
            return;
        }

        final Intent intent = Intents.getStartActivityIntent(this, clazz, extras);

        try {
            startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
                        Utils.logStackTrace(e);
            CDAdLog.d("Activity " + clazz.getName() + " not found. Did you declare it in your AndroidManifest.xml?");
        }
    }

    protected static long getBroadcastIdentifierFromIntent(Intent intent) {
        return intent.getLongExtra(BROADCAST_IDENTIFIER_KEY, -1);
    }

    @Deprecated // for testing
    BaseVideoViewController getBaseVideoViewController() {
        return mBaseVideoController;
    }

    @Deprecated // for testing
    void setBaseVideoViewController(final BaseVideoViewController baseVideoViewController) {
        mBaseVideoController = baseVideoViewController;
    }
}
