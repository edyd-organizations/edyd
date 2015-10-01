package com.oto.edyd;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.amap.api.location.AMapLocalWeatherForecast;
import com.amap.api.location.AMapLocalWeatherListener;
import com.amap.api.location.AMapLocalWeatherLive;
import com.amap.api.location.LocationManagerProxy;

/**
 * Created by yql on 2015/9/7.
 */
public class CurrentWeatherReportActivity extends Activity implements AMapLocalWeatherListener {

    private LocationManagerProxy mLocationManagerProxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.current_weather);
        init();
    }

    @Override
    public void onWeatherLiveSearched(AMapLocalWeatherLive aMapLocalWeatherLive) {
        if (aMapLocalWeatherLive!=null&&aMapLocalWeatherLive.getAMapException().getErrorCode() == 0) {

        } else {
            // 获取天气预报失败

        }
    }

    @Override
    public void onWeatherForecaseSearched(AMapLocalWeatherForecast aMapLocalWeatherForecast) {
        if (aMapLocalWeatherForecast!=null&&aMapLocalWeatherForecast.getAMapException().getErrorCode() == 0) {

        } else {
            // 获取天气预报失败

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 销毁定位
        mLocationManagerProxy.destroy();
    }
    protected void onDestroy() {
        super.onDestroy();

    }

    private void init() {
        mLocationManagerProxy = LocationManagerProxy.getInstance(this);
        mLocationManagerProxy.requestWeatherUpdates(LocationManagerProxy.WEATHER_TYPE_LIVE, this); //获取实时天气预报
        mLocationManagerProxy.requestWeatherUpdates(LocationManagerProxy.WEATHER_TYPE_FORECAST, this); //如果需要同时请求实时、未来三天天气，请确保定位获取位置后使用,分开调用，可忽略本句。
    }
}
