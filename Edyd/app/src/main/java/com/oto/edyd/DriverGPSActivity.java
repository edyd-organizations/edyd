package com.oto.edyd;

import android.app.Activity;
import android.os.Bundle;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.AMapNaviViewOptions;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.NaviInfo;
import android.content.Intent;
import android.view.KeyEvent;
import com.oto.edyd.utils.TTSController;

/**
 * Created by zfh on 2015/12/18.
 * 司机导航页面
 */
public class DriverGPSActivity extends Activity implements AMapNaviViewListener{

    private AMapNaviView mAmapAMapNaviView;
    // 导航可以设置的参数
    private boolean mDayNightFlag =false;// 默认为白天模式
    private boolean mDeviationFlag = true;// 默认进行偏航重算
    private boolean mJamFlag = true;// 默认进行拥堵重算
    private boolean mTrafficFlag =true;// 默认进行交通播报
    private boolean mCameraFlag =true;// 默认进行摄像头播报
    private boolean mScreenFlag = true;// 默认是屏幕常亮
    private AMapNaviListener mAmapNaviListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simplegps_navi);
        TTSController ttsManager = TTSController.getInstance(this);// 初始化语音模块
        ttsManager.init();
        AMapNavi.getInstance(this).setAMapNaviListener(ttsManager);// 设置语音模块播报
        //语音播报开始
        TTSController.getInstance(this).startSpeaking();
        // 实时导航方式进行导航
        AMapNavi.getInstance(this).startNavi(AMapNavi.GPSNaviMode);

        initView(savedInstanceState);
    }
    private void initView(Bundle savedInstanceState) {
        mAmapAMapNaviView = (AMapNaviView) findViewById(R.id.customnavimap);
        mAmapAMapNaviView.onCreate(savedInstanceState);
        mAmapAMapNaviView.setAMapNaviViewListener(this);
        // 设置模拟速度
        //AMapNavi.getInstance(this).setEmulatorNaviSpeed(100);
        // 开启模拟导航
       //AMapNavi.getInstance(this).startNavi(AMapNavi.EmulatorNaviMode);
        setAmapNaviViewOptions();
    }

    /**
     * 设置导航的参数
     */
    private void setAmapNaviViewOptions() {
        if (mAmapAMapNaviView == null) {
            return;
        }
        AMapNaviViewOptions viewOptions = new AMapNaviViewOptions();
        viewOptions.setSettingMenuEnabled(false);// 设置导航setting可用
        viewOptions.setNaviNight(mDayNightFlag);// 设置导航是否为黑夜模式
        viewOptions.setReCalculateRouteForYaw(mDeviationFlag);// 设置导偏航是否重算
        viewOptions.setReCalculateRouteForTrafficJam(mJamFlag);// 设置交通拥挤是否重算
        viewOptions.setTrafficInfoUpdateEnabled(mTrafficFlag);// 设置是否更新路况
        viewOptions.setCameraInfoUpdateEnabled(mCameraFlag);// 设置摄像头播报
        viewOptions.setScreenAlwaysBright(mScreenFlag);// 设置屏幕常亮情况
        //viewOptions.setNaviViewTopic(mThemeStle);// 设置导航界面主题样式

        mAmapAMapNaviView.setViewOptions(viewOptions);

    }
    private AMapNaviListener getAMapNaviListener() {
        if (mAmapNaviListener == null) {

            mAmapNaviListener = new AMapNaviListener() {

                @Override
                public void onTrafficStatusUpdate() {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onStartNavi(int arg0) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onReCalculateRouteForYaw() {
                    // 可以在频繁重算时进行设置,例如五次之后
                    // i++;
                    // if (i >= 5) {
                    // AMapNaviViewOptions viewOptions = new
                    // AMapNaviViewOptions();
                    // viewOptions.setReCalculateRouteForYaw(false);
                    // mAmapAMapNaviView.setViewOptions(viewOptions);
                    // }
                }

                @Override
                public void onReCalculateRouteForTrafficJam() {

                }

                @Override
                public void onLocationChange(AMapNaviLocation location) {

                }

                @Override
                public void onInitNaviSuccess() {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onInitNaviFailure() {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onGetNavigationText(int arg0, String arg1) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onEndEmulatorNavi() {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onCalculateRouteSuccess() {

                }

                @Override
                public void onCalculateRouteFailure(int arg0) {

                }

                @Override
                public void onArrivedWayPoint(int arg0) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onArriveDestination() {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onGpsOpenStatus(boolean arg0) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onNaviInfoUpdated(AMapNaviInfo arg0) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onNaviInfoUpdate(NaviInfo arg0) {

                    // TODO Auto-generated method stub

                }
            };
        }
        return mAmapNaviListener;
    }

    // -------处理

    @Override
    public void onNaviSetting() {

    }

    /**
     * 导航界面返回按钮监听
     * */
    @Override
    public void onNaviCancel() {
        Intent intent = new Intent(DriverGPSActivity.this,
                DriverGPSActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        finish();
    }

    @Override
    public void onNaviMapMode(int arg0) {

    }
    @Override
    public void onNaviTurnClick() {


    }

    @Override
    public void onNextRoadClick() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onScanViewButtonClick() {
        // TODO Auto-generated method stub

    }
    private void processBundle(Bundle bundle) {
     /*   if (bundle != null) {
            mDayNightFlag = bundle.getBoolean(Utils.DAY_NIGHT_MODE,
                    mDayNightFlag);
            mDeviationFlag = bundle.getBoolean(Utils.DEVIATION, mDeviationFlag);
            mJamFlag = bundle.getBoolean(Utils.JAM, mJamFlag);
            mTrafficFlag = bundle.getBoolean(Utils.TRAFFIC, mTrafficFlag);
            mCameraFlag = bundle.getBoolean(Utils.CAMERA, mCameraFlag);
            mScreenFlag = bundle.getBoolean(Utils.SCREEN, mScreenFlag);
            mThemeStle = bundle.getInt(Utils.THEME);

        }*/
    }
    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(DriverGPSActivity.this,
                    DriverGPSActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    // ------------------------------生命周期方法---------------------------
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mAmapAMapNaviView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Bundle bundle = getIntent().getExtras();
        processBundle(bundle);
        setAmapNaviViewOptions();
        AMapNavi.getInstance(this).setAMapNaviListener(getAMapNaviListener());
        //语音播报开始
       // TTSController.getInstance(this).startSpeaking();
        mAmapAMapNaviView.onResume();

    }

    @Override
    public void onPause() {
        mAmapAMapNaviView.onPause();
        super.onPause();
        AMapNavi.getInstance(this)
                .removeAMapNaviListener(getAMapNaviListener());

    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        mAmapAMapNaviView.onDestroy();
        //页面结束时，停止语音播报
        TTSController.getInstance(this).stopSpeaking();
    }

    @Override
    public void onLockMap(boolean arg0) {

        // TODO Auto-generated method stub

    }
}
