package com.chalkdigital.common;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;

import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.Utils;

@TargetApi(21)
public class CDAdIPGeolocationJobService extends JobService {

    private static final String TAG = "CDAdsGeoLocationJobService";
    @Override
    public boolean onStartJob(final JobParameters params) {
        CDAdLog.d(TAG, "onStartJob");
        try {
            CDAdIPGeolocationManager.sharedInstance(this, params).geoLocateIp(params.getExtras().getString(DataKeys.JOB_ACTION_KEY, CDAdActions.NEW_IP_LOCATION_REQUESTED));
        }  catch (Throwable throwable) {
            Utils.logStackTrace(throwable);
        }
        return true;
    }

    @Override
    public boolean onStopJob(final JobParameters params) {
        CDAdLog.d(TAG, "onStopJob");
        return true;
    }
}
