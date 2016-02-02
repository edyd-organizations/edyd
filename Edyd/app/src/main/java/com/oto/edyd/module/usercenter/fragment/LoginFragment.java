package com.oto.edyd.module.usercenter.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.TextView;

import com.oto.edyd.R;
import com.oto.edyd.module.usercenter.activity.ForgetPasswordActivity;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.OkHttpClientManager;
import com.oto.edyd.utils.ServiceUtil;
import com.squareup.okhttp.Request;
import com.umeng.message.UmengRegistrar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 功能：登录模块Fragment
 * 文件名：com.oto.edyd.usercenter.fragment.LoginFragment.java
 * 创建时间：2015/8/27
 * 作者：yql
 */
public class LoginFragment extends Fragment implements View.OnClickListener {
    
    private FragmentManager eFragmentManager; //Fragment管理器
    private LinearLayout back; //返回
    private TextView tvRegister; //登录页面注册按钮
    private EditText etUserName; //用户名
    private EditText etPassword; //密码
    private Button btLogin; //登录
    private TextView tvForgetPassword; //忘记密码
    private LinearLayout linearLayoutRemember;//点击记住密码
    private ImageView ivRememberPassword; //记住密码显示图片
    private ImageView visiblePassword; //密码是否可见
    private CusProgressDialog transitionDialog; //页面切换过度
    private Context context; //上下文对象
    private Common common; //share对象
    private Common userInfoCommon;
    private Common fixedCommon;
    private static final int HANDLER_LOGIN_SUCCESS_STATUS_CODE = 0x10; //登录成功返回码
    private static final int HANDLER_DEVICE_TOKEN_CODE = 0x11; //用户友盟设备ID请求成功返回码

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login, null); //加载布局文件
        init(view); //初始化数据
        return view;
    }

    /**
     * 初始化数据
     */
    private void init(View view) {
        initFields(view); //初始化字段
        initListener(); //初始化监听事件
        initWhetherRememberPassword(); //初始化记住密码状态
    }

    /**
     * 初始化字段
     * @param view LoginFragment布局View
     */
    private void initFields(View view) {
        back = (LinearLayout) view.findViewById(R.id.login_back);
        tvRegister = (TextView) view.findViewById(R.id.login_user_register);
        etUserName = (EditText) view.findViewById(R.id.login_username);
        etPassword = (EditText) view.findViewById(R.id.login_password);
        btLogin = (Button) view.findViewById(R.id.bt_login);
        tvForgetPassword = (TextView) view.findViewById(R.id.forget_password);
        ivRememberPassword = (ImageView) view.findViewById(R.id.remember_password);
        visiblePassword = (ImageView) view.findViewById(R.id.visible_password);
        linearLayoutRemember = (LinearLayout) view.findViewById(R.id.ll_remember);
        context = getActivity();
        common = new Common(context.getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        fixedCommon = new Common(context.getSharedPreferences(Constant.FIXED_FILE, Context.MODE_PRIVATE));
    }

    /**
     * 初始化监听事件
     */
    private void initListener() {
        back.setOnClickListener(this);
        btLogin.setOnClickListener(this);
        linearLayoutRemember.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
        tvForgetPassword.setOnClickListener(this);
        visiblePassword.setOnClickListener(this);
        etUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString();
                if(!TextUtils.isEmpty(content)) {
                    //用户名不为空
                    String password = etPassword.getText().toString();
                    if(!TextUtils.isEmpty(password)) {
                        //密码不为空，设置按钮可用,颜色为橙色
                        btLogin.setEnabled(true); //设置按钮可用
                        btLogin.setBackgroundResource(R.drawable.border_corner_login_enable); //设置按钮背景色
                    }
                } else {
                    //用户名为空，设置按钮不可用，颜色为灰色
                    btLogin.setBackgroundResource(R.drawable.border_corner_login);
                    btLogin.setEnabled(false); //设置按钮不可用
                }
            }
        });
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {

                String content = s.toString();
                if(!TextUtils.isEmpty(content)) {
                    //密码不为空
                    String userName = etUserName.getText().toString();
                    if(!TextUtils.isEmpty(userName)) {
                        //用户名不为空，设置按钮可用,颜色为橙色
                        btLogin.setEnabled(true); //设置按钮可用
                        btLogin.setBackgroundResource(R.drawable.border_corner_login_enable); //设置按钮背景色
                    }
                } else {
                    //密码为空，设置按钮不可用，颜色为灰色
                    btLogin.setBackgroundResource(R.drawable.border_corner_login);
                    btLogin.setEnabled(false); //设置按钮不可用
                }
            }
        });
    }

    /**
     * 初始记住密码状态
     */
    private void initWhetherRememberPassword() {
        userInfoCommon = new Common(context.getSharedPreferences(Constant.USER_INFO_FILE, Context.MODE_PRIVATE));
        String rememberUserName = userInfoCommon.getStringByKey(Constant.USER_NAME);
        if(!TextUtils.isEmpty(rememberUserName)) {
            //用户名不为空，初始化为选中状态
            String rememberPassword = userInfoCommon.getStringByKey(Constant.PASSWORD);
            etUserName.setText(rememberUserName);
            etPassword.setText(rememberPassword);
            ivRememberPassword.setImageResource(R.mipmap.ic_agree_protocol);
            ivRememberPassword.setTag(R.id.remember_password, true);
        } else {
            //用户名为空，初始化不选中状态
            ivRememberPassword.setImageResource(R.mipmap.ic_not_agree_protocol);
            ivRememberPassword.setTag(R.id.remember_password, false);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.login_back:
                getActivity().finish(); //结束LoginActivity
                break;
            case R.id.login_user_register: //登录用户注册
                eFragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction loginTransaction = eFragmentManager.beginTransaction();
                loginTransaction.addToBackStack(null);
                loginTransaction.replace(R.id.common_frame, new RegisterFragment());
                loginTransaction.commit();
                break;
            case R.id.bt_login: //用户登入
                login();
                break;
            case R.id.forget_password: //忘记密码
                intent = new Intent(context, ForgetPasswordActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_remember: //切换记住密码图标
                boolean tag = (boolean) ivRememberPassword.getTag(R.id.remember_password);
                if (tag) {
                    ivRememberPassword.setImageResource(R.mipmap.ic_not_agree_protocol);
                    ivRememberPassword.setTag(R.id.remember_password, false);
                } else {
                    ivRememberPassword.setImageResource(R.mipmap.ic_agree_protocol);
                    ivRememberPassword.setTag(R.id.remember_password, true);
                }
                break;
            case R.id.visible_password:
                int isVisible = etPassword.getInputType();
                if(isVisible == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) { //当前密码显示
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); //设置隐藏
                    etPassword.setSelection(etPassword.length()); //设置光标位置
                    visiblePassword.setImageResource(R.mipmap.cipher_text);
                } else { //当前密码隐藏
                    etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD); //设置显示
                    etPassword.setSelection(etPassword.length());
                    visiblePassword.setImageResource(R.mipmap.plain_text);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 用户登录
     */
    private void login() {
        String userName = etUserName.getText().toString();
        String password = etPassword.getText().toString();
        String url = Constant.ENTRANCE_PREFIX_v1 + "appPostLogin.json?mobile=" + userName + "&password=" + password + "&appKey=null"; //登录访问地址

        //请求数据
        OkHttpClientManager.getAsyn(url, new LoginResultCallback<String>(1) {
            @Override
            public void onError(Request request, Exception e) {
                //网络异常
                common.showToast(context, "登录异常");
                transitionDialog.dismissDialog();
            }

            @Override
            public void onResponse(String response) {
                //请求成功
                JSONObject jsonObject;
                JSONArray jsonArray;
                JSONObject item;

                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status"); //获取返回状态
                    if (!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        //用户名和密码错误
                        common.showToast(context, "用户名和密码错误");
                        transitionDialog.dismissDialog();
                        return;
                    }
                    jsonArray = jsonObject.getJSONArray("rows");
                    item = jsonArray.getJSONObject(0);
                    String sessionUuid = item.getString("sessionUuid"); //SessionUuid
                    String mpNumber = etUserName.getText().toString(); //用户名（手机号码）
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
                    if (!common.isSave(map)) {
                        //用户信息保存失败
                        common.showToast(context, Constant.USER_INFO_SAVE_FAIL);
                        return;
                    }
                    Message message = Message.obtain();
                    message.what = HANDLER_LOGIN_SUCCESS_STATUS_CODE;
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
        OkHttpClientManager.getAsyn(url,new LoginResultCallback<String>(2) {

            @Override
            public void onError(Request request, Exception e) {
                common.showToast(context, "设备ID获取异常");
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if(!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        common.showToast(context, "设备ID上传失败");
                        transitionDialog.dismissDialog();
                        common.isClearAccount();
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
        isNeedRememberPassword(); //是否需要记住密码
        //登录成功之后做的操作
        Intent intent = new Intent();
        String userName = common.getStringByKey(Constant.USER_NAME); //手机号码
        intent.putExtra("username", userName);
        getActivity().setResult(Constant.LOGIN_ACTIVITY_RETURN_CODE, intent);
        getActivity().finish();
    }

    /**
     * 是否需要记住密码
     */
    private void isNeedRememberPassword() {
        boolean isChecked = (boolean) ivRememberPassword.getTag(R.id.remember_password);
        String userName = etUserName.getText().toString();
        String password = etPassword.getText().toString();
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put(Constant.USER_NAME, userName);
        map.put(Constant.PASSWORD, password);
        if (!common.isSave(map)) {
            common.showToast(context, "保存偏好用户信息失败");
        }
        if (isChecked) {
            if (!userInfoCommon.isSave(map)) {
                common.showToast(context, "保存偏好用户信息失败");
            }
        } else {
            if (!userInfoCommon.isClearAccount()) {
                common.showToast(context, "清除偏好用户信息失败");
            }
        }
    }

    /**
     * 线程通讯
     */
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String sessionUuid;
            sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
            switch (msg.what) {
                case HANDLER_LOGIN_SUCCESS_STATUS_CODE: //登录请求返回成功
                    uploadDeviceToken(sessionUuid); //上传设备ID
                    break;
                case HANDLER_DEVICE_TOKEN_CODE: //设备ID上传成功返回码
                    isTurnLocationService();
                    break;
            }
        }
    };

    /**
     * 重写ResultCallback接口，实现访问网络前和后方法
     * @param <T>
     */
    abstract class LoginResultCallback<T> extends OkHttpClientManager.ResultCallback<T> {
        private int requestSequence; //请求次序
        private final static int START_ACCESS = 1; //首次访问
        private final static int END_ACCESS = 2; //末次访问

        /**
         * @param requestSequence 网络请求次序
         */
        public LoginResultCallback(int requestSequence) {
            this. requestSequence = requestSequence;
        }

        @Override
        public void onBefore() {
            //请求之前操作
            if(requestSequence == START_ACCESS) {
                //首次访问
                transitionDialog = new CusProgressDialog(context, "正在登录...");
                transitionDialog.showDialog();
            }
        }

        @Override
        public void onAfter() {
            //请求之后要做的操作
            if(requestSequence == END_ACCESS) {
                //末次访问
                transitionDialog.dismissDialog();
            }
        }
    }
}
