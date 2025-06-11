import 'package:cdads_flutter/cd_geo_info.dart';
import 'package:flutter/services.dart';

class CDAdController {
  late MethodChannel _channel;

  String? placementId;
  String? partnerId;
  bool isLocationAutoUpdateEnabled = true;
  bool isAdAutoRefreshEnabled = true;
  bool isTesting = false;
  bool isCloseable = false;
  bool useInappBrowser = false;
  CDGeoInfo? geoInfo;

  void bindChannel(MethodChannel channel) {
    _channel = channel;
  }

  Future<void> loadAd() async {
    final Map<String, dynamic> config = {
      'placementId': placementId,
      'partnerId': partnerId,
      'isLocationAutoUpdateEnabled': isLocationAutoUpdateEnabled,
      'isAdAutoRefreshEnabled': isAdAutoRefreshEnabled,
      'isTesting': isTesting,
      'isCloseable': isCloseable,
      'useInappBrowser': useInappBrowser,
      'geoInfo': geoInfo?.toMap(),
    };
    await _channel.invokeMethod('loadAd', config);
  }
}
