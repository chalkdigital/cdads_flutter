import 'generated/cdads_api.g.dart';
import 'cdads_events.dart';

/// Receives all native → Dart events via Pigeon CDAdsFlutterApi.
/// Distributes events to the appropriate StreamController per ad unit ID.
class CDAdsEventHandler implements CDAdsFlutterApi {
  CDAdsEventHandler._();

  static final _instance = CDAdsEventHandler._();

  static void setUp() {
    CDAdsFlutterApi.setUp(_instance);
  }

  // ── Subscriber registries ──────────────────────────────────────────────────

  // Each ad class registers a listener keyed by adUnitId.
  static final Map<String, void Function(CDAdsEvent)> _listeners = {};

  static void addListener(String adUnitId, void Function(CDAdsEvent) listener) {
    _listeners[adUnitId] = listener;
  }

  static void removeListener(String adUnitId) {
    _listeners.remove(adUnitId);
  }

  void _dispatch(String adUnitId, CDAdsEvent event) {
    _listeners[adUnitId]?.call(event);
  }

  // ── CDAdsFlutterApi implementation ─────────────────────────────────────────

  @override
  void onBannerLoaded(String adUnitId) =>
      _dispatch(adUnitId, const CDAdsbannerLoaded());

  @override
  void onBannerFailedToLoad(String adUnitId, CDAdsError error) =>
      _dispatch(adUnitId, CDAdsbannerFailed(error));

  @override
  void onBannerClicked(String adUnitId) =>
      _dispatch(adUnitId, const CDAdsbannerClicked());

  @override
  void onBannerWillLeaveApp(String adUnitId) =>
      _dispatch(adUnitId, const CDAdsbannerWillLeaveApp());

  @override
  void onBannerImpression(String adUnitId) =>
      _dispatch(adUnitId, const CDAdsbannerImpression());

  @override
  void onBannerExpanded(String adUnitId) =>
      _dispatch(adUnitId, const CDAdsbannerExpanded());

  @override
  void onBannerCollapsed(String adUnitId) =>
      _dispatch(adUnitId, const CDAdsbannerCollapsed());

  @override
  void onBannerDidClose(String adUnitId) =>
      _dispatch(adUnitId, const CDAdsbannerDidClose());

  @override
  void onInterstitialLoaded(String adUnitId) =>
      _dispatch(adUnitId, const CDAdsInterstitialLoaded());

  @override
  void onInterstitialFailedToLoad(String adUnitId, CDAdsError error) =>
      _dispatch(adUnitId, CDAdsInterstitialFailed(error));

  @override
  void onInterstitialWillAppear(String adUnitId) =>
      _dispatch(adUnitId, const CDAdsInterstitialWillAppear());

  @override
  void onInterstitialDidAppear(String adUnitId) =>
      _dispatch(adUnitId, const CDAdsInterstitialDidAppear());

  @override
  void onInterstitialWillDisappear(String adUnitId) =>
      _dispatch(adUnitId, const CDAdsInterstitialWillDisappear());

  @override
  void onInterstitialDidDisappear(String adUnitId) =>
      _dispatch(adUnitId, const CDAdsInterstitialDidDisappear());

  @override
  void onInterstitialExpired(String adUnitId) =>
      _dispatch(adUnitId, const CDAdsInterstitialExpired());

  @override
  void onInterstitialClicked(String adUnitId) =>
      _dispatch(adUnitId, const CDAdsInterstitialClicked());

  @override
  void onInterstitialWillLeaveApp(String adUnitId) =>
      _dispatch(adUnitId, const CDAdsInterstitialWillLeaveApp());

  @override
  void onRewardedVideoLoaded(String adUnitId) =>
      _dispatch(adUnitId, const CDAdsRewardedLoaded());

  @override
  void onRewardedVideoFailedToLoad(String adUnitId, CDAdsError error) =>
      _dispatch(adUnitId, CDAdsRewardedFailed(error));

  @override
  void onRewardedVideoFailedToPlay(String adUnitId, CDAdsError error) =>
      _dispatch(adUnitId, CDAdsRewardedFailedToPlay(error));

  @override
  void onRewardedVideoExpired(String adUnitId) =>
      _dispatch(adUnitId, const CDAdsRewardedExpired());

  @override
  void onRewardedVideoWillAppear(String adUnitId) =>
      _dispatch(adUnitId, const CDAdsRewardedWillAppear());

  @override
  void onRewardedVideoDidAppear(String adUnitId) =>
      _dispatch(adUnitId, const CDAdsRewardedDidAppear());

  @override
  void onRewardedVideoWillDisappear(String adUnitId) =>
      _dispatch(adUnitId, const CDAdsRewardedWillDisappear());

  @override
  void onRewardedVideoDidDisappear(String adUnitId) =>
      _dispatch(adUnitId, const CDAdsRewardedDidDisappear());

  @override
  void onRewardedVideoClicked(String adUnitId) =>
      _dispatch(adUnitId, const CDAdsRewardedClicked());

  @override
  void onRewardedVideoWillLeaveApp(String adUnitId) =>
      _dispatch(adUnitId, const CDAdsRewardedWillLeaveApp());

  @override
  void onRewardedVideoEarnedReward(String adUnitId, CDAdsReward reward) =>
      _dispatch(adUnitId, CDAdsRewardedEarned(reward));

  @override
  void onNativeAdLoaded(CDAdsNativeAdData data) =>
      _dispatch(data.adUnitId, CDAdsNativeAdLoaded(data));

  @override
  void onNativeAdFailedToLoad(String adUnitId, CDAdsError error) =>
      _dispatch(adUnitId, CDAdsNativeAdFailed(error));

  @override
  void onNativeAdExpired(String adUnitId) =>
      _dispatch(adUnitId, const CDAdsNativeAdExpired());

  @override
  void onLocationUpdated(CDAdsGeoInfo location) {
    // Broadcast to all location listeners (not keyed by ad unit)
    onLocationUpdate?.call(location);
  }

  @override
  void onNetworkReachabilityChanged(String status) {
    onReachabilityChange?.call(status);
  }

  // Global callbacks (not per-ad-unit)
  static void Function(CDAdsGeoInfo)? onLocationUpdate;
  static void Function(String)? onReachabilityChange;
}

