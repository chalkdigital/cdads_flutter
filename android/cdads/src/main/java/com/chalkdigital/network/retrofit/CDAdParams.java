package com.chalkdigital.network.retrofit;

/**
 * Created by arungupta on 07/12/16.
 */

public class CDAdParams {
    public static final String CDAdSDKBaseUrl_Test                 = "https://geoip.chalkdigital.com/";
    public static final String CDAdSDKBaseUrl_Production           = "https://geoip.chalkdigital.com/";
    public static final String CDAdBaserUrl                        = "https://e.cmcd1.com/";
    public static final String CDAdReverseGeocodeUrl               = "https://maps.googleapis.com/";
    public static final String port                                 = "";
    public static final String vpaidHtmlUrl                         = "https://s3.amazonaws.com/chalkiosapp/vpaid/vpaid_2.0/cd_sdk_vpaid_html_2.0.html";
    public static final String webViewBaseUrl                       = "https://www.chalkdigital.com";

    public static final String getSDKBaseUrl(){
        return CDAdSDKBaseUrl_Production;
    }
}
