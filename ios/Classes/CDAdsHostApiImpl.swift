import Flutter
import UIKit
import CDAds

/// Implements the Pigeon-generated CDAdsHostApi protocol.
/// Translates every Dart call into the corresponding CDAds Swift SDK call,
/// and fires CDAdsFlutterApi events back to Dart when the SDK fires callbacks.
@MainActor
final class CDAdsHostApiImpl: NSObject, CDAdsHostApi {

    // Sends events from native → Dart
    private let flutterApi: CDAdsFlutterApi

    // Strong references to banner delegates — CDABannerView.delegate is weak,
    // so without this the forwarder is deallocated before the ad loads and
    // bannerDidLoad never fires.
    private var bannerForwarders: [String: BannerEventForwarder] = [:]

    init(messenger: FlutterBinaryMessenger) {
        self.flutterApi = CDAdsFlutterApi(binaryMessenger: messenger)
    }

    // MARK: - SDK Lifecycle

    func initialize(config: CDAdsConfig) throws {
        var sdkConfig = CDAdsConfiguration(
            partnerKey:             config.partnerKey,
            host:                   config.host,
            appName:                config.appName,
            applicationIABCategory: config.applicationIabCategory
        )
        sdkConfig.environment                     = config.isTestEnvironment ? .test : .production
        sdkConfig.logLevel                        = CDAdsConfiguration.LogLevel(rawValue: config.logLevel.rawValue) ?? .off
        sdkConfig.gdprApplies                     = config.gdprApplies
        sdkConfig.hasConsent                      = config.hasConsent
        sdkConfig.enableTracking                  = config.enableTracking
        sdkConfig.enableLocationTracking          = config.enableLocationTracking
        sdkConfig.locationDistanceFilter          = config.locationDistanceFilter
        sdkConfig.locationUpdateInterval          = config.locationUpdateInterval
        sdkConfig.locationExpiryInterval          = config.locationExpiryInterval
        sdkConfig.clientHasUserTrackingPermission = config.clientHasUserTrackingPermission

        let sdk = CDAds.initialize(with: sdkConfig)

        // Wire SDK-level events back to Flutter
        sdk.location.onLocationUpdated = { [weak self] geo in
            self?.flutterApi.onLocationUpdated(location: geo.toPigeon()) { _ in }
        }
    }

    func updateConsent(gdprApplies: Bool, hasConsent: Bool) throws {
        CDAds.shared.updateConsent(gdprApplies: gdprApplies, hasConsent: hasConsent)
    }

    func setLocationEnabled(enabled: Bool) throws {
        CDAds.shared.setLocationEnabled(enabled)
    }

    // MARK: - Banner

    func loadBanner(request: CDAdsRequest) throws {
        guard let bannerView = CDABannerPlatformView.registry[request.adUnitId] else {
            throw CDAds.AdError(.invalidRequest, "No banner PlatformView registered for \(request.adUnitId)")
        }
        let forwarder = BannerEventForwarder(adUnitId: request.adUnitId, flutterApi: flutterApi)
        bannerForwarders[request.adUnitId] = forwarder
        bannerView.delegate = forwarder
        bannerView.load(request: request.toSDK())
    }

    func destroyBanner(adUnitId: String) throws {
        CDABannerPlatformView.registry[adUnitId]?.destroy()
        CDABannerPlatformView.registry.removeValue(forKey: adUnitId)
        bannerForwarders.removeValue(forKey: adUnitId)
    }

    // MARK: - Interstitial

    func loadInterstitial(request: CDAdsRequest) throws {
        let forwarder = InterstitialEventForwarder(adUnitId: request.adUnitId, flutterApi: flutterApi)
        CDAds.shared.interstitialManager.load(request: request.toSDK(), delegate: forwarder)
    }

    func isInterstitialReady(adUnitId: String) throws -> Bool {
        CDAds.shared.interstitialManager.isReady(adUnitId: adUnitId)
    }

    func showInterstitial(adUnitId: String) throws {
        guard let vc = topViewController() else {
            throw CDAds.AdError(.invalidRequest, "Could not find root view controller to present interstitial")
        }
        CDAds.shared.interstitialManager.show(adUnitId: adUnitId, from: vc)
    }

    func destroyInterstitial(adUnitId: String) throws {
        CDAds.shared.interstitialManager.destroy(adUnitId: adUnitId)
    }

