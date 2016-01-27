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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
 * 功能：选择角色类型
 * 文件名：com.oto.edyd.module.usercenter.activity.RoleTypeActivity.java
 * 创建时间：2016/1/27
 * 作者：yql
 */
public class RoleTypeActivity extends Activity implements View.OnClickListener {
    //--------------基本View控件--------------
    private LinearLayout back; //返回
    private ListView roleTypeList; //ListView
    private CusProgressDialog transitionDialog; //过度Dialog
    private TextView title; //标题
    private View bottomLine; //listView底线
    //--------------变量--------------
    private Context context; //上下文对象
    private Common common; //偏好文件LOGIN_PREFERENCES_FILE
    private List<RoleTypeBean> roleTypeBeanList = new ArrayList<RoleTypeBean>();
    private String enterpriseID; //企业ID
    private String enterpriseName; //企业名称
    private RoleTypeAdapter adapter; //适配器
    private int curPosition; //点击item项位置
    private final static int HANDLER_ROLE_TYPE_CODE = 0x10; //账户类型返回码
    private final static int HANDLER_UPDATE_ROLE_CODE = 0x11; //角色类型返回码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_list);
        init();
    }

    /**
     * 初始化数据
     */
    private void init() {
        initFields();
        initListener();
        title.setText("选择角色类型");
        Intent intent = getIntent();
        enterpriseID = intent.getStringExtra("enterprise_id");
        enterpriseName = intent.getStringExtra("account_type");
        requestRoleTypeList();
    }

    /**
     * 初始化字段
     */
    private void initFields() {
        context = RoleTypeActivity.this;
        back = (LinearLayout) findViewById(R.id.back);
        roleTypeList = (ListView) findViewById(R.id.common_list);
        title = (TextView) findViewById(R.id.common_list_title);
        bottomLine = findViewById(R.id.common_bottom_line);
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        back.setOnClickListener(this);
        roleTypeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                curPosition = position;
                updateRole(enterpriseID);
            }
        });
    }

    /**
     * 请求角色列表
     */
    private void requestRoleTypeList() {
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);

        String url = Constant.ENTRANCE_PREFIX + "inquireRoleOflogin.json?sessionUuid=" + sessionUuid + "&enterpriseId=" + enterpriseID;
        OkHttpClientManager.getAsyn(url, new RoleTypeResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                common.showToast(context, "角色列表请求异常");
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
                        Toast.makeText(RoleTypeActivity.this, "角色类型获取异常", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    jsonArray = jsonObject.getJSONArray("rows");
                    if(jsonArray.length() == 0 ) {
                        common.showToast(context, "暂无数据");
                        return;
                    }
                    for (int i = 0; i < jsonArray.length(); i++) {
                        item = jsonArray.getJSONObject(i);
                        RoleTypeBean roleTypeBean = new RoleTypeBean();
                        roleTypeBean.setOrgCode(item.getString("orgCode"));
                        roleTypeBean.setRoleName(item.getString("roleName"));
                        roleTypeBeanList.add(roleTypeBean);
                    }

                    Message message = Message.obtain();
                    message.what = HANDLER_ROLE_TYPE_CODE;
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
        OkHttpClientManager.getAsyn(accountUrl, new RoleTypeResultCallback<String>() {
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

    /**
     * 更新角色信息
     */
    private void updateRoleInfo() {
        RoleTypeBean roleTypeBean = roleTypeBeanList.get(curPosition);
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put(Constant.ORG_CODE, roleTypeBean.getOrgCode());
        map.put(Constant.ROLE_NAME, roleTypeBean.getRoleName());
        map.put(Constant.ENTERPRISE_ID, enterpriseID);
        map.put(Constant.ENTERPRISE_NAME, enterpriseName);
        if (!common.isSave(map)) {
            //角色信息保存失败
            common.showToast(context, "角色更新信息保存失败");
            return;
        }
        Intent intent = new Intent();
        setResult(0x30, intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_ROLE_TYPE_CODE: //角色列表请求返回成功
                    adapter = new RoleTypeAdapter(context);
                    roleTypeList.setAdapter(adapter);
                    bottomLine.setVisibility(View.VISIBLE);
                    break;
                case HANDLER_UPDATE_ROLE_CODE: //更新角色返回成功
                    updateRoleInfo();
                    break;
            }
        }
    };

    private class RoleTypeAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        public RoleTypeAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return roleTypeBeanList.size();
        }

        @Override
        public Object getItem(int position) {
            return roleTypeBeanList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = inflater.inflate(R.layout.common_list_item, null);
            RoleTypeBean roleType = roleTypeBeanList.get(position);
            TextView textView = (TextView) view.findViewById(R.id.common_list_text);
            textView.setText(roleType.getRoleName());
            return view;
        }
    }

    private abstract class RoleTypeResultCallback<T> extends OkHttpClientManager.ResultCallback<T>{
        @Override
        public void onBefore() {
            //请求之前操作
            transitionDialog = new CusProgressDialog(RoleTypeActivity.this, "正在加载...");
            transitionDialog.showDialog();
        }

        @Override
        public void onAfter() {
            //请求之后要做的操作
            transitionDialog.dismissDialog();
        }
    }

    /**
     * 角色列表实体类
     */
    private class RoleTypeBean {
        private String orgCode; //组织代码
        private String roleName; //组织管理员

        public String getOrgCode() {
            return orgCode;
        }

        public void setOrgCode(String orgCode) {
            this.orgCode = orgCode;
        }

        public String getRoleName() {
            return roleName;
        }

        public void setRoleName(String roleName) {
            this.roleName = roleName;
        }
    }
}
