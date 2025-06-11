package com.chalkdigital.ads;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkRequest;
import android.os.Build;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.webkit.WebView;
import com.chalkdigital.common.CDAdActions;
import com.chalkdigital.common.CDAdConstants;
import com.chalkdigital.common.CDAdLocationManager;
import com.chalkdigital.common.CDAdNetworkConnectivityCallback;
import com.chalkdigital.common.CDAdPermissionActivity;
import com.chalkdigital.common.CDTrackingManager;
import com.chalkdigital.common.CDTrackingService;
import com.chalkdigital.common.DataKeys;
import com.chalkdigital.common.SharedPreferencesHelper;
import com.chalkdigital.common.logging.CDAdLog;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;

import java.lang.ref.WeakReference;

/**
 * Created by arungupta on 07/12/16.
 */

public class CDAds {
    private boolean limitedTrackingEnabled;
    private CDAdsInitialisationParams cdAdsInitialisationParams;
    private static CDAds sharedCDAdsInstance;
    private Context mApplicationContext;
    private WeakReference<CDAdsListener> mCDAdsListenerWeakReference;
    private CDSDKAuthenticationReceiver mCDSdkAuthenticationReceiver;
    private CDSDKPermissionReceiver mCDSDKPermissionReceiver;
    private CDApplicationStateListener mCDApplicationStateListener;
    private CDApplicationStateMonitor mCDApplicationStateMonitor;

    private CDAdNetworkConnectivityCallback mCDAdNetworkConnectivityCallback;
    boolean isCDPermissionActivityVisible;
    private String mRequestAction;
    private static boolean isApplicationNewInstanceCreated;

