package com.oto.edyd;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.greendao.login.DaoMaster;
import com.oto.edyd.greendao.login.DaoSession;
import com.oto.edyd.greendao.login.UserInfo;
import com.oto.edyd.greendao.login.UserInfoDao;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.CustomSharedPreferences;
import com.oto.edyd.utils.NetWork;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yql on 2015/8/27.
 * 登入首页面
 */
public class LoginFragment extends Fragment implements View.OnClickListener{

    private View loginFragmentView;
    private FragmentManager loginFragmentManager; //LoginActivity布局管理器
    private LinearLayout loginBack; //登入返回
    private TextView loginUserRegister; //登入页面注册按钮
    private EditText etUserName; //用户名
    private EditText etPassword; //密码
    private Button btLogin; //登录
    private TextView forgetPassword; //忘记密码
    private CheckBox rememberPassword; //记住密码

    private CusProgressDialog loadingDialog; //页面切换过度
    private Common common;

    //数据库
    private DaoMaster.DevOpenHelper helper;
    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private UserInfoDao userInfoDao;
    private Cursor cursor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        loginFragmentView = inflater.inflate(R.layout.login, null);

        initFields(loginFragmentView); //初始化数据

        loginBack.setOnClickListener(this);
        btLogin.setOnClickListener(this);
        loginUserRegister.setOnClickListener(this);
        etUserName.addTextChangedListener(new TextWatcher() {
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
                        String password = etPassword.getText().toString();
                        if (password != null && !(password.equals(""))) {
                            btLogin.setEnabled(true); //设置按钮可用
                            btLogin.setBackgroundResource(R.drawable.border_corner_login_enable);
                        }
                    } else {
                        btLogin.setBackgroundResource(R.drawable.border_corner_login);
                        btLogin.setEnabled(false); //设置按钮不可用
                    }
                }
            }
        });
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!(s == null)) {
                    String inputContent = s.toString();
                    if(inputContent != null && !(inputContent.equals(""))){
                        String userName = etUserName.getText().toString();
                        if(userName != null && !(userName.equals(""))){
                            btLogin.setEnabled(true); //设置按钮可用
                            btLogin.setBackgroundResource(R.drawable.border_corner_login_enable);
                        }
                    } else {
                        btLogin.setBackgroundResource(R.drawable.border_corner_login);
                        btLogin.setEnabled(false); //设置按钮不可用
                    }
                }
            }
        });

        //记住密码
        Common userCommon = new Common(getActivity().getSharedPreferences(Constant.USER_INFO_FILE, Context.MODE_PRIVATE));
        String t_username = userCommon.getStringByKey(Constant.USER_NAME);
        if(t_username != null && (!t_username.equals(""))) {
            String t_password = userCommon.getStringByKey(Constant.PASSWORD);
            etUserName.setText(t_username);
            etPassword.setText(t_password);
            rememberPassword.setChecked(true);
        } else{
            rememberPassword.setChecked(false);
        }
        return loginFragmentView;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_back:
                getActivity().finish(); //结束LoginActivity
                break;
            case R.id.login_user_register:
                //登录用户注册
                this.loginFragmentManager = ((LoginActivity)getActivity()).loginFragmentManager;
                FragmentTransaction loginTransaction = loginFragmentManager.beginTransaction();
                loginTransaction.addToBackStack(null);
                loginTransaction.replace(R.id.common_frame, new RegisterFragment());
                loginTransaction.commit();
                break;
            case R.id.bt_login:
                //用户登入
                userLogin();
                break;
            case R.id.forget_password:
                //忘记密码
                break;
            default:
                break;
        }
    }

    public abstract class LoginResultCallback<T> extends OkHttpClientManager.ResultCallback<T>{
        @Override
        public void onBefore() {
            //请求之前操作
            loadingDialog = new CusProgressDialog(getActivity(), "正在登录...");
            loadingDialog.getLoadingDialog().show();
        }

        @Override
        public void onAfter() {
            //请求之后要做的操作
            loadingDialog.getLoadingDialog().dismiss();
        }
    }

    /**
     * 数据初始化
     */
    private void initFields(View view){
        loginBack = (LinearLayout) view.findViewById(R.id.login_back);
        loginUserRegister = (TextView) view.findViewById(R.id.login_user_register);
        etUserName = (EditText) view.findViewById(R.id.login_username);
        etPassword = (EditText) view.findViewById(R.id.login_password);
        btLogin = (Button) view.findViewById(R.id.bt_login);
        forgetPassword = (TextView) view.findViewById(R.id.forget_password);
        rememberPassword = (CheckBox) view.findViewById(R.id.remember_password);

        common = new Common(getActivity().getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));

        helper = new DaoMaster.DevOpenHelper(getActivity(), "USER_INFO", null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        userInfoDao = daoSession.getUserInfoDao();
        String orderBy = "_id COLLATE LOCALIZED ASC";
        cursor = db.query(userInfoDao.getTablename(), userInfoDao.getAllColumns(), null, null, null, null, orderBy);
    }

    /**
     * 用户登录
     */
    private void userLogin(){
        String userName = etUserName.getText().toString();
        String password = etPassword.getText().toString();

        if(TextUtils.isEmpty(userName)){
            Toast.makeText(getActivity().getApplicationContext(), Constant.NOT_NULL_USERNAME, Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(getActivity().getApplicationContext(), Constant.NOT_NULL_PASSWORD, Toast.LENGTH_SHORT).show();
            return;
        }
        String url = Constant.ENTRANCE_PREFIX+"login.json?mobile="+userName+"&password="+password+"&appKey=null"; //登录访问地址

        //判断网络是否有网络
        NetWork netWork = new NetWork(getContext());
        if(!netWork.isHaveInternet()){
            //无网络访问
            Toast.makeText(getActivity().getApplicationContext(), Constant.NOT_INTERNET_CONNECT, Toast.LENGTH_SHORT).show();
            return;
        }
        OkHttpClientManager.getAsyn(url, new LoginResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                //请求异常
                Toast.makeText(getActivity().getApplicationContext(), Constant.INTERNET_REQUEST_ABNORMAL,Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onResponse(String response) {
                //请求成功
                JSONObject loginJSON;
                final String sessionUuid;
                String username = etUserName.getText().toString();

                try {
                    loginJSON = new JSONObject(response);
                    if (!loginJSON.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        //验证登入是否成功
                        Toast.makeText(getActivity().getApplicationContext(), Constant.INVALID_USERNAME_PASSWORD, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    sessionUuid = loginJSON.getJSONArray("rows").getJSONObject(0).getString("sessionUuid");
                    Map<Object, Object> loginMap = new HashMap<Object, Object>();
                    loginMap.put(Constant.USER_NAME, username);
                    loginMap.put(Constant.SESSION_UUID, sessionUuid);
                    //Common common = new Common(getActivity().getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
                    if (!common.isSave(loginMap)) {
                        //用户信息保存失败
                        Toast.makeText(getActivity().getApplicationContext(), Constant.USER_INFO_SAVE_FAIL, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<Object, Object> rememberMap = new HashMap<Object, Object>();
                    rememberMap.put(Constant.USER_NAME, username);
                    rememberMap.put(Constant.PASSWORD, etPassword.getText().toString());
                    isRememberPassword(rememberPassword, rememberMap); //是否保存用户信息

//                    List sessionUUIDList = userInfoDao.queryBuilder()
//                            .where(UserInfoDao.Properties.Session_uuid.eq(sessionUuid))
//                            .list();
//                    if(sessionUUIDList.size() == 0) {
//                        String password = etPassword.getText().toString();
//                        UserInfo userInfo = new UserInfo(null, sessionUuid, username, password);
//                        userInfoDao.insertOrReplace(userInfo);
//                    }

                    //获取账户类型信息
                    String accountUrl = Constant.ENTRANCE_PREFIX + "SubmitEnter.json?sessionUuid="+sessionUuid+"&enterprisesId="+Constant.ENTERPRISE_TYPE_PERSONAL;
                    OkHttpClientManager.getAsyn(accountUrl, new OkHttpClientManager.ResultCallback<String>() {
                        JSONObject accountTypeJson;
                        JSONArray accountTypeArray;

                        @Override
                        public void onError(Request request, Exception e) {
                            //请求异常
                            Toast.makeText(getActivity().getApplicationContext(), Constant.INTERNET_REQUEST_ABNORMAL,Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onResponse(String response) {
                            try {
                                accountTypeJson = new JSONObject(response);
                                String status = accountTypeJson.getString("status");
                                if(!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
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
                                String roleUrl = Constant.ENTRANCE_PREFIX + "Compare.json?sessionUuid="+sessionUuid+"&enterpriseId="+enterpriseId;
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
                                            if(!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
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
                    String accountIDUrl = Constant.ENTRANCE_PREFIX + "getAccountIdBySessionUuid.json?sessionUuid="+sessionUuid;
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

                    //登录成功之后做的操作
                    Intent intent = new Intent();
                    intent.putExtra("username", username);
                    getActivity().setResult(Constant.LOGIN_ACTIVITY_RETURN_CODE, intent);
                    getActivity().finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 是否记住密码
     * @param checkBox
     */
    private void isRememberPassword(CheckBox checkBox, Map<Object, Object> loginMap) {
        boolean isChecked = checkBox.isChecked();
        Common userCommon = userCommon = new Common(getActivity().getSharedPreferences(Constant.USER_INFO_FILE, Context.MODE_PRIVATE));
        if(isChecked) {

            if(!userCommon.isSave(loginMap)) {
                Toast.makeText(getActivity(), "保存偏好用户信息失败！", Toast.LENGTH_SHORT);
            }
        } else {
            if(!userCommon.isClearAccount()) {
                Toast.makeText(getActivity(), "清除偏好用户信息失败！", Toast.LENGTH_SHORT);
            }
        }
    }

}
