package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.PolylineOptions;
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
 * Created by Administrator on 2015/12/1.
 */
public class ShowTrackActivity extends Activity {
    private Context mActivity;
    private TrackBean bean;
    private String sessionUuid;
    //声明变量
    private MapView mapView;
    private AMap aMap;
    private TrackLineBean tlb;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x12:
                    PolylineOptions line = new PolylineOptions();
                    ArrayList<TrackPointBean> list = tlb.getTraceInfo();
                    ArrayList<LatLng> pos = new ArrayList<LatLng>();

                    for (TrackPointBean point : list) {
                        LatLng latLng=new LatLng(point.getLat(),point.getLng());
                        pos.add(latLng);
                    }
                    line.addAll(pos);
                    line.color(Color.RED);
                    aMap.addPolyline(line);
                    break;
            }
        }
    };

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

    private void setUpMap() {
        // 绘制一个乌鲁木齐到哈尔滨的线
//        aMap.addPolyline((new PolylineOptions()).add(
//                new LatLng(43.828, 87.621), new LatLng(45.808, 126.55)).color(
//                Color.RED));
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
                        Toast.makeText(getApplicationContext(), "获取查询信息失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    jsonArray = jsonObject.getJSONArray("rows");
                    JSONObject obj = (JSONObject) jsonArray.get(0);
                    String objStr = obj.toString();
//                    Common.printErrLog("解析正常rows" + objStr);
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
}
