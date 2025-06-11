package com.chalkdigital.common;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.PersistableBundle;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.content.PermissionChecker;

import com.chalkdigital.common.db.CDDatabaseHelper;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.DeviceUtils;
import com.chalkdigital.common.util.Utils;
import com.chalkdigital.network.response.ReverseGeocodeResponse;
import com.chalkdigital.network.retrofit.CDAdCallbackListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;

public class CDAdLocationManager{

    private static final int LocationScheduleJobId = 1;
    private static final int RevGeocodeScheduleJobId = 2;
    private static final String TAG = "CDAdLocationManager";
    private static CDAdSDKPermissionReceiver mCDAdSDKPermissionReceiver;
    private static FusedLocationProviderClient mFusedLocationProviderClient;
    private static boolean isCDAdPermissionActivityVisible;
    private static String mRequestAction;
    private static boolean isNewLocationInProgress;
    private static boolean isReverseGeocodingLocation;
    private static boolean isContinuousLocationInProgress;
    private static Context mContext;
    private static Service mService;
    private static JobService mJobService;
    private static JobParameters mJobParameters;
    private static CDAdLocationManager mCDAdLocationManager;
    private HandlerThread mHandlerThread;
    private static CDAdLocationCallback mCDAdLocationCallback;

    private CDDatabaseHelper mDatabaseHelper;

    private CDDatabaseHelper getDatabaseHelper(){
        if (mDatabaseHelper==null)
            mDatabaseHelper = new CDDatabaseHelper(mContext);
        return mDatabaseHelper;
    }

    static synchronized CDAdLocationManager sharedInstance(final Service service){
        if (mCDAdLocationManager ==null){
            mCDAdLocationManager = new CDAdLocationManager();
        }
        mCDAdLocationManager.mService = service;
        mContext = service.getApplicationContext();
        return mCDAdLocationManager;
    }

    static synchronized CDAdLocationManager sharedInstance(final JobService jobService, final JobParameters jobParameters){
        if (mCDAdLocationManager ==null){
            mCDAdLocationManager = new CDAdLocationManager();
        }
        mContext = jobService.getApplicationContext();
        mCDAdLocationManager.mJobService = jobService;
        mCDAdLocationManager.mJobParameters = jobParameters;
        return mCDAdLocationManager;
    }

    public CDAdLocationManager() {
        isReverseGeocodingLocation = false;
    }


    public static synchronized boolean isReverseGeocodingLocation() {
        return isReverseGeocodingLocation;
    }

    public static synchronized void setReverseGeocodingLocation(final boolean reverseGeocodingLocation) {
        isReverseGeocodingLocation = reverseGeocodingLocation;
    }

    public static synchronized boolean isReceivingContinuousLocationUpdates() {
        return isContinuousLocationInProgress;
    }

    public static synchronized void setReceivingContinuousLocationUpdates(boolean receivingContinuousLocationUpdates) {
        isContinuousLocationInProgress = receivingContinuousLocationUpdates;
    }

    public static synchronized boolean isNewLocationInProgress() {
        return isNewLocationInProgress;
    }

    public static synchronized void setNewLocationInProgress(boolean newLocationInProgress) {
        isNewLocationInProgress = newLocationInProgress;
    }

    private void initializeLocationManager() {
        CDAdLog.d(TAG, "initializeLocationManager");
        if (mFusedLocationProviderClient == null) {
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
        }

        if (mCDAdLocationCallback == null) {
            mCDAdLocationCallback = new CDAdLocationCallback();
        }
    }

