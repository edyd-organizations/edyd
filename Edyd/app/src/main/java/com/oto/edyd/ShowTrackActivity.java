package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.LatLngBounds;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.PolylineOptions;
import com.amap.api.maps2d.overlay.DrivingRouteOverlay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.oto.edyd.model.TrackBean;
import com.oto.edyd.model.TrackLineBean;
import com.oto.edyd.model.TrackPointBean;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liubaozhong on 2015/12/1.
 * 显示地图轨迹。
 */
public class ShowTrackActivity extends Activity implements AMap.OnMarkerClickListener, AMap.InfoWindowAdapter, AMap.OnInfoWindowClickListener, AMap.OnMapLoadedListener {
    private static final int SHOW_TRACK = 1;//显示轨迹
    private static final int SHOW_POLYLINE = 0;//显示折线

    private Context mActivity;
    private TrackBean bean;
    private String sessionUuid;
    //声明变量
    private MapView mapView;
    private AMap aMap;
    private TrackLineBean tlb;
    private ArrayList<LatLng> pos;
    private CusProgressDialog loadingDialog; //页面切换过度
    //    private CheckBox cb_switchTrack;
    private RouteSearch routeSearch;

    private LatLonPoint startPoint;
    private LatLonPoint endPoint;

    private DriveRouteResult driveRouteResult;
    private TextView tv_traffic_detail;
    private LinearLayout ll_switch_track;//轨迹图和折线图的切换按钮
    private boolean isTrack = false;//表示现在地图上显示的是否是轨迹

    private boolean isShippingEndPoint = false;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            aMap.clear();
            switch (msg.what) {
                case 0x12:
                    if (tlb.getReceiverLat() > 0 && tlb.getSenderLat() > 0 && tlb.getReceiverLng() > 0 && tlb.getSenderLng() > 0) {

                        LatLng senderPoint = new LatLng(tlb.getSenderLat(), tlb.getSenderLng());
                        //发货人图标
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.deliveryside));
                        markerOptions.position(senderPoint);
                        markerOptions.title("发货方公司").draggable(true).anchor(0.5f, 1.0f);
                        Marker marker = aMap.addMarker(markerOptions);
//                        marker.showInfoWindow();

                        //收货人终点坐标
                        LatLng receiverPoint = new LatLng(tlb.getReceiverLat(), tlb.getReceiverLng());
                        //收货人图标
                        MarkerOptions marOptions = new MarkerOptions();
                        marOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.receivingparty));
                        marOptions.position(receiverPoint);
                        marOptions.title("收货方公司").draggable(true).anchor(0.5f, 1.0f);
                        Marker receiverMarker = aMap.addMarker(marOptions);
//                        receiverMarker.showInfoWindow();
                    }
                    //画轨迹线
                    PolylineOptions line = new PolylineOptions();
                    ArrayList<TrackPointBean> list = tlb.getTraceInfo();
                    pos = new ArrayList<LatLng>();
                    if (list.size() == 0) {
                        Common.showToast(mActivity, "暂时没有轨迹");
                        dissmissProgressDialog();
                    } else {
                        for (int i = 0; i < list.size(); i++) {
                            TrackPointBean point = list.get(i);
                            LatLng latLng = new LatLng(point.getLat(), point.getLng());
                            pos.add(latLng);
                            if (!TextUtils.isEmpty(point.getAddr())) {
                                addMarker(point, i, list);//添加所有的位置
                            }
                        }
                        line.addAll(pos);
                        line.color(Color.RED);
                        line.width(10);
                        aMap.addPolyline(line);

                    }
                    onMapLoaded();
                    if ((Integer) msg.obj == SHOW_TRACK) {
                        isTrack = true;
                        tv_track_name.setText("折线");
                        if (startPoint != null && endPoint != null) {
                            //汽车未走完的规划路径
                            calculateDriveRoute(startPoint, endPoint);
                        } else {
                            //有一个等于空
                            if (isShippingEndPoint) {
                                Common.showToast(mActivity, "发货方终点为空");
                            } else {
                                Common.showToast(mActivity, "收货方终点为空");
                            }

                        }
                    } else {
                        isTrack = false;
                        tv_track_name.setText("轨迹");
                        dissmissProgressDialog();
                        tv_traffic_detail.setVisibility(View.GONE);
                        tv_traff_time.setVisibility(View.GONE);
                    }

                    break;
            }
        }
    };
    private TextView tv_track_name;
    private TextView tv_traff_time;


    // 计算驾车路线
    private void calculateDriveRoute(LatLonPoint startPoint, LatLonPoint endPoint) {
        RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                startPoint, endPoint);
        // 第一个参数表示路径规划的起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，
        // 第四个参数表示避让区域，第五个参数表示避让道路
        RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, RouteSearch.DrivingDefault,
                null, null, "");
        routeSearch.calculateDriveRouteAsyn(query);// 异步路径规划驾车模式查询
    }

    // 计算驾车路线
