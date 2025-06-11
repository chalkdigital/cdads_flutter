package com.chalkdigital.common;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.Utils;

/**
 * Created by arungupta on 07/12/16.
 */

public class CDAdLocationManagerService extends Service{

    private static final String TAG = "CDAdLocationManagerService";
    private CDAdLocationManager mCDAdLocationManager;

    @Override
    public void onCreate() {
        mCDAdLocationManager = CDAdLocationManager.sharedInstance(this);
        CDAdLog.e(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        CDAdLog.d(TAG, "onStartCommand");
        try {
            if (intent!=null){
                CDAdLog.d(TAG,"Action :"+ intent.getAction());
                switch (intent.getAction()){
                    case CDAdActions.NEW_LOCATION_REQUESTED:
                        if (!mCDAdLocationManager.isProcessingLocation()){
                            mCDAdLocationManager.setNewLocationInProgress(true);
                            mCDAdLocationManager.startUpdatingLocation(intent.getLongExtra(DataKeys.LOCATION_CLIENT_UPDATE_INTERVAL, CDAdConstants.CDAdLocationClientSingleUpdateInterval));
                        }
                        return START_REDELIVER_INTENT;
                    case CDAdActions.CONTINUOUS_LOCATION_REQUESTED:
                        if (!mCDAdLocationManager.isReceivingContinuousLocationUpdates()){
                            mCDAdLocationManager.startUpdatingLocation(intent.getLongExtra(DataKeys.LOCATION_CLIENT_UPDATE_INTERVAL, CDAdConstants.CDAdLocationClientSingleUpdateInterval));
                        }
                        return START_REDELIVER_INTENT;
                    case CDAdActions.STOP_CONTINUOUS_LOCATION_UPDATES:
                        if (mCDAdLocationManager.isReceivingContinuousLocationUpdates()) {
                            mCDAdLocationManager.stopUpdatingLocation(false);
                            mCDAdLocationManager.setReceivingContinuousLocationUpdates(false);
                        }
                        return START_REDELIVER_INTENT;
                    case CDAdActions.REVERSE_GEOCODE_LOCATION:
                        mCDAdLocationManager.reverseGeocodeWithLocation(mCDAdLocationManager.getLastLocation(this.getApplicationContext()), false);
                        return START_REDELIVER_INTENT;
                }
            }
        }  catch (Throwable throwable) {
            Utils.logStackTrace(throwable);
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        CDAdLog.e(TAG, "onDestroy");
        try {
            mCDAdLocationManager.onDestroy(this);
        }  catch (Throwable throwable) {
            Utils.logStackTrace(throwable);
        }
        super.onDestroy();

    }







}

