package com.chalkdigital.common;

/**
 * Keys used in localExtras and serverExtras maps for CDAd custom events.
 */
public class DataKeys {
    public final static String TEMPO_LOG_LEVEL = "tempoLogLevel";
    public static final String VPAID_SOURCE_URL = "VPAID_SOURCE_URL";
    public static final String VPAID_SOURCE_HTML = "vpaid_source_html";
    public static final String OMID_JS_SERVICE_CONTENT = "omid-js-service-content";
    public static final String AD_REPORT_KEY = "cdads-intent-ad-report";
    public static final String AD_EVENTS_KEY = "cdads-intent-ad-events";
    public static final String JOB_ACTION_KEY = "jobActionKey";
    public static final String AD_SERVER_EXTRAS_KEY = "cdads-intent-ad-server-extras";
    public static final String AD_ID_KEY = "cdads-intent-ad-id";
    public static final String HTML_RESPONSE_BODY_KEY = "Html-Response-Body";
    public static final String SKIP_OFFSET = "Skip-Offset";
    public static final String SKIP_ENABLE = "Skip-Enable";
    public static final String REDIRECT_URL_KEY = "Redirect-Url";
    public static final String CLICKTHROUGH_URL_KEY = "Clickthrough-Url";
    public static final String CLICK_TRACKING_URL_KEY = "Click-Tracking-Url";
    public static final String SCROLLABLE_KEY = "Scrollable";
    public static final String CLICK_ACTION = "clickAction";
    public static final String BASE_SDK_EVENT_URL = "base_sdk_event_url";
    public static final String VAST = "vast";
    public static final String SSPID = "sspid";
    public final static String CD_LOG_LEVEL = "cdLogLevel";
    public static final String CREATIVE_ORIENTATION_KEY = "com.chalkdigitalorientation";
    public static final String EVENT_ID_PLACEHOLDER = "<EVT_ID>";
    public static final String SDK_ID_PLACEHOLDER = "<SDK_ID>";
    public static final String JSON_BODY_KEY = "com.chalkdigital.nativeads_json";
    public static final String BROADCAST_IDENTIFIER_KEY = "broadcastIdentifier";
    public static final String BROADCAST_INFO_KEY = "broadcastInfo";
    public static final String AD_UNIT_ID_KEY = "com.chalkdigitalad_unit_id";
    public static final String AD_WIDTH = "com.chalkdigitalad_width";
    public static final String AD_HEIGHT = "com.chalkdigitalad_height";
    public static final String ANDROID_INTERSTITIAL = "Android_Interstitial";
    public static final String ANDROID_REWARDED = "Android_Rewarded";
    public static final String ANDROID_BANNER = "Android_Banner";
    public final static String REWARDED = "rewarded";

    // Banner imp tracking fields
    public static final String BANNER_IMPRESSION_MIN_VISIBLE_DIPS = "Banner-Impression-Min-Pixels";
    public static final String BANNER_IMPRESSION_MIN_VISIBLE_MS = "Banner-Impression-Min-Ms";

    // Native fields
    public static final String IMPRESSION_MIN_VISIBLE_PERCENT = "Impression-Min-Visible-Percent";
    public static final String IMPRESSION_VISIBLE_MS = "Impression-Visible-Ms";
    public static final String IMPRESSION_MIN_VISIBLE_PX = "Impression-Min-Visible-Px";

    // Native Video fields
    public static final String PLAY_VISIBLE_PERCENT = "Play-Visible-Percent";
    public static final String PAUSE_VISIBLE_PERCENT = "Pause-Visible-Percent";
    public static final String MAX_BUFFER_MS = "Max-Buffer-Ms";
    public static final String EVENT_DETAILS = "Event-Details";

    // Rewarded Ad fields
    public static final String REWARDED_AD_CURRENCY_NAME_KEY = "Rewarded-Ad-Currency-Name";
    public static final String REWARDED_AD_CURRENCY_AMOUNT_STRING_KEY = "Rewarded-Ad-Currency-Value-String";
    public static final String REWARDED_AD_CUSTOMER_ID_KEY = "Rewarded-Ad-Customer-Id";
    public static final String REWARDED_AD_DURATION_KEY = "Rewarded-Ad-Duration";
    public static final String SHOULD_REWARD_ON_CLICK_KEY = "Should-Reward-On-Click";