    // MARK: - Rewarded Video

    func loadRewardedVideo(request: CDAdsRequest) throws {
        let forwarder = RewardedEventForwarder(adUnitId: request.adUnitId, flutterApi: flutterApi)
        CDAds.shared.rewardedVideoManager.load(request: request.toSDK(), delegate: forwarder)
    }

    func isRewardedVideoReady(adUnitId: String) throws -> Bool {
        CDAds.shared.rewardedVideoManager.isReady(adUnitId: adUnitId)
    }

    func showRewardedVideo(adUnitId: String) throws {
        guard let vc = topViewController() else {
            throw CDAds.AdError(.invalidRequest, "Could not find root view controller to present rewarded video")
        }
        CDAds.shared.rewardedVideoManager.show(adUnitId: adUnitId, from: vc)
    }

    // MARK: - Native Ads

    func loadNativeAd(request: CDAdsRequest) throws {
        let forwarder = NativeAdEventForwarder(adUnitId: request.adUnitId, flutterApi: flutterApi)
        CDAds.shared.nativeAdManager.load(request: request.toSDK(), delegate: forwarder)
    }

    func trackNativeAdImpression(adId: String) throws {
        CDAds.shared.nativeAdManager.trackImpression(adId: adId)
    }

    func trackNativeAdClick(adId: String) throws {
        CDAds.shared.nativeAdManager.trackClick(adId: adId)
    }

    func destroyNativeAd(adUnitId: String) throws {
        CDAds.shared.nativeAdManager.destroy(adUnitId: adUnitId)
    }

    // MARK: - In-app debug log

    func isDebugFileLoggingEnabled() throws -> Bool {
        CDAds.isDebugFileLoggingEnabled
    }

    func setDebugFileLoggingEnabled(enabled: Bool) throws {
        CDAds.isDebugFileLoggingEnabled = enabled
    }

    func debugLogs() throws -> String {
        CDAds.debugLogs()
    }

    func clearDebugLogs() throws {
        CDAds.clearDebugLogs()
    }

    func showDebugLogViewer() throws {
        guard let vc = topViewController() else { return }
        let debugVC = CDADebugLogViewController()
        let nav = UINavigationController(rootViewController: debugVC)
        vc.present(nav, animated: true)
    }

    func getVendorId() throws -> String {
        UIDevice.current.identifierForVendor?.uuidString ?? ""
    }

    // MARK: - Helpers

    private func topViewController() -> UIViewController? {
        guard let windowScene = UIApplication.shared.connectedScenes
            .compactMap({ $0 as? UIWindowScene })
            .first(where: { $0.activationState == .foregroundActive }),
              let rootVC = windowScene.windows.first(where: { $0.isKeyWindow })?.rootViewController
        else { return nil }

        var top = rootVC
        while let presented = top.presentedViewController {
            top = presented
        }
        return top
    }
}

// MARK: - Pigeon/SDK type conversions

extension CDAdsRequest {
    func toSDK() -> CDAdsAdRequest {
        var req = CDAdsAdRequest(adUnitId: adUnitId)
        req.keywords               = keywords
        req.targetingYearOfBirth   = targetingYearOfBirth
        req.targetingGender        = targetingGender
        req.targetingIncome        = targetingIncome
        req.targetingEducation     = targetingEducation
        req.targetingLanguage      = targetingLanguage
        req.locationAutoUpdateEnabled = locationAutoUpdateEnabled
        req.geoInfo                = geoInfo?.toSDK()
        req.ipAddress              = ipAddress
        return req
    }
}

extension CDAdsGeoInfo {
    func toSDK() -> CDAds.GeoInfo {
        var geo = CDAds.GeoInfo(latitude: lat, longitude: lon)
        geo.sourceType           = CDAds.GeoInfo.SourceType(rawValue: Int(type)) ?? .gps
        geo.countryCode          = countryCode
        geo.region               = region
        geo.city                 = city
        geo.zip                  = zip
        geo.streetAddress        = streetAddress
        geo.horizontalAccuracy   = horizontalAccuracy
        return geo
    }
}

extension CDAds.GeoInfo {
    func toPigeon() -> CDAdsGeoInfo {
        CDAdsGeoInfo(
            lat:               latitude,
            lon:               longitude,
            type:              Int64(sourceType.rawValue),
            countryCode:       countryCode,
            region:            region,
            city:              city,
            zip:               zip,
            streetAddress:     streetAddress,
            horizontalAccuracy: horizontalAccuracy
        )
    }
}

