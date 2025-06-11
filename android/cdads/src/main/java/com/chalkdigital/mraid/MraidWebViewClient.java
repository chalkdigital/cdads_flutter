package com.chalkdigital.mraid;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.chalkdigital.ads.resource.MraidJavascript;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.network.retrofit.CDAdParams;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;

/**
 * Handles injecting the MRAID javascript when encountering mraid.js urls
 */
public class MraidWebViewClient extends WebViewClient {

    public static final String MRAID_JS = "mraid.js";
    private static final String MRAID_INJECTION_JAVASCRIPT = "javascript:"
            + MraidJavascript.JAVASCRIPT_SOURCE;

    @SuppressWarnings("deprecation") // new method will simply call this one
    @Override
    public WebResourceResponse shouldInterceptRequest(@NonNull final WebView view,
            @NonNull final String url) {
        if(url.toLowerCase().contains("/favicon.ico")) {
            try {
                return new WebResourceResponse("image/png", null, null);
            } catch (Exception e) {

            }
        }
        else if (url.toLowerCase().contains(CDAdParams.webViewBaseUrl)){
            try {
                return new WebResourceResponse("text/html", null, null);
            } catch (Exception e) {
                CDAdLog.d("Failed to respond for : "+url);
            }
        }
        if (matchesInjectionUrl(url)) {
            return createMraidInjectionResponse();
        } else {
            return super.shouldInterceptRequest(view, url);
        }
    }

    @VisibleForTesting
    boolean matchesInjectionUrl(@NonNull final String url) {
        final Uri uri = Uri.parse(url.toLowerCase(Locale.US));
        return MRAID_JS.equals(uri.getLastPathSegment());
    }

    private WebResourceResponse createMraidInjectionResponse() {
        InputStream data = new ByteArrayInputStream(MRAID_INJECTION_JAVASCRIPT.getBytes());
        return new WebResourceResponse("text/javascript", "UTF-8", data);
    }
}
