package com.axisx.x_amap_track;

import java.util.concurrent.Callable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.amap.api.track.query.entity.Point;
import org.json.JSONObject;

@Deprecated
public class XAmapTrackPointCall implements Callable<String>{
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
                Log.d(TAG,"点位发生改变");
                Message msg = new Message();
                msg.obj = point;
                handler.sendMessage(msg);
            }
        });
        Log.d(TAG,"PointCallback初始化");
        xAmapTrackService.queryLatestPoint();
    }

    @Override
    public String call() throws Exception {
        Log.d(TAG,"点位回调开始");
        while(flag){
            Log.d(TAG,"等待点位返回");
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("time" , point.getTime());
        jsonObject.put("lat" , point.getLat());
        jsonObject.put("lng" , point.getLng());
        jsonObject.put("props" , point.getProps());
        jsonObject.put("direction" , point.getDirection());
        jsonObject.put("accuracy" , point.getAccuracy());
        jsonObject.put("height" , point.getHeight());
        Log.d(TAG,"点位返回："+jsonObject.toString());
        return jsonObject.toString();
    }
}