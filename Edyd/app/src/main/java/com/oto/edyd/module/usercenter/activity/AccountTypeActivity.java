package com.oto.edyd.module.usercenter.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.oto.edyd.R;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 功能：选择账户类型
 * 文件名：com.oto.edyd.module.usercenter.activity.AccountTypeActivity.java
 * 创建时间：2016/1/26
 * 作者：yql
 */
public class AccountTypeActivity extends Activity implements View.OnClickListener {
    //--------------基本View控件--------------
    private LinearLayout back; //返回
    private ListView accountTypeList; //ListView
    private CusProgressDialog transitionDialog; //过度Dialog
    private TextView title; //标题
    private View bottomLine; //listView底线
    //--------------变量--------------
    private Context context; //上下文对象
    private Common common; //偏好文件LOGIN_PREFERENCES_FILE
    private Common fixedCommon; //偏好文件FIXED_FILE
    private AccountTypeAdapter adapter; //适配器
    private List<AccountTypeInfoBean> accountTypeInfoBeanList = new ArrayList<AccountTypeInfoBean>(); //账户类型list
    private final static int HANDLER_ACCOUNT_TYPE_CODE = 0x10; //账户类型返回码
    private final static int HANDLER_UPDATE_ROLE_CODE = 0x11; //角色类型返回码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.common_list);
        init();
    }

    /**
     * 初始化数据
     */
    private void init() {
        initFields();
        initListener();
        title.setText("选择账户类型");
        requestAccountTypeList();
    }

    /**
     * 初始化字段
     */
    private void initFields() {
        context = AccountTypeActivity.this;
        back = (LinearLayout) findViewById(R.id.back);
        accountTypeList = (ListView) findViewById(R.id.common_list);
        title = (TextView) findViewById(R.id.common_list_title);
        bottomLine = findViewById(R.id.common_bottom_line);
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        fixedCommon = new Common(getSharedPreferences(Constant.FIXED_FILE, Context.MODE_PRIVATE));
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        back.setOnClickListener(this);
        accountTypeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                AccountTypeInfoBean accountTypeInfo = accountTypeInfoBeanList.get(position);
                String txEnterpriseId = accountTypeInfo.getEnterpriseId();
                String txEnterpriseName = accountTypeInfo.getEnterpriseName();

                if (position == 0) {
                    intent = new Intent();
                    intent.putExtra("account_type", txEnterpriseName);
                    intent.putExtra("enterpriseId", txEnterpriseId);

                    Map<Object, Object> map = new HashMap<Object, Object>();
                    map.put(Constant.ENTERPRISE_ID, Integer.valueOf(txEnterpriseId));
                    map.put(Constant.ENTERPRISE_NAME, txEnterpriseName);
                    map.put(Constant.ORG_CODE, String.valueOf(""));
                    map.put(Constant.ROLE_NAME, "");
                    map.put("role_id", Constant.PERSON);
                    if(!common.isSave(map)) {
                        common.showToast(context, "企业ID更新失败");
                    }
                    map.clear();
                    map.put(Constant.TRANSPORT_ROLE, Constant.DRIVER_ROLE_ID);
                    if(!fixedCommon.isSave(map)) {
                        common.showToast(context, "司机角色保存失败");
                    }
                    setResult(Constant.ACCOUNT_TYPE_RESULT_CODE, intent);
                    updateRole(txEnterpriseId);
                } else{
                    intent = new Intent(AccountTypeActivity.this, RoleTypeActivity.class);
                    intent.putExtra("account_type", txEnterpriseName);
                    intent.putExtra("enterprise_id", txEnterpriseId);
                    startActivityForResult(intent, 0x15);
                }
            }
        });
    }

    /**
     * 请求账户类型数据
     */
    private void requestAccountTypeList() {
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID); //获取当前sessionUUID
        String url = Constant.ENTRANCE_PREFIX_v1 + "appGetRelatedEnterpriseList.json?sessionUuid="+sessionUuid; //账户类型访问地址
        OkHttpClientManager.getAsyn(url, new AccountTypeResultCallback<String>() {

            @Override
            public void onError(Request request, Exception e) {
                common.showToast(context, "账户类型请求异常");
                transitionDialog.dismissDialog();
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                JSONObject item;
                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if (!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        //用户名和密码错误
                        common.showToast(context, "账户类型请求失败");
                        transitionDialog.dismissDialog();
                        return;
                    }
                    jsonArray = jsonObject.getJSONArray("rows");
                    if(jsonArray.length() == 0 ) {
                        common.showToast(context, "暂无数据");
                        return;
                    }
                    for (int i = 0; i < jsonArray.length(); i++) {
                        item = jsonArray.getJSONObject(i);
                        AccountTypeInfoBean accountTypeInfo = new AccountTypeInfoBean();
                        accountTypeInfo.setEnterpriseId(item.getString("enterpriseId"));
                        accountTypeInfo.setEnterpriseName(item.getString("enterpriseName"));
                        accountTypeInfoBeanList.add(accountTypeInfo);
                    }
                    Message message = Message.obtain();
                    message.what = HANDLER_ACCOUNT_TYPE_CODE;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * 更新角色
     * @param enterpriseId 企业ID
     */
    private void updateRole(String enterpriseId) {
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        String accountUrl = Constant.ENTRANCE_PREFIX + "Compare.json?sessionUuid="+sessionUuid+"&enterpriseId=" + enterpriseId;
        OkHttpClientManager.getAsyn(accountUrl, new AccountTypeResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                common.showToast(context, "更新角色异常");
                transitionDialog.dismissDialog();
            }
            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                JSONObject item;
                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if(!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        //角色类型请求失败
                        common.showToast(context, "更新角色失败");
                        transitionDialog.dismissDialog();
                        return;
                    }
                    jsonArray = jsonObject.getJSONArray("rows");
                    item = jsonArray.getJSONObject(0);
                    int roleID = item.getInt("role");
                    Map<Object, Object> map = new HashMap<Object, Object>();
                    map.put(getString(R.string.role_id), roleID);
                    //保存角色类型信息
                    if (!common.isSave(map)) {
                        common.showToast(context, "角色类型信息保存失败");
                        return;
                    }

                    Message message = Message.obtain();
                    message.what = HANDLER_UPDATE_ROLE_CODE;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_ACCOUNT_TYPE_CODE://账户类型返回成功
                    adapter = new AccountTypeAdapter(context);
                    accountTypeList.setAdapter(adapter);
                    bottomLine.setVisibility(View.VISIBLE);
                    break;
                case HANDLER_UPDATE_ROLE_CODE: //角色类型返回成功
                    finish();
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
        }
    }


    private class AccountTypeAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        public AccountTypeAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return accountTypeInfoBeanList.size();
        }

        @Override
        public Object getItem(int position) {
            return accountTypeInfoBeanList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = inflater.inflate(R.layout.common_list_item, null);
            AccountTypeInfoBean accountTypeInfo = accountTypeInfoBeanList.get(position);
            TextView textView = (TextView) view.findViewById(R.id.common_list_text);
            textView.setText(accountTypeInfo.getEnterpriseName());
            return view;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case 0x30: //已选角色，直接退出
                Intent intent = new Intent();
                setResult(Constant.ACCOUNT_TYPE_RESULT_CODE, intent);
                finish();
                break;
        }
    }

    private abstract class AccountTypeResultCallback<T> extends OkHttpClientManager.ResultCallback<T>{

        @Override
        public void onBefore() {
            transitionDialog = new CusProgressDialog(context, "正在加载...");
            transitionDialog.showDialog();
        }

        @Override
        public void onAfter() {
            transitionDialog.dismissDialog();
        }
    }

    /**
     * 企业实体
     */
    private class AccountTypeInfoBean {
        private String enterpriseId;
        private String enterpriseName;

        public String getEnterpriseId() {
            return enterpriseId;
        }

        public void setEnterpriseId(String enterpriseId) {
            this.enterpriseId = enterpriseId;
        }

        public String getEnterpriseName() {
            return enterpriseName;
        }

        public void setEnterpriseName(String enterpriseName) {
            this.enterpriseName = enterpriseName;
        }
    }
}
