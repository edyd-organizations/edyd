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
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.oto.edyd.model.OilDistributeDetail;
import com.oto.edyd.model.OilDistributeDetailTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yql on 2015/11/3.
 */
public class OilDistributeDetailActivity extends Activity implements View.OnClickListener {

    private LinearLayout back; //返回
    private TextView typeCardOrCarNumber; //卡号或者车牌号
    private TextView search; //查找
    private TextView cardNumber; //卡号
    private TextView carNumber; //车牌号
    private TextView balance; //余额
    private TextView lastTime; //最后加油时间
    private ExpandableListView distributeDetailList; //分配明细列表

    private List<OilDistributeDetail> oilDistributeDetails = new ArrayList<OilDistributeDetail>(); //列表数据
    Map<Integer, OilDistributeDetailTime> oilDistributeDetailTimeMap = new HashMap<Integer, OilDistributeDetailTime>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oil_card_distribute_detail);
        initFields(); //初始化数据
        requestDistributeUserList(); //请求列表数据

        back.setOnClickListener(this);
    }

    /**
     * 数据初始化
     */
    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
        typeCardOrCarNumber = (TextView) findViewById(R.id.type_car_number_or_card);
        search = (TextView) findViewById(R.id.search);
        cardNumber = (TextView) findViewById(R.id.card_number);
        carNumber = (TextView) findViewById(R.id.car_number);
        balance = (TextView) findViewById(R.id.balance);
        lastTime = (TextView) findViewById(R.id.last_time);
        distributeDetailList = (ExpandableListView) findViewById(R.id.distribute_detail_list);
        distributeDetailList.setGroupIndicator(null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x12: //油卡金额数据返回执行
                    distributeDetailList.setAdapter(new OilCardDistributeDetailAdapter(getApplicationContext()));
                    break;
            }
        }
    };

    /**
     * 请求预分配用户列表
     */
    private void requestDistributeUserList() {

        //假数据
        for(int i = 0; i < 10; i++) {
            OilDistributeDetail oilDistributeDetail = new OilDistributeDetail();
            oilDistributeDetail.setCar("闽F324" + i);
            oilDistributeDetail.setCard("535734673" + i);
            oilDistributeDetail.setBalance("300" + i);
            oilDistributeDetail.setStatus("完成");
            oilDistributeDetails.add(oilDistributeDetail);

            OilDistributeDetailTime oilDistributeDetailTime = new OilDistributeDetailTime();
            oilDistributeDetailTime.setApplyTime("2015-11-3 14:18:07");
            oilDistributeDetailTime.setCompletedTime("2015-11-3 14:18:07");
            oilDistributeDetailTimeMap.put(i, oilDistributeDetailTime);
        }


        Message message = new Message();
        message.what = 0x12;
        handler.sendMessage(message);
    }

    private class OilCardDistributeDetailAdapter extends BaseExpandableListAdapter {

        private Context context;
        private LayoutInflater inflater;

        private OilCardDistributeDetailAdapter(Context context) {
            this.context = context;
            inflater = LayoutInflater.from(context);
        }
        /**
         * 获取组的个数
         * @return
         */
        @Override
        public int getGroupCount() {
            return oilDistributeDetails.size();
        }

        /**
         * 获取指定组中的子元素个数
         * @param groupPosition
         * @return
         */
        @Override
        public int getChildrenCount(int groupPosition) {
            return 1;
        }

        /**
         * 获取指定组中的数据
         * @param groupPosition
         * @return
         */
        @Override
        public Object getGroup(int groupPosition) {
            return oilDistributeDetails.get(groupPosition);
        }

        /**
         * 获取指定组中的指定子元素数据。
         * @param groupPosition
         * @param childPosition
         * @return
         */
        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return oilDistributeDetailTimeMap.get(childPosition);
        }

        /**
         * 获取指定组中的指定子元素ID，这个ID在组里一定是唯一的。
         * @param groupPosition
         * @param childPosition
         * @return
         */
        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        /**
         * 获取指定组的ID，这个组ID必须是唯一的。
         * @param groupPosition
         * @return
         */
        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        /**
         * 获取显示指定组的视图对象。
         * @param groupPosition
         * @param isExpanded
         * @param convertView
         * @param parent
         * @return
         */
        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            TextView tCarNumber; //车牌号
            TextView tCardNumber; //卡号
            TextView tAmount; //金额
            TextView status; //状态
            ImageView iUpOrDown;// 图标指示器

            View view = inflater.inflate(R.layout.oil_card_distribute_detail_parent_item, null);
            tCarNumber = (TextView) view.findViewById(R.id.car_number);
            tCardNumber = (TextView) view.findViewById(R.id.card_number);
            tAmount = (TextView) view.findViewById(R.id.balance);
            status = (TextView) view.findViewById(R.id.oil_status);
            iUpOrDown = (ImageView) view.findViewById(R.id.ic_up_or_down);

            OilDistributeDetail oilDistributeDetail = oilDistributeDetails.get(groupPosition);
            tCarNumber.setText(oilDistributeDetail.getCar());
            tCardNumber.setText(oilDistributeDetail.getCard());
            tAmount.setText(oilDistributeDetail.getBalance());
            status.setText(oilDistributeDetail.getStatus());

            //是否展开设置不同的图片
            if(isExpanded) {
                iUpOrDown.setImageResource(R.mipmap.up_arrow);
            } else {
                iUpOrDown.setImageResource(R.mipmap.down_arrow);
            }
            return view;
        }

        /**
         * 获取一个视图对象，显示指定组中的指定子元素数据。
         * @param groupPosition
         * @param childPosition
         * @param isLastChild
         * @param convertView
         * @param parent
         * @return
         */
        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            TextView tApplyTime; //申请时间
            TextView tCompletedTime; //完成时间

            View view = inflater.inflate(R.layout.oil_card_distribute_detail_child_item, null);
            tApplyTime = (TextView) view.findViewById(R.id.apply_time);
            tCompletedTime = (TextView) view.findViewById(R.id.completed_time);

            OilDistributeDetailTime oilDistributeDetailTime = oilDistributeDetailTimeMap.get(groupPosition);
            tApplyTime.setText(oilDistributeDetailTime.getApplyTime());
            tCompletedTime.setText(oilDistributeDetailTime.getCompletedTime());

            return view;
        }

        /**
         * 组和子元素是否持有稳定的ID,也就是底层数据的改变不会影响到它们。
         * @return
         */
        @Override
        public boolean hasStableIds() {
            return true;
        }

        /**
         * 是否选中指定位置上的子元素。
         * @param groupPosition
         * @param childPosition
         * @return
         */
        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
