package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.model.Role;
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
 * Created by yql on 2015/11/11.
 */
public class RoleTypeActivity extends Activity implements View.OnClickListener {

    private LinearLayout back; //返回
    private ListView roleList; //列表
    private CusProgressDialog cusProgressDialog;
    private List<Role> roleDataSet = new ArrayList<Role>();

    private String enterpriseID; //企业ID
    private String enterpriseName; //企业名称
    private Common common;
    private Map<Object, Object> roleMap;

    private int tPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.role_list);
        initFields();
        requestRoleList(); //请求数据

        roleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tPosition = position;
                updateRole(enterpriseID);
            }
        });
        back.setOnClickListener(this);
    }

    /**
     * 初始化数据
     */
    private void initFields() {
        back = (LinearLayout) findViewById(R.id.role_type_back);
        roleList = (ListView) findViewById(R.id.role_list);
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));

        Intent intent = getIntent();
        enterpriseID = intent.getStringExtra("enterpriseId");
        enterpriseName = intent.getStringExtra("account_type");
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1000: //数据返回成功设置数据
                    roleList.setAdapter(new RoleAdapter(RoleTypeActivity.this));
                    break;
                case 1002: //更新角色
                    updateRoleInfo();
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.role_type_back: //返回
                finish();
                break;
        }
    }

    /**
     * 请求角色列表
     */
    private void requestRoleList() {

        Common common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        //String enterpriseId = common.getStringByKey(Constant.ENTERPRISE_ID);
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);

        String url = Constant.ENTRANCE_PREFIX + "inquireRoleOflogin.json?sessionUuid=" + sessionUuid + "&enterpriseId=" + enterpriseID ;
        OkHttpClientManager.getAsyn(url, new RoleTypeResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                JSONObject roleTypeJson;
                JSONArray roleTypeArray;
                String str = response;
                try {
                    roleTypeJson = new JSONObject(response);
                    String status = roleTypeJson.getString("status");
                    if(!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(RoleTypeActivity.this, "角色类型获取异常", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    roleTypeArray = roleTypeJson.getJSONArray("rows");
                    JSONObject jsonObject;
                    for(int i = 0; i < roleTypeArray.length(); i++) {
                        jsonObject = roleTypeArray.getJSONObject(i);
                        Role role = new Role();
                        role.setOrgCode(jsonObject.getInt("orgCode"));
                        role.setRoleName(jsonObject.getString("roleName"));
                        roleDataSet.add(role);
                    }

                    Message message = new Message();
                    message.what = 1000;
                    handler.sendMessage(message);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public abstract class RoleTypeResultCallback<T> extends OkHttpClientManager.ResultCallback<T>{
        @Override
        public void onBefore() {
            //请求之前操作
            cusProgressDialog = new CusProgressDialog(RoleTypeActivity.this, "正在加载...");
            cusProgressDialog.getLoadingDialog().show();
        }

        @Override
        public void onAfter() {
            //请求之后要做的操作
            cusProgressDialog.getLoadingDialog().dismiss();
        }
    }

    /**
     * 自定义适配器
     */
    private class RoleAdapter extends BaseAdapter {

        private Context context;
        private LayoutInflater inflater;

        private RoleAdapter(Context context) {
            this.context = context;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return roleDataSet.size();
        }

        @Override
        public Object getItem(int position) {
            return roleDataSet.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TextView content; //车牌号

            View view = inflater.inflate(R.layout.common_list_item, null);
            content = (TextView) view.findViewById(R.id.common_list_text);
            content.setText(roleDataSet.get(position).getRoleName());
            return view;
        }
    }

    /**
     * 更新角色信息
     */
    private void updateRoleInfo() {
        Role role = roleDataSet.get(tPosition);
        int orgCode = role.getOrgCode();
        String roleName = role.getRoleName();
        roleMap = new HashMap<Object, Object>();
        roleMap.put(Constant.ORG_CODE, String.valueOf(orgCode));
        roleMap.put(Constant.ROLE_NAME, roleName);
        roleMap.put(Constant.ENTERPRISE_ID, enterpriseID);
        roleMap.put(Constant.ENTERPRISE_NAME, enterpriseName);
        if (!common.isSave(roleMap)) {
            //角色信息保存失败
            Toast.makeText(getApplicationContext(), "角色信息保存失败", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        RoleTypeActivity.this.setResult(0x30, intent);
        finish();
    }
    /**
     * 更新角色
     */
    private void updateRole(String enterpriseId) {
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
                    //保存角色类型信息
                    if (!common.isSave(roleTypeMap)) {
                        Toast.makeText(getApplicationContext(), getString(R.string.role_type_info_save_error), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Message message = new Message();
                    message.what = 1002;
                    handler.sendMessage(message);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
