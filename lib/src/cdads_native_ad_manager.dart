import 'generated/cdads_api.g.dart';
import 'cdads.dart';
import 'cdads_event_handler.dart';
import 'cdads_events.dart';

/// Requests a native ad and delivers the asset data to Flutter for rendering.
///
/// ```dart
/// final manager = CDAdsNativeAdManager(adUnitId: 'YOUR_PLACEMENT_ID');
/// manager.onLoaded = (data) {
///   setState(() => _nativeAd = data);
/// };
/// manager.load();
/// ```
class CDAdsNativeAdManager {
  CDAdsNativeAdManager({required this.adUnitId});

  final String adUnitId;

  void Function(CDAdsNativeAdData)?  onLoaded;
  void Function(CDAdsError)?         onFailedToLoad;
  void Function()?                   onExpired;

  void load([CDAdsRequest? request]) {
    CDAdsEventHandler.addListener(adUnitId, _handleEvent);
    final req = request ?? CDAdsRequest(adUnitId: adUnitId);
    CDAds.hostApi.loadNativeAd(req);
  }

  /// Call when the native ad widget becomes visible on screen.
  Future<void> trackImpression(String adId) =>
      CDAds.hostApi.trackNativeAdImpression(adId);

  /// Call when the user taps the native ad.
  Future<void> trackClick(String adId) =>
      CDAds.hostApi.trackNativeAdClick(adId);

  void dispose() {
    CDAdsEventHandler.removeListener(adUnitId);
    CDAds.hostApi.destroyNativeAd(adUnitId);
  }

  void _handleEvent(CDAdsEvent event) {
    switch (event) {
      case CDAdsNativeAdLoaded(:final data):
        onLoaded?.call(data);
      case CDAdsNativeAdFailed(:final error):
        onFailedToLoad?.call(error);
      case CDAdsNativeAdExpired():
        onExpired?.call();
      default:
        break;
    }
  }
}
