package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
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
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.oto.edyd.model.TrackBean;
import com.oto.edyd.model.TrackLineBean;
import com.oto.edyd.model.TrackPointBean;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.logging.LogRecord;

/**
 * Created by liubaozhong on 2015/12/1.
 * 显示地图轨迹。
 */
public class ShowTrackActivity extends Activity implements AMap.OnMarkerClickListener, AMap.InfoWindowAdapter, AMap.OnInfoWindowClickListener, AMap.OnMapLoadedListener {
    private Context mActivity;
    private TrackBean bean;
    private String sessionUuid;
    //声明变量
    private MapView mapView;
    private AMap aMap;
    private TrackLineBean tlb;
    private ArrayList<LatLng> pos;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x12:
                    if (tlb.getReceiverLat() != 0 && tlb.getSenderLat() != 0 && tlb.getReceiverLng() != 0 && tlb.getSenderLng() != 0) {

                        //发货人图标
                        LatLng senderPoint = new LatLng(tlb.getSenderLat(), tlb.getSenderLng());
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.deliveryside));
                        markerOptions.position(senderPoint);
                        markerOptions.title("发货方公司").draggable(true).anchor(0.5f, 1.0f);
                        Marker marker = aMap.addMarker(markerOptions);
                        marker.showInfoWindow();

                        //收货人图标
                        LatLng receiverPoint = new LatLng(tlb.getReceiverLat(), tlb.getReceiverLng());
                        MarkerOptions marOptions = new MarkerOptions();
                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.receivingparty));
                        markerOptions.position(receiverPoint);
                        markerOptions.title("收货方公司").draggable(true).anchor(0.5f, 1.0f);
                        Marker receiverMarker = aMap.addMarker(marOptions);
                        receiverMarker.showInfoWindow();
                    }
                    //画轨迹线
                    PolylineOptions line = new PolylineOptions();
                    ArrayList<TrackPointBean> list = tlb.getTraceInfo();
                    pos = new ArrayList<LatLng>();
                    if (list.size() == 0) {
                        Common.showToast(mActivity, "暂时没有轨迹");
                    }
                    for (int i = 0; i < list.size(); i++) {
                        TrackPointBean point = list.get(i);
                        LatLng latLng = new LatLng(point.getLat(), point.getLng());
                        pos.add(latLng);
                        addMarker(point, i, list);//添加所有的位置
                    }

                    line.addAll(pos);
                    line.color(Color.RED);
                    line.width(5);
                    aMap.addPolyline(line);

                    onMapLoaded();
                    break;
            }
        }
    };
    private GeocodeSearch geocoderSearch;


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
            getInfo(true);
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
        aMap.setOnMapLoadedListener(this);// 设置amap加载成功事件监听器
        aMap.setMyLocationEnabled(true);//默认地图可以自动定位
        aMap.setOnInfoWindowClickListener(this);// 设置点击infoWindow事件监听器
        aMap.setInfoWindowAdapter(this);// 设置自定义InfoWindow样式

    }

    private void getInfo(final boolean isFist) {
        long primaryId = bean.getPrimaryId();

        String url = Constant.ENTRANCE_PREFIX + "getTruckPosition.json?sessionUuid="
                + sessionUuid + "&primaryId=" + primaryId;
//        Common.printErrLog("轨迹地图" + url);
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                Toast.makeText(getApplicationContext(), "获取信息失败", Toast.LENGTH_SHORT).show();
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

//        LatLonPoint latLonPoint = new LatLonPoint(trackPointBean.getLat(), trackPointBean.getLng());
//        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,
//                GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
//        geocoderSearch = new GeocodeSearch(this);
//        geocoderSearch.setOnGeocodeSearchListener(this);
//        geocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求
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

    @Override
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

}
