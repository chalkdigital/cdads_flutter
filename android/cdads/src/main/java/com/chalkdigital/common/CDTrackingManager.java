package com.chalkdigital.common;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.PersistableBundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.chalkdigital.common.db.CDDatabaseHelper;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.DeviceUtils;
import com.chalkdigital.network.response.TrackingRequestResponse;
import com.chalkdigital.network.response.TrackingResponse;
import com.chalkdigital.network.retrofit.CDAdCallbackListener;
import com.chalkdigital.network.retrofit.CDAdParams;
import com.chalkdigital.network.retrofit.CDAdRetrofit;
import com.chalkdigital.network.retrofit.CDAdService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Response;


public class CDTrackingManager  implements CDAdCallbackListener {
    private static CDTrackingManager sCDTrackingManager;
    private static final int TrackingcheduleJobId = 3;
    private Service mService;
    private JobService mJobService;
    private JobParameters mJobParameters;
    private Context mContext;
    private CDDatabaseHelper mDatabaseHelper;
    private final int MAX_LOCATION_LIMIT = 30;
    final int APITYPE_LOGIN = 1;
    final int APITYPE_TRACKING = 2;

    static synchronized CDTrackingManager sharedInstance(final Service service){
        if (sCDTrackingManager ==null){
            sCDTrackingManager = new CDTrackingManager();
        }
        sCDTrackingManager.mService = service;
        sCDTrackingManager.mContext = service.getApplicationContext();
        return sCDTrackingManager;
    }

    static synchronized CDTrackingManager sharedInstance(final JobService jobService, final JobParameters jobParameters){
        if (sCDTrackingManager ==null){
            sCDTrackingManager = new CDTrackingManager();
        }
        sCDTrackingManager.mContext = jobService.getApplicationContext();
        sCDTrackingManager.mJobService = jobService;
        sCDTrackingManager.mJobParameters = jobParameters;
        return sCDTrackingManager;
    }



    public void startSendingTrackingData(){
        cancelScheduledAlarm();
        sendPendingLocations();
        schedulePendingLocationAlarm();
    }

    public static synchronized void startTrackingService(Context context){
        CDAdLog.d("CDTrackingManager", "startTrackingService");
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Intent intent = new Intent(context, CDAdLocationManagerService.class);
            intent.setAction(CDAdActions.SEND_TRACK_DATA);
            intent.putExtra(DataKeys.LOCATION_CLIENT_UPDATE_INTERVAL, CDAdConstants.CDAdLocationClientSingleUpdateInterval);
            context.startService(intent);
        }

