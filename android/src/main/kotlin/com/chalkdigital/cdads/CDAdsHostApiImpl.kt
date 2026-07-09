package com.chalkdigital.cdads

import android.app.Activity
import com.google.android.gms.appset.AppSet
import com.google.android.gms.tasks.Tasks
import com.chalkdigital.cdads.banner.CDABannerListener
import com.chalkdigital.cdads.generated.CDAdsConfig
import com.chalkdigital.cdads.generated.CDAdsError
import com.chalkdigital.cdads.generated.CDAdsFlutterApi
import com.chalkdigital.cdads.generated.CDAdsGeoInfo
import com.chalkdigital.cdads.generated.CDAdsHostApi
import com.chalkdigital.cdads.generated.CDAdsNativeAdData
import com.chalkdigital.cdads.generated.CDAdsRequest
import com.chalkdigital.cdads.generated.CDAdsReward
import com.chalkdigital.cdads.interstitial.CDAInterstitialListener
import com.chalkdigital.cdads.native.CDANativeAdListener
import com.chalkdigital.cdads.rewarded.CDARewardedVideoListener
import com.chalkdigital.cdads.CDAdsConfiguration as SdkConfig
import com.chalkdigital.cdads.CDAdsAdRequest as SdkRequest
import com.chalkdigital.cdads.CDAdsGeoInfo as SdkGeoInfo
import com.chalkdigital.cdads.CDAdsError as SdkError
import com.chalkdigital.cdads.CDANativeAdData as SdkNativeAdData
import com.chalkdigital.cdads.CDAReward as SdkReward

/** Implements the Pigeon CDAdsHostApi — wires Dart calls to the native CDAds SDK. */
internal class CDAdsHostApiImpl(
    private val flutterApi: CDAdsFlutterApi,
    private val activityProvider: () -> Activity?,
) : CDAdsHostApi {

    @Volatile private var cachedVendorId: String = ""

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun initialize(config: CDAdsConfig) {
        val activity = activityProvider() ?: return
        val appContext = activity.applicationContext
        CDAds.initialize(
            context = appContext,
            configuration = config.toSdk(),
        )
        Thread {
            try {
                cachedVendorId = Tasks.await(AppSet.getClient(appContext).appSetIdInfo).id
            } catch (_: Exception) {}
        }.start()
    }

    override fun updateConsent(gdprApplies: Boolean, hasConsent: Boolean) {
        CDAds.updateConsent(gdprApplies, hasConsent)
    }

    override fun setLocationEnabled(enabled: Boolean) {
        CDAds.setLocationEnabled(enabled)
    }

    // ── Banner ────────────────────────────────────────────────────────────────

    override fun loadBanner(request: CDAdsRequest) {
        val view = CDABannerPlatformViewRegistry.get(request.adUnitId) ?: return
        view.bannerView.listener = BannerForwarder(request.adUnitId, flutterApi)
        view.bannerView.load(request.toSdk())
    }

    override fun destroyBanner(adUnitId: String) {
        CDABannerPlatformViewRegistry.get(adUnitId)?.bannerView?.destroy()
        CDABannerPlatformViewRegistry.remove(adUnitId)
    }

    // ── Interstitial ──────────────────────────────────────────────────────────

    override fun loadInterstitial(request: CDAdsRequest) {
        val ad = CDAds.interstitialManager.getOrCreate(request.adUnitId)
        ad.listener = InterstitialForwarder(request.adUnitId, flutterApi)
        ad.load(request.toSdk())
    }

    override fun isInterstitialReady(adUnitId: String): Boolean =
        CDAds.interstitialManager.isReady(adUnitId)

    override fun showInterstitial(adUnitId: String) {
        val activity = activityProvider() ?: return
        CDAds.interstitialManager.getOrCreate(adUnitId).show(activity)
    }

    override fun destroyInterstitial(adUnitId: String) {
        CDAds.interstitialManager.destroy(adUnitId)
    }

    // ── Rewarded Video ────────────────────────────────────────────────────────

    override fun loadRewardedVideo(request: CDAdsRequest) {
        val ad = CDAds.rewardedVideoManager.getOrCreate(request.adUnitId)
        ad.listener = RewardedForwarder(request.adUnitId, flutterApi)
        ad.load(request.toSdk())
    }

    override fun isRewardedVideoReady(adUnitId: String): Boolean =
        CDAds.rewardedVideoManager.isReady(adUnitId)

    override fun showRewardedVideo(adUnitId: String) {
        val activity = activityProvider() ?: return
        CDAds.rewardedVideoManager.getOrCreate(adUnitId).show(activity)
    }

    // ── Native Ad ─────────────────────────────────────────────────────────────

    override fun loadNativeAd(request: CDAdsRequest) {
        val nativeRequest = com.chalkdigital.cdads.native.CDANativeAdRequest()
        nativeRequest.listener = NativeAdForwarder(request.adUnitId, flutterApi)
        nativeRequest.load(request.toSdk())
    }

    override fun trackNativeAdImpression(adId: String) {
        CDAds.nativeAdManager.trackImpression(adId)
    }

    override fun trackNativeAdClick(adId: String) {
        CDAds.nativeAdManager.trackClick(adId)
    }

    override fun destroyNativeAd(adUnitId: String) {
        CDAds.nativeAdManager.destroy(adUnitId)
    }

    // ── In-app debug log ─────────────────────────────────────────────────────

    override fun isDebugFileLoggingEnabled(): Boolean = CDAds.isDebugFileLoggingEnabled

    override fun setDebugFileLoggingEnabled(enabled: Boolean) {
        CDAds.isDebugFileLoggingEnabled = enabled
    }

    override fun debugLogs(): String = CDAds.debugLogs()

    override fun clearDebugLogs() {
        CDAds.clearDebugLogs()
    }

    override fun showDebugLogViewer() {
        val activity = activityProvider() ?: return
        activity.startActivity(android.content.Intent(activity, CDADebugLogActivity::class.java))
    }

    override fun getVendorId(): String = cachedVendorId
}

