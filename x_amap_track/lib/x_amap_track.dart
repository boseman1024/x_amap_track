import 'dart:async';

import 'package:flutter/services.dart';

class XAmapTrack {
  static const MethodChannel _channel =
  const MethodChannel('x_amap_track');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<void> bind() async {
    await _channel.invokeMethod('bind');
  }
  static Future<void> unbind() async {
    await _channel.invokeMethod('unbind');
  }
  static Future<void> startAlarm() async {
    await _channel.invokeMethod('startAlarm');
  }
  static Future<void> stopAlarm() async {
    await _channel.invokeMethod('stopAlarm');
  }
}
