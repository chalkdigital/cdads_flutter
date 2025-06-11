package com.chalkdigital.common;

import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.chalkdigital.common.util.Utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Locale;

/**
 * Created by arungupta on 07/12/16.
 */

public class CDAdDeviceInfo {
    private String make;
    private String model;
    private String os;
    private String osv;
    private String hwv;
    private Context context;
    private static CDAdDeviceInfo sharedDeviceInfo;
    private String mUserAgent;
    private static final String DEFAULT_USER_AGENT = System.getProperty("http.agent");
    /** Unknown network class. */
    private static final int NETWORK_CLASS_UNKNOWN = 0;
    /** Class of broadly defined "2G" networks. */
    private static final int NETWORK_CLASS_2_G = 1;
    /** Class of broadly defined "3G" networks. */
    private static final int NETWORK_CLASS_3_G = 2;
    /** Class of broadly defined "4G" networks. */
    private static final int NETWORK_CLASS_4_G = 3;

    private CDAdDeviceInfo() {

    }

    public static CDAdDeviceInfo deviceInfo(Context context){
        if (sharedDeviceInfo == null){
            sharedDeviceInfo = new CDAdDeviceInfo(context);
        }
        return sharedDeviceInfo;
    }

    private CDAdDeviceInfo(Context context) {
        this.context = context;
        this.make = Build.MANUFACTURER;
        this.model = Build.MODEL;
        this.os = "Android";
        this.osv = Build.VERSION.RELEASE;
        this.hwv = Build.DEVICE;
    }

    public String getUa() {
        String userAgent;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                userAgent = WebSettings.getDefaultUserAgent(context);
            } else if (Looper.myLooper() == Looper.getMainLooper()) {
                // WebViews may only be instantiated on the UI thread. If anything goes
                // wrong with getting a user agent, use the system-specific user agent.
                userAgent = new WebView(context).getSettings().getUserAgentString();
            } else {
                userAgent = DEFAULT_USER_AGENT;
            }
        } catch (Exception e) {
            // Some custom ROMs may fail to get a user agent. If that happens, return
            // the Android system user agent.
            userAgent = DEFAULT_USER_AGENT;
        }
        return userAgent;
    }

    public int getLmt() {
        return SharedPreferencesHelper.getIntegerFromSharedPreferences(DataKeys.LIMITED_CDERTSER_TRACKING, 0, context);
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public String getOs() {
        return os;
    }

    public String getOsv() {
        return osv;
    }

    public String getHwv() {
        return hwv;
    }

    public Integer getH() {
        return (int) Utils.convertPixelsToDp(context.getResources().getDisplayMetrics().heightPixels, context);
    }

    public Integer getW() {
        return (int) Utils.convertPixelsToDp(context.getResources().getDisplayMetrics().widthPixels, context);
    }

    public float getPxratio() {
        return 0;
    }

    public int getJs() {
        return 1;
    }

    public String getLanguage() {
        return Locale.getDefault().getLanguage();
    }

    public String getCarrier() {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return manager.getNetworkOperatorName();
    }

    public int getConnectiontype() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null || !info.isConnected())
            return 0;
        switch (info.getType()){
            case ConnectivityManager.TYPE_ETHERNET:
                return 1;
            case ConnectivityManager.TYPE_WIFI:
                return 2;
            case ConnectivityManager.TYPE_MOBILE:
            {
                switch (getNetworkClass(getNetworkType(context))){
                    case NETWORK_CLASS_UNKNOWN:
                        return 3;
                    case NETWORK_CLASS_2_G:
                        return 4;
                    case NETWORK_CLASS_3_G:
                        return 5;
                    case NETWORK_CLASS_4_G:
                        return 6;
                }
            }
            default: return 0;
        }
    }

    public String getAdid() {
        return SharedPreferencesHelper.getStringFromSharedPreferences(DataKeys.UID, "", context);
    }

    public int getDevicetype(){
        int uiMode = context.getResources().getConfiguration().uiMode;
        if ((uiMode & Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_TELEVISION) {
            return 3;
        }
        else if ((uiMode & Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_CAR) {
            return 6;
        }
        else if ((uiMode & Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_CAR) {
            return 6;
        }
        else if ((context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE){
            return 5;
        }else{
            return 4;
        }
    }

    public static int getNetworkType(Context context) {
        return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
                .getNetworkType();
    }

    private static int getNetworkClassReflect(int networkType)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getNetworkClass = TelephonyManager.class.getDeclaredMethod("getNetworkClass", int.class);
        if (!getNetworkClass.isAccessible()) {
            getNetworkClass.setAccessible(true);
        }
        return (int) getNetworkClass.invoke(null, networkType);
    }

    /**
     * Return general class of network type, such as "3G" or "4G". In cases where classification is
     * contentious, this method is conservative.
     */
    public static int getNetworkClass(int networkType) {
        try {
            return getNetworkClassReflect(networkType);
        } catch (Exception ignored) {
        }

        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case 16: // TelephonyManager.NETWORK_TYPE_GSM:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return NETWORK_CLASS_2_G;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
            case 17: // TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                return NETWORK_CLASS_3_G;
            case TelephonyManager.NETWORK_TYPE_LTE:
            case 18: // TelephonyManager.NETWORK_TYPE_IWLAN:
                return NETWORK_CLASS_4_G;
            default:
                return NETWORK_CLASS_UNKNOWN;
        }
    }

    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

}
