package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.RouteOverLay;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.TTSController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liubaozhong on 2015/12/18.
 * 百宝箱导航
 */

public class BoxGPSActivity extends Activity implements PoiSearch.OnPoiSearchListener, AMapNaviListener {

    private static final int IS_END = 1;//结束点
    private static final int IS_START = 2;//开始点
    private boolean isSecond = false;//是否是第二次查询

    // --------------View基本控件---------------------

    private ImageView iv_reverse;
    private AutoCompleteTextView et_mStartPoint; // 起点输入
    private AutoCompleteTextView et_mEndPoint; // 终点输入
    private TextView tv_route; // 路径规划
    private MapView mMapView; // 地图控件
    private TextView tv_navi; // 导航按钮
    private Context mActivity;
    // 规划线路
    private RouteOverLay mRouteOverLay;
    // 地图和导航核心逻辑类
    private AMap mAmap;
    private AMapNavi mAmapNavi;
    private Marker mStartMarker;
    private Marker mEndMarker;
    private Marker mGPSMarker;

    private int mNaviMethod;
    private static final int ROUTE_METHOD = 1;// 执行计算线路操作

    private int mStartPointMethod = BY_MY_POSITION;
    private static final int BY_MY_POSITION = 0;// 以我的位置作为起点
    private static final int POI_SEARCH_POSITION = 1;// 关键字搜索
    //定位
    private LocationManagerProxy mLocationManger;
    private boolean mIsGetGPS = false;// 记录GPS定位是否成功

    // 记录起点、终点、途经点位置
    private NaviLatLng mStartPoint = new NaviLatLng();
    private NaviLatLng mEndPoint = new NaviLatLng();
    // 驾车路径规划起点，途经点，终点的list
    private List<NaviLatLng> mStartPoints = new ArrayList<NaviLatLng>();
    private List<NaviLatLng> mEndPoints = new ArrayList<NaviLatLng>();

    // 计算路的状态
    private final static int GPSNO = 0;// 使用我的位置进行计算、GPS定位还未成功状态
    private final static int CALCULATEERROR = 1;// 启动路径计算失败状态
    private final static int CALCULATESUCCESS = 2;// 启动路径计算成功状态

    private AMapLocationListener mLocationListener = new AMapLocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLocationChanged(AMapLocation location) {
            if (location != null && location.getAMapException().getErrorCode() == 0) {
                mIsGetGPS = true;
                mStartPoint = new NaviLatLng(location.getLatitude(), location.getLongitude());
//                mGPSMarker.setPosition(new LatLng(
//                        mStartPoint.getLatitude(), mStartPoint
//                        .getLongitude()));
                mStartPoints.clear();
                mStartPoints.add(mStartPoint);

                calculateRoute();

            } else {
                Common.showToastlong(mActivity, "定位异常,规划路径失败");
                dissmissProgressDialog();
            }
        }
    };
    private int currentPage;
    private PoiSearch.Query query;
    private PoiSearch poiSearch;
    private PoiResult poiResult;
    private String endPointStr;//结束编辑框的字符串
    private String startPointStr;//开始编辑框的字符串
    private AMapNaviListener mAmapNaviListener;

    private boolean mIsCalculateRouteSuccess = false;
    private CusProgressDialog loadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box_gps);
        initField();
        initView(savedInstanceState);
        initMapAndNavi();