        else{
            //if (!isJobServiceOn(context))
            scheduleTrackingJob(context);
        }
    }

    @TargetApi(22)
    public static int scheduleTrackingJob(Context context){
        ComponentName componentName = new ComponentName(context, CDAdTrackingJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(TrackingcheduleJobId, componentName);
        PersistableBundle bundle = new PersistableBundle();
        bundle.putString(DataKeys.JOB_ACTION_KEY, CDAdActions.SEND_TRACK_DATA);
//        builder.setPeriodic(1000)
        builder.setOverrideDeadline(100);
        builder.setExtras(bundle);
//                .setPersisted(true)
//                .setRequiresBatteryNotLow(true);

//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            builder = setImpInForeground(builder);
//        }

        JobScheduler jobScheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        return jobScheduler.schedule(builder.build());
    }

    private CDDatabaseHelper getDatabaseHelper(){
        if (mDatabaseHelper==null)
            mDatabaseHelper = new CDDatabaseHelper(mContext);
        return mDatabaseHelper;
    }

    public void sendPendingLocationsRequest(){
        HashMap<String, Object> params = getDatabaseHelper().getLocations(MAX_LOCATION_LIMIT);
        if (((ArrayList)params.get(DataKeys.LOCATION_DATA)).size()>0){
            CDAdDeviceInfo cdAdDeviceInfo = CDAdDeviceInfo.deviceInfo(mContext);
            HashMap<String, Object> device = new HashMap<String , Object>();
            device.put(DataKeys.LIMIT_ADVERTISER_TRACKING, cdAdDeviceInfo.getLmt());
            device.put(DataKeys.DEVICETYPE, cdAdDeviceInfo.getDevicetype()+"");
            device.put(DataKeys.DEVICEMAKE, cdAdDeviceInfo.getMake());
            device.put(DataKeys.DEVICEMODEL, cdAdDeviceInfo.getModel());
            device.put(DataKeys.DEVICEOS, cdAdDeviceInfo.getOs());
            device.put(DataKeys.OS_VERSION, cdAdDeviceInfo.getOsv());
            device.put(DataKeys.HARDWARE_VERSION, cdAdDeviceInfo.getHwv());
            device.put(DataKeys.LANGUAGE, cdAdDeviceInfo.getLanguage());
            device.put(DataKeys.HEIGHT, cdAdDeviceInfo.getH());
            device.put(DataKeys.WIDTH, cdAdDeviceInfo.getW());
            device.put(DataKeys.CARRIER, cdAdDeviceInfo.getCarrier());
            device.put(DataKeys.CONNECTION_TYPE, cdAdDeviceInfo.getConnectiontype()+"");
            device.put(DataKeys.UID, cdAdDeviceInfo.getAdid());
            device.put(DataKeys.UIDTYPE, "ADID");
            device.put(DataKeys.PIXELRATIO, String.format("%.1f", cdAdDeviceInfo.getPxratio()));
            device.put(DataKeys.JSENABLED, cdAdDeviceInfo.getJs()+"");
            device.put(DataKeys.SDK_KEY, SharedPreferencesHelper.getIntegerFromSharedPreferences(DataKeys.CDAdSDKKEYID, 0, mContext));
            device.put(DataKeys.SDK_BUILD_VERSION, CDAdConstants.CDAdBuildVersion);
            device.put(DataKeys.SDK_VERSION, CDAdConstants.CDAdSdkVersion);
            device.put(DataKeys.SDK_TRACKING_ENABLED, SharedPreferencesHelper.getBooleanFromSharedPreferences(DataKeys.IS_TRACKING_ENABLED, true, mContext)?1:0);

            String[] ids = (String[])params.get(DataKeys.IDS);
            params.remove(DataKeys.IDS);
            device.put(DataKeys.LOCATION_DATA, params);
            String host = null;

            try {
                host = mContext.getResources().getText(mContext.getResources().getIdentifier("SERVER_URL", "string", mContext.getPackageName())).toString();
            } catch (Resources.NotFoundException e) {

            }

            if (host != null && !host.isEmpty());
            CDAdService.performRequest(CDAdRetrofit.getsharedSDKRequestAPIInstance(host+"/"), "tracking", TrackingRequestResponse.class, new Class[] {HashMap.class}, new Object[] {params}, this, ids, APITYPE_TRACKING);

        }else{
            SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_TRACK_DATA_REQUESTED, false, mContext);
            SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_TRACK_DATA_UPLOAD_IN_PROCESS, false, mContext);
            SharedPreferencesHelper.putLongToSharedPreferences(DataKeys.LAST_PENDING_LOCATION_Time, Calendar.getInstance().getTimeInMillis(), mContext);
            stopService();
        }
    }

    public void sendRegistrationRequest(){
//        if (!SharedPreferencesHelper.getBooleanFromSharedPreferences(DataKeys.CDLOGIN, false, mContext)){
//            CDAdLog.d("Login Request", CDLoginRequest.getSharedLoginRequest(mContext).getParams().toString(), mContext);
//            CDADService.performRequest(CDADRetrofit.getsharedSDKRequestAPIInstance(), "login", TrackingRequestResponse.class,
//                    new Class[] {HashMap.class}, new Object[] {CDLoginRequest.getSharedLoginRequest(mContext).getParams()}, this, null, APITYPE_LOGIN);
//
//        }
    }

    private void persistChanges(TrackingResponse trackingResponse){
        if (trackingResponse!=null){
            SharedPreferencesHelper.putLongToSharedPreferences(DataKeys.CDAdLocationExpiryIntervalKey, trackingResponse.getAdLocationExpiryInterval(), mContext);
            SharedPreferencesHelper.putLongToSharedPreferences(DataKeys.CDAdfetchIntervalKey, trackingResponse.getADfetchInterval(), mContext);
            SharedPreferencesHelper.putLongToSharedPreferences(DataKeys.CDAdTrackingIntervalKey, trackingResponse.getTrackingInterval(), mContext);
            SharedPreferencesHelper.putFloatToSharedPreferences(DataKeys.CDAdDistanceFilterKey, trackingResponse.getDistanceFilter(), mContext);
            SharedPreferencesHelper.putIntegerToSharedPreferences(DataKeys.CDAdMinBGTimeKey, trackingResponse.getMinBGTime(), mContext);
            SharedPreferencesHelper.putIntegerToSharedPreferences(DataKeys.CDAdAcceptableAccuracyKey, trackingResponse.getAcceptableAccuracy(), mContext);
            SharedPreferencesHelper.putIntegerToSharedPreferences(DataKeys.CDAdReverseGeocodeDistanceFilterKey, trackingResponse.getReverseGeocodeDistanceFilter(), mContext);
            SharedPreferencesHelper.putIntegerToSharedPreferences(DataKeys.CDAdMaxLocationManagerRunningIntervalKey, trackingResponse.getMaxLocationManagerRunningInterval(), mContext);
            SharedPreferencesHelper.putIntegerToSharedPreferences(DataKeys.CDAdSDKKEYID, trackingResponse.getSdkkeyid(), mContext);
            if (trackingResponse.getDefaultAdServerUrl()!=null)
                SharedPreferencesHelper.putStringToSharedPreferences(DataKeys.CDAd_DEFAULT_BASE_URL, trackingResponse.getDefaultAdServerUrl(), mContext);
            else SharedPreferencesHelper.putStringToSharedPreferences(DataKeys.CDAd_DEFAULT_BASE_URL, CDAdParams.getSDKBaseUrl(), mContext);

            if (trackingResponse.getCountrySpecificAdUrl()!=null)
                SharedPreferencesHelper.putObjectToSharedPreferences(DataKeys.CDAd_COUNTRY_BASED_URLS, trackingResponse.getCountrySpecificAdUrl(), mContext);
            else SharedPreferencesHelper.removeFromSharedPreferences(DataKeys.CDAd_COUNTRY_BASED_URLS, mContext);
        }

    }

    @Override
    public void onNetworkRequestSuccess(Call call, Response response, Object object, int apitype) {
        TrackingRequestResponse trackingRequestResponse = (TrackingRequestResponse) response.body();
        if (trackingRequestResponse!=null && trackingRequestResponse.getStatus()==0){
            if (apitype == APITYPE_TRACKING){
                CDAdLog.d("CDAds SDK Tracking Success", trackingRequestResponse.getEdesc());
                getDatabaseHelper().deleteLocations((String[])object);
                persistChanges(trackingRequestResponse.getResponse());
                sendPendingLocationsRequest();
            }else{
                CDAdLog.d("CDAds SDK Authentication Success", trackingRequestResponse.getEdesc());
                SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.CDAdLOGIN, true, mContext);
                persistChanges(trackingRequestResponse.getResponse());
                schedulePendingLocationAlarm();
                SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_REGISTRATION_REQUESTED, false, mContext);
                SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_REGISTRATION_IN_PROCESS, false, mContext);
                Intent intent = new Intent();
                intent.setAction(CDAdActions.SDK_AUTHENTICATED);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                stopService();

            }

        }else if (trackingRequestResponse!=null && trackingRequestResponse.getStatus() == -1){
            CDAdLog.d("CDAds SDK Authentication Failed", trackingRequestResponse.getEdesc());
            if (apitype == APITYPE_LOGIN) {
                SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_REGISTRATION_REQUESTED, false, mContext);
                SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_REGISTRATION_IN_PROCESS, false, mContext);
                CDAdLocationManager.stopContinuousLocationUpdates(mContext);
            }
            else {
                SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_TRACK_DATA_REQUESTED, false, mContext);
                SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_TRACK_DATA_UPLOAD_IN_PROCESS, false, mContext);
            }
            stopService();
        }
    }

    @Override
    public void onNetworkRequestFailure(Call call, Throwable t, Object object, int apitype) {
//        CDADLog.d("CDAds SDK Authentication Failed", t.toString(), mContext);
        if (apitype == APITYPE_LOGIN) {
            SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_REGISTRATION_IN_PROCESS, false, mContext);
        }else{
            SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_TRACK_DATA_UPLOAD_IN_PROCESS, false, mContext);
        }

        stopService();
    }

    private PendingIntent getPendingIntent(){


        PendingIntent pendingIntent = PendingIntent.getService(mContext.getApplicationContext(), 0, getNewLocationRequestIntent(),
                PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    private Intent getNewLocationRequestIntent(){
        Intent intent = new Intent();
        intent.setAction(CDAdActions.SEND_TRACK_DATA);
        intent.setClass(mContext, CDTrackingService.class);
        return intent;

    }

    private void schedulePendingLocationAlarm(){
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
//        CDADLog.d("lastLocationTime", SharedPreferencesHelper.getLongFromSharedPreferences(DataKeys.LAST_PENDING_LOCATION_Time,0, mContext)+"", mContext);
//        CDADLog.d("currentLocationTime", Calendar.getInstance().getTimeInMillis()+"", mContext);
        long timeInterval = 24*3600*1000;
        alarmManager.setRepeating(AlarmManager.RTC, SharedPreferencesHelper.getLongFromSharedPreferences(DataKeys.LAST_PENDING_LOCATION_Time, Calendar.getInstance().getTimeInMillis(), mContext)+timeInterval, timeInterval, getPendingIntent());
    }

    private void cancelScheduledAlarm(){
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(getPendingIntent());
    }

    private void sendPendingLocations(){
        long lastPendingLocationSentTime = SharedPreferencesHelper.getLongFromSharedPreferences(DataKeys.LAST_PENDING_LOCATION_Time, 0, mContext);
        if (Calendar.getInstance().getTimeInMillis()-lastPendingLocationSentTime>=24*3600*1000 && DeviceUtils.isNetworkAvailable(mContext)){
            sendPendingLocationsRequest();
        }
    }

    synchronized void onDestroy(Context context){

        mContext = null;
    }


    private void stopService(){
        SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_TRACK_DATA_REQUESTED, false, mContext);
        if (mService!=null){
            mService.stopSelf();
        }
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (mJobService != null) {
                mJobService.jobFinished(mJobParameters, false);
            }
        }
    }
}
