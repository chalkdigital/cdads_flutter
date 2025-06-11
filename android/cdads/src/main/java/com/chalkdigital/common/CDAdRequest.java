package com.chalkdigital.common;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.location.Location;

import com.chalkdigital.ads.CDAds;
import com.chalkdigital.ads.CDAdsInitialisationParams;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.Utils;
import com.chalkdigital.nativeads.VideoConfiguration;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by arungupta on 07/12/16.
 */

public class CDAdRequest {

    /**
     * Set age for targeting user group. It's value is blank by default.
     */
    public String targetingAge;

    /**
     * Set gender for targeting user group. Use M for male, F for female and O for others. It's value is blank by default.
     */
    public String targetingGender;

    /**
     * Set interger income in USD for targeting user group. It's value is 0 by default.
     */
    public Integer targetingIncome;

    /**
     * Set education for targeting user group. It's value is blank by default.
     */
    public String targetingEducation;

    /**
     * Set language code for targeting user group. It's value is blank by default.
     */
    public String targetingLanguage;

    /**
     * Set keyword for targeting user group
     */
    public String keyword;


    private String ver;

    /**
     * Set locationAutoUpdateEnabled to automatic location update while requesting an ad, It's value is true by default
     */
    public Boolean locationAutoUpdateEnabled;

    /**
     * Set CDAdGeoInfo object for requesting an ad.
     */
    public CDAdGeoInfo geoInfo;
    public boolean onlySecureImpressionsAllowed;
    private Context mContext;

    /**
     * Set partner id of application in which this sdk is used
     */
    public String partnerId;

    /**
     * Set bundle id of application in which this sdk is used. It will only work when SDK is in testing mode.
     */
    public String bundleId;

    public boolean rewarded;

    /**
     * Set IAB category id of application in which this sdk is used. If your application comply multiple IAB ids then seprate them with comma (,).
     */
    public String cat;

    /**
     * Set publisher specific user id.
     */
    public String userId;

    /**
     * Set ad placement id
     */
    public String placementId;

    /**
     * Set testing to enable testing mode, use true to enable testing. Its value is false by default.
     */
    public boolean testing;

    /**
     * Set Video Configuration in case this request would be used for video ad.
     */
    public VideoConfiguration videoConfiguration;

    protected CDAdRequest(Context context) {
        this.ver = CDAdConstants.CDAdApiVersion;
        this.targetingAge = "";
        this.targetingEducation = "";
        this.targetingGender = "";
        this.targetingIncome = 0;
        this.targetingLanguage = "";
        this.locationAutoUpdateEnabled = true;
        this.geoInfo = new CDAdGeoInfo();
        this.keyword = "";
        this.mContext = context;
        this.onlySecureImpressionsAllowed = true;
        this.testing = false;
        this.cat = "";
        this.userId = "";
        this.placementId = "";
        this.rewarded = false;
        try {
            int id = context.getResources().getIdentifier("CDADS_PARTNER_ID", "string", context.getPackageName());
            if (id !=0)
                this.partnerId = context.getResources().getText(id).toString();
            else this.partnerId = "";
        } catch (Exception e) {
            Utils.logStackTrace(e);
            this.partnerId = "";
        }
        try {
            int id = context.getResources().getIdentifier("CDADS_BUNDLE_ID", "string", context.getPackageName());
            if (id > 0)
                this.bundleId = context.getResources().getText(id).toString();
            else this.bundleId = context.getApplicationContext().getPackageName();
        } catch (Exception e) {
            Utils.logStackTrace(e);
            this.bundleId = context.getApplicationContext().getPackageName();
        }
        try {
            int id = context.getResources().getIdentifier("CDADS_CAT", "string", context.getPackageName());
            if (id != 0)
                this.cat = context.getResources().getText(id).toString();
            else this.cat = "";
        } catch (Exception e) {
            Utils.logStackTrace(e);
            this.cat = "";
        }
    }

    public static class Builder{
        public CDAdRequest build(Context context){
            return new CDAdRequest(context);

        }
    }