extension CDAds.AdError {
    func toPigeon() -> CDAdsError {
        CDAdsError(code: Int64(code.rawValue), message: message)
    }
}

extension CDAds.NativeAdData {
    func toPigeon() -> CDAdsNativeAdData {
        CDAdsNativeAdData(
            adId:               adId,
            adUnitId:           adUnitId,
            impressionTrackers: impressionTrackers.map { $0.absoluteString },
            clickTrackers:      clickTrackers.map { $0.absoluteString },
            title:              title,
            body:               body,
            callToAction:       callToAction,
            advertiser:         advertiser,
            sponsoredLabel:     sponsoredLabel,
            mainImageUrl:       mainImageURL?.absoluteString,
            iconImageUrl:       iconImageURL?.absoluteString,
            starRating:         starRating,
            price:              price,
            clickUrl:           clickURL?.absoluteString,
            vastTag:            vastTagURL?.absoluteString,
            extras:             extras
        )
    }
}

extension CDAds.Reward {
    func toPigeon() -> CDAdsReward {
        CDAdsReward(currencyType: currencyType, amount: Int64(amount))
    }
}

// MARK: - Event Forwarders
// Each forwarder implements one SDK delegate protocol and fires the
// corresponding Pigeon CDAdsFlutterApi call back to Dart.

private final class BannerEventForwarder: CDABannerDelegate {
    private let adUnitId: String
    private let flutterApi: CDAdsFlutterApi

    init(adUnitId: String, flutterApi: CDAdsFlutterApi) {
        self.adUnitId  = adUnitId
        self.flutterApi = flutterApi
    }

    func bannerDidLoad(_ banner: CDABannerView) {
        flutterApi.onBannerLoaded(adUnitId: adUnitId) { _ in }
    }
    func bannerDidFailToLoad(_ banner: CDABannerView, error: CDAds.AdError) {
        flutterApi.onBannerFailedToLoad(adUnitId: adUnitId, error: error.toPigeon()) { _ in }
    }
    func bannerDidReceiveTap(_ banner: CDABannerView) {
        flutterApi.onBannerClicked(adUnitId: adUnitId) { _ in }
    }
    func bannerWillLeaveApplication(_ banner: CDABannerView) {
        flutterApi.onBannerWillLeaveApp(adUnitId: adUnitId) { _ in }
    }
    func bannerDidExpand(_ banner: CDABannerView) {
        flutterApi.onBannerExpanded(adUnitId: adUnitId) { _ in }
    }
    func bannerDidCollapse(_ banner: CDABannerView) {
        flutterApi.onBannerCollapsed(adUnitId: adUnitId) { _ in }
    }
    func bannerDidClose(_ banner: CDABannerView) {
        flutterApi.onBannerDidClose(adUnitId: adUnitId) { _ in }
    }
}

private final class InterstitialEventForwarder: CDAInterstitialDelegate {
    private let adUnitId: String
    private let flutterApi: CDAdsFlutterApi

    init(adUnitId: String, flutterApi: CDAdsFlutterApi) {
        self.adUnitId   = adUnitId
        self.flutterApi = flutterApi
    }

    func interstitialDidLoad(_ ad: CDAInterstitialAd) {
        flutterApi.onInterstitialLoaded(adUnitId: adUnitId) { _ in }
    }
    func interstitialDidFailToLoad(_ ad: CDAInterstitialAd, error: CDAds.AdError) {
        flutterApi.onInterstitialFailedToLoad(adUnitId: adUnitId, error: error.toPigeon()) { _ in }
    }
    func interstitialWillAppear(_ ad: CDAInterstitialAd) {
        flutterApi.onInterstitialWillAppear(adUnitId: adUnitId) { _ in }
    }
    func interstitialDidAppear(_ ad: CDAInterstitialAd) {
        flutterApi.onInterstitialDidAppear(adUnitId: adUnitId) { _ in }
    }
    func interstitialWillDisappear(_ ad: CDAInterstitialAd) {
        flutterApi.onInterstitialWillDisappear(adUnitId: adUnitId) { _ in }
    }
    func interstitialDidDisappear(_ ad: CDAInterstitialAd) {
        flutterApi.onInterstitialDidDisappear(adUnitId: adUnitId) { _ in }
    }
    func interstitialDidExpire(_ ad: CDAInterstitialAd) {
        flutterApi.onInterstitialExpired(adUnitId: adUnitId) { _ in }
    }
    func interstitialDidReceiveTap(_ ad: CDAInterstitialAd) {
        flutterApi.onInterstitialClicked(adUnitId: adUnitId) { _ in }
    }
    func interstitialWillLeaveApplication(_ ad: CDAInterstitialAd) {
        flutterApi.onInterstitialWillLeaveApp(adUnitId: adUnitId) { _ in }
    }
}

