import 'package:cdads_flutter/cd_initialization_params.dart';
import 'package:cdads_flutter/cdads_platform_interface.dart';

class CdadsFlutter {
  // static const MethodChannel _channel = MethodChannel('cdads_flutter');

  Future<String?> initialize(CDInitializationParams params) async {
    return CdadsPlatform.instance.initialize(params.toMap());
  }

  Future<String?> getPlatformVersion() {
    return CdadsPlatform.instance.getPlatformVersion();
  }
}
