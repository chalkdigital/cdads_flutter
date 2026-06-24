import 'generated/cdads_api.g.dart';
import 'cdads.dart';
import 'cdads_event_handler.dart';
import 'cdads_events.dart';

/// Loads and shows a fullscreen rewarded video ad.
///
/// ```dart
/// final ad = CDAdsRewardedVideoAd(adUnitId: 'YOUR_PLACEMENT_ID');
/// ad.onLoaded = () => ad.show();
/// ad.onEarnedReward = (reward) {
///   grantCoins(reward.amount);
/// };
/// ad.load();
/// ```
class CDAdsRewardedVideoAd {
  CDAdsRewardedVideoAd({required this.adUnitId});

  final String adUnitId;

  void Function()?               onLoaded;
  void Function(CDAdsError)?     onFailedToLoad;
  void Function(CDAdsError)?     onFailedToPlay;
  void Function()?               onExpired;
  void Function()?               onWillAppear;
  void Function()?               onDidAppear;
  void Function()?               onWillDismiss;
  void Function()?               onDismissed;
  void Function()?               onClicked;
  void Function()?               onWillLeaveApp;
  void Function(CDAdsReward)?    onEarnedReward;

  bool _isReady = false;
  bool get isReady => _isReady;

  void load([CDAdsRequest? request]) {
    _isReady = false;
    CDAdsEventHandler.addListener(adUnitId, _handleEvent);
    final req = request ?? CDAdsRequest(adUnitId: adUnitId);
    CDAds.hostApi.loadRewardedVideo(req);
  }

  Future<void> show() async {
    if (!_isReady) return;
    await CDAds.hostApi.showRewardedVideo(adUnitId);
  }

  void dispose() {
    CDAdsEventHandler.removeListener(adUnitId);
  }

  void _handleEvent(CDAdsEvent event) {
    switch (event) {
      case CDAdsRewardedLoaded():
        _isReady = true;
        onLoaded?.call();
      case CDAdsRewardedFailed(:final error):
        onFailedToLoad?.call(error);
      case CDAdsRewardedFailedToPlay(:final error):
        onFailedToPlay?.call(error);
      case CDAdsRewardedExpired():
        _isReady = false;
        onExpired?.call();
      case CDAdsRewardedWillAppear():
        onWillAppear?.call();
      case CDAdsRewardedDidAppear():
        onDidAppear?.call();
      case CDAdsRewardedWillDisappear():
        onWillDismiss?.call();
      case CDAdsRewardedDidDisappear():
        _isReady = false;
        onDismissed?.call();
      case CDAdsRewardedClicked():
        onClicked?.call();
      case CDAdsRewardedWillLeaveApp():
        onWillLeaveApp?.call();
      case CDAdsRewardedEarned(:final reward):
        onEarnedReward?.call(reward);
      default:
        break;
    }
  }
}
