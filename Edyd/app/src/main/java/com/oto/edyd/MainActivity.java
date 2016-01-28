package com.oto.edyd;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.lib.slidingmenu.SlidingMenu;
import com.oto.edyd.lib.slidingmenu.app.SlidingFragmentActivity;
import com.oto.edyd.module.common.activity.NoticeActivity;
import com.oto.edyd.module.oil.fragment.MainOilFragment;
import com.oto.edyd.module.tts.fragment.TransportDriverFragment;
import com.oto.edyd.module.tts.fragment.TransportUndertakeFragment;
import com.oto.edyd.module.usercenter.activity.LoginActivity;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.ServiceUtil;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;
import com.umeng.update.UmengUpdateAgent;

import java.util.Map;


/**
 * 功能：主Activity
 * 文件名：com.oto.edyd.MainActivity.java
 * 创建时间：2015/12/14
 * 作者：yql
 */
public class MainActivity extends SlidingFragmentActivity implements View.OnClickListener {
    //----------基础View控件-------------
    private TextView mainTitle; //标题
    private RadioButton home; //首页
    private RadioButton market; //商城
    private RadioButton vehicleServer; //车辆服务
    private RadioButton box; //百宝箱
    private ImageView enterLogin; //登录

    //----------变量-------------
    private MainViewHolder mainViewHolder; //用于主界面暂存Fragment
    private FragmentManager fragmentManager; //fragment管理器
    private long eOldTime; //记录点击退出时间
    private Common common; //共享文件LOGIN_PREFERENCES_FILE
    private Common fixedCommon; //共享文件FIXED_FILE
    private Context context; //上下文对象
    private LeftSlidingFragment leftMenuFragment; //侧滑Fragment
    private SlidingMenu slidingMenu; //侧滑对象
    private final static int HANDLER_ACCOUNT_TYPE_CODE = 0x10; //账户类型切换码
    private final static int HANDLER_SWITCH_DURATION = 500; //账户类型切换等待时间，单位：毫秒


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init(); //数据初始化
    }

    /**
     * 数据初始化
     */
    private void init() {
        initFields(); //初始化字段
        initListener(); //初始化监听器
        initLeftMenu(); //初始化侧边栏
        initMainIndex(); //初始化主界面
        initUmengService(); //初始化友盟消息推送服务
        invokeTimer(); //是否开启定时器
        if (common.isLogin()) {
            enterLogin.setVisibility(View.GONE);
        } else {
            enterLogin.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 初始化数据
     */
    private void initFields() {
        mainTitle = (TextView) findViewById(R.id.main_title);
        home = (RadioButton) findViewById(R.id.main_home);
        market = (RadioButton) findViewById(R.id.main_market);
        vehicleServer = (RadioButton) findViewById(R.id.main_vehicle_server);
        box = (RadioButton) findViewById(R.id.main_box);
        enterLogin = (ImageView) findViewById(R.id.enter_login);
        fragmentManager = getSupportFragmentManager();
        mainViewHolder = new MainViewHolder();
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        fixedCommon = new Common(getSharedPreferences(Constant.FIXED_FILE, Context.MODE_PRIVATE));
        context = MainActivity.this;
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        home.setOnClickListener(this);
        market.setOnClickListener(this);
        vehicleServer.setOnClickListener(this);
        box.setOnClickListener(this);
        enterLogin.setOnClickListener(this);
    }

    /**
     * 初始化左侧菜单
     */
    private void initLeftMenu() {
        setBehindContentView(R.layout.left_sliding_frame); //布局layout容器
        slidingMenu = getSlidingMenu();
        slidingMenu.setMode(SlidingMenu.LEFT);
        //触屏模式
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        //设置阴影宽度
        slidingMenu.setShadowWidth(3);
        //阴影图片宽度
        slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        //阴影样式
        slidingMenu.setShadowDrawable(R.drawable.left_sliding_shadow);
        //设置滑动菜单的宽度
        //slidingMenu.setBehindWidth(400);
        // 设置SlidingMenu菜单的宽度
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset); //SlidingMenu划出时主页面显示的剩余宽度
        // 设置渐入渐出效果的值
        slidingMenu.setFadeDegree(0.35f);
        leftMenuFragment = new LeftSlidingFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.left_sliding, leftMenuFragment).commit(); //页面添加到FrameLayout

        //设置右边（二级）侧滑菜单
//        slidingMenu.setSecondaryShadowDrawable(R.drawable.menu_left_shadow);
//        slidingMenu.setSecondaryMenu(R.layout.right_menu_frame);
//        Fragment rightMenuFragment = new MenuRightFragment();
//        getSupportFragmentManager().beginTransaction().replace(R.id.id_right_menu_frame, rightMenuFragment).commit();
    }

    /**
     * 显示左侧菜单
     *
     * @param view
     */
    public void showLeftMenu(View view) {
        getSlidingMenu().showMenu();
    }

    /**
     * 初始化主界面
     */
    private void initMainIndex() {
        mainViewHolder.indexFragment = new MainIndexFragment();
        fragmentManager.beginTransaction().replace(R.id.main_contain, mainViewHolder.indexFragment).commit();
    }

    /**
     * 初始化友盟服务
     */
    private void initUmengService() {
        //开启推送服务
        PushAgent pushAgent = PushAgent.getInstance(context);
        pushAgent.enable(); //开启推送
        pushAgent.onAppStart(); //统计应用启动

        //自动更新
        UmengUpdateAgent.setUpdateOnlyWifi(false); //关闭紧在wifi下更新
        UmengUpdateAgent.update(context); //开启自动更新

        //自定义通知栏样式
        UmengMessageHandler messageHandler = new UmengMessageHandler() {
            @Override
            public void dealWithCustomMessage(Context context, UMessage uMessage) {
                super.dealWithCustomMessage(context, uMessage);
            }

            @Override
            public void dealWithNotificationMessage(Context context, UMessage uMessage) {
                common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
                if (common.isLogin()) {
                    super.dealWithNotificationMessage(context, uMessage);
                }
            }
        };

        //自定义通知处理类
        UmengNotificationClickHandler umengNotificationClickHandler = new UmengNotificationClickHandler() {
            @Override
            public void dealWithCustomAction(Context context, UMessage uMessage) {
                super.dealWithCustomAction(context, uMessage);
            }

            @Override
            public void openActivity(Context context, UMessage uMessage) {
                super.openActivity(context, uMessage);
                Map<String, String> map = uMessage.extra;
                String messageType = map.get("messageType");
                Intent intent;
                if (messageType.equals(Constant.DRIVER_MESSAGE_TYPE)) { //司机消息
                    intent = new Intent(context, OrderOperateActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("order", 0);
                    startActivity(intent);
                } else if (messageType.equals(Constant.ENTERPRISE_MESSAGE_TYPE)) {
                    intent = new Intent(context, NoticeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }

            }
        };
        pushAgent.setMessageHandler(messageHandler); //自定义消息处理类
        pushAgent.setNotificationClickHandler(umengNotificationClickHandler); ////自定义通知处理类
        pushAgent.setMergeNotificaiton(false);  //合并通知消息 true始终只会看到一条消息
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_home:
                switchFooterMenu(1); //切换首页
                break;
            case R.id.main_market:
                switchFooterMenu(2); //切换油品
                break;
            case R.id.main_vehicle_server:
                switchFooterMenu(3); //切换运输服务
                break;
            case R.id.main_box:
                mainTitle.setText("百宝箱");
                switchFooterMenu(4); //切换百宝箱
                break;
            case R.id.enter_login: //登录入口
                if (common.isLogin()) {
                    common.showToast(context, "已经登录");
                    return;
                }
                Intent intent = new Intent(context, LoginActivity.class);
                startActivityForResult(intent, Constant.ACTIVITY_RETURN_CODE); //启动另外一个状态码
                break;
        }
    }

    /**
     * 暂存已经创建的Fragment
     */
    static class MainViewHolder {
        MainIndexFragment indexFragment; //首页
        MainOilFragment oilFragment; //油品
        Fragment transportFragment; //运输服务
        MainBoxFragment boxFragment; //百宝箱
    }

    /**
     * 用于接收Activity返回数据
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String enterpriseName;
        //登录返回
        if (resultCode == Constant.LOGIN_ACTIVITY_RETURN_CODE) {
            String userName = data.getExtras().getString("username");
            leftMenuFragment.adapterData(userName);
            //返回首页
            //判断首页是否已经缓存
            if (mainViewHolder.indexFragment == null) {
                //未缓存，创建新对象
                mainViewHolder.indexFragment = new MainIndexFragment();
            }
            fragmentManager.beginTransaction().replace(R.id.main_contain, mainViewHolder.indexFragment).commitAllowingStateLoss();
            enterLogin.setVisibility(View.GONE);
            home.setChecked(true);
        }
        //注册返回
        if (resultCode == Constant.REGISTER_ACTIVITY_RETURN_CODE) {
            String userName = data.getExtras().getString("username");
            leftMenuFragment.adapterData(userName);
            //返回首页
            //判断首页是否已经缓存
            if (mainViewHolder.indexFragment == null) {
                //未缓存，创建新对象
                mainViewHolder.indexFragment = new MainIndexFragment();
            }
            fragmentManager.beginTransaction().replace(R.id.main_contain, mainViewHolder.indexFragment).commitAllowingStateLoss();
            enterLogin.setVisibility(View.GONE);
            home.setChecked(true);
        }
        //账户类型返回
        if (resultCode == Constant.ACCOUNT_TYPE_RESULT_CODE) {
            int enterpriseId = Integer.valueOf(common.getStringByKey(Constant.ENTERPRISE_ID));
            enterpriseName = common.getStringByKey(Constant.ENTERPRISE_NAME);
            String roleName = common.getStringByKey(Constant.ROLE_NAME);
            leftMenuFragment.accountType.setText(enterpriseName);
            leftMenuFragment.roleType.setText(roleName);

            //判断MainActivity状态栏是否切换在运输服务，如果是则要更新公司角色
            if (vehicleServer.isChecked()) {
                //判断是否为个人账户
                if (enterpriseId == 0) {
                    //个人，切换司机角色
                    mainViewHolder.transportFragment = new TransportDriverFragment();
                    fragmentManager.beginTransaction().replace(R.id.main_contain, mainViewHolder.transportFragment).commitAllowingStateLoss();
                }
                new Thread(new WaitSwitchThread()).start();
            }
        }

        //运输服务角色选择返回更新
        if (resultCode == Constant.TRANSPORT_ROLE_CODE) {
            int transportRoleId = Integer.valueOf(fixedCommon.getStringByKey(Constant.TRANSPORT_ROLE));
            switch (transportRoleId) {
                case Constant.DRIVER_ROLE_ID: //司机
                    mainViewHolder.transportFragment = new TransportDriverFragment();
                    fragmentManager.beginTransaction().replace(R.id.main_contain, mainViewHolder.transportFragment).commitAllowingStateLoss();
                    break;
                case Constant.SHIPPER_ROLE_ID: //发货方
                    mainViewHolder.transportFragment = new TransportShipperFragment();
                    fragmentManager.beginTransaction().replace(R.id.main_contain, mainViewHolder.transportFragment).commitAllowingStateLoss();
                    break;
                case Constant.RECEIVER_ROLE_ID: //收货方
                    mainViewHolder.transportFragment = new TransportReceiverFragment();
                    fragmentManager.beginTransaction().replace(R.id.main_contain, mainViewHolder.transportFragment).commitAllowingStateLoss();
                    break;
                case Constant.UNDERTAKER_ROLE_ID: //承运方
                    mainViewHolder.transportFragment = new TransportUndertakeFragment();
                    fragmentManager.beginTransaction().replace(R.id.main_contain, mainViewHolder.transportFragment).commitAllowingStateLoss();
                    break;
            }
        }
    }


    /**
     * 按键监听
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //判断侧滑菜单是否显示
            if (!slidingMenu.isMenuShowing()) {
                //侧滑菜单不显示
                inTwoSecondsDBClickExit();
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 2秒内连续按返回键退出程序
     */
    public void inTwoSecondsDBClickExit() {
        if ((System.currentTimeMillis() - eOldTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
            eOldTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    /**
     * 等待切换
     */
    private class WaitSwitchThread implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(HANDLER_SWITCH_DURATION);
                Message message = new Message();
                message.what = HANDLER_ACCOUNT_TYPE_CODE;
                handler.sendMessage(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String enterpriseName = common.getStringByKey(Constant.ENTERPRISE_NAME);
            switch (msg.what) {
                case HANDLER_ACCOUNT_TYPE_CODE:
                    Class transportClass = mainViewHolder.transportFragment.getClass();
                    String className = transportClass.getSimpleName();
                    if (className.equals("TransportDriverFragment")) {
                        ((TransportDriverFragment) mainViewHolder.transportFragment).enterpriseName.setText(enterpriseName);
                    } else if (className.equals("TransportShipperFragment")) {
                        ((TransportShipperFragment) mainViewHolder.transportFragment).enterpriseName.setText(enterpriseName);
                    } else if (className.equals("TransportReceiverFragment")) {
                        ((TransportReceiverFragment) mainViewHolder.transportFragment).enterpriseName.setText(enterpriseName);
                    } else if (className.equals("TransportUndertakeFragment")) {
                        ((TransportUndertakeFragment) mainViewHolder.transportFragment).enterpriseName.setText(enterpriseName);
                    }
                    break;
            }
        }
    };

    /**
     * 退出操作
     */
    public void exitOperate() {
        mainTitle.setText("首页");
        //判断首页是否已经缓存
        if (mainViewHolder.indexFragment == null) {
            //未缓存，创建新对象
            mainViewHolder.indexFragment = new MainIndexFragment();
        }
        fragmentManager.beginTransaction().replace(R.id.main_contain, mainViewHolder.indexFragment).commit();
        home.setChecked(true);
        enterLogin.setVisibility(View.VISIBLE);
        stopTimer(); //停止定时
    }

    /**
     * 切换底部菜单栏
     *
     * @param index 底部菜单所以
     */
    private void switchFooterMenu(int index) {
        switch (index) {
            case 1: //首页
                mainTitle.setText("首页");
                //判断首页是否已经缓存
                if (mainViewHolder.indexFragment == null) {
                    //未缓存，创建新对象
                    mainViewHolder.indexFragment = new MainIndexFragment();
                }
                fragmentManager.beginTransaction().replace(R.id.main_contain, mainViewHolder.indexFragment).commitAllowingStateLoss();
                break;
            case 2: //油品
                if (!common.isLogin()) {
                    common.showToast(context, "用户未登录，请先登录");
                    return;
                }
                mainTitle.setText("油品");
                //判断首页是否已经缓存
                if (mainViewHolder.oilFragment == null) {
                    //未缓存，创建新对象
                    mainViewHolder.oilFragment = new MainOilFragment();
                }
                fragmentManager.beginTransaction().replace(R.id.main_contain, mainViewHolder.oilFragment).commitAllowingStateLoss();
                break;
            case 3: //运输服务
                mainTitle.setText("保险");
                if (!common.isLogin()) {
                    Toast.makeText(context, "用户未登录，请先登录", Toast.LENGTH_LONG).show();
//                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
//                    startActivityForResult(intent, 0x03);
                    return;
                }
                int enterpriseId = Integer.valueOf(common.getStringByKey(Constant.ENTERPRISE_ID)); //企业ID
                int transportRoleId = Integer.valueOf(fixedCommon.getStringByKey(Constant.TRANSPORT_ROLE)); //运输角色
                //判断是否为个人
                if (enterpriseId == 0) {
                    //个人只显示司机
                    mainViewHolder.transportFragment = new TransportDriverFragment();
                } else {
                    switchTransportRole(transportRoleId);
                }

                fragmentManager.beginTransaction().replace(R.id.main_contain, mainViewHolder.transportFragment).commitAllowingStateLoss();
                break;
            case 4: //百宝箱
                mainTitle.setText("百宝箱");
                //判断百宝箱是否已经缓存
                if (mainViewHolder.boxFragment == null) {
                    //未缓存，创建新对象
                    mainViewHolder.boxFragment = new MainBoxFragment();
                }
                fragmentManager.beginTransaction().replace(R.id.main_contain, mainViewHolder.boxFragment).commitAllowingStateLoss();
                break;
        }
    }

    /**
     * 切换运输角色
     *
     * @param transportRoleId
     */
    private void switchTransportRole(int transportRoleId) {
        switch (transportRoleId) {
            case 0: //司机
                mainViewHolder.transportFragment = new TransportDriverFragment();
                break;
            case 1: //收货方
                mainViewHolder.transportFragment = new TransportReceiverFragment();
                break;
            case 2: //发货方
                mainViewHolder.transportFragment = new TransportShipperFragment();
                break;
            case 3: //承运方
                mainViewHolder.transportFragment = new TransportUndertakeFragment();
                break;
        }
    }

    /**
     * 是否开启定时器
     */
    private void invokeTimer() {
        String typeCode = common.getStringByKey(Constant.TYPE_CODE); //用户代码
        if (typeCode != null && !typeCode.equals("")) {
            ServiceUtil.invokeTimerPOIService(context);
        }
    }

    /**
     * 结束定时器
     */
    public void stopTimer() {
        ServiceUtil.cancelAlarmManager(context);
    }
}
