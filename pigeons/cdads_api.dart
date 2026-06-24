// =============================================================================
// CDAds Flutter Plugin — Pigeon API Contract
// =============================================================================
// This file is the single source of truth for the Flutter ↔ Native boundary.
// Run code generation with:
//   dart run pigeon --input pigeons/cdads_api.dart
//
// iOS output  → ios/Classes/generated/CDAdsApi.g.swift
// Android     → android/src/main/kotlin/com/chalkdigital/cdads/generated/CDAdsApi.g.kt
// Dart        → lib/src/generated/cdads_api.g.dart
// =============================================================================

import 'package:pigeon/pigeon.dart';

@ConfigurePigeon(
  PigeonOptions(
    dartOut: 'lib/src/generated/cdads_api.g.dart',
    dartOptions: DartOptions(),
    swiftOut: 'ios/Classes/generated/CDAdsApi.g.swift',
    swiftOptions: SwiftOptions(),
    kotlinOut:
        'android/src/main/kotlin/com/chalkdigital/cdads/generated/CDAdsApi.g.kt',
    kotlinOptions: KotlinOptions(package: 'com.chalkdigital.cdads.generated'),
    copyrightHeader: 'pigeons/copyright.txt',
  ),
)

// =============================================================================
// DATA CLASSES
// =============================================================================

/// SDK initialisation configuration.
/// Maps to CDInitialisationParams on iOS and CDAdsInitialisationParams on Android.
///
/// partnerKey/host/appName/applicationIabCategory all default to empty and
/// fall back to the host app's Info.plist `CDAdsParams` dict (iOS) /
/// AndroidManifest.xml `com.chalkdigital.cdads.*` meta-data (Android) when
/// left empty — a non-empty value here always takes precedence.
class CDAdsConfig {
  CDAdsConfig({
    this.partnerKey = '',
    this.host = '',
    this.appName = '',
    this.applicationIabCategory = '',
    this.isTestEnvironment = false,
    this.logLevel = CDAdsLogLevel.off,
    this.gdprApplies = false,
    this.hasConsent = false,
    this.enableTracking = false,
    this.enableLocationTracking = false,
    this.locationDistanceFilter = 100.0,
    this.locationUpdateInterval = 30.0,
    this.locationExpiryInterval = 300.0,
    this.clientHasUserTrackingPermission = false,
  });

  final String partnerKey;
  final String host;
  final String appName;
  final String applicationIabCategory;
  final bool isTestEnvironment;
  final CDAdsLogLevel logLevel;
  final bool gdprApplies;
  final bool hasConsent;
  final bool enableTracking;

  /// Whether the SDK should track and batch-upload device location.
  /// Requires location permission to be granted by the app before enabling.
  final bool enableLocationTracking;

  /// Minimum distance (metres) before a location update is triggered.
  final double locationDistanceFilter;

  /// How often (seconds) to request a location update.
  final double locationUpdateInterval;

  /// How long (seconds) a cached location is considered valid for ad targeting.
  final double locationExpiryInterval;

  /// Pass true once the host app has obtained ATT / IDFA permission (iOS)
  /// or confirmed the user is not opted-out (Android).
  final bool clientHasUserTrackingPermission;
}

/// Per-request targeting parameters.
/// Attach to every ad load call to improve ad relevance.
class CDAdsRequest {
  CDAdsRequest({
    required this.adUnitId,
    this.keywords,
    this.targetingYearOfBirth,
    this.targetingGender,
    this.targetingIncome,
    this.targetingEducation,
    this.targetingLanguage,
    this.locationAutoUpdateEnabled = false,
    this.geoInfo,
    this.ipAddress,
  });

  final String adUnitId;
  final String? keywords;
  final String? targetingYearOfBirth;
  final String? targetingGender;
  final String? targetingIncome;
  final String? targetingEducation;
  final String? targetingLanguage;

  /// When true the SDK will attach the latest known device location to the request.
  final bool locationAutoUpdateEnabled;

  /// Manual geo override. Takes precedence over device location when provided.
  final CDAdsGeoInfo? geoInfo;

  /// Manual IP address override. Takes precedence over the SDK's auto-fetched public IP when set.
  final String? ipAddress;
}

/// Geo-location data for ad targeting.
class CDAdsGeoInfo {
  CDAdsGeoInfo({
    required this.lat,
    required this.lon,
    this.type = 1,
    this.countryCode,
    this.region,
    this.city,
    this.zip,
    this.streetAddress,
    this.horizontalAccuracy,
  });

  final double lat;
  final double lon;

  /// Location source type: 1 = GPS, 2 = IP, 3 = User-provided.
  final int type;

  final String? countryCode;
  final String? region;
  final String? city;
  final String? zip;
  final String? streetAddress;

  /// Horizontal accuracy in metres.
  final double? horizontalAccuracy;
}

