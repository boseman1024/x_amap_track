package com.axisx.x_amap_track;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.os.IBinder;
import android.util.Log;
import android.content.BroadcastReceiver;
import androidx.annotation.NonNull;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.embedding.engine.plugins.activity.*;
import io.flutter.embedding.engine.plugins.service.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import static android.content.Context.NOTIFICATION_SERVICE;
import com.amap.api.track.query.entity.Point;

public class XAmapTrackPlugin implements FlutterPlugin, MethodCallHandler,ActivityAware,ServiceAware{
    private final String TAG = "XAmapTrackPlugin";
    private static PluginRegistry.Registrar registrar;
    private Context context;
    private static Activity activity;

    private XAmapTrackService xAmapTrackService;

    private static IntentFilter s_intentFilter;
    private ThreadPoolExecutor threadPool;


    static {
        s_intentFilter = new IntentFilter();
        s_intentFilter.addAction("ALARM");
    }

    //服务连接
    private ServiceConnection conn = new ServiceConnection(){
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected===");
        }
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected===");
            try{
                xAmapTrackService = ((XAmapTrackService.MyBinder)service).getService();
                Intent i = new Intent();
                i.putExtra("SERVICE_ID", String.valueOf(93958));
                i.putExtra("TERMINAL_ID", String.valueOf(231732097));
                i.putExtra("TRACK_ID", String.valueOf(20));
                xAmapTrackService.onStartCommand(i, 0, 0);
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

    public static void registerWith(Registrar registrar) {
        XAmapTrackPlugin.registrar = registrar;
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "x_amap_track");
        channel.setMethodCallHandler(new XAmapTrackPlugin(registrar.context()));
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } if (call.method.equals("bind")) {
            Intent intent = new Intent(context,XAmapTrackService.class);
            context.bindService(intent,conn,Context.BIND_AUTO_CREATE);
        } if (call.method.equals("unbind")) {
            context.unbindService(conn);
        } if (call.method.equals("startAlarm")) {
            startAlarm();
        } if (call.method.equals("stopAlarm")) {
            stopAlarm();
        } if (call.method.equals("startRecord")) {
            xAmapTrackService.startTrack();
        } if (call.method.equals("stopRecord")) {
            xAmapTrackService.stopTrack();
        } if (call.method.equals("queryLatestPoint")) {
            xAmapTrackService.queryLatestPoint();
        } if (call.method.equals("watchLatestPoint")) {
            result.success(asyncGetLatestPoint());
        } if (call.method.equals("queryTerminalTrack")) {
            xAmapTrackService.queryTerminalTrack();
        } if (call.method.equals("queryHistoryTrack")) {
            xAmapTrackService.queryHistoryTrack();
        } if (call.method.equals("queryTerminal")) {
            xAmapTrackService.queryTerminal();
        } else {
            //result.notImplemented();
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

    public Future<Point> asyncGetLatestPoint() {
        threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        XAmapTrackPointCall xAmapTrackPointCall = new XAmapTrackPointCall(xAmapTrackService);
        Future<Point> future = threadPool.submit(xAmapTrackPointCall);
        return future;
    }

}
