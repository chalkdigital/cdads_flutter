package com.chalkdigital.common;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.chalkdigital.ads.CDAdErrorCode;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.Reflection;
import com.chalkdigital.common.util.Utils;
import com.chalkdigital.network.response.GetUUidsResponse;
import com.chalkdigital.network.response.SSBBaseResponse;
import com.chalkdigital.network.response.UpdateDeviceInfoResponse;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static com.chalkdigital.common.ExternalViewabilitySessionManager.ViewabilityVendor;

public class CDAdsUtils {
    private static Context sContext;
    public static boolean initialised;
    public static Boolean UseBeacon;

    public enum LocationAwareness { NORMAL, TRUNCATED, DISABLED }

    /**
     * Browser agent to handle URIs with scheme HTTP or HTTPS
     */
    public enum BrowserAgent {
        /**
         * CDAd's in-app browser
         */
        IN_APP,

        /**
         * Default browser application on device
         */
        NATIVE;

        /**
         * Maps header value from CDAd's AdServer to browser agent:
         * 0 is CDAd's in-app browser (IN_APP), and 1 is device's default browser (NATIVE).
         * For null or all other undefined values, returns default browser agent IN_APP.
         * @param browserAgent Integer header value from CDAd's AdServer.
         * @return IN_APP for 0, NATIVE for 1, and IN_APP for null or all other undefined values.
         */
        @NonNull
        public static BrowserAgent fromConfiguration(@Nullable final Integer browserAgent) {
            if (browserAgent == null) {
                return IN_APP;
            }

            return browserAgent == 1 ? IN_APP : NATIVE;
        }
    }

    private static final String CDADS_REWARDED_VIDEOS =
            "com.chalkdigital.ads.CDAdRewardedVideos";
    private static final String CDADS_REWARDED_VIDEO_MANAGER =
            "com.chalkdigital.ads.CDAdRewardedVideoManager";
    private static final String CDADS_REWARDED_VIDEO_LISTENER =
            "com.chalkdigital.ads.CDAdRewardedVideoListener";
    private static final String CDADS_REWARDED_VIDEO_MANAGER_REQUEST_PARAMETERS =
            "com.chalkdigital.ads.CDAdRewardedVideoManager$VideoConfiguration";

    private static final int DEFAULT_LOCATION_PRECISION = 6;
    private static final long DEFAULT_LOCATION_REFRESH_TIME = 60;

    @NonNull private static volatile LocationAwareness sLocationAwareness = LocationAwareness.NORMAL;
    private static volatile int sLocationPrecision = DEFAULT_LOCATION_PRECISION;
    private static volatile long sMinimumLocationRefreshTime = DEFAULT_LOCATION_REFRESH_TIME;
    @NonNull private static volatile BrowserAgent sBrowserAgent = BrowserAgent.IN_APP;
    private static volatile boolean sIsBrowserAgentOverriddenByClient = false;
    private static boolean sSearchedForUpdateActivityMethod = false;
    @Nullable private static Method sUpdateActivityMethod;
    private static boolean isGeoIpLocationEnabled = true;
    private static boolean isGDPR = false;
    private static boolean isConsent = false;

    public static boolean isIsGeoIpLocationEnabled() {
        return isGeoIpLocationEnabled;
    }

    public static void setGeoIpLocationEnabled(final boolean isGeoIpLocationEnabled) {
        CDAdsUtils.isGeoIpLocationEnabled = isGeoIpLocationEnabled;
    }

    public static CDAdErrorCode initialize(final Context context){
        try {
            sContext = context;
            if (UseBeacon == null){
                UseBeacon = true;
            }
            CDAdErrorCode cdAdErrorCode = Utils.checkMandatoryParams(context);
            if (cdAdErrorCode != null)
            {
                CDAdLog.e(cdAdErrorCode.toString());
                return cdAdErrorCode;
            }
            initialiseAdvertisingIdentifiers(context);
            disableViewability(ViewabilityVendor.MOAT);
            disableViewability(ViewabilityVendor.AVID);
            disableViewability(ViewabilityVendor.OMSDK);


        } catch (Throwable throwable) {
            Utils.logStackTrace(throwable);
            return CDAdErrorCode.UNSPECIFIED;
        }
        return null;
//        new OmViewabilitySession().initialize(context);

//        if (!CDAdLocationManager.isJobServiceOn(context))
//            CDAdLocationManager.scheduleLocationJob(context);
    }

