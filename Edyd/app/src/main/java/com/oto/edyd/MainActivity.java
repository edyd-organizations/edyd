package com.oto.edyd;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.lib.slidingmenu.SlidingMenu;
import com.oto.edyd.lib.slidingmenu.app.SlidingFragmentActivity;
import com.oto.edyd.usercenter.activity.LoginActivity;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.widget.CustomViewPager;
import com.umeng.update.UmengUpdateAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends SlidingFragmentActivity implements View.OnClickListener {

    private CustomViewPager customViewPager;
    private FragmentPagerAdapter mAdapter; //ViewPager适配器
    public EFragmentAdapter eAdapter;
    private List<Fragment> listFragment = new ArrayList<Fragment>(); //存储Fragment
    private RadioButton home; //首页
    private RadioButton market; //商城
    private RadioButton vehicleServer; //车辆服务
    private RadioButton box; //百宝箱
    public FragmentManager fragmentManager; //fragment管理器

    private Common common;
    //private Common globalCommon;
    private Common fixedCommon;
    private TextView mainTitle; //标题
    private LeftSlidingFragment leftMenuFragment;
    private long exitTime = 0; // 定义一个变量，来标识退出时间

    boolean[] fragmentsUpdateFlag = { false, false, false, false };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //设置ActionBar无标题
        setContentView(R.layout.activity_main);

        initFields(); //初始化字段
        initLeftMenu();//初始化侧滑栏
        initViewPager(); //初始化ViewPager

        home.setOnClickListener(this);
        market.setOnClickListener(this);
        vehicleServer.setOnClickListener(this);
        box.setOnClickListener(this);

        UmengUpdateAgent.setUpdateOnlyWifi(false);
        UmengUpdateAgent.update(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 处理底部菜单点击事件,根据不同按钮刷新不同页面
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        Intent intent;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        switch (v.getId()) {
            case R.id.main_home:
                mainTitle.setText("首页");
                customViewPager.setCurrentItem(0);
                break;
            case R.id.main_market:
                mainTitle.setText("商城");
                customViewPager.setCurrentItem(1);
                //intent = new Intent(MainActivity.this, WaitBuild.class);
                //startActivity(intent);
                break;
            case R.id.main_vehicle_server:
                mainTitle.setText("运输服务");
                if (!common.isLogin()) {
                    Toast.makeText(getApplicationContext(), "用户未登录，请先登录", Toast.LENGTH_LONG).show();
                    intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivityForResult(intent, 0x03);
                    return;
                }
                customViewPager.setCurrentItem(2);
                break;
            case R.id.main_box:
                mainTitle.setText("百宝箱");
                customViewPager.setCurrentItem(3);
                break;
            default:
                Toast.makeText(getApplicationContext(), "错误导航", Toast.LENGTH_SHORT);
                break;
        }
    }

    /**
     * 初始化数据
     */
    private void initFields() {
        home = (RadioButton) findViewById(R.id.main_home); //底部菜单-首页
        market = (RadioButton) findViewById(R.id.main_market); //底部菜单-商城
        vehicleServer = (RadioButton) findViewById(R.id.main_vehicle_server); //底部菜单-交通服务
        box = (RadioButton) findViewById(R.id.main_box); //底部菜单-百宝箱
        customViewPager = (CustomViewPager) findViewById(R.id.content_viewpager); //获取ViewPager实现多页面滑动
        customViewPager.setIsScrollable(false); //设置不能滚动
        fragmentManager = getSupportFragmentManager();
        mainTitle = (TextView) findViewById(R.id.main_title);
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        //globalCommon = new Common(getSharedPreferences(Constant.GLOBAL_FILE, Context.MODE_PRIVATE));
        fixedCommon = new Common(getSharedPreferences(Constant.FIXED_FILE, Context.MODE_PRIVATE));
    }

    /**
     * 初始化左侧菜单
     */
    private void initLeftMenu() {
        setBehindContentView(R.layout.left_sliding_frame); //布局layout容器
        SlidingMenu slidingMenu = getSlidingMenu();
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
     * 初始化ViewPager
     */
    private void initViewPager() {
        MainIndexFragment indexFragment = new MainIndexFragment();
        MainMarketFragment marketFragment = new MainMarketFragment();
        //TransportUndertakeFragment transportServiceFragment = new TransportUndertakeFragment();
        MainBoxFragment boxFragment = new MainBoxFragment();

        listFragment.add(indexFragment);
        listFragment.add(marketFragment);
        String txTransportId = fixedCommon.getStringByKey(Constant.TRANSPORT_ROLE);
        if(txTransportId != null && !txTransportId.equals("")) {
            int transportRoleId = Integer.valueOf(txTransportId);
            switch (transportRoleId) {
                case Constant.DRIVER_ROLE_ID: //司机
                    TransportDriverFragment transportDriverFragment = new TransportDriverFragment();
                    listFragment.add(transportDriverFragment);
                    break;
                case Constant.SHIPPER_ROLE_ID: //发货方
                    //listFragment.add(transportServiceFragment);
                    TransportShipperFragment transportShipperFragment = new TransportShipperFragment();
                    listFragment.add(transportShipperFragment);
                    break;
                case Constant.RECEIVER_ROLE_ID: //收货方
                    //listFragment.add(transportServiceFragment);
                    TransportReceiverFragment transportReceiverFragment = new TransportReceiverFragment();
                    listFragment.add(transportReceiverFragment);
                    break;
                case Constant.UNDERTAKER_ROLE_ID: //承运方
                    TransportUndertakeFragment transportServiceFragment = new TransportUndertakeFragment();
                    listFragment.add(transportServiceFragment);
                    break;

            }
        } else {
            TransportDriverFragment transportDriverFragment = new TransportDriverFragment();
            listFragment.add(transportDriverFragment);
        }


        listFragment.add(boxFragment);

//        mAdapter = new FragmentPagerAdapter(fragmentManager){
//            @Override
//            public int getCount() {
//                return listFragment.size();
//            }
//            @Override
//            public Fragment getItem(int position) {
//                return listFragment.get(position);
//            }
//        };
        eAdapter = new EFragmentAdapter(fragmentManager);
        customViewPager.setAdapter(eAdapter);
    }

    /**
     * 显示左侧菜单
     *
     * @param view
     */
    public void showLeftMenu(View view) {
        getSlidingMenu().showMenu();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
        TransportUndertakeFragment transportServiceFragment;
        //登录返回
        if (resultCode == Constant.LOGIN_ACTIVITY_RETURN_CODE) {
            String username = data.getExtras().getString("username");
            leftMenuFragment.userAlias.setText(username);
            leftMenuFragment.exit.setVisibility(View.VISIBLE);
            leftMenuFragment.slidingBottomLine.setVisibility(View.VISIBLE);
            //Common common = new Common(getActivity().getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
            //String enterpriseName = common.getStringByKey(Constant.ENTERPRISE_NAME);
            //if(enterpriseName != null) {
            leftMenuFragment.accountType.setText("个人");
            //}

            leftMenuFragment.dataSets.clear();
            for (int i = 0; i < leftMenuFragment.textResources.length; i++) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("list_image", leftMenuFragment.imageResources[i]);
                map.put("list_text", leftMenuFragment.textResources[i]);
                map.put("list_arrow", R.mipmap.right_arrow);
                leftMenuFragment.dataSets.add(map);
            }
            leftMenuFragment.simpleAdapter.notifyDataSetChanged();
            comeBackIndex();
//            enterpriseName = common.getStringByKey(Constant.ENTERPRISE_NAME);
//            int transportRoleId = Integer.valueOf(fixedCommon.getStringByKey(Constant.TRANSPORT_ROLE));
//            switch (transportRoleId) {
//                case 0: //司机
//                    //TransportDriverFragment transportDriverFragment = new TransportDriverFragment();
//                    TransportDriverFragment transportDriverFragment = (TransportDriverFragment) listFragment.get(2);
//                    transportDriverFragment.enterpriseName.setText(enterpriseName);
//                    break;
//                case 1: //发货方
//                    //TransportShipperFragment transportShipperFragment = new TransportShipperFragment();
//                    TransportShipperFragment transportShipperFragment = (TransportShipperFragment) listFragment.get(2);
//                    transportShipperFragment.enterpriseName.setText(enterpriseName);
//                    break;
//                case 2: //收货方
//                    //TransportReceiverFragment transportReceiverFragment = new TransportReceiverFragment();
//                    TransportReceiverFragment transportReceiverFragment = (TransportReceiverFragment) listFragment.get(2);
//                    transportReceiverFragment.enterpriseName.setText(enterpriseName);
//                    break;
//                case 3: //承运方
//                    //TransportUndertakeFragment transportUndertakeFragment = new TransportUndertakeFragment();
//                    TransportUndertakeFragment transportUndertakeFragment = (TransportUndertakeFragment) listFragment.get(2);
//                    transportUndertakeFragment.enterpriseName.setText(enterpriseName);
//                    break;
//            }
        }
        //注册返回
        if (resultCode == Constant.REGISTER_ACTIVITY_RETURN_CODE) {
            String username = data.getExtras().getString("username");
            leftMenuFragment.userAlias.setText(username);
            leftMenuFragment.accountType.setText("个人");
            leftMenuFragment.exit.setVisibility(View.VISIBLE);
            leftMenuFragment.slidingBottomLine.setVisibility(View.VISIBLE);
            leftMenuFragment.dataSets.clear();
            for (int i = 0; i < leftMenuFragment.textResources.length; i++) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("list_image", leftMenuFragment.imageResources[i]);
                map.put("list_text", leftMenuFragment.textResources[i]);
                map.put("list_arrow", R.mipmap.right_arrow);
                leftMenuFragment.dataSets.add(map);
            }
            leftMenuFragment.simpleAdapter.notifyDataSetChanged();
//            enterpriseName = common.getStringByKey(Constant.ENTERPRISE_NAME);
//            int transportRoleId = Integer.valueOf(fixedCommon.getStringByKey(Constant.TRANSPORT_ROLE));
//            switch (transportRoleId) {
//                case 0: //司机
//                    //TransportDriverFragment transportDriverFragment = new TransportDriverFragment();
//                    TransportDriverFragment transportDriverFragment = (TransportDriverFragment) listFragment.get(2);
//                    transportDriverFragment.enterpriseName.setText(enterpriseName);
//                    break;
//                case 1: //发货方
//                    //TransportShipperFragment transportShipperFragment = new TransportShipperFragment();
//                    TransportShipperFragment transportShipperFragment = (TransportShipperFragment) listFragment.get(2);
//                    transportShipperFragment.enterpriseName.setText(enterpriseName);
//                    break;
//                case 2: //收货方
//                    //TransportReceiverFragment transportReceiverFragment = new TransportReceiverFragment();
//                    TransportReceiverFragment transportReceiverFragment = (TransportReceiverFragment) listFragment.get(2);
//                    transportReceiverFragment.enterpriseName.setText(enterpriseName);
//                    break;
//                case 3: //承运方
//                    //TransportUndertakeFragment transportUndertakeFragment = new TransportUndertakeFragment();
//                    TransportUndertakeFragment transportUndertakeFragment = (TransportUndertakeFragment) listFragment.get(2);
//                    transportUndertakeFragment.enterpriseName.setText(enterpriseName);
//                    break;
//            }
        }
        //账户类型返回
        if (resultCode == Constant.ACCOUNT_TYPE_RESULT_CODE) {
            //String accountTypeStr = data.getExtras().getString("account_type");
            //Common common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
            enterpriseName = common.getStringByKey(Constant.ENTERPRISE_NAME);
            String roleName = common.getStringByKey(Constant.ROLE_NAME);

            leftMenuFragment.accountType.setText(enterpriseName);
            leftMenuFragment.roleType.setText(roleName);
            int transportRoleId = Integer.valueOf(fixedCommon.getStringByKey(Constant.TRANSPORT_ROLE));
            switch (transportRoleId) {
                case Constant.DRIVER_ROLE_ID: //司机
                    TransportDriverFragment transportDriverFragment = (TransportDriverFragment) listFragment.get(2);
                    if (!(transportDriverFragment.enterpriseName == null)) {
                        transportDriverFragment.enterpriseName.setText(enterpriseName);
                    }
                    break;
                case Constant.SHIPPER_ROLE_ID: //发货方
                    TransportShipperFragment transportShipperFragment = (TransportShipperFragment) listFragment.get(2);
                    if (!(transportShipperFragment.enterpriseName == null)) {
                        transportShipperFragment.enterpriseName.setText(enterpriseName);
                    }
                    break;
                case Constant.RECEIVER_ROLE_ID: //收货方
                    TransportReceiverFragment transportReceiverFragment = (TransportReceiverFragment) listFragment.get(2);
                    if (!(transportReceiverFragment.enterpriseName == null)) {
                        transportReceiverFragment.enterpriseName.setText(enterpriseName);
                    }
                    break;
                case Constant.UNDERTAKER_ROLE_ID: //承运方
                    TransportUndertakeFragment transportUndertakeFragment = (TransportUndertakeFragment) listFragment.get(2);
                    if (!(transportUndertakeFragment.enterpriseName == null)) {
                        transportUndertakeFragment.enterpriseName.setText(enterpriseName);
                    }
                    break;
            }
        }

        //运输服务角色选择返回更新
        if (resultCode == Constant.TRANSPORT_ROLE_CODE) {
            //transportServiceFragment = (TransportServiceFragment)listFragment.get(2);
            int transportRoleId = Integer.valueOf(fixedCommon.getStringByKey(Constant.TRANSPORT_ROLE));
            switch (transportRoleId) {
                case Constant.DRIVER_ROLE_ID: //司机
                    //transportServiceFragment.transportRole.setText("司机");
                    TransportDriverFragment transportDriverFragment = new TransportDriverFragment();
                    listFragment.set(2, transportDriverFragment);
                    fragmentsUpdateFlag[2] = true;
                    eAdapter.notifyDataSetChanged();
                    break;
                case Constant.SHIPPER_ROLE_ID: //发货方
                    //transportServiceFragment.transportRole.setText("发货方");
                    TransportShipperFragment transportShipperFragment = new TransportShipperFragment();
                    listFragment.set(2, transportShipperFragment);
                    fragmentsUpdateFlag[2] = true;
                    eAdapter.notifyDataSetChanged();
                    break;
                case Constant.RECEIVER_ROLE_ID: //收货方
                    //transportServiceFragment.transportRole.setText("收货方");
                    TransportReceiverFragment transportReceiverFragment = new TransportReceiverFragment();
                    listFragment.set(2, transportReceiverFragment);
                    fragmentsUpdateFlag[2] = true;
                    eAdapter.notifyDataSetChanged();
                    break;
                case Constant.UNDERTAKER_ROLE_ID: //承运方
                    //transportServiceFragment.transportRole.setText("承运方");
                    TransportUndertakeFragment transportUndertakeFragment = new TransportUndertakeFragment();
                    listFragment.set(2, transportUndertakeFragment);
                    fragmentsUpdateFlag[2] = true;
                    eAdapter.notifyDataSetChanged();
                    break;
            }
        }
    }

    /**
     * 按键监听
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 2秒内连续按返回键退出程序
     */
    public void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    /**
     * FragmentPager刷新Fragment
     * FragmentPager适配器
     */
    class EFragmentAdapter extends FragmentPagerAdapter {
        FragmentManager fm;

        EFragmentAdapter(FragmentManager fm) {
            super(fm);
            this.fm = fm;
        }

        @Override
        public int getCount() {
            return listFragment.size();
        }

        /**
         * 返回指定位置Fragment
         * @param position
         * @return
         */
        @Override
        public Fragment getItem(int position) {
            return listFragment.get(position);
        }

        /**
         * 这里存在POSITION_UNCHANGED和POSITION_NONE这两种情况
         * 1、返回POSITION_UNCHANGED表示item位置没有变换，会使用FragmentManager缓存中的Fragment,所以
         * 即使调用了notifyDataSetChanged没有刷新数据的效果
         * 2、返回POSITION_NONE表示item不存在，需要重新绘制，此时会从新加载数据源中的数据
         * @param object
         * @return
         */
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            //得到缓存的fragment
            Fragment fragment = (Fragment) super.instantiateItem(container,
                    position);
            //得到tag，这点很重要
            String fragmentTag = fragment.getTag();

            if (fragmentsUpdateFlag[position]) {
                //如果这个fragment需要更新
                FragmentTransaction ft = fm.beginTransaction();
                //移除旧的fragment
                ft.remove(fragment);
                //换成新的fragment
                fragment = listFragment.get(position);
                //添加新fragment时必须用前面获得的tag，这点很重要
                ft.add(container.getId(), fragment, fragmentTag);
                ft.attach(fragment);
                ft.commitAllowingStateLoss();
                ft = null;
                //复位更新标志
                fragmentsUpdateFlag[position] = false;
            }

            return fragment;
        }
    }

    /**
     * 返回首页
     */
    public void comeBackIndex() {
        customViewPager.setCurrentItem(0);
        home.setChecked(true);
    }
}
