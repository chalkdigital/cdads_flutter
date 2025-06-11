package com.chalkdigital.ads;

public interface HtmlWebViewListener {
    void onLoaded(BaseHtmlWebView mHtmlWebView);
    void onFailed(CDAdErrorCode unspecified);
    void onClicked();
    void onCollapsed();
}
