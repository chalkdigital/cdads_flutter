package com.chalkdigital.ads;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import com.chalkdigital.common.logging.CDAdLog;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by arungupta on 27/12/17.
 */

final class CDApplicationStateMonitor implements Application.ActivityLifecycleCallbacks {

    private static CDApplicationStateMonitor instance;
    private static boolean applicationState = false, paused = true;
    private Handler handler = new Handler();
    private Runnable check;
    private static List<CDApplicationStateListener> listeners =
            new CopyOnWriteArrayList();
    public static final long CHECK_DELAY = 500;
    public static final String TAG = "CDApplicationState";


    private CDApplicationStateMonitor() {
    }

    public boolean isForeground(){
        return applicationState;
    }

    public boolean isBackground(){
        return !applicationState;
    }

    public static void init(Application app){
        if (instance == null){
            instance = new CDApplicationStateMonitor();
            app.registerActivityLifecycleCallbacks(instance);
        }
    }

    public static CDApplicationStateMonitor get(){
        if (instance == null) {
            throw new IllegalStateException(
                    "CDApplicationState is not initialised - invoke " +
                            "at least once with parameterised init/get");
        }
        return instance;
    }

    public static CDApplicationStateMonitor get(Context context){
        if (instance == null) {
            Context appCtx = context.getApplicationContext();
            if (appCtx instanceof Application) {
                init((Application)appCtx);
            }
            throw new IllegalStateException(
                    "CDAds is not initialised with" +
                            " Application object");
        }
        return instance;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        paused = false;
        boolean wasBackground = !applicationState;
        applicationState = true;

        if (check != null)
            handler.removeCallbacks(check);

        if (wasBackground){
            CDAdLog.i(TAG, "went foreground");
            for (CDApplicationStateListener l : listeners) {
                if (l == null){
                    listeners.remove(l);
                    continue;
                }
                try {
                    l.onBecameForeground();
                } catch (Exception exc) {
                    CDAdLog.e(TAG, "Listener threw exception!");
                }
            }
        } else {
            CDAdLog.i(TAG, "still foreground");
        }
    }

    @Override
    public void onActivityPaused(final Activity activity) {
        paused = true;

        if (check != null)
            handler.removeCallbacks(check);

        check = new CDActivityStateRunnable(activity.getApplicationContext());
        handler.postDelayed(check, CHECK_DELAY);
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public void addListener(CDApplicationStateListener listener){
        listeners.add(listener);
    }

    public void removeListener(CDApplicationStateListener listener){
        listeners.remove(listener);
    }

    private static class CDActivityStateRunnable implements Runnable {

        private Context mContext;

        public CDActivityStateRunnable(Context context) {
            mContext = context;
        }

        @Override
        public void run() {
            if (applicationState && paused) {
                applicationState = false;
                CDAdLog.i(TAG, "went background");
                for (CDApplicationStateListener l : listeners) {
                    if (l == null){
                        listeners.remove(l);
                        continue;
                    }
                    try {
                        l.onBecameBackground();
                    } catch (Exception exc) {
                        CDAdLog.e(TAG, exc.getLocalizedMessage());
                    }
                }
            } else {
                CDAdLog.i(TAG, "still foreground overridden pause");
            }
        }
    }
}
