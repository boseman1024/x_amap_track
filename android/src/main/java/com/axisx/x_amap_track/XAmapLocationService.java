package com.axisx.x_amap_track;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Binder;
import android.util.Log;
import android.app.PendingIntent;
import android.app.Notification;
import com.amap.api.location.*;
import android.app.NotificationManager;
import android.app.NotificationChannel;

public class XAmapLocationService extends  Service{
    private final String TAG = "XAmapLocationService";
    private final IBinder binder = new MyBinder();        //绑定器
    private Intent intent;
    private AMapLocationClient aMapLocationClient;
    private LocationCallback locationCallback;
    //声明定位回调监听器
    final AMapLocationListener mLocationListener = new AMapLocationListener(){
        @Override
        public void onLocationChanged(AMapLocation amapLocation){
            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
                    if(locationCallback!=null){
                        locationCallback.onLocationChange(amapLocation);
                        Log.d(TAG, "发送终端轨迹：");
                    }
                }else {
                    //定位失败
                    Log.e(TAG,"定位失败, ErrCode:" + amapLocation.getErrorCode() + ", errInfo:" + amapLocation.getErrorInfo());
                }
            }
        }
    };


    public void setLocationCallback(LocationCallback locationCallback){
        this.locationCallback = locationCallback;
    }

    public interface LocationCallback{
        void onLocationChange(AMapLocation amapLocation);
    }

    public class MyBinder extends Binder {
        public XAmapLocationService getService() {
            return XAmapLocationService.this;    //返回本服务
        }
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
        NotificationManager manager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel=new NotificationChannel("channel_1","notification",NotificationManager.IMPORTANCE_HIGH);
        manager.createNotificationChannel (channel);
        Notification.Builder builder = new Notification.Builder(this, "channel_1");
        Intent nfIntent = new Intent(this,XAmapLocationService.class);
        nfIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0))
                .setSmallIcon(com.axisx.x_amap_track.R.mipmap.ic_launcher)
                .setContentTitle("后台运行中")
                .setContentText("后台运行中");
        Notification notification = builder.build();
        startForeground(9527, notification);
        Log.i(TAG, "onCreate()");
    }

    /** 断开绑定或者停止服务时执行 */
    @Override
    public void onDestroy() {
        stopForeground(true);
        aMapLocationClient.onDestroy();
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
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
        return super.onStartCommand(intent, flags, startId);
    }

    /** 断开绑定时执行 */
    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind()");
        return super.onUnbind(intent);
    }

    public void init(){
        if(aMapLocationClient==null){
            aMapLocationClient = new AMapLocationClient(this);
            aMapLocationClient.setLocationListener(mLocationListener);
            AMapLocationClientOption aMapLocationClientOption = new AMapLocationClientOption();
            aMapLocationClientOption.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.Transport);
            aMapLocationClientOption.setMockEnable(true);
            aMapLocationClientOption.setNeedAddress(true);
            aMapLocationClientOption.setOnceLocation(false);
            aMapLocationClientOption.setSensorEnable(true);
            aMapLocationClientOption.setInterval(2000);
            aMapLocationClientOption.setLocationCacheEnable(true);
            aMapLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            aMapLocationClientOption.setHttpTimeOut(20000);
            aMapLocationClient.setLocationOption(aMapLocationClientOption);
            Log.d(TAG,"定位初始化完成");
        }
    }

    public void startLocation(){
        Log.d(TAG,"开始定位");
        aMapLocationClient.startLocation();
    }
    public void stopLocation(){
        Log.d(TAG,"结束定位");
        aMapLocationClient.stopLocation();
    }
}
