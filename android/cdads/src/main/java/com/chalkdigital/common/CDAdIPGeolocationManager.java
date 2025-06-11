package com.chalkdigital.common;

import android.annotation.TargetApi;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.PersistableBundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.chalkdigital.common.db.CDDatabaseHelper;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.DeviceUtils;
import com.chalkdigital.network.response.IPGeoLocationRequestResponse;
import com.chalkdigital.network.response.IPGeoLocationResponse;
import com.chalkdigital.network.retrofit.CDAdCallbackListener;
import com.chalkdigital.network.retrofit.CDAdParams;
import com.chalkdigital.network.retrofit.CDAdRetrofit;
import com.chalkdigital.network.retrofit.CDAdService;

import retrofit2.Call;
import retrofit2.Response;

public class CDAdIPGeolocationManager implements CDAdCallbackListener {

    private static final String TAG = "CDAdIPGeolocationManager";
    private static final int GeoLocateIPScheduleJobId = 2;
    private Context mApplicationContext;
    private boolean isNewLocationInProgress;
    private static CDAdIPGeolocationManager mCDAdIPGeolocationManager;
    private Service mService;
    private JobService mJobService;
    private JobParameters mJobParameters;

    private CDDatabaseHelper mDatabaseHelper;

    private CDDatabaseHelper getDatabaseHelper(){
        if (mDatabaseHelper==null)
            mDatabaseHelper = new CDDatabaseHelper(mApplicationContext);
        return mDatabaseHelper;
    }


    static CDAdIPGeolocationManager sharedInstance(final Service service){
        if (mCDAdIPGeolocationManager ==null){
            mCDAdIPGeolocationManager = new CDAdIPGeolocationManager(service);
        }
        return mCDAdIPGeolocationManager;
    }

    static CDAdIPGeolocationManager sharedInstance(final JobService jobService, final JobParameters jobParameters){
        if (mCDAdIPGeolocationManager ==null){
            mCDAdIPGeolocationManager = new CDAdIPGeolocationManager(jobService, jobParameters);
        }
        return mCDAdIPGeolocationManager;
    }

    public CDAdIPGeolocationManager(final Service service) {
        if (mApplicationContext==null)
            mApplicationContext = service.getApplicationContext();
        mService = service;
    }

    public CDAdIPGeolocationManager(final JobService jobService, final JobParameters jobParameters) {
        if (mApplicationContext==null)
            mApplicationContext = jobService.getApplicationContext();
        mJobService = jobService;
        mJobParameters = jobParameters;
    }

    public synchronized boolean isNewLocationInProgress(Context context) {
        return isNewLocationInProgress;
    }

    public synchronized boolean isIPAvailable(Context context) {
        return SharedPreferencesHelper.getStringFromSharedPreferences(DataKeys.PUBLIC_IP, "", context).equals("")?false:true;
    }