    @NonNull
    public static LocationAwareness getLocationAwareness() {
        Preconditions.checkNotNull(sLocationAwareness);

        return sLocationAwareness;
    }

    public static void setLocationAwareness(@NonNull final LocationAwareness locationAwareness) {
        Preconditions.checkNotNull(locationAwareness);

        sLocationAwareness = locationAwareness;
    }

    public static int getLocationPrecision() {
        return sLocationPrecision;
    }

    /**
     * Sets the precision to use when the SDK's location awareness is set
     * to {@link LocationAwareness#TRUNCATED}.
     */
    public static void setLocationPrecision(int precision) {
        sLocationPrecision = Math.min(Math.max(0, precision), DEFAULT_LOCATION_PRECISION);
    }

    public static void setMinimumLocationRefreshTime(
            final long minimumLocationRefreshTime) {
        sMinimumLocationRefreshTime = minimumLocationRefreshTime;
    }

    public static long getMinimumLocationRefreshTime() {
        return sMinimumLocationRefreshTime;
    }

    public static void setBrowserAgent(@NonNull final BrowserAgent browserAgent) {
        Preconditions.checkNotNull(browserAgent);

        sBrowserAgent = browserAgent;
        sIsBrowserAgentOverriddenByClient = true;
    }

    public static void setBrowserAgentFromAdServer(
            @NonNull final BrowserAgent adServerBrowserAgent) {
        Preconditions.checkNotNull(adServerBrowserAgent);

        if (sIsBrowserAgentOverriddenByClient) {
            CDAdLog.w("Browser agent already overridden by client with value " + sBrowserAgent);
        } else {
            sBrowserAgent = adServerBrowserAgent;
        }
    }

    public static void setLogLevel(@NonNull final Level level) {
        CDAdLog.setSdkHandlerLevel(level);
    }

    @NonNull
    public static BrowserAgent getBrowserAgent(String clickAction) {
        Preconditions.checkNotNull(sBrowserAgent);
        if (sIsBrowserAgentOverriddenByClient || clickAction == null)
            return sBrowserAgent;
        else return clickAction.equals("1")?BrowserAgent.IN_APP:BrowserAgent.NATIVE;
    }

    @VisibleForTesting
    static boolean isBrowserAgentOverriddenByClient() {
        return sIsBrowserAgentOverriddenByClient;
    }

    @VisibleForTesting
    @Deprecated
    public static void resetBrowserAgent() {
        sBrowserAgent = BrowserAgent.NATIVE;
        sIsBrowserAgentOverriddenByClient = false;
    }

    //////// CDAd LifecycleListener messages ////////

    public static void onCreate(@NonNull final Activity activity) {
        CDAdLifecycleManager.getInstance(activity).onCreate(activity);
        updateActivity(activity);
    }

    public static void onStart(@NonNull final Activity activity) {
        CDAdLifecycleManager.getInstance(activity).onStart(activity);
        updateActivity(activity);
    }

    public static void onPause(@NonNull final Activity activity) {
        CDAdLifecycleManager.getInstance(activity).onPause(activity);
    }

    public static void onResume(@NonNull final Activity activity) {
        CDAdLifecycleManager.getInstance(activity).onResume(activity);
        updateActivity(activity);
    }

    public static void onRestart(@NonNull final Activity activity) {
        CDAdLifecycleManager.getInstance(activity).onRestart(activity);
        updateActivity(activity);
    }

    public static void onStop(@NonNull final Activity activity) {
        CDAdLifecycleManager.getInstance(activity).onStop(activity);
    }

    public static void onDestroy(@NonNull final Activity activity) {
        CDAdLifecycleManager.getInstance(activity).onDestroy(activity);
    }

    public static void onBackPressed(@NonNull final Activity activity) {
        CDAdLifecycleManager.getInstance(activity).onBackPressed(activity);
    }

    public static void disableViewability(@NonNull final ViewabilityVendor vendor) {
        Preconditions.checkNotNull(vendor);

        vendor.disable();
    }

    /**
     * Enable GDPR mode
     * @param isGDPR boolean value to enable GDPR compilance. By default GDPR compilance is off.
     */
    public static void setGDPR(final boolean isGDPR) {
        CDAdsUtils.isGDPR = isGDPR;
    }