// ── Type conversion extensions ────────────────────────────────────────────────

private fun CDAdsConfig.toSdk(): SdkConfig = SdkConfig(
    partnerKey = partnerKey,
    host = host,
    appName = appName,
    applicationIABCategory = applicationIabCategory,
    isTestEnvironment = isTestEnvironment,
    logLevel = logLevel.toSdkLogLevel(),
    gdprApplies = gdprApplies,
    hasConsent = hasConsent,
    enableTracking = enableTracking,
    enableLocationTracking = enableLocationTracking,
    locationDistanceFilter = locationDistanceFilter.toFloat(),
    locationUpdateInterval = (locationUpdateInterval * 1000).toLong(),
    locationExpiryInterval = (locationExpiryInterval * 1000).toLong(),
    clientHasUserTrackingPermission = clientHasUserTrackingPermission,
)

private fun com.chalkdigital.cdads.generated.CDAdsLogLevel.toSdkLogLevel(): SdkConfig.LogLevel =
    when (this) {
        com.chalkdigital.cdads.generated.CDAdsLogLevel.ALL   -> SdkConfig.LogLevel.ALL
        com.chalkdigital.cdads.generated.CDAdsLogLevel.TRACE -> SdkConfig.LogLevel.TRACE
        com.chalkdigital.cdads.generated.CDAdsLogLevel.DEBUG -> SdkConfig.LogLevel.DEBUG
        com.chalkdigital.cdads.generated.CDAdsLogLevel.INFO  -> SdkConfig.LogLevel.INFO
        com.chalkdigital.cdads.generated.CDAdsLogLevel.WARN  -> SdkConfig.LogLevel.WARN
        com.chalkdigital.cdads.generated.CDAdsLogLevel.ERROR -> SdkConfig.LogLevel.ERROR
        com.chalkdigital.cdads.generated.CDAdsLogLevel.FATAL -> SdkConfig.LogLevel.ERROR
        com.chalkdigital.cdads.generated.CDAdsLogLevel.OFF   -> SdkConfig.LogLevel.OFF
    }

private fun CDAdsRequest.toSdk(): SdkRequest = SdkRequest(
    adUnitId = adUnitId,
    keywords = keywords,
    targetingYearOfBirth = targetingYearOfBirth,
    targetingGender = targetingGender,
    targetingIncome = targetingIncome,
    targetingEducation = targetingEducation,
    targetingLanguage = targetingLanguage,
    locationAutoUpdateEnabled = locationAutoUpdateEnabled,
    geoInfo = geoInfo?.toSdk(),
    ipAddress = ipAddress,
)

private fun CDAdsGeoInfo.toSdk(): SdkGeoInfo = SdkGeoInfo(
    latitude = lat,
    longitude = lon,
    sourceType = type.toInt(),
    countryCode = countryCode,
    region = region,
    city = city,
    zip = zip,
    streetAddress = streetAddress,
    horizontalAccuracy = horizontalAccuracy,
)

private fun SdkError.toPigeon(): CDAdsError =
    CDAdsError(code = code.rawValue.toLong(), message = message)

