import 'dart:async';
import 'package:flutter/services.dart';

class XAmapTrack {
  static const MethodChannel _channel = const MethodChannel('x_amap_track');
  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

/*  static Future<void> bind(num serviceId,num terminalId,num trackId) async {
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
  static Future<void> queryTerminalTrack(num startTime,num endTime) async {
    await _channel.invokeMethod('queryTerminalTrack',{'startTime':'$startTime','endTime':'$endTime'});
  }
  static Future<void> queryHistoryTrack(num startTime,num endTime) async {
    await _channel.invokeMethod('queryHistoryTrack',{'startTime':'$startTime','endTime':'$endTime'});
  }
  static Future<void> queryTerminal() async {
    await _channel.invokeMethod('queryTerminal');
  }
  static Future<void> startRecord() async {
    await _channel.invokeMethod('startRecord');
  }
  static Future<void> stopRecord() async {
    await _channel.invokeMethod('stopRecord');
  }*/

  static Future<void> bindLocation() async {
    await _channel.invokeMethod('bindLocation');
  }
  static Future<void> unbindLocation() async {
    await _channel.invokeMethod('unbindLocation');
  }
  static Future<void> initLocation() async {
    await _channel.invokeMethod('initLocation');
  }
  static Future<void> startLocation() async {
    await _channel.invokeMethod('startLocation');
  }
  static Future<void> stopLocation() async {
    await _channel.invokeMethod('stopLocation');
  }
  static Future<void> openPermissionSetting() async{
    await _channel.invokeMethod('openPermissionSetting');
  }
}