/// Structured data for a native ad — passed to Flutter for rendering.
/// The host app (or plugin consumer) builds the widget from these assets.
class CDAdsNativeAdData {
  CDAdsNativeAdData({
    required this.adId,
    required this.adUnitId,
    required this.impressionTrackers,
    required this.clickTrackers,
    this.title,
    this.body,
    this.callToAction,
    this.advertiser,
    this.sponsoredLabel,
    this.mainImageUrl,
    this.iconImageUrl,
    this.starRating,
    this.price,
    this.clickUrl,
    this.vastTag,
    this.extras,
  });

  /// Unique identifier for this ad instance — used for impression/click tracking.
  final String adId;
  final String adUnitId;

  final List<String?> impressionTrackers;
  final List<String?> clickTrackers;

  final String? title;
  final String? body;
  final String? callToAction;
  final String? advertiser;
  final String? sponsoredLabel;
  final String? mainImageUrl;
  final String? iconImageUrl;
  final double? starRating;
  final String? price;
  final String? clickUrl;

  /// VAST tag URL for native video ads. Present only when adType == video.
  final String? vastTag;

  /// Any extra key-value assets returned by the ad server.
  final Map<String?, String?>? extras;
}

/// Reward granted to the user after completing a rewarded video.
class CDAdsReward {
  CDAdsReward({
    required this.currencyType,
    required this.amount,
  });

  final String currencyType;
  final int amount;
}

/// A platform-level error returned from ad operations.
class CDAdsError {
  CDAdsError({
    required this.code,
    required this.message,
  });

  /// Platform-agnostic error codes defined in [CDAdsErrorCode].
  final int code;
  final String message;
}

// =============================================================================
// ENUMERATIONS
// =============================================================================

/// Log verbosity levels — mirrors CDLogLevel (iOS) and java.util.logging.Level (Android).
enum CDAdsLogLevel {
  all,
  trace,
  debug,
  info,
  warn,
  error,
  fatal,
  off,
}

/// Platform-agnostic error codes.
/// Negative values are reserved for native-side internal errors.
enum CDAdsErrorCode {
  unknown,
  noFill,
  networkError,
  timeout,
  invalidRequest,
  adExpired,
  sdkNotInitialized,
}

/// Banner and MREC size presets.
/// Passed as creation params to the PlatformView. [custom] uses the explicit
/// `customWidth`/`customHeight` creation params instead of a preset size.
enum CDAdsSize {
  banner320x50,
  banner300x50,
  banner300x250,
  banner320x100,
  banner728x90,
  banner728x250,
  banner300x600,
  banner970x250,
  banner480x320,
  banner320x480,
  banner768x1024,
  banner1086x1086,
  custom,
}

// =============================================================================
// HOST API  (Dart → Native)
// Native platforms implement this interface.
// Flutter calls these methods to drive ad lifecycle.
// =============================================================================

@HostApi()
abstract class CDAdsHostApi {
  // ── SDK Lifecycle ──────────────────────────────────────────────────────────

  /// Must be called once, as early as possible in app startup.
  void initialize(CDAdsConfig config);

  /// Update GDPR consent state at runtime (e.g. after the consent dialog).
  void updateConsent({required bool gdprApplies, required bool hasConsent});

  /// Enable or disable device location tracking without full re-initialisation.
  void setLocationEnabled(bool enabled);

  // ── Banner / MREC ──────────────────────────────────────────────────────────
  // Banner and MREC views are rendered via PlatformView.
  // View creation params (adUnitId, size, showCloseButton, isAutoRefreshEnabled,
  // refreshInterval) are passed directly to the PlatformView factory — they're
  // construction-time settings, not part of the per-load targeting request.

  /// Trigger an ad load for an already-created banner PlatformView.
  void loadBanner(CDAdsRequest request);

  /// Release resources for a banner. Call when the host widget is disposed.
  void destroyBanner(String adUnitId);

  // ── Interstitial ───────────────────────────────────────────────────────────

  /// Pre-fetch an interstitial ad. Listen for [onInterstitialLoaded] before calling show.
  void loadInterstitial(CDAdsRequest request);

  /// Returns true if a loaded interstitial is ready to display.
  bool isInterstitialReady(String adUnitId);

  /// Present the loaded interstitial. No-op if not ready.
  void showInterstitial(String adUnitId);

  /// Release an interstitial and its cached creative.
  void destroyInterstitial(String adUnitId);

  // ── Rewarded Video ─────────────────────────────────────────────────────────

  /// Pre-fetch a rewarded video ad.
  void loadRewardedVideo(CDAdsRequest request);

  /// Returns true if a rewarded video is loaded and ready to play.
  bool isRewardedVideoReady(String adUnitId);

  /// Present the rewarded video. No-op if not ready.
  void showRewardedVideo(String adUnitId);

  // ── Native Ads ─────────────────────────────────────────────────────────────

