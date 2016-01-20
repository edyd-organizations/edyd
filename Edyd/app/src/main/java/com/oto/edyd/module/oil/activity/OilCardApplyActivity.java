package com.oto.edyd.module.oil.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.oto.edyd.R;
import com.oto.edyd.SelectDepartmentActivity;
import com.oto.edyd.model.SelectDepartment;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 功能：油卡申请
 * 文件名：com.oto.edyd.module.oil.activity.OilCardApplyActivity.java
 * 创建时间：2015/10/27.
 * 作者：yql
 */
public class OilCardApplyActivity extends Activity implements View.OnClickListener{

    //--------------基本View控件----------------
    private LinearLayout back; //返回
    private EditText carId; //车牌号
    private EditText user; //使用人
    private RadioGroup isNeedPassword; //是否需要密码
    private EditText setPassword; //密码设置
    private TextView txPasswordFlag; //是否需要密码标识
    private RadioGroup isSmsRemind; //是否需要短信提醒
    private EditText phoneNumber; //联系方式
    private Spinner limitOil; //油品限定
    private Spinner oilSupplier; //油卡供应商
    private EditText limitDailyOilAmount; //限日加油金额
    private LinearLayout companyDepartment; //公司部门布局
    private EditText company; //公司
    private EditText etDepartment; //部门
    private EditText consignAddress; //寄存地址
    private TextView submit; //提交
    private RadioButton needPassword; //不需要密码
    private RadioButton needMessage; //需要短信

