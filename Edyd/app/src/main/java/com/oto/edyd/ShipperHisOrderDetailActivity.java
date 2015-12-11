package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.model.OrderDetail;
import com.oto.edyd.model.OrderPerTime;
import com.oto.edyd.model.ShipperHisOrderBean;
import com.oto.edyd.model.TrackBean;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lbz on 2015/12/8.
 * 发货方历史订单详情
 */

public class ShipperHisOrderDetailActivity extends Activity {

    private TextView executeFirstTime, executeFirstDate, //接单时间
            executeSecondTime, executeSecondDate, //到达装货时间
            executeThirdTime, executeThirdDate, //完成装货时间
            executeFourTime, executeFourDate, //在途中时间
            executeFiveTime, executeFiveDate, //到达卸货时间
            executeSixTime, executeSixDate; //完成卸货时间

    private TextView orderNumber; //订单号
    private ImageView orderStatus; //接单状态
    private TextView startPoint; //起始点
    private TextView shipper; //发货人
    private TextView phoneNumber; //发货人联系电话
    private TextView fromAddress;//从哪到哪
    private TextView toAddress;

    private TextView goodsName, //货物名称
            goodsTotalVolume, //货物总体积
            goodsTotalQuantity, //货物总数量
            goodsTotalWeight; //货物总质量
    private Common common;
    private Activity mActivity;
    private static final int firstLoad = 0;//第一次加载
    private long primaryId;
    private CusProgressDialog loadingDialog;
    private ShipperHisOrderBean bean;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            OrderDetail orderDetail = (OrderDetail) msg.obj;
            goodsName.setText(orderDetail.getGoodNameLists().toString());
            goodsTotalVolume.setText(orderDetail.getGoodsTotalVolume());
            goodsTotalQuantity.setText(orderDetail.getGoodsTotalQuantity());
            goodsTotalWeight.setText(orderDetail.getGoodsTotalWeight());
            List<OrderPerTime> list = orderDetail.getOrderPerTimeList();
            executeFirstTime.setText(list.get(0).getHour());
            executeFirstDate.setText(list.get(0).getDate());
            executeSecondTime.setText(list.get(1).getHour());
            executeSecondDate.setText(list.get(1).getDate());
            executeThirdTime .setText(list.get(2).getHour());
            executeThirdDate .setText(list.get(2).getDate());
            executeFourTime.setText(list.get(3).getHour());
            executeFourDate.setText(list.get(3).getDate());
            executeFiveTime.setText(list.get(4).getHour());
            executeFiveDate.setText(list.get(4).getDate());
            executeSixTime.setText(list.get(5).getHour());
            executeSixDate.setText(list.get(5).getDate());
            dimissLoading();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipper_order_detail);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        bean = (ShipperHisOrderBean) bundle.getSerializable("detailBean");
        primaryId = bean.getPrimaryId();

        initField();
        initView();
        requestData(firstLoad, ""); //请求数据
    }

    private void initView() {
        //线形图时间
        executeFirstTime = (TextView) findViewById(R.id.execute_first_time);
        executeFirstDate = (TextView) findViewById(R.id.execute_first_date);
        executeSecondTime = (TextView) findViewById(R.id.execute_second_time);
        executeSecondDate = (TextView) findViewById(R.id.execute_second_date);
        executeThirdTime = (TextView) findViewById(R.id.execute_third_time);
        executeThirdDate = (TextView) findViewById(R.id.execute_third_date);
        executeFourTime = (TextView) findViewById(R.id.execute_four_time);
        executeFourDate = (TextView) findViewById(R.id.execute_four_date);
        executeFiveTime = (TextView) findViewById(R.id.execute_five_time);
        executeFiveDate = (TextView) findViewById(R.id.execute_five_date);
        executeSixTime = (TextView) findViewById(R.id.execute_six_time);
        executeSixDate = (TextView) findViewById(R.id.execute_six_date);

        //详情的第二部分
        orderNumber = (TextView) findViewById(R.id.orderNumView);//订单号
        orderNumber.setText(bean.getControlNum());
        startPoint = (TextView) findViewById(R.id.tv_addr_detail); //收货地址
        startPoint.setText(bean.getReceiverAddr());
        shipper = (TextView) findViewById(R.id.shipper_name); //收货人
        shipper.setText(bean.getReceiverName());
        phoneNumber = (TextView) findViewById(R.id.phone_number_one); //收货人联系电话
        phoneNumber.setText(bean.getReceiverContactTel());
        fromAddress = (TextView) findViewById(R.id.fromAddress);//从哪
        fromAddress.setText(bean.getSenderAddrProviceAndCity());
        toAddress = (TextView) findViewById(R.id.toAddress);
        toAddress.setText(bean.getReceiverAddrProviceAndCity());
        //货物信息部分
        goodsName = (TextView) findViewById(R.id.goods_name);
        goodsTotalVolume = (TextView) findViewById(R.id.goods_total_volume);
        goodsTotalQuantity = (TextView) findViewById(R.id.goods_total_quantity);
        goodsTotalWeight = (TextView) findViewById(R.id.goods_total_weight);
    }

    private void initField() {
        mActivity = this;
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
    }

    public void back(View view) {
        finish();
    }

    /**
     * 加载数据
     *
     * @param loadType 是否是第一次
     */
    private void requestData(final int loadType, String serachParames) {

        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        String url = Constant.ENTRANCE_PREFIX + "appQueryOrderDetail.json?sessionUuid=" + sessionUuid + "&primaryId=" + primaryId;
        //第一次进来显示loading
        if (loadType == firstLoad) {
            loadingDialog = new CusProgressDialog(mActivity, "正在获取数据...");
            loadingDialog.getLoadingDialog().show();
        }
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                dimissLoading();
                Toast.makeText(getApplicationContext(), "获取信息失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    if (!jsonObject.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(getApplicationContext(), "返回信息失败", Toast.LENGTH_SHORT).show();
                        dimissLoading();
                        return;
                    }
                    jsonArray = jsonObject.getJSONArray("rows");
                    JSONObject obj = jsonArray.getJSONObject(0);
                    fillList(obj, loadType);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void fillList(JSONObject obj, int loadType) throws JSONException {
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setGoodsTotalVolume(obj.getDouble("totalVolume") + "");
        orderDetail.setGoodsTotalQuantity(obj.getDouble("totalNum") + "");
        orderDetail.setGoodsTotalWeight(obj.getDouble("totalWeight") + "");
        //货物名
//        List list = orderDetail.getGoodNameLists();
        List<String> goodList = new ArrayList<String>();
        JSONArray goodjarr = obj.getJSONArray("goodsName");
        for (int i = 0; i < goodjarr.length(); i++) {
            String googObj = goodjarr.getString(i);
            goodList.add(googObj);
        }
        orderDetail.setGoodNameLists(goodList);
        //操作时间
        JSONArray timejarr = obj.getJSONArray("operTime");
        List<OrderPerTime> timeList = new ArrayList<OrderPerTime>();
        for (int i = 0; i < timejarr.length(); i++) {
            OrderPerTime orderPerTime = new OrderPerTime();
            JSONObject timeObj = timejarr.getJSONObject(i);
            orderPerTime.setDate(timeObj.getInt("month") + "-" + timeObj.getInt("day"));
            orderPerTime.setHour(timeObj.getInt("hours") + "-" + timeObj.getInt("minutes"));
            timeList.add(orderPerTime);
        }
        orderDetail.setOrderPerTimeList(timeList);

        Message message = Message.obtain();
        message.obj = orderDetail;
        handler.sendMessage(message);
    }

    private void dimissLoading() {
        loadingDialog.getLoadingDialog().dismiss();
    }

}