    public boolean isReady(){
        if (geoInfo != null && !geoInfo.getCountryCode().equals(""))
            return true;
        if (!locationAutoUpdateEnabled){
            CDAdLog.i("CDAdRequest", "Location Auto Update is disabled on CDAdRequest. Either set complete geoinfo on CDAdRequest object or set locationAutoUpdateEnabled flag on CDAdRequest");
        }
        return false;
    }

    public HashMap<String, Object> getParams(){
        if (locationAutoUpdateEnabled)
            geoInfo = SharedPreferencesHelper.getObjectFromSharedPreferences(DataKeys.GEOINFO, CDAdGeoInfo.class, mContext.getApplicationContext());
        if (geoInfo==null){
            geoInfo = new CDAdGeoInfo();
            if (locationAutoUpdateEnabled){
                Location location = SharedPreferencesHelper.getObjectFromSharedPreferences(DataKeys.LAST_LOCATION, Location.class, mContext.getApplicationContext());
                if (location!=null){
                    geoInfo.setLat((float) location.getLatitude());
                    geoInfo.setLon((float) location.getLongitude());
                    geoInfo.setAccuracy(location.getAccuracy()+"");
                    try {
                        geoInfo.setType(Integer.parseInt(location.getProvider()));
                    } catch (NumberFormatException e) {
//                        Utils.logStackTrace(e);
                        geoInfo.setType(CDAdConstants.CDAdLocTypeDevice);

                    }
                }
            }
        }
        HashMap<String , Object> map = new HashMap<>();
        if (geoInfo.getCity()!=null)
            map.put(DataKeys.CITY, geoInfo.getCity());
        if (geoInfo.getRegion()!=null)
            map.put(DataKeys.STATE, geoInfo.getRegion());
        if (geoInfo.getZip()!=null)
            map.put(DataKeys.ZIP, geoInfo.getZip());
        if (geoInfo.getLat()!=null)
            map.put(DataKeys.LAT, geoInfo.getLat());
        if (geoInfo.getLon()!=null)
            map.put(DataKeys.LNG, geoInfo.getLon());
        if (geoInfo.getMetro()!=null)
            map.put(DataKeys.METRO, geoInfo.getMetro());
        if (geoInfo.getCountryCode()!=null)
            map.put(DataKeys.COUNTRY, geoInfo.getCountryCode());
        if (geoInfo.getType()!=CDAdConstants.CDAdLocTypeUnavailable)
            map.put(DataKeys.LOCTYPE, geoInfo.getType());
        if (geoInfo.getAccuracy()!=null)
            map.put(DataKeys.HORIZONTAL_ACCURACY, geoInfo.getAccuracy());
        map.put(DataKeys.PLACEMENTID, placementId);
        map.put(DataKeys.UIDTYPE, CDAdConstants.CDAdUidType);
        map.put(DataKeys.AGE, targetingAge);
        map.put(DataKeys.GENDER, targetingGender);
        map.put(DataKeys.INCOME, targetingIncome);
        map.put(DataKeys.EDUCATION, targetingEducation);
        map.put(DataKeys.USERID, userId);
        map.put(DataKeys.KEYWORD, keyword);
        map.put(DataKeys.VER, ver);
        map.put(DataKeys.FMT, "");
        map.put(DataKeys.SDK_VER, CDAdConstants.CDAdSdkVersion);
        map.put(DataKeys.CAT, cat);
        map.put(DataKeys.REWARDED, rewarded?1:0);
        map.put(DataKeys.SECURE, onlySecureImpressionsAllowed?1:0);
        map.put(DataKeys.BUNDLE, (testing && bundleId !=null && !bundleId.equals(""))?bundleId:mContext.getApplicationContext().getPackageName());
        map.put(DataKeys.PUB, partnerId);
        map.put(DataKeys.KEY, CDAds.runningInstance().getCdAdsInitialisationParams().getPartnerKey());
        map.put(DataKeys.PLAY_STORE_URL, "https://play.google.com/store/apps/details?id="+map.get(DataKeys.BUNDLE));
        map.put(DataKeys.REQ_ID, UUID.randomUUID().toString());
        map.put(DataKeys.SDK_BUILD_VERSION, CDAdConstants.CDAdBuildVersion);
        return map;

    }

    /**
     * Get name of application
     * @param context application context refrence
     * @return string value of app name in which this sdk is integrated.
     */
    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }
}
