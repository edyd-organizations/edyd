package com.oto.edyd;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.lib.imageindicator.AutoPlayManager;
import com.oto.edyd.lib.imageindicator.ImageIndicatorView;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;

import retrofit.mime.MultipartTypedOutput;

/**
 * Created by yql on 2015/8/25.
 * 首页界面
 */
public class MainIndexFragment extends Fragment implements View.OnClickListener{

    private View mainIndexView; //首页
    private ImageIndicatorView imageIndicatorView; //图片指示器
    private boolean isAutoPlay = true; //是否自动播放图片
    private LinearLayout locationWeather; //本地天气
    private LinearLayout locationPosition;
    private TextView latestNews; //最新旋转效果
    private LinearLayout oilService; //油品服务
    private LinearLayout insuranceService; //保险
    private LinearLayout financialService; //金融服务
    private LinearLayout vehicleOrder; //我的车辆订单
    private LinearLayout orderMain; //商品订单
    private LinearLayout collectionMain; //我的收藏
    private LinearLayout todayOrder; //今日订单
    private LinearLayout panorama; //全景图
    private RelativeLayout discountMain; //打折

    private Intent intent; //跳转

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainIndexView = inflater.inflate(R.layout.main_index, null);
        initFields(mainIndexView);
        playSlides(); //播放幻灯片
        locationWeather.setOnClickListener(this);
        locationPosition.setOnClickListener(this);
        oilService.setOnClickListener(this);
        insuranceService.setOnClickListener(this);
        financialService.setOnClickListener(this);
        vehicleOrder.setOnClickListener(this);
        orderMain.setOnClickListener(this);
        collectionMain.setOnClickListener(this);
        todayOrder.setOnClickListener(this);
        orderMain.setOnClickListener(this);
        panorama.setOnClickListener(this);
        discountMain.setOnClickListener(this);
        return mainIndexView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.weather_main: //本地天气
                //weatherProgressDialog.getLoadingDialog().show();
                intent = new Intent(getActivity(), WeatherActivity.class);
                startActivity(intent);
                break;
            case R.id.position_main: //本地位置
                intent = new Intent(getActivity(), MultyLocationActivity.class);
                startActivity(intent);
                break;
            case R.id.oil_server_main: //油品服务
                intent = new Intent(getActivity(), OilServiceActivity.class);
                startActivity(intent);
                break;
            case R.id.insurance_main: //保险服务
                intent = new Intent(getActivity(), InsuranceActivity.class);
                startActivity(intent);
                break;
            case R.id.finance_main: //保险服务
                intent = new Intent(getActivity(), FinancialActivity.class);
                startActivity(intent);
                break;
            case R.id.guidepost_main: //我的车辆订单
                intent = new Intent(getActivity(), OrderOperateActivity.class);
                startActivity(intent);
                break;
            case R.id.order_main: //商品订单
                intent = new Intent(getActivity(), WaitBuild.class);
                startActivity(intent);
                break;
            case R.id.collection_main: //我的收藏
                intent = new Intent(getActivity(), WaitBuild.class);
                startActivity(intent);
                break;
            case R.id.order_today: //今日订单
                intent = new Intent(getActivity(), OrderOperateActivity.class);
                startActivity(intent);
                break;
            case R.id.panorama: //全景图
                intent = new Intent(getActivity(), WaitBuild.class);
                startActivity(intent);
                break;
            case R.id.discount_main: //打折
                intent = new Intent(getActivity(), WaitBuild.class);
                startActivity(intent);
                break;
        }
    }

    /**
     * 初始化变量
     * @param view
     */
    private void initFields(View view) {
        imageIndicatorView = (ImageIndicatorView)view.findViewById(R.id.indicate_view); //图片轮播
        locationWeather = (LinearLayout) view.findViewById(R.id.weather_main); //本地天气
        locationPosition = (LinearLayout) view.findViewById(R.id.position_main); //本地位置
        latestNews = (TextView) view.findViewById(R.id.latest_news); //最新
        oilService = (LinearLayout) view.findViewById(R.id.oil_server_main); //油品服务
        insuranceService = (LinearLayout) view.findViewById(R.id.insurance_main); //保险服务
        financialService = (LinearLayout) view.findViewById(R.id.finance_main); //金融服务
        vehicleOrder = (LinearLayout) view.findViewById(R.id.guidepost_main); //我的车辆订单
        orderMain = (LinearLayout) view.findViewById(R.id.order_main); //商品订单
        collectionMain = (LinearLayout) view.findViewById(R.id.collection_main); //我的收藏
        todayOrder = (LinearLayout) view.findViewById(R.id.order_today); //今日订单
        panorama = (LinearLayout) view.findViewById(R.id.panorama); //全景图
        discountMain = (RelativeLayout) view.findViewById(R.id.discount_main);

        Animation mAnimationRight = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_textview);
        mAnimationRight.setFillAfter(true);
        latestNews.setAnimation(mAnimationRight);
    }
    /**
     * 图片轮播
     */
    private void playSlides() {
        Integer[] resArray = new Integer[] { R.mipmap.slide_01, R.mipmap.slide_02 };
        imageIndicatorView.setupLayoutByDrawable(resArray);
        imageIndicatorView.setIndicateStyle(ImageIndicatorView.INDICATE_ARROW_ROUND_STYLE);
        imageIndicatorView.show();
        //是否启动自动轮播
        if(isAutoPlay){
            autoPlay();
        }
    }

    /**
     * 自动播放图片
     */
    private void autoPlay() {
        AutoPlayManager autoBrocastManager =  new AutoPlayManager(imageIndicatorView);
        autoBrocastManager.setBroadcastEnable(true);
        autoBrocastManager.setBroadCastTimes(5);//loop times
        autoBrocastManager.setBroadcastTimeIntevel(3 * 1000, 3 * 1000);//set first play time and interval
        autoBrocastManager.loop();
    }
}
