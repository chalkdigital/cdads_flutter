package com.chalkdigital.network;

import android.content.Context;
import android.net.Uri;

import com.chalkdigital.common.GpsHelper;

/**
 * Url Rewriter that replaces CDAd templates for Google Advertising ID and Do Not Track settings
 * when a request is queued for dispatch by the HurlStack in Volley.
 */
public class PlayServicesUrlRewriter {
//        implements HurlStack.UrlRewriter {
    private static final String IFA_PREFIX = "ifa:";

    public static final String UDID_TEMPLATE = "cd_tmpl_cdertising_id";
    public static final String DO_NOT_TRACK_TEMPLATE = "cd_tmpl_do_not_track";

    private final String deviceIdentifier;
    private final Context applicationContext;

    public PlayServicesUrlRewriter(String deviceId, Context context) {
        deviceIdentifier = deviceId;
        applicationContext = context.getApplicationContext();
    }

//    @Override
    public String rewriteUrl(final String url) {
        if (!url.contains(UDID_TEMPLATE) && !url.contains(DO_NOT_TRACK_TEMPLATE)) {
            return url;
        }

        String prefix = "";
        GpsHelper.AdvertisingInfo cdertisingInfo = new GpsHelper.AdvertisingInfo(deviceIdentifier, false);

        // Attempt to fetch the Google Play Services fields
        if (GpsHelper.isPlayServicesAvailable(applicationContext)) {
            // We can do this synchronously because urlRewrite happens in a background thread.
            GpsHelper.AdvertisingInfo playServicesAdInfo = GpsHelper.fetchAdvertisingInfoSync(applicationContext);
            if (playServicesAdInfo != null) {
                prefix = IFA_PREFIX;
                cdertisingInfo = playServicesAdInfo;
            }
        }

        // Fill in the templates
        String toReturn = url.replace(UDID_TEMPLATE, Uri.encode(prefix + cdertisingInfo.cdertisingId));
        toReturn = toReturn.replace(DO_NOT_TRACK_TEMPLATE, cdertisingInfo.limitAdTracking ? "1" : "0");
        return toReturn;
    }
}
