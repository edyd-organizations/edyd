package com.oto.edyd;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.RouteOverLay;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.OkHttpClientManager;
import com.oto.edyd.utils.TTSController;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by zfh and lbz on 2015/12/21.
 * 司机导航路径
 */
public class DriverGPSPathActivity extends Activity implements AMapNaviListener,
        AMapLocationListener {
    private static final int GET_END_POI = 1;
    private MapView mMapView;
    private AMap mAMap;
    private AMapNavi mAMapNavi;
    private boolean mIsCalculateRouteSuccess = false;
    // 规划线路
    private RouteOverLay mRouteOverLay;
    // 起点终点坐标
    private NaviLatLng mNaviStart;
    private NaviLatLng mNaviEnd;
    // 起点终点列表
    private ArrayList<NaviLatLng> mStartPoints = new ArrayList<NaviLatLng>();
    private ArrayList<NaviLatLng> mEndPoints = new ArrayList<NaviLatLng>();

    private ProgressDialog mGPSProgressDialog;// GPS过程显示状态
//    private boolean isGPS;
    private long primaryId;//主键
    private CusProgressDialog loadingDialog;
    private Activity mActivity;
    private String sessionUuid;
    private LocationManagerProxy mLocationManger;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case GET_END_POI:
                    //起点为空
                        mLocationManger = LocationManagerProxy.getInstance(mActivity);
                        //进行一次定位
                        mLocationManger.requestLocationData(LocationProviderProxy.AMapNetwork, -1, 15, DriverGPSPathActivity.this);
//                        showProgressDialog();// 显示进度框

                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__driverpath);
        initField(savedInstanceState);
        //请求终点坐标
        requestDate();
        setUpMap();
    }

    private void initField(Bundle savedInstanceState) {
        mActivity=this;
        Intent intent = getIntent();
        primaryId=intent.getLongExtra("PrimaryId", 1);
        Common common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        sessionUuid = common.getStringByKey(Constant.SESSION_UUID);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);// 设置声音控制
        mAMapNavi = AMapNavi.getInstance(this);
        mAMapNavi.setAMapNaviListener(this);
        mMapView = (MapView) findViewById(R.id.simple_route_map);
        mMapView.onCreate(savedInstanceState);
        if (mAMap == null) {
            mAMap = mMapView.getMap();
        }
        mRouteOverLay = new RouteOverLay(mAMap, null);
