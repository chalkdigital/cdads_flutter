package com.chalkdigital.common;

/**
 * Created by arungupta on 07/12/16.
 */

public class CDAdConstants {
    public static final int CONSTANT_READ_TIMEOUT_INTERVAL                   =  30;
    public static final int CONSTANT_CONNECTION_TIMEOUT_INTERVAL             =  10;
    public static final long CDAdfetchInterval                               =  30; //in seconds
    public static final long CDAdLocationServiceTimeoutInterval                =  30; //in seconds
    public static final long CDAdTrackingInterval                              =  900; //in seconds
    public static final float CDAdDistanceFilter                               =  10.0f;
    public static final int CDAdMinBGTime                                      =  5;
    public static final int CDAdMinAcceptableLocationAccuracy                  =  5;
    public static final int CDAdWaitForLocationsTime                           =  3;
    public static final int CDAdAcceptableAccuracy                             =  65;
    public static final int CDAdReverseGeocodeDistanceFilter                   =  1000;
    public static final long CDAdLocationExpiryInterval                      =  120;
    public static final String CDAdApiVersion                                =  "1.0";
    public static final Integer CDAdLocTypeDevice                             =   1;
    public static final Integer CDAdLocTypeUserProvided                        =   3;
    public static final Integer CDAdLocTypeUnavailable                        =   0;
    public static final Integer CDAdLocTypeIP                                  =   2;
    public static final String CDAdUidType                                   =  "1";
    public static final int CDAdMaxLocationManagerRunningInterval              =  30;
    public static final int CDAdMaxDwellTime                                 =  900;
    public static final String CDAdSdkVersion                                =  "2.1.0";
    public static final String CDAdBuildVersion                              =  "1.0.0";
    public static final long CDAdLoctionExpiryNotRequired                    = -1;
    public static final long CDAdLocationClientUpdatesNotRequired             = CDAdLoctionExpiryNotRequired;
    public static final long CDAdLocationClientSingleUpdateInterval           = 100000000;
    public static final int CDAdLocationPermissionGranted                     = 0;
    public static final int CDAdLocationPermissionDenied                      = 1;
    public static final int CDAdLocationPermissionUnKnown                     = 2;
    public static final int CDAdLocationServiceUnavailable                    = 3;
    public static final String mraidJSPrefix                                   = "<script src='https://s3.amazonaws.com/chalkiosapp/mraid/mraid.js'></script>\n";
    public static final String sparkJSPrefix                                   = "<script src='https://imasdk.googleapis.com/js/sdkloader/ima3.js'></script>\n<script src='https://s3.amazonaws.com/chalkiosapp/vpaid/spark.js'></script>\n<script type='text/javascript'>window.onload=function(){" +
            "setTimeout(initilizePlayer('vpaid_xml', 'ad_width', 'ad_height'), 0);" +
            "}</script>";
    public static final String responseBodyPrefix                              = "<body style=\"margin:0;padding:0\">";
    public static final String interstitialResponseBodyPrefix                  = "<html><head><meta name=\"viewport\" content=\"width=device-width,initial-scale=1,maximum-scale=1,user-scalable=0,viewport-fit=contain\"><body style=\"margin:0;padding:0;position:relative;text-align:center;display:flex;width:100%;height:100%;justify-content:center;flex-direction:column;\">";
//    align-items:center
    public static final String interstitialResponseBodySuffix                  = "</body>\n</head></html>";
    public static final String responseBodySuffix                              = "</body>\n";
    public static final int INTERSTITIAL_TIMEOUT_INTERVAL                       = 30;
    public static final float CDAdViewabilitySuccessVisibilityRatio            = 0.5f;


    public static int EVENT_REQUESTS = 0;
    public static int EVENT_IMPRESSION = 1;
    public static int EVENT_CLICK = 3;
    public static int EVENT_LOAD = 5;
    public static int EVENT_START = 6;
    public static int EVENT_FQ = 7;
    public static int EVENT_MP = 8;
    public static int EVENT_TQ = 9;
    public static int EVENT_COMPLETE = 10;

    public static int EVENT_VIEWABILITY_25_PERCENT = 25;
    public static int EVENT_VIEWABILITY_50_PERCENT = 26;
    public static int EVENT_VIEWABILITY_75_PERCENT = 27;
    public static int EVENT_VIEWABILITY_100_PERCENT = 28;


    public enum StretchType{
        STRETCH_NONE("0"),
        STRETCH_TYPE_ASPECT_FIT("1"),
        STRETCH_TYPE_ASPECT_FILL("2");

        private final String text;

        StretchType(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
