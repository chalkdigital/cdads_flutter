import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'generated/cdads_api.g.dart';
import 'cdads.dart';
import 'cdads_event_handler.dart';
import 'cdads_events.dart';

/// Flutter widget that renders a banner or MREC ad via a native PlatformView.
///
/// ```dart
/// CDAdsBannerWidget(
///   adUnitId: 'YOUR_PLACEMENT_ID',
///   size: CDAdsSize.banner300x250,
///   showCloseButton: true,
///   onLoaded: () => print('banner loaded'),
/// )
/// ```
///
/// For a size not covered by the presets, pass `CDAdsSize.custom` with
/// explicit [customWidth]/[customHeight].
class CDAdsBannerWidget extends StatefulWidget {
  const CDAdsBannerWidget({
    required this.adUnitId,
    required this.size,
    this.customWidth,
    this.customHeight,
    this.showCloseButton = false,
    this.isAutoRefreshEnabled = true,
    this.refreshInterval = 30,
    this.request,
    this.onLoaded,
    this.onFailedToLoad,
    this.onImpression,
    this.onClicked,
    this.onWillLeaveApp,
    this.onExpanded,
    this.onCollapsed,
    this.onDidClose,
    super.key,
  })  : assert(
          size != CDAdsSize.custom || (customWidth != null && customHeight != null),
          'customWidth and customHeight are required when size is CDAdsSize.custom',
        );

  final String adUnitId;
  final CDAdsSize size;

  /// Required when [size] is [CDAdsSize.custom].
  final double? customWidth;
  final double? customHeight;

  /// When `true` a close (×) button is displayed in the top-right corner of the banner.
  final bool showCloseButton;

  /// When `true` the banner automatically reloads on [refreshInterval]. Default `true`.
  final bool isAutoRefreshEnabled;

  /// Seconds between automatic ad refreshes. Default 30s.
  final double refreshInterval;

  final CDAdsRequest? request;
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
  State<CDAdsBannerWidget> createState() => _CDAdsBannerWidgetState();

  /// Extra width/height this widget needs beyond its requested ad size when
  /// [showCloseButton] is true — zero otherwise. Host apps must reserve this
  /// much additional space around the ad (e.g. a slightly larger container)
  /// so the close button, which straddles the ad content's top-left corner
  /// and overhangs outside the ad's own bounds, isn't clipped.
  ///
  /// The two platforms need different amounts because their native SDKs
  /// reserve the gutter differently:
  /// - iOS (`CDABannerView.swift`): a 50pt button centered exactly on the ad's
  ///   corner point — 25pt overhangs top, 25pt overhangs left. The ad itself
  ///   stays flush against the container's right/bottom edges, so only
  ///   top-left space is needed.
  /// - Android (`CDABannerView.kt`): `CORNER_INSET_DP = 12dp`, reserved
  ///   symmetrically on the start *and* end edges (so the ad content stays
  ///   horizontally centered rather than shifted) but only on the top edge
  ///   vertically (the button never straddles the bottom) — i.e. 24dp extra
  ///   width, 12dp extra height.
  ///
  /// Pair with [closeButtonAlignment] when positioning this widget over a
  /// same-sized placeholder/background, so the ad content (not the extra
  /// gutter) lines up with it on both platforms.
  static Size closeButtonExtraSize(bool showCloseButton) {
    if (!showCloseButton) return Size.zero;
    return Platform.isIOS ? const Size(50, 25) : const Size(24, 12);
  }
}

class _CDAdsBannerWidgetState extends State<CDAdsBannerWidget> {
  @override
  void initState() {
    super.initState();
    CDAdsEventHandler.addListener(widget.adUnitId, _handleEvent);
  }

  @override
  void dispose() {
    CDAdsEventHandler.removeListener(widget.adUnitId);
    CDAds.hostApi.destroyBanner(widget.adUnitId);
    super.dispose();
  }

  void _handleEvent(CDAdsEvent event) {
    switch (event) {
      case CDAdsbannerLoaded():
        widget.onLoaded?.call();
      case CDAdsbannerFailed(:final error):
        widget.onFailedToLoad?.call(error);
      case CDAdsbannerImpression():
        widget.onImpression?.call();
      case CDAdsbannerClicked():
        widget.onClicked?.call();
      case CDAdsbannerWillLeaveApp():
        widget.onWillLeaveApp?.call();
      case CDAdsbannerExpanded():
        widget.onExpanded?.call();
      case CDAdsbannerCollapsed():
        widget.onCollapsed?.call();
      case CDAdsbannerDidClose():
        widget.onDidClose?.call();
      default:
        break;
    }
  }

  void _onPlatformViewCreated(int id) {
    // PlatformView is ready — trigger the actual ad load via Pigeon
    final req = widget.request ?? CDAdsRequest(adUnitId: widget.adUnitId);
    CDAds.hostApi.loadBanner(req);
  }

  @override
  Widget build(BuildContext context) {
    final size = widget.size.cgSize(customWidth: widget.customWidth, customHeight: widget.customHeight);
    final creationParams = {
      'adUnitId':             widget.adUnitId,
      'sizePreset':           widget.size.name,
      'width':                size.width,
      'height':               size.height,
      'showCloseButton':      widget.showCloseButton,
      'isAutoRefreshEnabled': widget.isAutoRefreshEnabled,
      'refreshInterval':      widget.refreshInterval,
    };

    // The close button straddles the ad content's top-left corner (native SDK behavior
    // on both platforms). Flutter's platform-view embedding clips the native view to
    // exactly this widget's allocated rect, so without this extra space the overhanging
    // part of the button gets clipped away and is invisible (it isn't on plain
    // UIKit/Android views, which don't clip by default). See [closeButtonExtraSize] for
    // why the two platforms need different amounts.
    final extra = CDAdsBannerWidget.closeButtonExtraSize(widget.showCloseButton);

    return SizedBox(
      width:  size.width + extra.width,
      height: size.height + extra.height,
      child: Platform.isIOS
          ? UiKitView(
              viewType: 'cdads_flutter/banner',
              creationParams: creationParams,
              creationParamsCodec: const StandardMessageCodec(),
              onPlatformViewCreated: _onPlatformViewCreated,
            )
          : AndroidView(
              viewType: 'cdads_flutter/banner',
              creationParams: creationParams,
              creationParamsCodec: const StandardMessageCodec(),
              onPlatformViewCreated: _onPlatformViewCreated,
            ),
    );
  }
}

// Helper extension used in widget build
extension on CDAdsSize {
  ({double width, double height}) cgSize({double? customWidth, double? customHeight}) => switch (this) {
        CDAdsSize.banner320x50    => (width: 320, height: 50),
        CDAdsSize.banner300x50    => (width: 300, height: 50),
        CDAdsSize.banner300x250   => (width: 300, height: 250),
        CDAdsSize.banner320x100   => (width: 320, height: 100),
        CDAdsSize.banner728x90    => (width: 728, height: 90),
        CDAdsSize.banner728x250   => (width: 728, height: 250),
        CDAdsSize.banner300x600   => (width: 300, height: 600),
        CDAdsSize.banner970x250   => (width: 970, height: 250),
        CDAdsSize.banner480x320   => (width: 480, height: 320),
        CDAdsSize.banner320x480   => (width: 320, height: 480),
        CDAdsSize.banner768x1024  => (width: 768, height: 1024),
        CDAdsSize.banner1086x1086 => (width: 1086, height: 1086),
        CDAdsSize.custom          => (width: customWidth ?? 0, height: customHeight ?? 0),
      };
}
