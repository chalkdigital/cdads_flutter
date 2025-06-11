package com.chalkdigital.spark;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.chalkdigital.ads.resource.SparkJavascript;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.network.retrofit.CDAdParams;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;

public class SparkWebViewClient extends WebViewClient {
    public static final String SPARK_JS = "spark.js";
    private static String SPARK_INJECTION_JAVASCRIPT = "";

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
        SPARK_INJECTION_JAVASCRIPT = "javascript:"
                + SparkJavascript.getJavascriptSource(view.getContext());
        if (matchesInjectionUrl(url)) {
            return createSparkInjectionResponse();
        } else {
            return super.shouldInterceptRequest(view, url);
        }
    }

    @VisibleForTesting
    boolean matchesInjectionUrl(@NonNull final String url) {
        final Uri uri = Uri.parse(url.toLowerCase(Locale.US));
        return SPARK_JS.equals(uri.getLastPathSegment());
    }

    private WebResourceResponse createSparkInjectionResponse() {
        InputStream data = new ByteArrayInputStream(SPARK_INJECTION_JAVASCRIPT.getBytes());
        return new WebResourceResponse("text/javascript", "UTF-8", data);
    }
}
