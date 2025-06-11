package com.chalkdigital.common;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Looper;

import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.DeviceUtils;
import com.chalkdigital.common.util.Utils;

public class CDAdTrackingJobService extends JobService {

    private static CDTrackingManager sCDTrackingManager;

    private static final String TAG = "CDAdTrackingJobService";


    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        CDAdLog.d(TAG, "onStartJob");
        try{
            sCDTrackingManager = CDTrackingManager.sharedInstance(this, jobParameters);
            String action = jobParameters.getExtras().getString(DataKeys.JOB_ACTION_KEY);
            switch (action) {
                case CDAdActions.SEND_TRACK_DATA:
                    SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_TRACK_DATA_REQUESTED, true, this.getApplicationContext());
                    if (DeviceUtils.isNetworkAvailable(this.getApplicationContext())) {
                        SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_TRACK_DATA_UPLOAD_IN_PROCESS, true, this.getApplicationContext());
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Looper.prepare();
                                sCDTrackingManager.startSendingTrackingData();
                                Looper.loop();
                            }
                        }).start();
                    }
            }

        }
        catch (Throwable throwable){
            Utils.logStackTrace(throwable);
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        CDAdLog.d(TAG, "onStopJob");
        try {
            sCDTrackingManager.onDestroy(this);
        } catch (Throwable throwable) {
            Utils.logStackTrace(throwable);
        }
        return true;
    }
}
