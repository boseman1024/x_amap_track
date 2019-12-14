package com.axisx.x_amap_track;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Binder;
import android.util.Log;
import android.app.PendingIntent;
import android.app.Notification;
import com.amap.api.track.*;
import com.amap.api.track.ErrorCode;
import com.amap.api.track.OnTrackLifecycleListener;
import com.amap.api.track.query.model.*;
import com.amap.api.track.query.entity.*;
import com.amap.api.track.TrackParam;
import java.util.ArrayList;
import java.util.List;

public class XAmapTrackService extends  Service{
    private static final String CHANNEL_ID_SERVICE_RUNNING = "CHANNEL_ID_SERVICE_RUNNING";
    private final String TAG = "XAmapTrackService";
    private final IBinder binder = new MyBinder();        //绑定器
    private Intent intent;
    private PointCallback pointCallback;
    private AMapTrackClient aMapTrackClient;
    private boolean isServiceRunning;
    private boolean isGatherRunning;
    private long SERVICE_ID;
    private long TERMINAL_ID;
    private long TRACK_ID;

    final OnTrackListener onTrackListener = new OnTrackListener() {
        @Override
        public void onParamErrorCallback(ParamErrorResponse paramErrorResponse){
            Log.d(TAG, "参数错误");
        }
        @Override
        public void onAddTrackCallback(AddTrackResponse addTrackResponse) {
            if (addTrackResponse.isSuccess()) {
                TRACK_ID = addTrackResponse.getTrid();
                Log.d(TAG, "创建新轨迹成功："+TRACK_ID);
                startGather();
            } else {
                Log.d(TAG, "创建新轨迹失败");
            }
        }
        @Override
        public void onLatestPointCallback(LatestPointResponse latestPointResponse){
            if (latestPointResponse.isSuccess()) {
                Point point = latestPointResponse.getLatestPoint().getPoint();
                Log.d(TAG, "实时位置："+point.getTime());
                if(pointCallback!=null){pointCallback.onPointChange(point);}
            } else {
                Log.d(TAG, "查询实时位置失败");
                if(pointCallback!=null){pointCallback.onPointChange(null);}
            }
        }
        @Override
        public void onCreateTerminalCallback(AddTerminalResponse response){

        }
        @Override
        public void onDistanceCallback(DistanceResponse response){

        }
        @Override
        public void onHistoryTrackCallback(HistoryTrackResponse historyTrackResponse){
            if (historyTrackResponse.isSuccess()) {
                HistoryTrack historyTrack = historyTrackResponse.getHistoryTrack();
                Log.d(TAG, historyTrack.getCount()+" ");
            } else {
                Log.d(TAG, "查询历史轨迹");
            }
        }
        @Override
        public void onQueryTerminalCallback(QueryTerminalResponse queryTerminalResponse){
            if (queryTerminalResponse.isSuccess()) {

            } else {

            }
        }
        @Override
        public void onQueryTrackCallback(QueryTrackResponse queryTrackResponse){
            if (queryTrackResponse.isSuccess()) {
                Log.d(TAG, "查询终端轨迹数："+queryTrackResponse.getCount());
                List<Track> list = queryTrackResponse.getTracks();
                for(int i=0;i<list.size();i++){
                    Log.d(TAG, "查询终端轨迹ID："+list.get(i).getTrid());
                }
            } else {
                Log.d(TAG, "查询终端轨迹失败");
            }
        }
    };

    final OnTrackLifecycleListener onTrackLifecycleListener = new OnTrackLifecycleListener() {
        @Override
        public void onBindServiceCallback(int status, String msg) {
            Log.d(TAG, "onBindServiceCallback, status: " + status + ", msg: " + msg);
        }
        @Override
        public void onStartTrackCallback(int status, String msg) {
            if (status == ErrorCode.TrackListen.START_TRACK_SUCEE || status == ErrorCode.TrackListen.START_TRACK_SUCEE_NO_NETWORK) {
                // 成功启动
                Log.d(TAG, "启动服务成功");
                isServiceRunning = true;
                if(SERVICE_ID!=0&&TERMINAL_ID!=0&&TRACK_ID==0){
                    addTrack();
                }else{
                    startGather();
                }
            } else if (status == ErrorCode.TrackListen.START_TRACK_ALREADY_STARTED) {
                // 已经启动
                Log.d(TAG, "启动服务成功");
                isServiceRunning = true;
                if(SERVICE_ID!=0&&TERMINAL_ID!=0&&TRACK_ID==0){
                    addTrack();
                }else{
                    startGather();
                }
            } else {
                Log.d(TAG, "error onStopTrackCallback, status: " + status + ", msg: " + msg);
            }
        }
        @Override
        public void onStopTrackCallback(int status, String msg) {
            if (status == ErrorCode.TrackListen.STOP_TRACK_SUCCE) {
                // 成功停止
                Log.d(TAG, "停止服务成功");
                isServiceRunning = false;
                isGatherRunning = false;
                stopGather();
            } else {
                Log.d(TAG, "error onStopTrackCallback, status: " + status + ", msg: " + msg);
            }
        }
        @Override
        public void onStartGatherCallback(int status, String msg) {
            if (status == ErrorCode.TrackListen.START_GATHER_SUCEE ||
                    status == ErrorCode.TrackListen.START_GATHER_ALREADY_STARTED) {
                Log.d(TAG,"定位采集开启成功");
            } else {
                Log.d(TAG,"定位采集启动异常");
                Log.d(TAG, "error onStartGatherCallback, status: " + status + ", msg: " + msg);
            }
        }
        @Override
        public void onStopGatherCallback(int status, String msg) {
            if (status == ErrorCode.TrackListen.STOP_GATHER_SUCCE) {
                Log.d(TAG,"定位采集停止成功");
                isGatherRunning = false;
            } else {
                Log.d(TAG, "error onStopGatherCallback, status: " + status + ", msg: " + msg);
            }
        }
    };

