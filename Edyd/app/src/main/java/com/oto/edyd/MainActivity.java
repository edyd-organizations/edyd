package com.oto.edyd;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
    private List<Fragment> listFragment = new ArrayList<Fragment>(); //存储Fragment
    private RadioButton home; //首页
    private RadioButton market; //商城
    private RadioButton vehicleServer; //车辆服务
    private RadioButton box; //百宝箱
    public FragmentManager fragmentManager; //fragment管理器

    private Common common;
    private TextView mainTitle; //标题
    private LeftSlidingFragment leftMenuFragment;
    // 定义一个变量，来标识退出时间
    private long exitTime = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //设置ActionBar无标题
        setContentView(R.layout.activity_main);

        initFields(); //初始化字段
        initLeftMenu();//初始化侧滑栏
        initViewPager(); //初始化首页

        home.setOnClickListener(this);
        market.setOnClickListener(this);
        vehicleServer.setOnClickListener(this);
        box.setOnClickListener(this);

        UmengUpdateAgent.setUpdateOnlyWifi(false);
        UmengUpdateAgent.update(this);
    }

/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }*/

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
                // Common common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
                if (!common.isLogin()) {
                    Toast.makeText(getApplicationContext(), "用户未登录，请先登录", Toast.LENGTH_LONG).show();
                    intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivityForResult(intent, 0x03);
                    return;
                }
                //intent = new Intent(MainActivity.this, OrderOperateActivity.class);
                //startActivity(intent);
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
//        globalCommon = new Common(getSharedPreferences(Constant.GLOBAL_FILE, Context.MODE_PRIVATE));
//        Map<Object, Object> map = new HashMap<Object, Object>();
//        map.put(Constant.TRANSPORT_ROLE, 0); //默认运输角色，设置为司机，标识0
//        //保存账户ID
//        if (!globalCommon.isSave(map)) {
//            Toast.makeText(getApplicationContext(), "运输服务角色保存异常", Toast.LENGTH_SHORT).show();
//            return;
//        }
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
     * 初始化首页
     */
    private void initViewPager() {
        MainIndexFragment indexFragment = new MainIndexFragment();
        MainMarketFragment marketFragment = new MainMarketFragment();
        //MainVehicleServerFragment vehicleServerFragment = new MainVehicleServerFragment();
        TransportServiceFragment transportServiceFragment = new TransportServiceFragment();
        MainBoxFragment boxFragment = new MainBoxFragment();

        listFragment.add(indexFragment);
        listFragment.add(marketFragment);
        listFragment.add(transportServiceFragment);
        listFragment.add(boxFragment);

        mAdapter = new FragmentPagerAdapter(fragmentManager){
            @Override
            public int getCount() {
                return listFragment.size();
            }
            @Override
            public Fragment getItem(int position) {
                return listFragment.get(position);
            }
        };
        customViewPager.setAdapter(mAdapter);
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
        TransportServiceFragment transportServiceFragment;
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
            enterpriseName = common.getStringByKey(Constant.ENTERPRISE_NAME);
            transportServiceFragment = (TransportServiceFragment) listFragment.get(2);
            if (!(transportServiceFragment.enterpriseName == null)) {
                transportServiceFragment.enterpriseName.setText(enterpriseName);
            }
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
            transportServiceFragment = (TransportServiceFragment) listFragment.get(2);
            enterpriseName = common.getStringByKey(Constant.ENTERPRISE_NAME);
            if (!(transportServiceFragment.enterpriseName == null)) {
                transportServiceFragment.enterpriseName.setText(enterpriseName);
            }
        }
        //账户类型返回
        if (resultCode == Constant.ACCOUNT_TYPE_RESULT_CODE) {
            //String accountTypeStr = data.getExtras().getString("account_type");
            //Common common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
            enterpriseName = common.getStringByKey(Constant.ENTERPRISE_NAME);
            String roleName = common.getStringByKey(Constant.ROLE_NAME);

            leftMenuFragment.accountType.setText(enterpriseName);
            leftMenuFragment.roleType.setText(roleName);
            transportServiceFragment = (TransportServiceFragment) listFragment.get(2);
            if (!(transportServiceFragment.enterpriseName == null)) {
                transportServiceFragment.enterpriseName.setText(enterpriseName);
            }
        }

        //运输服务角色选择返回更新
        if (resultCode == Constant.TRANSPORT_ROLE_CODE) {
            Common globalCommon = new Common(getSharedPreferences(Constant.GLOBAL_FILE, Context.MODE_PRIVATE));
            transportServiceFragment = (TransportServiceFragment)listFragment.get(2);
            int transportRoleId = Integer.valueOf(globalCommon.getStringByKey(Constant.TRANSPORT_ROLE));
            switch (transportRoleId) {
                case 0: //司机
                    transportServiceFragment.transportRole.setText("司机");
                    break;
                case 1: //发货方
                    transportServiceFragment.transportRole.setText("发货方");
                    break;
                case 2: //收货方
                    transportServiceFragment.transportRole.setText("收货方");
                    break;
                case 3: //承运方
                    transportServiceFragment.transportRole.setText("承运方");
                    break;
            }
        }
    }


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

    class FragmentVPAdapter extends FragmentPagerAdapter {
        //private List<Fragment> listFragment;
        private FragmentManager fm;
        public FragmentVPAdapter(FragmentManager fm, List<Fragment> listFragment) {
            super(fm);
            this.fm = fm;
            //this.listFragment = listFragment;
        }

        public void setFragments(List<Fragment> fragments) {
            if(listFragment != null){
                FragmentTransaction ft = fm.beginTransaction();
                for(Fragment f : listFragment){
                    ft.remove(f);
                }
                ft.commitAllowingStateLoss();
                ft=null;
                fm.executePendingTransactions();
            }
            listFragment = fragments;
            notifyDataSetChanged();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int arg0) {
            return listFragment.get(arg0);
        }

        @Override
        public int getCount() {
            return listFragment.size();
        }
    }
}
