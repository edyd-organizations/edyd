package com.oto.edyd;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.model.Driver;
import com.oto.edyd.model.TransportDispatch;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yql on 2015/11/25.
 */
public class TransportOrderDispatchDetailActivity extends Activity implements View.OnClickListener {

    private LinearLayout back; //返回
    private TextView orderFlowWaterNumber; //订单流水号
    private TextView placeOrderTime; //下单时间
    private TextView transportRoute; //运输路线
    private TextView arriveTime; //到达时间
    private TextView goodsName; //货物名称
    private TextView goodsTotalNumber; //货物总数量
    private TextView goodsTotalWeight; //货物总重量
    private TextView goodsTotalVolume; //货物总体积
    //private EditText tvDistributeOrderNumber;//调度单号
    private EditText driverName; //司机名称
    private EditText carNumber; //车牌号
    private EditText identityCard; //身份证
    private EditText driverPhoneNumber; //司机手机号码
    private TextView distributeOrder; //调度
    private TextView quickDispatch; //新增司机

    private TransportDispatch transportDispatch; //派单详情

    private Common common;
    private String sessionUuid; //用户唯一标识
    private String globalDistributeOrderNumber; //调度单号
    private Driver driver; //司机信息
    private CusProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transport_order_dispatch_detail);
        initFields();

        requestGoodsName();
        back.setOnClickListener(this);
        driverName.setOnClickListener(this);
        distributeOrder.setOnClickListener(this);
        quickDispatch.setOnClickListener(this);
        driverName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!(s == null)) {
                    String inputContent = s.toString();
                    if (inputContent != null && !(inputContent.equals(""))) {
                        String txDriverPhoneNumber = driverPhoneNumber.getText().toString();
                        if (txDriverPhoneNumber != null && !(txDriverPhoneNumber.equals(""))) {
                            distributeOrder.setEnabled(true); //设置按钮可用
                            distributeOrder.setBackgroundResource(R.drawable.border_corner_login_enable);
                        }
                    } else {
                        distributeOrder.setBackgroundResource(R.drawable.border_corner_login);
                        distributeOrder.setEnabled(false); //设置按钮不可用
                    }
                }
            }
        });
        driverPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!(s == null)) {
                    String inputContent = s.toString();
                    if (inputContent != null && !(inputContent.equals(""))) {
                        String txDriverName = driverName.getText().toString();
                        if (txDriverName != null && !(txDriverName.equals(""))) {
                            distributeOrder.setEnabled(true); //设置按钮可用
                            distributeOrder.setBackgroundResource(R.drawable.border_corner_login_enable);
                        }
                    } else {
                        distributeOrder.setBackgroundResource(R.drawable.border_corner_login);
                        distributeOrder.setEnabled(false); //设置按钮不可用
                    }
                }
            }
        });
    }

    /**
     * 初始化数据
     */
    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
        orderFlowWaterNumber = (TextView) findViewById(R.id.order_number);
        placeOrderTime = (TextView) findViewById(R.id.place_order_time);
        transportRoute = (TextView) findViewById(R.id.transport_route);
        arriveTime = (TextView) findViewById(R.id.arrive_time);
        goodsName = (TextView) findViewById(R.id.goods_name);
        goodsTotalNumber = (TextView) findViewById(R.id.goods_total_number);
        goodsTotalWeight = (TextView) findViewById(R.id.goods_total_weight);
        goodsTotalVolume = (TextView) findViewById(R.id.goods_total_volume);
        //tvDistributeOrderNumber = (EditText) findViewById(R.id.tv_distribute_order_number);
        driverName = (EditText) findViewById(R.id.driver_name);
        carNumber = (EditText) findViewById(R.id.car_number);
        identityCard = (EditText) findViewById(R.id.identity_card);
        driverPhoneNumber = (EditText) findViewById(R.id.driver_phone_number);
        distributeOrder = (TextView) findViewById(R.id.distribute_order);
        quickDispatch = (TextView) findViewById(R.id.quick_dispatch);

        Intent intent = getIntent();
        transportDispatch = (TransportDispatch) intent.getExtras().getSerializable("distribute_order_item");
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.back: //返回
                finish();
                break;
            case R.id.driver_name: //司机名称
                intent = new Intent(TransportOrderDispatchDetailActivity.this, SelectDriverActivity.class);
                startActivityForResult(intent, 0x15);
                break;
            case R.id.distribute_order: //运单调度
                new AlertDialog.Builder(TransportOrderDispatchDetailActivity.this).setTitle("派单")
                        .setMessage("确认调度吗？")
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestDistributeOrder();
                            }
                        })
                        .setNegativeButton("否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
                break;
            case R.id.quick_dispatch: //快速调度
                String orderFlowNumber = orderFlowWaterNumber.getText().toString();
                intent = new Intent(TransportOrderDispatchDetailActivity.this, FastDistributeActivity.class);
                intent.putExtra("distribute_order_number", orderFlowNumber);
                intent.putExtra("order_id", transportDispatch.getId());
                startActivityForResult(intent, 0x12);
                break;
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x10: //货物名称请求返回
                    //requestDistributeOrderNumber(); //请求调度单号
                    setData(); //设置数据
                    loadingDialog.dismissDialog();
                    break;
                case 0x11: //调度单号返回
//                    setData(); //设置数据
//                    loadingDialog.dismissDialog();
                    break;
                case 0x15: //调度成功返回
                    Toast.makeText(TransportOrderDispatchDetailActivity.this, "调度成功", Toast.LENGTH_SHORT).show();
                    setResult(0x35);
                    finish();
                    break;
            }
        }
    };

    /**
     * 请求货物名称
     */
    private void requestGoodsName() {
        String url = Constant.ENTRANCE_PREFIX_v1 + "inqueryOrderDetailListByMainOrderId.json?sessionUuid=" + sessionUuid +
                "&releationId=" + transportDispatch.getId();
        loadingDialog = new CusProgressDialog(TransportOrderDispatchDetailActivity.this, "正在拼命加载...");
        loadingDialog.getLoadingDialog().show();
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if(!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        //角色类型请求失败
                        //Toast.makeText(getApplicationContext(), "角色用户请求失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String goodsName = "";
                    jsonArray = jsonObject.getJSONArray("rows");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        if(jsonArray.length() == 1) {
                            goodsName += jsonArray.getString(i);
                        } else {
                            if(i == jsonArray.length() - 1) {
                                goodsName += jsonArray.getString(i);
                            }
                            goodsName += jsonArray.getString(i) + "、";
                        }
                    }
                    transportDispatch.setGoodsName(goodsName);
                    Message message = Message.obtain();
                    message.what = 0x10;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 调度单号
     */
    private void requestDistributeOrderNumber() {
        String url = Constant.ENTRANCE_PREFIX_v1 + "inqueryOrderNumber.json?sessionUuid=" + sessionUuid +
                "&type=1";
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if (!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        //角色类型请求失败
                        //Toast.makeText(getApplicationContext(), "角色用户请求失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    jsonArray = jsonObject.getJSONArray("rows");
                    globalDistributeOrderNumber = jsonArray.getString(0);
                    transportDispatch.setDistributeOrderNumber(globalDistributeOrderNumber);

                    Message message = Message.obtain();
                    message.what = 0x11;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 设置数据
     */
    private void setData() {
        orderFlowWaterNumber.setText(transportDispatch.getOrderFlowWaterNumber());
        placeOrderTime.setText(transportDispatch.getPlaceOrderDate());
        transportRoute.setText(transportDispatch.getStartAndEndAddress());
        arriveTime.setText(transportDispatch.getArriveTime());
        goodsName.setText(transportDispatch.getGoodsName());
        goodsTotalNumber.setText(transportDispatch.getTotalNumber());
        goodsTotalWeight.setText(transportDispatch.getTotalWeight());
        goodsTotalVolume.setText(transportDispatch.getTotalVolume());
        //tvDistributeOrderNumber.setText(transportDispatch.getDistributeOrderNumber());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case 0x30: //司机返回
                Bundle bundle = data.getExtras();
                driver = (Driver) bundle.getSerializable("driver");
                driverName.setText(driver.getDriverName());
                carNumber.setText(driver.getCarId());
                identityCard.setText(driver.getIdentityCard());
                driverPhoneNumber.setText(driver.getDriverPhoneNumber());
                break;
            case 0x40:
                String txDriverPhoneNumber = data.getStringExtra("driver_name");
                driverPhoneNumber.setText(txDriverPhoneNumber);
                break;
            case 0x50: //快速调度返回
                setResult(0x35);
                finish();
                break;
        }
    }

    /**
     * 调度订单
     */
    private void requestDistributeOrder() {
        String txDriverName = driverName.getText().toString();
        if(txDriverName != null && txDriverName.equals("")) {
            Toast.makeText(TransportOrderDispatchDetailActivity.this, "司机名称不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        String txCarNumber = carNumber.getText().toString();
        if(txCarNumber != null && txCarNumber.equals("")) {
            Toast.makeText(TransportOrderDispatchDetailActivity.this, "车牌号不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        String txIdentityCard = identityCard.getText().toString();
        if(txIdentityCard != null && txIdentityCard.equals("")) {
            Toast.makeText(TransportOrderDispatchDetailActivity.this, "身份证不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        String txPhoneNumber = driverPhoneNumber.getText().toString();
        if(txPhoneNumber != null && txPhoneNumber.equals("")) {
            Toast.makeText(TransportOrderDispatchDetailActivity.this, "司机电话不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        String orderFlowNumber = orderFlowWaterNumber.getText().toString();
        String url = Constant.ENTRANCE_PREFIX_v1 + "appGenerateControlOrder.json?sessionUuid=" + sessionUuid + "&controlNum=" + orderFlowNumber +
                "&trunckNum=" + driver.getCarId() + "&realAccoutId=" + driver.getDriverId() + "&driverName=" + driver.getDriverName() +
                "&driverTel=" + driver.getDriverPhoneNumber() + "&idCard=" + driver.getIdentityCard() + "&orderId=" + transportDispatch.getId();

        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>(){

            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if (!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        //Toast.makeText(getApplicationContext(), "调度成功", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Message message = Message.obtain();
                    message.what = 0x15;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


}
