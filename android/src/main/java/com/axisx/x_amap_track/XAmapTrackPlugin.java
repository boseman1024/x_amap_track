package com.axisx.x_amap_track;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.net.Uri;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.os.IBinder;
import android.os.Build;
import android.util.Log;
import android.content.BroadcastReceiver;
import androidx.annotation.NonNull;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.embedding.engine.plugins.activity.*;
import io.flutter.embedding.engine.plugins.service.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import static android.content.Context.NOTIFICATION_SERVICE;
import android.os.Handler;
import android.os.Message;
import com.amap.api.track.query.entity.Point;
import com.amap.api.track.query.entity.HistoryTrack;
import com.amap.api.track.query.model.QueryTrackResponse;
import com.amap.api.location.*;
import com.alibaba.fastjson.JSON;
import java.util.HashMap;
import java.util.Map;

public class XAmapTrackPlugin implements FlutterPlugin, MethodCallHandler,ActivityAware,ServiceAware{
    private final String TAG = "XAmapTrackPlugin";
    private static PluginRegistry.Registrar registrar;
    private Context context;
    private static Activity activity;
    private static EventChannel.EventSink eventSink;
    /*private XAmapTrackService xAmapTrackService;*/
    private XAmapLocationService xAmapLocationService;
    private static IntentFilter s_intentFilter;
    private ThreadPoolExecutor threadPool;

    private String SERVICE_ID;
    private String TERMINAL_ID;
    private String TRACK_ID;

    static {
        s_intentFilter = new IntentFilter();
        s_intentFilter.addAction("ALARM");
    }

    //轨迹：监听传递
/*    private Handler trackHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.obj!=null){
                QueryTrackResponse queryTrackResponse = (QueryTrackResponse)msg.obj;
                String json = JSON.toJSONString(queryTrackResponse);
                Log.d(TAG,"点位返回："+json);
                if(eventSink!=null){
                    Log.d(TAG,"eventSink已初始化：");
                    eventSink.success(json);
                }else{
                    Log.d(TAG,"eventSink未初始化");
                }

            }
            Log.d(TAG,"点位监听结束");
        }
    };*/

    //轨迹服务连接
/*    private ServiceConnection trackConn = new ServiceConnection(){
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected===");
        }
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected===");
            try{
                xAmapTrackService = ((XAmapTrackService.MyBinder)service).getService();
                Intent i = new Intent();
                i.putExtra("SERVICE_ID", SERVICE_ID);
                i.putExtra("TERMINAL_ID", TERMINAL_ID);
                i.putExtra("TRACK_ID", TRACK_ID);
                xAmapTrackService.onStartCommand(i, 0, 0);
                xAmapTrackService.setQueryTrackCallback(new XAmapTrackService.QueryTrackCallback(){
                    @Override
                    public void onQueryTrackChange(QueryTrackResponse queryTrackResponse){
                        Message msg = new Message();
                        msg.obj = queryTrackResponse;
                        trackHandler.sendMessage(msg);
                    }
                });
            }catch (Exception e){
                Log.d(TAG,"服务异常："+e);
            }
        }
    };*/

