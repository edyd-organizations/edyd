package com.oto.edyd;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;

/**
 * Created by yql on 2015/10/8.
 */
public class LocationActivity extends Activity implements LocationSource, AMapLocationListener {

    private AMap aMap;
    private MapView mapView;
    private OnLocationChangedListener mListener;
    private LocationManagerProxy mAMapLocationManager;
    private Button reActivateLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location);
        mapView = new MapView(getApplicationContext());
        init();
        reActivateLocation = (Button) findViewById(R.id.re_activate_location);
        reActivateLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reActivate(mListener);
            }
        });
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
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
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        deactivate();
    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null && amapLocation.getAMapException().getErrorCode() == 0) {
                //mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
                sendLocationInfo(amapLocation);
                deactivate();
            } else {
                //Log.e("AmapErr","Location ERR:" + amapLocation.getAMapException().getErrorCode());
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
     * 再次激活定位
     * @param listener
     */
    public void reActivate(OnLocationChangedListener listener) {
        activate(listener);
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
    /**
     * 发送定位信息
     * @param amapLocation
     */
    private void sendLocationInfo(AMapLocation amapLocation) {
        String provider = amapLocation.getProvider(); //定位类型
        double longitude = amapLocation.getLongitude(); //经度
        double latitude = amapLocation.getLatitude(); //纬度
        float speed = 0f;
        float bearing = 0f;
        if(provider.equals("lbs")) { //网格定位

        } else if(provider.equals("gps")) { //定位
            speed = amapLocation.getSpeed(); //速度
            bearing = amapLocation.getBearing(); //定位方向
        }
    }
}