//        firstOrientation();
    }

    private void firstOrientation() {

        mLocationManger = LocationManagerProxy.getInstance(this);
        //进行一次定位
        mLocationManger.requestLocationData(LocationProviderProxy.AMapNetwork, -1, 15, new AMapLocationListener() {
                    @Override
                    public void onLocationChanged(AMapLocation location) {
                        dissmissProgressDialog();
                        if (location != null && location.getAMapException().getErrorCode() == 0) {
                            NaviLatLng point = new NaviLatLng(location.getLatitude(), location.getLongitude());
                            mGPSMarker.setPosition(new LatLng(point.getLatitude(), point.getLongitude()));
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            mAmap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
                        } else {
                            Common.showToastlong(mActivity, "定位出现异常");
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
        );
        showProgressDialog();// 显示进度框
    }

    private void initField() {
        mActivity = this;
    }

    private void initView(Bundle savedInstanceState) {
        //初始化地图
        mMapView = (MapView) findViewById(R.id.navi_mapview);
        mMapView.onCreate(savedInstanceState);
        mAmap = mMapView.getMap();
        mRouteOverLay = new RouteOverLay(mAmap, null);

        iv_reverse = (ImageView) findViewById(R.id.iv_reverse);

        //起点文本框
        et_mStartPoint = (AutoCompleteTextView) findViewById(R.id.et_mStartPoint);
        et_mStartPoint.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newText = s.toString().trim();
                Inputtips inputTips = new Inputtips(mActivity,
                        new Inputtips.InputtipsListener() {

                            @Override
                            public void onGetInputtips(List<Tip> tipList, int rCode) {
                                if (rCode == 0) {// 正确返回
                                    List<String> listString = new ArrayList<String>();
                                    for (int i = 0; i < tipList.size(); i++) {
                                        listString.add(tipList.get(i).getName());
                                    }
                                    ArrayAdapter<String> aAdapter = new ArrayAdapter<String>(
                                            getApplicationContext(),
                                            R.layout.route_inputs, listString);
                                    et_mStartPoint.setAdapter(aAdapter);
                                    aAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                try {
                    // 第一个参数表示提示关键字，第二个参数默认代表全国，也可以为城市区号
                    inputTips.requestInputtips(newText, "");

                } catch (AMapException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et_mEndPoint = (AutoCompleteTextView) findViewById(R.id.et_mEndPoint);
        et_mEndPoint.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newText = s.toString().trim();
                Inputtips inputTips = new Inputtips(mActivity,
                        new Inputtips.InputtipsListener() {

                            @Override
                            public void onGetInputtips(List<Tip> tipList, int rCode) {
                                if (rCode == 0) {// 正确返回
                                    List<String> listString = new ArrayList<String>();
                                    for (int i = 0; i < tipList.size(); i++) {
                                        listString.add(tipList.get(i).getName());
                                    }
                                    ArrayAdapter<String> aAdapter = new ArrayAdapter<String>(
                                            getApplicationContext(),
                                            R.layout.route_inputs, listString);
                                    et_mEndPoint.setAdapter(aAdapter);
                                    aAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                try {
                    // 第一个参数表示提示关键字，第二个参数默认代表全国，也可以为城市区号
                    inputTips.requestInputtips(newText, "");

                } catch (AMapException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        tv_route = (TextView) findViewById(R.id.tv_route);
        tv_route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNaviMethod = ROUTE_METHOD;
                calculateRoute();
            }
        });
        tv_navi = (TextView) findViewById(R.id.tv_navi);
        tv_navi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsCalculateRouteSuccess) {
                    Intent intent = new Intent(mActivity, DriverGPSActivity.class);
                    startActivity(intent);
                } else {
                    Common.showToast(mActivity, "请先进行相对应的路径规划，再进行导航");
                }


            }
        });
    }

    private void calculateRoute() {
        //起终点字符串
        startPointStr = et_mStartPoint.getText().toString().trim();
        endPointStr = et_mEndPoint.getText().toString().trim();
        if (TextUtils.isEmpty(startPointStr)) {
            //他是用自己的位置当起点的
            mStartPointMethod = BY_MY_POSITION;
        } else {
            mStartPointMethod = POI_SEARCH_POSITION;
        }
        if (TextUtils.isEmpty(endPointStr)) {
            Common.showToast(mActivity, "终点不能为空");
            return;
        }
        if (mStartPointMethod == BY_MY_POSITION) {
            //起点为空
            if (!mIsGetGPS) {
                mLocationManger = LocationManagerProxy.getInstance(this);
                //进行一次定位
                mLocationManger.requestLocationData(LocationProviderProxy.AMapNetwork, -1, 15, mLocationListener);
                showProgressDialog();// 显示进度框
                return;
            }
            mIsGetGPS = false;
            startSearchQuery(IS_END);
        } else {
            //起点不为空
            startSearchQuery(IS_START);
        }

    }

    /**
     * 开始进行poi搜索
     */
    private void startSearchQuery(int isStartEnd) {
        showProgressDialog();// 显示进度框
        currentPage = 0;
        if (isStartEnd == IS_START) {
            // 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
            query = new PoiSearch.Query(startPointStr, "", "厦门");
        } else {
            // 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
            query = new PoiSearch.Query(endPointStr, "", "厦门");
        }
        query.setPageSize(10);// 设置每页最多返回多少条poiitem
        query.setPageNum(currentPage);// 设置查第一页

        poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
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
     * 对行车路线进行规划
     */
    private int calculateDriverRoute() {
        int driveMode = getDriveMode();
        int code = CALCULATEERROR;

        if (mAmapNavi.calculateDriveRoute(mStartPoints, mEndPoints, null,
                driveMode)) {
            code = CALCULATESUCCESS;
        } else {
            code = CALCULATEERROR;
        }
        return code;
    }

    /**
     * 根据选择，获取行车策略
     */
    private int getDriveMode() {
//        String strategyMethod = mStrategyText.getText().toString();
//        // 速度优先
//        if (mStrategyMethods[0].equals(strategyMethod)) {
//            return AMapNavi.DrivingDefault;
//        }
//        // 花费最少
//        else if (mStrategyMethods[1].equals(strategyMethod)) {
//            return AMapNavi.DrivingSaveMoney;
//
//        }
        // 距离最短
//        else if (mStrategyMethods[2].equals(strategyMethod)) {
        return AMapNavi.DrivingShortDistance;
//        }
//        // 不走高速
//        else if (mStrategyMethods[3].equals(strategyMethod)) {
//            return AMapNavi.DrivingNoExpressways;
//        }
//        // 时间最短且躲避拥堵
//        else if (mStrategyMethods[4].equals(strategyMethod)) {
//            return AMapNavi.DrivingFastestTime;
//        } else if (mStrategyMethods[5].equals(strategyMethod)) {
//            return AMapNavi.DrivingAvoidCongestion;
//        } else {
//            return AMapNavi.DrivingDefault;
//        }
    }

    public void back(View view) {
        finish();
    }

    /**
     * 初始化地图和导航相关内容
     */
    private void initMapAndNavi() {
        // 初始语音播报资源
        setVolumeControlStream(AudioManager.STREAM_MUSIC);// 设置声音控制
        //语音播报开始

        mAmapNavi = AMapNavi.getInstance(this);// 初始化导航引擎
        mAmapNavi.setAMapNaviListener(this);

        // 初始化Marker添加到地图
        mStartMarker = mAmap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(), R.mipmap.getup))));
        mEndMarker = mAmap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(), R.mipmap.ending))));
        mGPSMarker = mAmap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(),
                                R.mipmap.navi_box_marker))));

    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        loadingDialog.getLoadingDialog().dismiss();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    // 驾车导航
                    isSecond = false;
                    int driverIndex = calculateDriverRoute();
                    if (driverIndex == CALCULATEERROR) {
                        Common.showToast(mActivity, "路线计算失败,检查参数情况");
                        return;
                    } else if (driverIndex == GPSNO) {
                        Common.showToast(mActivity, "定位异常");
                        return;
                    }
                    // 显示路径规划的窗体