    public static synchronized void stopUpdatingLocation(boolean isNewLocation){
        if (!isReceivingContinuousLocationUpdates()){
            if (isNewLocationInProgress())
                setNewLocationInProgress(false);
            CDAdLog.d(TAG, "stopUpdatingLocation");
            stopLocationManager();
            if (mService!=null){
                mService.stopSelf();
                mService = null;
                CDAdLog.d(TAG, "stopService");
            }
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                if (mJobService != null) {
                    mJobService.jobFinished(mJobParameters, false);
                    CDAdLog.d(TAG, "onJobFinished");
                    mJobService = null;
                    mJobParameters = null;
                }
            }
        }
    }

    private static void stopLocationManager(){
        if (mFusedLocationProviderClient!=null && mCDAdLocationCallback!=null){
            try {
                mFusedLocationProviderClient.removeLocationUpdates(mCDAdLocationCallback);
                CDAdLog.i(TAG, "Removed location listner");
            } catch (Exception ex) {
                CDAdLog.i(TAG, "fail to remove location listner, ignore", ex);
            }
        }
    }

    public static void startIPGeoLocationService(Context context){
        if (CDAdsUtils.isIsGeoIpLocationEnabled())
            CDAdIPGeolocationManager.startIPGeolocationService(CDAdActions.NEW_IP_LOCATION_REQUESTED, context);
        else CDAdIPGeolocationManager.postFailureBroadcast(CDAdActions.NEW_IP_LOCATION_REQUESTED, context);
    }

    public static void startReverseGeocodingService(Context context){
        Intent intent1 = new Intent(CDAdActions.REVERSE_GEOCODE_LOCATION);
        intent1.setClass(context, CDAdLocationManagerService.class);
        context.startService(intent1);
    }

    @SuppressLint("MissingPermission")
    private void startLocationManager(Long interval){
        CDAdLog.d(TAG, "Location updates requested");
        try {
            LocationRequest locationRequest = new LocationRequest();
            if (interval!=null){
                locationRequest.setInterval(interval);
                locationRequest.setFastestInterval(interval);
            }
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mHandlerThread = new HandlerThread("location looper thread");
            mHandlerThread.start();
            mFusedLocationProviderClient.requestLocationUpdates(locationRequest, mCDAdLocationCallback, mHandlerThread.getLooper());
            if (!SharedPreferencesHelper.getBooleanFromSharedPreferences(DataKeys.IS_LAST_LOCATION_AVAILABLE, false, mContext)){
                try {
                    mFusedLocationProviderClient.getLastLocation()
                            .addOnSuccessListener(new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    if (location != null) {
                                        try {
                                            updateLocation(location, false);
                                        } catch (Throwable throwable) {
                                            Utils.logStackTrace(throwable);
                                        }
                                    }
                                }
                            });
                } catch (SecurityException ex) {
                    CDAdLog.i(TAG, "fail to request last location, ignore", ex);
                }
            }
        } catch (SecurityException ex) {
            onLocationServiceNotAvailable(mContext);
            stopUpdatingLocation(false);
            CDAdLog.i(TAG, "fail to request location update, ignore", ex);
        }
    }

    private static void onLocationServiceNotAvailable(Context context){
        Location lastLocation = LocationService.getLastKnownLocation(context);
        if (lastLocation!=null){
            if (lastLocation!=null && System.currentTimeMillis()-lastLocation.getTime()<CDAdConstants.CDAdLocationExpiryInterval*1000){
                lastLocation.setProvider(CDAdConstants.CDAdLocTypeDevice +"");
                SharedPreferencesHelper.putObjectToSharedPreferences(DataKeys.LAST_LOCATION, lastLocation, context);
                SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_LAST_LOCATION_AVAILABLE, true, context);
                if (SharedPreferencesHelper.getBooleanFromSharedPreferences(DataKeys.IS_LAST_GEO_LOCATION_AVAILABLE, false, context) || !DeviceUtils.isNetworkAvailable(context)){
                    CDAdLocationManager.postLocationChangeNotification(lastLocation, context);
                }else{
                    CDAdLocationManager.startReverseGeocodingService(context);
                }

            }else CDAdLocationManager.startIPGeoLocationService(context);
        }else CDAdLocationManager.startIPGeoLocationService(context);
    }

    void startUpdatingLocation(Long interval){
        initializeLocationManager();
        startLocationManager(interval);
    }

    synchronized boolean isProcessingLocation(){
        return isNewLocationInProgress() || isReceivingContinuousLocationUpdates();
    }

    void updateLocation(Location location, boolean isNewLocation){
        if (System.currentTimeMillis()-location.getTime()>CDAdConstants.CDAdLocationExpiryInterval*1000){
            CDAdLog.v(TAG, "provider: "+location.getProvider()+" Discarding location, already expired");
            return;
        }
        CDAdLog.v(TAG, "provider: "+location.getProvider()+" onupdateLocation: " + location);
        if (SharedPreferencesHelper.getBooleanFromSharedPreferences(DataKeys.IS_LAST_TRACKING_LOCATION_AVAILABLE, false, mContext)){
            Location lastTrackingLocation = (Location)SharedPreferencesHelper.getObjectFromSharedPreferences(DataKeys.LAST_TRACKING_LOCATION,
                    Location.class, mContext);
//            if (lastTrackingLocation!=null){
                long locationInterval = SharedPreferencesHelper.getLongFromSharedPreferences(DataKeys.LOCATION_UPDATE_INTERVAL, CDAdConstants.CDAdTrackingInterval, mContext) * 1000;
                if (location.getTime()-lastTrackingLocation.getTime()>=locationInterval && lastTrackingLocation.distanceTo(location)>=SharedPreferencesHelper.getFloatFromSharedPreferences(DataKeys.DISTANCE_FILTER, CDAdConstants.CDAdDistanceFilter, mContext)){
                    saveLocation(location);
                }
//            }else{
//                saveLocation(location);
//            }
        }
        else saveLocation(location);

        SharedPreferencesHelper.putObjectToSharedPreferences(DataKeys.LAST_LOCATION, location, mContext);
        SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_LAST_LOCATION_AVAILABLE, true, mContext);
        getDatabaseHelper().saveLocation(location, Constants.CDLocTypeGPS, mContext);
        reverseGeocodeWithLocation(location, isNewLocation);

    }

    private void saveLocation(Location location){
        location.setProvider(CDAdConstants.CDAdLocTypeDevice +"");
        SharedPreferencesHelper.putObjectToSharedPreferences(DataKeys.LAST_TRACKING_LOCATION, location, mContext);
        SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_LAST_TRACKING_LOCATION_AVAILABLE, true, mContext);
    }

    synchronized void reverseGeocodeWithLocation(Location location, boolean isNewLocation){
        if (mContext!=null && location!=null && !isReverseGeocodingLocation()){
            CDAdGeoInfo cdAdGeoInfo = SharedPreferencesHelper.getObjectFromSharedPreferences(DataKeys.GEOINFO, CDAdGeoInfo.class, mContext);
            Geocoder geocoder = new Geocoder(mContext, Locale.ENGLISH);
            List<android.location.Address> addressList= null;
            try {
                addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            } catch (IOException e) {
                Utils.logStackTrace(e);
                CDAdLog.d("Reverse Geocode save address", e.getLocalizedMessage());
            }
            if (addressList != null && addressList.size()>0){
                android.location.Address address = addressList.get(0);
                setReverseGeocodingLocation(true);
                saveAddress(address, location, mContext) ;
                SharedPreferencesHelper.putObjectToSharedPreferences(DataKeys.LAST_GEO_LOCATION, location, mContext);
                SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_LAST_GEO_LOCATION_AVAILABLE, true, mContext);
            }else {
                cdAdGeoInfo.setLat((float)location.getLatitude());
                cdAdGeoInfo.setLon((float)location.getLongitude());
                cdAdGeoInfo.setTime(location.getTime());
                cdAdGeoInfo.setAccuracy(String.format("%.0f", location.getAccuracy()));
                SharedPreferencesHelper.putObjectToSharedPreferences(DataKeys.GEOINFO, cdAdGeoInfo, mContext);
                postLocationChangeNotification(location, mContext);
                stopUpdatingLocation(isNewLocation);
            }
        }else{
            stopUpdatingLocation(isNewLocation);
        }
        setReverseGeocodingLocation(false);
        if (mContext!=null){
            postLocationChangeNotification(location, mContext);
        }
        stopUpdatingLocation(isNewLocation);

    }


    public static void postLocationChangeNotification(Location location, Context context){
        Intent intent = new Intent();
        if (location!=null){
            Bundle bundle = new Bundle();
            bundle.putParcelable(LocationManager.KEY_LOCATION_CHANGED, location);
            intent.setAction(CDAdActions.LOCATION_CHANGED);
            intent.putExtras(bundle);
            CDAdLog.d("CDAdUtil", "postingLocationChangeNotification "+location.toString());
        }else{
            intent.setAction(CDAdActions.LOCATION_ERROR);
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        CDAdLog.d("CDAdUtil", "postedLocationChangeNotification "+location.toString());
    }

    public static Location getLastGeoLocation(Context context){
        return SharedPreferencesHelper.getObjectFromSharedPreferences(DataKeys.LAST_GEO_LOCATION, Location.class, context);
    }

    public static Location getLastLocation(Context context){
        return SharedPreferencesHelper.getObjectFromSharedPreferences(DataKeys.LAST_LOCATION, Location.class, context);
    }

    public static void saveAddress(Address address, Location location, Context context){
        CDAdGeoInfo cdAdGeoInfo = new CDAdGeoInfo();
        cdAdGeoInfo.setLat((float)location.getLatitude());
        cdAdGeoInfo.setLon((float)location.getLongitude());
        cdAdGeoInfo.setTime(location.getTime());
        try {
            cdAdGeoInfo.setType(CDAdConstants.CDAdLocTypeDevice);
            cdAdGeoInfo.setAccuracy(!location.getProvider().equals(CDAdConstants.CDAdLocTypeIP)?String.format("%.2f", location.getAccuracy()):"IP");
        } catch (NumberFormatException e) {
            Utils.logStackTrace(e);

        } finally {
        }
        if (address!=null){
            if (address.getCountryCode()!=null){
                try {
                    cdAdGeoInfo.setCountryCode(context.getResources().getText(context.getResources().getIdentifier(address.getCountryCode(), "string", context.getPackageName())).toString());
                } catch (Resources.NotFoundException e) {
                    CDAdLog.d("Unable to get alpha-3 country code for country name : "+address.getCountryCode());
                }
            }
            if (address.getAdminArea()!=null){
                cdAdGeoInfo.setRegion(address.getAdminArea());
            }
            if (address.getLocality()!=null){
                cdAdGeoInfo.setCity(address.getLocality());
            }
            if (address.getPostalCode()!=null){
                cdAdGeoInfo.setZip(address.getPostalCode());
            }
        }
        SharedPreferencesHelper.putObjectToSharedPreferences(DataKeys.GEOINFO, cdAdGeoInfo, context);

    }

    public static int getLocationPermissionStatus(Context context){
        boolean isFineLocationPermissionGranted;
        boolean isFineLocationPermissionDenied;
        boolean isFineLocationPermissionDeniedAppOp = false;
        if(Build.VERSION.SDK_INT >= 23) {
            int fineLocationPermissionStatus = PermissionChecker.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
            isFineLocationPermissionGranted = fineLocationPermissionStatus==PermissionChecker.PERMISSION_GRANTED;
            isFineLocationPermissionDenied = fineLocationPermissionStatus == PermissionChecker.PERMISSION_DENIED;
            isFineLocationPermissionDeniedAppOp = fineLocationPermissionStatus == PermissionChecker.PERMISSION_DENIED_APP_OP;
        }
        else {
            int fineLocationPermissionStatus = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            isFineLocationPermissionGranted = fineLocationPermissionStatus== PackageManager.PERMISSION_GRANTED;
            isFineLocationPermissionDenied = fineLocationPermissionStatus == PackageManager.PERMISSION_DENIED;
        }
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}
        if(!gps_enabled && !network_enabled) {
            return CDAdConstants.CDAdLocationServiceUnavailable;
        } else if (isFineLocationPermissionGranted) {
            return CDAdConstants.CDAdLocationPermissionGranted;
        } else if (isFineLocationPermissionDeniedAppOp) {
            return CDAdConstants.CDAdLocationPermissionDenied;
        } else if (isFineLocationPermissionDenied) {
                if (SharedPreferencesHelper.getBooleanFromSharedPreferences(DataKeys.CDAdSDKLocationUserPreferenceReceived, false, context))
                    return CDAdConstants.CDAdLocationPermissionDenied;
                else return CDAdConstants.CDAdLocationPermissionUnKnown;
        }else{
            return CDAdConstants.CDAdLocationPermissionUnKnown;
        }
    }

    public static void stopContinuousLocationUpdates(Context context) {
        if (SharedPreferencesHelper.getBooleanFromSharedPreferences(DataKeys.IS_RECEIVING_CONTINUOUS_LOCATION_UPDATES, false, context)) {
            Intent intent = new Intent(context, CDAdLocationManagerService.class);
            intent.setAction(CDAdActions.STOP_CONTINUOUS_LOCATION_UPDATES);
            context.startService(intent);
        }
    }

    public static void startLocationService(String requestAction, Context context) {
        long locationClientUpdateInterval = getLocationClientInterval(context);
        if (requestAction.equals(CDAdActions.CONTINUOUS_LOCATION_REQUESTED) && locationClientUpdateInterval == CDAdConstants.CDAdLocationClientUpdatesNotRequired) {
            requestAction = CDAdActions.NEW_LOCATION_REQUESTED;
        }
        switch (requestAction) {
            case CDAdActions.CONTINUOUS_LOCATION_REQUESTED:
                if (!SharedPreferencesHelper.getBooleanFromSharedPreferences(DataKeys.IS_RECEIVING_CONTINUOUS_LOCATION_UPDATES, false, context)) {
                    Intent intent = new Intent(context, CDAdLocationManagerService.class);
                    intent.setAction(requestAction);
                    intent.putExtra(DataKeys.LOCATION_CLIENT_UPDATE_INTERVAL, locationClientUpdateInterval);
                    context.startService(intent);
                }
                break;
            default:
                if (!SharedPreferencesHelper.getBooleanFromSharedPreferences(DataKeys.IS_RECEIVING_CONTINUOUS_LOCATION_UPDATES, false, context)) {

                    if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        Intent intent = new Intent(context, CDAdLocationManagerService.class);
                        intent.setAction(requestAction);
                        intent.putExtra(DataKeys.LOCATION_CLIENT_UPDATE_INTERVAL, CDAdConstants.CDAdLocationClientSingleUpdateInterval);
                        context.startService(intent);
                    }

                    else{
                        //if (!isJobServiceOn(context))
                            scheduleLocationJob(context);
                    }
                }
                break;
        }
    }




    static long getLocationClientInterval(Context context) {
        long locationInterval = SharedPreferencesHelper.getLongFromSharedPreferences(DataKeys.LOCATION_UPDATE_INTERVAL, CDAdConstants.CDAdTrackingInterval, context) * 1000;
        long adRefreshInterval = SharedPreferencesHelper.getLongFromSharedPreferences(DataKeys.AD_LOCATION_EXPIRY_INTERVAL, CDAdConstants.CDAdLocationExpiryInterval, context) * 1000;
        long locationClientInterval = CDAdConstants.CDAdLocationClientSingleUpdateInterval;
        if (SharedPreferencesHelper.getBooleanFromSharedPreferences(DataKeys.IS_TRACKING_ENABLED, false, context)) {
            if (adRefreshInterval != CDAdConstants.CDAdLoctionExpiryNotRequired)
                locationClientInterval = locationInterval >= adRefreshInterval ? adRefreshInterval : locationInterval;
            else locationClientInterval = locationInterval;
        } else if (adRefreshInterval != CDAdConstants.CDAdLoctionExpiryNotRequired) {
            locationClientInterval = adRefreshInterval;
        }
        return locationClientInterval;
    }

    public static boolean shouldWaitToProceedForAdRequest(Context context){
        CDAdLog.d(TAG, "shouldWaitToProceedForAdRequest");
        if (SharedPreferencesHelper.getBooleanFromSharedPreferences(DataKeys.IS_LAST_LOCATION_AVAILABLE, false, context)){
            CDAdLog.d(TAG, "LAST_LOCATION_AVAILABLE");
            int locationPermissionStatus = getLocationPermissionStatus(context);
            Location lastLocation = SharedPreferencesHelper.getObjectFromSharedPreferences(DataKeys.LAST_LOCATION, Location.class, context);
            if (lastLocation!=null && System.currentTimeMillis()-lastLocation.getTime()<CDAdConstants.CDAdLocationExpiryInterval*1000){
                if ((lastLocation.getProvider().equals(CDAdConstants.CDAdLocTypeIP+"") && locationPermissionStatus != CDAdConstants.CDAdLocationPermissionDenied && locationPermissionStatus != CDAdConstants.CDAdLocationServiceUnavailable)){
                    requestLocationUpdate(context);
                    return true;
                }
                CDAdLog.d(TAG, "LAST_LOCATION_Valid");
                if (SharedPreferencesHelper.getBooleanFromSharedPreferences(DataKeys.IS_LAST_GEO_LOCATION_AVAILABLE, false, context)){
                    CDAdLog.d(TAG, "LAST_GEO_LOCATION_AVAILABLE");
                    return false;
                }else {
                    CDAdLog.d(TAG, "LAST_GEO_NOT_AVAILABLE");

                    if ((locationPermissionStatus == CDAdConstants.CDAdLocationServiceUnavailable || locationPermissionStatus == CDAdConstants.CDAdLocationPermissionDenied) && !CDAdsUtils.isIsGeoIpLocationEnabled()) {
                        CDAdLog.d(TAG, "Can not update location");
                        return false;
                    }
                    else if(!isReverseGeocodingLocation()){
                        CDAdLog.d(TAG, "REVERSE GEOCODE");
                        requestReverseGeocode(context);
                        return true;
                    }
                }
            }else if (isNewLocationInProgress()) {
                CDAdLog.d(TAG, "Already Fetching Location");
                return true;
            }
        }
        CDAdLog.d(TAG, "requestLocationUpdate");
        requestLocationUpdate(context);

        return true;
    }


    private static void requestReverseGeocode(Context context){
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Intent intent = new Intent(CDAdActions.REVERSE_GEOCODE_LOCATION);
            intent.setClass(context, CDAdLocationManagerService.class);
            context.startService(intent);
        }

        else{
            scheduleReverseGeocodeJob(context);
        }
    }

    @TargetApi(22)
    public static boolean isJobServiceOn( Context context ) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService( Context.JOB_SCHEDULER_SERVICE ) ;

        boolean hasBeenScheduled = false ;

        for ( JobInfo jobInfo : scheduler.getAllPendingJobs() ) {
            if ( jobInfo.getId() == LocationScheduleJobId ) {
                hasBeenScheduled = true ;
                break ;
            }
        }

        return hasBeenScheduled ;
    }

    @TargetApi(22)
    public static int scheduleLocationJob(Context context){
        ComponentName componentName = new ComponentName(context, CDAdLocationManagerJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(LocationScheduleJobId, componentName);
//        builder.setPeriodic(1000)
        builder.setOverrideDeadline(100);
//                .setPersisted(true)
//                .setRequiresBatteryNotLow(true);

//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            builder = setImpInForeground(builder);
//        }

        PersistableBundle bundle = new PersistableBundle();
        bundle.putString(DataKeys.JOB_ACTION_KEY, CDAdActions.NEW_LOCATION_REQUESTED);
        builder.setExtras(bundle);
        JobScheduler jobScheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        return jobScheduler.schedule(builder.build());
    }

    @TargetApi(22)
    public static int scheduleReverseGeocodeJob(Context context){
        ComponentName componentName = new ComponentName(context, CDAdLocationManagerJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(RevGeocodeScheduleJobId, componentName);
        builder.setOverrideDeadline(100);
        PersistableBundle bundle = new PersistableBundle();
        bundle.putString(DataKeys.JOB_ACTION_KEY, CDAdActions.REVERSE_GEOCODE_LOCATION);
        builder.setExtras(bundle);
        JobScheduler jobScheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        return jobScheduler.schedule(builder.build());
    }

//    @TargetApi(28)
//    public static JobInfo.Builder setImpInForeground(JobInfo.Builder builder){
//            return builder.setImportantWhileForeground(true);
//    }

    static void requestLocationUpdate(Context context){
        CDAdLog.d("Bannerview", "Starting Location Service");
        checkPermissionsAndStartLocationService(CDAdActions.NEW_LOCATION_REQUESTED, context);
    }

    static void checkPermissionsAndStartLocationService(String requestAction, Context context) {


        int locationPermissionStatus = getLocationPermissionStatus(context);

        if (locationPermissionStatus == CDAdConstants.CDAdLocationPermissionGranted) {
            startLocationService(requestAction, context);
        } else if (locationPermissionStatus == CDAdConstants.CDAdLocationServiceUnavailable || locationPermissionStatus == CDAdConstants.CDAdLocationPermissionDenied) {
            onLocationServiceNotAvailable(context);
        } else if (locationPermissionStatus == CDAdConstants.CDAdLocationPermissionUnKnown){
            setRequestAction(requestAction);
//            if (!isCDAdPermissionActivityVisible) {
//                isCDAdPermissionActivityVisible = true;
                IntentFilter filter = new IntentFilter(CDAdActions.LOCATION_PERMISSION_GRANTED);
                filter.addAction(CDAdActions.LOCATION_PERMISSION_DENIED);
                if (mCDAdSDKPermissionReceiver == null) {
                    mCDAdSDKPermissionReceiver = new CDAdSDKPermissionReceiver();
                }
                LocalBroadcastManager.getInstance(context).registerReceiver(mCDAdSDKPermissionReceiver, filter);
                try {
                    Intent intent = new Intent(context, CDAdPermissionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.setAction(CDAdActions.LOCATION_PERMISSION_REQUESTED);
                    context.startActivity(intent);
                } catch (Exception e) {
                    LocalBroadcastManager.getInstance(context).unregisterReceiver(mCDAdSDKPermissionReceiver);
                    startIPGeoLocationService(context);
                }
//            }
        }

    }

    public static void setRequestAction(String requestAction) {
//        if (requestAction.equals(CDAdActions.CONTINUOUS_LOCATION_REQUESTED))
            mRequestAction = requestAction;
    }

    synchronized void onDestroy(Context context){
        if (context!=null && isNewLocationInProgress())
            CDAdIPGeolocationManager.postFailureBroadcast(CDAdActions.NEW_IP_LOCATION_REQUESTED, context);
        stopUpdatingLocation(false);
        mHandlerThread.quit();
        mCDAdLocationCallback = null;
        mCDAdSDKPermissionReceiver = null;
        mContext = null;
    }

    private static class CDAdSDKPermissionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case CDAdActions.LOCATION_PERMISSION_GRANTED:
                    SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.CDAdSDKLocationUserPreferenceReceived, true, context);
                    startLocationService(mRequestAction, context);
                    break;
                case CDAdActions.LOCATION_PERMISSION_DENIED:
                    SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.CDAdSDKLocationUserPreferenceReceived, true, context);
                    startIPGeoLocationService(context);
                    break;
                default:
                    break;
            }
            LocalBroadcastManager.getInstance(context).unregisterReceiver(mCDAdSDKPermissionReceiver);
        }
    }

    private static class CDAdLocationCallback extends LocationCallback{
        @Override
        public void onLocationResult(final LocationResult locationResult) {
            super.onLocationResult(locationResult);
            try {
                if (mContext!=null && locationResult.getLastLocation()!=null && mCDAdLocationManager!=null);
                mCDAdLocationManager.updateLocation(locationResult.getLastLocation(), true);
            } catch (Throwable throwable) {
                Utils.logStackTrace(throwable);
            }
        }

        @Override
        public void onLocationAvailability(final LocationAvailability locationAvailability) {
            try {
                if (mContext!=null && !locationAvailability.isLocationAvailable()){
                    CDAdLog.d(TAG, "Location not available");
                    onLocationServiceNotAvailable(mContext);
                }
                super.onLocationAvailability(locationAvailability);
            } catch (Throwable throwable) {
                super.onLocationAvailability(locationAvailability);
                Utils.logStackTrace(throwable);
            }
        }
    }





}