    // Viewability fields
    public static final String EXTERNAL_VIDEO_VIEWABILITY_TRACKERS_KEY = "External-Video-Viewability-Trackers";

    /**
     * @deprecated as of 4.12, replaced by {@link #REWARDED_AD_CUSTOMER_ID_KEY}
     */
    @Deprecated
    public static final String REWARDED_VIDEO_CUSTOMER_ID = "Rewarded-Ad-Customer-Id";

    // Video tracking fields
    public static final String VIDEO_TRACKERS_KEY = "Video-Trackers";

    public final static String DEVICE_OS = "Device-OS";
    public final static String CONTENT_TYPE = "Content-Type";
    public final static String CDAdNotifyUIDeviceLocationChanged = "CDAdNotifyUIDeviceLocationChanged";
    public final static String CDAdNotifyNetworkReachabilityChanged = "CDAdNotifyNetworkReachabilityChanged";
    public final static String CDAdNetworkStatus = "CDAdNetworkStatus";
    public final static String CDAdNetworkStatusReachable = "CDAdNetworkStatusReachable";
    public final static String CDAdNetworkStatusNotReachable = "CDAdNetworkStatusNotReachable";
    public final static String CDAdNetworkStatusUnknown = "CDAdNetworkStatusUnknown";
    public final static String CDAdRegistration = "CDAdRegistration";
    public final static String CDAdChangeParams = "CDAdChangeParams";
    public final static String CDAdfetchIntervalKey = "CDAdfetchInterval";
    public final static String CDAdTrackingIntervalKey = "CDAdTrackingInterval";
    public final static String CDAdDistanceFilterKey = "CDAdDistanceFilter";
    public final static String CDAdAcceptableAccuracyKey = "CDAdAcceptableAccuracy";
    public final static String CDAdLocationExpiryIntervalKey = "CDAdLocationExpiryInterval";
    public final static String CDAdReverseGeocodeDistanceFilterKey = "CDAdReverseGeocodeDistanceFilter";
    public final static String CDAdMinBGTimeKey = "CDAdMinBGTime";
    public final static String CDAdMaxLocationManagerRunningIntervalKey = "CDAdMaxLocationManagerRunningInterval";
    public final static String CDAdServerCodeHeaderKey = "SERVER_CODE";
    public final static String LAT = "lat";
    public final static String LNG = "lng";
    public final static String METRO = "metro";
    public final static String LOCATION_CHANGED = "locationChanged";
    public final static String DISTANCE_FILTER = "distanceFilter";
    public final static String LOCATION_UPDATE_INTERVAL = "locationUpdateInterval";
    public final static String AD_LOCATION_EXPIRY_INTERVAL = "adLocationExpiryInterval";
    public final static String LOCATION_CLIENT_UPDATE_INTERVAL = "locationClientUpdateInterval";
    public final static String LAST_GEOCODED_LOCATION = "lastGeocodedLocation";
    public final static String GEOINFO = "geoinfo";
    public final static String LAST_LOCATION_TIME = "lastLocationTime";
    public final static String LAST_GEOCODED_LOCATION_TIME = "lastGeocodedLocationTime";
    public final static String CDAd_LOG_LEVEL = "cdAdLogLevel";