//        mAMap.setOnMapLoadedListener(this);
        mIsCalculateRouteSuccess = false;
    }

    /**
     * 显示进度框
     */

    private void showProgressDialog() {
        if (loadingDialog == null) {
            loadingDialog = new CusProgressDialog(mActivity, "正在获取数据...");
        }
        loadingDialog.getLoadingDialog().show();

    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        loadingDialog.getLoadingDialog().dismiss();
    }

    private void requestDate() {
        //第一次进来显示loading
        showProgressDialog();

        String url = Constant.ENTRANCE_PREFIX_v1 + "appQueryNaviPosition.json?sessionUuid="
                + sessionUuid + "&primaryId="+primaryId;
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                dissmissProgressDialog();
                Toast.makeText(getApplicationContext(), "获取信息失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    if (!jsonObject.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(getApplicationContext(), "返回信息失败", Toast.LENGTH_SHORT).show();
                        dissmissProgressDialog();
                        return;
                    }
                    jsonArray = jsonObject.getJSONArray("rows");

                    double endLag = jsonArray.getDouble(1);
                    double endLng = jsonArray.getDouble(0);
                    mNaviEnd = new NaviLatLng(endLag, endLng);
                    mEndPoints.clear();
                    mEndPoints.add(mNaviEnd);

                    handler.sendEmptyMessage(GET_END_POI);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }





    public void startGps(View view) {//开始导航

//        if (!isGPS) {
//            Common.showToast(this, "暂无数据，无法导航");
//            return;
//        }

        if (mIsCalculateRouteSuccess) {
            Intent intent = new Intent(mActivity, DriverGPSActivity.class);
            startActivity(intent);
        } else {
            Common.showToast(mActivity, "请先进行相对应的路径规划，再进行导航");
        }
//        if ((endLag != 0.0) && endLng != 0.0) {
//            Intent intent = new Intent(DriverGPSPathActivity.this, DriverGPSActivity.class);
//            startActivity(intent);
//        } else {
//            Common.showToast(this, "暂无数据，无法导航");
//            return;
//        }
    }


    // 计算驾车路线
    private void calculateDriveRoute() {
        boolean isSuccess = mAMapNavi.calculateDriveRoute(mStartPoints, mEndPoints, null, AMapNavi.DrivingDefault);
        if (!isSuccess) {
            Toast.makeText(this, "路线计算失败,检查参数情况", Toast.LENGTH_SHORT).show();
            dissmissProgressDialog();
        }
    }

    // --------------------导航监听回调事件-----------------------------
    @Override
    public void onArriveDestination() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onArrivedWayPoint(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCalculateRouteFailure(int arg0) {
        Common.showToast(mActivity, "路径规划出错" + arg0);
        mIsCalculateRouteSuccess = false;
        dissmissProgressDialog();
    }

    @Override
    public void onCalculateRouteSuccess() {
        dissmissProgressDialog();
        AMapNaviPath naviPath = mAMapNavi.getNaviPath();
        if (naviPath == null) {
            return;
        }
        // 获取路径规划线路，显示到地图上
        mRouteOverLay.setRouteInfo(naviPath);
        mRouteOverLay.addToMap();
        mIsCalculateRouteSuccess = true;
        mRouteOverLay.zoomToSpan();
        mAMap.setTrafficEnabled(true);
    }

    @Override
    public void onEndEmulatorNavi() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onGetNavigationText(int arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onGpsOpenStatus(boolean arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onInitNaviFailure() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onInitNaviSuccess() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onLocationChange(AMapNaviLocation arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onReCalculateRouteForYaw() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStartNavi(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTrafficStatusUpdate() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            Intent intent = new Intent(DriverGPSPathActivity.this, OrderOperateActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//            startActivity(intent);
//            finish();
//        }
        return super.onKeyDown(keyCode, event);
    }

//    /**
//     * 显示GPS进度框
//     */
//    private void showGPSProgressDialog() {
//        if (mGPSProgressDialog == null)
//            mGPSProgressDialog = new ProgressDialog(this);
//        mGPSProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        mGPSProgressDialog.setIndeterminate(false);
//        mGPSProgressDialog.setCancelable(true);
//        mGPSProgressDialog.setMessage("定位中...");
//        mGPSProgressDialog.show();
//    }
//
//    /**
//     * 隐藏进度框
//     */
//    private void dissmissGPSProgressDialog() {
//        if (mGPSProgressDialog != null) {
//            mGPSProgressDialog.dismiss();
//        }
//    }
    // ------------------生命周期重写函数---------------------------

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
//        deactivate();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        //删除导航监听
        AMapNavi.getInstance(this).removeAMapNaviListener(this);
        mAMapNavi.destroy();
        TTSController.getInstance(this).stopSpeaking();

    }

    // ------------------定位---------------------------

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
//        mAMap.setLocationSource(this);// 设置定位监听
        mAMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        mAMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        mAMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
        mAMap.clear();
    }

//    /**
//     * 激活定位
//     */
//    @Override
//    public void activate(OnLocationChangedListener listener) {
//        mListener = listener;
//        if (mAMapLocationManager == null) {
//            mAMapLocationManager = LocationManagerProxy.getInstance(this);
//            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
//            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用removeUpdates()方法来取消定位请求
//            // 在定位结束后，在合适的生命周期调用destroy()方法
//            // 其中如果间隔时间为-1，则定位只定一次
//            // 在单次定位情况下，定位无论成功与否，都无需调用removeUpdates()方法移除请求，定位sdk内部会移除
//            mAMapLocationManager.requestLocationData(LocationProviderProxy.AMapNetwork, -1, 1, this);
//            showGPSProgressDialog();
//        } else {
//            dissmissGPSProgressDialog();
//            Common.showToast(this, "定位异常");
//        }
//    }

//    /**
//     * 停止定位
//     */
//    @Override
//    public void deactivate() {
//        mListener = null;
//        if (mAMapLocationManager != null) {
//            mAMapLocationManager.removeUpdates(this);
//            mAMapLocationManager.destroy();
//
//        }
//        mAMapLocationManager = null;
//    }

    //定位成功后回调
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null && amapLocation.getAMapException().getErrorCode() == 0) {
//            isGPS = true;
            double latitude = amapLocation.getLatitude();
            double longitude = amapLocation.getLongitude();
            mNaviStart = new NaviLatLng(latitude, longitude);
            mStartPoints.clear();
            mStartPoints.add(mNaviStart);
//            dissmissGPSProgressDialog();
//            showGPSProgressDialog();
            calculateDriveRoute();

        } else {
            // Log.e("AmapErr", "Location ERR:" + amapLocation.getAMapException().getErrorCode());
            Common.showToast(this, "定位异常");
            dissmissProgressDialog();
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
}
