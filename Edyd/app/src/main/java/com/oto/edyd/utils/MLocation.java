package com.oto.edyd.utils;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yql on 2015/10/27.
 */
public class MLocation  implements LocationSource, AMapLocationListener {

    private Context context; //上下文
    private AMap aMap;
    private MapView mapView;
    private LocationManagerProxy mAMapLocationManager;
    private AMapLocation location;
    private Common common;
    private String controlId; //订单号
    private String controlStatus; //订单状态

    public MLocation(Context context, Common common, String controlId, String controlStatus) {
        this.context = context;
        this.common = common;
        this.controlId = controlId;
        this.controlStatus = controlStatus;
        init();
    }

    /**
     * 启动定位
     */
/*    public void startLocation() {
        init();
    }*/

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        location = amapLocation;
        int errorCode = location.getAMapException().getErrorCode();
        if (amapLocation != null && errorCode == 0) {
            sendEndStatusLocationInfo(amapLocation);
            deactivate();
        } else {
            Log.e("AmapErr", "Location ERR:" + amapLocation.getAMapException().getErrorCode());
            if(errorCode == 33) {
                Common.showToastlong(context, "应用无定位权限，请开启");
            }
//            else{
//                Common.showToastlong(context, "定位出现异常");
//            }
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

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        if (mAMapLocationManager == null) {
            mAMapLocationManager = LocationManagerProxy.getInstance(context);
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
        if (mAMapLocationManager != null) {
            mAMapLocationManager.removeUpdates(this);
            mAMapLocationManager.destroy();
        }
        mAMapLocationManager = null;
    }


    /**
     * 初始化AMap对象
     */
    private void init() {
        mapView = new MapView(context);
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


    private void sendEndStatusLocationInfo(AMapLocation amapLocation) {

        String url = ""; //访问地址
        // String accountId= common.getStringByKey("ACCOUNT_ID"); //登录用户ID
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID); //会话ID
        String tel = common.getStringByKey("user_name"); //电话号码
        String provider = amapLocation.getProvider(); //定位类型
        double longitude = amapLocation.getLongitude(); //经度
        double latitude = amapLocation.getLatitude(); //纬度
        float speed = 0f;
        float direction = 0f;

        url = Constant.ENTRANCE_PREFIX + "appRecordTrackInfo.json?sessionUuid="+sessionUuid+"&lng="+longitude+"&lat="+latitude+"&controlId="+controlId+"&tel="+tel
                +"&controlStatus="+ controlStatus;
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
    /**
     * 获取sessionid
     * @return
     */
    private String getSessionUUID() {
        return common.getStringByKey(Constant.SESSION_UUID);
    }
}
