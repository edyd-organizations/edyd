package com.oto.edyd.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * 定时获得经纬度，并发送经纬度
 * Created by yql on 2015/10/7.
 */
public class TimerService extends Service implements LocationSource, AMapLocationListener {

    //private Timer timer = new Timer(); //定时对象
    //private final int PERIOD = 1*60*1000; //定时间隔

    private AMap aMap;
    private MapView mapView;
    public static OnLocationChangedListener mListener = null;
    private LocationManagerProxy mAMapLocationManager;
    private TimerServiceBinder binder = new TimerServiceBinder();

    private Common common;
    private List<Integer> controlIDList = new ArrayList<Integer>(); //用于存储调度单号
    private List<Integer> controlStatusList = new ArrayList<Integer>(); //用于存储调度单号
    private AMapLocation location;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mListener != null) {
            reActivate(mListener);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        //startTimer(); //每隔十五秒执行一次
        mapView = new MapView(getApplicationContext());

        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
        }
    }
    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        location = amapLocation;

        if (mListener != null && amapLocation != null) {
            if (amapLocation != null && amapLocation.getAMapException().getErrorCode() == 0) {
                getTimerOrder();
                deactivate();
            } else {
                Log.e("AmapErr","Location ERR:" + amapLocation.getAMapException().getErrorCode());
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mAMapLocationManager == null) {
            mAMapLocationManager = LocationManagerProxy.getInstance(this);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用removeUpdates()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用destroy()方法
            // 其中如果间隔时间为-1，则定位只定一次
            // 在单次定位情况下，定位无论成功与否，都无需调用removeUpdates()方法移除请求，定位sdk内部会移除
            mAMapLocationManager.requestLocationData(
                    LocationProviderProxy.AMapNetwork, -1, 10, this);
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        //mListener = null;
        if (mAMapLocationManager != null) {
            mAMapLocationManager.removeUpdates(this);
            mAMapLocationManager.destroy();
        }
        mAMapLocationManager = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //Log.e("M_SERVICE", "onBind");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //Log.e("M_SERVICE", "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        //Log.e("M_SERVICE", "onDestroy");
        super.onDestroy();
    }

//    /**
//     * 定时任务执行类
//     */
//    class TimerGetLongitudeAndLatitude extends TimerTask {
//        @Override
//        public void run() {
//            Message message = new Message();
//            message.what = 0x10;
//            handler.sendMessage(message);
//        }
//    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x11:
                    if (controlIDList.size() > 0) {
                        sendLocationInfo(location);
                    }
                    break;
            }
        }
    };

    public class TimerServiceBinder extends Binder {
        //返回本地服务
        public TimerService getService() {
            return TimerService.this;
        }
    }

    /**
     * 查询订单数据
     */
    private void getTimerOrder() {
        String sessionUUID = getSessionUUID();
        if(sessionUUID == null || sessionUUID.equals("")) {
            return;
        }
        String url = Constant.ENTRANCE_PREFIX + "appQueryOrderList.json?sessionUuid="+sessionUUID+"&page="+1+"&rows="+10;
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                controlIDList.clear();
                controlStatusList.clear();
                try {
                    jsonObject = new JSONObject(response);
                    if (!jsonObject.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        //Toast.makeText(getApplicationContext(), "定时器订单列表数据获取失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    jsonArray = jsonObject.getJSONArray("rows");
                    if (controlIDList != null) {
                        controlIDList.clear();
                    }
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject JSONOrder = jsonArray.getJSONObject(i);
                        int controlStatus = JSONOrder.getInt("controlStatus");
                        if (controlStatus > 17 && controlStatus < 99) {
                            controlIDList.add(JSONOrder.getInt("ID"));
                            controlStatusList.add(controlStatus);
                        }
                    }
                    Message message = new Message();
                    message.what = 0x11;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 发送定位信息
     * @param amapLocation
     */
    private void sendLocationInfo(AMapLocation amapLocation) {
        String url = ""; //访问地址
        // String accountId= common.getStringByKey("ACCOUNT_ID"); //登录用户ID
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID); //会话ID
        String tel = common.getStringByKey("user_name"); //电话号码
        String provider = amapLocation.getProvider(); //定位类型
        double longitude = amapLocation.getLongitude(); //经度
        double latitude = amapLocation.getLatitude(); //纬度
        float speed = 0f;
        float direction = 0f;



        if(provider.equals("lbs")) { //网格定位
            for(int i = 0; i < controlIDList.size(); i++) {
                url = Constant.ENTRANCE_PREFIX + "appRecordTrackInfo.json?sessionUuid="+sessionUuid+"&lng="+longitude+"&lat="+latitude+"&controlId="+controlIDList.get(i)+"&tel="+tel
                +"&controlStatus="+controlStatusList.get(i);
                OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
                    @Override
                    public void onError(Request request, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonObject;
                        JSONArray jsonArray;
                        try {
                            jsonObject = new JSONObject(response);
                            if (!jsonObject.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                                //Toast.makeText(getApplicationContext(), "lib定位数据异常", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            //Toast.makeText(getApplicationContext(), "发送经纬度", Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } else if(provider.equals("gps")) { //定位
            speed = amapLocation.getSpeed(); //速度
            direction = amapLocation.getBearing(); //定位方向

            for(int i = 0; i < controlIDList.size(); i++) {
                url = Constant.ENTRANCE_PREFIX + "appRecordTrackInfo.json?lng="+longitude+"&lat="+latitude+"&controlId="
                        +controlIDList.get(i)+"&tel="+tel+"&speed="+speed+"&direction="+direction+"&controlStatus="+controlStatusList.get(i);
                OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
                    @Override
                    public void onError(Request request, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonObject;
                        JSONArray jsonArray;
                        try {
                            jsonObject = new JSONObject(response);
                            if (!jsonObject.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                                Toast.makeText(getApplicationContext(), "gps定位数据异常", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        }
    }

    /**
     * 启动定时器
     */
//    public void startTimer() {
//        timer = new Timer();
//        timer.schedule(new TimerGetLongitudeAndLatitude(), 0, PERIOD); //每隔十五秒执行一次
//    }

    /**
     * 停止定时器
     */
//    public void stopTimer() {
//        timer.cancel();
//    }
    /**
     * 再次激活定位
     * @param listener
     */
    public void reActivate(OnLocationChangedListener listener) {
        activate(listener);
    }

    /**
     * 获取sessionid
     * @return
     */
    private String getSessionUUID() {
        return common.getStringByKey(Constant.SESSION_UUID);
    }

    /**
     * 检查是否插入经纬度
     */
//    private void isInsertLA() {
//        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID); //会话ID
//        String url = Constant.ENTRANCE_PREFIX + "appJudgeLatestStatus.json?sessionUuid="+sessionUuid+"&controlId="+controlId;
//        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
//            JSONObject jsonObject;
//            JSONArray jsonArray;
//            @Override
//            public void onError(Request request, Exception e) {
//
//            }
//
//            @Override
//            public void onResponse(String response) {
//                try {
//                    jsonObject = new JSONObject(response);
//                    if (!jsonObject.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
//                        //Toast.makeText(getApplicationContext(), "lib定位数据异常", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//
//                    jsonArray = jsonObject.getJSONArray("rows");
//                    boolean isInsert = jsonArray.getBoolean(0); //是否有插入
//
//                    if(!isInsert) {
//                        Message message = new Message();
//                        message.what = 0x12;
//                        handler.sendMessage(message);
//                    }
//
//                    //Toast.makeText(getApplicationContext(), "发送经纬度", Toast.LENGTH_SHORT).show();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }
}
