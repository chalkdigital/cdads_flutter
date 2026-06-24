import 'generated/cdads_api.g.dart';

/// Sealed event hierarchy dispatched to ad listeners.
/// Use exhaustive switch patterns in [CDAdsInterstitialAd],
/// [CDAdsRewardedVideoAd], [CDAdsNativeAdManager] and [CDAdsBannerWidget].
sealed class CDAdsEvent {
  const CDAdsEvent();
}

// ── Banner ────────────────────────────────────────────────────────────────────
final class CDAdsbannerLoaded      extends CDAdsEvent { const CDAdsbannerLoaded(); }
final class CDAdsbannerFailed      extends CDAdsEvent { final CDAdsError error; const CDAdsbannerFailed(this.error); }
final class CDAdsbannerImpression  extends CDAdsEvent { const CDAdsbannerImpression(); }
final class CDAdsbannerClicked     extends CDAdsEvent { const CDAdsbannerClicked(); }
final class CDAdsbannerWillLeaveApp extends CDAdsEvent { const CDAdsbannerWillLeaveApp(); }
final class CDAdsbannerExpanded    extends CDAdsEvent { const CDAdsbannerExpanded(); }
final class CDAdsbannerCollapsed   extends CDAdsEvent { const CDAdsbannerCollapsed(); }
final class CDAdsbannerDidClose    extends CDAdsEvent { const CDAdsbannerDidClose(); }

// ── Interstitial ──────────────────────────────────────────────────────────────
final class CDAdsInterstitialLoaded        extends CDAdsEvent { const CDAdsInterstitialLoaded(); }
final class CDAdsInterstitialFailed        extends CDAdsEvent { final CDAdsError error; const CDAdsInterstitialFailed(this.error); }
final class CDAdsInterstitialWillAppear    extends CDAdsEvent { const CDAdsInterstitialWillAppear(); }
final class CDAdsInterstitialDidAppear     extends CDAdsEvent { const CDAdsInterstitialDidAppear(); }
final class CDAdsInterstitialWillDisappear extends CDAdsEvent { const CDAdsInterstitialWillDisappear(); }
final class CDAdsInterstitialDidDisappear  extends CDAdsEvent { const CDAdsInterstitialDidDisappear(); }
final class CDAdsInterstitialExpired       extends CDAdsEvent { const CDAdsInterstitialExpired(); }
final class CDAdsInterstitialClicked       extends CDAdsEvent { const CDAdsInterstitialClicked(); }
final class CDAdsInterstitialWillLeaveApp  extends CDAdsEvent { const CDAdsInterstitialWillLeaveApp(); }

// ── Rewarded Video ────────────────────────────────────────────────────────────
final class CDAdsRewardedLoaded        extends CDAdsEvent { const CDAdsRewardedLoaded(); }
final class CDAdsRewardedFailed        extends CDAdsEvent { final CDAdsError error; const CDAdsRewardedFailed(this.error); }
final class CDAdsRewardedFailedToPlay  extends CDAdsEvent { final CDAdsError error; const CDAdsRewardedFailedToPlay(this.error); }
final class CDAdsRewardedExpired       extends CDAdsEvent { const CDAdsRewardedExpired(); }
final class CDAdsRewardedWillAppear    extends CDAdsEvent { const CDAdsRewardedWillAppear(); }
final class CDAdsRewardedDidAppear     extends CDAdsEvent { const CDAdsRewardedDidAppear(); }
final class CDAdsRewardedWillDisappear extends CDAdsEvent { const CDAdsRewardedWillDisappear(); }
final class CDAdsRewardedDidDisappear  extends CDAdsEvent { const CDAdsRewardedDidDisappear(); }
final class CDAdsRewardedClicked       extends CDAdsEvent { const CDAdsRewardedClicked(); }
final class CDAdsRewardedWillLeaveApp  extends CDAdsEvent { const CDAdsRewardedWillLeaveApp(); }
final class CDAdsRewardedEarned        extends CDAdsEvent { final CDAdsReward reward; const CDAdsRewardedEarned(this.reward); }

// ── Native Ad ─────────────────────────────────────────────────────────────────
final class CDAdsNativeAdLoaded  extends CDAdsEvent { final CDAdsNativeAdData data; const CDAdsNativeAdLoaded(this.data); }
final class CDAdsNativeAdFailed  extends CDAdsEvent { final CDAdsError error; const CDAdsNativeAdFailed(this.error); }
final class CDAdsNativeAdExpired extends CDAdsEvent { const CDAdsNativeAdExpired(); }
