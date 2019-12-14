import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:x_amap_track/x_amap_track.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> with WidgetsBindingObserver{
  String _platformVersion = 'Unknown';
  static const EventChannel _eventChannel = EventChannel("x_amap_track_event_channel");
  String _point = '';
  @override
  void initState() {
    super.initState();
    initPlatformState();
    WidgetsBinding.instance.addObserver(this);
    _eventChannel.receiveBroadcastStream().listen((data){
      setState(() {
        _point = data;
      });
    });
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    super.didChangeAppLifecycleState(state);
    if(state == AppLifecycleState.paused){
      print("生命周期：暂停");
    }
    if(state == AppLifecycleState.resumed){
      print("生命周期：恢复");
    }
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  Future<void> initPlatformState() async {
    String platformVersion;
    try {
      platformVersion = await XAmapTrack.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }
    await XAmapTrack.platformVersion;

    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        body: Center(
            child: Column(
              children: <Widget>[
                Text('Running on: $_platformVersion\n'),
                Text('Running on: $_point\n'),
                RaisedButton(
                  onPressed: () async{
                    await XAmapTrack.startAlarm();
                  },
                  child: const Text(
                      'startAlarm',
                      style: TextStyle(fontSize: 12)
                  ),
                ),
                RaisedButton(
                  onPressed: () async {
                    await XAmapTrack.stopAlarm();
                  },
                  child: const Text(
                      'stopAlarm',
                      style: TextStyle(fontSize: 12)
                  ),
                ),
                RaisedButton(
                  onPressed: () async{
                    await XAmapTrack.bind(93958,231732097,20);
                  },
                  child: const Text(
                      'bind',
                      style: TextStyle(fontSize: 12)
                  ),
                ),
                RaisedButton(
                  onPressed: () async{
                    await XAmapTrack.unbind();
                  },
                  child: const Text(
                      'unbind',
                      style: TextStyle(fontSize: 12)
                  ),
                ),
                RaisedButton(
                  onPressed: () async{
                    await XAmapTrack.queryLatestPoint();
                  },
                  child: const Text(
                      'queryLatestPoint',
                      style: TextStyle(fontSize: 12)
                  ),
                ),
                RaisedButton(
                  onPressed: () async{
                    await XAmapTrack.queryTerminalTrack();
                  },
                  child: const Text(
                      'queryTerminalTrack',
                      style: TextStyle(fontSize: 12)
                  ),
                ),
                RaisedButton(
                  onPressed: () async{
                    await XAmapTrack.queryHistoryTrack();
                  },
                  child: const Text(
                      'queryHistoryTrack',
                      style: TextStyle(fontSize: 12)
                  ),
                ),
                RaisedButton(
                  onPressed: () async{
                    await XAmapTrack.queryTerminal();
                  },
                  child: const Text(
                      'queryTerminal',
                      style: TextStyle(fontSize: 12)
                  ),
                ),
                RaisedButton(
                  onPressed: () async{
                    await XAmapTrack.startRecord();
                  },
                  child: const Text(
                      'startRecord',
                      style: TextStyle(fontSize: 12)
                  ),
                ),
                RaisedButton(
                  onPressed: () async{
                    await XAmapTrack.stopRecord();
                  },
                  child: const Text(
                      'stopRecord',
                      style: TextStyle(fontSize: 12)
                  ),
                ),
              ],
            )
        ),
      ),
    );
  }
}
