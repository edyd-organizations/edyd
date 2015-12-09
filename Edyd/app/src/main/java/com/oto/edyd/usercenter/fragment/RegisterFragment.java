package com.oto.edyd.usercenter.fragment;

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
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.oto.edyd.R;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.NetWork;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

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
public class RegisterFragment extends Fragment implements View.OnClickListener, View.OnFocusChangeListener{

    private FragmentManager eFragmentManager; //LoginActivity布局管理器

    private LinearLayout back; //注册返回
    private EditText etRegisterPhoneNumber; //注册手机号
    private EditText etRegisterVerificationCode; //验证码
    private Button btObtainVerificationCode; //获取验证码
    private EditText etRegisterUserAlias; //注册用户别名
    private EditText etRegisterPassword; //注册密码
    //private ImageView ivAgreeProtocol; //协议复选框
    //private TextView tvProtocolIntroduction; //协议描述
    private Button btRegister; //注册按钮
    private Button btAlreadyRegister; //已经注册
    private VerificationProgressAsyncTask verificationProgressAsyncTask; //异步消息对象
    private boolean asyncIsOver = false; //异步线程是否结束
    private CusProgressDialog transitionDialog; //过度对话框
    private Common common;
    private Common fixedCommon;
    private static final int MOBILE_PHONE_LENGTH  = 11; //手机号码长度

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
        //ivAgreeProtocol = (ImageView) view.findViewById(R.id.cb_agree_protocol);
        //tvProtocolIntroduction = (TextView) view.findViewById(R.id.protocol_introduction);
        btRegister = (Button) view.findViewById(R.id.bt_register);
        btAlreadyRegister = (Button) view.findViewById(R.id.bt_already_register);
        eFragmentManager = getActivity().getSupportFragmentManager();
        common = new Common(getActivity().getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        fixedCommon = new Common(getActivity().getSharedPreferences(Constant.FIXED_FILE, Context.MODE_PRIVATE));
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        back.setOnClickListener(this);
        btObtainVerificationCode.setOnClickListener(this);
        btRegister.setOnClickListener(this);
        btAlreadyRegister.setOnClickListener(this);
        etRegisterVerificationCode.setOnFocusChangeListener(this);
        etRegisterPassword.setOnFocusChangeListener(this);

        //手机号码监听
        etRegisterPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString();
                if(!TextUtils.isEmpty(content)) {
                    //手机号码不为空
                    String verificationCode = etRegisterVerificationCode.getText().toString();
                    if(!TextUtils.isEmpty(verificationCode)) {
                        //验证码不为空
                        String alias = etRegisterUserAlias.getText().toString();
                        if(!TextUtils.isEmpty(alias)) {
                            //别名不为空
                            String password = etRegisterPassword.getText().toString();
                            if(!TextUtils.isEmpty(password)) {
                                int length = s.length();
                                if(length == MOBILE_PHONE_LENGTH) {
                                    //密码不为空，以及满足以上条件，设置获取验证码橙色且可用
                                    setVerificationButtonEnable();
                                } else {
                                    setVerificationCodeDisable();
                                }
                            }
                        }
                    }
                } else {
                    //手机号码为空，设置获取验证码按钮灰色且不可用
                    setVerificationCodeDisable();
                }
            }
        });

        //验证码输入监听
        etRegisterVerificationCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString();
                if(!TextUtils.isEmpty(content)) {
                    //验证码不为空
                    String mobilePhone = etRegisterPhoneNumber.getText().toString();
                    if(!TextUtils.isEmpty(mobilePhone)) {
                        //手机号码不为空
                        String alias = etRegisterUserAlias.getText().toString();
                        if(!TextUtils.isEmpty(alias)) {
                            //别名不为空
                            String password = etRegisterPassword.getText().toString();
                            if(!TextUtils.isEmpty(password)) {
                                //密码不为空，以及满足以上条件，设置注册按钮橙色且可用
                                setRegisterButtonEnabled();
                            }
                        }
                    }
                } else {
                    //手机号码为空，设置注册按钮灰色且不可用
                    setRegisterButtonDisabled();
                }


