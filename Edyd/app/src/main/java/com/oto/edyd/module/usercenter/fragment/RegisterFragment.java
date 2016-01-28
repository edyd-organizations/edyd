package com.oto.edyd.module.usercenter.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.oto.edyd.R;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.NetWork;
import com.oto.edyd.utils.OkHttpClientManager;
import com.oto.edyd.utils.ServiceUtil;
import com.squareup.okhttp.Request;
import com.umeng.message.UmengRegistrar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

/**
 * 功能：注册模块Fragment
 * 文件名：com.oto.edyd.usercenter.fragment.RegisterFragment.java
 * 创建时间：2015/8/31
 * 作者：yql
 */
public class RegisterFragment extends Fragment implements View.OnClickListener{

    private FragmentManager eFragmentManager; //LoginActivity布局管理器
    private LinearLayout back; //注册返回
    private EditText etRegisterPhoneNumber; //注册手机号
    private EditText etRegisterVerificationCode; //验证码
    private Button btObtainVerificationCode; //获取验证码
    private EditText etRegisterUserAlias; //注册用户别名
    private EditText etRegisterPassword; //注册密码
    private EditText confirmPassword; //用户注册
    private Button btRegister; //注册按钮
    private Button btAlreadyRegister; //已经注册
    private ImageView visiblePassword; //密码是否可见
    private VerificationCodeProgressAsyncTask asyncTask; //异步消息对象
    private boolean asyncIsOver = true; //异步线程是否结束
    private CusProgressDialog transitionDialog; //过度对话框
    private Context context; //上下文对象
    private Common common;
    private Common fixedCommon;

