import 'generated/cdads_api.g.dart';
import 'cdads.dart';
import 'cdads_event_handler.dart';
import 'cdads_events.dart';

/// Loads and shows a fullscreen interstitial ad.
///
/// ```dart
/// final ad = CDAdsInterstitialAd(adUnitId: 'YOUR_PLACEMENT_ID');
/// ad.onLoaded    = () => ad.show();
/// ad.onDismissed = () => print('closed');
/// ad.load();
/// ```
class CDAdsInterstitialAd {
  CDAdsInterstitialAd({required this.adUnitId});

  final String adUnitId;

  void Function()?            onLoaded;
  void Function(CDAdsError)?  onFailedToLoad;
  void Function()?            onWillAppear;
  void Function()?            onDidAppear;
  void Function()?            onWillDismiss;
  void Function()?            onDismissed;
  void Function()?            onExpired;
  void Function()?            onClicked;
  void Function()?            onWillLeaveApp;

  bool _isReady = false;
  bool get isReady => _isReady;

  void load([CDAdsRequest? request]) {
    _isReady = false;
    CDAdsEventHandler.addListener(adUnitId, _handleEvent);
    final req = request ?? CDAdsRequest(adUnitId: adUnitId);
    CDAds.hostApi.loadInterstitial(req);
  }

  Future<void> show() async {
    if (!_isReady) return;
    await CDAds.hostApi.showInterstitial(adUnitId);
  }

  void dispose() {
    CDAdsEventHandler.removeListener(adUnitId);
    CDAds.hostApi.destroyInterstitial(adUnitId);
  }

  void _handleEvent(CDAdsEvent event) {
    switch (event) {
      case CDAdsInterstitialLoaded():
        _isReady = true;
        onLoaded?.call();
      case CDAdsInterstitialFailed(:final error):
        onFailedToLoad?.call(error);
      case CDAdsInterstitialWillAppear():
        onWillAppear?.call();
      case CDAdsInterstitialDidAppear():
        onDidAppear?.call();
      case CDAdsInterstitialWillDisappear():
        onWillDismiss?.call();
      case CDAdsInterstitialDidDisappear():
        _isReady = false;
        onDismissed?.call();
      case CDAdsInterstitialExpired():
        _isReady = false;
        onExpired?.call();
      case CDAdsInterstitialClicked():
        onClicked?.call();
      case CDAdsInterstitialWillLeaveApp():
        onWillLeaveApp?.call();
      default:
        break;
    }
  }
}
