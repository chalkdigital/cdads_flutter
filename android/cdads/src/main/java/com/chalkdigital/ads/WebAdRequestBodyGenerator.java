package com.chalkdigital.ads;

import android.content.Context;

import com.chalkdigital.common.CDAdDeviceInfo;
import com.chalkdigital.common.CDAdRequest;
import com.chalkdigital.common.CDAdsUtils;
import com.chalkdigital.common.AdRequestBodyGenerator;
import com.chalkdigital.common.ClientMetadata;
import com.chalkdigital.common.Constants;
import com.chalkdigital.common.DataKeys;
import com.chalkdigital.common.SharedPreferencesHelper;
import com.chalkdigital.nativeads.VideoConfiguration;

import java.util.HashMap;
import java.util.Map;

import static com.chalkdigital.common.ExternalViewabilitySessionManager.ViewabilityVendor;

public class WebAdRequestBodyGenerator extends AdRequestBodyGenerator {
    private final boolean mIsStorePictureSupported;

    public WebAdRequestBodyGenerator(Context context, boolean isStorePictureSupported) {
        super(context);
        mIsStorePictureSupported = isStorePictureSupported;
    }

    @Override
    public String generateUrlString(String serverHostname) {
        initUrlString(serverHostname, Constants.AD_HANDLER);

        setApiVersion("6");

        final ClientMetadata clientMetadata = ClientMetadata.getInstance(mContext);
        addBaseParams(clientMetadata);

        setMraidFlag(true);

        setExternalStoragePermission(mIsStorePictureSupported);

        enableViewability(ViewabilityVendor.getEnabledVendorKey());

        return getFinalUrlString();
    }

    @Deprecated
    public AdRequestBodyGenerator withFacebookSupported(boolean enabled) {
        return this;
    }

    private static final String PUBLISHER_KEY = "publisher";
    private static final String ID_KEY = "id";
    private static final String FORMAT_KEY = "format";
    private static final String BANNER_KEY = "banner";
    private static final String VIDEO_KEY = "video";
    private static final String INSTL_KEY = "instl";
    private static final String IMP_KEY = "imp";
    private static final String API_KEY = "api";
    private static final String NAME_KEY = "name";
    private static final String DOMAIN_KEY = "domain";
    private static final String STOREURL_KEY = "storeurl";
    private static final String APP_KEY = "app";
    private static final String REGION_KEY = "region";
    private static final String LAT_KEY = "lat";
    private static final String LON_KEY = "lon";
    private static final String TYPE_KEY = "type";
    public static final String GEO_KEY = "geo";
    private static final String CONNECTIONTYPE_KEY = "connectiontype";
    private static final String IFA_KEY = "ifa";
    private static final String YOB_KEY = "yob";
    private static final String OSV_KEY = "osv";
    private static final String HWV_KEY = "hwv";
    private static final String DEVICE_KEY = "device";
    private static final String USER_KEY = "user";
    private static final String DISPLAY_MANAGER = "displaymanager";
    private static final String DISPLAY_MANAGER_VER = "displaymanagerver";
    private static final String TAG_TD = "tagid";
    private static final String EXT = "ext";
    private static final String GDPR = "gdpr";
    private static final String CONSENT = "consent";
    private static final String REGS = "regs";
    private static final String TEST = "test";


