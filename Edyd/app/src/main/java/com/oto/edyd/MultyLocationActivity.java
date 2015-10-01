package com.oto.edyd;

import android.app.Activity;
import android.graphics.Camera;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.Window;
import android.widget.RadioGroup;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.oto.edyd.utils.Constant;

import java.util.Random;

/**
 * Created by yql on 2015/9/6.
 */
public class MultyLocationActivity extends Activity implements LocationSource, AMapLocationListener, RadioGroup.OnCheckedChangeListener {

    private AMap aMap; //操作地图的工具类
    private MapView mapView; //地图控件，用于地图显示
    private OnLocationChangedListener mListener;
    private LocationManagerProxy mAMapLocationManager;
    private RadioGroup mGPSModeGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //不显示程序标题栏
        setContentView(R.layout.multy_location);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState); //此方法必须重写
        init();
    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        //aMap.clear();
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null&& amapLocation.getAMapException().getErrorCode() == 0) {
                //mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
                //重新绘制覆盖物
                LatLng latLng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
                // 定位成功后把地图移动到当前可视区域内
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
                aMap.addMarker(new MarkerOptions().position(latLng)
                        .anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory
                        .fromResource(R.mipmap.location_marker)));
                // 自定义定位成功后绘制圆形
                aMap.addCircle(new CircleOptions().center(latLng).radius(50)
                        .fillColor(Color.TRANSPARENT).strokeColor(Color.BLUE)
                        .strokeWidth(0));
            } else {
                Log.e("AmapErr", "Location ERR:" + amapLocation.getAMapException().getErrorCode());
            }
        }
    }
    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        deactivate();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
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
            mAMapLocationManager.requestLocationData(LocationProviderProxy.AMapNetwork, 6 * 1000, 10, this);
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mAMapLocationManager != null) {
            mAMapLocationManager.removeUpdates(this);
            mAMapLocationManager.destroy();
        }
        mAMapLocationManager = null;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    /**
     * 初始化
     */
    private void init() {
        if(aMap == null) {
            aMap = mapView.getMap();
        }
        setUpMap();
    }

    /**
     * 设置一些amap属性
     */
    private void setUpMap() {
//        //定位、移动到地图中心点，跟踪并根据面向方向旋转地图。
//        aMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_ROTATE);
//
//        // 自定义系统定位蓝点
//        MyLocationStyle myLocationStyle = new MyLocationStyle();// 自定义定位蓝点图标
//        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.location_marker));
//        myLocationStyle.strokeColor(Color.BLACK); // 自定义精度范围的圆形边框颜色
//        myLocationStyle.strokeWidth(1); //自定义精度范围的圆形边框宽度
//        aMap.setMyLocationStyle(myLocationStyle);// 将自定义的 myLocationStyle 对象添加到地图上
//        mAMapLocationManager = LocationManagerProxy.getInstance(MultyLocationActivity.this); // 构造 LocationManagerProxy 对象
//
//        aMap.setLocationSource(this);// 设置定位监听
//        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
//        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
//        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
//       // aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Constant.XIAMEN, 20)); //为了设置默认打开缩放比例


        aMap.setMapType(AMap.MAP_TYPE_NORMAL); // 矢量地图模式

        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_ROTATE); // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种

        // 自定义系统定位蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();// 自定义定位蓝点图标
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.location_marker));
        myLocationStyle.strokeColor(Color.TRANSPARENT); // 自定义精度范围的圆形边框颜色
       // myLocationStyle.strokeWidth(0); //自定义精度范围的圆形边框宽度
        //myLocationStyle.radiusFillColor(Color.TRANSPARENT);
        aMap.setMyLocationStyle(myLocationStyle);// 将自定义的 myLocationStyle 对象添加到地图上

        aMap.moveCamera(CameraUpdateFactory.zoomTo(19)); //缩放级别设置
        aMap.clear();


    }
}
