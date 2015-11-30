package com.oto.edyd;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yql on 2015/11/27.
 */
public class FastDistributeActivity extends Activity implements View.OnClickListener {

    private LinearLayout back; //返回
    private TextView tvDistributeOrderNumber; //调度单号
    private EditText tvDriverPhoneNumber; //司机电话
    private EditText tvCardNumber; //车牌号
    private TextView tvFastDistribute; //调度
    private Common common;

    private FastDispatch fastDispatch;
    private String distributeOrderNumber;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fast_distribute);
        initFields();
        Intent intent = getIntent();
        distributeOrderNumber = intent.getStringExtra("distribute_order_number");
        orderId = intent.getStringExtra("order_id");

        tvDistributeOrderNumber.setText(distributeOrderNumber);
        back.setOnClickListener(this);
        tvCardNumber.setOnClickListener(this);
        tvFastDistribute.setOnClickListener(this);

        tvDriverPhoneNumber.addTextChangedListener(new TextWatcher() {
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
                        String carNumber = tvCardNumber.getText().toString();
                        if (carNumber != null && !(carNumber.equals(""))) {
                            tvFastDistribute.setEnabled(true); //设置按钮可用
                            tvFastDistribute.setBackgroundResource(R.drawable.border_corner_login_enable);
                        }
                    } else {
                        tvFastDistribute.setBackgroundResource(R.drawable.border_corner_login);
                        tvFastDistribute.setEnabled(false); //设置按钮不可用
                    }
                }
            }
        });

        tvCardNumber.addTextChangedListener(new TextWatcher() {
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
                        String driverPhoneNumber = tvDriverPhoneNumber.getText().toString();
                        if (driverPhoneNumber != null && !(driverPhoneNumber.equals(""))) {
                            tvFastDistribute.setEnabled(true); //设置按钮可用
                            tvFastDistribute.setBackgroundResource(R.drawable.border_corner_login_enable);
                        }
                    } else {
                        tvFastDistribute.setBackgroundResource(R.drawable.border_corner_login);
                        tvFastDistribute.setEnabled(false); //设置按钮不可用
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
        tvDistributeOrderNumber = (TextView) findViewById(R.id.tv_distribute_order_number);
        tvDriverPhoneNumber = (EditText) findViewById(R.id.tv_driver_phone_number);
        tvCardNumber = (EditText) findViewById(R.id.tv_car_number);
        tvFastDistribute = (TextView) findViewById(R.id.distribute_order);
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.back: //返回
                finish();
                break;
            case R.id.tv_car_number: //车牌号
                intent = new Intent(FastDistributeActivity.this, SelectCarActivity.class);
                startActivityForResult(intent, 0x10);
                break;
            case R.id.distribute_order: //调度
                new AlertDialog.Builder(FastDistributeActivity.this).setTitle("派单")
                        .setMessage("确认调度吗？")
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                fastDispatch();
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

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x10:
                    Toast.makeText(FastDistributeActivity.this, "调度成功", Toast.LENGTH_SHORT).show();
                    setResult(0x50);
                    finish();
                    break;
            }
        }
    };

    /**
     * 快速调度
     */
    private void fastDispatch() {
        String mPhone = tvDriverPhoneNumber.getText().toString();
        Pattern pattern = Pattern.compile("^(0|86|17951)?(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])[0-9]{8}$"); //匹配手机
        Matcher matcher = pattern.matcher(mPhone);
        if(!matcher.matches()) {
            Toast.makeText(getApplicationContext(), "手机号码格式不对", Toast.LENGTH_SHORT).show();
            return;
        }

        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        String url = Constant.ENTRANCE_PREFIX + "quickDispatch.json?sessionUuid=" + sessionUuid + "&mobile=" + mPhone
                + "&truckId=" + fastDispatch.getTruckId() + "&trunckNum=" + fastDispatch.getTruckNum() + "&operateFlag=app" +
                "&controlNum=" + distributeOrderNumber + "&orderId=" + orderId;

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

                    Message message = Message.obtain();
                    message.what = 0x10;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String trunkId = "";
        String trunkNumber = "";
        switch (resultCode) {
            case 0x20: //车牌号返回
                trunkId = data.getStringExtra("primary_id");
                trunkNumber = data.getStringExtra("car_number");
                break;
            case 0x21: //手动输入车牌号
                trunkNumber = data.getStringExtra("car_number");
                break;
        }
        tvCardNumber.setText(trunkNumber);
        fastDispatch = new FastDispatch();
        //fastDispatch.setMobile(tvDistributeOrderNumber.getText().toString());
        fastDispatch.setTruckId(trunkId);
        fastDispatch.setTruckNum(trunkNumber);
    }

    private class FastDispatch{
        private String truckId;
        private String truckNum;


        public String getTruckId() {
            return truckId;
        }

        public void setTruckId(String truckId) {
            this.truckId = truckId;
        }

        public String getTruckNum() {
            return truckNum;
        }

        public void setTruckNum(String truckNum) {
            this.truckNum = truckNum;
        }
    }
}
