package com.chalkdigital.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.DeviceUtils;
import com.chalkdigital.common.util.Utils;

/**
 * Created by arungupta on 25/09/17.
 */

public class CDAdConnectivityChangeReceiver extends BroadcastReceiver {

    public static String IS_NETWORK_CONNECTED = "isNetworkConnected";
    @Override
    public void onReceive(Context context, Intent intent) {
        CDAdLog.d("CDAdConnectivityChangeReceiver","Started Reverse Geocoding Service");
        try {
            if (intent!=null && intent.getAction()!=null){
                switch (intent.getAction()){
                    case ConnectivityManager.CONNECTIVITY_ACTION:
                        postNetworkChangeNotification(DeviceUtils.isNetworkAvailable(context), context);
                        break;
                }
            }
        }  catch (Throwable throwable) {
            Utils.logStackTrace(throwable);
        }
    }

    public static void postNetworkChangeNotification(boolean isConnected, Context context){
        try {
            if (isConnected) {
                SharedPreferencesHelper.putStringToSharedPreferences(DataKeys.PUBLIC_IP, "", context);
                if (CDAdsUtils.isIsGeoIpLocationEnabled() && CDAdLocationManager.getLocationPermissionStatus(context)!=CDAdConstants.CDAdLocationPermissionGranted){
                    CDAdIPGeolocationManager.startIPGeolocationService(CDAdActions.NEW_IP_LOCATION_REQUESTED, context);
                }
            }
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putBoolean(IS_NETWORK_CONNECTED, isConnected);
            intent.setAction(CDAdActions.CDAdNotifyNetworkReachabilityChanged);
            intent.putExtras(bundle);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            Utils.performNetworkPendingTasks(context);
            CDAdLog.d("CDAdUtil", "postingNetworkChangeNotification connection status: "+(isConnected?"connected":"not connected"));
        } catch (Throwable throwable) {
            Utils.logStackTrace(throwable);
        }
    }
}