    public class MyBinder extends Binder {
        public XAmapTrackService getService() {
            return XAmapTrackService.this;    //返回本服务
        }
    }

    public void setPointCallback(PointCallback pointCallback) {
        this.pointCallback = pointCallback;
    }

    public interface PointCallback {
        void onPointChange(Point point);
    }

    /** 绑定时执行 */
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind()");
        return binder;
    }

    /** 只在创建时执行一次 */
    @Override
    public void onCreate() {
        super.onCreate();
        init();
        Notification.Builder builder;
        builder = new Notification.Builder(this, CHANNEL_ID_SERVICE_RUNNING);
        Intent nfIntent = new Intent(this,XAmapTrackService.class);
        nfIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0))
                .setSmallIcon(com.axisx.x_amap_track.R.mipmap.ic_launcher)
                .setContentTitle("后台运行中")
                .setContentText("后台运行中");
        Notification notification = builder.build();
        startForeground(1001, notification);
        Log.i(TAG, "onCreate()");
    }

    /** 断开绑定或者停止服务时执行 */
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        Log.i(TAG, "onDestroy()");
    }

    /** 当内存不够时执行该方法 */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.i(TAG, "onLowMemory()");
        onDestroy();
    }

    /** 当重新绑定是执行 */
    @Override
    public void onRebind(Intent intent) {
        // TODO Auto-generated method stub
        super.onRebind(intent);
        Log.i(TAG, "onRebind()");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);
        Log.i(TAG, "onStart()");
    }

    /** 每次执行Service都会执行该方法 */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand --->flags = " + flags + "     startId = " + startId);
        this.intent = intent;
        SERVICE_ID = Long.valueOf(intent.getStringExtra("SERVICE_ID"));
        TERMINAL_ID = Long.valueOf(intent.getStringExtra("TERMINAL_ID"));
        TRACK_ID = Long.valueOf(intent.getStringExtra("TRACK_ID"));
        return super.onStartCommand(intent, flags, startId);
    }

    /** 断开绑定时执行 */
    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind()");
        return super.onUnbind(intent);
    }

    public void init(){
        if(aMapTrackClient==null){
            aMapTrackClient = new AMapTrackClient(this);
            aMapTrackClient.setInterval(5, 30);
        }
    }

    public void startTrack(){
        if (!isServiceRunning) {
            TrackParam trackParam = new TrackParam(SERVICE_ID, TERMINAL_ID);
            aMapTrackClient.startTrack(trackParam, onTrackLifecycleListener);
            isServiceRunning = true;
        }
    }

    public void stopTrack(){
        if (isServiceRunning) {
            aMapTrackClient.stopTrack(new TrackParam(SERVICE_ID, TERMINAL_ID), onTrackLifecycleListener);
            isServiceRunning = false;
        }
    }

    public void startGather(){
        if (!isGatherRunning) {
            aMapTrackClient.setTrackId(TRACK_ID);
            aMapTrackClient.startGather(onTrackLifecycleListener);
            isGatherRunning = true;
        }
    }

    public void stopGather(){
        if (isGatherRunning) {
            aMapTrackClient.stopGather(onTrackLifecycleListener);
            isGatherRunning = false;
        }
    }

    public void addTrack(){
            aMapTrackClient.addTrack(new AddTrackRequest(SERVICE_ID, TERMINAL_ID), onTrackListener);
    }

    public void queryLatestPoint(){
        aMapTrackClient.queryLatestPoint(new LatestPointRequest(SERVICE_ID,TERMINAL_ID,TRACK_ID),onTrackListener);
    }

    public void queryHistoryTrack(){
        HistoryTrackRequest historyTrackRequest = new HistoryTrackRequest(
                SERVICE_ID,
                TERMINAL_ID,
                System.currentTimeMillis() - 12 * 60 * 60 * 1000,
                System.currentTimeMillis(),
                0,      // 不绑路
                0,      // 不做距离补偿
                5000,   // 距离补偿阈值，只有超过5km的点才启用距离补偿
                0,  // 由旧到新排序
                1,  // 返回第1页数据
                100,    // 一页不超过100条
                ""  // 暂未实现，该参数无意义，请留空
        );
        aMapTrackClient.queryHistoryTrack(historyTrackRequest, onTrackListener);
    }
    public void queryTerminal(){
        QueryTerminalRequest queryTerminalRequest = new QueryTerminalRequest(
                SERVICE_ID,
                ""
        );
        aMapTrackClient.queryTerminal(queryTerminalRequest, onTrackListener);
    }
    public void queryTerminalTrack(){
        QueryTrackRequest queryTrackRequest = new QueryTrackRequest(
                SERVICE_ID,
                TERMINAL_ID,
                System.currentTimeMillis() - 24 * 60 * 60 * 1000,
                System.currentTimeMillis()
        );
        aMapTrackClient.queryTerminalTrack(queryTrackRequest, onTrackListener);
    }

}
