package com.chalkdigital.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;

import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.Utils;
    public class CDAdNetworkConnectivityCallback extends ConnectivityManager.NetworkCallback{
        private Context mContext;
        private static final String TAG = "CDAdConnectivityService";

        public CDAdNetworkConnectivityCallback(Context context) {
            super();
            mContext = context;
        }

        @Override
        public void onAvailable(Network network) {
            super.onAvailable(network);
            CDAdConnectivityChangeReceiver.postNetworkChangeNotification(true, mContext);
            Utils.performNetworkPendingTasks(mContext);
            CDAdLog.d(TAG, "network available");
        }

        @Override
        public void onUnavailable() {
            super.onUnavailable();
            CDAdConnectivityChangeReceiver.postNetworkChangeNotification(false, mContext);
            CDAdLog.d(TAG, "network not available");
        }
    }