//    private void calculateDriveRoute() {
//        boolean isSuccess = mAMapNavi.calculateDriveRoute(mStartPoints, mEndPoints, null, AMapNavi.DrivingDefault);
//        if (!isSuccess) {
//            Toast.makeText(this, "路线计算失败,检查参数情况", Toast.LENGTH_SHORT).show();
//            dissmissProgressDialog();
//        }
//    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.track_map);
        mActivity = this;
        //高德地图
        mapView = (MapView) findViewById(R.id.track_mapview);
        mapView.onCreate(savedInstanceState);// 必须要写
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
        }
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        bean = (TrackBean) bundle.getSerializable("detailBean");
        if (bean != null) {
            initFields(); //初始化数据
            getInfo(SHOW_POLYLINE);
        }
    }

    /**
     * 添加标记到地图
     *
     * @param point
     * @param index 表示第几个坐标
     */

    private void addMarker(TrackPointBean point, int index, ArrayList<TrackPointBean> list) {
        MarkerOptions markerOptions = new MarkerOptions();
        if (index == 0) {
            //如果是第一个坐标
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.getup));
        } else if (index == (list.size() - 1)) {
            //如果是最后一个坐标
            if (!"收货完成".equals(list.get(list.size() - 1).getControlStatus())) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.car));
                //添加起点坐标
                startPoint = new LatLonPoint(point.getLat(), point.getLng());
                if (point.getControlStatusInt() >= 30) {
                    //终点是收货地
                    if (tlb.getReceiverLat() > 0 && tlb.getReceiverLng() > 0) {
                        endPoint = new LatLonPoint(tlb.getReceiverLat(), tlb.getReceiverLng());
                    }
                    isShippingEndPoint = false;
                } else {
                    //终点是发货地
                    if (tlb.getSenderLat() > 0 && tlb.getSenderLng() > 0) {
                        endPoint = new LatLonPoint(tlb.getSenderLat(), tlb.getSenderLng());
                    }
                    isShippingEndPoint = true;
                }
            } else {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ending));
            }
        } else {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.abit));
        }
        markerOptions.position(new LatLng(point.getLat(), point.getLng()));
        markerOptions.title("车辆信息").draggable(true).anchor(0.5f, 1.0f);
        Marker marker = aMap.addMarker(markerOptions);

        marker.setObject(point);
