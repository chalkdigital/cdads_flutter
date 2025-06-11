package com.chalkdigital.spark;

import android.support.annotation.NonNull;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public interface SparkWebViewDebugListener {
    /**
     * @see WebChromeClient#onJsAlert(WebView, String, String, JsResult)
     */
    boolean onJsAlert(@NonNull String message, @NonNull JsResult result);

    /**
     * @see WebChromeClient#onConsoleMessage(ConsoleMessage)
     */
    boolean onConsoleMessage(@NonNull ConsoleMessage consoleMessage);
}