//                if (s != null) {
//                    String inputContent = s.toString();
//                    if (inputContent != null && !(inputContent.equals(""))) {
//                        String phoneNumber = etRegisterPhoneNumber.getText().toString();
//                        String alias = etRegisterUserAlias.getText().toString();
//                        String password = etRegisterPassword.getText().toString();
//
//                        if (phoneNumber != null && !(phoneNumber.equals(""))) {
//                            if (alias != null && !(alias.equals(""))) {
//                                if (password != null && !(password.equals(""))) {
//                                    setEnabled();
//                                }
//                            }
//                        }
//                    } else {
//                        setDisable();
//                    }
//                }
            }
        });

        //用户别名
        etRegisterUserAlias.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString();
                if(!TextUtils.isEmpty(content)) {
                    //用户别名不为空
                    String mobilePhone = etRegisterPhoneNumber.getText().toString();
                    if(!TextUtils.isEmpty(mobilePhone)) {
                        //手机号码不为空
                        String verificationCode = etRegisterVerificationCode.getText().toString();
                        if(!TextUtils.isEmpty(verificationCode)) {
                            //验证码不为空
                            String password = etRegisterPassword.getText().toString();
                            if(!TextUtils.isEmpty(password)) {
                                //密码不为空，以及满足以上条件，设置注册按钮橙色且可用
                                setRegisterButtonEnabled();
                            }
                        }
                    }
                } else {
                    //用户别名为空，设置注册按钮灰色且不可用
                    setRegisterButtonDisabled();
                }


//                if (!(s == null)) {
//                    String inputContent = s.toString();
//                    if (inputContent != null && !(inputContent.equals(""))) {
//                        String phoneNumber = etRegisterPhoneNumber.getText().toString();
//                        String code = etRegisterVerificationCode.getText().toString();
//                        String password = etRegisterPassword.getText().toString();
//
//                        if (phoneNumber != null && !(phoneNumber.equals(""))) {
//                            if (code != null && !(code.equals(""))) {
//                                if (password != null && !(password.equals(""))) {
//                                    setEnabled();
//                                }
//                            }
//                        }
//                    } else {
//                        setDisable();
//                    }
//                }
            }
        });

        //密码框改变监听器
        etRegisterPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString();
                if(!TextUtils.isEmpty(content)) {
                    //密码不为空
                    String mobilePhone = etRegisterPhoneNumber.getText().toString();
                    if(!TextUtils.isEmpty(mobilePhone)) {
                        //手机号码为空
                        String verificationCode = etRegisterVerificationCode.getText().toString();
                        if(!TextUtils.isEmpty(verificationCode)) {
                            //验证码不为空
                            String alias = etRegisterUserAlias.getText().toString();
                            if(!TextUtils.isEmpty(alias)) {
                                //用户别名不为空，以及满足以上条件，设置注册按钮橙色且可用
                                setRegisterButtonEnabled();
                            }
                        }
                    }
                } else {
                    //密码为空，设置注册按钮灰色且不可用
                    setRegisterButtonDisabled();
                }