//        marker.showInfoWindow();

    }


    private void setUpMap() {
        aMap.setMapType(AMap.MAP_TYPE_NORMAL); // 矢量地图模式
        aMap.setOnMarkerClickListener(this);// 设置点击marker事件监听器
//        aMap.setOnMapLoadedListener(this);// 设置amap加载成功事件监听器
        aMap.setMyLocationEnabled(true);//默认地图可以自动定位
        aMap.setOnInfoWindowClickListener(this);// 设置点击infoWindow事件监听器
        aMap.setInfoWindowAdapter(this);// 设置自定义InfoWindow样式
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

    private void getInfo(final int trackType) {
        showProgressDialog();

        long primaryId = bean.getPrimaryId();
        String url;
        if (trackType == SHOW_TRACK) {
            //轨迹
            url = Constant.ENTRANCE_PREFIX_v1 + "getRealMapLineTest.json?sessionUuid="
                    + sessionUuid + "&primaryId=" + primaryId;
        } else {
            //折线
            url = Constant.ENTRANCE_PREFIX + "getTruckPosition.json?sessionUuid="
                    + sessionUuid + "&primaryId=" + primaryId;
        }
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                Toast.makeText(getApplicationContext(), "获取信息失败", Toast.LENGTH_SHORT).show();
                dissmissProgressDialog();
                //请求失败checkbox返回原来的状态
//                cb_switchTrack.setChecked(!cb_switchTrack.isChecked());
            }

            @Override
            public void onResponse(String response) {
//                Common.printErrLog("轨迹地图" + response);
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    if (!jsonObject.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(getApplicationContext(), "返回信息失败", Toast.LENGTH_SHORT).show();
                        dissmissProgressDialog();
                        //请求失败checkbox返回原来的状态
//                        cb_switchTrack.setChecked(!cb_switchTrack.isChecked());
                        return;
                    }
                    jsonArray = jsonObject.getJSONArray("rows");
                    JSONObject obj = (JSONObject) jsonArray.get(0);
                    String objStr = obj.toString();
                    tlb = Common.readJsonToCommandObject(objStr);


                    Message message = new Message();
                    message.what = 0x12;
                    message.obj = (Integer) trackType;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void back(View view) {
        finish();
    }

    private void initFields() {

        tv_track_name = (TextView) findViewById(R.id.tv_track_name);
        tv_traff_time = (TextView) findViewById(R.id.tv_traff_time);
        tv_traffic_detail = (TextView) findViewById(R.id.tv_traff_detail);
        final Common common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        routeSearch = new RouteSearch(this);
        routeSearch.setRouteSearchListener(new RouteSearch.OnRouteSearchListener() {
            @Override
            public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

            }

            @Override
            public void onDriveRouteSearched(DriveRouteResult result, int rCode) {
                if (rCode == 0) {
                    if (result != null && result.getPaths() != null
                            && result.getPaths().size() > 0) {
                        driveRouteResult = result;
                        DrivePath drivePath = driveRouteResult.getPaths().get(0);
                        List<DriveStep> setps = drivePath.getSteps();
                        List<LatLng> points = new ArrayList<LatLng>();
                        for (DriveStep step : setps) {
                            for (LatLonPoint po : step.getPolyline()) {
                                points.add(new LatLng(po.getLatitude(), po.getLongitude()));
                            }
                        }
                        //画轨迹线
                        PolylineOptions line = new PolylineOptions();
                        line.addAll(points);
                        line.color(Color.BLUE);
                        line.width(10);
                        line.setDottedLine(true);
                        aMap.addPolyline(line);

//                        aMap.clear();// 清理地图上的所有覆盖物
//                        DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
//                                ShowTrackActivity.this, aMap, drivePath, driveRouteResult.getStartPos(),
//                                driveRouteResult.getTargetPos());

//                        drivingRouteOverlay.removeFromMap();
//                        drivingRouteOverlay.addToMap();
//                        drivingRouteOverlay.setNodeIconVisibility(false);

                        dissmissProgressDialog();
                        tv_traffic_detail.setVisibility(View.VISIBLE);
                        String strDis = drivePath.getDistance() / 1000 + "公里；";//返回驾车距离，单位米。
                        //提示距离
                        if (isShippingEndPoint) {
                            tv_traffic_detail.setText("距离发货地大约：" + strDis);
                        } else {
                            tv_traffic_detail.setText("距离收货地大约：" + strDis);
                        }

                        double minTime = drivePath.getDuration() / 60;//返回驾车预计时间，单位秒
                        tv_traff_time.setVisibility(View.VISIBLE);

                        if (minTime < 60) {
                            tv_traff_time.setText("大约需要：" + minTime + "分钟");
                        } else {
                            tv_traff_time.setText("大约需要：" + (int) minTime / 60 + "小时" + minTime % 60 + "分钟");
                        }
//                        drivingRouteOverlay.zoomToSpan();
                    } else {
                        common.showToast(mActivity, "对不起，没有搜索到相关数据！");
                    }
                } else if (rCode == 27) {
                    common.showToast(mActivity, "搜索失败,请检查网络连接！");
                } else if (rCode == 32) {
                    common.showToast(mActivity, "key验证无效！");
                } else {
                    common.showToast(mActivity, "未知错误，请稍后重试!错误码为"
                            + rCode);
                }
            }

            @Override
            public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

            }
        });

//        cb_switchTrack = (CheckBox) findViewById(R.id.cb_switchTrack);
//        cb_switchTrack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (((CheckBox) v).isChecked()) {
//                    //选中就用轨迹图
//                    getInfo(SHOW_TRACK);
//                } else {
//                    //未选中折现图
//                    getInfo(SHOW_POLYLINE);
//                }
//            }
//        });
        ll_switch_track = (LinearLayout) findViewById(R.id.ll_switch_track);
        ll_switch_track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTrack) {
                    //切换成折线图
                    getInfo(SHOW_POLYLINE);
                } else {
                    //切换成轨迹图
                    getInfo(SHOW_TRACK);
                }
            }
        });

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
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }


    private View render(Marker marker) {
        View infoWindow = View.inflate(mActivity, R.layout.track_info_windom, null);
        TrackPointBean trackPointBean = (TrackPointBean) marker.getObject();
        TextView Scheduling = ((TextView) infoWindow.findViewById(R.id.Scheduling));//调度单号
        Scheduling.setText(tlb.getControlNum());
        TextView carNumber = (TextView) infoWindow.findViewById(R.id.carNumber);//车牌号
        carNumber.setText(tlb.getTrunckNum());
        TextView driver = (TextView) infoWindow.findViewById(R.id.driver);//司机
        driver.setText(tlb.getDriverName());
        TextView phone = (TextView) infoWindow.findViewById(R.id.phone);//电话
        phone.setText(tlb.getDriverTel());
        TextView state = (TextView) infoWindow.findViewById(R.id.state);//状态
        state.setText(trackPointBean.getControlStatus());
        TextView time = (TextView) infoWindow.findViewById(R.id.time);//时间
        time.setText(trackPointBean.getOperTime());
        TextView address = (TextView) infoWindow.findViewById(R.id.address);//地址
        address.setText(trackPointBean.getAddr());
        return infoWindow;
    }


    public boolean onMarkerClick(Marker marker) {
//            render(marker);
        return false;
    }

    /**
     * 自定义信息窗口
     *
     * @param marker
     * @return
     */
    @Override
    public View getInfoWindow(Marker marker) {
        View view;
        if ("车辆信息".equals(marker.getTitle())) {
            //轨迹普通的点窗口
            view = render(marker);
        } else {
            //收发点窗口
            view = senRecWindom(marker);
        }
        return view;
    }

    private View senRecWindom(Marker marker) {
        View infoWindow = View.inflate(mActivity, R.layout.track_info_windom_receiver_sender, null);
        TextView tv_companyName = (TextView) infoWindow.findViewById(R.id.tv_companyName);//公司
        TextView tv_ContactPerson = (TextView) infoWindow.findViewById(R.id.tv_ContactPerson);//联系人
        TextView tv_tel = (TextView) infoWindow.findViewById(R.id.tv_tel);//电话
        TextView tv_address = (TextView) infoWindow.findViewById(R.id.tv_address);//地址
        if ("发货方公司".equals(marker.getTitle())) {
            //发货方公司
            tv_companyName.setText(tlb.getSenderName());
            tv_ContactPerson.setText(tlb.getSenderContactPerson());
            tv_tel.setText(tlb.getSenderContactTel());
            tv_address.setText(tlb.getSenderAddr());
        } else {
            //收货方公司
            tv_companyName.setText(tlb.getReceiverName());
            tv_ContactPerson.setText(tlb.getReceiverContactPerson());
            tv_tel.setText(tlb.getReceiverContactTel());
            tv_address.setText(tlb.getReceiverAddr());
        }

        return infoWindow;

    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        marker.hideInfoWindow();
    }


    public void onMapLoaded() {

        LatLngBounds.Builder buidler = new LatLngBounds.Builder();
        if (tlb != null && tlb.getReceiverLat() != 0 && tlb.getSenderLat() != 0
                && tlb.getReceiverLng() != 0 && tlb.getSenderLng() != 0) {
            buidler.include(new LatLng(tlb.getSenderLat(), tlb.getSenderLng()));
            buidler.include(new LatLng(tlb.getReceiverLat(), tlb.getReceiverLng()));
        }
        if (pos != null && pos.size() != 0) {
            for (LatLng po : pos) {
                buidler.include(po);
            }
            LatLngBounds bounds = buidler.build();
            aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10));
        }

    }
}