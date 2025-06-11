package com.chalkdigital.common;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;

import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.Utils;

@TargetApi(21)
public class CDAdLocationManagerJobService extends JobService {
    private static CDAdLocationManager mCDAdLocationManager;

    private static final String TAG = "CDAdLocationManagerJobService";

    @Override
    public boolean onStartJob(final JobParameters params) {
        CDAdLog.d(TAG, "onStartJob");
        try {
            mCDAdLocationManager = CDAdLocationManager.sharedInstance(this, params);
            String action = params.getExtras().getString(DataKeys.JOB_ACTION_KEY);
            switch (action){
                case CDAdActions.NEW_LOCATION_REQUESTED:
                    if (!mCDAdLocationManager.isProcessingLocation()){
                        mCDAdLocationManager.setNewLocationInProgress(true);
                        mCDAdLocationManager.startUpdatingLocation(null);
                    }
                    break;

                case CDAdActions.REVERSE_GEOCODE_LOCATION:
                    mCDAdLocationManager.reverseGeocodeWithLocation(mCDAdLocationManager.getLastLocation(this.getApplicationContext()), false);
                    break;

                default:
                    break;
            }
        } catch (Throwable throwable) {
            Utils.logStackTrace(throwable);
        }
        return true;
    }

    @Override
    public boolean onStopJob(final JobParameters params) {
        CDAdLog.d(TAG, "onStopJob");
        try {
            mCDAdLocationManager.onDestroy(this);
        } catch (Throwable throwable) {
            Utils.logStackTrace(throwable);
        }
        return true;
    }
}
