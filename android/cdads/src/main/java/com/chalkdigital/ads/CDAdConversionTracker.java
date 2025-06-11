package com.chalkdigital.ads;

import android.content.Context;
import android.content.SharedPreferences;

import com.chalkdigital.common.BaseUrlGenerator;
import com.chalkdigital.common.ClientMetadata;
import com.chalkdigital.common.Constants;
import com.chalkdigital.common.SharedPreferencesHelper;
import com.chalkdigital.common.logging.CDAdLog;
//import com.chalkdigital.network.TrackingRequest;
//import com.chalkdigital.volley.VolleyError;

public class CDAdConversionTracker {
    private Context mContext;
    private String mIsTrackedKey;
    private SharedPreferences mSharedPreferences;
    private String mPackageName;

    public void reportAppOpen(Context context) {
        if (context == null) {
            return;
        }

        mContext = context;
        mPackageName = mContext.getPackageName();
        mIsTrackedKey = mPackageName + " tracked";
        mSharedPreferences = SharedPreferencesHelper.getSharedPreferences(mContext);

        if (!isAlreadyTracked()) {
//            TrackingRequest.makeTrackingHttpRequest(new ConversionUrlGenerator().generateUrlString(Constants.HOST),
////                    mContext, new TrackingRequest.Listener() {
////                @Override
////                public void onResponse(@NonNull String url) {
////                    mSharedPreferences
////                            .edit()
////                            .putBoolean(mIsTrackedKey, true)
////                            .commit();
////                }
////
////                @Override
////                public void onErrorResponse(final VolleyError volleyError) { }
////            });
        } else {
            CDAdLog.d("Conversion already tracked");
        }
    }

    private boolean isAlreadyTracked() {
        return mSharedPreferences.getBoolean(mIsTrackedKey, false);
    }

    private class ConversionUrlGenerator extends BaseUrlGenerator {
        @Override
        public String generateUrlString(String serverHostname) {
            initUrlString(serverHostname, Constants.CONVERSION_TRACKING_HANDLER);
            setApiVersion("6");
            setPackageId(mPackageName);
            ClientMetadata clientMetadata = ClientMetadata.getInstance(mContext);
            setAppVersion(clientMetadata.getAppVersion());
            appendAdvertisingInfoTemplates();
            return getFinalUrlString();
        }

        private void setPackageId(String packageName) {
            addParam("id", packageName);
        }
    }
}
