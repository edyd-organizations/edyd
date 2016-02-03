package com.oto.edyd.module.usercenter.activity;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

/**
 * 功能：找回密码
 * 文件名：com.oto.edyd.usercenter.activity.ForgetPasswordActivity.java
 * 创建时间：2015/10/26.
 * 作者：yql
 */
public class ForgetPasswordActivity extends Activity implements View.OnClickListener{

    // --------------View基本控件---------------------
    private LinearLayout back; //返回
    private EditText etMobilePhoneNumber; //手机号码
    private Button obtainVerificationCode; //获取验证码
    private EditText etVerificationCode; //验证码
    private EditText etRegisterUserPassword; //密码
    private Button btModifyButton; //完成按钮
    private ImageView visiblePassword; //密码是否可见

    // --------------线程通讯对象---------------------
    //异步线程
    private VerificationCodeProgressAsyncTask asyncTask; //异步消息对象

    // --------------变量---------------------
    private Context context; //上下文对象
    private Common common; //偏好共享对象
    private CusProgressDialog transitionDialog; //过度对话框
    private boolean asyncIsOver = true; //异步线程是否结束
    //Handler返回码
    private static final int MOBILE_PHONE_ALREADY_REGISTER = 0x10; //账号已注册
    private static final int MOBILE_PHONE_WITHOUT_REGISTER = 0x11; //账号未注册
    private static final int HANDLER_MODIFY_PASSWORD_SUCCESS = 0x12; //请求修改密码成功
    private static final int HANDLER_REQUEST_VERIFY_CODE_UNKNOWN_ERROR = 0x13; //请求验证码未知错误
    private static final int VERIFICATION_AUTHENTICATE_SUCCESS = 0x14; //验证码认证成功
    private static final int HANDLER_FREQUENTLY_REQUEST_VERIFY_CODE = 467; //频繁请求验证码
    private static final int HANDLER_INVALID_VERIFY_CODE = 468; //无效验证码
    private static final int MOBILE_PHONE_LENGTH  = 11; //手机号码长度


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forget_password);
        init(); //初始化数据
    }

    /**
     * 数据初始化
     */
    private void init() {
        initFields(); //初始化字段
        initListener(); //初始化监听器
        initSMSDK(); //初始化短信服务
    }

    /**
     * 数据初始化
     */
    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
        etMobilePhoneNumber = (EditText) findViewById(R.id.register_phone_number);
        obtainVerificationCode = (Button) findViewById(R.id.get_verification_code);
        etVerificationCode = (EditText) findViewById(R.id.verification_code);
        etRegisterUserPassword = (EditText) findViewById(R.id.register_user_password);
        btModifyButton = (Button) findViewById(R.id.modify_password);
        visiblePassword = (ImageView) findViewById(R.id.visible_password);
        context = ForgetPasswordActivity.this;
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        back.setOnClickListener(this);
        obtainVerificationCode.setOnClickListener(this);
        btModifyButton.setOnClickListener(this);
        visiblePassword.setOnClickListener(this);

        //手机号码输入框注册监听器
        etMobilePhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString(); //手机号码
                if(!TextUtils.isEmpty(content)) { //判断手机号码是否为空
                    //手机号码不为空
                    int length = s.length(); //手机号码长度
                    if(length == MOBILE_PHONE_LENGTH && asyncIsOver ) { //判断手机号码长度是否11位
                        //手机号码长度11位
                        setVerificationCodeButtonEnable(); //设置获取验证码按钮橙色且可用
                        String verificationCode = etVerificationCode.getText().toString(); //获取验证码
                        if(!TextUtils.isEmpty(verificationCode)) { //判断验证码是否为空
                            //验证码不为空
                            String alias = etRegisterUserPassword.getText().toString();
                            if(!TextUtils.isEmpty(alias)) { //判断密码是否为空
                                //密码不为空
                                setModifyPasswordButtonEnabled(); //满足以上条件，设置注册按钮位橙色且可用
                            }
                        }
                    } else {
                        //手机号码小于11位
                        setVerificationCodeButtonDisable(); //设置获取验证码按钮灰色且不可用
                    }
                } else {
                    //手机号码为空
                    setVerificationCodeButtonDisable();
                    setModifyPasswordButtonDisabled(); //设置注册按钮不可用
                }
            }
        });

        etVerificationCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString(); //验证码
                if (!TextUtils.isEmpty(content)) { //判断验证码是否为空
                    String phoneNumber = etMobilePhoneNumber.getText().toString(); //获取手机号码
                    if (!TextUtils.isEmpty(phoneNumber)) {
                        String password = etRegisterUserPassword.getText().toString(); //获取密码
                        if (!TextUtils.isEmpty(password)) {
                            setModifyPasswordButtonEnabled(); //设置密码按钮可用且为橙色
                        }
                    }
                } else {
                    //验证码为空
                    setModifyPasswordButtonDisabled(); //设置密码按钮不可用且为灰色
                }
            }
        });
        etRegisterUserPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString(); //密码
                if (!TextUtils.isEmpty(content)) { //判断密码是否为空
                    //密码不为空
                    String phoneNumber = etMobilePhoneNumber.getText().toString(); //获取手机号码
                    if (!TextUtils.isEmpty(phoneNumber)) { //判断手机号码是否为空
                        //手机号码不为空
                        String verifyCode = etVerificationCode.getText().toString(); //验证码
                        if (!TextUtils.isEmpty(verifyCode)) { ///判断验证码是否为空
                            //验证码不为空
                            setModifyPasswordButtonEnabled();
                        }
                    }
                } else {
                    //密码为空
                    setModifyPasswordButtonDisabled();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: //返回
                finish();
                break;
            case R.id.get_verification_code: //获取验证码
                confirmMoPoWhetherAlRegister(); //验证用户是否已注册
                break;
            case R.id.modify_password: //修改密码
                authenticateVerificationIsSuccess(); //认证验证码
                break;
            case R.id.visible_password:
                int isVisible = etRegisterUserPassword.getInputType();
                if(isVisible == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) { //当前密码显示
                    etRegisterUserPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); //设置隐藏
                    etRegisterUserPassword.setSelection(etRegisterUserPassword.length()); //设置光标位置
                    visiblePassword.setImageResource(R.mipmap.cipher_text);
                } else { //当前密码隐藏
                    etRegisterUserPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD); //设置显示
                    etRegisterUserPassword.setSelection(etRegisterUserPassword.length());
                    visiblePassword.setImageResource(R.mipmap.plain_text);
                }
                break;
        }
    }

    /**
     * 验证手机号码是否已注册
     */
    private void confirmMoPoWhetherAlRegister() {
        String mobilePhone = etMobilePhoneNumber.getText().toString(); //手机号码
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
                        //有数据代表账号已注册
                        message.what = MOBILE_PHONE_ALREADY_REGISTER;
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
     * 请求短信验证码
     */
    private void requestVerificationCode() {
        String mobilePhone = etMobilePhoneNumber.getText().toString(); //手机号码

        //开启获取验证码倒计时
        asyncTask = new VerificationCodeProgressAsyncTask();
        asyncTask.execute(Constant.WAITING_TIME_VERIFICATION);
        //校验验证码
        SMSSDK.getVerificationCode("86", mobilePhone);
    }

    /**
     *认证短信验证码
     */
    private void authenticateVerificationIsSuccess() {
        String mobilePhone = etMobilePhoneNumber.getText().toString(); //手机号码
        String verificationCode = etVerificationCode.getText().toString(); //验证码
        String password = etRegisterUserPassword.getText().toString(); //密码

        //匹配密码是否符合要求
        Pattern pt = Pattern.compile(Constant.MATCH_REGISTER_PASSWORD);
        Matcher matcher = pt.matcher(password);
        if(!matcher.matches()){
            common.showToast(context, "密码必须为6位字母加数字");
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
     * 请求修改密码
     */
    private void requestModifyPassword() {
        String userName = etMobilePhoneNumber.getText().toString(); //用户名
        String password = etRegisterUserPassword.getText().toString(); //密码

        //判断网络连通性
        if(!isNetworkAvailable(context)){
            //网络不可用
            common.showToast(context, Constant.NOT_INTERNET_CONNECT);
            return;
        }
        String url = Constant.ENTRANCE_PREFIX+"forgetPassword.json?mobile="+userName+"&newPassword="+password+"&verificationCode=000000"+"&appKey=null"; //登录访问地址
        OkHttpClientManager.getAsyn(url, new ModifyPasswordResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                common.showToast(context, "请求修改密码异常");
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if (!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        common.showToast(context, "修改失败");
                        return;
                    }
                    Message message = Message.obtain();
                    message.what = HANDLER_MODIFY_PASSWORD_SUCCESS;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 初始化短信
     */
    private void initSMSDK() {
        SMSSDK.initSDK(getApplicationContext(), Constant.APPKEY, Constant.APPSECRET);
        EventHandler eh = new EventHandler(){
            @Override
            public void afterEvent(int event, int result, Object data) {
                Message message = Message.obtain();
                if (result == SMSSDK.RESULT_COMPLETE) {
                    //回调完成
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        //提交验证码成功
                        message.what = VERIFICATION_AUTHENTICATE_SUCCESS;
                        handler.sendMessage(message);
                    }else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE){
                        //获取验证码成功
                    }else if (event ==SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES){
                        //返回支持发送验证码的国家列表
                    }
                }else{
                    //这里不是UI线程，如果要更新或者操作UI，要调用UI线程
                    String dataStr = data.toString();
                    String str = dataStr.substring(dataStr.indexOf(":")+1).trim();
                    try {
                        JSONObject dataJson = new JSONObject(str);
                        int status = dataJson.getInt("status");
                        switch (status) {
                            case HANDLER_FREQUENTLY_REQUEST_VERIFY_CODE: //请求验证码过于频繁
                                message.what = HANDLER_FREQUENTLY_REQUEST_VERIFY_CODE;
                                handler.sendMessage(message);
                                break;
                            case HANDLER_INVALID_VERIFY_CODE: //无效验证码
                                message.what = HANDLER_INVALID_VERIFY_CODE;
                                handler.sendMessage(message);
                                break;
                            default: //未知错误
                                message.what = HANDLER_REQUEST_VERIFY_CODE_UNKNOWN_ERROR;
                                handler.sendMessage(message);
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
     * 线程通讯
     */
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MOBILE_PHONE_ALREADY_REGISTER: //账号已注册，不能获取验证码
                   requestVerificationCode(); //请求验证码
                    break;
                case MOBILE_PHONE_WITHOUT_REGISTER: //账号未注册，请求获取验证码
                    common.showToast(context, "账号未注册");
                    break;
                case HANDLER_MODIFY_PASSWORD_SUCCESS: //修改密码成功
                    common.showToast(context, "修改成功");
                    finish();
                    break;
                case HANDLER_INVALID_VERIFY_CODE: //无效验证码
                    common.showToast(context, "无效验证码");
                    break;
                case VERIFICATION_AUTHENTICATE_SUCCESS: //验证码认证成功
                    requestModifyPassword(); //修改密码
                    break;
                case HANDLER_FREQUENTLY_REQUEST_VERIFY_CODE: //请求验证码过于频繁
                    common.showToast(context, "请求校验验证码频繁");
                    break;
                case HANDLER_REQUEST_VERIFY_CODE_UNKNOWN_ERROR: //请求验证码未知错误
                    common.showToast(context, "请求校验验未知错误");
                default:
            }
        }
    };

    /**
     * 检查网络连通性
     * @param context 上下文对象
     * @return true有网络；false无网络
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
     * 设置获取验证码按钮可用且颜色为橙色
     */
    private void setVerificationCodeButtonEnable() {
        obtainVerificationCode.setEnabled(true);
        obtainVerificationCode.setBackgroundResource(R.drawable.border_corner_login_enable);
    }

    /**
     * 设置获取验证码按钮不可用且颜色为灰色
     */
    private void setVerificationCodeButtonDisable() {
        obtainVerificationCode.setEnabled(false);
        obtainVerificationCode.setBackgroundResource(R.drawable.border_corner_login);
    }

    /**
     * 设置修改密码按钮可用且颜色为橙色
     */
    private void setModifyPasswordButtonEnabled() {
        btModifyButton.setEnabled(true); //设置按钮可用
        btModifyButton.setBackgroundResource(R.drawable.border_corner_login_enable);
    }

    /**
     * 设置修改密码按钮不可用且颜色为灰色
     */
    private void setModifyPasswordButtonDisabled() {
        btModifyButton.setBackgroundResource(R.drawable.border_corner_login);
        btModifyButton.setEnabled(false); //设置按钮不可用
    }

    /**
     * 网络请求回调接口
     * @param <T>
     */
    public abstract class ModifyPasswordResultCallback<T> extends OkHttpClientManager.ResultCallback<T>{

        @Override
        public void onBefore() {
            //请求之前操作
            transitionDialog = new CusProgressDialog(context, "正在修改...");
            transitionDialog.getLoadingDialog().show();
        }

        @Override
        public void onAfter() {
            //请求之后要做的操作
            transitionDialog.getLoadingDialog().dismiss();
        }
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
            asyncIsOver = false; //标记异步线程开始
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
            asyncIsOver = true; //标记异步线程结束
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
                obtainVerificationCode.setText("重新发送");
            } else {
                obtainVerificationCode.setText("重新发送("+String.valueOf(cusTime)+")");
            }

        }
    }
}
