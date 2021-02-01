import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_hardware_interaction/flutter_hardware_interaction.dart';

void main() {
  const MethodChannel channel = MethodChannel('flutter_hardware_interaction');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await FlutterHardwareInteraction.platformVersion, '42');
  });
}
