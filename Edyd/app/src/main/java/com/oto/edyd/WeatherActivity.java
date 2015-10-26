package com.oto.edyd;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.oto.edyd.model.FutureWeatherBean;
import com.oto.edyd.model.HoursWeatherBean;
import com.oto.edyd.model.PMBean;
import com.oto.edyd.model.WeatherBean;
import com.oto.edyd.service.WeatherService;
import com.oto.edyd.lib.swiperefresh.PullToRefreshBase;
import com.oto.edyd.lib.swiperefresh.PullToRefreshScrollView;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.NetWork;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class WeatherActivity extends Activity implements LocationSource, AMapLocationListener {

    private static String currentCity; //定位当前城市

    private AMap aMap; //操作地图的工具类
    private MapView mapView; //地图控件，用于地图显示
    private OnLocationChangedListener mListener;
    private LocationManagerProxy mAMapLocationManager;
    private String resultCity;

    private Context mContext;
    private PullToRefreshScrollView mPullToRefreshScrollView = null; //下拉组件
    private ScrollView mScrollView = null; //滚动视图
    private WeatherService mService;

    private boolean isRunningService = false;

    private TextView tv_city,// 城市
            tv_release,// 发布时间
            tv_now_weather,// 天气
            tv_today_temp,// 温度
            tv_now_temp,// 当前温度
            tv_aqi,// 空气质量指数
            tv_quality,// 空气质量
            tv_next_three,// 3小时
            tv_next_six,// 6小时
            tv_next_nine,// 9小时
            tv_next_twelve,// 12小时
            tv_next_fifteen,// 15小时
            tv_next_three_temp,// 3小时温度
            tv_next_six_temp,// 6小时温度
            tv_next_nine_temp,// 9小时温度
            tv_next_twelve_temp,// 12小时温度
            tv_next_fifteen_temp,// 15小时温度
            tv_today_temp_a,// 今天温度a
            tv_today_temp_b,// 今天温度b
            tv_tommorrow,// 明天
            tv_tommorrow_temp_a,// 明天温度a
            tv_tommorrow_temp_b,// 明天温度b
            tv_thirdday,// 第三天
            tv_thirdday_temp_a,// 第三天温度a
            tv_thirdday_temp_b,// 第三天温度b
            tv_fourthday,// 第四天
            tv_fourthday_temp_a,// 第四天温度a
            tv_fourthday_temp_b,// 第四天温度b
            tv_humidity,// 湿度
            tv_wind, tv_uv_index,// 紫外线指数
            tv_dressing_index;// 穿衣指数

    private ImageView iv_now_weather,//现在天气
            iv_now_weather_b, //将来天气
            iv_next_three,// 3小时
            iv_next_six,// 6小时
            iv_next_nine,// 9小时
            iv_next_twelve,// 12小时
            iv_next_fifteen,// 15小时
            iv_today_weather,// 今天
            iv_tommorrow_weather,// 明天
            iv_thirdday_weather,// 第三天
            iv_fourthday_weather;// 第四天

    private RelativeLayout rl_city;
    private LinearLayout weather_linear;

    private boolean isRunning = false; //下拉刷新回调执行
    private int count = 0;
    private TextView city_separator;

    private CusProgressDialog weatherProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        weatherProgressDialog = new CusProgressDialog(this, "正在加载天气...");
        weatherProgressDialog.getLoadingDialog().show();
        //new Thread(new WeatherDialogThread(weatherProgressDialog)).start(); //弹出对话框线程

        setContentView(R.layout.current_weather);
        NetWork netWork = new NetWork(this);
        mContext = getApplicationContext();
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState); //此方法必须重写
        if(!netWork.isHaveInternet()) {
            Toast.makeText(WeatherActivity.this, "无网络请求", Toast.LENGTH_SHORT).show();
            weatherProgressDialog.dismissDialog();
            return;
        }
        init();
    }


    /**
     * 初始化Service
     * @param city
     */
    private void initService(String city) {
        Intent intent = new Intent(mContext, WeatherService.class);
        intent.putExtra("city", city);
        startService(intent);
        isRunningService = bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((WeatherService.WeatherServiceBinder)service).getService(); //获取Service
            mService.setCallBack(new WeatherService.OnParserCallBack() {

                @Override
                public void OnParserComplete(List<HoursWeatherBean> hoursWeatherBeanList, PMBean pmBean, WeatherBean weatherBean) {

                    if(weatherProgressDialog != null) {
                        weatherProgressDialog.dismissDialog();
                    }
                    mPullToRefreshScrollView.onRefreshComplete();
                    if (hoursWeatherBeanList != null && hoursWeatherBeanList.size() >= 5) {
                        setHourViews(hoursWeatherBeanList);
                    }

                    if (pmBean != null) {
                        setPMView(pmBean);
                    }

                    if (weatherBean != null) {
                        setWeatherViews(weatherBean);
                    }
                }
            });
            mService.loadData(currentCity);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService.removeCallBack();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
    protected void onDestroy() {
        if(isRunningService) {
            unbindService(conn);
        }
        if(mapView != null) {
            mapView.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == 1) {
            String cityName = data.getStringExtra("city");
            mService.loadData(cityName);
        }
    }

    //数据初始化
    private void init() {
        mPullToRefreshScrollView = (PullToRefreshScrollView)findViewById(R.id.pull_refresh_scrollview);
        mScrollView = mPullToRefreshScrollView.getRefreshableView();

        rl_city = (RelativeLayout) findViewById(R.id.rl_city);
        tv_city = (TextView) findViewById(R.id.tv_city);
        tv_release = (TextView) findViewById(R.id.tv_release);
        tv_now_weather = (TextView) findViewById(R.id.tv_now_weather);
        tv_today_temp = (TextView) findViewById(R.id.tv_today_temp);
        tv_now_temp = (TextView) findViewById(R.id.tv_now_temp);
        tv_aqi = (TextView) findViewById(R.id.tv_aqi);
        tv_quality = (TextView) findViewById(R.id.tv_quality);
        tv_next_three = (TextView) findViewById(R.id.tv_next_three);
        tv_next_six = (TextView) findViewById(R.id.tv_next_six);
        tv_next_nine = (TextView) findViewById(R.id.tv_next_nine);
        tv_next_twelve = (TextView) findViewById(R.id.tv_next_twelve);
        tv_next_fifteen = (TextView) findViewById(R.id.tv_next_fifteen);
        tv_next_three_temp = (TextView) findViewById(R.id.tv_next_three_temp);
        tv_next_six_temp = (TextView) findViewById(R.id.tv_next_six_temp);
        tv_next_nine_temp = (TextView) findViewById(R.id.tv_next_nine_temp);
        tv_next_twelve_temp = (TextView) findViewById(R.id.tv_next_twelve_temp);
        tv_next_fifteen_temp = (TextView) findViewById(R.id.tv_next_fifteen_temp);
        tv_today_temp_a = (TextView) findViewById(R.id.tv_today_temp_a);
        tv_today_temp_b = (TextView) findViewById(R.id.tv_today_temp_b);
        tv_tommorrow = (TextView) findViewById(R.id.tv_tommorrow);
        tv_tommorrow_temp_a = (TextView) findViewById(R.id.tv_tommorrow_temp_a);
        tv_tommorrow_temp_b = (TextView) findViewById(R.id.tv_tommorrow_temp_b);
        tv_thirdday = (TextView) findViewById(R.id.tv_thirdday);
        tv_thirdday_temp_a = (TextView) findViewById(R.id.tv_thirdday_temp_a);
        tv_thirdday_temp_b = (TextView) findViewById(R.id.tv_thirdday_temp_b);
        tv_fourthday = (TextView) findViewById(R.id.tv_fourthday);
        tv_fourthday_temp_a = (TextView) findViewById(R.id.tv_fourthday_temp_a);
        tv_fourthday_temp_b = (TextView) findViewById(R.id.tv_fourthday_temp_b);
        tv_humidity = (TextView) findViewById(R.id.tv_humidity);
        tv_wind = (TextView) findViewById(R.id.tv_wind);
        tv_uv_index = (TextView) findViewById(R.id.tv_uv_index);
        tv_dressing_index = (TextView) findViewById(R.id.tv_dressing_index);

        iv_now_weather = (ImageView) findViewById(R.id.iv_now_weather);
        iv_now_weather_b = (ImageView) findViewById(R.id.iv_now_weather_b);
        iv_next_three = (ImageView) findViewById(R.id.iv_next_three);
        iv_next_six = (ImageView) findViewById(R.id.iv_next_six);
        iv_next_nine = (ImageView) findViewById(R.id.iv_next_nine);
        iv_next_twelve = (ImageView) findViewById(R.id.iv_next_twelve);
        iv_next_fifteen = (ImageView) findViewById(R.id.iv_next_fifteen);
        iv_today_weather = (ImageView) findViewById(R.id.iv_today_weather);
        iv_tommorrow_weather = (ImageView) findViewById(R.id.iv_tommorrow_weather);
        iv_thirdday_weather = (ImageView) findViewById(R.id.iv_thirdday_weather);
        iv_fourthday_weather = (ImageView) findViewById(R.id.iv_fourthday_weather);
        weather_linear = (LinearLayout)findViewById(R.id.weather_linear);
        city_separator = (TextView) findViewById(R.id.city_separator);


        mPullToRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ScrollView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
                NetWork netWork = new NetWork(getApplicationContext());
                if(!netWork.isHaveInternet()){
                    //无网络访问
                    Toast.makeText(getApplicationContext().getApplicationContext(), Constant.NOT_INTERNET_CONNECT, Toast.LENGTH_SHORT).show();
                    mPullToRefreshScrollView.onRefreshComplete();
                    return;
                }
                mService.loadData(tv_city.getText().toString());
            }
        });
        rl_city.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //startActivityForResult(new Intent(mContext, CityActivity.class), 1);
            }
        });

        if(aMap == null) {
            aMap = mapView.getMap();
        }
        setUpMap();
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
     * 设置城市数据
     * @param weatherBean
     */
    public void setWeatherViews(WeatherBean weatherBean) {
        tv_city.setText(weatherBean.getCity());
        tv_release.setText(weatherBean.getRelease());
        tv_now_weather.setText(weatherBean.getWeather());

        String[]tempArry = weatherBean.getTemperature().split("~");// º ↑↓
        String temp_str_a = tempArry[0].substring(0, tempArry[0].indexOf("℃"));
        String temp_str_b = tempArry[1].substring(0, tempArry[1].indexOf("℃"));
        tv_today_temp.setText("↑ "+temp_str_a+"º ↓ "+temp_str_b+"º");
        tv_now_temp.setText(weatherBean.getTemp() + "º");

        Calendar calendar = Calendar.getInstance();
        int time = calendar.get(Calendar.HOUR_OF_DAY);
        String prefixStr = null;
        if(time >= 6 && time <=18) {
            prefixStr = "d";
            //weather_linear.setBackgroundResource(R.mipmap.bg_weather_day);
        } else {
            prefixStr = "n";
            weather_linear.setBackgroundResource(R.mipmap.bg_weather_night);
        }
        iv_now_weather.setImageResource(getResources().getIdentifier(prefixStr + weatherBean.getWeather_id(), "mipmap", "com.oto.edyd"));
        if(weatherBean.getWeather_id().equals(weatherBean.getWeather_id_b())) {
            city_separator.setVisibility(View.INVISIBLE);
            iv_now_weather_b.setVisibility(View.INVISIBLE);
        }else {
            iv_now_weather_b.setImageResource(getResources().getIdentifier(prefixStr + weatherBean.getWeather_id_b(), "mipmap", "com.oto.edyd"));
        }
        tv_humidity.setText(weatherBean.getHumidity());
        tv_wind.setText(weatherBean.getWind_direction() + " "+weatherBean.getWind_strength());
        tv_uv_index.setText(weatherBean.getUv_index());
        tv_dressing_index.setText(weatherBean.getDressing_index());



        tv_today_temp_a.setText(temp_str_a+"º");
        tv_today_temp_b.setText(temp_str_b+"º");
        List<FutureWeatherBean> futureWeatherBeanList = weatherBean.getFutureList();
        if (futureWeatherBeanList.size() == 3) {
            setFutureViews(tv_tommorrow, iv_today_weather, tv_tommorrow_temp_a, tv_tommorrow_temp_b, futureWeatherBeanList.get(0));
            setFutureViews(tv_thirdday, iv_thirdday_weather, tv_thirdday_temp_a, tv_thirdday_temp_b, futureWeatherBeanList.get(1));
            setFutureViews(tv_fourthday, iv_fourthday_weather, tv_fourthday_temp_a, tv_fourthday_temp_b,futureWeatherBeanList.get(2));
        }
    }

    private void setFutureViews(TextView tv_week, ImageView iv_weather, TextView tv_temp_a, TextView tv_temp_b, FutureWeatherBean futureWeatherBean) {
        tv_week.setText(futureWeatherBean.getWeek());
        iv_weather.setImageResource(getResources().getIdentifier("d" + futureWeatherBean.getWeather_id(), "mipmap", "com.oto.edyd"));
        String[]tempArry = futureWeatherBean.getTemperature().split("~");// º ↑↓
        String temp_str_a = tempArry[0].substring(0, tempArry[0].indexOf("℃"));
        String temp_str_b = tempArry[1].substring(0, tempArry[1].indexOf("℃"));
        tv_temp_a.setText(temp_str_a + "º");
        tv_temp_b.setText(temp_str_b + "º");

    }

    /**
     * 设置3小时间隔数据
     * @param hoursWeatherBeanList
     */
    private void setHourViews(List<HoursWeatherBean> hoursWeatherBeanList) {
        setHourData(tv_next_three, iv_next_three, tv_next_three_temp, hoursWeatherBeanList.get(0));
        setHourData(tv_next_six, iv_next_six, tv_next_six_temp, hoursWeatherBeanList.get(1));
        setHourData(tv_next_nine, iv_next_nine, tv_next_nine_temp, hoursWeatherBeanList.get(2));
        setHourData(tv_next_twelve, iv_next_twelve, tv_next_twelve_temp, hoursWeatherBeanList.get(3));
        setHourData(tv_next_fifteen, iv_next_fifteen, tv_next_fifteen_temp, hoursWeatherBeanList.get(4));
    }
    //填充未来3小时
    private void setHourData(TextView tv_hour, ImageView iv_weather, TextView tv_temp, HoursWeatherBean hoursWeatherBean) {
        String prefixStr = null;
        int time = Integer.valueOf(hoursWeatherBean.getTime());
        if(time >= 6 && time <=18) {
            prefixStr = "d";
        } else {
            prefixStr = "n";
        }

        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String date_a = sdf.format(date);
        String date_b = hoursWeatherBean.getSfdate().substring(0,8);
        if(date_a.equals(date_b)) { //今天
            if(time >=0 && time <=12) {//上午
                hoursWeatherBean.setTime("上午"+ String.valueOf(time)+"时");
            } else {
                //下午
                hoursWeatherBean.setTime("下午"+ String.valueOf(time)+"时");
            }
        } else {//明天
            if(time >=0 && time <=12) {//上午
                hoursWeatherBean.setTime("明天上午"+ String.valueOf(time)+"时");
            }else {
                //下午
                hoursWeatherBean.setTime("明天下午"+ String.valueOf(time)+"时");
            }
        }

        tv_hour.setText(hoursWeatherBean.getTime());
        iv_weather.setImageResource(getResources().getIdentifier(prefixStr + hoursWeatherBean.getWeather_id(), "mipmap", "com.oto.edyd"));
        tv_temp.setText(hoursWeatherBean.getTemp_a());
    }

    private void setPMView(PMBean pmData) {
        tv_aqi.setText(pmData.getAqi());
        tv_quality.setText(pmData.getQuality());
    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null&& amapLocation.getAMapException().getErrorCode() == 0) {
                currentCity = amapLocation.getCity(); //取得当前定位城市化
                if(currentCity != null) {
                    if(currentCity.indexOf("市") != -1) {
                        resultCity = currentCity.substring(0, currentCity.indexOf("市")).trim();
                    }
                    if(resultCity != null) {
                        initService(resultCity);
                    }
                }
                //mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
            } else {
                Log.e("AmapErr", "Location ERR:" + amapLocation.getAMapException().getErrorCode());
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
            mAMapLocationManager.requestLocationData(LocationProviderProxy.AMapNetwork, -1, 10, this);
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mAMapLocationManager != null) {
            //mAMapLocationManager.removeUpdates(this);
            mAMapLocationManager.destroy();
        }
        mAMapLocationManager = null;
    }

    /**
     * 对话框线程类
     */
    private class WeatherDialogThread implements Runnable {
        private CusProgressDialog exitProgressDialog;

        public WeatherDialogThread(CusProgressDialog exitProgressDialog) {
            this.exitProgressDialog = exitProgressDialog;
        }
        @Override
        public void run() {
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Message message = new Message();
            message.what = 1000;
            handler.sendMessage(message);
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1000: //退出登录
                    weatherProgressDialog.dismissDialog();
                    break;
            }
        }
    };
}
