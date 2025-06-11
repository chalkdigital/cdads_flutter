import 'package:cdads_flutter/cdads_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

abstract class CdadsPlatform extends PlatformInterface {
  /// Constructs a CdadsPlatform.
  CdadsPlatform() : super(token: _token);

  static final Object _token = Object();

  static CdadsPlatform _instance = MethodChannelCdads();

  /// The default instance of [CdadsPlatform] to use.
  ///
  /// Defaults to [MethodChannelCdads].
  static CdadsPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [CdadsPlatform] when
  /// they register themselves.
  static set instance(CdadsPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> initialize(Map<String, dynamic> params) {
    throw UnimplementedError('initialize has not been implemented.');
  }
}
