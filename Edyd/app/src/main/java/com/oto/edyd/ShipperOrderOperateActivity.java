package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.oto.edyd.model.ShipperOrderOperateItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能：运输服务发货方在途订单
 * 文件名：com.oto.edyd.ShipperOrderOperateActivity.java
 * 创建时间：2015/12/3.
 * 作者：yql
 */
public class ShipperOrderOperateActivity extends Activity implements View.OnClickListener, AbsListView.OnScrollListener {

    private LinearLayout back; //返回
    private ListView shipperOrderLists; //发货方订单操作列表
    private SwipeRefreshLayout swipeContainer; //下拉刷新控件
    private List<ShipperOrderOperateItem> shipperOrderOperateItemList = new ArrayList<ShipperOrderOperateItem>(); //订单集合
    private int visibleLastIndex = 0; //最后可视项索引
    private boolean loadFlag = false;
    private final static int ROWS = 10; //分页加载数据每页10
    private ShipperOrderOperateAdapter adapter; //适配器对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shipper_order_operate);
        init(); //初始化数据
        requestShipperOrderOperateData(); //请求发货方订单数据
    }

    /**
     * 初始化数据
     */
    private void init() {
        initFields();
        initListener();
    }

    /**
     * 初始化字段
     */
    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
        shipperOrderLists = (ListView) findViewById(R.id.shipper_order_lists);
        adapter = new ShipperOrderOperateAdapter(ShipperOrderOperateActivity.this);
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
    }

    /**
     * 初始化监听事件
     */
    private void initListener() {
        back.setOnClickListener(this);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //刷新操作

            }
        });
        shipperOrderLists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ShipperOrderOperateItem orderItem = shipperOrderOperateItemList.get(position);
                Intent intent = new Intent(ShipperOrderOperateActivity.this, ShipperOrderOperateItemDetailActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: //返回
                finish();
                break;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        int lastIndex = adapter.getCount(); //数据集最后一项的索引
        if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE  && visibleLastIndex ==lastIndex){
            //如果是自动加载,可以在这里放置异步加载数据的代码
            if(loadFlag) {
                loadFlag = false;
                if(lastIndex % ROWS == 0) {
                    int page = lastIndex / ROWS + 1;
                    //pageRequestTransportOderData(page, ROWS);
                }
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        visibleLastIndex = firstVisibleItem + visibleItemCount;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x20: //订单数据返回
                    shipperOrderLists.setAdapter(new ShipperOrderOperateAdapter(ShipperOrderOperateActivity.this));
                    break;
            }
        }
    };

    /**
     * 请求发货方在途订单
     */
    private void requestShipperOrderOperateData() {
//        Common common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
//        String sessionUUID = common.getStringByKey(Constant.SESSION_UUID);
//        String url = "";

        for(int i = 0; i <= 20; i++) {
            ShipperOrderOperateItem shipperOrderOperateItem = new ShipperOrderOperateItem();
            shipperOrderOperateItem.setOrderNumber("EDYD435234234"+i);
            shipperOrderOperateItem.setDistance("300" + i);
            shipperOrderOperateItem.setStartAndEndAddress(i + "---" + i + 1);
            shipperOrderOperateItem.setEndAddress("晋江市罗山市地税局");
            shipperOrderOperateItem.setReceiver("李四" + i);
            shipperOrderOperateItem.setPhoneNumber("1803986949" + i);
            shipperOrderOperateItem.setOrderStatus("20");
            shipperOrderOperateItemList.add(shipperOrderOperateItem);
        }

        Message msg = Message.obtain();
        msg.what = 0x20;
        handler.sendMessage(msg);
    }

    /**
     * 订单列表适配器
     */
    private class ShipperOrderOperateAdapter extends BaseAdapter{

        private LayoutInflater inflater;

        public ShipperOrderOperateAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return shipperOrderOperateItemList.size();
        }

        @Override
        public Object getItem(int position) {
            return shipperOrderOperateItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;

            //通过复用convertView来提高ListView性能
            if(convertView == null) {
                //如果convertView为空则创建
                convertView = inflater.inflate(R.layout.shipper_order_operate_item, null);
                //将布局中的对象存储在viewHolder中，避免再次查找，提高性能
                viewHolder = new ViewHolder();
                viewHolder.orderFlowNunber = (TextView) convertView.findViewById(R.id.order_flow_number);
                viewHolder.orderDistance = (TextView) convertView.findViewById(R.id.distance_load_goods);
                viewHolder.orderStatus = (ImageView) convertView.findViewById(R.id.order_status);
                viewHolder.startAndEndAddress = (TextView) convertView.findViewById(R.id.start_and_end_address);
                viewHolder.endAddress = (TextView) convertView.findViewById(R.id.end_address);
                viewHolder.receiver = (TextView) convertView.findViewById(R.id.receiver);
                viewHolder.phoneNunber = (TextView) convertView.findViewById(R.id.consignee_phone_number);
                //将viewHolder存储在convertView中
                convertView.setTag(viewHolder);
            } else {
                //converView不为空，取出viewHolder
                viewHolder = (ViewHolder) convertView.getTag();
            }

            //设置数据
            ShipperOrderOperateItem shipperOrderOperateItem = shipperOrderOperateItemList.get(position);
            viewHolder.orderFlowNunber.setText(shipperOrderOperateItem.getOrderNumber());
            viewHolder.orderDistance.setText(shipperOrderOperateItem.getDistance());
            //viewHolder.orderStatus
            viewHolder.startAndEndAddress.setText(shipperOrderOperateItem.getStartAndEndAddress());
            viewHolder.endAddress.setText(shipperOrderOperateItem.getEndAddress());
            viewHolder.receiver.setText(shipperOrderOperateItem.getReceiver());
            viewHolder.phoneNunber.setText(shipperOrderOperateItem.getPhoneNumber());
            return convertView;
        }
    }

    /**
     * 缓存list item对象
     */
    static class ViewHolder{
        TextView orderFlowNunber; //订单流水号
        TextView orderDistance; //距离装货地
        ImageView orderStatus; //订单状态
        TextView startAndEndAddress; //其实和结束地址
        TextView endAddress; //结束地址
        TextView receiver; //收货人
        TextView phoneNunber; //收货人联系电话
    }
}
