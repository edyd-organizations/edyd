package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.Window;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.TextView;
import android.widget.Toast;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.AMap.OnMapLoadedListener;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.Projection;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.LatLngBounds;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;


public class PanoramaActivity extends Activity implements OnMapLoadedListener,AMap.OnMarkerClickListener,
        AMap.OnInfoWindowClickListener,AMap.InfoWindowAdapter,AMap.OnMarkerDragListener,GeocodeSearch.OnGeocodeSearchListener {
    private MapView mapView; //地图控件，用于地图显示
    private AMap aMap; //操作地图的工具类
    private Common common;
    private String sessionUuid;
    String location;//得到调度车辆的所有位置信息
    MarkerOptions markerOptions;//标记对象
/*    String driverName;//司机姓名
    String controlNum;//调度单号
    String driverTel;//司机电话
    String trunckNum;//车号
    double slat;//纬度
    double slng;//经度
    String order;//调度单状况
    String operTime;//操作时间*/
    List<CarInfo> addInfo;
    private LocationManagerProxy mAMapLocationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //不显示程序标题栏
        setContentView(R.layout.activity_panorama);
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        mapView = (MapView) findViewById(R.id.map);
        // mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState); //此方法必须重写
        init();
        requestport();//请求接口
    }
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x20:
                    traverse();//遍历容器
            }
        }
    };

    private void traverse() {
        for (int i=0;i<addInfo.size();i++){
            CarInfo carInfo = addInfo.get(i);
            LatLng  latlng = new LatLng(carInfo.getSlat(), carInfo.getSlng());
            addAllMarker(latlng,carInfo);//添加所有的位置
        }
    }

    private void requestport() {
        addInfo = new ArrayList<CarInfo>();
        // location="http://www.edyd.cn/api/v1.0/" +"viewTruckPanorama.json?"+"&sessionUuid="+sessionUuid;//得到调度车辆的所有位置信息
          location="http://www.edyd.cn/api/v1.0/" +"viewTruckPanorama.json?"+"&sessionUuid="+"879425d835d34ac183dddddf831ecdc7";//得到调度车辆的所有位置信息
        OkHttpClientManager.getAsyn(location, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                Toast.makeText(PanoramaActivity.this, "失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response) {
                JSONObject accountTypeJson;
                JSONArray accountTypeArray;
                try {
                    accountTypeJson = new JSONObject(response);
                    accountTypeArray = accountTypeJson.getJSONArray("rows");
                    for (int i = 0; i < accountTypeArray.length(); i++) {
                        JSONObject jsonObject = accountTypeArray.getJSONObject(i);
                        int controlStatus = jsonObject.getInt("controlStatus");
                        String order = "";
                        switch (controlStatus){
                            case 0:
                                order="状态异常";
                            case 20:
                                order ="装货在途";
                                break;
                            case 30:
                                order ="到达装货";
                                break;
                            case 40:
                                order="装货完成";
                                break;
                            case 50:
                                order ="送货在途";
                                break;
                            case 60:
                                order ="到达收货";
                                break;
                            case 99:
                                order="收货完成";
                                break;
                        }

                        String lat = jsonObject.getString("lat");//纬度
                        String lng = jsonObject.getString("lng");//经度
                        String driverName = jsonObject.getString("driverName");
                        String controlNum = jsonObject.getString("controlNum");
                        String driverTel = jsonObject.getString("driverTel");
                        String trunckNum = jsonObject.getString("trunckNum");
                        String operTime = jsonObject.getString("operTime");
                        double slat = Double.parseDouble(lat);
                        double slng = Double.parseDouble(lng);
                        CarInfo carinfo=new CarInfo();
                        carinfo.setDriverName(driverName);//司机名字
                        carinfo.setControlNum(controlNum);//调度单号
                        carinfo.setDriverTel(driverTel);//司机电话
                        carinfo.setTrunckNum(trunckNum);//车牌好
                        carinfo.setOperTime(operTime);//操作时间
                        carinfo.setOrder(order);
                        carinfo.setSlat(slat);
                        carinfo.setSlng(slng);
                       /* LatLng  latlng = new LatLng(carinfo.getSlat(), carinfo.getSlng());
                        addAllMarker(latlng);//添加所有的位置*/
                        addInfo.add(carinfo);

                    }
                    Message message = Message.obtain();
                    message.what = 0x20;
                    handler.sendMessage(message);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    private void addAllMarker(LatLng latlng,CarInfo carInfo) {

        markerOptions = new MarkerOptions();
       // markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.truck));
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.car));
        markerOptions.position(latlng);
        markerOptions.title("车辆信息").draggable(true).anchor(0.5f, 0.5f);
        Marker marker = aMap.addMarker(markerOptions);
        marker.setObject(carInfo);
        marker.showInfoWindow();

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
        aMap.setMapType(AMap.MAP_TYPE_NORMAL); // 矢量地图模式
        aMap.setOnMapLoadedListener(this);// 设置amap加载成功事件监听器
        aMap.setOnMarkerClickListener(this);// 设置点击marker事件监听器
        aMap.setOnInfoWindowClickListener(this);// 设置点击infoWindow事件监听器
        aMap.setInfoWindowAdapter(this);// 设置自定义InfoWindow样式
        aMap.setMyLocationEnabled(true);//默认地图可以自动定位
    }

    @Override
    public void onMapLoaded() {
        // 设置所有maker显示在当前可视区域地图中
       /* LatLngBounds bounds = new LatLngBounds.Builder()
                .include(new LatLng(34.7466, 113.625367)).include(new LatLng(30.679879, 104.064855))
                .include(new LatLng(39.989614, 116.481763)).build();
        aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,10));*/
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
     * 对marker标注点点击响应事件
     */
    @Override
    public boolean onMarkerClick(final Marker marker) {
            if (aMap != null) {
              //  jumpPoint(marker);
            }
        return false;
    }


    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        //deactivate();
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
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
    /**
     * 监听点击infowindow窗口事件回调
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this,"infowindow点击",Toast.LENGTH_SHORT).show();
    }

    /**
     * 监听自定义infowindow窗口的infowindow事件回调
     */
    @Override
    public View getInfoWindow(Marker marker) {
        View infoWindow = getLayoutInflater().inflate(
                R.layout.custom_info_window, null);
        render(marker, infoWindow);
        return infoWindow;
    }

    /**
     * 自定义infowinfow窗口
     */
    private void render(Marker marker, View infoWindow) {
      //  Object object = marker.getObject();
        String title = marker.getTitle();
        CarInfo carInfo = (CarInfo) marker.getObject();
        TextView titleUi = ((TextView) infoWindow.findViewById(R.id.title));
        if (title != null) {
            SpannableString titleText = new SpannableString(title);
            titleText.setSpan(new ForegroundColorSpan(Color.BLUE), 0,
                    titleText.length(), 0);
            titleUi.setTextSize(12);
            titleUi.setText(titleText);

        } else {
            titleUi.setText("");
        }
       /* TextView Scheduling = ((TextView) infoWindow.findViewById(R.id.Scheduling));//调度单号
        Scheduling.setText("调度单号："+addInfo.get());
        TextView carNumber = (TextView) infoWindow.findViewById(R.id.carNumber);//车牌号
        carNumber.setText("车 牌号："+trunckNum);
        TextView driver = (TextView) infoWindow.findViewById(R.id.driver);//司机
        driver.setText("司  机："+driverName);
        TextView phone = (TextView) infoWindow.findViewById(R.id.phone);//电话
        driver.setText("电  话："+driverTel);
        TextView state = (TextView) infoWindow.findViewById(R.id.state);//状态
        state.setText("状  态："+order);
        TextView time = (TextView) infoWindow.findViewById(R.id.time);//时间
        time.setText("时  间："+operTime);
        TextView address = (TextView) infoWindow.findViewById(R.id.address);//地址
        time.setText("地  址：");*/
        TextView Scheduling = ((TextView) infoWindow.findViewById(R.id.Scheduling));//调度单号
        Scheduling.setText("调度单号："+carInfo.getControlNum());
        TextView carNumber = (TextView) infoWindow.findViewById(R.id.carNumber);//车牌号
        carNumber.setText("车  牌  号："+carInfo.getTrunckNum());
        TextView driver = (TextView) infoWindow.findViewById(R.id.driver);//司机
        driver.setText("司       机："+carInfo.getDriverName());
        TextView phone = (TextView) infoWindow.findViewById(R.id.phone);//电话
        phone.setText("电       话："+carInfo.getDriverTel());
        TextView state = (TextView) infoWindow.findViewById(R.id.state);//状态
        state.setText("状       态："+carInfo.getOrder());
       /* TextView time = (TextView) infoWindow.findViewById(R.id.time);//时间
        time.setText("时  间："+carInfo.getOperTime());*/
        TextView address = (TextView) infoWindow.findViewById(R.id.address);//地址
        address.setText("地       址：");

    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    /**
     * 逆地理编码回调
     */
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

    }

    /**
     * 地理编码查询回调
     */
    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }
}
