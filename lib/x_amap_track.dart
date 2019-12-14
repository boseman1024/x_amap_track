import 'dart:async';
import 'package:flutter/services.dart';

class XAmapTrack {
  static const MethodChannel _channel = const MethodChannel('x_amap_track');
  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<void> bind(num serviceId,num terminalId,num trackId) async {
    await _channel.invokeMethod('bind',{'serviceId': '$serviceId','terminalId': '$terminalId','trackId': '$trackId'});
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
  static Future<void> queryLatestPoint() async {
    await _channel.invokeMethod('queryLatestPoint');
  }
  static Future<void> queryTerminalTrack() async {
    await _channel.invokeMethod('queryTerminalTrack');
  }
  static Future<void> queryHistoryTrack() async {
    await _channel.invokeMethod('queryHistoryTrack');
  }
  static Future<void> queryTerminal() async {
    await _channel.invokeMethod('queryTerminal');
  }
  static Future<void> startRecord() async {
    await _channel.invokeMethod('startRecord');
  }
  static Future<void> stopRecord() async {
    await _channel.invokeMethod('stopRecord');
  }
}
