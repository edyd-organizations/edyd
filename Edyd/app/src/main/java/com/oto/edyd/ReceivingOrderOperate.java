package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;

import java.util.ArrayList;
import java.util.List;

public class ReceivingOrderOperate extends Activity implements View.OnClickListener {
    private LinearLayout historyTransportOrderBack; //返回
    private ListView receiveOrderList; //接单
    private CusProgressDialog receiveOrderDialog; //页面切换过度
    private SwipeRefreshLayout mPullToRefreshScrollView = null; //下拉组件
    private ReceiveOrderListAdapter receiveOrderListAdapter; //自定义适配器
    private Common common;
    private List<Integer> idList = new ArrayList<Integer>(); //ID集合
    private List<Integer> primaryIdList = new ArrayList<Integer>(); //主键ID集合
    private List<String> orderList = new ArrayList<String>(); //订单集合
    private List<String> senderAddressList = new ArrayList<String>(); //发货方地址集合
    private List<String> senderList = new ArrayList<String>();//发货人集合
    private List<String> phoneNumberList = new ArrayList<String>(); //发货人联系电话集合
    private List<Integer> orderStatusList = new ArrayList<Integer>(); //订单状态
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiving_order_operate);
        initFields();
    }

    /**
     * 初始化数据
     */
    private void initFields() {
       /* historyTransportOrderBack = (LinearLayout) findViewById(R.id.history_transport_order_back);
        historyTransportOrderBack.setOnClickListener(this);*/
        receiveOrderList = (ListView) findViewById(R.id.receive_order_list);
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        mPullToRefreshScrollView = (SwipeRefreshLayout)findViewById(R.id.swipe_container);
    }

    private class ReceiveOrderListAdapter extends BaseAdapter {

        private Context context;
        private LayoutInflater inflater;

        public ReceiveOrderListAdapter(Context context) {
            this.context = context;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return idList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

//        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//        public void otherVersion(TextView orderStatus) {
//            orderStatus.setTranslationX(40); //平移，只有在API版本为11上才能运行
//            orderStatus.setTranslationY(-20);
//        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView orderNumber; //订单号
            TextView tvDistance; //距离
            TextView fromTo;//从哪里到哪里
            TextView startPoint; //发货地址
            TextView shipper; //发货人
            TextView phoneNumber; //发货人联系电话
            //TextView receiveOrder; //接单
            ImageView orderStatus; //单子状态

            convertView = inflater.inflate(R.layout.receiving_order_operation_item, null);

            orderNumber = (TextView) convertView.findViewById(R.id.orderNumView);
            tvDistance = (TextView) convertView.findViewById(R.id.tv_distance);
            fromTo = (TextView) convertView.findViewById(R.id.from_to);
            startPoint = (TextView) convertView.findViewById(R.id.tv_addr_detail); //发货地址
            shipper = (TextView) convertView.findViewById(R.id.shipper_name); //发货人
            phoneNumber = (TextView) convertView.findViewById(R.id.phone_number_one); //发货人联系电话
            orderStatus = (ImageView) convertView.findViewById(R.id.order_status); //订单状态

            switch (orderStatusList.get(position)) {
//                case 17: //未接单
//                    receiveOrder.setText(getString(R.string.receive_order));
//                    break;
//                case 20: //已接单
//                    orderStatus.setImageResource(R.mipmap.tts_loading_way);
//                    receiveOrder.setText("到达装货");
//                    break;
//                case 30: //	到达装货
//                    orderStatus.setImageResource(R.mipmap.tts_arrived_load);
//                    receiveOrder.setText("装货完成");
//                    break;
//                case 40: //装货完成
//                    orderStatus.setImageResource(R.mipmap.tts_completion_load);
//                    receiveOrder.setText("送货在途");
//                    break;
//                case 50: //送货在途
//                    orderStatus.setImageResource(R.mipmap.tts_delivery_way);
//                    receiveOrder.setText("到达收货");
//                    break;
//                case 60: //到达收货
//                    orderStatus.setImageResource(R.mipmap.tts_arrived_receive);
//                    receiveOrder.setText("收货完成");
//                    break;
                case 99: //收货完成
                    orderStatus.setImageResource(R.mipmap.finished_receive);
                    break;
            }

            orderNumber.setText(orderList.get(position));
            startPoint.setText(senderAddressList.get(position));
            shipper.setText(senderList.get(position));
            phoneNumber.setText(phoneNumberList.get(position));
            shipper.setText(senderList.get(position));
            phoneNumber.setText(phoneNumberList.get(position));
            return convertView;
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    receiveOrderListAdapter = new ReceiveOrderListAdapter(getApplicationContext());
                    receiveOrderList.setAdapter(receiveOrderListAdapter);
                    receiveOrderDialog.getLoadingDialog().dismiss();
                    //setListViewHeightBasedOnChildren(receiveOrderList);
                    break;
                case 2:
                    receiveOrderListAdapter = new ReceiveOrderListAdapter(getApplicationContext());
                    receiveOrderList.setAdapter(receiveOrderListAdapter);
                    mPullToRefreshScrollView.setRefreshing(false); //停止刷新
                    break;
                case 3:
                    receiveOrderListAdapter.notifyDataSetChanged(); //通知ListView更新
                    //loadMoreButton.setText("加载更多");
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
          /*  case R.id.history_transport_order_back:
                finish();*/
        }
    }
}
