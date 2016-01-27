package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yql on 2015/9/9.
 */
public class AccountTypeActivity extends Activity implements View.OnClickListener{

    private LinearLayout accountTypeBack; //返回
    private ListView accountTypeList; //ListView
    private CusProgressDialog accountTypeLoadingDialog; //过度Dialog
    private TextView commonListTitle; //标题

    List<String> accountTypeData = new ArrayList<String>(); //ListView显示数据
    List<Integer> enterpriseIdData = new ArrayList<Integer>(); //企业ID
    List<Map<String, Object>> dataSets = new ArrayList<Map<String, Object>>();
    private int idResources[];
    private Common common;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.common_list);

        initFields(); //初始化数据
        requestAccountTypeData(); //请求账户类型数据


        accountTypeList.setOnItemClickListener(new AccountTypeListItemOnClickListener()); //Item监听
        accountTypeBack.setOnClickListener(this);
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            Intent intent;
            switch (msg.what) {
                case 1001://请求成功更新界面
                    //accountTypeList.setAdapter(new ArrayAdapter<String>(AccountTypeActivity.this, android.R.layout.simple_expandable_list_item_1, accountTypeData));
                    SimpleAdapter simpleAdapter = new SimpleAdapter(AccountTypeActivity.this, dataSets, R.layout.account_type_list_item,
                            new String[]{"account_type_id", "account_type_text", "common_list_arrow"}, idResources); //ListView适配器
                    accountTypeList.setAdapter(simpleAdapter);
                    accountTypeLoadingDialog.getLoadingDialog().dismiss();
                    break;
                case 0x21:
                    finish();
                    break;
            }
        }
    };

    /**
     * 初始化数据
     */
    private void initFields() {
        accountTypeBack = (LinearLayout) findViewById(R.id.back);
        accountTypeList = (ListView) findViewById(R.id.common_list);
        commonListTitle = (TextView) findViewById(R.id.common_list_title);
        idResources = new int[]{R.id.account_type_id, R.id.account_type_text, R.id.common_list_arrow};
        commonListTitle.setText("账户类型");
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: //退出按钮
                finish();
                break;
        }
    }

    /**
     * 监听ListView
     *
     */
    private class AccountTypeListItemOnClickListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent;
            TextView enterpriseId = (TextView)view.findViewById(R.id.account_type_id);
            TextView typeTextView = (TextView)view.findViewById(R.id.account_type_text);
            String textEnterpriseId = enterpriseId.getText().toString();
            String textEnterpriseName = typeTextView.getText().toString();

            if (position == 0) {
                intent = new Intent();
                intent.putExtra("account_type", textEnterpriseName);
                intent.putExtra("enterpriseId", textEnterpriseId);

                Common common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
                Map<Object, Object> map = new HashMap<Object, Object>();
                map.put(Constant.ENTERPRISE_ID, Integer.valueOf(textEnterpriseId));
                map.put(Constant.ENTERPRISE_NAME, textEnterpriseName);
                map.put(Constant.ORG_CODE, String.valueOf(""));
                map.put(Constant.ROLE_NAME, "");
                map.put("role_id", 3);
                if(!common.isSave(map)) {
                    Toast.makeText(AccountTypeActivity.this, "企业ID更新失败", Toast.LENGTH_SHORT).show();
                }
                Common fixedCommon = new Common(getSharedPreferences(Constant.FIXED_FILE, Context.MODE_PRIVATE));
                map.clear();
                map.put(Constant.TRANSPORT_ROLE, Constant.DRIVER_ROLE_ID);
                if(!fixedCommon.isSave(map)) {
                    Toast.makeText(AccountTypeActivity.this, "司机角色保存失败", Toast.LENGTH_SHORT).show();
                }
                AccountTypeActivity.this.setResult(Constant.ACCOUNT_TYPE_RESULT_CODE, intent);
                updateRole(textEnterpriseId);
            } else{
                intent = new Intent(AccountTypeActivity.this, RoleTypeActivity.class);
                intent.putExtra("account_type", textEnterpriseName);
                intent.putExtra("enterpriseId", textEnterpriseId);
                startActivityForResult(intent, 0x15);
            }
//            AccountTypeActivity.this.setResult(Constant.ACCOUNT_TYPE_RESULT_CODE, intent);

