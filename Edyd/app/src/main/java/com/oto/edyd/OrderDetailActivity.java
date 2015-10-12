package com.oto.edyd;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.model.OrderDetail;
import com.oto.edyd.model.OrderPerTime;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.NumberFormat;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yql on 2015/9/15.
 */
public class OrderDetailActivity extends Activity implements View.OnClickListener{

    private LinearLayout orderDetailBack; //返回
    private Button receiveOrder; //按钮

    private TextView tvFirstTime, tvSecondTime, tvThirdTime, tvFourTime, tvFiveTime, tvSixTime;
    private ImageView executeFirstPoint, //接单点
            executeSecondPoint, //到达装货点
            executeThirdPoint, //装货完成点
            executeFourPoint, //送货在途点
            executeFivePoint, //到达收货点
            executeSixPoint; //收货完成点

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

    NumberFormat numberFormat = new NumberFormat(); //格式化时间
    private CusProgressDialog loadingDialog; //页面切换过度

    private Common common; //common偏好设置
    private int reOrderStatus; //订单状态

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_detail);
        initFields();
        String primaryId = getIntent().getStringExtra("primaryId");
//        TranView tView = (TranView)getIntent().getSerializableExtra("S_VIEW");
//        View view = tView.getView();

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
        executeSixPoint = (ImageView) findViewById(R.id.execute_six_point);

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

        executeFirstLine = (ImageView) findViewById(R.id.execute_first_line);
        executeSecondLine = (ImageView) findViewById(R.id.execute_second_line);
        executeThirdLine = (ImageView) findViewById(R.id.execute_third_line);
        executeFourLine = (ImageView) findViewById(R.id.execute_four_line);
        executeFiveLine = (ImageView) findViewById(R.id.execute_five_line);

        orderNumber = (TextView) findViewById(R.id.order_number);
        orderDate = (TextView) findViewById(R.id.order_date);
        startPoint = (TextView) findViewById(R.id.receive_order_start_address); //发货地址
        endPoint = (TextView) findViewById(R.id.receive_order_end_address); //收货地址
        shipper = (TextView) findViewById(R.id.shipper_name); //发货人
        phoneNumber = (TextView) findViewById(R.id.phone_number_one); //发货人联系电话
        consignee = (TextView) findViewById(R.id.consignee); //收货人
        consigneePhoneNumber = (TextView) findViewById(R.id.consignee_phone_number); //收货人联系电话

        tvFirstTime = (TextView) findViewById(R.id.tv_first_time);
        tvSecondTime = (TextView) findViewById(R.id.tv_second_time);
        tvThirdTime = (TextView) findViewById(R.id.tv_third_time);
        tvFourTime = (TextView) findViewById(R.id.tv_four_time);
        tvFiveTime = (TextView) findViewById(R.id.tv_five_time);
        tvSixTime = (TextView) findViewById(R.id.tv_six_time);

        goodsName = (TextView) findViewById(R.id.goods_name);
        goodsTotalVolume = (TextView) findViewById(R.id.goods_total_volume);
        goodsTotalQuantity = (TextView) findViewById(R.id.goods_total_quantity);
        goodsTotalWeight = (TextView) findViewById(R.id.goods_total_weight);

        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
    }

    /**
     * 根据ID请求订单详情数据
     *
     * @param primaryID
     */
    private void requestOrderDetailData(String primaryID) {
        Common common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        String url = Constant.ENTRANCE_PREFIX + "appQueryOrderDetail.json?sessionUuid="+sessionUuid+"&primaryId=" + primaryID;
        OkHttpClientManager.getAsyn(url, new OrderDetailResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONObject rowJson;
                try {
                    jsonObject = new JSONObject(response);
                    if(!jsonObject.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(getApplicationContext(), "订单详情查询异常", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //实体数据设置
                    OrderDetail orderDetail = new OrderDetail();
                    rowJson = jsonObject.getJSONArray("rows").getJSONObject(0);

                    //订单日期
                    JSONArray perTimeArray = rowJson.getJSONArray("operTime");
                    List<OrderPerTime> orderPerTimeList = new ArrayList<OrderPerTime>(); //时间集
                    for (int i = 0; i < perTimeArray.length(); i++) {
                        JSONObject perJSON = perTimeArray.getJSONObject(i);
                        OrderPerTime orderPerTime = new OrderPerTime();
                        orderPerTime.setDate(formatDate(perJSON.getInt("month") + 1, perJSON.getInt("date")));
                        orderPerTime.setHour(formatTime(perJSON.getInt("hours"), perJSON.getInt("minutes")));
                        orderPerTimeList.add(orderPerTime);
                    }
                    orderDetail.setOrderPerTimeList(orderPerTimeList);

                    //订单详情
                    orderDetail.setControlId(rowJson.getInt("ID"));
                    orderDetail.setControlNum(rowJson.getString("controlNum")); //订单号
                    //orderDetail.setControlStatus(rowJson.getJSONArray("controlStatus").getInt(0));
                    //订单状态
                    JSONArray orderStatusArray = rowJson.getJSONArray("controlStatus");
                    List<Integer> orderStatusLists = new ArrayList<Integer>(); //订单状态
                    for(int i = 0; i < orderStatusArray.length(); i++) {
                        orderStatusLists.add(orderStatusArray.getInt(i));
                    }
                    orderDetail.setOrderStatusLists(orderStatusLists);

                    orderDetail.setStartPoint(rowJson.getString("senderAddr")); //起始地点
                    orderDetail.setEndPoint(rowJson.getString("receiverAddr")); //终点地址
                    orderDetail.setShipper(rowJson.getString("senderContactPerson")); //发货人
                    orderDetail.setPhoneNumber(rowJson.getString("senderContactTel")); //发货联系人
                    orderDetail.setConsignee(rowJson.getString("receiverContactPerson")); //收货人
                    orderDetail.setConsigneePhoneNumber(rowJson.getString("receiverContactTel")); //收货人联系电话

                    //货物
                    //orderDetail.setGoodsName(rowJson.getJSONArray("goodsName").getString(0)); //获取名称
                    JSONArray goodNameArray = rowJson.getJSONArray("goodsName");
                    List<String> goodNameLists = new ArrayList<String>(); //订单状态
                    for (int i = 0; i < goodNameArray.length(); i++) {
                        goodNameLists.add(goodNameArray.getString(i));
                    }
                    orderDetail.setGoodNameLists(goodNameLists);

                    orderDetail.setGoodsTotalVolume(rowJson.getString("totalVolume")); //货物总体积
                    orderDetail.setGoodsTotalQuantity(rowJson.getString("totalNum")); //货物总数量
                    orderDetail.setGoodsTotalWeight(rowJson.getString("totalWeight"));

                    Message message = new Message();
                    message.what = 0x12;
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("orderDetail", orderDetail);
                    message.setData(bundle);
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 格式化日期
     * @param month
     * @param day
     * @return
     */
    private String formatDate(int month, int day) {
        String date =  numberFormat.outputDoubleNumber(month) + "-" + numberFormat.outputDoubleNumber(day);
        return date;
    }

    /**
     * 格式化时间
     * @param hours
     * @param minutes
     * @return
     */
    private String formatTime(int hours, int minutes) {
        String time = numberFormat.outputDoubleNumber(hours) + ":" +numberFormat.outputDoubleNumber(minutes);
        return time;
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            OrderDetail orderDetail;
            Bundle bundle;
            switch (msg.what) {
                case 0x12:
                    bundle = msg.getData();
                    orderDetail = (OrderDetail)bundle.getSerializable("orderDetail");
                    reOrderStatus = orderDetail.getOrderStatusLists().get(0); //获取订单最新状态
                    setOrderDetailInfo(orderDetail);
                    break;
                case 0x13:
                    String primaryId = getIntent().getStringExtra("primaryId");
                    requestOrderDetailData(primaryId);
            }
        }
    };

    /**
     * 设置订单数据
     * @param orderDetail
     */
    public void setOrderDetailInfo(OrderDetail orderDetail) {
        List<OrderPerTime> orderPerTimeList = orderDetail.getOrderPerTimeList();
        List<Integer> orderStatusLists = orderDetail.getOrderStatusLists();
        int type = orderPerTimeList.size(); //类别

        if(!(orderPerTimeList.size() == orderStatusLists.size())) {
            Toast.makeText(getApplicationContext(), "订单详情数据显示异常", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (type) {
            case 1: //未接单
                break;
            case 2: //接单
                executeFirstPoint.setImageResource(R.mipmap.line_car); //第一个点设置为车辆图标
                for(int i = 0; i < 1; i++) {
                    if(i == 0) {
                        executeFirstTime.setText(orderPerTimeList.get(i).getHour());
                        executeFirstDate.setText(orderPerTimeList.get(i).getDate());
                        //设置颜色...
                        tvFirstTime.setTextColor(getResources().getColor(R.color.user_icon_8));
                        executeFirstTime.setTextColor(getResources().getColor(R.color.user_icon_8));
                        executeFirstDate.setTextColor(getResources().getColor(R.color.user_icon_8));
                    }
                }
                break;
            case 3: //到达装货
                executeFirstPoint.setImageResource(R.mipmap.execute_green);
                executeFirstLine.setImageResource(R.mipmap.line_green);
                executeSecondPoint.setImageResource(R.mipmap.line_car);
                for(int i = 0; i < 2; i++) {
                    if(i == 0) {
                        executeSecondTime.setText(orderPerTimeList.get(i).getHour());
                        executeSecondDate.setText(orderPerTimeList.get(i).getDate());
                        //设置颜色...
                        tvSecondTime.setTextColor(getResources().getColor(R.color.user_icon_8));
                        executeSecondTime.setTextColor(getResources().getColor(R.color.user_icon_8));
                        executeSecondDate.setTextColor(getResources().getColor(R.color.user_icon_8));

                        tvFirstTime.setTextColor(getResources().getColor(R.color.dim_foreground_dark));
                        executeFirstTime.setTextColor(getResources().getColor(R.color.dim_foreground_dark));
                        executeFirstDate.setTextColor(getResources().getColor(R.color.dim_foreground_dark));
                    } else if(i == 1) {
                        executeFirstTime.setText(orderPerTimeList.get(i).getHour());
                        executeFirstDate.setText(orderPerTimeList.get(i).getDate());
                    }
                }
                break;
            case 4: //装货完成
                executeFirstPoint.setImageResource(R.mipmap.execute_green);
                executeFirstLine.setImageResource(R.mipmap.line_green);
                executeSecondPoint.setImageResource(R.mipmap.execute_green);
                executeSecondLine.setImageResource(R.mipmap.line_green);
                executeThirdPoint.setImageResource(R.mipmap.line_car);
                for(int i = 0; i < 3; i++) {
                    if(i == 0) {
                        executeThirdTime.setText(orderPerTimeList.get(i).getHour());
                        executeThirdDate.setText(orderPerTimeList.get(i).getDate());
                        //设置颜色...
                        tvThirdTime.setTextColor(getResources().getColor(R.color.user_icon_8));
                        executeThirdTime.setTextColor(getResources().getColor(R.color.user_icon_8));
                        executeThirdDate.setTextColor(getResources().getColor(R.color.user_icon_8));

                        tvSecondTime.setTextColor(getResources().getColor(R.color.dim_foreground_dark));
                        executeSecondTime.setTextColor(getResources().getColor(R.color.dim_foreground_dark));
                        executeSecondDate.setTextColor(getResources().getColor(R.color.dim_foreground_dark));
                    } else if(i == 1) {
                        executeSecondTime.setText(orderPerTimeList.get(i).getHour());
                        executeSecondDate.setText(orderPerTimeList.get(i).getDate());
                    } else if( i == 2) {
                        executeFirstTime.setText(orderPerTimeList.get(i).getHour());
                        executeFirstDate.setText(orderPerTimeList.get(i).getDate());
                    }
                }
                break;
            case 5: //送货在途
                executeFirstPoint.setImageResource(R.mipmap.execute_green);
                executeFirstLine.setImageResource(R.mipmap.line_green);
                executeSecondPoint.setImageResource(R.mipmap.execute_green);
                executeSecondLine.setImageResource(R.mipmap.line_green);
                executeThirdPoint.setImageResource(R.mipmap.execute_green);
                executeThirdLine.setImageResource(R.mipmap.line_green);
                executeFourPoint.setImageResource(R.mipmap.line_car);
                for(int i = 0; i < 4; i++) {
                    if(i == 0) {
                        executeFourTime.setText(orderPerTimeList.get(i).getHour());
                        executeFourDate.setText(orderPerTimeList.get(i).getDate());
                        //设置颜色...
                        tvFourTime.setTextColor(getResources().getColor(R.color.user_icon_8));
                        executeFourTime.setTextColor(getResources().getColor(R.color.user_icon_8));
                        executeFourDate.setTextColor(getResources().getColor(R.color.user_icon_8));

                        tvThirdTime.setTextColor(getResources().getColor(R.color.dim_foreground_dark));
                        executeThirdTime.setTextColor(getResources().getColor(R.color.dim_foreground_dark));
                        executeThirdDate.setTextColor(getResources().getColor(R.color.dim_foreground_dark));
                    } else if(i == 1) {
                        executeThirdTime.setText(orderPerTimeList.get(i).getHour());
                        executeThirdDate.setText(orderPerTimeList.get(i).getDate());
                    } else if( i == 2) {
                        executeSecondTime.setText(orderPerTimeList.get(i).getHour());
                        executeSecondDate.setText(orderPerTimeList.get(i).getDate());
                    } else if(i == 3) {
                        executeFirstTime.setText(orderPerTimeList.get(i).getHour());
                        executeFirstDate.setText(orderPerTimeList.get(i).getDate());
                    }
                }
                break;
            case 6: //到达收货
                executeFirstPoint.setImageResource(R.mipmap.execute_green);
                executeFirstLine.setImageResource(R.mipmap.line_green);
                executeSecondPoint.setImageResource(R.mipmap.execute_green);
                executeSecondLine.setImageResource(R.mipmap.line_green);
                executeThirdPoint.setImageResource(R.mipmap.execute_green);
                executeThirdLine.setImageResource(R.mipmap.line_green);
                executeFourPoint.setImageResource(R.mipmap.execute_green);
                executeFourLine.setImageResource(R.mipmap.line_green);
                executeFivePoint.setImageResource(R.mipmap.line_car);
                for(int i = 0; i < 5; i++) {
                    if(i == 0) {
                        executeFiveTime.setText(orderPerTimeList.get(i).getHour());
                        executeFiveDate.setText(orderPerTimeList.get(i).getDate());
                        //设置颜色...
                        tvFiveTime.setTextColor(getResources().getColor(R.color.user_icon_8));
                        executeFiveTime.setTextColor(getResources().getColor(R.color.user_icon_8));
                        executeFiveDate.setTextColor(getResources().getColor(R.color.user_icon_8));

                        tvFourTime.setTextColor(getResources().getColor(R.color.dim_foreground_dark));
                        executeFourTime.setTextColor(getResources().getColor(R.color.dim_foreground_dark));
                        executeFourDate.setTextColor(getResources().getColor(R.color.dim_foreground_dark));
                    } else if(i == 1) {
                        executeFourTime.setText(orderPerTimeList.get(i).getHour());
                        executeFourDate.setText(orderPerTimeList.get(i).getDate());
                    } else if( i == 2) {
                        executeThirdTime.setText(orderPerTimeList.get(i).getHour());
                        executeThirdDate.setText(orderPerTimeList.get(i).getDate());
                    } else if(i == 3) {
                        executeSecondTime.setText(orderPerTimeList.get(i).getHour());
                        executeSecondDate.setText(orderPerTimeList.get(i).getDate());
                    } else if(i == 4) {
                        executeFirstTime.setText(orderPerTimeList.get(i).getHour());
                        executeFirstDate.setText(orderPerTimeList.get(i).getDate());
                    }
                }
                break;
            case 7: //收货完成
                executeFirstPoint.setImageResource(R.mipmap.execute_green);
                executeFirstLine.setImageResource(R.mipmap.line_green);
                executeSecondPoint.setImageResource(R.mipmap.execute_green);
                executeSecondLine.setImageResource(R.mipmap.line_green);
                executeThirdPoint.setImageResource(R.mipmap.execute_green);
                executeThirdLine.setImageResource(R.mipmap.line_green);
                executeFourPoint.setImageResource(R.mipmap.execute_green);
                executeFourLine.setImageResource(R.mipmap.line_green);
                executeFivePoint.setImageResource(R.mipmap.execute_green);
                executeFiveLine.setImageResource(R.mipmap.line_green);
                executeSixPoint.setImageResource(R.mipmap.line_car);
                for(int i = 0; i < 6; i++) {
                    if(i == 0) {
                        executeSixTime.setText(orderPerTimeList.get(i).getHour());
                        executeSixDate.setText(orderPerTimeList.get(i).getDate());
                        //设置颜色...
                        tvSixTime.setTextColor(getResources().getColor(R.color.user_icon_8));
                        executeSixTime.setTextColor(getResources().getColor(R.color.user_icon_8));
                        executeSixDate.setTextColor(getResources().getColor(R.color.user_icon_8));

                        tvFiveTime.setTextColor(getResources().getColor(R.color.dim_foreground_dark));
                        executeFiveTime.setTextColor(getResources().getColor(R.color.dim_foreground_dark));
                        executeFiveDate.setTextColor(getResources().getColor(R.color.dim_foreground_dark));
                    } else if(i == 1) {
                        executeFiveTime.setText(orderPerTimeList.get(i).getHour());
                        executeFiveDate.setText(orderPerTimeList.get(i).getDate());
                    } else if( i == 2) {
                        executeFourTime.setText(orderPerTimeList.get(i).getHour());
                        executeFourDate.setText(orderPerTimeList.get(i).getDate());
                    } else if(i == 3) {
                        executeThirdTime.setText(orderPerTimeList.get(i).getHour());
                        executeThirdDate.setText(orderPerTimeList.get(i).getDate());
                    } else if(i == 4) {
                        executeSecondTime.setText(orderPerTimeList.get(i).getHour());
                        executeSecondDate.setText(orderPerTimeList.get(i).getDate());
                    } else if(i == 5) {
                        executeFirstTime.setText(orderPerTimeList.get(i).getHour());
                        executeFirstDate.setText(orderPerTimeList.get(i).getDate());
                    }
                }
                break;
        }

        orderNumber.setText(orderDetail.getControlNum());
        orderDate.setText(orderDetail.getOrderDate());
        startPoint.setText(orderDetail.getStartPoint());
        endPoint.setText(orderDetail.getEndPoint());
        shipper.setText(orderDetail.getShipper());
        phoneNumber.setText(orderDetail.getPhoneNumber());
        consignee.setText(orderDetail.getConsignee());
        consigneePhoneNumber.setText(orderDetail.getConsigneePhoneNumber());

        switch (orderStatusLists.get(0)) {
            case 17: //未接单
                receiveOrder.setText(getString(R.string.receive_order));
                break;
            case 20: //已接单
                orderStatus.setImageResource(R.mipmap.tts_loading_way);
                receiveOrder.setText("到达装货");
                break;
            case 30: //	到达装货
                orderStatus.setImageResource(R.mipmap.tts_arrived_load);
                receiveOrder.setText("装货完成");
                break;
            case 40: //装货完成
                orderStatus.setImageResource(R.mipmap.tts_completion_load);
                receiveOrder.setText("送货在途");
                break;
            case 50: //送货在途
                orderStatus.setImageResource(R.mipmap.tts_delivery_way);
                receiveOrder.setText("到达收货");
                break;
            case 60: //到达收货
                orderStatus.setImageResource(R.mipmap.tts_arrived_receive);
                receiveOrder.setText("收货完成");
                break;
            case 99: //收货完成
                //orderStatus.setImageResource(R.mipmap.ic_have_been_receive);
                receiveOrder.setBackgroundResource(R.drawable.border_corner_login);
                receiveOrder.setText("完成订单");
                receiveOrder.setEnabled(false);
                orderStatus.setVisibility(View.GONE);
                break;
        }

        List<String> goodNameList = orderDetail.getGoodNameLists();
        String goods = "";
        for(int i = 0; i < goodNameList.size(); i++) {
            goods = goods + goodNameList.get(i);
        }
        goodsName.setText(goods);
        goodsTotalVolume.setText(orderDetail.getGoodsTotalVolume());
        goodsTotalQuantity.setText(orderDetail.getGoodsTotalQuantity());
        goodsTotalWeight.setText(orderDetail.getGoodsTotalWeight());

        receiveOrder.setOnClickListener(new CusOnClickListener(orderDetail)); //给接单按钮添加监听事件
        //loadingDialog.getLoadingDialog().dismiss();
    }

    public abstract class OrderDetailResultCallback<T> extends OkHttpClientManager.ResultCallback<T>{
        @Override
        public void onBefore() {
            //请求之前操作
            loadingDialog = new CusProgressDialog(OrderDetailActivity.this, "正在拼命加载...");
            loadingDialog.getLoadingDialog().show();
        }

        @Override
        public void onAfter() {
            //请求之后要做的操作
            loadingDialog.dismissDialog();
        }
    }

    /**
     * 更新订单
     * @param orderDetail
     */
    private void operationOrder(final OrderDetail orderDetail) {
        int controlId = orderDetail.getControlId();
        String controlNum = orderDetail.getControlNum();
        final int controlStatus = orderDetail.getOrderStatusLists().get(0);
        String sessionUUID = getSessionUUID();
        String url = Constant.ENTRANCE_PREFIX + "appUpdateOrderStatus.json?sessionUuid="+sessionUUID+"&controlId="+controlId+"&controlStatus="+controlStatus+"&controlNum="+controlNum;
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                List<OrderPerTime> orderPerTimeList = orderDetail.getOrderPerTimeList();
                try {
                    jsonObject = new JSONObject(response);
                    if (!jsonObject.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(getApplicationContext(), "接单异常异常", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Message message = new Message();
                    message.what = 0x13;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 获取sessionid
     * @return
     */
    private String getSessionUUID() {
        return common.getStringByKey(Constant.SESSION_UUID);
    }

    private class CusOnClickListener implements View.OnClickListener {
        private OrderDetail orderDetail;
        public CusOnClickListener(OrderDetail orderDetail){
            this.orderDetail = orderDetail;
        }
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.detail_receive_order:
                    if((((TextView)v).getText().toString().equals("完成订单"))) {
                        Toast.makeText(getApplicationContext(), "订单已完成，不能操作", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    new AlertDialog.Builder(OrderDetailActivity.this).setTitle("接单")
                            .setMessage("确认"+((TextView)v).getText().toString()+"吗？")
                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    operationOrder(orderDetail);
                                }
                            })
                            .setNegativeButton("否", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                    break;
            }
        }
    }
}
