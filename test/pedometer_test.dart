import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:pedometer/pedometer.dart';

void main() {
  const MethodChannel channel = MethodChannel('pedometer');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('isStarted', () async {
    Pedometer ped = Pedometer();
    expect(await ped.isStarted(), '42');
  });
}
