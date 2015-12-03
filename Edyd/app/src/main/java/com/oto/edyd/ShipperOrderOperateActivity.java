package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.oto.edyd.model.ShipperOrderOperateItem;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

/**
 * 发货方在途订单Activity
 * Created by yql on 2015/12/3.
 */
public class ShipperOrderOperateActivity extends Activity implements View.OnClickListener {

    private LinearLayout back; //返回
    private ListView shipperOrderLists; //发货方订单操作列表

    private List<ShipperOrderOperateItem> shipperOrderOperateItemList = new ArrayList<ShipperOrderOperateItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shipper_order_operate);
        initFields();

        requestShipperOrderOperateData(); //请求发货方订单数据
        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: //返回
                finish();
                break;
        }
    }

    /**
     * 初始化订单数据
     */
    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
        shipperOrderLists = (ListView) findViewById(R.id.shipper_order_lists);
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
            shipperOrderOperateItem.setOrderFlowNumber("EDYD435234234"+i);
            shipperOrderOperateItem.setDistance("300" + i);
            shipperOrderOperateItem.setStartAndEndAddress(i + "---" + i + 1);
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
     * 订单
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

            if(convertView == null) {
                convertView = inflater.inflate(R.layout.shipper_order_operate_item, null);
                viewHolder = new ViewHolder();
                viewHolder.orderFlowNunber = (TextView) convertView.findViewById(R.id.order_flow_number);
                viewHolder.orderDistance = (TextView) convertView.findViewById(R.id.distance_load_goods);
                viewHolder.orderStatus = (ImageView) convertView.findViewById(R.id.order_status);
                viewHolder.startAndEndAddress = (TextView) convertView.findViewById(R.id.start_and_end_address);
                viewHolder.endAddress = (TextView) convertView.findViewById(R.id.end_address);
                viewHolder.receiver = (TextView) convertView.findViewById(R.id.receiver);
                viewHolder.phoneNunber = (TextView) convertView.findViewById(R.id.consignee_phone_number);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            ShipperOrderOperateItem shipperOrderOperateItem = shipperOrderOperateItemList.get(position);
            viewHolder.orderFlowNunber.setText(shipperOrderOperateItem.getOrderFlowNumber());
            viewHolder.orderDistance.setText(shipperOrderOperateItem.getDistance());
            //viewHolder.orderStatus
            viewHolder.startAndEndAddress.setText(shipperOrderOperateItem.getStartAndEndAddress());
            viewHolder.endAddress.setText(shipperOrderOperateItem.getEndAddress());
            viewHolder.receiver.setText(shipperOrderOperateItem.getReceiver());
            viewHolder.phoneNunber.setText(shipperOrderOperateItem.getPhoneNumber());

            return convertView;
        }
    }

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
