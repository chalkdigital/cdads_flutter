package com.chalkdigital.application;

import android.app.Application;
import android.util.Log;

import com.chalkdigital.BuildConfig;
import com.chalkdigital.ads.CDAds;
import com.chalkdigital.ads.CDAdsInitialisationParams;
import com.chalkdigital.common.DataKeys;
import com.chalkdigital.common.SharedPreferencesHelper;

class CDAdSampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_TRACKING_PERMISSION_GRANTED, true, this.getApplicationContext());
        CDAdsInitialisationParams cdAdsInitialisationParams = new CDAdsInitialisationParams(this.getApplicationContext());
        cdAdsInitialisationParams.setLogLevel(BuildConfig.DEBUG? Log.VERBOSE:Log.ASSERT);
        CDAds.initialiseWithParams(cdAdsInitialisationParams, this).start();
        SharedPreferencesHelper.putIntegerToSharedPreferences(DataKeys.TEMPO_LOG_LEVEL, BuildConfig.DEBUG?Log.VERBOSE:Log.ASSERT, this);

    }
}
