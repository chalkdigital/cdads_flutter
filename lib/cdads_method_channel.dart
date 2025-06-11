import 'package:cdads_flutter/cdads_platform_interface.dart';
import 'package:flutter/services.dart';

/// An implementation of [CdadsPlatform] that uses method channels.
class MethodChannelCdads extends CdadsPlatform {
  /// The method channel used to interact with the native platform.
  // @visibleForTesting
  final methodChannel = const MethodChannel('cdads_flutter');

  @override
  Future<String?> getPlatformVersion() async {
    final version =
        await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<String?> initialize(Map<String, dynamic> params) async {
    await methodChannel.invokeMethod<String>('initialize', params);
    return null;
  }
}