    //--------------变量----------------
    private Context context; //上下文对象
    private SelectDepartment selectDepartment;
    private CusProgressDialog oilDialog; //过度
    private boolean isPassword = true; //是否需要密码
    private boolean isMessage = true; //是否需要短信
    private Common common; //偏好文件LOGIN_PREFERENCES_FILE
    private final static int HANDLER_AUTH_CODE = 0x10; //认证车牌号不存在返回码
    private final static int HANDLER_APPLY_SUCCESS_CODE = 0x11; //油卡申请成功返回码


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oil_card_application);
        init(); //初始化数据
    }

    /**
     * 初始化数据
     */
    private void init() {
        initFields();
        initListener();
        initDisplay();
    }

    /**
     * 初始化字段
     */
    private void initFields() {
        context = OilCardApplyActivity.this;
        back = (LinearLayout) findViewById(R.id.back);
        carId = (EditText) findViewById(R.id.car_id);
        user = (EditText) findViewById(R.id.user);
        isNeedPassword = (RadioGroup) findViewById(R.id.is_need_password);
        setPassword = (EditText) findViewById(R.id.set_password);
        txPasswordFlag = (TextView) findViewById(R.id.tx_password_flag);
        isSmsRemind = (RadioGroup) findViewById(R.id.is_sms_remind);
        phoneNumber = (EditText) findViewById(R.id.phone_number);
        limitOil = (Spinner) findViewById(R.id.limit_oil);
        oilSupplier = (Spinner) findViewById(R.id.oil_supplier);
        limitDailyOilAmount = (EditText) findViewById(R.id.limit_daily_oil_amount);
        companyDepartment = (LinearLayout) findViewById(R.id.company_department);
        company = (EditText) findViewById(R.id.company);
        etDepartment = (EditText) findViewById(R.id.department);
        consignAddress = (EditText) findViewById(R.id.consign_address);
        submit = (TextView) findViewById(R.id.submit);
        needPassword = (RadioButton) findViewById(R.id.need_password);
        needMessage = (RadioButton) findViewById(R.id.need_message);
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        isNeedPassword.check(needPassword.getId());
        isSmsRemind.check(needMessage.getId());
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        submit.setOnClickListener(this);
        back.setOnClickListener(this);
        etDepartment.setOnClickListener(this);
        isNeedPassword.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int radioButtonId = group.getCheckedRadioButtonId();
                RadioButton rb = (RadioButton) findViewById(radioButtonId);
                String isCheckText = rb.getText().toString();

                if (isCheckText.equals("是")) {
                    isPassword = true;
                    setPassword.setEnabled(true);
                    txPasswordFlag.setVisibility(View.VISIBLE);
                    setApplyButtonDisable();
                } else {
                    isPassword = false;
                    setPassword.setText("");
                    setPassword.setEnabled(false);
                    txPasswordFlag.setVisibility(View.GONE);

                    String txCarId = carId.getText().toString(); //车牌号
                    //判断车牌号是否为空
                    if (!TextUtils.isEmpty(txCarId)) {
                        //密码不为空
                        String txPhoneNumber = phoneNumber.getText().toString(); //联系人
                        //判断联系人是否为空
                        if (!TextUtils.isEmpty(txPhoneNumber)) {
                            //联系人不为空
                            String txConsignAddress = consignAddress.getText().toString(); //寄存地址
                            //判断寄存地址是否为空
                            if(!TextUtils.isEmpty(txConsignAddress)) {
                                //寄存地址不为空
                                if(getEnterpriseAccountType() != 0) {
                                    String txDepartmentName = etDepartment.getText().toString(); //部门名称
                                    //判断部门名称是否为空
                                    if(!TextUtils.isEmpty(txDepartmentName)) {
                                        //部门名称不为空
                                        //设置按钮可用，颜色为橙色
                                        setApplyButtonEnable();
                                    }
                                } else {
                                    setApplyButtonEnable();
                                }
                            }
                        }
                    } else {
                        setApplyButtonDisable();
                    }
                }

            }
        });
        isSmsRemind.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int radioButtonId = group.getCheckedRadioButtonId();
                RadioButton rb = (RadioButton) findViewById(radioButtonId);
                String isCheckText = rb.getText().toString();

                if (isCheckText.equals("是")) {
                    isMessage = true;
                } else {
                    isMessage = false;
                }
            }
        });

        //车牌号监听
        carId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString();
                //判断车牌号是否为空
                if(!TextUtils.isEmpty(content)) {
                    //车牌号不为空
                    String txPhoneNumber = phoneNumber.getText().toString(); //联系人
                    //判断联系人是否为空
                    if(!TextUtils.isEmpty(txPhoneNumber)) {
                        //联系人不为空
                        //是否需要判断密码
                        if(isPassword) {
                            String txPassword = setPassword.getText().toString(); //密码
                            //判断密码是否为空
                            if(!TextUtils.isEmpty(txPassword)) {
                                //密码不为空
                                String txConsignAddress = consignAddress.getText().toString(); //寄存地址
                                //判断寄存地址是否为空
                                if(!TextUtils.isEmpty(txConsignAddress)) {
                                    //寄存地址不为空
                                    if(getEnterpriseAccountType() != 0) {
                                        String txDepartmentName = etDepartment.getText().toString(); //部门名称
                                        //判断部门名称是否为空
                                        if(!TextUtils.isEmpty(txDepartmentName)) {
                                            //部门名称不为空
                                            //设置按钮可用，颜色为橙色
                                            setApplyButtonEnable();
                                        }
                                    } else {
                                        setApplyButtonEnable();
                                    }
                                }
                            }
                        } else {
                            setApplyButtonEnable();
                        }
                    }
                } else {
                    //车牌号为空，设置按钮不可用，颜色为灰色
                    setApplyButtonDisable();
                }
            }
        });

        //手机号码监听
        phoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString();
                //判断联系人是否为空
                if (!TextUtils.isEmpty(content)) {
                    //联系人不为空
                    String txCarId = carId.getText().toString(); //车牌号
                    //判断车牌号是否为空
                    if (!TextUtils.isEmpty(txCarId)) {
                        //车牌号不为空
                        //是否需要判断密码
                        if (isPassword) {
                            String txPassword = setPassword.getText().toString(); //密码
                            //判断密码是否为空
                            if (!TextUtils.isEmpty(txPassword)) {
                                //密码不为空
                                String txConsignAddress = consignAddress.getText().toString(); //寄存地址
                                //判断寄存地址是否为空
                                if(!TextUtils.isEmpty(txConsignAddress)) {
                                    //寄存地址不为空
                                    if(getEnterpriseAccountType() != 0) {
                                        String txDepartmentName = etDepartment.getText().toString(); //部门名称
                                        //判断部门名称是否为空
                                        if(!TextUtils.isEmpty(txDepartmentName)) {
                                            //部门名称不为空
                                            //设置按钮可用，颜色为橙色
                                            setApplyButtonEnable();
                                        }
                                    } else {
                                        setApplyButtonEnable();
                                    }
                                }
                            }
                        } else {
                            setApplyButtonEnable();
                        }
                    }
                } else {
                    //联系人为空，设置按钮不可用，颜色为灰色
                    setApplyButtonDisable();
                }
            }
        });

        setPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //是否需要判断密码
                if (isPassword) {
                    String content = s.toString();
                    if (!TextUtils.isEmpty(content)) {
                        String txCarId = carId.getText().toString(); //车牌号
                        //判断车牌号是否为空
                        if (!TextUtils.isEmpty(txCarId)) {
                            //密码不为空
                            String txPhoneNumber = phoneNumber.getText().toString(); //联系人
                            //判断联系人是否为空
                            if (!TextUtils.isEmpty(txPhoneNumber)) {
                                //联系人不为空
                                String txConsignAddress = consignAddress.getText().toString(); //寄存地址
                                //判断寄存地址是否为空
                                if(!TextUtils.isEmpty(txConsignAddress)) {
                                    //寄存地址不为空，设置按钮可用，颜色为橙色
                                    if(getEnterpriseAccountType() != 0) {
                                        String txDepartmentName = etDepartment.getText().toString(); //部门名称
                                        //判断部门名称是否为空
                                        if(!TextUtils.isEmpty(txDepartmentName)) {
                                            //部门名称不为空
                                            //设置按钮可用，颜色为橙色
                                            setApplyButtonEnable();
                                        }
                                    } else {
                                        setApplyButtonEnable();
                                    }
                                }
                            }
                        }
                    } else {
                        //密码为空，设置按钮不可用，颜色为灰色
                        setApplyButtonDisable();
                    }
                }
            }
        });

        consignAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString(); //寄存地址
                //判断寄存地址是否为空
                if (!TextUtils.isEmpty(content)) {
                    //寄存地址不为空
                    String txCarId = carId.getText().toString(); //车牌号
                    //判断车牌号是否为空
                    if (!TextUtils.isEmpty(txCarId)) {
                        //车牌号不为空
                        String txPhoneNumber = phoneNumber.getText().toString(); //联系人
                        //判断联系人是否为空
                        if (!TextUtils.isEmpty(txPhoneNumber)) {
                            //联系人不为空
                            //是否需要判断密码
                            if (isPassword) {
                                String txPassword = setPassword.getText().toString(); //密码
                                //判断密码是否为空
                                if (!TextUtils.isEmpty(txPassword)) {
                                    //密码不为空，设置按钮可用，颜色为橙色
                                    if (getEnterpriseAccountType() != 0) {
                                        String txDepartmentName = etDepartment.getText().toString(); //部门名称
                                        //判断部门名称是否为空
                                        if (!TextUtils.isEmpty(txDepartmentName)) {
                                            //部门名称不为空
                                            //设置按钮可用，颜色为橙色
                                            setApplyButtonEnable();
                                        }
                                    } else {
                                        setApplyButtonEnable();
                                    }
                                }
                            } else {
                                setApplyButtonEnable();
                            }
                        }
                    }

                } else {
                    //寄存地址为空
                    setApplyButtonDisable();
                }
            }
        });

        etDepartment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString(); //部门
                //判断部门是否为空
                if (!TextUtils.isEmpty(content)) {
                    //部门不为空
                    String txCarId = carId.getText().toString(); //车牌号
                    //判断车牌号是否为空
                    if (!TextUtils.isEmpty(txCarId)) {
                        //车牌号不为空
                        String txPhoneNumber = phoneNumber.getText().toString(); //联系人
                        //判断联系人是否为空
                        if (!TextUtils.isEmpty(txPhoneNumber)) {
                            //联系人不为空
                            //是否需要判断密码
                            if (isPassword) {
                                String txPassword = setPassword.getText().toString(); //密码
                                //判断密码是否为空
                                if (!TextUtils.isEmpty(txPassword)) {
                                    //密码不为空
                                    String txConsignAddress = consignAddress.getText().toString(); //寄存地址
                                    //判断寄存地址是否为空
                                    if (!TextUtils.isEmpty(txConsignAddress)) {
                                        //寄存地址不为空，设置按钮可用，颜色为橙色
                                        setApplyButtonEnable();
                                    }
                                }
                            } else {
                                setApplyButtonEnable();
                            }
                        }
                    }
                } else {
                    //部门为空
                    setApplyButtonDisable();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.submit:
                //验证车牌号
                String cardId = carId.getText().toString(); //车牌号
                confirmCarNumber(cardId);
                break;
            case R.id.back: //返回
                finish();
                break;
            case R.id.department: //部门
                intent = new Intent(getApplicationContext(), SelectDepartmentActivity.class);
                startActivityForResult(intent, 0x20);
                break;
        }
    }

    /**
     * 验证车牌号
     * @param cardId 车牌号
     */
    private void confirmCarNumber(String cardId) {
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        String url = Constant.ENTRANCE_PREFIX + "inqueryOilCardByCarId.json?sessionUuid=" + sessionUuid + "&carId=" + cardId;
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                common.showToast(context, "车牌号验证异常");
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if(!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        common.showToast(context, "车牌号验证失败");
                        return;
                    }
                    jsonArray = jsonObject.getJSONArray("rows");
                    Message message = Message.obtain();
                    if(jsonArray.length() > 0) {
                        common.showToast(context, "卡号已注册");
                    } else {
                        message.what = HANDLER_AUTH_CODE; //卡号不存在
                    }
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * 显示初始化控制
     */
    private void initDisplay() {
        boolean isHide = true; //是否隐藏公司和部门，*管理员不隐藏，其他隐藏
        switch (Integer.valueOf(getEnterpriseAccountType())) {
            case 0:
                isHide = true;
                break;
            default:
                isHide = false;
        }
        if(isHide) {
            companyDepartment.setVisibility(View.GONE);

        } else {
            companyDepartment.setVisibility(View.VISIBLE);
            String enterpriseName = common.getStringByKey("enterprise_name");
            company.setText(enterpriseName);
        }
    }

    /**
     * 校验表单
     */
    private void verifyForm() {
        boolean isCheckedNeedPassword = isPassword; //检查是否需要密码
        String carNumber = carId.getText().toString(); //车牌号
        String mPhone = phoneNumber.getText().toString(); //手机号码
        Pattern pattern = null;
        Matcher matcher = null;

        //验证车牌号格式是否正确
        pattern = Pattern.compile("^[\\u4e00-\\u9fa5]{1}[A-Z]{1}[A-Z_0-9]{5}$");
        matcher = pattern.matcher(carNumber);
        if(!matcher.matches()) {
            common.showToast(context, "车牌号格式不正确");
            return;
        }
        //验证密码位数是否小于四位
        if(isCheckedNeedPassword) {
            String password = (setPassword.getText().toString()).trim();
            password = password.replaceAll(" ", "");
            if(password.length() < 4) {
                common.showToast(context, "密码位数要求4-6位整数");
                return;
            }
        }
        //验证手机号格式是否正确
        pattern = Pattern.compile("^(0|86|17951)?(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])[0-9]{8}$"); //匹配手机
        matcher = pattern.matcher(mPhone);
        if(!matcher.matches()) {
            common.showToast(context, "手机号码格式不对-6位整数");
            return;
        }
        applyOilCard(); //申请油卡
    }

    /**
     * 申请油卡
     */
    private void applyOilCard() {
        int is_sms, is_need_password; //是否需要短信、是否需要密码
        String txCarId = carId.getText().toString(); //车牌号
        String txUser = user.getText().toString(); //使用人
        String txPassword = setPassword.getText().toString(); //密码
        String mobilePhone = phoneNumber.getText().toString(); //联系人
        String txLimitOil = limitOil.getSelectedItem().toString(); //油品限定
        txLimitOil = txLimitOil.replace("#", "%23");
        String txOilSupplier = oilSupplier.getSelectedItem().toString(); //油卡供应商
        String everyDailyAmount = limitDailyOilAmount.getText().toString(); //每日加油量
        String txConsignAddress = consignAddress.getText().toString(); //寄存地址

        if(isPassword) { //需要密码
            is_need_password = 1;
        } else { //不需要密码
            is_need_password = 2;
        }

        if(isMessage) { //需要短信
            is_sms = 1;
        } else { //不需要短信
            is_sms = 2;
        }

        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        //个人
        String accountID = common.getStringByKey("ACCOUNT_ID");
        String userName = common.getStringByKey(Constant.USER_NAME); //用户账号
        //公司
        String enterpriseID = common.getStringByKey(Constant.ENTERPRISE_ID);
        String enterpriseName = common.getStringByKey(Constant.ENTERPRISE_NAME);
        String url = Constant.ENTRANCE_PREFIX + "insertOilCard.json?carId="+txCarId + "&oilHolder=" + txUser +"&sessionUuid="+sessionUuid+"&setPwd="+String.valueOf(is_need_password)+"&pwd="+txPassword+
                "&setMessage="+is_sms+"&mobile="+mobilePhone +"&oilNum="+txLimitOil+"&oilType=" + txOilSupplier +"&everyDayOil="+everyDailyAmount+
                "&accountType=0" + "&recAddress=" + txConsignAddress;
        if(enterpriseID.equals("0")) {
            url = url + "&realAccountId="+accountID + "&accountMobile="+userName +"&enterpriseId=" + enterpriseID;
        } else {
            url = url + "&tenantId="+selectDepartment.getTenantId() + "&orgId="+selectDepartment.getOrgId()+ "&orgCode="+selectDepartment.getOrgCode()+
                    "&orgName=" + selectDepartment.getText() +"&enterpriseId=" + enterpriseID + "&enterpriseName=" + enterpriseName;
        }

        OkHttpClientManager.getAsyn(url, new ApplyResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                common.showToast(context, "油卡申请异常");
            }

            @Override
            public void onResponse(String response) {
                //请求成功
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(response);
                    if (!jsonObject.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        //验证申请油品是否成功
                        common.showToast(context, "油卡申请失败");
                        return;
                    }
                    Message message = Message.obtain();
                    message.what = HANDLER_APPLY_SUCCESS_CODE;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public abstract class ApplyResultCallback<T> extends OkHttpClientManager.ResultCallback<T>{
        @Override
        public void onBefore() {
            //请求之前操作
            oilDialog = new CusProgressDialog(context, "正在申请...");
            oilDialog.getLoadingDialog().show();
        }

        @Override
        public void onAfter() {
            //请求之后要做的操作
            oilDialog.getLoadingDialog().dismiss();
        }
    }

    /**
     * 判断是否是企业用户
     * @return
     */
    private int getEnterpriseAccountType() {
        String role = common.getStringByKey(Constant.ENTERPRISE_ID);
        if(role.equals("")){
            common.showToast(context, "企业ID为空");
            return 4;
        }
        return Integer.valueOf(role);
    }

    /**
     * activity成功返回
     * @param requestCode 启动码
     * @param resultCode 返回码
     * @param data 数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case 0x22:
                selectDepartment = (SelectDepartment) data.getSerializableExtra("department");
                etDepartment.setText(selectDepartment.getText());
                break;
        }
    }

    /**
     * 线程通讯
     */
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_AUTH_CODE: //不存在
                    verifyForm(); //校验表单
                    break;
                case HANDLER_APPLY_SUCCESS_CODE: //油卡申请成功返回
                    common.showToast(context, "申请成功");
                    finish();
                    break;
            }
        }
    };

    /**
     * 设置申请办卡按钮可用
     */
    private void setApplyButtonEnable() {
        submit.setEnabled(true); //设置按钮可用
        submit.setBackgroundResource(R.drawable.border_corner_login_enable);
    }

    /**
     * 设置申请办卡按钮不可用
     */
    private void setApplyButtonDisable() {
        submit.setBackgroundResource(R.drawable.border_corner_login);
        submit.setEnabled(false); //设置按钮不可用
    }
}