//            Map<Object, Object> map = new HashMap<Object, Object>();
//            map.put(Constant.ENTERPRISE_ID, Integer.valueOf(textEnterpriseId));
//            map.put(Constant.ENTERPRISE_NAME, textEnterpriseName);
//            if(!common.isSave(map)) {
//                Toast.makeText(AccountTypeActivity.this, "企业ID更新失败", Toast.LENGTH_SHORT).show();
//            }
            //updateRole(textEnterpriseId);
        }
    }

    /**
     * 请求数据
     */
    private void requestAccountTypeData() {

        //获取当前sessionUUID
        CustomSharedPreferences customSharedPreferences = new CustomSharedPreferences(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        String sessionUuid = customSharedPreferences.getPreferencesStringByKey(Constant.SESSION_UUID);

        String url = Constant.ENTRANCE_PREFIX+"selectEnterprise.json?sessionUuid="+sessionUuid; //账户类型访问地址

        NetWork netWork = new NetWork(AccountTypeActivity.this);
        if(!netWork.isHaveInternet()) {
            Toast.makeText(AccountTypeActivity.this, "无法请求网络", Toast.LENGTH_SHORT).show();
            return;
        }
        OkHttpClientManager.getAsyn(url, new AccountTypeResultCallback<String>() {

            JSONObject accountTypeJson;
            JSONArray accountTypeArray;

            @Override
            public void onError(Request request, Exception e) {
                accountTypeLoadingDialog.getLoadingDialog().dismiss();
                Toast.makeText(AccountTypeActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response) {
                Map<String, String> accountTypeMap = new HashMap<String, String>();
                try {
                    accountTypeJson = new JSONObject(response);
                    String status = accountTypeJson.getString("status");
                    if(!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        if(status.equals(Constant.NOT_SETTING_SESSION_ID)) {
                            Toast.makeText(AccountTypeActivity.this, "没有设置会话ID", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(AccountTypeActivity.this, "账户类型获取异常", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    accountTypeArray = accountTypeJson.getJSONArray("rows");
                    for(int i = 0; i < accountTypeArray.length(); i++) {
                        JSONObject objectJson = accountTypeArray.getJSONObject(i);
                        accountTypeData.add(objectJson.getString("enterpriseName"));
                        enterpriseIdData.add(objectJson.getInt("enterpriseId"));
                        //accountTypeMap.put("accountId", objectJson.getString("accountId"));
                        //accountTypeMap.put("enterpriseId", objectJson.getString("enterpriseId"));
                    }

                    for(int i = 0; i < accountTypeData.size(); i++) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("account_type_id", enterpriseIdData.get(i));
                        map.put("account_type_text", accountTypeData.get(i));
                        map.put("common_list_arrow", R.mipmap.right_arrow);
                        dataSets.add(map);
                    }

                    Message message = new Message();
                    message.what = 1001;
                    handler.sendMessage(message);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public abstract class AccountTypeResultCallback<T> extends OkHttpClientManager.ResultCallback<T>{
        @Override
        public void onBefore() {
            //请求之前操作
            accountTypeLoadingDialog = new CusProgressDialog(AccountTypeActivity.this, "正在加载...");
            accountTypeLoadingDialog.getLoadingDialog().show();
        }

        @Override
        public void onAfter() {
            //请求之后要做的操作
            accountTypeLoadingDialog.getLoadingDialog().dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case 0x30: //已选角色，直接退出
                Intent intent = new Intent();
                AccountTypeActivity.this.setResult(Constant.ACCOUNT_TYPE_RESULT_CODE, intent);
                finish();
                break;
        }
    }
    /**
     * 更新角色
     */
    private void updateRole(String enterpriseId) {
        Common common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        //获取角色用户
        String accountUrl = Constant.ENTRANCE_PREFIX + "Compare.json?sessionUuid="+common.getStringByKey(Constant.SESSION_UUID)+"&enterpriseId=" + enterpriseId;
        OkHttpClientManager.getAsyn(accountUrl, new OkHttpClientManager.ResultCallback<String>() {
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
                        //Toast.makeText(getApplicationContext(), "角色用户请求失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    roleArray = roleJSON.getJSONArray("rows");
                    JSONObject rowsJson = roleArray.getJSONObject(0);
                    int roleID = rowsJson.getInt("role");
                    Map<Object, Object> roleTypeMap = new HashMap<Object, Object>();
                    roleTypeMap.put(getString(R.string.role_id), roleID);
                    Common common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
                    //保存角色类型信息
                    if (!common.isSave(roleTypeMap)) {
                        Toast.makeText(getApplicationContext(), getString(R.string.role_type_info_save_error), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Message message = new Message();
                    message.what = 0x21;
                    handler.sendMessage(message);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
