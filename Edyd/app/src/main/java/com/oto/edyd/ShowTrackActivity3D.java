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
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.RouteOverLay;
import com.amap.api.services.geocoder.GeocodeSearch;
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

/**
 * Created by liubaozhong on 2015/12/1.
 * 显示地图轨迹。
 */
public class ShowTrackActivity3D extends Activity implements AMap.OnMarkerClickListener, AMap.InfoWindowAdapter, AMap.OnInfoWindowClickListener, AMap.OnMapLoadedListener, AMapNaviListener {
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
    private AMapNavi mAMapNavi;

    // 起点终点坐标
    private NaviLatLng mNaviStart;
    private NaviLatLng mNaviEnd;
    // 起点终点列表
    private ArrayList<NaviLatLng> mStartPoints = new ArrayList<NaviLatLng>();
    private ArrayList<NaviLatLng> mEndPoints = new ArrayList<NaviLatLng>();

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
                        marker.showInfoWindow();

                        //收货人终点坐标
                        LatLng receiverPoint = new LatLng(tlb.getReceiverLat(), tlb.getReceiverLng());
                        mNaviEnd = new NaviLatLng(tlb.getReceiverLat(), tlb.getReceiverLng());
                        mEndPoints.clear();
                        mEndPoints.add(mNaviEnd);
                        //收货人图标
                        MarkerOptions marOptions = new MarkerOptions();
                        marOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.receivingparty));
                        marOptions.position(receiverPoint);
                        marOptions.title("收货方公司").draggable(true).anchor(0.5f, 1.0f);
                        Marker receiverMarker = aMap.addMarker(marOptions);
                        receiverMarker.showInfoWindow();
                    }
                    //画轨迹线
                    PolylineOptions line = new PolylineOptions();
                    ArrayList<TrackPointBean> list = tlb.getTraceInfo();
                    pos = new ArrayList<LatLng>();
                    if (list.size() == 0) {
                        Common.showToast(mActivity, "暂时没有轨迹");
                        dissmissProgressDialog();
                        return;
                    }
                    for (int i = 0; i < list.size(); i++) {
                        TrackPointBean point = list.get(i);
                        LatLng latLng = new LatLng(point.getLat(), point.getLng());
                        pos.add(latLng);
                        if (!TextUtils.isEmpty(point.getAddr())) {
                            addMarker(point, i, list);//添加所有的位置
                        }
                    }
//                    calculateDriveRoute();//规划路线

                    line.addAll(pos);
                    line.color(Color.RED);
                    line.width(15);
                    aMap.addPolyline(line);
                    onMapLoaded();
                    dissmissProgressDialog();
                    break;
            }
        }
    };
    private GeocodeSearch geocoderSearch;
    private RouteOverLay mRouteOverLay;


    // 计算驾车路线
    private void calculateDriveRoute() {
        boolean isSuccess = mAMapNavi.calculateDriveRoute(mStartPoints, mEndPoints, null, AMapNavi.DrivingDefault);
        if (!isSuccess) {
            Toast.makeText(this, "路线计算失败,检查参数情况", Toast.LENGTH_SHORT).show();
            dissmissProgressDialog();
        }
    }
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
                mNaviStart = new NaviLatLng(point.getLat(), point.getLng());
                mStartPoints.clear();
                mStartPoints.add(mNaviStart);
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
        marker.showInfoWindow();

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

    private void getInfo(final int isTrack) {
       showProgressDialog();

        long primaryId = bean.getPrimaryId();
        String url;
        if (isTrack==SHOW_TRACK) {
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
        Common common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        sessionUuid = common.getStringByKey(Constant.SESSION_UUID);

//        mAMapNavi = AMapNavi.getInstance(this);
//        mAMapNavi.setAMapNaviListener(this);
//        mRouteOverLay = new RouteOverLay(aMap, null);


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
        if (tlb != null && tlb.getReceiverLat() != 0 && tlb.getSenderLat() != 0 && tlb.getReceiverLng() != 0 && tlb.getSenderLng() != 0) {
            buidler.include(new LatLng(tlb.getSenderLat(), tlb.getSenderLng()));
            buidler.include(new LatLng(tlb.getReceiverLat(), tlb.getReceiverLng()));
        }
        if (pos != null && pos.size() != 0) {
            for (LatLng po : pos) {
                buidler.include(po);
            }
//            buidler.include()
            LatLngBounds bounds = buidler.build();
            aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10));
        }
    }

    @Override
    public void onInitNaviFailure() {

    }

    @Override
    public void onInitNaviSuccess() {

    }

    @Override
    public void onStartNavi(int i) {

    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

    }

    @Override
    public void onGetNavigationText(int i, String s) {

    }

    @Override
    public void onEndEmulatorNavi() {

    }

    @Override
    public void onArriveDestination() {

    }

    @Override
    public void onCalculateRouteSuccess() {

    }

    @Override
    public void onCalculateRouteFailure(int i) {

    }

    @Override
    public void onReCalculateRouteForYaw() {

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {

    }

    @Override
    public void onArrivedWayPoint(int i) {

    }

    @Override
    public void onGpsOpenStatus(boolean b) {

    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {

    }
}
