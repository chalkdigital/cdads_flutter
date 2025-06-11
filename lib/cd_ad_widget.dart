import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'cd_ad_controller.dart';
import 'cd_ad_size.dart';

class CDAdWidget extends StatefulWidget {
  final CDAdSize adSize;
  final CDAdController controller;

  final void Function()? onAdLoaded;
  final void Function()? onAdFailed;
  final void Function()? onAdTapped;
  final void Function()? onInterstitialClosed;

  const CDAdWidget(
      {required this.adSize,
      required this.controller,
      super.key,
      this.onAdLoaded,
      this.onAdFailed,
      this.onAdTapped,
      this.onInterstitialClosed});

  @override
  State<CDAdWidget> createState() => _CDAdWidgetState();
}

class _CDAdWidgetState extends State<CDAdWidget> {
  late MethodChannel _channel;

  @override
  void initState() {
    super.initState();
    _channel = const MethodChannel('cdads_ad_view_0'); // same id used in Swift
    _channel.setMethodCallHandler(_handleNativeCallbacks);
  }

  Future<void> _handleNativeCallbacks(MethodCall call) async {
    switch (call.method) {
      case 'onAdLoaded':
        widget.onAdLoaded?.call();
        break;
      case 'onAdFailed':
        widget.onAdFailed?.call();
        break;
      case 'onAdTapped':
        widget.onAdTapped?.call();
        break;
      case 'onInterstitialClosed':
        widget.onInterstitialClosed?.call();
        break;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Container(
        width: widget.adSize.width + 32,
        height: widget.adSize.height + 16,
        alignment: Alignment.bottomCenter,
        color: Colors.transparent,
        child: (Platform.isIOS)
            ? UiKitView(
                viewType: 'cdads_ad_view',
                layoutDirection: TextDirection.ltr,
                creationParams: {
                  'adSize': widget.adSize.toMap(),
                },
                creationParamsCodec: const StandardMessageCodec(),
                onPlatformViewCreated: (int id) {
                  final channel = MethodChannel('cdads_ad_view_$id');
                  widget.controller.bindChannel(channel);
                },
              )
            : AndroidView(
                viewType: 'cdads_ad_view',
                creationParams: {
                  'adSize': widget.adSize.toMap(),
                },
                creationParamsCodec: const StandardMessageCodec(),
                onPlatformViewCreated: (int id) {
                  final channel = MethodChannel('cdads_ad_view_$id');
                  widget.controller.bindChannel(channel);
                },
              ));
  }
}
