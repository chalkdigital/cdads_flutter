package com.chalkdigital.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.Utils;

public class CDAdBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent!=null && intent.getAction()!=null){
                switch (intent.getAction()){
                    case Intent.ACTION_BOOT_COMPLETED:
                        CDAdLog.d("CDBootReceiver", "Device booted");
                        CDTrackingManager.startTrackingService(context.getApplicationContext());

                }
            }
        } catch (Throwable throwable) {
            Utils.logStackTrace(throwable);
        }
    }
}