    //定位：监听传递
    private Handler locationHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.obj!=null){
                AMapLocation aMapLocation = (AMapLocation)msg.obj;
                Map<String,Object> map = new HashMap<String,Object>();
                map.put("locationType",aMapLocation.getLocationType());//获取当前定位结果来源，如网络定位结果，详见定位类型表
                map.put("lat",aMapLocation.getLatitude());//获取纬度
                map.put("lng",aMapLocation.getLongitude());//获取经度
                map.put("accuracy",aMapLocation.getAccuracy());//获取精度信息
                map.put("address",aMapLocation.getAddress());//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                map.put("country",aMapLocation.getCountry());//国家信息
                map.put("province",aMapLocation.getProvince());//省信息
                map.put("city",aMapLocation.getCity());//城市信息
                map.put("district",aMapLocation.getDistrict());//城区信息
                map.put("street",aMapLocation.getStreet());//街道信息
                map.put("streetNum",aMapLocation.getStreetNum());//街道门牌号信息
                map.put("cityCode",aMapLocation.getCityCode());//城市编码
                map.put("adCode",aMapLocation.getAdCode());//地区编码
                map.put("aoiName",aMapLocation.getAoiName());//获取当前定位点的AOI信息
                map.put("buildingId",aMapLocation.getBuildingId());//获取当前室内定位的建筑物Id
                map.put("floor",aMapLocation.getFloor());//获取当前室内定位的楼层
                map.put("gpsAccuracyStatus",aMapLocation.getGpsAccuracyStatus());//获取GPS的当前状态
                map.put("bearing",aMapLocation.getBearing());//获取方向角
                map.put("conScenario",aMapLocation.getConScenario());//室内外置信度
                map.put("speed",aMapLocation.getSpeed());//速度
                map.put("trustedLevel",aMapLocation.getTrustedLevel());//定位结果的可信度
                map.put("coordType",aMapLocation.getCoordType());//坐标系类型
                map.put("statellites",aMapLocation.getSatellites());//卫星数量
                String json = JSON.toJSONString(map);
                Log.d(TAG,"点位返回："+json);
                if(eventSink!=null){
                    Log.d(TAG,"eventSink已初始化：");
                    eventSink.success(json);
                }else{
                    Log.d(TAG,"eventSink未初始化");
                }

            }
            Log.d(TAG,"点位监听结束");
        }
    };

    //定位服务
    private ServiceConnection locationConn = new ServiceConnection(){
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "关闭定位服务");
        }
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "开启定位服务");
            try{
                xAmapLocationService = ((XAmapLocationService.MyBinder)service).getService();
                Intent i = new Intent();
                xAmapLocationService.onStartCommand(i, 0, 0);
                xAmapLocationService.setLocationCallback(new XAmapLocationService.LocationCallback(){
                    @Override
                    public void onLocationChange(AMapLocation amapLocation){
                        Message msg = new Message();
                        msg.obj = amapLocation;
                        locationHandler.sendMessage(msg);
                    }
                });
            }catch (Exception e){
                Log.d(TAG,"服务异常："+e);
            }
        }
    };

    //定时任务监听
    private final BroadcastReceiver m_testReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals("ALARM")) {
                Log.d(TAG, "ALARM is Received");
                startAlarm();
            }
        }
    };

    public XAmapTrackPlugin(Context context) {
        this.context = context;
    }
    public XAmapTrackPlugin() {}

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        final MethodChannel channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "x_amap_track");
        channel.setMethodCallHandler(new XAmapTrackPlugin(flutterPluginBinding.getApplicationContext()));
        final EventChannel eventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "x_amap_track_event_channel");
        eventChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object o, EventChannel.EventSink eventSink) {
                XAmapTrackPlugin.this.eventSink = eventSink;
                Log.d(TAG,(eventSink==null)+" ");
            }
            @Override
            public void onCancel(Object o) {
                XAmapTrackPlugin.this.eventSink = null;
            }
        });
    }
    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {}

    @Override
    public void onAttachedToActivity(ActivityPluginBinding activityPluginBinding) {
        //绑定activity
        if(this.activity==null){
            this.activity = activityPluginBinding.getActivity();
        }
    }
    @Override
    public void onDetachedFromActivityForConfigChanges(){}
    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding activityPluginBinding){}
    @Override
    public void onDetachedFromActivity(){}
    @Override
    public void onAttachedToService(@NonNull ServicePluginBinding servicePluginBinding){
        Log.d(TAG,"服务："+servicePluginBinding.getService());
    }
    @Override
    public void onDetachedFromService(){}

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        }
/*        if (call.method.equals("bind")) {
            SERVICE_ID = call.argument("serviceId");
            TERMINAL_ID = call.argument("terminalId");
            TRACK_ID = call.argument("trackId");
            Intent intent = new Intent(context,XAmapTrackService.class);
            context.bindService(intent,trackConn,Context.BIND_AUTO_CREATE);
        }
        if (call.method.equals("unbind")) {
            SERVICE_ID = null;
            TERMINAL_ID = null;
            TRACK_ID = null;
            context.unbindService(trackConn);
        }
        if (call.method.equals("startAlarm")) {
            startAlarm();
        }
        if (call.method.equals("stopAlarm")) {
            stopAlarm();
        }
        if (call.method.equals("startRecord")) {
            xAmapTrackService.startTrack();
            result.success("success ");
        }
        if (call.method.equals("stopRecord")) {
            xAmapTrackService.stopGather();
            result.success("success ");
        }
        if (call.method.equals("queryLatestPoint")) {
            xAmapTrackService.queryLatestPoint();
            result.success("success ");
        }
        if (call.method.equals("queryTerminalTrack")) {
            long startTime = Long.valueOf(call.argument("startTime").toString());
            long endTime = Long.valueOf(call.argument("endTime").toString());
            xAmapTrackService.queryTerminalTrack(startTime,endTime);
            result.success("success ");
        }
        if (call.method.equals("queryHistoryTrack")) {
            long startTime = Long.valueOf(call.argument("startTime").toString());
            long endTime = Long.valueOf(call.argument("endTime").toString());
            xAmapTrackService.queryHistoryTrack(startTime,endTime);
            result.success("success ");
        }
        if (call.method.equals("queryTerminal")) {
            xAmapTrackService.queryTerminal();
            result.success("success ");
        }*/
        //定位服务
        if(call.method.equals("bindLocation")){
            Intent intent = new Intent(context,XAmapLocationService.class);
            context.bindService(intent,locationConn,Context.BIND_AUTO_CREATE);
            result.success("success ");
        }
        if(call.method.equals("unbindLocation")){
            context.unbindService(locationConn);
            result.success("success ");
        }
        if(call.method.equals("initLocation")){
            xAmapLocationService.init();
            result.success("success ");
        }
        if(call.method.equals("startLocation")){
            xAmapLocationService.startLocation();
            result.success("success ");
        }
        if(call.method.equals("stopLocation")){
            xAmapLocationService.stopLocation();
            result.success("success ");
        }
        if(call.method.equals("openPermissionSetting")){
            openPermissionSetting();
            result.success("success ");
        }
    }

    public void startAlarm(){
        context.registerReceiver(m_testReceiver, s_intentFilter);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                new Intent("ALARM"), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 3 * 1000, // 60s later
                pendingIntent);
    }
    public void stopAlarm(){
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                new Intent("ALARM"), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        try {
            am.cancel(pendingIntent);
            Log.d(TAG, "Alarm is Canceled.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Alarm is not Canceled: " + e.toString());
        }
        context.unregisterReceiver(m_testReceiver);
    }
    public void openPermissionSetting(){
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        context.startActivity(intent);
    }
}