    public final static String CITY = "city";
    public final static String STATE = "state";
    public final static String ZIP = "zip";
    public final static String COUNTRY = "country";
    public final static String LOCTYPE = "loctype";
    public final static String HORIZONTAL_ACCURACY = "haccuracy";
    public final static String ADTYPE = "adtype";
    public final static String UID = "uid";
    public final static String OS = "os";
    public final static String DEVICEOS = "deviceOS";
    public final static String UA = "ua";
    public final static String SITE = "site";
    public final static String SITEID = "siteid";
    public final static String PLACEMENTID = "placementid";
    public final static String UIDTYPE = "uidType";
    public final static String AGE = "age";
    public final static String USERID = "userId";
    public final static String GENDER = "gender";
    public final static String INCOME = "income";
    public final static String EDUCATION = "education";
    public final static String KEYWORD = "keyword";
    public final static String VER = "ver";
    public final static String SDK_VER = "sdkver";
    public final static String CAT = "cat";
    public final static String AD_TYPE = "adtype";
    public final static String BUNDLE = "bundle";
    public final static String TESTINGBUNDLE = "testingBundle";
    public final static String SECURE = "secure";
    public final static String DNT = "dnt";
    public final static String LIMIT_ADVERTISER_TRACKING = "limitedAdvertiserTracking";
    public final static String IP = "ip";
    public final static String FMT = "fmt";
    public final static String KEY = "key";
    public final static String PUB = "pub";
    public final static String PARTNERID = "partnerId";
    public final static String PLAY_STORE_URL = "playStoreUrl";
    public final static String REQ_ID = "id";
    public final static String SDK_VERSION = "sdkversion";
    public final static String SDK_BUILD_VERSION = "sdkbuildversion";
    public static final String IS_EMULATOR = "emulator";

    public final static String SDK_KEY = "sdkkey";
    public final static String SDK_SECRET_KEY = "sdksecretkey";

    public final static String LIMITED_CDERTSER_TRACKING = "limitedAdvertiserTracking";
    public final static String SDK_TRACKING_ENABLED = "sdkTrackingEnabled";
    public final static String DEVICE_TYPE = "devicetype";
    public final static String DEVICETYPE = "deviceType";
    public final static String OS_VERSION = "osVersion";
    public final static String HEIGHT = "h";
    public final static String WIDTH = "w";
    public final static String STRETCH = "stretch";
    public final static String EVENTS = "events";
    public final static String AD_ID = "adIdForEvents";
    public final static String PIXEL_RATIO = "pxratio";
    public final static String PIXELRATIO = "pixelRatio";
    public final static String JS_ENABLED = "js";
    public final static String JSENABLED = "jsenabled";
    public final static String CARRIER = "carrier";
    public final static String LANGUAGE = "language";
    public final static String HARDWARE_VERSION = "hardwareVersion";
    public final static String CONNECTION_TYPE = "connectionType";
    public final static String DEVICE_MAKE = "make";
    public final static String DEVICEMAKE = "deviceMake";
    public final static String DEVICE_MODEL = "model";
    public final static String DEVICEMODEL = "deviceModel";

    public final static String ERROR_DESCRIPTION = "desc";
    public final static String SERVER_CODE = "SERVER_CODE";
    public final static String SERVER_URL = "server_url";
    public final static String SERVER_PATH = "server_path";

    public final static String LOCATION_DATA = "locationdata";
    public final static String IDS = "ids";
    public final static String LOCATION_TYPE = "locationType";

    public final static String IS_TRACKING_ENABLED = "isTrackingEnabled";
    public final static String IS_RECEIVING_CONTINUOUS_LOCATION_UPDATES = "receivingContinuousLocationUpdates";
    public final static String IS_NEW_LOCATION_IN_PROGRESS = "newLocationInProgress";
    public final static String IS_NEW_IP_LOCATION_IN_PROGRESS = "newIPLocationInProgress";
    public final static String PUBLIC_IP = "publicIp";
    public final static String IS_CONNECTIVITY_SERVICE_RUNNING = "isConnectivityServiceRunning";
    public final static String IS_LOCATION_MANAGER_SERVICE_RUNNING = "isLocationManagerServiceRunning";
    public final static String IS_REVERSE_GEOCODING_LOCATION = "reverseGeocodingLocation";

    public final static String IS_REGISTRATION_REQUESTED = "isRegistrationRequested";
    public final static String IS_REGISTRATION_IN_PROCESS = "isRegistrationInProcess";
    public final static String IS_TRACK_DATA_REQUESTED = "isTrackDataRequested";
    public final static String IS_TRACK_DATA_UPLOAD_IN_PROCESS = "isTrackDataUploadInProcess";