    private CDAds(Application application) {
        super();
        isApplicationNewInstanceCreated = true;
        CDApplicationStateMonitor.init(application);
        mApplicationContext = application.getApplicationContext();
        mCDApplicationStateListener = new ApplicationStateListener();
        mCDApplicationStateMonitor = CDApplicationStateMonitor.get();
        mCDApplicationStateMonitor.addListener(mCDApplicationStateListener);
        isCDPermissionActivityVisible = false;
        mRequestAction = CDAdActions.NEW_LOCATION_REQUESTED;
        initialiseAdvertisingIdentifiers();
        DisplayMetrics displaymetrics = mApplicationContext.getResources().getDisplayMetrics();
        float pxratio = displaymetrics.density;
        SharedPreferencesHelper.putFloatToSharedPreferences(DataKeys.PIXEL_RATIO, pxratio, mApplicationContext);
        SharedPreferencesHelper.putIntegerToSharedPreferences(DataKeys.HEIGHT, (int) (displaymetrics.heightPixels / pxratio), mApplicationContext);
        SharedPreferencesHelper.putIntegerToSharedPreferences(DataKeys.WIDTH, (int) (displaymetrics.widthPixels / pxratio), mApplicationContext);

        try {
            if (SharedPreferencesHelper.getStringFromSharedPreferences(DataKeys.UA, "", mApplicationContext).equals("")){
                WebView webView = new WebView(mApplicationContext);
                String user_agent = webView.getSettings().getUserAgentString();
                SharedPreferencesHelper.putStringToSharedPreferences(DataKeys.UA, user_agent, mApplicationContext);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SharedPreferencesHelper.putStringToSharedPreferences(DataKeys.UA, SharedPreferencesHelper.getStringFromSharedPreferences(DataKeys.UA, "", mApplicationContext), mApplicationContext);
        }
        SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_TRACKING_ENABLED, true, mApplicationContext);
        SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_TRACKING_PERMISSION_GRANTED, true, mApplicationContext);
    }

    public static CDAds initialiseWithParams(CDAdsInitialisationParams cdAdsInitialisationParams, Application application) {
        if (sharedCDAdsInstance == null) {
            sharedCDAdsInstance = new CDAds(application);
        }
        sharedCDAdsInstance.cdAdsInitialisationParams = cdAdsInitialisationParams;
        return sharedCDAdsInstance;
    }

    public static CDAds runningInstance() {
        return sharedCDAdsInstance;
    }


    public void start() {
        if (isApplicationNewInstanceCreated){
            performSDKStartOperations();
        }else{
            CDAdLocationManager.stopContinuousLocationUpdates(mApplicationContext);
        }
    }

    private void performSDKStartOperations() {
        if (mCDAdNetworkConnectivityCallback==null){
            mCDAdNetworkConnectivityCallback = new CDAdNetworkConnectivityCallback(mApplicationContext);
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(mApplicationContext, ConnectivityManager.class);
            connectivityManager.requestNetwork(new NetworkRequest.Builder().build(),mCDAdNetworkConnectivityCallback);
//            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(mApplicationContext, ConnectivityManager.class);
//            connectivityManager.unregisterNetworkCallback(mCDAdNetworkConnectivityCallback);
        }
        if (SharedPreferencesHelper.getBooleanFromSharedPreferences(DataKeys.IS_TRACKING_ENABLED, true, mApplicationContext)) {
            if (!SharedPreferencesHelper.getBooleanFromSharedPreferences(DataKeys.IS_TRACKING_PERMISSION_GRANTED, false, mApplicationContext) && !SharedPreferencesHelper.getBooleanFromSharedPreferences(DataKeys.IS_TRACKING_PERMISSION_DENIED, false, mApplicationContext)) {
                if (mCDSDKPermissionReceiver == null) {
                    mCDSDKPermissionReceiver = new CDSDKPermissionReceiver();
                }
                IntentFilter filter = new IntentFilter(CDAdActions.TRACKING_PERMISSION_GRANTED);
                filter.addAction(CDAdActions.TRACKING_PERMISSION_DENIED);
                LocalBroadcastManager.getInstance(mApplicationContext).registerReceiver(mCDSDKPermissionReceiver, filter);
                Intent intent = new Intent(mApplicationContext, CDAdPermissionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(CDAdActions.TRACKING_PERMISSION_REQUESTED);
                mApplicationContext.startActivity(intent);
            } else {
                SharedPreferencesHelper.putLongToSharedPreferences(DataKeys.AD_LOCATION_EXPIRY_INTERVAL, cdAdsInitialisationParams.getAdLocationExpiryInterval(), mApplicationContext);
                SharedPreferencesHelper.putLongToSharedPreferences(DataKeys.LOCATION_UPDATE_INTERVAL, cdAdsInitialisationParams.getLocationUpdateInterval(), mApplicationContext);
                SharedPreferencesHelper.putFloatToSharedPreferences(DataKeys.DISTANCE_FILTER, cdAdsInitialisationParams.getDistanceFilter(), mApplicationContext);
//                if (mCDSdkAuthenticationReceiver == null)
//                    mCDSdkAuthenticationReceiver = new CDSDKAuthenticationReceiver();
//                IntentFilter filter = new IntentFilter(CDAdActions.SDK_AUTHENTICATED);
                CDTrackingManager.startTrackingService(mApplicationContext);
                if (SharedPreferencesHelper.getBooleanFromSharedPreferences(DataKeys.IS_TRACKING_ENABLED, true, mApplicationContext) && SharedPreferencesHelper.getBooleanFromSharedPreferences(DataKeys.IS_TRACKING_PERMISSION_GRANTED, false, mApplicationContext)) {
                    checkPermissionsAndStartLocationService(CDAdActions.CONTINUOUS_LOCATION_REQUESTED);
                }
                isApplicationNewInstanceCreated = false;
            }
        }

    }

    void checkPermissionsAndStartLocationService(String requestAction) {
        int locationPermissionStatus = CDAdLocationManager.getLocationPermissionStatus(mApplicationContext);

        if (locationPermissionStatus != CDAdConstants.CDAdLocationPermissionUnKnown) {
            CDAdLocationManager.startLocationService(requestAction, mApplicationContext);
        } else if(Build.VERSION.SDK_INT >= 23){
            if (mCDSDKPermissionReceiver == null) {
                mCDSDKPermissionReceiver = new CDSDKPermissionReceiver();
            }
            setRequestAction(requestAction);
            if (!isCDPermissionActivityVisible) {
                isCDPermissionActivityVisible = true;
                IntentFilter filter = new IntentFilter(CDAdActions.LOCATION_PERMISSION_GRANTED);
                filter.addAction(CDAdActions.LOCATION_PERMISSION_DENIED);
                LocalBroadcastManager.getInstance(mApplicationContext).registerReceiver(mCDSDKPermissionReceiver, filter);
                Intent intent = new Intent(mApplicationContext, CDAdPermissionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(CDAdActions.LOCATION_PERMISSION_REQUESTED);
                mApplicationContext.startActivity(intent);
            }
        }

    }

    public void setRequestAction(String requestAction) {
        if (requestAction.equals(CDAdActions.CONTINUOUS_LOCATION_REQUESTED))
            mRequestAction = requestAction;
    }



    public CDAdsInitialisationParams getCdAdsInitialisationParams() {
        return cdAdsInitialisationParams;
    }

    public void setCdAdsInitialisationParams(CDAdsInitialisationParams cdAdsInitialisationParams) {
        this.cdAdsInitialisationParams = cdAdsInitialisationParams;
    }


    public void setEnableTracking(boolean enableTracking) {
        SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_TRACKING_ENABLED, enableTracking, mApplicationContext);
    }

    public boolean isLimitedTrackingEnabled() {
        return limitedTrackingEnabled;
    }

    public void setLimitedTrackingEnabled(boolean limitedTrackingEnabled) {
        this.limitedTrackingEnabled = limitedTrackingEnabled;
    }

    public CDAdsListener getCdAdsListener() {
        if (mCDAdsListenerWeakReference!=null){
            return mCDAdsListenerWeakReference.get();
        }
        return null;
    }

    public void setCdAdsListener(CDAdsListener cdAdsListener) {
        this.mCDAdsListenerWeakReference = new WeakReference<CDAdsListener>(cdAdsListener);
    }

    private class CDSDKAuthenticationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (SharedPreferencesHelper.getBooleanFromSharedPreferences(DataKeys.IS_TRACKING_ENABLED, true, mApplicationContext) && SharedPreferencesHelper.getBooleanFromSharedPreferences(DataKeys.IS_TRACKING_PERMISSION_GRANTED, false, mApplicationContext) && mCDApplicationStateMonitor.isForeground()) {
                checkPermissionsAndStartLocationService(CDAdActions.CONTINUOUS_LOCATION_REQUESTED);
            }
            LocalBroadcastManager.getInstance(mApplicationContext).unregisterReceiver(mCDSdkAuthenticationReceiver);
        }
    }

    private class CDSDKPermissionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case CDAdActions.LOCATION_PERMISSION_GRANTED:
                    CDAdLocationManager.startLocationService(mRequestAction, mApplicationContext);
                    break;
                case CDAdActions.LOCATION_PERMISSION_DENIED:
                    SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_LOCATION_PROMPT_RESPONDED_BY_USER, true, mApplicationContext);
                    CDAdLocationManager.startIPGeoLocationService(mApplicationContext);
                    break;
                case CDAdActions.TRACKING_PERMISSION_GRANTED:
                    SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_TRACKING_PERMISSION_GRANTED, true, mApplicationContext);
                    SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_TRACKING_PERMISSION_DENIED, false, mApplicationContext);
                    start();
                    break;
                case CDAdActions.TRACKING_PERMISSION_DENIED:
                    SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_TRACKING_PERMISSION_GRANTED, false, mApplicationContext);
                    SharedPreferencesHelper.putBooleanToSharedPreferences(DataKeys.IS_TRACKING_PERMISSION_DENIED, true, mApplicationContext);
                    start();
                default:
                    break;
            }
            LocalBroadcastManager.getInstance(mApplicationContext).unregisterReceiver(mCDSDKPermissionReceiver);
        }
    }


    private void initialiseAdvertisingIdentifiers() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String adid = "";
                int isLimitedTrackingEnabled = 1;
                try {
                    AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(mApplicationContext);
                    if (adInfo != null) {
                        adid = adInfo.getId();
                        isLimitedTrackingEnabled = adInfo.isLimitAdTrackingEnabled() ? 1 : 0;
                    }
                    // Use the advertising id
                } catch (Exception exception) {
                    CDAdLog.d("Feching ADID", exception.getMessage());
                } finally {
                    SharedPreferencesHelper.putStringToSharedPreferences(DataKeys.UID, adid, mApplicationContext);
                    SharedPreferencesHelper.putIntegerToSharedPreferences(DataKeys.LIMITED_ADVERTSER_TRACKING, isLimitedTrackingEnabled, mApplicationContext);
                }
            }
        }).start();
    }

    private class ApplicationStateListener implements CDApplicationStateListener{

        @Override
        public void onBecameForeground() {
            CDAds.this.start();
        }

        @Override
        public void onBecameBackground() {
            CDAdLocationManager.stopContinuousLocationUpdates(mApplicationContext);
            isApplicationNewInstanceCreated = true;
        }
    }
}