    /**
     * Enable user consent
     * @param isConsent boolean value to enable user consent for GDPR terms. By default it's value is false.
     */
    public static void setConsent(final boolean isConsent) {
        CDAdsUtils.isConsent = isConsent;
    }

    /**
     * Check if GDPR mode is enabled.
     * @return boolean value
     */
    public static boolean isGDPREnabled() {
        return isGDPR;
    }

    /**
     * Check if user consent is provided for GDPR.
     * @return boolean value
     */
    public static boolean isConsentProvided() {
        return isConsent;
    }

    ////////// CDAd RewardedVideoControl methods //////////
    // These methods have been deprecated as of release 4.9 due to SDK modularization. CDAd is
    // inside of the base module while CDAdRewardedVideos is inside of the rewarded video module.
    // CDAdRewardedVideos methods must now be called with reflection because the publisher
    // may have excluded the rewarded video module.


    /**
     * @deprecated As of release 4.9, use CDAdRewardedVideos#initializeRewardedVideo instead
     */
    @Deprecated
    public static void initializeRewardedVideo(@NonNull Activity activity, MediationSettings... mediationSettings) {
        try {
            new Reflection.MethodBuilder(null, "initializeRewardedVideo")
                    .setStatic(Class.forName(CDADS_REWARDED_VIDEOS))
                    .addParam(Activity.class, activity)
                    .addParam(MediationSettings[].class, mediationSettings)
                    .execute();
        } catch (ClassNotFoundException e) {
                        Utils.logStackTrace(e);
            CDAdLog.w("initializeRewardedVideo was called without the rewarded video module");
        } catch (NoSuchMethodException e) {
                        Utils.logStackTrace(e);
            CDAdLog.w("initializeRewardedVideo was called without the rewarded video module");
        } catch (Exception e) {
                        Utils.logStackTrace(e);
            CDAdLog.e("Error while initializing rewarded video", e);
        }
    }

    @VisibleForTesting
    static void updateActivity(@NonNull Activity activity) {
        if (!sSearchedForUpdateActivityMethod) {
            sSearchedForUpdateActivityMethod = true;
            try {
                Class cdAdRewardedVideoManagerClass = Class.forName(
                        CDADS_REWARDED_VIDEO_MANAGER);
                sUpdateActivityMethod = Reflection.getDeclaredMethodWithTraversal(
                        cdAdRewardedVideoManagerClass, "updateActivity", Activity.class);
            } catch (ClassNotFoundException e) {
                        Utils.logStackTrace(e);
                // rewarded video module not included
            } catch (NoSuchMethodException e) {
                        Utils.logStackTrace(e);
                // rewarded video module not included
            }
        }

        if (sUpdateActivityMethod != null) {
            try {
                sUpdateActivityMethod.invoke(null, activity);
            } catch (IllegalAccessException e) {
                        Utils.logStackTrace(e);
                CDAdLog.e("Error while attempting to access the update activity method - this " +
                        "should not have happened", e);
            } catch (InvocationTargetException e) {
                        Utils.logStackTrace(e);
                CDAdLog.e("Error while attempting to access the update activity method - this " +
                        "should not have happened", e);
            }
        }
    }

    /**
     * @deprecated As of release 4.9, use CDAdRewardedVideos#setRewardedVideoListener instead
     */
    @Deprecated
    public static void setRewardedVideoListener(@Nullable Object listener) {
        try {
            Class cdAdRewardedVideoListenerClass = Class.forName(
                    CDADS_REWARDED_VIDEO_LISTENER);
            new Reflection.MethodBuilder(null, "setRewardedVideoListener")
                    .setStatic(Class.forName(CDADS_REWARDED_VIDEOS))
                    .addParam(cdAdRewardedVideoListenerClass, listener)
                    .execute();
        } catch (ClassNotFoundException e) {
                        Utils.logStackTrace(e);
            CDAdLog.w("setRewardedVideoListener was called without the rewarded video module");
        } catch (NoSuchMethodException e) {
                        Utils.logStackTrace(e);
            CDAdLog.w("setRewardedVideoListener was called without the rewarded video module");
        } catch (Exception e) {
                        Utils.logStackTrace(e);
            CDAdLog.e("Error while setting rewarded video listener", e);
        }
    }

