import 'package:cdads_flutter/cd_ad_controller.dart';
import 'package:cdads_flutter/cd_ad_defines.dart';
import 'package:cdads_flutter/cd_ad_size.dart';
import 'package:cdads_flutter/cd_ad_widget.dart';
import 'package:cdads_flutter/cd_geo_info.dart';
import 'package:cdads_flutter/cd_initialization_params.dart';
import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:cdads_flutter/cdads_flutter.dart';

void main() async {
  runApp(const MyApp());
}

Future<void> initialiseCdadsSdk() async {
  final params = CDInitializationParams(
      partnerKey: "7d95de69cfc7cc03c3a05b4fde9662b8",
      applicationIABCategory: "IAB15-10",
      distanceFilter: 5.0,
      locationUpdateInterval: 30.0,
      adLocationExpiryInterval: 300.0,
      logLevel: CDLogLevel.all,
      provider: CDADProvider.chalk,
      environment: CDEnvironment.production,
      enableTracking: true);
  final cdadsInstance = CdadsFlutter();
  await cdadsInstance.initialize(params);
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  final _cdadsPlugin = CdadsFlutter();
  final _controller = CDAdController()
    ..placementId = "0"
    ..partnerId = "chalkboard"
    ..isLocationAutoUpdateEnabled = false
    ..isAdAutoRefreshEnabled = true
    ..isTesting = false
    ..isCloseable = true
    ..useInappBrowser = true
    ..geoInfo = CDGeoInfo(
      lat: 34.640305,
      lon: -111.10628,
      countryCode: "USA",
      region: "CA",
      city: "Valley Center",
      zip: "86024",
    );

  @override
  void initState() {
    super.initState();
    initPlatformState();
    initialiseCdadsSdk();
    Future.delayed(const Duration(seconds: 5), () {
      _controller.loadAd();
    });
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion =
          await _cdadsPlugin.getPlatformVersion() ?? 'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              const Text('Running on:'),
              Text(_platformVersion),
              const SizedBox(height: 20),
              CDAdWidget(
                controller: _controller,
                adSize: CDAdSize.banner300x250,
              )
            ],
          ),
        ),
      ),
    );
  }
}