private final class RewardedEventForwarder: CDARewardedVideoDelegate {
    private let adUnitId: String
    private let flutterApi: CDAdsFlutterApi

    init(adUnitId: String, flutterApi: CDAdsFlutterApi) {
        self.adUnitId   = adUnitId
        self.flutterApi = flutterApi
    }

    func rewardedVideoDidLoad(_ ad: CDARewardedVideoAd) {
        flutterApi.onRewardedVideoLoaded(adUnitId: adUnitId) { _ in }
    }
    func rewardedVideoDidFailToLoad(_ ad: CDARewardedVideoAd, error: CDAds.AdError) {
        flutterApi.onRewardedVideoFailedToLoad(adUnitId: adUnitId, error: error.toPigeon()) { _ in }
    }
    func rewardedVideoDidFailToPlay(_ ad: CDARewardedVideoAd, error: CDAds.AdError) {
        flutterApi.onRewardedVideoFailedToPlay(adUnitId: adUnitId, error: error.toPigeon()) { _ in }
    }
    func rewardedVideoDidExpire(_ ad: CDARewardedVideoAd) {
        flutterApi.onRewardedVideoExpired(adUnitId: adUnitId) { _ in }
    }
    func rewardedVideoWillAppear(_ ad: CDARewardedVideoAd) {
        flutterApi.onRewardedVideoWillAppear(adUnitId: adUnitId) { _ in }
    }
    func rewardedVideoDidAppear(_ ad: CDARewardedVideoAd) {
        flutterApi.onRewardedVideoDidAppear(adUnitId: adUnitId) { _ in }
    }
    func rewardedVideoWillDisappear(_ ad: CDARewardedVideoAd) {
        flutterApi.onRewardedVideoWillDisappear(adUnitId: adUnitId) { _ in }
    }
    func rewardedVideoDidDisappear(_ ad: CDARewardedVideoAd) {
        flutterApi.onRewardedVideoDidDisappear(adUnitId: adUnitId) { _ in }
    }
    func rewardedVideoDidReceiveTap(_ ad: CDARewardedVideoAd) {
        flutterApi.onRewardedVideoClicked(adUnitId: adUnitId) { _ in }
    }
    func rewardedVideoWillLeaveApplication(_ ad: CDARewardedVideoAd) {
        flutterApi.onRewardedVideoWillLeaveApp(adUnitId: adUnitId) { _ in }
    }
    func rewardedVideoShouldRewardUser(_ ad: CDARewardedVideoAd, reward: CDAds.Reward) {
        flutterApi.onRewardedVideoEarnedReward(adUnitId: adUnitId, reward: reward.toPigeon()) { _ in }
    }
}

private final class NativeAdEventForwarder: CDANativeAdDelegate {
    private let adUnitId: String
    private let flutterApi: CDAdsFlutterApi

    init(adUnitId: String, flutterApi: CDAdsFlutterApi) {
        self.adUnitId   = adUnitId
        self.flutterApi = flutterApi
    }

    func nativeAdDidLoad(_ request: CDANativeAdRequest, data: CDAds.NativeAdData) {
        // Store for impression/click tracking, then send assets to Flutter.
        // SDK delegate callbacks are always delivered on the main thread.
        MainActor.assumeIsolated {
            CDAds.shared.nativeAdManager.storeLoadedAd(data)
        }
        flutterApi.onNativeAdLoaded(data: data.toPigeon()) { _ in }
    }
    func nativeAdDidFailToLoad(_ request: CDANativeAdRequest, error: CDAds.AdError) {
        flutterApi.onNativeAdFailedToLoad(adUnitId: adUnitId, error: error.toPigeon()) { _ in }
    }
    func nativeAdDidExpire(_ request: CDANativeAdRequest) {
        flutterApi.onNativeAdExpired(adUnitId: adUnitId) { _ in }
    }
}