private fun SdkNativeAdData.toPigeon(): CDAdsNativeAdData = CDAdsNativeAdData(
    adId = adUnitId,
    adUnitId = adUnitId,
    impressionTrackers = impressionTrackers,
    clickTrackers = clickTrackers,
    title = title,
    body = body,
    callToAction = callToAction,
    advertiser = sponsoredText,
    sponsoredLabel = sponsoredText,
    mainImageUrl = mainImageUrl,
    iconImageUrl = iconUrl,
    starRating = starRating,
    price = price,
    clickUrl = clickUrl,
)

private fun SdkReward.toPigeon(): CDAdsReward =
    CDAdsReward(currencyType = currencyType, amount = amount.toLong())

// ── Event forwarders ──────────────────────────────────────────────────────────

private class BannerForwarder(
    private val adUnitId: String,
    private val api: CDAdsFlutterApi,
) : CDABannerListener {
    override fun onBannerLoaded(adUnitId: String) =
        api.onBannerLoaded(adUnitId) {}
    override fun onBannerFailedToLoad(adUnitId: String, error: SdkError) =
        api.onBannerFailedToLoad(adUnitId, error.toPigeon()) {}
    override fun onBannerImpression(adUnitId: String) =
        api.onBannerImpression(adUnitId) {}
    override fun onBannerClicked(adUnitId: String) =
        api.onBannerClicked(adUnitId) {}
    override fun onBannerWillLeaveApplication(adUnitId: String) =
        api.onBannerWillLeaveApp(adUnitId) {}
    override fun onBannerExpanded(adUnitId: String) =
        api.onBannerExpanded(adUnitId) {}
    override fun onBannerCollapsed(adUnitId: String) =
        api.onBannerCollapsed(adUnitId) {}
    override fun onBannerDidClose(adUnitId: String) =
        api.onBannerDidClose(adUnitId) {}
}

private class InterstitialForwarder(
    private val adUnitId: String,
    private val api: CDAdsFlutterApi,
) : CDAInterstitialListener {
    override fun onInterstitialLoaded(adUnitId: String) =
        api.onInterstitialLoaded(adUnitId) {}
    override fun onInterstitialFailedToLoad(adUnitId: String, error: SdkError) =
        api.onInterstitialFailedToLoad(adUnitId, error.toPigeon()) {}
    override fun onInterstitialShown(adUnitId: String) =
        api.onInterstitialWillAppear(adUnitId) {}
    override fun onInterstitialDismissed(adUnitId: String) =
        api.onInterstitialDidDisappear(adUnitId) {}
    override fun onInterstitialExpired(adUnitId: String) =
        api.onInterstitialExpired(adUnitId) {}
    override fun onInterstitialClicked(adUnitId: String) =
        api.onInterstitialClicked(adUnitId) {}
}

private class RewardedForwarder(
    private val adUnitId: String,
    private val api: CDAdsFlutterApi,
) : CDARewardedVideoListener {
    override fun onRewardedVideoLoaded(adUnitId: String) =
        api.onRewardedVideoLoaded(adUnitId) {}
    override fun onRewardedVideoFailedToLoad(adUnitId: String, error: SdkError) =
        api.onRewardedVideoFailedToLoad(adUnitId, error.toPigeon()) {}
    override fun onRewardedVideoShown(adUnitId: String) =
        api.onRewardedVideoWillAppear(adUnitId) {}
    override fun onRewardedVideoCompleted(adUnitId: String) =
        api.onRewardedVideoDidAppear(adUnitId) {}
    override fun onRewardedVideoDismissed(adUnitId: String) =
        api.onRewardedVideoDidDisappear(adUnitId) {}
    override fun onRewardedVideoExpired(adUnitId: String) =
        api.onRewardedVideoExpired(adUnitId) {}
    override fun onRewardedVideoClicked(adUnitId: String) =
        api.onRewardedVideoClicked(adUnitId) {}
    override fun onUserEarnedReward(adUnitId: String, reward: SdkReward) =
        api.onRewardedVideoEarnedReward(adUnitId, reward.toPigeon()) {}
}

private class NativeAdForwarder(
    private val adUnitId: String,
    private val api: CDAdsFlutterApi,
) : CDANativeAdListener {
    override fun onNativeAdLoaded(adUnitId: String, data: SdkNativeAdData) =
        api.onNativeAdLoaded(data.toPigeon()) {}
    override fun onNativeAdFailedToLoad(adUnitId: String, error: SdkError) =
        api.onNativeAdFailedToLoad(adUnitId, error.toPigeon()) {}
    override fun onNativeAdExpired(adUnitId: String) =
        api.onNativeAdExpired(adUnitId) {}
}
