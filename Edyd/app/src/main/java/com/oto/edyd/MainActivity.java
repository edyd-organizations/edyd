package com.oto.edyd;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.Toast;

import com.oto.edyd.lib.slidingmenu.SlidingMenu;
import com.oto.edyd.lib.slidingmenu.app.SlidingFragmentActivity;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.widget.CustomViewPager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends SlidingFragmentActivity implements View.OnClickListener{

    private CustomViewPager customViewPager;
    private FragmentPagerAdapter mAdapter; //ViewPager适配器
    private List<Fragment> listFragment = new ArrayList<Fragment>(); //存储Fragment
    private RadioButton home; //首页
    private RadioButton market; //商城
    private RadioButton vehicleServer; //车辆服务
    private RadioButton box; //百宝箱
    public FragmentManager fragmentManager; //fragment管理器

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
     * @param v
     */
    @Override
    public void onClick(View v) {
        Intent intent;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        switch (v.getId()){
            case R.id.main_home:
                customViewPager.setCurrentItem(0);
                break;
            case R.id.main_market:
                customViewPager.setCurrentItem(1);
                //intent = new Intent(MainActivity.this, LocationActivity.class);
                //startActivity(intent);
                break;
            case R.id.main_vehicle_server:
                //customViewPager.setCurrentItem(2);
                Common common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
                if (!common.isLogin()) {
                    Toast.makeText(getApplicationContext(), "用户未登录，请先登录", Toast.LENGTH_LONG).show();
                    //intent = new Intent(MainActivity.this, LoginActivity.class);
                    //startActivity(intent);
                    return;
                }
                intent = new Intent(MainActivity.this, OrderOperateActivity.class);
                startActivity(intent);
                break;
            case R.id.main_box:
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
        home = (RadioButton)findViewById(R.id.main_home); //底部菜单-首页
        market = (RadioButton)findViewById(R.id.main_market); //底部菜单-商城
        vehicleServer = (RadioButton)findViewById(R.id.main_vehicle_server); //底部菜单-交通服务
        box = (RadioButton)findViewById(R.id.main_box); //底部菜单-百宝箱
        customViewPager = (CustomViewPager)findViewById(R.id.content_viewpager); //获取ViewPager实现多页面滑动
        customViewPager.setIsScrollable(false); //设置不能滚动
        fragmentManager = getSupportFragmentManager();
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
        Fragment leftMenuFragment = new LeftSlidingFragment();
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
    private  void initViewPager() {
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
     * @param view
     */
    public void showLeftMenu(View view) {
        getSlidingMenu().showMenu();
    }
}
