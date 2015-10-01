package com.oto.edyd;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.model.OrderDetail;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by yql on 2015/9/15.
 */
public class OrderDetailActivity extends Activity implements View.OnClickListener {

    private LinearLayout orderDetailBack; //返回
    private Button receiveOrder; //按钮


    private ImageView executeFirstPoint, //接单点
            executeSecondPoint, //到达装货点
            executeThirdPoint, //完成装货点
            executeFourPoint, //在途中点
            executeFivePoint, //到达卸货点
            executeSixPoint; //完成卸货点

    private TextView executeFirstTime, executeFirstDate, //接单时间
            executeSecondTime, executeSecondDate, //到达装货时间
            executeThirdTime, executeThirdDate, //完成装货时间
            executeFourTime, executeFourDate, //在途中时间
            executeFiveTime, executeFiveDate, //到达卸货时间
            executeSixTime, executeSixDate; //完成卸货时间

    private ImageView executeFirstLine, //第一条线
            executeSecondLine, //第二条线
            executeThirdLine, //第三条线
            executeFourLine, //第四条线
            executeFiveLine; //第五条线

    private TextView orderNumber; //订单号
    private TextView orderDate; //订单日期
    private ImageView orderStatus; //接单状态
    private TextView startPoint; //起始点
    private TextView endPoint; //终点
    private TextView shipper; //发货人
    private TextView phoneNumber; //发货人联系电话
    private TextView consignee; //收货人
    private TextView consigneePhoneNumber; //收货人联系人电话

    private TextView goodsName, //货物名称
            goodsTotalVolume, //货物总体积
            goodsTotalQuantity, //货物总数量
            goodsTotalWeight; //货物总质量

    private int controlId; //订单ID
    private String controlNum; //订单号
    private int controlStatus; //订单状态

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_detail);
        initFields();
        String primaryId = getIntent().getExtras().getString("primaryId");
        requestOrderDetailData(primaryId);
        orderDetailBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.order_detail_back:
                finish();
                break;
        }
    }

    /**
     * 初始化数据
     */
    private void initFields() {
        orderDetailBack = (LinearLayout) findViewById(R.id.order_detail_back);
        receiveOrder = (Button) findViewById(R.id.detail_receive_order); //接单按钮
        orderStatus = (ImageView) findViewById(R.id.order_status); //接单状态

        executeFirstPoint = (ImageView) findViewById(R.id.execute_first_point);
        executeSecondPoint = (ImageView) findViewById(R.id.execute_second_point);
        executeThirdPoint = (ImageView) findViewById(R.id.execute_third_point);
        executeFourPoint = (ImageView) findViewById(R.id.execute_four_point);
        executeFivePoint = (ImageView) findViewById(R.id.execute_five_point);

        executeFirstTime = (TextView) findViewById(R.id.execute_first_time);
        executeFirstDate = (TextView) findViewById(R.id.execute_first_date);
        executeSecondTime = (TextView) findViewById(R.id.execute_second_time);
        executeSecondDate = (TextView) findViewById(R.id.execute_second_date);
        executeThirdTime = (TextView) findViewById(R.id.execute_third_time);
        executeThirdDate = (TextView) findViewById(R.id.execute_third_date);
        executeFourTime = (TextView) findViewById(R.id.execute_four_time);
        executeFourDate = (TextView) findViewById(R.id.execute_third_date);
        executeFiveTime = (TextView) findViewById(R.id.execute_five_time);
        executeFiveDate = (TextView) findViewById(R.id.execute_five_date);
        executeSixTime = (TextView) findViewById(R.id.execute_six_time);
        executeSixDate = (TextView) findViewById(R.id.execute_six_date);

        executeFirstLine = (ImageView) findViewById(R.id.execute_first_line);
        executeSecondLine = (ImageView) findViewById(R.id.execute_second_line);
        executeThirdLine = (ImageView) findViewById(R.id.execute_third_line);
        executeFourLine = (ImageView) findViewById(R.id.execute_four_line);
        executeFiveLine = (ImageView) findViewById(R.id.execute_five_line);

        orderNumber = (TextView) findViewById(R.id.tv_order_number);
        orderDate = (TextView) findViewById(R.id.order_date);
        startPoint = (TextView) findViewById(R.id.receive_order_start_address); //发货地址
        endPoint = (TextView) findViewById(R.id.receive_order_end_address); //收货地址
        shipper = (TextView) findViewById(R.id.shipper_name); //发货人
        phoneNumber = (TextView) findViewById(R.id.phone_number_one); //发货人联系电话
        consignee = (TextView) findViewById(R.id.consignee); //收货人
        consigneePhoneNumber = (TextView) findViewById(R.id.consignee_phone_number); //收货人联系电话

        goodsName = (TextView) findViewById(R.id.goods_name);
        goodsTotalVolume = (TextView) findViewById(R.id.goods_total_volume);
        goodsTotalQuantity = (TextView) findViewById(R.id.goods_total_quantity);
        goodsTotalWeight = (TextView) findViewById(R.id.goods_total_weight);
    }

    /**
     * 根据ID请求订单详情数据
     *
     * @param primaryID
     */
    private void requestOrderDetailData(String primaryID) {

        String url = Constant.ENTRANCE_PREFIX + "appQueryOrderDetail.json?sessionUuid=8fe80b0852e848fabbcea08d333bd9d4&primaryId=" + primaryID;
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONObject rowJson;
                try {
                    jsonObject = new JSONObject(response);
                    if (!jsonObject.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(getApplicationContext(), "订单详情查询异常", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //订单日期
                    rowJson = jsonObject.getJSONArray("rows").getJSONObject(0);
                    JSONArray operTimeArray = rowJson.getJSONArray("operTime");
                    JSONObject operTimeJSON = operTimeArray.getJSONObject(0);

                    //实体数据设置
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setControlId(rowJson.getInt("ID"));
                    orderDetail.setControlNum(rowJson.getString("controlNum"));
                    orderDetail.setControlStatus(rowJson.getJSONArray("controlStatus").getInt(0));
                    orderDetail.setExecuteFirstTime(operTimeJSON.getString(""));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
