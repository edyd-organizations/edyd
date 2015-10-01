package com.oto.edyd;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.oto.edyd.lib.swiperefresh.PullToRefreshBase;
import com.oto.edyd.lib.swiperefresh.PullToRefreshScrollView;
import com.oto.edyd.widget.MenuListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yql on 2015/9/18.
 */
public class HistoryTransportOrderActivity extends Activity implements View.OnClickListener {

    private LinearLayout historyTransportOrderBack; //返回
    private MenuListView historyTransportOrderList; //历史运单list

    private PullToRefreshScrollView mPullToRefreshScrollView = null; //下拉组件
    private ScrollView mScrollView = null; //滚动视图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_transport_order);
        initFields();
        HistoryOrderAdapter historyOrderAdapter = new HistoryOrderAdapter(getApplicationContext());
        historyTransportOrderList.setAdapter(historyOrderAdapter);
        historyTransportOrderBack.setOnClickListener(this);
        mPullToRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ScrollView>() {
            /**
             * 刷新要做的操作
             * @param refreshView
             */
            @Override
            public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
                //mPullToRefreshScrollView.onRefreshComplete();停止刷新
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.history_transport_order_back:
                finish();
        }
    }

    private void initFields() {
        historyTransportOrderBack = (LinearLayout) findViewById(R.id.history_transport_order_back);
        historyTransportOrderList = (MenuListView) findViewById(R.id.history_order_list);

        mPullToRefreshScrollView = (PullToRefreshScrollView)findViewById(R.id.pull_refresh_scrollview_order);
        mScrollView = mPullToRefreshScrollView.getRefreshableView();
    }

    private class HistoryOrderAdapter extends BaseAdapter{
        TextView orderStatus; //订单状态
        private Context context;
        private LayoutInflater inflater;

        public HistoryOrderAdapter(Context context) {
            this.context = context;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = inflater.inflate(R.layout.history_order_item, null);
            orderStatus = (TextView) convertView.findViewById(R.id.order_status);
            otherVersion(orderStatus);

            Animation mAnimationRight = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_textview_one);
            mAnimationRight.setFillAfter(true);
            orderStatus.setAnimation(mAnimationRight); //旋转
            return convertView;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void otherVersion(TextView orderStatus) {
        orderStatus.setTranslationX(40); //平移，只有在API版本为11上才能运行
        orderStatus.setTranslationY(-20);
    }
}
