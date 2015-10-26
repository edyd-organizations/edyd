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
import android.widget.Toast;

import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.NetWork;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

/**
 * Created by yql on 2015/10/26.
 */
public class ForgetPasswordActivity extends Activity implements View.OnClickListener, View.OnFocusChangeListener{

    private LinearLayout back; //返回
    private EditText registerPhoneNumber; //手机号码
    private Button getVerificationCode; //获取验证码
    private EditText verificationCode; //验证码
    private EditText registerUserPassword; //密码
    private Button btModifyButton; //完成按钮


    private boolean asyncIsOver = false; //异步线程是否结束
    private VerificationProgressAsyncTask verificationProgressAsyncTask; //异步消息对象
    //private CusProgressDialog modifyDialog; //页面切换过度

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forget_password);
        initFields();
        verify();

        back.setOnClickListener(this);
        getVerificationCode.setOnClickListener(this);
        btModifyButton.setOnClickListener(this);

        //控制获取验证码按钮是否可用
        registerPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null) {
                    String phoneNumber = s.toString();
                    if (phoneNumber != null && !(phoneNumber.equals(""))) {
                        int phoneNumberLength = phoneNumber.length();
                        if (phoneNumberLength == 11) {
                            getVerificationCode.setEnabled(true);
                            getVerificationCode.setBackgroundResource(R.drawable.border_corner_login_enable);
                        } else {
                            getVerificationCode.setEnabled(false);
                            getVerificationCode.setBackgroundResource(R.drawable.border_corner_login);
                        }
                    } else {
                        getVerificationCode.setEnabled(false);
                        getVerificationCode.setBackgroundResource(R.drawable.border_corner_login);
                    }
                }
            }
        });
    }


    /**
     * 校验表单是否为空，并设置完成按钮是否可用
     */
    private void verify() {
        registerPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null) {
                    String inputContent = s.toString();
                    if (inputContent != null && !(inputContent.equals(""))) {
                        String code = verificationCode.getText().toString();
                        String password = registerUserPassword.getText().toString();

                        if (code != null && !(code.equals(""))) {
                            if (password != null && !(password.equals(""))) {
                                setEnabled();
                            }
                        }
                    } else {
                        setDisable();
                    }
                }
            }
        });
        verificationCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null) {
                    String inputContent = s.toString();
                    if (inputContent != null && !(inputContent.equals(""))) {
                        String phoneNumber = registerPhoneNumber.getText().toString();
                        String password = registerUserPassword.getText().toString();

                        if (phoneNumber != null && !(phoneNumber.equals(""))) {
                            if (password != null && !(password.equals(""))) {
                                setEnabled();
                            }
                        }
                    } else {
                        setDisable();
                    }
                }
            }
        });
        registerUserPassword.addTextChangedListener(new TextWatcher() {
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
                        String phoneNumber = registerPhoneNumber.getText().toString();
                        String code = verificationCode.getText().toString();

                        if (phoneNumber != null && !(phoneNumber.equals(""))) {
                            if (code != null && !(code.equals(""))) {
                                setEnabled();
                            }
                        }
                    } else {
                        setDisable();
                    }
                }
            }
        });
    }

    /**
     * 数据初始化
     */
    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
        registerPhoneNumber = (EditText) findViewById(R.id.register_phone_number);
        getVerificationCode = (Button) findViewById(R.id.get_verification_code);
        verificationCode = (EditText) findViewById(R.id.verification_code);
        registerUserPassword = (EditText) findViewById(R.id.register_user_password);
        btModifyButton = (Button) findViewById(R.id.modify_password);

        initSMSDK(); //初始化短信服务
    }

    /**
     * 初始化短信
     */
    private void initSMSDK() {
        SMSSDK.initSDK(getApplicationContext(), Constant.APPKEY, Constant.APPSECRET);
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
                            handler.sendMessage(message);
                            return;
                        }

                    } catch (JSONException e) {
                        message = new Message();
                        message.what = Constant.NETWORK_EXCEPTION;
                        handler.sendMessage(message);
                        e.printStackTrace();
                    }

                }
            }
        };
        SMSSDK.registerEventHandler(eh); //注册短信回调
    }

    public abstract class ModifyPasswordResultCallback<T> extends OkHttpClientManager.ResultCallback<T>{
        @Override
        public void onBefore() {
            //请求之前操作
           // modifyDialog = new CusProgressDialog(getApplicationContext(), "正在修改...");
           // modifyDialog.getLoadingDialog().show();
        }

        @Override
        public void onAfter() {
            //请求之后要做的操作
           // modifyDialog.getLoadingDialog().dismiss();
        }
    }

    /**
     * 修改请求
     */
    private void passVerify() {
        String userName = registerPhoneNumber.getText().toString(); //用户名
        String code = verificationCode.getText().toString(); //验证码
        String password = registerUserPassword.getText().toString(); //密码

        if(!isNetworkAvailable(getApplicationContext())){ //判断网络是否可用
            return;
        }

        String url = Constant.ENTRANCE_PREFIX+"forgetPassword.json?mobile="+userName+"&newPassword="+password+"&verificationCode=000000"+"&appKey=null"; //登录访问地址
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if(!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(getApplicationContext(), "修改密码失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(getApplicationContext(), "修改密码成功", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 线程通讯
     */
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 520: //无效验证码
                    toastMessage(msg.what);
                    break;
                case 700:
                    toastMessage(msg.what);
                    break;
                case 701:
                    toastMessage(msg.what);
                    break;
                default:
            }
        }
    };
    /**
     * 提示
     * @param status
     */
    private void toastMessage(int status) {
        switch (status) {
            case 520:
                Toast.makeText(getApplicationContext(),"无效验证码", Toast.LENGTH_SHORT).show();
                break;
            case 700:
                Toast.makeText(getApplicationContext(),"用户注册异常", Toast.LENGTH_SHORT).show();
                break;
            case 701:
                Toast.makeText(getApplicationContext(),"网络异常", Toast.LENGTH_SHORT).show();
                break;
            default:
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: //返回
                finish();
                break;
            case R.id.get_verification_code: //获取验证码
                if(!isNetworkAvailable(getApplicationContext())) {
                    Toast.makeText(getApplicationContext(), "无网络连接", Toast.LENGTH_SHORT).show();
                }
                String mobile = registerPhoneNumber.getText().toString();
                verificationProgressAsyncTask = new VerificationProgressAsyncTask(getVerificationCode);
                verificationProgressAsyncTask.execute(Constant.WAITING_TIME_VERIFICATION);
                SMSSDK.getVerificationCode("86", mobile); //向短信服务运营商请求短信验证码
                break;
            case R.id.modify_password:
                modifyConfirm();
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.verification_code: //验证码位数验证
                if(!hasFocus) {
                    String editable = ((EditText)v).getText().toString();
                    if(editable !=null && !(editable.equals(""))) {
                        if(editable.length() != Constant.VERIFICATION_LENGTH) {
                            Toast.makeText(getApplicationContext(), "验证码位数必须为4位", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
            case R.id.register_user_password: //密码位数验证
                if(!hasFocus) {
                    String editable = ((EditText)v).getText().toString();
                    if(editable !=null && !(editable.equals(""))) {
                        if(editable.length() < Constant.PASSWORD_LENGTH) {
                            Toast.makeText(getApplicationContext(), "密码位数不能低于6位", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Pattern pt = Pattern.compile("^(?!\\D+$)(?![^a-zA-Z]+$)\\S{6,20}$");
                        Matcher matcher = pt.matcher(editable.toString());
                        if(!matcher.matches()){
                            Toast.makeText(getApplicationContext(), "密码必须字母加数字", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
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
            btVerificationCode.setBackgroundResource(R.drawable.border_corner_login); //获取验证码后变灰色
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

    /**
     * 表单验证
     */
    private void modifyConfirm() {
        String userName = registerPhoneNumber.getText().toString(); //用户名
        String code = verificationCode.getText().toString(); //验证码
        String password = registerUserPassword.getText().toString(); //密码

        if(TextUtils.isEmpty(userName)) {
            Toast.makeText(getApplicationContext(),"手机号码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(code)) {
            Toast.makeText(getApplicationContext(),"验证码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(),"密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        Pattern pt = Pattern.compile("^(?!\\D+$)(?![^a-zA-Z]+$)\\S{6,20}$");
        Matcher matcher = pt.matcher(password);
        if(!matcher.matches()){
            Toast.makeText(getApplicationContext(), "密码必须为6位字母加数字", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!isNetworkAvailable(getApplicationContext())){ //判断网络是否可用
            Toast.makeText(getApplicationContext(), "网络不可用", Toast.LENGTH_SHORT).show();
            return;
        }
        //校验验证码
        SMSSDK.submitVerificationCode("86", userName, code);
    }

    private boolean isNetworkAvailable(Context context) {
        NetWork netWork = new NetWork(getApplicationContext());
        if(!netWork.isHaveInternet()){
            //无网络访问
            return false;
        }
        return true;
    }

    private void setStatus(Button btVerificationCode) {
        btVerificationCode.setEnabled(true); //可点击
        btVerificationCode.setTextSize(10);
        btVerificationCode.setText(R.string.confirm_code_text);
        btVerificationCode.setBackgroundResource(R.drawable.border_corner_login_enable);
    }

    private void setEnabled() {
        btModifyButton.setEnabled(true); //设置按钮可用
        btModifyButton.setBackgroundResource(R.drawable.border_corner_login_enable);
    }
    private void setDisable() {
        btModifyButton.setBackgroundResource(R.drawable.border_corner_login);
        btModifyButton.setEnabled(false); //设置按钮不可用
    }
}
