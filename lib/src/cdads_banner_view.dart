import 'package:flutter/material.dart';

import 'cdads_banner_widget.dart';
import 'cdads_landing_page_behaviour.dart';
import 'generated/cdads_api.g.dart';

/// Drop-in banner slot: composes a placeholder with [CDAdsBannerWidget],
/// handling close-button sizing/alignment (see
/// [CDAdsBannerWidget.closeButtonExtraSize]) and foreground/background
/// teardown internally, so host apps don't need their own `Stack`/`SizedBox`
/// math or `WidgetsBindingObserver` boilerplate.
///
/// ```dart
/// CDAdsBannerView(
///   width: 320,
///   height: 50,
///   placeholder: Container(color: Colors.grey),
///   adUnitId: 'YOUR_PLACEMENT_ID',
///   request: myRequest,
///   showCloseButton: true,
/// )
/// ```
///
/// [placeholder] is sized to exactly [width] x [height] and shown until
/// [request] is supplied and an ad has loaded over it (it keeps showing
/// through transparent/unloaded areas, and remains the fallback on failure).
/// Tears the [CDAdsBannerWidget] PlatformView down while the app is
/// backgrounded — rather than just hiding it — so no ad request fires
/// off-screen; it's recreated on resume.
class CDAdsBannerView extends StatefulWidget {
  const CDAdsBannerView({
    required this.width,
    required this.height,
    required this.placeholder,
    this.adUnitId,
    this.size = CDAdsSize.custom,
    this.request,
    this.showCloseButton = false,
    this.isAutoRefreshEnabled = true,
    this.refreshInterval = 30,
    this.landingPageBehaviour = CDAdsLandingPageBehaviour.inAppBrowser,
    this.onLoaded,
    this.onFailedToLoad,
    this.onImpression,
    this.onClicked,
    this.onWillLeaveApp,
    this.onExpanded,
    this.onCollapsed,
    this.onDidClose,
    super.key,
  });

  /// Bare ad content size — does not include the close button's extra
  /// gutter, which this widget reserves internally.
  final double width;
  final double height;

  /// Shown at [width] x [height] until the ad loads on top of it, and
  /// remains visible as the fallback if it fails to load. Also shown
  /// whenever [adUnitId]/[request] is null or the app is backgrounded.
  final Widget placeholder;

  /// No ad is requested while this is null.
  final String? adUnitId;

  final CDAdsSize size;
  final CDAdsRequest? request;

  /// When `true` a close (×) button is displayed straddling the ad's
  /// top-left corner, once an ad has actually loaded.
  final bool showCloseButton;

  /// When `true` the banner automatically reloads on [refreshInterval]. Default `true`.
  final bool isAutoRefreshEnabled;

  /// Seconds between automatic ad refreshes. Default 30s.
  final double refreshInterval;

  /// Controls how tapped ad landing pages are opened. Default [CDAdsLandingPageBehaviour.inAppBrowser].
  final CDAdsLandingPageBehaviour landingPageBehaviour;

  final void Function()? onLoaded;
  final void Function(CDAdsError)? onFailedToLoad;
  final void Function()? onImpression;
  final void Function()? onClicked;

  /// User is leaving the app via an ad click (e.g. opening the App Store).
  final void Function()? onWillLeaveApp;

  /// An MRAID ad expanded to fullscreen.
  final void Function()? onExpanded;

  /// A previously expanded MRAID ad collapsed back to its default state.
  final void Function()? onCollapsed;

  /// The user tapped the close button (only fires when [showCloseButton] is true).
  final void Function()? onDidClose;

  @override
  State<CDAdsBannerView> createState() => _CDAdsBannerViewState();
}

class _CDAdsBannerViewState extends State<CDAdsBannerView>
    with WidgetsBindingObserver {
  bool _isForeground = true;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    final isForeground = state == AppLifecycleState.resumed;
    if (isForeground != _isForeground) {
      setState(() => _isForeground = isForeground);
    }
  }

  @override
  Widget build(BuildContext context) {
    final adUnitId = widget.adUnitId;
    final request = widget.request;
    final extra =
        CDAdsBannerWidget.closeButtonExtraSize(widget.showCloseButton);

    return SizedBox(
      width: widget.width + extra.width,
      height: widget.height + extra.height,
      child: Stack(
        alignment: Alignment.bottomCenter,
        children: [
          Container(
            padding: EdgeInsets.only(
                left: extra.width / 2,
                right: extra.width / 2,
                top: extra.height),
            width: widget.width,
            height: widget.height,
            child: widget.placeholder,
          ),
          if (adUnitId != null && request != null && _isForeground)
            CDAdsBannerWidget(
              key: ValueKey(adUnitId),
              adUnitId: adUnitId,
              request: request,
              size: widget.size,
              customWidth:
                  widget.size == CDAdsSize.custom ? widget.width : null,
              customHeight:
                  widget.size == CDAdsSize.custom ? widget.height : null,
              showCloseButton: widget.showCloseButton,
              isAutoRefreshEnabled: widget.isAutoRefreshEnabled,
              refreshInterval: widget.refreshInterval,
              landingPageBehaviour: widget.landingPageBehaviour,
              onLoaded: widget.onLoaded,
              onFailedToLoad: widget.onFailedToLoad,
              onImpression: widget.onImpression,
              onClicked: widget.onClicked,
              onWillLeaveApp: widget.onWillLeaveApp,
              onExpanded: widget.onExpanded,
              onCollapsed: widget.onCollapsed,
              onDidClose: widget.onDidClose,
            ),
        ],
      ),
    );
  }
}