    private static final int MOBILE_PHONE_LENGTH  = 11; //手机号码长度
    //private static final int VERIFICATION_CODE_LENGTH = 4; //验证码长度
    private static final int MOBILE_PHONE_WITHOUT_REGISTER = 0x11; //账号未注册
    //private static final int USER_ALREADY_EXIST = 504; //用户已存在
    //private static final int USER_REGISTER_EXCEPTION = 0x13; //注册异常
    private static final int USER_REGISTER_SUCCESS = 0x14; //用户注册成功
    private static final int USER_REGISTER_SUB_COMPANY_SUCCESS = 0x15; //用户注册子公司成功返回码
    private static final int HANDLER_ACCOUNT_TYPE_SUCCESS_STATUS_CODE = 0x16; //账户类型返回码
    private static final int HANDLER_ROLE_TYPE_SUCCESS_CODE = 0x17; //角色类型请求成功返回码
    private static final int HANDLER_ACCOUNT_TYPE_SUCCESS_CODE = 0x18; //账户ID请求成功返回码
    private static final int INVALID_VERIFICATION_CODE = 0x19; //无效验证码
    private static final int VERIFICATION_AUTHENTICATE_SUCCESS = 0x20; //验证码认证成功
    private static final int HANDLER_DRIVER_ROLE = 0x21; //司机返回码
    private static final int HANDLER_DEVICE_TOKEN_CODE = 0x22; //用户友盟设备ID请求成功返回码

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.register, null); //加载布局view
        init(view); //数据初始化
        return view;
    }

    /**
     * 初始化数据
     */
    private void init(View view) {
        initFields(view); //初始化字段
        initListener(); //初始化监听器
        initSMSDK(); //初始化短信服务
    }

    /**
     * 初始化字段
     */
    private void initFields(View view) {
        back = (LinearLayout) view.findViewById(R.id.register_back);
        etRegisterPhoneNumber = (EditText) view.findViewById(R.id.register_phone_number);
        btObtainVerificationCode = (Button) view.findViewById(R.id.verification_code);
        etRegisterUserAlias = (EditText) view.findViewById(R.id.register_user_alias);
        etRegisterVerificationCode = (EditText) view.findViewById(R.id.register_verification_code);
        etRegisterPassword = (EditText) view.findViewById(R.id.register_user_password);
        confirmPassword = (EditText) view.findViewById(R.id.confirm_password);
        btRegister = (Button) view.findViewById(R.id.bt_register);
        visiblePassword = (ImageView) view.findViewById(R.id.visible_password);
        btAlreadyRegister = (Button) view.findViewById(R.id.bt_already_register);
        eFragmentManager = getActivity().getSupportFragmentManager();
        common = new Common(getActivity().getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        fixedCommon = new Common(getActivity().getSharedPreferences(Constant.FIXED_FILE, Context.MODE_PRIVATE));
        context = getActivity();
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        back.setOnClickListener(this);
        btObtainVerificationCode.setOnClickListener(this);
        btRegister.setOnClickListener(this);
        btAlreadyRegister.setOnClickListener(this);
        visiblePassword.setOnClickListener(this);

        //手机号码输入框注册监听器
        etRegisterPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString(); //手机号码
                if(!TextUtils.isEmpty(content) && asyncIsOver) { //判断手机号码是否为空，且判断异步线程是否结束
                    //手机号码不为空
                    int length = s.length(); //手机号码长度
                    if(length == MOBILE_PHONE_LENGTH && asyncIsOver) { //判断手机号码长度是否11位
                        //手机号码长度11位
                        setVerificationCodeButtonEnable(); //设置获取验证码按钮橙色且可用
                        String verificationCode = etRegisterVerificationCode.getText().toString(); //获取验证码
                        if(!TextUtils.isEmpty(verificationCode)) { //判断验证码是否为空
                            //验证码不为空
                            String alias = etRegisterUserAlias.getText().toString(); //判断用户别名是否为空
                            if(!TextUtils.isEmpty(alias)) {
                                //别名不为空
                                String password = etRegisterPassword.getText().toString(); //获取密码
                                if(!TextUtils.isEmpty(password)) { //判断密码是否为空
                                    //密码不为空
                                    setRegisterButtonEnabled(); //满足以上条件，设置注册按钮位橙色且可用
                                }
                            }
                        }
                    } else {
                        //手机号码小于11位
                        setVerificationCodeButtonDisable(); //设置获取验证码按钮灰色且不可用
                    }
                } else {
                    //手机号码为空
                    setVerificationCodeButtonDisable();
                    setRegisterButtonDisabled(); //设置注册按钮不可用
                }
            }
        });

        //验证码输入框注册监听器
        etRegisterVerificationCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString(); //验证码
                if(!TextUtils.isEmpty(content)) { //判断验证码是否为空
                    //验证码不为空
                    String mobilePhone = etRegisterPhoneNumber.getText().toString(); //手机号码
                    if(!TextUtils.isEmpty(mobilePhone)) { //判断手机号码是否为空
                        //手机号码不为空
                        String alias = etRegisterUserAlias.getText().toString(); //别名
                        if(!TextUtils.isEmpty(alias)) { //判断别名是否为空
                            //别名不为空
                            String password = etRegisterPassword.getText().toString(); //密码
                            if(!TextUtils.isEmpty(password)) { //判读密码是否为空
                                //密码不为空
                                setRegisterButtonEnabled(); //以及满足以上条件，设置注册按钮橙色且可用
                            }
                        }
                    }
                } else {
                    //手机号码为空
                    setRegisterButtonDisabled(); //设置注册按钮灰色且不可用
                }
            }
        });

        //别名输入框注册监听器
        etRegisterUserAlias.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString(); //别名
                if(!TextUtils.isEmpty(content)) { //判断别名是否为空
                    //别名不为空
                    String mobilePhone = etRegisterPhoneNumber.getText().toString(); //手机号码
                    if(!TextUtils.isEmpty(mobilePhone)) { //判断手机号码是否为空
                        //手机号码不为空
                        String verificationCode = etRegisterVerificationCode.getText().toString(); //验证码
                        if(!TextUtils.isEmpty(verificationCode)) { //判断验证码是否为空
                            //验证码不为空
                            String password = etRegisterPassword.getText().toString(); //密码
                            if(!TextUtils.isEmpty(password)) { //判断密码是否为空
                                //密码不为空
                                setRegisterButtonEnabled(); //以及满足以上条件，设置注册按钮橙色且可用
                            }
                        }
                    }
                } else {
                    //用户别名为空
                    setRegisterButtonDisabled(); //设置注册按钮灰色且不可用
                }
            }
        });

        //密码输入框注册监听器
        etRegisterPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString(); //密码
                if(!TextUtils.isEmpty(content)) { //判断密码是否为空
                    //密码不为空
                    String mobilePhone = etRegisterPhoneNumber.getText().toString(); //手机号码
                    if(!TextUtils.isEmpty(mobilePhone)) { //判断手机号码是否为空
                        //手机号码为空
                        String verificationCode = etRegisterVerificationCode.getText().toString(); //验证码
                        if(!TextUtils.isEmpty(verificationCode)) { //判断验证码是否为空
                            //验证码不为空
                            String alias = etRegisterUserAlias.getText().toString(); //别名
                            if(!TextUtils.isEmpty(alias)) { //判断别名是否为空
                                //用户别名不为空
                                setRegisterButtonEnabled(); //以及满足以上条件，设置注册按钮橙色且可用
                            }
                        }
                    }
                } else {
                    //密码为空
                    setRegisterButtonDisabled(); //设置注册按钮灰色且不可用
                }
            }
        });
    }
    
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.register_back: //注册返回
                eFragmentManager.popBackStack(); //LoginActivity的FragmentManager管理器中弹出RegisterFragment
                break;
            case R.id.verification_code: //获取验证码
                confirmMoPoWhetherAlRegister(); //验证手机号码是否已注册
                break;
            case R.id.bt_register: //注册
                authenticateVerificationIsSuccess(); //验证码认证
                break;
            case R.id.bt_already_register: //返回登录页面
                eFragmentManager.popBackStack();
                break;
            case R.id.visible_password:
                int isVisible = etRegisterPassword.getInputType();
                if(isVisible == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) { //当前密码显示
                    etRegisterPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); //设置隐藏
                    etRegisterPassword.setSelection(etRegisterPassword.length()); //设置光标位置
                    visiblePassword.setImageResource(R.mipmap.cipher_text);
                } else { //当前密码隐藏
                    etRegisterPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD); //设置显示
                    etRegisterPassword.setSelection(etRegisterPassword.length());
                    visiblePassword.setImageResource(R.mipmap.plain_text);
                }
                break;

            default:
                break;
        }
    }

    /**
     * 初始化短信
     */
    private void initSMSDK() {
        SMSSDK.initSDK(getActivity(), Constant.APPKEY, Constant.APPSECRET);
        EventHandler eh = new EventHandler(){
            @Override
            public void afterEvent(int event, int result, Object data) {
                Message message;
                if (result == SMSSDK.RESULT_COMPLETE) {
                    //回调完成
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        //提交验证码成功
                        message = Message.obtain();
                        message.what = VERIFICATION_AUTHENTICATE_SUCCESS;
                        handler.sendMessage(message);
                    } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE){
                        //获取验证码成功
                    } else if (event ==SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES){
                        //返回支持发送验证码的国家列表
                    }
                } else {
                    //transitionDialog.dismissDialog();
                    //这里不是UI线程，如果要更新或者操作UI，要调用UI线程
                    String dataStr = data.toString();
                    String str = dataStr.substring(dataStr.indexOf(":")+1).trim();
                    message = Message.obtain();
                    try {
                        JSONObject dataJson = new JSONObject(str);
                        int status = dataJson.getInt("status");
                        if(String.valueOf(status).equals(Constant.INVALID_VERIFICATION_CODE)) { //无效验证码
                            message.what = INVALID_VERIFICATION_CODE;
                            handler.sendMessage(message);
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        SMSSDK.registerEventHandler(eh); //注册短信回调
    }

    /**
     * 验证手机号码是否已注册
     */
    private void confirmMoPoWhetherAlRegister() {
        String mobilePhone = etRegisterPhoneNumber.getText().toString(); //手机号码
        //验证手机号码格式是否正确
        Pattern pattern = Pattern.compile(Constant.MATCH_MOBILE_PHONE); //手机号匹配模式
        Matcher matcher = pattern.matcher(mobilePhone);
        if(!matcher.matches()) {
            common.showToast(context, "请输入正确的手机号码");
            return;
        }
        //判断网络连通性
        if(!isNetworkAvailable(context)){
            //网络不可用
            common.showToast(context, Constant.NOT_INTERNET_CONNECT);
            return;
        }
        String url = Constant.ENTRANCE_PREFIX + "getAccountListByLoginName.json?mobile=" + mobilePhone;
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                //请求异常
                common.showToast(context, Constant.INTERNET_REQUEST_ABNORMAL);
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    jsonArray = jsonObject.getJSONArray("rows");
                    Message message = Message.obtain();
                    if (jsonArray.length() > 0) { //判断该账号是否存在数据
                        //有数据代表账号已注册，不能获取验证码
                        common.showToast(context, "账号已注册");
                        return;
                    } else {
                        //无数据代表账号未注册
                        message.what = MOBILE_PHONE_WITHOUT_REGISTER;
                    }
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 请求验证码
     */
    private void requestVerificationCode() {
        String mobilePhone = etRegisterPhoneNumber.getText().toString(); //手机号码
        //开启获取验证码按钮倒计时
        asyncTask = new VerificationCodeProgressAsyncTask();
        asyncTask.execute(Constant.WAITING_TIME_VERIFICATION);
        //向短信服务运营商请求短信验证码
        SMSSDK.getVerificationCode("86", mobilePhone);
    }

    /**
     *认证验证码
     */
    private void authenticateVerificationIsSuccess() {
        String mobilePhone = etRegisterPhoneNumber.getText().toString(); //手机号码
        String verificationCode = etRegisterVerificationCode.getText().toString(); //验证码
        String password = etRegisterPassword.getText().toString(); //密码
        String txConfirmPassword = confirmPassword.getText().toString(); //确认密码

        //匹配密码是否符合要求
        Pattern pt = Pattern.compile(Constant.MATCH_REGISTER_PASSWORD);
        Matcher matcher = pt.matcher(password);
        if(!matcher.matches()){
            common.showToast(context, "密码必须为6位字母加数字");
            return;
        }

        //密码和确认密码验证
        if(!password.equals(txConfirmPassword)) {
            common.showToast(context, "两次密码输入不一致");
            return;
        }

        //判断网络是否可用
        if(!isNetworkAvailable(context)){
            common.showToast(context, "网络不可用");
            return;
        }
        //校验验证码
        SMSSDK.submitVerificationCode("86", mobilePhone, verificationCode);
    }

    /**
     * 线程通讯
     */
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String sessionUuid;
            switch (msg.what) {
                case MOBILE_PHONE_WITHOUT_REGISTER: //账号未注册，请求获取验证码
                    requestVerificationCode(); //请求验证码
                    break;
                case VERIFICATION_AUTHENTICATE_SUCCESS: //验证码认证成功
                    register();
                    break;
                case USER_REGISTER_SUCCESS: //用户注册成功
                    sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
                    uploadDeviceToken(sessionUuid);
                    break;
                case HANDLER_DEVICE_TOKEN_CODE: //设备ID上传成功返回码
                    isTurnLocationService();
                    break;
                case INVALID_VERIFICATION_CODE: //无效验证码
                    common.showToast(context, "无效验证码");
                    break;
                default:
            }
        }
    };

    /**
     * 注册请求
     */
    private void register() {
        String mobilePhone = etRegisterPhoneNumber.getText().toString(); //手机号码
        String alias = etRegisterUserAlias.getText().toString(); //别名
        String password = etRegisterPassword.getText().toString(); //密码

        String url = Constant.ENTRANCE_PREFIX_v1 + "appPostRegister.json?mobile=" + mobilePhone + "&password=" + password + "&fullName=" + alias + "&appKey=null";
        OkHttpClientManager.getAsyn(url, new RegisterResultCallback<String>(1) {
            @Override
            public void onError(Request request, Exception e) {
                //注册异常
                common.showToast(context, "注册异常");
                transitionDialog.dismissDialog();
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                JSONObject item;

                try {
                    jsonObject = new JSONObject(response);
                    jsonArray = jsonObject.getJSONArray("rows");
                    String status = jsonObject.getString("status");
                    Message message = Message.obtain();
                    if (status.equals(Constant.USER_ALREADY_EXIST)) { //验证用户是否注册
                        //用户已注册
                        common.showToast(context, getString(R.string.account_have_been_register));
                        return;
                    }
                    if (!status.equals(Constant.LOGIN_SUCCESS_STATUS)) { //用户注册异常
                        common.showToast(context, "注册异常");
                        return;
                    }
                    jsonArray = jsonObject.getJSONArray("rows");
                    item = jsonArray.getJSONObject(0);
                    String sessionUuid = item.getString("sessionUuid"); //SessionUuid
                    String mpNumber = etRegisterPhoneNumber.getText().toString(); //用户名（手机号码）
                    String enterpriseId = item.getString("enterpriseId"); //企业ID
                    String enterpriseName = item.getString("enterpriseName"); //企业名称
                    String accountId = item.getString("accountId"); //账户ID
                    String roleId = item.getString("roleId"); //角色ID
                    String tenantId = item.getString("tenantId"); //租户ID
                    String typeCode = item.getString("typeCode"); //0-司机角色

                    Map<Object, Object> map = new HashMap<Object, Object>();
                    map.put(Constant.USER_NAME, mpNumber);
                    map.put(Constant.SESSION_UUID, sessionUuid);
                    map.put(Constant.ENTERPRISE_ID, enterpriseId);
                    map.put(Constant.ENTERPRISE_NAME, enterpriseName);
                    map.put(Constant.TENANT_ID, tenantId);
                    map.put("role_id", roleId);
                    map.put("ACCOUNT_ID", accountId);
                    map.put(Constant.TYPE_CODE, typeCode);
                    if (!common.isSave(map)) { //判断用户信息是否保存成功
                        //用户信息保存失败
                        common.showToast(context, "注册信息保存失败");
                        transitionDialog.dismissDialog();
                        return;
                    }
                    message.what = USER_REGISTER_SUCCESS;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 上传设备ID号
     * @param sessionUuid 用户唯一标示
     */
    private void uploadDeviceToken(String sessionUuid) {
        String deviceToken = UmengRegistrar.getRegistrationId(context);
        String url = Constant.ENTRANCE_PREFIX_v1 + "insertOrUpdatePhoneToken.json?sessionUuid=" + sessionUuid + "&deviceToken=" + deviceToken + "&phoneType=" + Constant.DEVICE_TYPE;
        OkHttpClientManager.getAsyn(url,new RegisterResultCallback<String>(2) {

            @Override
            public void onError(Request request, Exception e) {
                common.showToast(context, "设备ID获取异常");
                transitionDialog.dismissDialog();
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if(!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        common.showToast(context, "设备ID上传失败");
                        transitionDialog.dismissDialog();
                        return;
                    }
                    Message message = Message.obtain();
                    message.what = HANDLER_DEVICE_TOKEN_CODE;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 开启订单跟踪定位服务
     */
    private void isTurnLocationService() {
        String typeCode = common.getStringByKey(Constant.TYPE_CODE);
        //开启订单跟踪定位服务
        if(typeCode.equals("0")) {
            //0-代表司机角色，开启定位服务
            ServiceUtil.invokeTimerPOIService(context);
        }
        initTransportServiceRoleType();
    }

    /**
     * 初始化运输服务角色
     */
    private void initTransportServiceRoleType() {
        Common userCommon = new Common(context.getSharedPreferences(Constant.USER_INFO_FILE, Context.MODE_PRIVATE));
        if (!userCommon.isClearAccount()) {
            Toast.makeText(getActivity(), "清除偏好用户信息失败！", Toast.LENGTH_SHORT);
        }

        String roleType = fixedCommon.getStringByKey(Constant.TRANSPORT_ROLE);
        if(TextUtils.isEmpty(roleType)) {
            Map<Object, Object> map = new HashMap<Object, Object>();
            map.put(Constant.TRANSPORT_ROLE, Constant.DRIVER_ROLE_ID); //默认运输角色，设置为司机，标识0
            //保存账户ID
            if (!fixedCommon.isSave(map)) {
                common.showToast(context, "运输服务角色保存异常");
                return;
            }
        }

        //登录成功之后做的操作
        Intent intent = new Intent();
        String userName = common.getStringByKey(Constant.USER_NAME); //手机号码
        intent.putExtra("username", userName);
        getActivity().setResult(Constant.REGISTER_ACTIVITY_RETURN_CODE, intent);
        getActivity().finish();
    }

    public abstract class RegisterResultCallback<T> extends OkHttpClientManager.ResultCallback<T>{
        private int requestSequence; //请求次序
        private final static int START_ACCESS = 1; //首次访问
        private final static int END_ACCESS = 2; //末次访问

        public RegisterResultCallback(int requestSequence) {
            this. requestSequence = requestSequence;
        }

        @Override
        public void onBefore() {
            //请求之前操作
            if(requestSequence == START_ACCESS) {
                //首次访问
                transitionDialog = new CusProgressDialog(context, "正在注册...");
                transitionDialog.getLoadingDialog().show();
            }
        }

        @Override
        public void onAfter() {
            //请求之后要做的操作
            if(requestSequence == END_ACCESS) {
                //末次访问
                transitionDialog.getLoadingDialog().dismiss();
            }
        }
    }

    /**
     * 检查网络连通性
     * @param context 上下文对象
     * @return
     */
    private boolean isNetworkAvailable(Context context) {
        NetWork netWork = new NetWork(context);
        if(!netWork.isHaveInternet()){
            //无网络访问
            return false;
        }
        return true;
    }

    /**
     * 设置获取验证码按钮可用
     */
    private void setVerificationCodeButtonEnable() {
        btObtainVerificationCode.setEnabled(true);
        btObtainVerificationCode.setBackgroundResource(R.drawable.border_corner_login_enable);
    }

    /**
     * 设置获取验证码按钮不可用
     */
    private void setVerificationCodeButtonDisable() {
        btObtainVerificationCode.setEnabled(false);
        btObtainVerificationCode.setBackgroundResource(R.drawable.border_corner_login);
    }

    /**
     * 设置注册按钮可用
     */
    private void setRegisterButtonEnabled() {
        btRegister.setEnabled(true); //设置按钮可用
        btRegister.setBackgroundResource(R.drawable.border_corner_login_enable);
    }

    /**
     * 设置注册按钮不可用
     */
    private void setRegisterButtonDisabled() {
        btRegister.setBackgroundResource(R.drawable.border_corner_login);
        btRegister.setEnabled(false); //设置按钮不可用
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //SMSSDK.unregisterEventHandler(eh); //取消单个
        SMSSDK.unregisterAllEventHandler(); //取消所有
    }

    /**
     *  AsyncTask定义了三种泛型类型 Params，Progress和Result。
     *  Params 启动任务执行的输入参数，比如HTTP请求的URL。
     *  Progress 后台任务执行的百分比。
     *  Result 后台执行任务最终返回的结果，比如String。
     */
    private class VerificationCodeProgressAsyncTask extends AsyncTask<Integer, Integer, String> {
        /**
         * 该方法运行在UI线程当中,并且运行在UI线程当中 可以对UI空间进行设置
         */
        @Override
        protected void onPreExecute() {
            asyncIsOver = false;
            setVerificationCodeButtonDisable(); //设置获取验证码按钮为灰色不可用
        }

        /**
         * 这里的Integer参数对应AsyncTask中的第一个参数
         * 这里的String返回值对应AsyncTask的第三个参数
         * 该方法并不运行在UI线程当中，主要用于异步操作，所有在该方法中不能对UI当中的空间进行设置和修改
         * 但是可以调用publishProgress方法触发onProgressUpdate对UI进行操作
         */
        @Override
        protected String doInBackground(Integer... params) {
            if(isCancelled()) { //判断线程是否取消
                //已取消
                return Constant.THREAD_CANCEL; //返回CANCEL标志
            }
            int waitingTime = params[0].intValue(); //异步等待时间
            try {
                for(int i = waitingTime; i >= 0 ; i--) {
                    publishProgress(i);
                    Thread.sleep(Constant.THREAD_WAITING_TIME); //线程等待
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Constant.RETURN_SUCCESS; //线程执行完毕
        }

        /**
         *执行结束调用
         * 这里的String参数对应AsyncTask中的第三个参数（也就是接收doInBackground的返回值）
         * 在doInBackground方法执行结束之后在运行，并且运行在UI线程当中 可以对UI空间进行设置
         * @param result
         */
        @Override
        protected void onPostExecute(String result) {
            asyncIsOver = true;
            setVerificationCodeButtonEnable(); //设置获取验证码按钮为橙色可用
        }

        /**
         * 取消一个正在执行的任务,onCancelled方法将会被调用
         */
        @Override
        protected void onCancelled() {
            super.onCancelled();
            setVerificationCodeButtonEnable(); //设置获取验证码按钮为橙色可用
        }

        /**
         * 这里的Integer参数对应AsyncTask中的第二个参数
         * 在doInBackground方法当中，，每次调用publishProgress方法都会触发onProgressUpdate执行
         * onProgressUpdate是在UI线程中执行，所有可以对UI空间进行操作
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
            int cusTime = values[0].intValue();
            if(cusTime == 0) {
                btObtainVerificationCode.setText("重新发送");
            } else {
                btObtainVerificationCode.setText("重新发送("+String.valueOf(cusTime)+")");
            }

        }
    }
}
