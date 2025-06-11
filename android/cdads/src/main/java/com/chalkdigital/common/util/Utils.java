package com.chalkdigital.common.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.chalkdigital.ads.CDAdErrorCode;
import com.chalkdigital.common.CDAdActions;
import com.chalkdigital.common.CDAdConstants;
import com.chalkdigital.common.CDAdDeviceInfo;
import com.chalkdigital.common.CDAdLocationManager;
import com.chalkdigital.common.CDTrackingManager;
import com.chalkdigital.common.CDTrackingService;
import com.chalkdigital.common.Constants;
import com.chalkdigital.common.DataKeys;
import com.chalkdigital.common.SharedPreferencesHelper;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.network.retrofit.CDAdCallback;
import com.chalkdigital.network.retrofit.CDAdParams;
import com.chalkdigital.network.retrofit.CDAdRetrofit;
import com.chalkdigital.network.retrofit.CDAdService;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class Utils {
    private static final AtomicLong sNextGeneratedId = new AtomicLong(1);

    public static String sha1(String string) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] bytes = string.getBytes("UTF-8");
            digest.update(bytes, 0, bytes.length);
            bytes = digest.digest();

            for (final byte b : bytes) {
                stringBuilder.append(String.format("%02X", b));
            }

            return stringBuilder.toString().toLowerCase(Locale.US);
        } catch (Exception e) {
                        Utils.logStackTrace(e);
            return "";
        }
    }

    /**
     * Adaptation of View.generateViewId() from API 17.
     * There is only a guarantee of ID uniqueness within a given session. Please do not store these
     * values between sessions.
     */
    public static long generateUniqueId() {
        for (;;) {
            final long result = sNextGeneratedId.get();
            long newValue = result + 1;
            if (newValue > Long.MAX_VALUE - 1) {
                newValue = 1;
            }
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    public static boolean bitMaskContainsFlag(final int bitMask, final int flag) {
        return (bitMask & flag) != 0;
    }

    public static float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    public static int convertDpToPixels(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int px = (int) (dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static long getCompessedDateStringFromTimeInUTC(long time){
        String date = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        date = dateFormat.format(new Date(time));
        return Long.parseLong(date);

    }

    public static int getInterstitialScale(Context context, String adWidth, String adHeight, String stretch, int orientation){
        Double width = 0.0;
        Double height = 0.0;
        try {
            width = Double.parseDouble(adWidth);
            height = Double.parseDouble(adHeight);
        } catch (NumberFormatException e) {
                        Utils.logStackTrace(e);
            width = 0.0;
            height = 0.0;
        } finally {
            if (width > 0.0 && height > 0.0){
                Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                Double w = new Double(display.getWidth());
                Double h = new Double(display.getHeight());
                if (orientation!= DeviceUtils.getScreenOrientation(context) && orientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED){
                    Double temp = h;
                    h = w;
                    w = temp;
                }
                Double widthScale = w/width;
                Double heightScale = h/height;
                Double scale = 100d;
                if (stretch.equals(CDAdConstants.StretchType.STRETCH_TYPE_ASPECT_FILL.toString())){
                    scale = Math.max(widthScale, heightScale) * 100d;
                }else if(stretch.equals(CDAdConstants.StretchType.STRETCH_TYPE_ASPECT_FIT.toString())){
                    scale = Math.min(widthScale, heightScale) * 100d;
                }
                return scale.intValue();
            }else{
                return 100;
            }
        }

    }

    public static void scaleLayoutParams(Context context, String adWidth, String adHeight, FrameLayout.LayoutParams layoutParams, String stretch, int orientation){
        Double width = 0.0;
        Double height = 0.0;
        try {
            width = Double.parseDouble(adWidth);
            height = Double.parseDouble(adHeight);
        } catch (NumberFormatException e) {
                        Utils.logStackTrace(e);
            width = 0.0;
            height = 0.0;
        } finally {
            if (width > 0.0 && height > 0.0 && stretch.equals(CDAdConstants.StretchType.STRETCH_TYPE_ASPECT_FILL.toString())){
                Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                Double w = new Double(display.getWidth());
                Double h = new Double(display.getHeight());
                if (orientation!= DeviceUtils.getScreenOrientation(context) && orientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED){
                    Double temp = h;
                    h = w;
                    w = temp;
                }
                Double widthScale = w/width;
                Double heightScale = h/height;
                Double scale = Math.max(widthScale, heightScale)/ Math.min(widthScale, heightScale);
                int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
                int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
                if (orientation!= DeviceUtils.getScreenOrientation(context) && orientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED){
                    int temp = screenHeight;
                    screenHeight = screenWidth;
                    screenWidth = temp;
                }
                if (widthScale>heightScale){
                    layoutParams.height = (int) (screenHeight *scale);
                    layoutParams.width = screenWidth;
                }else{
                    layoutParams.height = screenHeight;
                    layoutParams.width = (int) (screenWidth *scale);
                }
                layoutParams.gravity = Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL;
            }
        }

    }

    // For writing to a Parcel
    public <K extends Parcelable,V extends Parcelable> void writeParcelableMap(
            Parcel parcel, int flags, Map<K, V > map)
    {
        parcel.writeInt(map.size());
        for(Map.Entry<K, V> e : map.entrySet()){
            parcel.writeParcelable(e.getKey(), flags);
            parcel.writeParcelable(e.getValue(), flags);
        }
    }

    // For reading from a Parcel
    public <K extends Parcelable,V extends Parcelable> Map<K,V> readParcelableMap(
            Parcel parcel, Class<K> kClass, Class<V> vClass)
    {
        int size = parcel.readInt();
        Map<K, V> map = new HashMap<>(size);
        for(int i = 0; i < size; i++){
            map.put(kClass.cast(parcel.readParcelable(kClass.getClassLoader())),
                    vClass.cast(parcel.readParcelable(vClass.getClassLoader())));
        }
        return map;
    }

    public static void logStackTrace(Throwable t){

        try {
            HashMap<String, Object> params = new HashMap<>();
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            params.put(DataKeys.EXCEPTION, sw.toString());
            params.put(DataKeys.SDK_VER, CDAdConstants.CDAdSdkVersion);
            params.put(DataKeys.DEVICE_MAKE, Build.MANUFACTURER);
            params.put(DataKeys.DEVICE_MODEL, Build.MODEL);
            params.put(DataKeys.DEVICEOS, "Android");
            params.put(DataKeys.OS_VERSION, Build.VERSION.RELEASE);
            params.put(DataKeys.HARDWARE_VERSION, Build.DEVICE);
            params.put(DataKeys.SDK_BUILD_VERSION, CDAdConstants.CDAdBuildVersion);
            String debugUrl = "";
            try {
                Application application = getApplicationUsingReflection();
                if (application!=null){
                    Context context = application.getApplicationContext();
                    if (context !=null){
                        CDAdDeviceInfo cdAdDeviceInfo = CDAdDeviceInfo.deviceInfo(context);
                        params.put(DataKeys.LANGUAGE, cdAdDeviceInfo.getLanguage());
                        params.put(DataKeys.HEIGHT, cdAdDeviceInfo.getH());
                        params.put(DataKeys.WIDTH, cdAdDeviceInfo.getW());
                        params.put(DataKeys.CARRIER, cdAdDeviceInfo.getCarrier());
                        params.put(DataKeys.CONNECTION_TYPE, cdAdDeviceInfo.getConnectiontype());
                        params.put(DataKeys.PIXEL_RATIO, String.format("%.1f", cdAdDeviceInfo.getPxratio()));
                        ApplicationInfo applicationInfo = context.getApplicationInfo();
                        int stringId = applicationInfo.labelRes;
                        params.put(DataKeys.SITE, stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId));
                        params.put(DataKeys.BUNDLE, context.getPackageName());

                        try {
                            debugUrl = context.getResources().getText(context.getResources().getIdentifier("CDADS_DEBUG_URL", "string", context.getPackageName())).toString();
                        }catch (Throwable throwable){
                        }
                        String defaultVpaidUrl = "";
                        try {
                            defaultVpaidUrl = context.getResources().getText(context.getResources().getIdentifier("VPAID_SOURCE_URL", "string", context.getPackageName())).toString();
                        }catch (Throwable throwable){
                        }
                        params.put(DataKeys.VPAID_SOURCE_URL, SharedPreferencesHelper.getStringFromSharedPreferences(DataKeys.VPAID_SOURCE_URL, defaultVpaidUrl, context));

                        try {
                            params.put(DataKeys.PARTNERID, context.getResources().getText(context.getResources().getIdentifier("CDADS_PARTNER_ID", "string", context.getPackageName())).toString());
                        }catch (Exception e){
                            params.put(DataKeys.SERVER_PATH , CDAdErrorCode.CDADS_PARTNER_ID_NOT_CONFIGURED.toString());
                        }

                        try {
                            params.put(DataKeys.TESTINGBUNDLE, context.getResources().getText(context.getResources().getIdentifier("CDADS_BUNDLE_ID", "string", context.getPackageName())).toString());
                        }catch (Exception e){

                        }

                        try {
                            params.put(DataKeys.SERVER_URL , context.getResources().getText(context.getResources().getIdentifier("SERVER_URL", "string", context.getPackageName())).toString());
                        } catch (Exception e) {
                            params.put(DataKeys.SERVER_URL , CDAdErrorCode.CDADS_SERVER_URL_NOT_CONFIGURED.toString());
                        }

                        try {
                            params.put(DataKeys.SERVER_PATH , context.getResources().getText(context.getResources().getIdentifier("SERVER_PATH", "string", context.getPackageName())).toString());
                        } catch (Exception e) {
                            params.put(DataKeys.SERVER_PATH , CDAdErrorCode.CDADS_SERVER_PATH_NOT_CONFIGURED.toString());
                        }
                    }
                }
            }catch (Exception e){

            }
            if (debugUrl!=null && !debugUrl.equals("") && URLUtil.isValidUrl(debugUrl)) {
                if (!debugUrl.endsWith("/"))
                    debugUrl = debugUrl+"/";
                CDAdLog.d("Logging Exception", sw.toString());
                CDAdService.performCall(CDAdRetrofit.getsharedDebugRequestAPIInstance(debugUrl), "logException", Object.class, new Class[]{HashMap.class}, new Object[]{params}, new CDAdCallback<Object>(null, 0) {
                    @Override
                    public void onNetworkResponse(final Call<Object> call, final Response<Object> response, final Object object, final int apitype) {
                        CDAdLog.d(response.code()+"");
                    }

                    @Override
                    public void onNetworkFailure(final Call<Object> call, final Throwable t, final Object object, final int apitype) {
                    CDAdLog.d(t.getMessage());
                    }
                });
            }else{
                CDAdLog.d("Failed to Log Exception, Debug url not found", sw.toString());
            }
        } catch (Throwable throwable) {
            CDAdLog.d(throwable.getMessage());
        }
    }

    public static Application getApplicationUsingReflection() throws Exception {
        return (Application) Class.forName("android.app.AppGlobals")
                .getMethod("getInitialApplication").invoke(null, (Object[]) null);
    }

    public interface VpaidSourceListener{
        public void onSuccess(String html);
        public void onFailure();
    }
    public static void getSavedVpaidHTML(final String vpaidSourceUrl, final Context context, final VpaidSourceListener vpaidSourceListener){
        String savedSourceUrl = SharedPreferencesHelper.getStringFromSharedPreferences(DataKeys.VPAID_SOURCE_URL, "", context);
        Boolean isVpaidUrlValid = (vpaidSourceUrl!=null && !vpaidSourceUrl.equals("") && URLUtil.isValidUrl(vpaidSourceUrl));
        String url = null;
        if (isVpaidUrlValid && savedSourceUrl.equalsIgnoreCase(vpaidSourceUrl)){
            vpaidSourceListener.onSuccess(SharedPreferencesHelper.getStringFromSharedPreferences(DataKeys.VPAID_SOURCE_HTML, "", context));
            return;
        }
        else if (!isVpaidUrlValid && !savedSourceUrl.equals("")){
            vpaidSourceListener.onSuccess(SharedPreferencesHelper.getStringFromSharedPreferences(DataKeys.VPAID_SOURCE_HTML, "", context));
            return;
        }else if (!isVpaidUrlValid && savedSourceUrl.equals("")){
            try {
                url = context.getResources().getText(context.getResources().getIdentifier("VPAID_SOURCE_URL", "string", context.getPackageName())).toString();
            } catch (Resources.NotFoundException e) {
                CDAdLog.d("VPAID_SOURCE_URL not found in strings xml, using default value");
                url = CDAdParams.vpaidHtmlUrl;
            }
        }else{
            url = vpaidSourceUrl;
        }
        String serverUrl = null;
        try {
            serverUrl = context.getResources().getText(context.getResources().getIdentifier("SERVER_URL", "string", context.getPackageName())).toString();
        } catch (Resources.NotFoundException e) {
            CDAdLog.d("SERVER_URL not found in strings xml, using default value");
        }

        if (serverUrl==null){

            return;
        }
        CDAdService.performCall(CDAdRetrofit.getsharedSDKRequestAPIInstance(serverUrl+"/"), "fetchVpaidHTML", ResponseBody.class, new Class[]{String.class}, new Object[]{url}, new CDAdCallback<ResponseBody>(null, 0) {
            @Override
            public void onNetworkResponse(final Call<ResponseBody> call, final Response<ResponseBody> response, final Object object, final int apitype) {
                if (response!=null && response.body()!=null && response.body().contentLength()>0){
                    String vpaidHTML = convertResponseToString(response.body());
                    if (!vpaidHTML.equals("")) {
                        SharedPreferencesHelper.putStringToSharedPreferences(DataKeys.VPAID_SOURCE_URL, vpaidSourceUrl, context);
                        SharedPreferencesHelper.putStringToSharedPreferences(DataKeys.VPAID_SOURCE_HTML, vpaidHTML, context);
                        vpaidSourceListener.onSuccess(vpaidHTML);
                    }else{
                        vpaidSourceListener.onFailure();
                    }
                }
                else vpaidSourceListener.onFailure();
            }

            @Override
            public void onNetworkFailure(final Call<ResponseBody> call, final Throwable t, final Object object, final int apitype) {
                vpaidSourceListener.onFailure();
            }
        });

    }

    public static String convertResponseToString(ResponseBody body) {
        try {

            InputStream inputStream = null;

            StringBuilder sb = null;

            try {

                inputStream = body.byteStream();
                sb = new StringBuilder();

                int ch;
                while((ch = inputStream.read()) != -1)
                    sb.append((char)ch);



                return sb.toString();
            } catch (IOException e) {
                return "";
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (IOException e) {
            return "";
        }
    }

    public static void setDesktopMode(WebView webView, boolean enabled) {
//        String newUserAgent = webView.getSettings().getUserAgentString();
//        if (enabled) {
//            try {
//                String ua = webView.getSettings().getUserAgentString();
//                String androidOSString = webView.getSettings().getUserAgentString().substring(ua.indexOf("("), ua.indexOf(")") + 1);
//                newUserAgent = webView.getSettings().getUserAgentString().replace(androidOSString, "(X11; Linux x86_64)");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } else {
//            newUserAgent = null;
//        }
//
//        webView.getSettings().setUserAgentString(newUserAgent);
//        webView.getSettings().setUseWideViewPort(enabled);
//        webView.getSettings().setLoadWithOverviewMode(enabled);
//        webView.reload();
    }

    public static Activity getActivity(View view) {
        Context context = view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }

    public static Activity getActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }

    public static Activity getActivity() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);

            Map<Object, Object> activities = (Map<Object, Object>) activitiesField.get(activityThread);
            if (activities == null)
                return null;

            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    Activity activity = (Activity) activityField.get(activityRecord);
                    return activity;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static CDAdErrorCode checkMandatoryParams(Context context){
//        try {
//            context.getResources().getText(context.getResources().getIdentifier("CDADS_PARTNER_ID", "string", context.getPackageName())).toString();
//        } catch (Exception e) {
//            return CDAdErrorCode.CDADS_PARTNER_ID_NOT_CONFIGURED;
//        }
//
//        try {
//            context.getResources().getText(context.getResources().getIdentifier("CDADS_CAT", "string", context.getPackageName())).toString();
//        } catch (Exception e) {
//            return CDAdErrorCode.CDADS_CAT_NOT_CONFIGURED;
//        }

        try {
            context.getResources().getText(context.getResources().getIdentifier("SERVER_URL", "string", context.getPackageName())).toString();
        } catch (Exception e) {
            return CDAdErrorCode.CDADS_SERVER_URL_NOT_CONFIGURED;
        }

        try {
            context.getResources().getText(context.getResources().getIdentifier("SERVER_PATH", "string", context.getPackageName())).toString();
        } catch (Exception e) {
            return CDAdErrorCode.CDADS_SERVER_PATH_NOT_CONFIGURED;
        }
        return null;
    }

    public static String SHA256 (String text){

        String hashtext = "";
        try {
            // Static getInstance method is called with hashing SHA
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // digest() method called
            // to calculate message digest of an input
            // and return array of byte
            byte[] messageDigest = md.digest(text.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            hashtext = no.toString(16);

            while (hashtext.length() < 64) {
                hashtext = "0" + hashtext;
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Utils.logStackTrace(e);
        }

        return hashtext;
    }


    public static void saveUuids(ArrayList<String> uuids, Context context){
        Object obj = SharedPreferencesHelper.getObjectFromSharedPreferences(DataKeys.SSB_UUIDS, ArrayList.class, context);
        if (obj !=null && obj.getClass() == ArrayList.class && ((ArrayList
        )obj).size()>0){
            uuids.addAll((ArrayList)obj);
            if (uuids!=null && uuids.size()>0){
                SharedPreferencesHelper.putObjectToSharedPreferences(DataKeys.SSB_UUIDS, uuids, context);
            }
        }
    }

    public static ArrayList<String> getUuids(Context context){
        Object obj = SharedPreferencesHelper.getObjectFromSharedPreferences(DataKeys.SSB_UUIDS, ArrayList.class, context);
        if (obj !=null && obj.getClass() == ArrayList.class && ((ArrayList
                )obj).size()>0){
            return (ArrayList)obj;
        }
        return null;
    }

    public static void removeUuids(ArrayList<String> uuids, Context context){
        Object obj = SharedPreferencesHelper.getObjectFromSharedPreferences(DataKeys.SSB_UUIDS, ArrayList.class, context);
        if (obj !=null && obj.getClass() == ArrayList.class && ((ArrayList
                )obj).size()>0){
            ArrayList<String> savedList = (ArrayList)obj;
            savedList.removeAll(uuids);
            SharedPreferencesHelper.putObjectToSharedPreferences(DataKeys.SSB_UUIDS, savedList, context);
        }
    }

    public static String getLocationType(Location location){
        return (location.getProvider() == null || location.getProvider().equals(Constants.CDLocTypeIP))? Constants.CDLocTypeIP:(location.getProvider().equals(LocationManager.GPS_PROVIDER)||location.getProvider().equals(LocationManager.NETWORK_PROVIDER)||location.getProvider().equals(LocationManager.PASSIVE_PROVIDER))?Constants.CDLocTypeGPS:Constants.CDLocTypeOther;
    }


    public static void performNetworkPendingTasks(Context context){
        Intent newIntent;
        if (SharedPreferencesHelper.getBooleanFromSharedPreferences(DataKeys.IS_TRACK_DATA_REQUESTED, false, context)){
            CDTrackingManager.startTrackingService(context.getApplicationContext());
        }
        if (SharedPreferencesHelper.getBooleanFromSharedPreferences(DataKeys.IS_TRACKING_ENABLED, false, context)
                && SharedPreferencesHelper.getBooleanFromSharedPreferences(DataKeys.IS_TRACKING_PERMISSION_GRANTED, false, context)
                && !SharedPreferencesHelper.getBooleanFromSharedPreferences(DataKeys.IS_RECEIVING_CONTINUOUS_LOCATION_UPDATES, false, context)
                && !SharedPreferencesHelper.getBooleanFromSharedPreferences(DataKeys.IS_NEW_LOCATION_IN_PROGRESS, false, context)){
            int locationPermissionStatus = CDAdLocationManager.getLocationPermissionStatus(context);
            if (locationPermissionStatus==CDAdConstants.CDAdLocationPermissionGranted)
            {
                CDAdLocationManager.startLocationService(CDAdActions.NEW_LOCATION_REQUESTED, context);
            }else{
                CDAdLocationManager.startIPGeoLocationService(context);
            }
        }

    }

}