  /// Request a native ad. Assets are returned via [onNativeAdLoaded].
  void loadNativeAd(CDAdsRequest request);

  /// Fire an impression beacon for a native ad.
  /// Call this once when the ad becomes visible (e.g. using a VisibilityDetector).
  void trackNativeAdImpression(String adId);

  /// Fire a click beacon for a native ad.
  /// Call this when the user taps the ad creative.
  void trackNativeAdClick(String adId);

  /// Release a native ad's cached data.
  void destroyNativeAd(String adUnitId);

  // ── In-app debug log ───────────────────────────────────────────────────────
  // Modern equivalent of the legacy Tempo "*##*" debug console. File/buffer
  // capture is independent of CDAdsConfig.logLevel — see CDAdsDebugTrigger in
  // the Dart layer for the magic-code matcher used to reveal it.

  /// Whether SDK log lines are being written to the in-app debug log. Defaults to false.
  bool isDebugFileLoggingEnabled();

  /// Toggles in-app debug log capture. Independent of CDAdsConfig.logLevel.
  void setDebugFileLoggingEnabled(bool enabled);

  /// The SDK's captured debug log text (oldest first).
  String debugLogs();

  /// Clears the in-app debug log buffer and its backing file.
  void clearDebugLogs();

  /// Presents the native in-app debug log viewer (CDADebugLogViewController on
  /// iOS, CDADebugLogActivity on Android) on top of the current screen.
  void showDebugLogViewer();
}

// =============================================================================
// FLUTTER API  (Native → Dart)
// Flutter (the plugin) implements this interface.
// Native platforms call these methods to fire ad events back to Dart.
// =============================================================================

@FlutterApi()
abstract class CDAdsFlutterApi {
  // ── Banner Events ──────────────────────────────────────────────────────────

  void onBannerLoaded(String adUnitId);
  void onBannerFailedToLoad(String adUnitId, CDAdsError error);
  void onBannerClicked(String adUnitId);

  /// User is leaving the app via an ad click (e.g. opening the App Store).
  void onBannerWillLeaveApp(String adUnitId);

  /// Fired when the banner's viewability tracker confirms an impression.
  /// Android only — iOS has no equivalent signal at the banner-delegate level.
  void onBannerImpression(String adUnitId);

  /// An MRAID ad expanded to fullscreen.
  void onBannerExpanded(String adUnitId);

  /// A previously expanded MRAID ad collapsed back to its default state.
  void onBannerCollapsed(String adUnitId);

  /// The user tapped the banner's close button (only fires when showCloseButton is true).
  void onBannerDidClose(String adUnitId);

  // ── Interstitial Events ────────────────────────────────────────────────────

  void onInterstitialLoaded(String adUnitId);
  void onInterstitialFailedToLoad(String adUnitId, CDAdsError error);
  void onInterstitialWillAppear(String adUnitId);
  void onInterstitialDidAppear(String adUnitId);
  void onInterstitialWillDisappear(String adUnitId);
  void onInterstitialDidDisappear(String adUnitId);

  /// A previously loaded interstitial has expired and can no longer be shown.
  void onInterstitialExpired(String adUnitId);
  void onInterstitialClicked(String adUnitId);
  void onInterstitialWillLeaveApp(String adUnitId);

  // ── Rewarded Video Events ──────────────────────────────────────────────────

  void onRewardedVideoLoaded(String adUnitId);
  void onRewardedVideoFailedToLoad(String adUnitId, CDAdsError error);

  /// The video started playing but failed before completion (no reward granted).
  void onRewardedVideoFailedToPlay(String adUnitId, CDAdsError error);

  void onRewardedVideoExpired(String adUnitId);
  void onRewardedVideoWillAppear(String adUnitId);
  void onRewardedVideoDidAppear(String adUnitId);
  void onRewardedVideoWillDisappear(String adUnitId);
  void onRewardedVideoDidDisappear(String adUnitId);
  void onRewardedVideoClicked(String adUnitId);
  void onRewardedVideoWillLeaveApp(String adUnitId);

  /// User completed the video — grant the reward.
  void onRewardedVideoEarnedReward(String adUnitId, CDAdsReward reward);

  // ── Native Ad Events ───────────────────────────────────────────────────────

  /// All native ad assets are delivered here. Render them using your own widget.
  void onNativeAdLoaded(CDAdsNativeAdData data);
  void onNativeAdFailedToLoad(String adUnitId, CDAdsError error);

  /// A previously loaded native ad has expired and should be removed from view.
  void onNativeAdExpired(String adUnitId);

  // ── SDK-Level Events ───────────────────────────────────────────────────────

  /// Fired by the SDK's background location manager when a new location batch is ready.
  void onLocationUpdated(CDAdsGeoInfo location);

  /// "reachable" | "notReachable" | "unknown"
  void onNetworkReachabilityChanged(String status);
}
