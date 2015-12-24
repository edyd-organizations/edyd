package com.oto.edyd;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.oto.edyd.utils.Common;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liubaozhong on 2015/12/18.
 * 百宝箱导航
 */

public class BoxGPSActivity extends Activity implements GeocodeSearch.OnGeocodeSearchListener, PoiSearch.OnPoiSearchListener {

    // --------------View基本控件---------------------
    private ImageView iv_reverse;// 模拟导航按钮
    private AutoCompleteTextView et_mStartPoint;// 起点输入
    private AutoCompleteTextView et_mEndPoint;// 终点输入
    private TextView tv_route;// 路径规划
    private MapView mMapView;// 地图控件
    private TextView tv_navi;// 导航按钮
    private Context mActivity;

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
    private static final int GEO_POSITION = 1;// 逆地址编码
    //定位
    private LocationManagerProxy mLocationManger;
    private boolean mIsGetGPS = false;// 记录GPS定位是否成功
    private boolean isGetEndPoint=false;//记录结果点是否请求成功

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
                mGPSMarker.setPosition(new LatLng(
                        mStartPoint.getLatitude(), mStartPoint
                        .getLongitude()));
                mStartPoints.clear();
                mStartPoints.add(mStartPoint);

                dissmissGPSProgressDialog();

                calculateRoute();

            } else {
                Common.showToast(mActivity, "定位出现异常");
            }
        }
    };
    private int currentPage;
    private PoiSearch.Query query;
    private PoiSearch poiSearch;
    private PoiResult poiResult;
    private String endPointStr;//结束编辑框的字符串
    private String startPointStr;//开始编辑框的字符串

//    private GeocodeSearch geocoderSearch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box_gps);
        initField();
        initView(savedInstanceState);
        initMapAndNavi();
    }

    private void initField() {
//        geocoderSearch = new GeocodeSearch(this);
//        geocoderSearch.setOnGeocodeSearchListener(this);
        mActivity = this;
    }

    private void initView(Bundle savedInstanceState) {
        //初始化地图
        mMapView = (MapView) findViewById(R.id.navi_mapview);
        mMapView.onCreate(savedInstanceState);
        mAmap = mMapView.getMap();

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
    }

    private void calculateRoute() {
        //起终点字符串
         startPointStr = et_mStartPoint.getText().toString().trim();
         endPointStr = et_mEndPoint.getText().toString().trim();
        if (TextUtils.isEmpty(endPointStr)) {
            Common.showToast(mActivity, "终点不能为空");
            return;
        }
        //定位
        if (mStartPointMethod == BY_MY_POSITION && !mIsGetGPS) {
            mLocationManger = LocationManagerProxy.getInstance(this);
            //进行一次定位
            mLocationManger.requestLocationData(LocationProviderProxy.AMapNetwork, -1, 15, mLocationListener);
            showGPSProgressDialog();
            return;
        }
        mIsGetGPS = false;

        startSearchQuery();

    }

    /**
     * 开始进行poi搜索
     */
    private void startSearchQuery() {
        showProgressDialog();// 显示进度框
        currentPage = 0;
        query = new PoiSearch.Query(endPointStr, "", "");// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
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
        if (mProgressDialog == null)
            mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setMessage("线路规划中");
        mProgressDialog.show();
    }

    /**
     * 对行车路线进行规划
     */
    private int calculateDriverRoute() {
//        int driveMode = getDriveMode();
        int code = CALCULATEERROR;

        if (mAmapNavi.calculateDriveRoute(mStartPoints, mEndPoints, null,
                AMapNavi.DrivingDefault)) {
            code = CALCULATESUCCESS;
        } else {
            code = CALCULATEERROR;
        }
        return code;
    }

    /**
     * 根据选择，获取行车策略
     */
//    private int getDriveMode() {
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
//            return AMapNavi.DrivingShortDistance;
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
//
//    }
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

    private ProgressDialog mProgressDialog;// 路径规划过程显示状态
    private ProgressDialog mGPSProgressDialog;// GPS过程显示状态

    /**
     * 隐藏进度框
     */
    private void dissmissGPSProgressDialog() {
        if (mGPSProgressDialog != null) {
            mGPSProgressDialog.dismiss();
        }
    }

    /**
     * 显示GPS进度框
     */
    private void showGPSProgressDialog() {
        if (mGPSProgressDialog == null)
            mGPSProgressDialog = new ProgressDialog(this);
        mGPSProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mGPSProgressDialog.setIndeterminate(false);
        mGPSProgressDialog.setCancelable(true);
        mGPSProgressDialog.setMessage("定位中...");
        mGPSProgressDialog.show();
    }


    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

    }

    /**
     * 地理编码，把地址转化成坐标；
     */
    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }
    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    // 驾车导航
                    int driverIndex = calculateDriverRoute();
                    if (driverIndex == CALCULATEERROR) {
                        Common.showToast(mActivity, "路线计算失败,检查参数情况");
                        return;
                    } else if (driverIndex == GPSNO) {
                        return;
                    }
                    // 显示路径规划的窗体
                    showProgressDialog();
                    break;
            }
        }
    };
    @Override
    public void onPoiSearched(PoiResult result, int rCode) {
        dissmissProgressDialog();// 隐藏对话框
        if (rCode == 0) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(query)) {// 是否是同一条
                    poiResult = result;
                    // 取得搜索到的poiitems有多少页
                    List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息

                    if (poiItems != null && poiItems.size() > 0) {
//                        mAmap.clear();// 清理之前的图标
//                        PoiOverlay poiOverlay = new PoiOverlay(mAmap, poiItems);
//                        poiOverlay.removeFromMap();
//                        poiOverlay.addToMap();
//                        poiOverlay.zoomToSpan();
                        PoiItem location = poiItems.get(0);
                        mEndPoint = new NaviLatLng(location.getLatLonPoint().getLatitude(),
                                location.getLatLonPoint().getLongitude());
                        mEndPoints.clear();
                        mEndPoints.add(mEndPoint);
                        //标签置为true。
//                        isGetEndPoint =true;
//                       doSearchQuery();
                        Message msg=Message.obtain();
                        msg.what=1;
                        handler.sendMessage(msg);

                    } else if (suggestionCities != null
                            && suggestionCities.size() > 0) {
                        Common.showToast(mActivity,"你输入的地址不完整");
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
}
