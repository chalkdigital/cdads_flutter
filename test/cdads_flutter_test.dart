import 'package:flutter_test/flutter_test.dart';
import 'package:cdads_flutter/cdads_flutter.dart';
import 'package:cdads_flutter/cdads_platform_interface.dart';
import 'package:cdads_flutter/cdads_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockCdadsPlatform
    with MockPlatformInterfaceMixin
    implements CdadsPlatform {
  @override
  Future<String?> getPlatformVersion() => Future.value('42');

  @override
  Future<String?> initialize(Map<String, dynamic> params) {
    throw UnimplementedError();
  }
}

void main() {
  final CdadsPlatform initialPlatform = CdadsPlatform.instance;

  test('$MethodChannelCdads is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelCdads>());
  });

  test('getPlatformVersion', () async {
    CdadsFlutter cdadsPlugin = CdadsFlutter();
    MockCdadsPlatform fakePlatform = MockCdadsPlatform();
    CdadsPlatform.instance = fakePlatform;

    expect(await cdadsPlugin.getPlatformVersion(), '42');
  });
}
