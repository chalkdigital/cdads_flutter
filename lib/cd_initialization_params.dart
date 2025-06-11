import 'package:cdads_flutter/cd_ad_defines.dart';

class CDInitializationParams {
  final String partnerKey;
  final String applicationIABCategory;
  final String? partnerSecret;
  final double distanceFilter; // CLLocationDistance => double in Dart
  final double locationUpdateInterval; // NSTimeInterval => double
  final double adLocationExpiryInterval; // NSTimeInterval => double
  final CDLogLevel logLevel;
  final CDADProvider provider;
  final CDEnvironment environment;
  final bool showTrackingTerms;
  final bool? gdpr;
  final bool? consent;
  final bool clientHasUserTrackingPermission;
  final String appName;
  final String? appKey;
  final String? secretKey;
  final String? host;
  final bool? enableTracking;

  // Note: `geoIpLocationEnable` is readonly in Objective-C, so we don't include it here for initialization

  const CDInitializationParams({
    required this.partnerKey,
    required this.applicationIABCategory,
    this.partnerSecret,
    required this.distanceFilter,
    required this.locationUpdateInterval,
    required this.adLocationExpiryInterval,
    this.logLevel = CDLogLevel.off,
    this.provider = CDADProvider.chalk, // default to chalk
    this.environment = CDEnvironment.test,
    this.showTrackingTerms = false,
    this.gdpr,
    this.consent,
    this.clientHasUserTrackingPermission = true,
    this.appName = "",
    this.appKey,
    this.secretKey,
    this.host,
    this.enableTracking = false,
  });

  Map<String, dynamic> toMap() {
    return {
      'partnerKey': partnerKey,
      'applicationIABCategory': applicationIABCategory,
      'partnerSecret': partnerSecret,
      'distanceFilter': distanceFilter,
      'locationUpdateInterval': locationUpdateInterval,
      'adLocationExpiryInterval': adLocationExpiryInterval,
      'logLevel': logLevel.value, // passing integer value
      'provider': provider.name, // passing string name
      'environment': environment.name, // passing string name
      'showTrackingTerms': showTrackingTerms,
      'gdpr': gdpr,
      'consent': consent,
      'clientHasUserTrackingPermission': clientHasUserTrackingPermission,
      'appName': appName,
      'appKey': appKey,
      'secretKey': secretKey,
      'host': host,
      'enableTracking': enableTracking,
    };
  }
}
