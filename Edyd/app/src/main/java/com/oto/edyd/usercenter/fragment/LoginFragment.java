package com.oto.edyd.usercenter.fragment;

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
import com.oto.edyd.usercenter.activity.ForgetPasswordActivity;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.NetWork;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

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
    private CusProgressDialog transitionDialog; //页面切换过度
    private Context context; //上下文对象
    private Common common; //share对象
    private Common userInfoCommon;
    private Common fixedCommon;
    private static final int HANDLER_LOGIN_SUCCESS_STATUS_CODE = 0x10; //登录成功返回码
    private static final int HANDLER_ACCOUNT_TYPE_SUCCESS_STATUS_CODE = 0x11; //账户类型请求成功返回码
    private static final int HANDLER_ROLE_TYPE_SUCCESS_CODE = 0x12; //角色类型请求成功返回码
    private static final int HANDLER_ACCOUNT_TYPE_SUCCESS_CODE = 0x13; //用户账户ID请求成功返回码

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
                intent = new Intent(getActivity(), ForgetPasswordActivity.class);
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
        String url = Constant.ENTRANCE_PREFIX + "login.json?mobile=" + userName + "&password=" + password + "&appKey=null"; //登录访问地址

        //判断网络是否有网络
        NetWork netWork = new NetWork(context);
        if (!netWork.isHaveInternet()) {
            //无网络访问
            common.showToast(context, Constant.NOT_INTERNET_CONNECT);
            return;
        }

        //请求数据
        OkHttpClientManager.getAsyn(url, new LoginResultCallback<String>(1) {
            @Override
            public void onError(Request request, Exception e) {
                //网络异常
                common.showToast(context, Constant.INTERNET_REQUEST_ABNORMAL);
                transitionDialog.getLoadingDialog().dismiss();
            }

            @Override
            public void onResponse(String response) {
                //请求成功
                JSONObject jsonObject;
                JSONArray jsonArray;
                String sessionUuid;
                String userName; //手机号码

                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status"); //获取返回状态
                    if (!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        //用户名和密码错误
                        common.showToast(context, Constant.INVALID_USERNAME_PASSWORD);
                        transitionDialog.getLoadingDialog().dismiss();
                        return;
                    }
                    jsonArray = jsonObject.getJSONArray("rows");
                    sessionUuid = jsonArray.getJSONObject(0).getString("sessionUuid");
                    userName = etUserName.getText().toString();
                    Map<Object, Object> map = new HashMap<Object, Object>();
                    map.put(Constant.USER_NAME, userName);
                    map.put(Constant.SESSION_UUID, sessionUuid);
                    if (!common.isSave(map)) {
                        //用户信息保存失败
                        common.showToast(context, Constant.USER_INFO_SAVE_FAIL);
                        transitionDialog.getLoadingDialog().dismiss();
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
     * 请求账户类型信息
     * @param sessionUuid 用户唯一标示
     */
    private void requestAccountTypeInfo(String sessionUuid) {
        String url = Constant.ENTRANCE_PREFIX + "SubmitEnter.json?sessionUuid=" + sessionUuid + "&enterprisesId=" + Constant.ENTERPRISE_TYPE_PERSONAL;
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                //请求异常
                common.showToast(context, Constant.INTERNET_REQUEST_ABNORMAL);
                transitionDialog.getLoadingDialog().dismiss();
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if (!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        //账户类型请求失败
                        common.showToast(context, Constant.ACCOUNT_TYPE_INFO_REQUEST_FAIL);
                        transitionDialog.getLoadingDialog().dismiss();
                        return;
                    }

                    jsonArray = jsonObject.getJSONArray("rows");
                    JSONObject jsonRows = jsonArray.getJSONObject(0);
                    int enterpriseId = jsonRows.getInt("enterpriseId");
                    int tenantId = jsonRows.getInt("tenantId");
                    String enterpriseName = jsonRows.getString("enterpriseName");
                    Map<Object, Object> map = new HashMap<Object, Object>();
                    map.put(Constant.ENTERPRISE_ID, enterpriseId);
                    map.put(Constant.ENTERPRISE_NAME, enterpriseName);
                    map.put(Constant.TENANT_ID, tenantId);
                    //保存账户类型信息
                    if (!common.isSave(map)) {
                        common.showToast(context, Constant.ACCOUNT_TYPE_INFO_SAVE_FAIL);
                        transitionDialog.getLoadingDialog().dismiss();
                        return;
                    }

                    Message message = Message.obtain();
                    message.what = HANDLER_ACCOUNT_TYPE_SUCCESS_STATUS_CODE;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 请求角色类型
     * @param sessionUuid 用户唯一标示
     * @param enterpriseId 企业ID
     */
    private void requestRoleType(String sessionUuid, String enterpriseId) {
        String url = Constant.ENTRANCE_PREFIX + "Compare.json?sessionUuid=" + sessionUuid + "&enterpriseId=" + enterpriseId;

        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                //请求异常
                common.showToast(context, Constant.INTERNET_REQUEST_ABNORMAL);
                transitionDialog.getLoadingDialog().dismiss();
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if (!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        //角色类型请求异常
                        common.showToast(context, "角色类型请求异常");
                        transitionDialog.getLoadingDialog().dismiss();
                        return;
                    }

                    jsonArray = jsonObject.getJSONArray("rows");
                    JSONObject rowsJson = jsonArray.getJSONObject(0);
                    int roleID = rowsJson.getInt("role");
                    Map<Object, Object> map = new HashMap<Object, Object>();
                    map.put("role_id", roleID);
                    //保存角色类型信息
                    if (!common.isSave(map)) {
                        //角色类型保存失败
                        common.showToast(context, getString(R.string.role_type_info_save_error));
                        transitionDialog.getLoadingDialog().dismiss();
                        return;
                    }

                    Message message = Message.obtain();
                    message.what = HANDLER_ROLE_TYPE_SUCCESS_CODE;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 请求账户ID
     * @param sessionUuid
     */
    private void requestAccountId(String sessionUuid) {
        String accountIDUrl = Constant.ENTRANCE_PREFIX + "getAccountIdBySessionUuid.json?sessionUuid=" + sessionUuid;

        OkHttpClientManager.getAsyn(accountIDUrl, new LoginResultCallback<String>(2) {
            @Override
            public void onError(Request request, Exception e) {
                //请求异常
                common.showToast(context, Constant.INTERNET_REQUEST_ABNORMAL);
                transitionDialog.getLoadingDialog().dismiss();
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    jsonArray = jsonObject.getJSONArray("rows");
                    int accountID = jsonArray.getInt(0);
                    Map<Object, Object> map = new HashMap<Object, Object>();
                    map.put("ACCOUNT_ID", accountID);
                    //保存账户ID
                    if (!common.isSave(map)) {
                        common.showToast(context, getString(R.string.role_type_info_save_error));
                        transitionDialog.getLoadingDialog().dismiss();
                        return;
                    }

                    Message message = Message.obtain();
                    message.what = HANDLER_ACCOUNT_TYPE_SUCCESS_CODE;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
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
            String enterpriseId; //企业ID
            switch (msg.what) {
                case HANDLER_LOGIN_SUCCESS_STATUS_CODE: //登录请求返回成功
                    sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
                    requestAccountTypeInfo(sessionUuid);
                    break;
                case HANDLER_ACCOUNT_TYPE_SUCCESS_STATUS_CODE: //账户类型请求返回成功
                    sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
                    enterpriseId = common.getStringByKey(Constant.ENTERPRISE_ID);
                    requestRoleType(sessionUuid, enterpriseId);
                    break;
                case HANDLER_ROLE_TYPE_SUCCESS_CODE: //角色类型请求返回成功
                    sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
                    requestAccountId(sessionUuid);
                    break;
                case HANDLER_ACCOUNT_TYPE_SUCCESS_CODE: //账户ID请求返回成功
                    initTransportServiceRoleType();
                    break;
            }
        }
    };

    /**
     * 重写ResultCallback接口，操作访问网络前和网络后需要实现的动作
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
                transitionDialog = new CusProgressDialog(getActivity(), "正在登录...");
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
}
