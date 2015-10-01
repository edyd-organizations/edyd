package com.oto.edyd;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yql on 2015/9/15.
 */
public class MyWayBillActivity extends FragmentActivity implements View.OnClickListener{

    private TabLayout mTabLayout;
    private ViewPager viewPager;

    private LayoutInflater mInflater;
    //private List<String> wayBillTitle = new ArrayList<>();//页卡标题集合
    private String[] wayBillTitle;
    private View notTransportView, transportingView, beenTransportView;//页卡视图
    private List<View> mViewList = new ArrayList<>();//页卡视图集合

    private ListView  notWayBillList; //第一个ListView
    private ListView ingWayBillList; //第二个ListView
    private ListView beenWayBillList; //第三个ListView

    private LinearLayout myWayBillBack; //返回
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_way_bill);
        initFields();

        //第一个
        mInflater = LayoutInflater.from(this);
        notTransportView = mInflater.inflate(R.layout.not_transport_way_bill_list, null);
        notWayBillList = (ListView) notTransportView.findViewById(R.id.not_way_bill_list);
        String notTransportContent[] = new String[]{"厦门湖里-广州", "苹果60吨", "张三", "2015-9-15 19：00"};
        List<Map<String, String>> dataSetsOne = new ArrayList<Map<String, String>>();
        int notTransportID[] = new int[] {R.id.bill_address, R.id.bill_ton, R.id.bill_username, R.id.bill_date};
        for(int i = 0; i <= 10; i++) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("bill_address", notTransportContent[0]);
            map.put("bill_ton", notTransportContent[1]);
            map.put("bill_username", notTransportContent[2]);
            map.put("bill_date", notTransportContent[3]);
            dataSetsOne.add(map);
        }
        SimpleAdapter simpleAdapterOne = new SimpleAdapter(getApplicationContext(), dataSetsOne, R.layout.my_way_bill_item,
                new String[]{"bill_address", "bill_ton", "bill_username", "bill_date"}, notTransportID); //ListView适配器
        notWayBillList.setAdapter(simpleAdapterOne);

        //第二个
        transportingView = mInflater.inflate(R.layout.transporting_way_bill_list, null);
        ingWayBillList = (ListView)transportingView.findViewById(R.id.ing_way_bill_list);
        ingWayBillList.setAdapter(simpleAdapterOne);

        beenTransportView = mInflater.inflate(R.layout.been_transport_way_bill_list, null);
        beenWayBillList = (ListView) beenTransportView.findViewById(R.id.been_way_bill_list);
        beenWayBillList.setAdapter(simpleAdapterOne);

        //添加页卡视图
        mViewList.add(notTransportView);
        mViewList.add(transportingView);
        mViewList.add(beenTransportView);
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);//设置tab模式，当前为系统默认模式
        mTabLayout.addTab(mTabLayout.newTab().setText(wayBillTitle[0]));//添加tab选项卡
        mTabLayout.addTab(mTabLayout.newTab().setText(wayBillTitle[1]));
        mTabLayout.addTab(mTabLayout.newTab().setText(wayBillTitle[2]));

        MyPagerAdapter mAdapter = new MyPagerAdapter(mViewList);
        viewPager.setAdapter(mAdapter);//给ViewPager设置适配器
        mTabLayout.setupWithViewPager(viewPager);//将TabLayout和ViewPager关联起来。
        mTabLayout.setTabsFromPagerAdapter(mAdapter);//给Tabs设置适配器

        myWayBillBack.setOnClickListener(this);
    }

    private void initFields() {
        mTabLayout = (TabLayout)findViewById(R.id.mTabLayout);
        viewPager = (ViewPager) findViewById(R.id.mViewPager);
        myWayBillBack = (LinearLayout) findViewById(R.id.my_way_bill_back);
        wayBillTitle = getResources().getStringArray(R.array.my_way_bill);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_way_bill_back:
                finish();
                break;
        }
    }

    //ViewPager适配器
    private class MyPagerAdapter extends PagerAdapter {
        private List<View> mViewList;

        public MyPagerAdapter(List<View> mViewList) {
            this.mViewList = mViewList;
        }

        @Override
        public int getCount() {
            return mViewList.size();//页卡数
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;//官方推荐写法
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mViewList.get(position));//添加页卡
            return mViewList.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mViewList.get(position));//删除页卡
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return wayBillTitle[position];//页卡标题
        }

    }
}