//                    showProgressDialog();
                    break;
                case 2:
                    isSecond = true;
                    startSearchQuery(IS_END);
                    break;
            }
        }
    };

    @Override
    public void onPoiSearched(PoiResult result, int rCode) {

        if (rCode == 0) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(query)) {// 是否是同一条
                    poiResult = result;
                    // 取得搜索到的poiitems有多少页
                    List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息

                    if (poiItems != null && poiItems.size() > 0) {
                        PoiItem location = poiItems.get(0);
                        //判断通过是否是用自己的位置当起点
                        if (mStartPointMethod == POI_SEARCH_POSITION) {
                            //不是用自己的位置当起点
                            if (isSecond) {
                                mEndPoint = new NaviLatLng(location.getLatLonPoint().getLatitude(),
                                        location.getLatLonPoint().getLongitude());
                                mEndPoints.clear();
                                mEndPoints.add(mEndPoint);
                                Message msg = Message.obtain();
                                msg.what = 1;
                                handler.sendMessage(msg);

                            } else {
                                mStartPoint = new NaviLatLng(location.getLatLonPoint().getLatitude(),
                                        location.getLatLonPoint().getLongitude());
                                mStartPoints.clear();
                                mStartPoints.add(mStartPoint);
                                Message msg = Message.obtain();
                                msg.what = 2;
                                handler.sendMessage(msg);
                            }
                        } else {
                            //用自己的位置当起点
                            mEndPoint = new NaviLatLng(location.getLatLonPoint().getLatitude(),
                                    location.getLatLonPoint().getLongitude());
                            mEndPoints.clear();
                            mEndPoints.add(mEndPoint);
                            Message msg = Message.obtain();
                            msg.what = 1;
                            handler.sendMessage(msg);

                        }

                    } else if (suggestionCities != null
                            && suggestionCities.size() > 0) {
                        Common.showToast(mActivity, "你输入的地址不完整");
                    } else {
                        Common.showToast(mActivity, "对不起，没有搜索到相关数据！");
                    }
                }
            } else {
                Common.showToast(mActivity, "对不起，没有搜索到相关数据！");
            }
        } else if (rCode == 27) {
            Common.showToast(mActivity, "搜索失败,请检查网络连接！");

        } else if (rCode == 32) {
            Common.showToast(mActivity, "key验证无效！");

        } else {
            Common.showToast(mActivity, "未知错误，请稍后重试!错误码为" + rCode);
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
        dissmissProgressDialog();
        Common.showToast(mActivity, "路径规划出错" + arg0);
        mIsCalculateRouteSuccess = false;
    }

    @Override
    public void onCalculateRouteSuccess() {
        dissmissProgressDialog();
        AMapNaviPath naviPath = mAmapNavi.getNaviPath();
        if (naviPath == null) {
            return;
        }
        // 获取路径规划线路，显示到地图上
        mRouteOverLay.setRouteInfo(naviPath);
        mRouteOverLay.addToMap();
        mRouteOverLay.zoomToSpan();
        mIsCalculateRouteSuccess = true;
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
    // ------------------生命周期重写函数---------------------------

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        // 以上两句必须重写
        // 以下两句逻辑是为了保证进入首页开启定位和加入导航回调
        AMapNavi.getInstance(this).setAMapNaviListener(this);

        TTSController.getInstance(this).startSpeaking();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        AMapNavi.getInstance(this)
                .removeAMapNaviListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        // 删除监听
        mAmapNavi.destroy();
        if (mLocationManger != null) {
            mLocationManger.destroy();
        }
    }

    @Override
    public void onNaviInfoUpdate(NaviInfo arg0) {

    }
}