    /**
     * @deprecated As of release 4.9, use CDAdRewardedVideos#loadRewardedVideo instead
     */
    @Deprecated
    public static void loadRewardedVideo(@NonNull String adUnitId,
            @Nullable MediationSettings... mediationSettings) {
        CDAdsUtils.loadRewardedVideo(adUnitId, null, mediationSettings);
    }

    /**
     * @deprecated As of release 4.9, use CDAdRewardedVideos#loadRewardedVideo instead
     */
    @Deprecated
    public static void loadRewardedVideo(@NonNull String adUnitId,
            @Nullable Object requestParameters,
            @Nullable MediationSettings... mediationSettings) {
        try {
            Class requestParametersClass = Class.forName(
                    CDADS_REWARDED_VIDEO_MANAGER_REQUEST_PARAMETERS);
            new Reflection.MethodBuilder(null, "loadRewardedVideo")
                    .setStatic(Class.forName(CDADS_REWARDED_VIDEOS))
                    .addParam(String.class, adUnitId)
                    .addParam(requestParametersClass, requestParameters)
                    .addParam(MediationSettings[].class, mediationSettings)
                    .execute();
        } catch (ClassNotFoundException e) {
                        Utils.logStackTrace(e);
            CDAdLog.w("loadRewardedVideo was called without the rewarded video module");
        } catch (NoSuchMethodException e) {
                        Utils.logStackTrace(e);
            CDAdLog.w("loadRewardedVideo was called without the rewarded video module");
        } catch (Exception e) {
                        Utils.logStackTrace(e);
            CDAdLog.e("Error while loading rewarded video", e);
        }
    }

    /**
     * @deprecated As of release 4.9, use CDAdRewardedVideos#hasRewardedVideo instead
     */
    @Deprecated
    public static boolean hasRewardedVideo(@NonNull String adUnitId) {
        try {
            return (boolean) new Reflection.MethodBuilder(null, "hasRewardedVideo")
                    .setStatic(Class.forName(CDADS_REWARDED_VIDEOS))
                    .addParam(String.class, adUnitId)
                    .execute();
        } catch (ClassNotFoundException e) {
                        Utils.logStackTrace(e);
            CDAdLog.w("hasRewardedVideo was called without the rewarded video module");
        } catch (NoSuchMethodException e) {
                        Utils.logStackTrace(e);
            CDAdLog.w("hasRewardedVideo was called without the rewarded video module");
        } catch (Exception e) {
                        Utils.logStackTrace(e);
            CDAdLog.e("Error while checking rewarded video", e);
        }
        return false;
    }

    /**
     * @deprecated As of release 4.9, use CDAdRewardedVideos#showRewardedVideo instead
     */
    @Deprecated
    public static void showRewardedVideo(@NonNull String adUnitId) {
        try {
            new Reflection.MethodBuilder(null, "showRewardedVideo")
                    .setStatic(Class.forName(CDADS_REWARDED_VIDEOS))
                    .addParam(String.class, adUnitId)
                    .execute();
        } catch (ClassNotFoundException e) {
                        Utils.logStackTrace(e);
            CDAdLog.w("showRewardedVideo was called without the rewarded video module");
        } catch (NoSuchMethodException e) {
                        Utils.logStackTrace(e);
            CDAdLog.w("showRewardedVideo was called without the rewarded video module");
        } catch (Exception e) {
                        Utils.logStackTrace(e);
            CDAdLog.e("Error while showing rewarded video", e);
        }
    }

    private static void initialiseAdvertisingIdentifiers(final Context context) {
        initialised = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                String adid = "";
                int isLimitedTrackingEnabled = 1;
                try {
                    AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
                    if (adInfo != null) {
                        adid = adInfo.getId();
                        isLimitedTrackingEnabled = adInfo.isLimitAdTrackingEnabled() ? 1 : 0;
                    }
                    // Use the cdertising id
                }
                catch (IOException timeoutException) {
                    // User didn responded to permission dialog
                }
                catch (Exception exception) {
                        Utils.logStackTrace(exception);
                    CDAdLog.d("Feching ADID", exception.getMessage());
                } finally {
                    SharedPreferencesHelper.putStringToSharedPreferences(DataKeys.UID, adid, context);
                    SharedPreferencesHelper.putIntegerToSharedPreferences(DataKeys.LIMITED_CDERTSER_TRACKING, isLimitedTrackingEnabled, context);
                }
            }
        }).start();
    }

}