    public final static String IS_TRACKING_PERMISSION_GRANTED = "isTrackingPermissionGranted";
    public final static String IS_TRACKING_PERMISSION_DENIED = "isTrackingPermissionDenied";
    public final static String IS_LOCATION_PROMPT_RESPONDED_BY_USER = "isLocationPromptRespondedByUser";
    public final static String IS_LAST_LOCATION_AVAILABLE = "isLastLocationAvailable";
    public final static String IS_LAST_GEO_LOCATION_AVAILABLE = "isLastGeoLocationAvailable";
    public final static String LAST_LOCATION = "lastLocation";
    public final static String LAST_PENDING_LOCATION_Time = "lastPendingLocationTime";
    public final static String LAST_GEO_LOCATION = "lastGeoLocation";
    public final static String IS_LAST_TRACKING_LOCATION_AVAILABLE = "isLastTrackingLocationAvailable";
    public final static String LAST_TRACKING_LOCATION = "lastTrackingLocation";
    public final static String LIMITED_ADVERTSER_TRACKING = "limitedAdvertiserTracking";

    public final static String LOCATION_SERVICE_CDAdTrackingInterval = "locationServiceCDAdTrackingInterval";
    public final static String LOCATION_SERVICE_CDAdDistanceFilter = "locationServiceCDAdDistanceFilter";
    public final static String LOCATION_PERMISSION_ERROR = "locationPermissionError";
    public final static String LOCATION_PERMISSION_DENIED = "locationPermissionDenied";


    public static final String CDAdSdkTrackingPermissionGranted      = "CDAdSdkTrackingPermissionGranted";
    public static final String CDAdSDKLocationUserPreferenceReceived      = "CDAdSDKLocationUserPreferenceReceived";
    public static final String CDAdSDKKEYID = "sdkkeyid";
    public static final String CDAdLOGIN = "cdAdlogin";
    public static final String CDAd_DEFAULT_BASE_URL = "cdAdAdDefautBaseUrl";
    public static final String CDAd_COUNTRY_BASED_URLS = "cdAdAdCountryBasedUrls";

    public static final String IS_STRECH_ENABLED = "isStrechEnabled";
    public static final String AD_VIEW = "adView";
    public final static String FORCE_CLOSE_BUTTON = "forceCloseButton";

    public static final String VIDEO_CONF = "videoConf";
    public static final String MIN_DURATION = "minduration";
    public static final String MAX_DURATION = "maxduration";
    public static final String START_DELAY = "startdelay";
    public static final String LINEARITY = "linearity";
    public static final String SKIP = "skip";
    public static final String SKIP_MIN = "skipmin";
    public static final String SKIP_AFTER = "skipafter";
    public static final String MAX_EXTEND = "maxextended";
    public static final String MIN_BITRATE = "minbitrate";
    public static final String MAX_BITRATE = "maxbitrate";

    public static final String MIME_TYPES = "mimetypes";
    public static final String PROTOCOLS = "protocols";
    public static final String EXCEPTION = "exception";

    public static final String SSB_APP_KEY = "appKey";
    public static final String SSB_DIGEST = "digest";
    public static final String SSB_IDFA = "idfa";
    public static final String SSB_DEVICE_ID = "deviceId";
    public static final String SSB_TIMESTAMP = "t";
    public static final String SSB_UUID = "uuid";
    public static final String SSB_UUIDS = "ssbuuids";
    public static final String SSB_SAVED_BEACON = "savedbeacon";
    public static final String SSB_MAJOR = "major";
    public static final String SSB_MINOR = "minor";
    public static final String SSB_PROXIMITY = "proximity";
    public static final String SSB_LOCALEID = "localeId";
    public static final String SSB_ADTYPE = "adType";
    public static final String SSB_POPUP = "popup";
    public static final String SSB_TEXT = "text";
    public static final String SSB_BANNER = "banner";
    public static final String SSB_SIZE = "size";
    public static final String SSB_PLATFORM = "platform";
    public static final String SSB_DEVICETOKEN = "deviceToken";
    public static final String SSB_LOCALE = "sysLocale";
    public static final String SSB_SYSTEM_MODEL = "sysModel";
    public static final String SSB_SYSTEM_VER = "sysVer";
    public static final String SSB_BUNDLE_ID = "bundleId";
    public static final String SSB_APP_VER = "appver";


}