//                if (!(s == null)) {
//                    String inputContent = s.toString();
//                    if (inputContent != null && !(inputContent.equals(""))) {
//                        String phoneNumber = etRegisterPhoneNumber.getText().toString();
//                        String code = etRegisterVerificationCode.getText().toString();
//                        String alias = etRegisterUserAlias.getText().toString();
//
//                        if (phoneNumber != null && !(phoneNumber.equals(""))) {
//                            if (code != null && !(code.equals(""))) {
//                                if (alias != null && !(alias.equals(""))) {
//                                    setEnabled();
//                                }
//                            }
//                        }
//                    } else {
//                        setDisable();
//                    }
//                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.register_back: //注册返回
                eFragmentManager.popBackStack(); //activity的后退栈中弹出fragment
                break;
            case R.id.verification_code: //获取验证码
                String userName = etRegisterPhoneNumber.getText().toString();
                if(!isNetworkAvailable(getActivity())){ //判断网络是否可用
                    Toast.makeText(getActivity().getApplicationContext(), Constant.NOT_INTERNET_CONNECT, Toast.LENGTH_SHORT).show();
                    return;
                }
                isUserExist(userName);
                break;
            case R.id.bt_register:
                //注册
                transitionDialog = new CusProgressDialog(getActivity(), "正在注册中...");
                transitionDialog.getLoadingDialog().show();
                register();
                break;
            case R.id.bt_already_register:
                eFragmentManager.popBackStack(); //activity的后退栈中弹出fragment
                break;
            default:
                break;
        }
    }

    /**
     * 捕获线程消息
     */
    Handler registerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 504: //账号已注册
                    toastMessage(msg.what);
                    break;
                case 520: //无效验证码
                    toastMessage(msg.what);
                    break;
                case 700:
                    toastMessage(msg.what);
                    break;
                case 701:
                    toastMessage(msg.what);
                    break;
                case 702:
                    toastMessage(msg.what);
                    break;
                case 703:
                    String mobile = etRegisterPhoneNumber.getText().toString();
                    verificationProgressAsyncTask = new VerificationProgressAsyncTask(btObtainVerificationCode);
                    verificationProgressAsyncTask.execute(Constant.WAITING_TIME_VERIFICATION);
                    SMSSDK.getVerificationCode("86", mobile); //向短信服务运营商请求短信验证码
                    break;
                default:
            }
        }
    };

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.register_verification_code: //验证码位数验证
                if(!hasFocus) {
                    String editable = ((EditText)v).getText().toString();
                    if(editable !=null && !(editable.equals(""))) {
                        if(editable.length() != Constant.VERIFICATION_LENGTH) {
                            Toast.makeText(getActivity(), "验证码位数必须为4位", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
            case R.id.register_user_password: //密码位数验证
                if(!hasFocus) {
                    String editable = ((EditText)v).getText().toString();
                    if(editable !=null && !(editable.equals(""))) {
                        if(editable.length() < Constant.PASSWORD_LENGTH) {
                            Toast.makeText(getActivity(), "密码位数不能低于6位", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Pattern pt = Pattern.compile("^(?!\\D+$)(?![^a-zA-Z]+$)\\S{6,20}$");
                        Matcher matcher = pt.matcher(editable.toString());
                        if(!matcher.matches()){
                            Toast.makeText(getActivity(), "密码必须字母加数字", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
        }
    }

    public abstract class RegisterResultCallback<T> extends OkHttpClientManager.ResultCallback<T>{
        @Override
        public void onBefore() {
            super.onBefore();
        }

        @Override
        public void onAfter() {
            super.onAfter();
        }
    }

    public abstract class RegisterSubCompanyResultCallback<T> extends OkHttpClientManager.ResultCallback<T>{
        @Override
        public void onBefore() {
            super.onBefore();
        }

        @Override
        public void onAfter() {
            super.onAfter();
        }
    }

    /**
     *  AsyncTask定义了三种泛型类型 Params，Progress和Result。
     *  Params 启动任务执行的输入参数，比如HTTP请求的URL。
     *  Progress 后台任务执行的百分比。
     *  Result 后台执行任务最终返回的结果，比如String。
     */
    private class VerificationProgressAsyncTask extends AsyncTask<Integer, Integer, String> {

        private Button btVerificationCode; //获取验证码

        public VerificationProgressAsyncTask(Button verificationCode) {
            super();
            this.btVerificationCode = verificationCode;
        }

        /**
         * 该方法运行在UI线程当中,并且运行在UI线程当中 可以对UI空间进行设置
         */
        @Override
        protected void onPreExecute() {
            btVerificationCode.setEnabled(false); //不可点击
            //btVerificationCode.setTextSize(12); //设置字体大小
            btVerificationCode.setBackgroundResource(R.color.dim_foreground_dark); //获取验证码后变灰色
        }

        /**
         * 这里的Integer参数对应AsyncTask中的第一个参数
         * 这里的String返回值对应AsyncTask的第三个参数
         * 该方法并不运行在UI线程当中，主要用于异步操作，所有在该方法中不能对UI当中的空间进行设置和修改
         * 但是可以调用publishProgress方法触发onProgressUpdate对UI进行操作
         */
        @Override
        protected String doInBackground(Integer... params) {

            if(isCancelled()) {
                return Constant.THREAD_CANCEL; //线程取消
            }
            ThreadOperator netOperator = new ThreadOperator();
            int waitingTime = params[0].intValue();
            for(int i = waitingTime; i >= 0 ; i--) {
                publishProgress(i);
                netOperator.sleep();
            }
            return Constant.RETURN_SUCCESS; //线程执行完毕
        }

        /**
         *
         * 这里的String参数对应AsyncTask中的第三个参数（也就是接收doInBackground的返回值）
         * 在doInBackground方法执行结束之后在运行，并且运行在UI线程当中 可以对UI空间进行设置
         * @param result
         */
        @Override
        protected void onPostExecute(String result) {
            asyncIsOver = true;
            setStatus(btVerificationCode); //设置状态
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            setStatus(btVerificationCode); //设置状态
        }

        /**
         * 这里的Integer参数对应AsyncTask中的第二个参数
         * 在doInBackground方法当中，，每次调用publishProgress方法都会触发onProgressUpdate执行
         * onProgressUpdate是在UI线程中执行，所有可以对UI空间进行操作
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
            int cusTime = values[0].intValue();
            btVerificationCode.setText("重新发送("+String.valueOf(cusTime)+")");
        }
    }

    /**
     * 线程等待
     */
    public class ThreadOperator {
        public void sleep() {
            try {
                Thread.sleep(Constant.THREAD_WAITING_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void setStatus(Button btVerificationCode) {
        btVerificationCode.setEnabled(true); //可点击
        btVerificationCode.setTextSize(10);
        btVerificationCode.setText(R.string.confirm_code_text);
        btVerificationCode.setBackgroundResource(R.color.user_icon_8);
    }

    /**
     * 初始化短信
     */
    private void initSMSDK() {
        SMSSDK.initSDK(getActivity(), Constant.APPKEY, Constant.APPSECRET);
        EventHandler eh = new EventHandler(){
            @Override
            public void afterEvent(int event, int result, Object data) {
                if (result == SMSSDK.RESULT_COMPLETE) {
                    //回调完成
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        //提交验证码成功
                        passVerify();
                    }else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE){
                        //获取验证码成功
//                        if(!asyncIsOver) { //判断异步线程是否结束
//                            verificationProgressAsyncTask.cancel(true); //停止倒计时，并设置获取验证码按钮可用
//                        }
                    }else if (event ==SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES){
                        //返回支持发送验证码的国家列表
                    }
                }else{
                    transitionDialog.dismissDialog();
                    //这里不是UI线程，如果要更新或者操作UI，要调用UI线程
                    String dataStr = data.toString();
                    String str = dataStr.substring(dataStr.indexOf(":")+1).trim();
                    Message message;
                    try {
                        JSONObject dataJson = new JSONObject(str);
                        int status = dataJson.getInt("status");
                        if(String.valueOf(status).equals(Constant.INVALID_VERIFICATION_CODE)) { //无效验证码
                            message = new Message();
                            message.what = Integer.valueOf(Constant.INVALID_VERIFICATION_CODE);
                            registerHandler.sendMessage(message);
                            return;
                        }

                    } catch (JSONException e) {
                        message = new Message();
                        message.what = Constant.NETWORK_EXCEPTION;
                        registerHandler.sendMessage(message);
                        e.printStackTrace();
                    }

                }
            }
        };
        SMSSDK.registerEventHandler(eh); //注册短信回调
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //SMSSDK.unregisterEventHandler(eh); //取消单个
        SMSSDK.unregisterAllEventHandler(); //取消所有
    }

    /**
     * 注册验证
     */
    private void register() {
        String userName = etRegisterPhoneNumber.getText().toString(); //用户名
        String code = etRegisterVerificationCode.getText().toString(); //验证码
        String alias = etRegisterUserAlias.getText().toString(); //别名
        String password = etRegisterPassword.getText().toString(); //密码

        if(TextUtils.isEmpty(userName)) {
            Toast.makeText(getActivity(),"手机号码不能为空", Toast.LENGTH_SHORT).show();
            transitionDialog.dismissDialog();
            return;
        }
        if(TextUtils.isEmpty(code)) {
            Toast.makeText(getActivity(),"验证码不能为空", Toast.LENGTH_SHORT).show();
            transitionDialog.dismissDialog();
            return;
        }
        if(TextUtils.isEmpty(alias)) {
            Toast.makeText(getActivity(),"昵称不能为空", Toast.LENGTH_SHORT).show();
            transitionDialog.dismissDialog();
            return;
        }
        if(TextUtils.isEmpty(password)) {
            Toast.makeText(getActivity(),"密码不能为空", Toast.LENGTH_SHORT).show();
            transitionDialog.dismissDialog();
            return;
        }

        Pattern pt = Pattern.compile("^(?!\\D+$)(?![^a-zA-Z]+$)\\S{6,20}$");
        Matcher matcher = pt.matcher(password);
        if(!matcher.matches()){
            Toast.makeText(getActivity(), "密码必须为6位字母加数字", Toast.LENGTH_SHORT).show();
            transitionDialog.dismissDialog();
            return;
        }
        if(!isNetworkAvailable(getActivity())){ //判断网络是否可用
            Toast.makeText(getActivity(), "网络不可用", Toast.LENGTH_SHORT).show();
            transitionDialog.dismissDialog();
            return;
        }
        //校验验证码
        SMSSDK.submitVerificationCode("86", userName, code);
    }

    /**
     * 注册请求
     */
    private void passVerify() {
        String userName = etRegisterPhoneNumber.getText().toString(); //用户名
        String code = etRegisterVerificationCode.getText().toString(); //验证码
        String alias = etRegisterUserAlias.getText().toString(); //别名
        String password = etRegisterPassword.getText().toString(); //密码

        if(!isNetworkAvailable(getActivity())){ //判断网络是否可用
            return;
        }

        OkHttpClientManager.Param[] params =null;
        OkHttpClientManager.postAsyn(Constant.ENTRANCE_PREFIX + "register.json?mobile=" + userName + "&password=" + password + "&verificationCode=000000" + "&fullName=" + alias + "&appKey=null", params, new RegisterResultCallback<String>() {

            @Override
            public void onError(Request request, Exception e) {
                //请求失败
                //Toast.makeText(getActivity().getApplicationContext(), "注册异常", Toast.LENGTH_SHORT).show();
                transitionDialog.dismissDialog();
            }

            @Override
            public void onResponse(String response) {
                JSONObject registerJson;
                final String sessionUuid; //用户唯一标识UUID
                String userName = etRegisterPhoneNumber.getText().toString();

                try {
                    registerJson = new JSONObject(response);
                    String status = registerJson.getString("status");
                    if (status.equals(Constant.USER_ALREADY_EXIST)) { //验证用户是否注册
                        Message message = new Message();
                        message.what = Integer.valueOf(Constant.USER_ALREADY_EXIST);
                        registerHandler.sendMessage(message);
                        transitionDialog.dismissDialog();
                        return;
                    }
                    if (!status.equals(Constant.LOGIN_SUCCESS_STATUS)) { //用户注册异常
                        Message message = new Message();
                        message.what = Integer.valueOf(Constant.USER_REGISTER_EXCEPTION);
                        registerHandler.sendMessage(message);
                        transitionDialog.dismissDialog();
                        return;
                    }
                    sessionUuid = registerJson.getJSONArray("rows").getJSONObject(0).getString("sessionUuid");

                    //注册子公司
                    OkHttpClientManager.Param[] params = null;
                    OkHttpClientManager.postAsyn(Constant.ENTRANCE_PREFIX + "addVirtualCom.json?sessionUuid=" + sessionUuid, params, new RegisterSubCompanyResultCallback<String>() {

                        JSONObject registerSubJson;

                        @Override
                        public void onError(Request request, Exception e) {
                            transitionDialog.dismissDialog();
                        }

                        @Override
                        public void onResponse(String response) {
                            try {
                                registerSubJson = new JSONObject(response);
                                String status = registerSubJson.getString("status");
                                if (!status.equals(Constant.LOGIN_SUCCESS_STATUS)) { //用户注册异常
                                    Message message = new Message();
                                    message.what = Integer.valueOf(Constant.USER_REGISTER_EXCEPTION);
                                    registerHandler.sendMessage(message);
                                    transitionDialog.dismissDialog();
                                    return;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });

                    //获取账户类型信息
                    String accountUrl = Constant.ENTRANCE_PREFIX + "SubmitEnter.json?sessionUuid=" + sessionUuid + "&enterprisesId=" + Constant.ENTERPRISE_TYPE_PERSONAL;
                    OkHttpClientManager.getAsyn(accountUrl, new OkHttpClientManager.ResultCallback<String>() {
                        JSONObject accountTypeJson;
                        JSONArray accountTypeArray;

                        @Override
                        public void onError(Request request, Exception e) {
                            //请求异常
                            Toast.makeText(getActivity(), Constant.INTERNET_REQUEST_ABNORMAL, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onResponse(String response) {
                            try {
                                accountTypeJson = new JSONObject(response);
                                String status = accountTypeJson.getString("status");
                                if (!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                                    //账户类型请求失败
                                    Toast.makeText(getActivity().getApplicationContext(), Constant.ACCOUNT_TYPE_INFO_REQUEST_FAIL, Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                accountTypeArray = accountTypeJson.getJSONArray("rows");
                                JSONObject rowsJson = accountTypeArray.getJSONObject(0);

                                int enterpriseId = rowsJson.getInt("enterpriseId");
                                int tenantId = rowsJson.getInt("tenantId");
                                String enterpriseName = rowsJson.getString("enterpriseName");
                                Map<Object, Object> accountTypeMap = new HashMap<Object, Object>();
                                accountTypeMap.put(Constant.ENTERPRISE_ID, enterpriseId);
                                accountTypeMap.put(Constant.ENTERPRISE_NAME, enterpriseName);

                                //Common common = new Common(getActivity().getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
                                //保存账户类型信息
                                if (!common.isSave(accountTypeMap)) {
                                    Toast.makeText(getActivity().getApplicationContext(), Constant.ACCOUNT_TYPE_INFO_SAVE_FAIL, Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                //获取角色用户
                                String roleUrl = Constant.ENTRANCE_PREFIX + "Compare.json?sessionUuid=" + sessionUuid + "&enterpriseId=" + enterpriseId;
                                OkHttpClientManager.getAsyn(roleUrl, new OkHttpClientManager.ResultCallback<String>() {
                                    @Override
                                    public void onError(Request request, Exception e) {

                                    }

                                    @Override
                                    public void onResponse(String response) {
                                        JSONObject roleJSON;
                                        JSONArray roleArray;
                                        try {
                                            roleJSON = new JSONObject(response);
                                            String status = roleJSON.getString("status");
                                            if (!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                                                //角色类型请求失败
                                                //Toast.makeText(getActivity().getApplicationContext(), "角色用户请求失败", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                            roleArray = roleJSON.getJSONArray("rows");
                                            JSONObject rowsJson = roleArray.getJSONObject(0);
                                            int roleID = rowsJson.getInt("role");
                                            Map<Object, Object> roleTypeMap = new HashMap<Object, Object>();
                                            roleTypeMap.put("role_id", roleID);
                                            //Common common = new Common(getActivity().getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
                                            //保存角色类型信息
                                            if (!common.isSave(roleTypeMap)) {
                                                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.role_type_info_save_error), Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    //获取用户ID
                    String accountIDUrl = Constant.ENTRANCE_PREFIX + "getAccountIdBySessionUuid.json?sessionUuid=" + sessionUuid;
                    OkHttpClientManager.getAsyn(accountIDUrl, new OkHttpClientManager.ResultCallback<String>() {
                        @Override
                        public void onError(Request request, Exception e) {

                        }

                        @Override
                        public void onResponse(String response) {
                            JSONObject accountIDJSON;
                            JSONArray accountIDArray;
                            try {
                                accountIDJSON = new JSONObject(response);
                                accountIDArray = accountIDJSON.getJSONArray("rows");
                                int accountID = accountIDArray.getInt(0);
                                Map<Object, Object> accountIDMap = new HashMap<Object, Object>();
                                accountIDMap.put("ACCOUNT_ID", accountID);
                                //保存账户ID
                                if (!common.isSave(accountIDMap)) {
                                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.role_type_info_save_error), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });


                    Map<Object, Object> registerMap = new HashMap<Object, Object>();
                    registerMap.put(Constant.USER_NAME, userName);
                    registerMap.put(Constant.SESSION_UUID, sessionUuid);
                    registerMap.put(Constant.ENTERPRISE_ID, Constant.ENTERPRISE_TYPE_PERSONAL);
                    Common common = new Common(getActivity().getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
                    if (!common.isSave(registerMap)) { //保存用户信息
                        Toast.makeText(getActivity().getApplicationContext(), "用户信息保存失败", Toast.LENGTH_SHORT).show();
                        transitionDialog.dismissDialog();
                        return;
                    }
                    Common userCommon = userCommon = new Common(getActivity().getSharedPreferences(Constant.USER_INFO_FILE, Context.MODE_PRIVATE));
                    if (!userCommon.isClearAccount()) {
                        Toast.makeText(getActivity(), "清除偏好用户信息失败！", Toast.LENGTH_SHORT);
                    }

                    Map<Object, Object> map = new HashMap<Object, Object>();
                    map.put(Constant.TRANSPORT_ROLE, Constant.DRIVER_ROLE_ID); //默认运输角色，设置为司机，标识0
                    //保存账户ID
                    if (!fixedCommon.isSave(map)) {
                        Toast.makeText(getActivity().getApplicationContext(), "运输服务角色保存异常", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //登录成功之后做的操作
                    transitionDialog.dismissDialog();
                    Intent intent = new Intent();
                    intent.putExtra("username", userName);
                    getActivity().setResult(Constant.REGISTER_ACTIVITY_RETURN_CODE, intent);
                    getActivity().finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 提示
     * @param status
     */
    private void toastMessage(int status) {
        switch (status) {
            case 504:
                Toast.makeText(getActivity(),"帐号已注册", Toast.LENGTH_SHORT).show();
                break;
            case 520:
                Toast.makeText(getActivity(),"无效验证码", Toast.LENGTH_SHORT).show();
                break;
            case 700:
                Toast.makeText(getActivity(),"用户注册异常", Toast.LENGTH_SHORT).show();
                break;
            case 701:
                Toast.makeText(getActivity(),"网络异常", Toast.LENGTH_SHORT).show();
                break;
            case 702:
                Toast.makeText(getActivity(),getString(R.string.account_have_been_register), Toast.LENGTH_SHORT).show();
            default:
        }
    }

    private boolean isNetworkAvailable(Context context) {
        NetWork netWork = new NetWork(getContext());
        if(!netWork.isHaveInternet()){
            //无网络访问
            return false;
        }
        return true;
    }

    private void isUserExist(String mobile) {

        String url = Constant.ENTRANCE_PREFIX+"getAccountListByLoginName.json?mobile="+mobile; //验证用户是否存在地址

        //判断网络是否有网络
        NetWork netWork = new NetWork(getContext());
        if(!netWork.isHaveInternet()){
            //无网络访问
            Toast.makeText(getActivity().getApplicationContext(), Constant.NOT_INTERNET_CONNECT, Toast.LENGTH_SHORT).show();
            return;
        }
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                //请求异常
                Toast.makeText(getActivity().getApplicationContext(), Constant.INTERNET_REQUEST_ABNORMAL,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    jsonArray = jsonObject.getJSONArray("rows");
                    Message message = new Message();
                    if(jsonArray.length() > 0) {
                        message.what = 702;
                    } else {
                        message.what = 703;
                    }
                    registerHandler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void setEnabled() {
        btRegister.setEnabled(true); //设置按钮可用
        btRegister.setBackgroundResource(R.drawable.border_corner_login_enable);
    }
    private void setDisable() {
        btRegister.setBackgroundResource(R.drawable.border_corner_login);
        btRegister.setEnabled(false); //设置按钮不可用
    }

    /**
     * 设置获取验证码按钮可用
     */
    private void setVerificationButtonEnable() {
        btObtainVerificationCode.setEnabled(true);
        btObtainVerificationCode.setBackgroundResource(R.drawable.border_corner_login_enable);
    }

    /**
     * 设置获取验证码按钮不可用
     */
    private void setVerificationCodeDisable() {
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
}
