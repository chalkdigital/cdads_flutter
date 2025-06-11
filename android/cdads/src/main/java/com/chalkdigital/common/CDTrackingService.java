package com.chalkdigital.common;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;

import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.DeviceUtils;


/**
 * Created by arungupta on 07/12/16.
 */

public class CDTrackingService extends Service {

    private static Context mApplicationContext;

    private static final String TAG = "CDTrackingService";
    private CDTrackingManager mCDTrackingManager;




    @Override
    public void onCreate() {
        super.onCreate();
        CDAdLog.d(TAG, "onCreate");
        mCDTrackingManager = CDTrackingManager.sharedInstance(this);
        mApplicationContext = this.getApplicationContext();
        SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_TRACK_DATA_UPLOAD_IN_PROCESS, false, mApplicationContext);
        SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_REGISTRATION_IN_PROCESS, false, mApplicationContext);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        CDAdLog.d(TAG, "onStartCommand");
        if (intent!=null && intent.getAction()!=null){
            switch (intent.getAction()){
                case CDAdActions.SEND_TRACK_DATA:
                    SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_TRACK_DATA_REQUESTED, true, mApplicationContext);
                    if (DeviceUtils.isNetworkAvailable(mApplicationContext)) {
                        SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_TRACK_DATA_UPLOAD_IN_PROCESS, true, mApplicationContext);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Looper.prepare();
                                mCDTrackingManager.startSendingTrackingData();
                                Looper.loop();
                            }
                        }).start();
                    }
                    else stopSelf();
                    return START_REDELIVER_INTENT;
            }
        }
        return START_NOT_STICKY;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();

    }

}
