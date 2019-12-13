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

  @override
  void initState() {
    super.initState();
    initPlatformState();
    WidgetsBinding.instance.addObserver(this);
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
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
            child: Column(
              children: <Widget>[
                Text('Running on: $_platformVersion\n'),
                RaisedButton(
                  onPressed: () async{
                    await XAmapTrack.startAlarm();
                  },
                  child: const Text(
                      'startAlarm',
                      style: TextStyle(fontSize: 20)
                  ),
                ),
                RaisedButton(
                  onPressed: () async {
                    await XAmapTrack.stopAlarm();
                  },
                  child: const Text(
                      'stopAlarm',
                      style: TextStyle(fontSize: 20)
                  ),
                ),
                RaisedButton(
                  onPressed: () async{
                    await XAmapTrack.bind();
                  },
                  child: const Text(
                      'bind',
                      style: TextStyle(fontSize: 20)
                  ),
                ),
                RaisedButton(
                  onPressed: () async{
                    await XAmapTrack.unbind();
                  },
                  child: const Text(
                      'unbind',
                      style: TextStyle(fontSize: 20)
                  ),
                ),
              ],
            )
        ),
      ),
    );
  }
}
