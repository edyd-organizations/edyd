package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
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

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

/**
 * 油卡设置密码界面
 */


public class OilCarSetPasswordActivity extends Activity implements View.OnClickListener {
    String autoCode;//验证码
    TextView teSubmit;//提交
    Button sendCode;//发送验证码
    EditText edPassword;
    EditText edSurepassword;
    private Common common;
    EditText edAutoCode;
    private Context context; //上下文对象
    private boolean asyncIsOver = false; //异步线程是否结束
    private VerificationCodeProgressAsyncTask asyncTask; //异步消息对象
    private static final int VERIFICATION_AUTHENTICATE_SUCCESS = 0x20; //验证码认证成功
    private final static int INVALID_MOBILE_PHONE = 467; //请求校验验证码频繁（5分钟内同一个appkey的同一个号码最多只能校验三次）
    private final static int INVALID_VERIFICATION_CODE = 468; //无效验证码
    private LinearLayout back; //返回
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oil_car_set_password);
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        init();//初始化
        initSMSDK(); //初始化短信服务
    }
    private void init() {
        edPassword = (EditText) findViewById(R.id.password);
        edSurepassword = (EditText) findViewById(R.id.surepassword);
        edAutoCode = (EditText) findViewById(R.id.autoCode);
        teSubmit = (TextView) findViewById(R.id.submit);
        teSubmit.setOnClickListener(this);
        sendCode = (Button) findViewById(R.id.btnSendCode);
        sendCode.setOnClickListener(this);
        context = OilCarSetPasswordActivity.this;
        back = (LinearLayout) findViewById(R.id.back);
        back.setOnClickListener(this);
        edPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String autoCode = edAutoCode.getText().toString().trim();
                if (!(s == null)) {
                    if (!TextUtils.isEmpty(s.toString())) {
                        if (autoCode != null && !(autoCode.equals(""))) {
                            teSubmit.setEnabled(true); //设置按钮可用
                            teSubmit.setBackgroundResource(R.drawable.border_corner_login_enable);
                        }
                    } else {
                        teSubmit.setBackgroundResource(R.drawable.border_corner_login);
                        teSubmit.setEnabled(false); //设置按钮不可用
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edAutoCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = edPassword.getText().toString().trim();
                if (!(s == null)) {
                    if (!TextUtils.isEmpty(s.toString())) {
                        if (password != null && !(password.equals(""))) {
                            teSubmit.setEnabled(true); //设置按钮可用
                            teSubmit.setBackgroundResource(R.drawable.border_corner_login_enable);
                        }
                    } else {
                        teSubmit.setBackgroundResource(R.drawable.border_corner_login);
                        teSubmit.setEnabled(false); //设置按钮不可用
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * 初始化短信
     */
    private void initSMSDK() {
        SMSSDK.initSDK(this, Constant.APPKEY, Constant.APPSECRET);
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
                    try {
                        JSONObject dataJson = new JSONObject(str);
                        int status = dataJson.getInt("status");
                        message = Message.obtain();
                        switch (status) {
                            case INVALID_MOBILE_PHONE: //验证码请求频繁
                                message.what = Integer.valueOf(Constant.INVALID_MOBILE_PHONE);
                                handler.sendMessage(message);
                                break;
                            case INVALID_VERIFICATION_CODE: //无效验证码
                                message.what = Integer.valueOf(Constant.INVALID_VERIFICATION_CODE);
                                handler.sendMessage(message);
                                break;
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
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case VERIFICATION_AUTHENTICATE_SUCCESS: //验证码认证成功
                    requestport();//请求接口
                    break;
                case INVALID_VERIFICATION_CODE: //无效验证码
                    common.showToast(context, "无效验证码");
                    break;
                case INVALID_MOBILE_PHONE: //无效手机号
                    common.showToast(context, "5分钟内同一个号码最多只能校验三次");
                    break;
                default:
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.submit:
                checkInfo();//检查信息
                break;
            case R.id.btnSendCode:
                requestVerificationCode(); //请求验证码
                break;
            case R.id.back: //返回
                finish();
                break;
        }
    }

    private void checkInfo() {
        String password = edPassword.getText().toString().trim();
        String surePassword = edSurepassword.getText().toString().trim();
        String autoCode = edAutoCode.getText().toString().trim();
        String mobilePhone = common.getStringByKey(Constant.USER_NAME);
        if(password != null && password.equals("")){
           Common.showToast(this,"密码不能为空");
            return;
        }
        Pattern pattern = Pattern.compile("^(?!\\D+$)(?![^a-zA-Z]+$)\\S{6,20}$"); //字母数字
        Matcher matcher = pattern.matcher(password);
        if(!matcher.matches()) {
            Common.showToast(this, "密码必须包含数字、字母、特殊字符且长度为6-20");
            return;
        }
        if(surePassword != null && surePassword.equals("")){
            Common.showToast(this, "确认密码不能为空");
            return;
        }

        if(!password.equals(surePassword)) {
            Common.showToast(this, "密码输入不一致");
            return;
        }
        if (autoCode!=null&&autoCode.equals("")){
            Common.showToast(this, "验证码不能为空");
            return;
        }
        //校验验证码
        SMSSDK.submitVerificationCode("86", mobilePhone, autoCode);
    }
    /**
     * 请求验证码
     */
    private void requestVerificationCode() {
        String mobilePhone = common.getStringByKey(Constant.USER_NAME);
        //开启获取验证码按钮倒计时
        asyncTask = new VerificationCodeProgressAsyncTask();
        asyncTask.execute(Constant.WAITING_TIME_VERIFICATION);
        //向短信服务运营商请求短信验证码
        SMSSDK.getVerificationCode("86", mobilePhone);
    }

    private void requestport() {
        String password = edPassword.getText().toString().trim();
        String accountID = common.getStringByKey("ACCOUNT_ID");
        String url = Constant.ENTRANCE_PREFIX + "createPayPassword.json?accountId="+accountID+"&payPassword="+password;
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                //请求异常
                common.showToast(OilCarSetPasswordActivity.this, Constant.INTERNET_REQUEST_ABNORMAL);
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    jsonArray = jsonObject.getJSONArray("rows");
                    common.showToast(OilCarSetPasswordActivity.this, "设置密码成功");
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
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
                sendCode.setText("重新发送");
            } else {
                sendCode.setText("重新发送("+String.valueOf(cusTime)+")");
            }

        }
    }
    /**
     * 设置获取验证码按钮不可用
     */
    private void setVerificationCodeButtonDisable() {
        sendCode.setEnabled(false);
        sendCode.setBackgroundResource(R.drawable.border_corner_login);
    }

    /**
     * 设置获取验证码按钮可用
     */
    private void setVerificationCodeButtonEnable() {
        sendCode.setEnabled(true);
        sendCode.setBackgroundResource(R.drawable.border_corner_login_enable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //SMSSDK.unregisterEventHandler(eh); //取消单个
        SMSSDK.unregisterAllEventHandler(); //取消所有
    }
}
