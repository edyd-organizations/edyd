package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.model.OilCardInfo;
import com.oto.edyd.model.OilDistributeDetail;
import com.oto.edyd.model.OilDistributeDetailTime;
import com.oto.edyd.model.OilTransactionDetail;
import com.oto.edyd.model.OilTransactionDetailItem;
import com.oto.edyd.module.oil.activity.OilCardApplyActivity;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yql on 2015/11/3.
 */
public class OilTransactionDetailActivity extends Activity implements View.OnClickListener {

    private LinearLayout back; //返回
    private TextView typeCardOrCarNumber; //卡号或者车牌号
    private TextView cardNumber; //卡号
    private TextView carNumber; //车牌号
    private TextView balance; //余额
    private TextView lastTime; //最后加油时间
    private ExpandableListView distributeDetailList; //分配明细列表
    private TextView tOilCardApply; //油卡申请
    private TextView tAmountDistribute; //金额分配
    private CusProgressDialog dialog;

    private List<OilTransactionDetail> oilDistributeDetails = new ArrayList<OilTransactionDetail>(); //列表数据
    Map<Integer, OilTransactionDetailItem> oilTransactionDetailItemMap = new HashMap<Integer, OilTransactionDetailItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oil_card_transaction_detail);
        initFields(); //初始化数据

        Bundle bundle = getIntent().getExtras();
        OilCardInfo oilCardInfo = (OilCardInfo) bundle.getSerializable("oil_card");
        requestDistributeUserList(oilCardInfo); //请求列表数据

        back.setOnClickListener(this);
        tOilCardApply.setOnClickListener(this);
        tAmountDistribute.setOnClickListener(this);
    }

    /**
     * 数据初始化
     */
    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
        typeCardOrCarNumber = (TextView) findViewById(R.id.type_car_number_or_card);
        cardNumber = (TextView) findViewById(R.id.card_number);
        carNumber = (TextView) findViewById(R.id.car_number);
        balance = (TextView) findViewById(R.id.balance);
        lastTime = (TextView) findViewById(R.id.last_time);
        distributeDetailList = (ExpandableListView) findViewById(R.id.distribute_detail_list);
        distributeDetailList.setGroupIndicator(null);
        tOilCardApply = (TextView) findViewById(R.id.oil_card_apply);
        tAmountDistribute = (TextView) findViewById(R.id.oil_card_account_distribute);
        dialog = new CusProgressDialog(OilTransactionDetailActivity.this);
    }

    @Override
    public void onClick(View v) {
        Intent intent;

        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.oil_card_apply: //油卡申请
                intent = new Intent(getApplicationContext(), OilCardApplyActivity.class); //油卡金额分配
                startActivity(intent);
                break;
            case R.id.oil_card_account_distribute: //金额分配
                intent = new Intent(getApplicationContext(), OilCardAmountDistributeActivity.class); //油卡金额分配
                startActivity(intent);
                break;
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x12: //油卡金额数据返回执行
                    OilTransactionDetail oilTransactionDetail = oilDistributeDetails.get(0);
                    cardNumber.setText(oilTransactionDetail.getCardId());
                    carNumber.setText(oilTransactionDetail.getCarId());
                    lastTime.setText(oilTransactionDetail.getTransactionTime());
                    OilTransactionDetailItem oilTransactionDetailItem = oilTransactionDetailItemMap.get(0);
                    balance.setText(oilTransactionDetailItem.getCardBalance());
                    distributeDetailList.setAdapter(new OilCardDistributeDetailAdapter(getApplicationContext()));
                    break;
                case 0x13: //暂无数据
                    Toast.makeText(OilTransactionDetailActivity.this, "暂无数据", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    /**
     * 请求预分配用户列表
     */
    private void requestDistributeUserList(OilCardInfo oilCardInfo) {
        Common common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        String enterpriseId = common.getStringByKey(Constant.ENTERPRISE_ID);
        String orgCode = common.getStringByKey(Constant.ORG_CODE);
        String url = "";

        if(enterpriseId.equals("0")) {
            url = Constant.ENTRANCE_PREFIX + "inquireOilCardDetail.json?sessionUuid=" + sessionUuid + "&enterpriseId=" + enterpriseId+
                    "&cardId=" + oilCardInfo.getCardId();
        } else {
            url = Constant.ENTRANCE_PREFIX + "inquireOilCardDetail.json?sessionUuid=" + sessionUuid + "&enterpriseId=" + enterpriseId+
                    "&orgCode=" + orgCode + "&cardId=" + oilCardInfo.getCardId();
        }
        OkHttpClientManager.getAsyn(url, new OilTransactionResultCallback<String> () {
            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                JSONObject transactionDetailJSON;
                JSONArray transactionDetailArray;
                try {
                    transactionDetailJSON = new JSONObject(response);
                    String status = transactionDetailJSON.getString("status");
                    if(!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        //变更列表数据获取失败
                        Toast.makeText(getApplicationContext(), "交易明细信息获取异常", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    transactionDetailArray = transactionDetailJSON.getJSONArray("rows");
                    Message message = new Message();
                    if(transactionDetailArray.length() > 0) {
                        for(int i = 0; i<transactionDetailArray.length(); i++) {
                            JSONObject jsonObject = transactionDetailArray.getJSONObject(i);
                            OilTransactionDetail oilTransactionDetail = new OilTransactionDetail(); //父节点
                            OilTransactionDetailItem oilTransactionDetailItem = new OilTransactionDetailItem(); //子节点

                            oilTransactionDetail.setCarId(jsonObject.getString("cardHolder")); //车牌号
                            oilTransactionDetail.setCardId(jsonObject.getString("cardId")); //卡号
                            oilTransactionDetail.setTransactionTime(jsonObject.getString("detailDT")); //交易时间
                            oilTransactionDetail.setTurnover(jsonObject.getString("detailMoney")); //交易额
                            oilTransactionDetail.setTransactionType(jsonObject.getString("detailType")); //交易类型
                            oilDistributeDetails.add(oilTransactionDetail);

                            oilTransactionDetailItem.setOilType(jsonObject.getString("oilType")); //油品种类
                            oilTransactionDetailItem.setUnitPrice(jsonObject.getString("detailPrice")); //单价
                            oilTransactionDetailItem.setAddOilNumber(jsonObject.getString("detailNum")); //加油量
                            oilTransactionDetailItem.setCardBalance(jsonObject.getString("balance")); //卡余额
                            oilTransactionDetailItem.setTransactionAddress(jsonObject.getString("address")); //交易地点
                            oilTransactionDetailItemMap.put(i, oilTransactionDetailItem);
                        }
                        message.what = 0x12;
                    } else {
                        message.what = 0x13;
                    }
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


//        //假数据
//        for(int i = 0; i < 10; i++) {
//            OilTransactionDetail oilDistributeDetail = new OilTransactionDetail();
//            oilDistributeDetail.setCar("闽F324" + i);
//            oilDistributeDetail.setCard("535734673" + i);
//            oilDistributeDetail.setTime("2015-11-3 17:45:53");
//            oilDistributeDetail.setBalance("300" + i);
//            oilDistributeDetails.add(oilDistributeDetail);
//
//            OilTransactionDetailItem oilTransactionDetailItem = new OilTransactionDetailItem();
//            oilTransactionDetailItem.setOilCategory("93#汽油");
//            oilTransactionDetailItem.setUnitPrice("￥3" + i);
//            oilTransactionDetailItem.setAddOilQuantity("4" + i + "L");
//            oilTransactionDetailItem.setCardBalance("￥50" + i);
//            oilTransactionDetailItem.setAddress("中国石化福建厦门石油分公司东渡加油站");
//            oilTransactionDetailItemMap.put(i, oilTransactionDetailItem);
//        }
//
//
//        Message message = new Message();
//        message.what = 0x12;
//        handler.sendMessage(message);
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
            return oilTransactionDetailItemMap.get(childPosition);
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
            TextView time; //时间
            TextView tAmount; //金额
            ImageView iUpOrDown;// 图标指示器
            TextView tradeStatus; //交易状态

            View view = inflater.inflate(R.layout.oil_card_transaction_detail_parent_item, null);
            tCarNumber = (TextView) view.findViewById(R.id.car_number);
            tCardNumber = (TextView) view.findViewById(R.id.card_number);
            tAmount = (TextView) view.findViewById(R.id.balance);
            time = (TextView) view.findViewById(R.id.time);
            tradeStatus = (TextView) view.findViewById(R.id.trade_status);
            iUpOrDown = (ImageView) view.findViewById(R.id.ic_up_or_down);

            OilTransactionDetail oilTransactionDetail = oilDistributeDetails.get(groupPosition);
            tCarNumber.setText(oilTransactionDetail.getCarId());
            tCardNumber.setText(oilTransactionDetail.getCardId());
            time.setText(oilTransactionDetail.getTransactionTime());
            tAmount.setText(oilTransactionDetail.getTurnover());
            String status = oilTransactionDetail.getTransactionType();
            if(status.equals("圈存")) {
                tradeStatus.setText("+");
            }else {
                tradeStatus.setText("-");
            }

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
            TextView tOilCategory; //油品分类
            TextView tUnitPrice; //单价
            TextView tAddOilQuantity; //加油量
            TextView tCardBalance; //卡余额
            TextView tAddress; //地址

            View view = inflater.inflate(R.layout.oil_card_transaction_detail_child_item, null);
            tOilCategory = (TextView) view.findViewById(R.id.oil_category);
            tUnitPrice = (TextView) view.findViewById(R.id.unit_price);
            tAddOilQuantity = (TextView) view.findViewById(R.id.add_oil_quantity);
            tCardBalance = (TextView) view.findViewById(R.id.card_balance);
            tAddress = (TextView) view.findViewById(R.id.address);

            OilTransactionDetailItem oilTransactionDetailItem = oilTransactionDetailItemMap.get(groupPosition);
            tOilCategory.setText(oilTransactionDetailItem.getOilType());
            tUnitPrice.setText(oilTransactionDetailItem.getUnitPrice());
            tAddOilQuantity.setText(oilTransactionDetailItem.getAddOilNumber());
            tCardBalance.setText(oilTransactionDetailItem.getCardBalance());
            tAddress.setText(oilTransactionDetailItem.getTransactionAddress());
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

    abstract  class OilTransactionResultCallback<T> extends OkHttpClientManager.ResultCallback<T>{
        @Override
        public void onBefore() {
            dialog.showDialog();
        }

        @Override
        public void onAfter() {
            dialog.dismissDialog();
        }
    }
}