    public Map<String, Object> bodyWithParams(Map<String, Object> params, boolean testing, Context context){
        Integer adType = (Integer) params.get(DataKeys.AD_TYPE);

        HashMap<String, Object> dict = new HashMap<String , Object>();
        dict.put(PUBLISHER_KEY, params.get(DataKeys.PUB));
        dict.put(DataKeys.KEY, params.get(DataKeys.KEY));
        dict.put(DataKeys.REQ_ID, params.get(DataKeys.REQ_ID));
        dict.put(DataKeys.VER, params.get(DataKeys.VER));
        dict.put(TEST, new Integer(testing?1:0));

        HashMap<String, Object> imp = new HashMap<String , Object>();
        imp.put(ID_KEY, "1");

        HashMap<String, Object> format = new HashMap<String , Object>();
        format.put(DataKeys.HEIGHT, params.get(DataKeys.HEIGHT));
        format.put(DataKeys.WIDTH, params.get(DataKeys.WIDTH));

        Object[] formatArray = {format};
        if (adType <2){
            Integer[] api = {3,5};
            HashMap<String, Object> banner = new HashMap<String , Object>();
            banner.put(FORMAT_KEY, formatArray);
            banner.put(API_KEY, api);
            imp.put(BANNER_KEY, banner);
        }
        else if (adType <4){
            HashMap<String, Object> video = new HashMap<String , Object>();
//            video.put(FORMAT_KEY, formatArray);
            Integer[] api = {1,2};
            video.put(API_KEY, api);
            video.put(DataKeys.HEIGHT, params.get(DataKeys.HEIGHT));
            video.put(DataKeys.WIDTH, params.get(DataKeys.WIDTH));
            VideoConfiguration videoConfiguration = (VideoConfiguration)params.get(DataKeys.VIDEO_CONF);
            video.put(DataKeys.LINEARITY, videoConfiguration.getLinearity());
            video.put(DataKeys.SKIP, videoConfiguration.getSkip());
            video.put(DataKeys.SKIP_AFTER, videoConfiguration.getSkipAfter());
            video.put(DataKeys.SKIP_MIN, videoConfiguration.getSkipMin());
            video.put(DataKeys.START_DELAY, videoConfiguration.getStartDelay());
            video.put(DataKeys.MIN_DURATION, videoConfiguration.getMinDuration());
            video.put(DataKeys.MAX_DURATION, videoConfiguration.getMaxDuration());
            video.put(DataKeys.MIN_BITRATE, videoConfiguration.getMinBitrate());
            video.put(DataKeys.MAX_BITRATE, videoConfiguration.getMaxBitrate());
            video.put(DataKeys.MIME_TYPES, new String[]{"video/mp4", "video/3gpp"});
            video.put(DataKeys.PROTOCOLS, new Integer[]{1,2,3,4,5,6});
//            HashMap<String, Object> ext = new HashMap<>();
//            ext.put(DataKeys.REWARDED, params.get(DataKeys.REWARDED));
//            video.put(EXT, ext);


            imp.put(VIDEO_KEY, video);
        }

        HashMap<String, Object> regs = new HashMap<>();
        HashMap<String, Integer> ext = new HashMap<>();
        ext.put(GDPR, CDAdsUtils.isGDPREnabled()?1:0);
        regs.put(EXT, ext);
        imp.put(REGS, regs);
        imp.put(DISPLAY_MANAGER, Constants.DISPLAY_MANAGER_NAME);
        imp.put(DISPLAY_MANAGER_VER, params.get(DataKeys.SDK_VER));
        imp.put(INSTL_KEY, adType%2);
        imp.put(TAG_TD, params.get(DataKeys.PLACEMENTID));
        imp.put(DataKeys.SECURE, params.get(DataKeys.SECURE));
        HashMap<String, Object>[] imps = new HashMap[1];
        imps[0] = imp;
        dict.put(IMP_KEY, imps);
        HashMap<String, Object> app = new HashMap<String , Object>();
        app.put(DataKeys.BUNDLE, params.get(DataKeys.BUNDLE));
        app.put(NAME_KEY, CDAdRequest.getApplicationName(mContext));
        app.put(DOMAIN_KEY, "");
        app.put(STOREURL_KEY, params.get(DataKeys.PLAY_STORE_URL));
        app.put(DataKeys.CAT, ((String)params.get(DataKeys.CAT)).split(","));
        dict.put(APP_KEY, app);

        CDAdDeviceInfo cdAdDeviceInfo = CDAdDeviceInfo.deviceInfo(mContext);
        HashMap<String, Object> device = new HashMap<String , Object>();
        HashMap<String, Object> geo = new HashMap<String , Object>();
        if (params.keySet().contains(DataKeys.LAT))
            geo.put(LAT_KEY, params.get(DataKeys.LAT));
        if (params.keySet().contains(DataKeys.LNG))
            geo.put(LON_KEY, params.get(DataKeys.LNG));
        if (params.keySet().contains(DataKeys.LOCTYPE))
            geo.put(TYPE_KEY, params.get(DataKeys.LOCTYPE));
        if (params.keySet().contains(DataKeys.COUNTRY))
            geo.put(DataKeys.COUNTRY, params.get(DataKeys.COUNTRY));
        if (params.keySet().contains(DataKeys.STATE))
            geo.put(REGION_KEY, params.get(DataKeys.STATE));
        if (params.keySet().contains(DataKeys.CITY))
            geo.put(DataKeys.CITY, params.get(DataKeys.CITY));
        if (params.keySet().contains(DataKeys.ZIP))
            geo.put(DataKeys.ZIP, params.get(DataKeys.ZIP));
        if (params.keySet().contains(DataKeys.METRO))
            geo.put(DataKeys.METRO, params.get(DataKeys.METRO));
        if (geo.size()>0)
            device.put(GEO_KEY, geo);
        device.put(DataKeys.UA, cdAdDeviceInfo.getUa());
        device.put(DataKeys.DNT, cdAdDeviceInfo.getLmt());
        device.put(DataKeys.IP, SharedPreferencesHelper.getStringFromSharedPreferences(DataKeys.PUBLIC_IP, "", context));
        device.put(DataKeys.IP, SharedPreferencesHelper.getStringFromSharedPreferences(DataKeys.PUBLIC_IP, "", context));
        device.put(DataKeys.DEVICE_TYPE, cdAdDeviceInfo.getDevicetype());
        device.put(DataKeys.DEVICE_MAKE, cdAdDeviceInfo.getMake());
        device.put(DataKeys.DEVICE_MODEL, cdAdDeviceInfo.getModel());
        device.put(DataKeys.OS, cdAdDeviceInfo.getOs());
        device.put(OSV_KEY, cdAdDeviceInfo.getOsv());
        device.put(HWV_KEY, cdAdDeviceInfo.getHwv());
        device.put(DataKeys.LANGUAGE, cdAdDeviceInfo.getLanguage());
        device.put(DataKeys.HEIGHT, cdAdDeviceInfo.getH());
        device.put(DataKeys.WIDTH, cdAdDeviceInfo.getW());
        device.put(DataKeys.CARRIER, cdAdDeviceInfo.getCarrier());
        device.put(CONNECTIONTYPE_KEY, cdAdDeviceInfo.getConnectiontype());
        device.put(IFA_KEY, cdAdDeviceInfo.getAdid());
//        device.put(DataKeys.UIDTYPE, params.get(DataKeys.UIDTYPE));
        device.put(DataKeys.PIXEL_RATIO, String.format("%.1f", cdAdDeviceInfo.getPxratio()));
        device.put(DataKeys.JS_ENABLED, cdAdDeviceInfo.getJs()+"");
        dict.put(DEVICE_KEY, device);

        HashMap<String, Object> user = new HashMap<String , Object>();
        if (params.get(DataKeys.USERID) == null || params.get(DataKeys.USERID).equals("")){
            user.put(ID_KEY, SharedPreferencesHelper.getStringFromSharedPreferences("ckuid", "", mContext));
        }else
            user.put(ID_KEY, params.get(DataKeys.USERID));
        user.put(YOB_KEY, params.get(DataKeys.AGE));
        user.put(DataKeys.GENDER, params.get(DataKeys.GENDER));
        user.put(DataKeys.INCOME, params.get(DataKeys.INCOME));
        user.put(DataKeys.EDUCATION, params.get(DataKeys.EDUCATION));
        user.put(DataKeys.KEYWORD, params.get(DataKeys.KEYWORD));
        HashMap<String, Integer> ext1 = new HashMap<>();
        ext1.put(CONSENT, CDAdsUtils.isConsentProvided()?1:0);
        user.put(EXT, ext1);
        dict.put(USER_KEY, user);

        return dict;
    }
}
