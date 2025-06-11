package com.chalkdigital.common;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.Utils;

public class CDAdIPGeolocationService extends Service {

    private static final String TAG = "CDAdIPGeolocationService";


    @Override
    public void onCreate() {
        CDAdLog.e(TAG, "onCreate");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            CDAdIPGeolocationManager.sharedInstance(this).geoLocateIp(intent.getAction());
        }  catch (Throwable throwable) {
            Utils.logStackTrace(throwable);
        }
        return START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        CDAdLog.e(TAG, "onDestroy");
        super.onDestroy();

    }

    private void stopService(){
        CDAdLog.d(TAG, "stopService");

    }
}
