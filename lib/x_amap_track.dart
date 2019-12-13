import 'dart:async';

import 'package:flutter/services.dart';
import 'entity/Point.dart';

class XAmapTrack {
  static const MethodChannel _channel =
  const MethodChannel('x_amap_track');
  static StreamController<Point> sc;
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
  static Future<Stream> watchTrack() async{
    Duration interval = Duration(seconds: 5);
    Stream<int> stream = Stream.periodic(interval, (data) => data);
    return stream;
  }
  //TODO
  static Stream<Point> watchLatestPoint() async* {
    sc = StreamController<Point>();
    final point = await _channel.invokeMethod('watchLatestPoint');
    sc.sink.add(point);
    yield* sc.stream;
  }
  static Future<void> stopWatchLatestPoint() async {
    await sc.close();
  }
}
