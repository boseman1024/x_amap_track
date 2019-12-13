package com.axisx.x_amap_track;

import java.util.concurrent.Callable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.amap.api.track.query.entity.Point;

public class XAmapTrackPointCall implements Callable<Point>{
    private final String TAG = "XAmapTrackPointCall";
    private boolean flag = true;
    private Point point;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.obj!=null){
                point = (Point)msg.obj;
            }
            flag = false;
            Log.d(TAG,"点位监听结束");
        }
    };

    public XAmapTrackPointCall(XAmapTrackService xAmapTrackService){
        xAmapTrackService.setPointCallback(new XAmapTrackService.PointCallback(){
            @Override
            public void onPointChange(Point point){
                Message msg = new Message();
                msg.obj = point;
                handler.sendMessage(msg);
            }
        });
        xAmapTrackService.queryLatestPoint();
    }

    @Override
    public Point call() throws Exception {
        Log.d(TAG,"点位回调开始");
        while(flag){
            Log.d(TAG,"等待点位返回");
        }
        Log.d(TAG,"点位返回："+point.getTime());
        return point;
    }
}