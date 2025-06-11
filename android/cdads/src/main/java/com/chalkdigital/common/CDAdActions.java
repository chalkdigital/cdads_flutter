package com.chalkdigital.common;

/**
 * Created by arungupta on 27/12/16.
 */

public class CDAdActions {
    public final static String LOCATION_CHANGED = "locationChanged";
    public final static String LOCATION_ERROR = "locationError";
    public final static String IP_RECEIVED = "ipReceived";
    public final static String IP_ERROR = "ipError";
    public final static String SDK_AUTHENTICATED = "sdkAuthenticated";
    public final static String SCHEDULE_PENDING_LOCATION_REQUEST = "schedulePendingLocationRequest";
    public final static String APP_LAUNCHED = "appLaunched";
    public final static String NEW_LOCATION_REQUESTED = "newLocationRequested";
    public final static String NEW_IP_LOCATION_REQUESTED = "newIPLocationRequested";
    public final static String IP_REQUESTED = "ipRequested";
    public final static String CONTINUOUS_LOCATION_REQUESTED = "continuousLocationRequested";
    public final static String STOP_CONTINUOUS_LOCATION_UPDATES = "stopContinuousLocationUpdates";
    public final static String NEW_LOCATION_RECEIVED = "newLocationReceived";
    public final static String REVERSE_GEOCODE_LOCATION = "revereseGeoodeLocation";
    public final static String LOGIN_REQUESTED = "loginRequested";
    public final static String SEND_TRACK_DATA = "sendTrackData";
    public final static String LOCATION_PERMISSION_REQUESTED = "locationPermissionRequested";
    public final static String TRACKING_PERMISSION_REQUESTED = "trackingPermissionRequested";
    public final static String LOCATION_PERMISSION_GRANTED = "locationPermissionGranted";
    public final static String LOCATION_PERMISSION_DENIED = "locationPermissionDenied";
    public final static String TRACKING_PERMISSION_GRANTED = "trackingPermissionGranted";
    public final static String TRACKING_PERMISSION_DENIED = "trackingPermissionDenied";

    public static final String CDAdNotifyUIDeviceLocationChanged  = "CDAdNotifyUIDeviceLocationChanged";
    public static final String CDAdNotifyTrackingPermissionGranted  = "CDAdNotifyTrackingPermissionGranted";
    public static final String CDAdNotifyNetworkReachabilityChanged  = "CDAdNotifyNetworkReachabilityChanged";
    public static final String CDAdNotifySdkUserAuthenticated  = "CDAdNotifySdkUserAuthenticated";
}
