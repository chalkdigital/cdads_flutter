package com.chalkdigital.ads;

import android.content.Context;
import android.util.Log;

import com.chalkdigital.common.CDAdConstants;
import com.chalkdigital.common.DataKeys;
import com.chalkdigital.common.SharedPreferencesHelper;

/**
 * Created by arungupta on 20/12/16.
 */


public class CDAdsInitialisationParams {
    private float distanceFilter;
    private long locationUpdateInterval;
    private long adLocationExpiryInterval;
    private CDDefines.CDADProvider provider;
    private CDDefines.CDEnvironment environment;
    private Context mContext;
    private String applicationIABCategory;
    private String siteId;
    private String partnerKey;

    public CDAdsInitialisationParams(Context context) {
        mContext = context;
        distanceFilter = SharedPreferencesHelper.getFloatFromSharedPreferences(DataKeys.CDAdDistanceFilterKey, CDAdConstants.CDAdDistanceFilter, context.getApplicationContext());
        locationUpdateInterval = SharedPreferencesHelper.getLongFromSharedPreferences(DataKeys.CDAdTrackingIntervalKey, CDAdConstants.CDAdTrackingInterval, context.getApplicationContext());
        adLocationExpiryInterval = SharedPreferencesHelper.getLongFromSharedPreferences(DataKeys.CDAdLocationExpiryIntervalKey, CDAdConstants.CDAdLocationExpiryInterval, context.getApplicationContext());
        setLogLevel(Log.ASSERT);
        environment = CDDefines.CDEnvironment.CDEnvironmentTest;
        provider = CDDefines.CDADProvider.CDAD_PROVIDER_Chalkdigital;
        applicationIABCategory = "";
        siteId = "";
        partnerKey = "";
    }

    public float getDistanceFilter() {
        return distanceFilter;
    }

    public void setDistanceFilter(float distanceFilter) {
        this.distanceFilter = distanceFilter;
    }

    public long getLocationUpdateInterval() {
        return locationUpdateInterval;
    }

    public void setLocationUpdateInterval(long locationUpdateInterval) {
        this.locationUpdateInterval = locationUpdateInterval;
    }

    public long getAdLocationExpiryInterval() {
        return adLocationExpiryInterval;
    }

    public void setAdLocationExpiryInterval(long adLocationExpiryInterval) {
        this.adLocationExpiryInterval = adLocationExpiryInterval;
    }

    public void setLogLevel(int logLevel) {
        SharedPreferencesHelper.putIntegerToSharedPreferences(DataKeys.CD_LOG_LEVEL, logLevel, mContext.getApplicationContext());
    }

    public CDDefines.CDADProvider getProvider() {
        return provider;
    }

    public void setProvider(CDDefines.CDADProvider provider) {
        this.provider = provider;
    }

    public CDDefines.CDEnvironment getEnvironment() {
        return environment;
    }

    public void setEnvironment(CDDefines.CDEnvironment environment) {
        this.environment = environment;
    }

    public String getApplicationIABCategory() {
        return applicationIABCategory;
    }

    public void setApplicationIABCategory(String applicationIABCategory) {
        this.applicationIABCategory = applicationIABCategory;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getPartnerKey() {
        return partnerKey;
    }

    public void setPartnerKey(String partnerKey) {
        this.partnerKey = partnerKey;
    }
}
