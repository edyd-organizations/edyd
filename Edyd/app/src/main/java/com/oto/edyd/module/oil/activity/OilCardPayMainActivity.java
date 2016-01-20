package com.oto.edyd.module.oil.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oto.edyd.OilCarSetPasswordActivity;
import com.oto.edyd.R;
import com.oto.edyd.module.oil.activity.ChinaMerchantsBankWebPayActivity;
import com.oto.edyd.module.oil.model.MerchantsBankOrder;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.NumberFormat;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 功能：油卡充值主页
 * 文件名：com.oto.edyd.OilCardPayMainActivity.java
 * 创建时间：2015/12/14
 * 作者：yql
 */
public class OilCardPayMainActivity extends Activity implements View.OnClickListener {

    //---------------------View基本控件---------------------
    private LinearLayout back; //返回
    private EditText etAccount; //金额
    //private EditText etPayPassword; //支付密码
    //private TextView forgetPayPassword; //忘记支付密码
    private TextView payWho; //支付给谁
    private TextView btPay; //支付按钮

    //---------------------变量---------------------
    private Common common; //偏好共享对象
    private Context context; //上下文对象
    //招行对接信息
    private MerchantsBankOrder merchantsBankOrder; //订单实体
    private final static String FIX_BRANCH_ID = "0592"; //商户开户分行号
    private final static String FIX_CoNo = "002968"; //商户号
    //返回码
    private final static int HANDLER_ORDER_NUMBER_REQUEST_EXCEPTION = 0x09; //订单号请求异常返回码
    //private final static int HANDLER_PASSWORD_VERIFY_SUCCESS = 0x10; //密码认证成功
    //private final static int HANDLER_PASSWORD_VERIFY_FAIL = 0x11; //密码认证失败
    private final static int HANDLER_PREPARE_PAY_DATA_SAVE_SUCCESS = 0x12; //预充值数据保存成功返回码
    private final static int HANDLER_CHECK_CODE_BACK = 0x13; //校验码返回成功
    private final static int HANDLER_CONFIRM_ACCOUNT_SUCCESS = 0x14; //验证用户账户存在返回码
    private final static int BACK_CODE = 0x20; //返回码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.china_merchants_bank_pay);
        init(); //初始化数据
    }

    /**
     * 初始化数据
     */
    private void init() {
        initFields(); //初始化字段
        initListener(); //初始化监听
        requestOrderNumber(); //请求订单号
        String enterpriseName = common.getStringByKey(Constant.ENTERPRISE_NAME);
        payWho.setText("注：支付对象为" + enterpriseName+ "账户");
    }

    /**
     * 初始化字段
     */
    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
        etAccount = (EditText) findViewById(R.id.pay_amount);
        //etPayPassword = (EditText) findViewById(R.id.pay_password);
        //forgetPayPassword = (TextView) findViewById(R.id.forget_pay_password);
        payWho = (TextView) findViewById(R.id.pay_who);
        btPay = (TextView) findViewById(R.id.pay);
        context = OilCardPayMainActivity.this;
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, context.MODE_PRIVATE));
        merchantsBankOrder = new MerchantsBankOrder();
        merchantsBankOrder.setBranchId(FIX_BRANCH_ID);
        merchantsBankOrder.setCoNo(FIX_CoNo);
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        back.setOnClickListener(this);
        btPay.setOnClickListener(this);
        //forgetPayPassword.setOnClickListener(this);
        etAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString(); //金额
                if (!TextUtils.isEmpty(content)) { //判断金额是否为空
                    //金额不为空
                    setPayButtonEnabled();
                } else {
                    //金额为空，设置按钮不可用
                    setPayButtonDisabled();
                }
            }
        });
//        etPayPassword.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {}
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                String content = s.toString(); //密码
//                if (!TextUtils.isEmpty(content)) { //判断密码是否为空
//                    //密码不为空
//                    String account = etAccount.getText().toString(); //金额
//                    if (!TextUtils.isEmpty(account)) { //判断金额是否为空
//                        //金额不为空
//                        setPayButtonEnabled();
//                    }
//                } else {
//                    //密码为空，设置按钮不可用
//                    setPayButtonDisabled();
//                }
//            }
//        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: //返回
                finish();
                break;
            case R.id.pay: //支付
                confirmAccountIsExist(); //验证账户是否存在
                break;