    private synchronized void setNewLocationInProgress(boolean newLocationInProgress) {
        SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_NEW_IP_LOCATION_IN_PROGRESS, newLocationInProgress, mApplicationContext);
    }


    public static boolean shouldWaitForIP(Context context){
        if (SharedPreferencesHelper.getStringFromSharedPreferences(DataKeys.PUBLIC_IP, "", context).equals("")){
            startIPGeolocationService(CDAdActions.IP_REQUESTED, context);
            return true;
        }
        return false;
    }

    private void requestIpGeolocation(String requestAction){
        CDAdService.performRequest(CDAdRetrofit.createSharedInstance(CDAdParams.getSDKBaseUrl()), "geolocateIp", IPGeoLocationRequestResponse.class, null, null, this, requestAction, 0);
    }

    @Override
    public void onNetworkRequestSuccess(Call call, Response response, Object object, int apitype) {
        CDAdLog.d(TAG,"IP GeoLocation Success");
        IPGeoLocationRequestResponse requestResponse = (IPGeoLocationRequestResponse)response.body();
        if (requestResponse.getStatus()==0){
            IPGeoLocationResponse ipGeoLocationResponse = requestResponse.getResponse();
            if (ipGeoLocationResponse!=null){
                String clientIp = ipGeoLocationResponse.getClientIp();
                Intent intent = new Intent();
                if (clientIp!=null && clientIp.length()>0){
                    SharedPreferencesHelper.putStringToSharedPreferences(DataKeys.PUBLIC_IP, clientIp, mApplicationContext);
                    intent.setAction(CDAdActions.IP_RECEIVED);
                }else{
                    intent.setAction(CDAdActions.IP_ERROR);
                }
                LocalBroadcastManager.getInstance(mApplicationContext).sendBroadcast(intent);
                if (object.equals(CDAdActions.NEW_IP_LOCATION_REQUESTED)){
                    CDAdGeoInfo geoInfo = new CDAdGeoInfo();
                    if (ipGeoLocationResponse.getCity()!=null)
                        geoInfo.setCity(ipGeoLocationResponse.getCity());
                    if (ipGeoLocationResponse.getLatitude()!=null)
                        geoInfo.setLat(Float.parseFloat(ipGeoLocationResponse.getLatitude()));
                    if (ipGeoLocationResponse.getLongitude()!=null)
                        geoInfo.setLon(Float.parseFloat(ipGeoLocationResponse.getLongitude()));
                    if (ipGeoLocationResponse.getCountryCode()!=null) {
                        try {
                            geoInfo.setCountryCode(mApplicationContext.getResources().getText(mApplicationContext.getResources().getIdentifier(ipGeoLocationResponse.getCountryCode(), "string", mApplicationContext.getPackageName())).toString());
                        } catch (Resources.NotFoundException e) {
                            CDAdLog.d("Unable to get alpha-3 country code from ");
                        }
                    }
                    if (ipGeoLocationResponse.getPostalCode()!=null)
                        geoInfo.setZip(ipGeoLocationResponse.getPostalCode());
                    if (ipGeoLocationResponse.getRegionName()!=null)
                        geoInfo.setRegion(ipGeoLocationResponse.getRegionName());
                    geoInfo.setType(CDAdConstants.CDAdLocTypeIP);
                    geoInfo.setAccuracy("IP");
                    geoInfo.setTime(System.currentTimeMillis());
                    Location location = new Location(LocationManager.PASSIVE_PROVIDER);
                    location.setLatitude(geoInfo.getLat());
                    location.setLongitude(geoInfo.getLon());
                    location.setProvider(CDAdConstants.CDAdLocTypeIP+"");
                    location.setTime(geoInfo.getTime());
                    if (SharedPreferencesHelper.getBooleanFromSharedPreferences(DataKeys.IS_LAST_TRACKING_LOCATION_AVAILABLE, false, mApplicationContext)){
                        Location lastTrackingLocation = (Location)SharedPreferencesHelper.getObjectFromSharedPreferences(DataKeys.LAST_TRACKING_LOCATION,
                                Location.class, mApplicationContext);
                        long locationInterval = SharedPreferencesHelper.getLongFromSharedPreferences(DataKeys.LOCATION_UPDATE_INTERVAL, CDAdConstants.CDAdTrackingInterval, mApplicationContext) * 1000;
                        if (location.getTime()-lastTrackingLocation.getTime()>=locationInterval && lastTrackingLocation.distanceTo(location)>=SharedPreferencesHelper.getFloatFromSharedPreferences(DataKeys.DISTANCE_FILTER, CDAdConstants.CDAdDistanceFilter, mApplicationContext)){
                            saveLocation(location);
                        }
                    }
                    else saveLocation(location);
                    SharedPreferencesHelper.putObjectToSharedPreferences(DataKeys.LAST_LOCATION, location, mApplicationContext);
                    SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_LAST_LOCATION_AVAILABLE, true, mApplicationContext);
                    if ((geoInfo == null || geoInfo.getCountryCode()==null || geoInfo.getCountryCode().equals("") || !SharedPreferencesHelper.getBooleanFromSharedPreferences(DataKeys.IS_LAST_GEO_LOCATION_AVAILABLE, false, mApplicationContext) || CDAdLocationManager.getLastGeoLocation(mApplicationContext).distanceTo(location)>=
                            getCDAdReverseGeocodeDistanceFilter(mApplicationContext))){
                        CDAdLog.d(TAG,"Started Reverse Geocoding Service");
                        Intent intent1 = new Intent(CDAdActions.REVERSE_GEOCODE_LOCATION);
                        intent1.setClass(mApplicationContext, CDAdLocationManagerService.class);
                        mApplicationContext.startService(intent1);
                    }else {
                        SharedPreferencesHelper.putObjectToSharedPreferences(DataKeys.GEOINFO, geoInfo, mApplicationContext);
                        CDAdLocationManager.postLocationChangeNotification(location, mApplicationContext);
                    }
                }
            }
        }

        if (requestResponse.getStatus()!=0 || requestResponse.getResponse()==null){
            postFailureBroadcast(object, mApplicationContext);
        }

        stopService();
    }

    public static void postFailureBroadcast(Object action, Context context){
        Intent intent = new Intent();
        if (action.equals(CDAdActions.IP_REQUESTED))
            intent.setAction(CDAdActions.IP_ERROR);
        else intent.setAction(CDAdActions.LOCATION_ERROR);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        CDAdLog.d(TAG,"Posted Location fail broadcast");
    }

    public static int getCDAdReverseGeocodeDistanceFilter(Context context){
        return SharedPreferencesHelper.getIntegerFromSharedPreferences(DataKeys.CDAdReverseGeocodeDistanceFilterKey, CDAdConstants.CDAdReverseGeocodeDistanceFilter, context);
    }

    @Override
    public void onNetworkRequestFailure(Call call, Throwable t, Object object, int apitype) {
        CDAdLocationManager.postLocationChangeNotification(null, mApplicationContext);
        postFailureBroadcast(object, mApplicationContext);
        CDAdLog.d(TAG,"IP GeoLocation Failed "+ t.getLocalizedMessage());
        stopService();
    }



    private void saveLocation(Location location){
        SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_LAST_TRACKING_LOCATION_AVAILABLE, true, mApplicationContext);
        SharedPreferencesHelper.putObjectToSharedPreferences(DataKeys.LAST_TRACKING_LOCATION, location, mApplicationContext);
        getDatabaseHelper().saveLocation(location, Constants.CDLocTypeIP, mApplicationContext);
    }


    public static void startIPGeolocationService(String requestAction, Context context) {
        if (DeviceUtils.isNetworkAvailable(context)) {
            if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                Intent intent = new Intent(context, CDAdIPGeolocationService.class);
                intent.setAction(requestAction);
                context.startService(intent);
            }

            else{
                //if (!isJobServiceOn(context))
                scheduleIPGeolocationJob(context, requestAction);
            }
        }
    }

    @TargetApi(22)
    public static int scheduleIPGeolocationJob(Context context, String requestAction){
        ComponentName componentName = new ComponentName(context, CDAdIPGeolocationJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(GeoLocateIPScheduleJobId, componentName);
        PersistableBundle bundle = new PersistableBundle();
        bundle.putString(DataKeys.JOB_ACTION_KEY, requestAction);
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

    void geoLocateIp(String action){
        if (!isNewLocationInProgress(mApplicationContext)){
            setNewLocationInProgress(true);
            requestIpGeolocation(action);
        }
    }


    private void stopService(){
        setNewLocationInProgress(false);
        if (mService!=null){
            mService.stopSelf();
        }
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (mJobService != null) {
                mJobService.jobFinished(mJobParameters, false);
            }
        }
    }

    void onDestroy(){

    }
}