//            case R.id.forget_pay_password: //忘记支付密码
//                Intent intent=new Intent(this,OilCarSetPasswordActivity.class);
//                startActivity(intent);
//                break;
        }
    }

    /**
     * 请求系统订单号
     */
    private void requestOrderNumber() {
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        String url = Constant.ENTRANCE_PREFIX_v1 + "appGeneratorOrderNum.json?sessionUuid=" + sessionUuid;

        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {

            @Override
            public void onError(Request request, Exception e) {
                //订单号请求异常
                Message message = Message.obtain();
                message.what = HANDLER_ORDER_NUMBER_REQUEST_EXCEPTION;
                handler.sendMessage(message);
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;

                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status"); //获取返回状态
                    if (!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        //订单号请求失败
                        common.showToast(context, "订单号请求失败");
                        return;
                    }
                    String tBillNo = jsonObject.getJSONArray("rows").getString(0);
                    merchantsBankOrder.setBillNo(tBillNo);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 验证当前登录用户账户是否存在
     */
    private void confirmAccountIsExist() {
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID); //用户唯一标示
        String enterpriseId = common.getStringByKey(Constant.ENTERPRISE_ID); //企业ID
        String orgCode = common.getStringByKey(Constant.ORG_CODE); //组织ID
        String accountId = common.getStringByKey(Constant.ACCOUNT_ID); //账户ID

        String url = Constant.ENTRANCE_PREFIX_v1 + "isShengAccountValidate.json?sessionUuid=" + sessionUuid + "&enterpriseId=" + enterpriseId +
                "&accountId=" + accountId;
        if(!TextUtils.isEmpty(orgCode)) {
            url = url + "&OrgCode=" + orgCode;
        }
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {

            @Override
            public void onError(Request request, Exception e) {
                common.showToast(context, "验证账户请求异常");
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if(!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        common.showToast(context, "验证账户请求错误");
                    }
                    jsonArray = jsonObject.getJSONArray("rows");
                    boolean isExist = jsonArray.getBoolean(0);
                    Message message = Message.obtain();
                    if(isExist) {
                        message.what = HANDLER_CONFIRM_ACCOUNT_SUCCESS;
                        handler.sendMessage(message);
                    } else {
                        common.showToast(context, "账户不存在，请申请");
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 验证支付密码
     */
//    private void verifyPayPassword() {
//        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
//        String accountId = common.getStringByKey("ACCOUNT_ID"); //账户ID
//        //String payPassword = etPayPassword.getText().toString(); //支付密码
//
//
//        String url = Constant.ENTRANCE_PREFIX + "testPayPassword.json?sessionUuid=" + sessionUuid + "&accountId=" + accountId + "&payPassword=";
//        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
//
//            @Override
//            public void onError(Request request, Exception e) {
//                common.showToast(context, "请求异常");
//            }
//
//            @Override
//            public void onResponse(String response) {
//                JSONObject jsonObject;
//                JSONArray jsonArray;
//                try {
//                    jsonObject = new JSONObject(response);
//                    String status = jsonObject.getString("status"); //返回状态
//                    if(!status.equals(Constant.LOGIN_SUCCESS_STATUS)) { //判断是否成功返回
//                        common.showToast(context, "验证失败");
//                    }
//                    jsonArray = jsonObject.getJSONArray("rows");
//                    boolean flag = jsonArray.getBoolean(0); //认证成功返回true， false不成功
//                    Message message = Message.obtain();
//                    if(flag) {
//                        message.what = HANDLER_PASSWORD_VERIFY_SUCCESS; //支付密码认证成功
//                    } else {
//                        message.what = HANDLER_PASSWORD_VERIFY_FAIL; //支付密码认证失败
//                    }
//                    handler.sendMessage(message);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

    /**
     * 请求校验码
     */
    private void requestCheckCode() {
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String billNoDate = sdf.format(date);
        merchantsBankOrder.setDate(billNoDate);

        String url = Constant.ENTRANCE_PREFIX_v1 + "appBulidVerifyCode.json?sessionUuid=" + sessionUuid +
                "&branchId=" + merchantsBankOrder.getBranchId() + "&coNo=" + merchantsBankOrder.getCoNo() +
                "&amount=" +merchantsBankOrder.getAmount() + "&date=" + merchantsBankOrder.getDate() +
                "&billNo=" + merchantsBankOrder.getBillNo() + "&merchantPara=" + "&merchantUrl=" + Constant.CMB_CALLBACK_ADDRESS;
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {

            @Override
            public void onError(Request request, Exception e) {
                common.showToast(context, "校验码请求异常");
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if(!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        common.showToast(context, "校验码请求失败");
                    }
                    jsonArray = jsonObject.getJSONArray("rows");
                    merchantsBankOrder.setMerchantCode(jsonArray.getString(0));
                    Message message = Message.obtain();
                    message.what = HANDLER_CHECK_CODE_BACK;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * 提交订单
     */
    private void postOrder() {
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        String accountId = common.getStringByKey("ACCOUNT_ID"); //账户ID
        String enterpriseId = common.getStringByKey(Constant.ENTERPRISE_ID);
        String url = "";

        if(enterpriseId.equals("0")) {
            url = Constant.ENTRANCE_PREFIX_v1 + "appInsertBillInfo.json?sessionUuid=" + sessionUuid + "&accountId=" + accountId +
                    "&enterpriseId=" + enterpriseId + "&branchId=" + FIX_BRANCH_ID + "&cono=" + FIX_CoNo +
                    "&billNo=" + merchantsBankOrder.getBillNo() + "&billNoDate=" + merchantsBankOrder.getDate() + "&amount=" + merchantsBankOrder.getAmount();
        } else {
            String orgCode = common.getStringByKey(Constant.ORG_CODE);
            url = Constant.ENTRANCE_PREFIX_v1 + "appInsertBillInfo.json?sessionUuid=" + sessionUuid + "&accountId=" + accountId +
                    "&enterpriseId=" + enterpriseId + "&orgCode=" + orgCode + "&branchId=" + FIX_BRANCH_ID + "&cono=" + FIX_CoNo +
            "&billNo=" +merchantsBankOrder.getBillNo() + "&billNoDate=" + merchantsBankOrder.getDate() + "&amount=" + merchantsBankOrder.getAmount();
        }

        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>(){

            @Override
            public void onError(Request request, Exception e) {
                common.showToast(context, "充值异常");
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;

                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if(!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        common.showToast(context, "充值数据保存异常");
                    }
                    Message message = Message.obtain();
                    message.what = HANDLER_PREPARE_PAY_DATA_SAVE_SUCCESS;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 线程通讯
     */
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_ORDER_NUMBER_REQUEST_EXCEPTION: //订单号请求异常
                    common.showToast(context, "订单号请求异常");
                    finish();
                    break;
//                case HANDLER_PASSWORD_VERIFY_SUCCESS: //密码认证成功
//                    confirmPayDialog(); //弹出确认框
//                    break;
//                case HANDLER_PASSWORD_VERIFY_FAIL: //密码认证失败
//                    common.showToast(context, "密码不正确");
//                    break;
                case HANDLER_CHECK_CODE_BACK: //校验码返回成功
                    postOrder(); //提交订单
                    break;
                case HANDLER_PREPARE_PAY_DATA_SAVE_SUCCESS: //预充值数据成功返回
                    Intent intent = new Intent(OilCardPayMainActivity.this, ChinaMerchantsBankWebPayActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("pay_order", merchantsBankOrder);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, BACK_CODE);
                    break;
                case HANDLER_CONFIRM_ACCOUNT_SUCCESS: //验证用户账户存在返回码
                    //verifyPayPassword(); //验证支付密码
                    confirmPayDialog();
                    break;
            }
        }
    };

    /**
     * 设置注册按钮可用
     */
    private void setPayButtonEnabled() {
        btPay.setEnabled(true); //设置按钮可用
        btPay.setBackgroundResource(R.drawable.border_corner_login_enable);
    }

    /**
     * 设置注册按钮不可用
     */
    private void setPayButtonDisabled() {
        btPay.setBackgroundResource(R.drawable.border_corner_login);
        btPay.setEnabled(false); //设置按钮不可用
    }

    private class PayResultCallback extends OkHttpClientManager.ResultCallback<String>{
        private int requestSequence; //请求次序

        public PayResultCallback() {
            this.requestSequence = requestSequence;
        }

        @Override
        public void onError(Request request, Exception e) {}

        @Override
        public void onResponse(String response) {}
    };

    /**
     * 格式化金额
     * @param amount
     * @return 格式化后的金额，要求保留两位小数
     */
    private String formatAmount(String amount) {
        String content = ""; //声明格式化字符串
        NumberFormat numberFormat = new NumberFormat();
        int pointIndex = amount.indexOf("."); //小数点的位置，不存在返归-1
        if(pointIndex > 0) {  //判断是否存在小数
            //存在
            int pointAfterLength = amount.substring(pointIndex + 1).length(); //小数位数长度
            switch (pointAfterLength) { //判断小数位长度
                case 0:
                    content = "ERROR_FORMAT"; //有小数点，后无小数
                    break;
                case 1:
                    content = amount + "0"; //有一位小数，再补0
                    break;
                case 2:
                    content = amount; //两位小数，正确格式
                    break;
                default:
                    content = "OVER_LENGTH"; //小数点位数超过两位
                    break;
            }
        } else {
            //不存在小数点，默认两位小数位补00
            content =  amount + "." +numberFormat.outputDoubleNumber(0);
        }
        return content;
    }

    /**
     * 确认支付对话框
     */
    private void confirmPayDialog() {
        String amount = etAccount.getText().toString(); //金额
        String array[] = amount.split("\\."); //已“.”分割整数和小数部分，进行验证

        if(array.length > 0 &&!TextUtils.isEmpty(array[0])) { //判断是否只输入“.”
            //数组有值
            Double dAmount = Double.valueOf(amount);
            if(dAmount == 0) {
                //金额不能等于零
                common.showToast(context, "数值不能为零");
                return;
            }
            String tAmount = formatAmount(amount); //验证金额格式及格式化金额
            if(tAmount.equals("OVER_LENGTH")) {
                common.showToast(context, "请保留两位小数");
                return;
            } else if(tAmount.equals("ERROR_FORMAT")) {
                common.showToast(context, "充值金额格式不正确");
                return;
            }
            merchantsBankOrder.setAmount(tAmount); //格式正确，设置值
        } else {
            common.showToast(context, "充值金额格式不正确");
            return;
        }
        String enterpriseName = common.getStringByKey(Constant.ENTERPRISE_NAME);

        new AlertDialog.Builder(OilCardPayMainActivity.this).setTitle("支付")
                .setMessage("确定支付对象为" + enterpriseName + "账户")
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestCheckCode(); //请求校验码
                    }
                })
                .setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case BACK_CODE: //网页返回
                requestOrderNumber(); //重新请求订单号
                break;
        }
    }
}
