package com.oto.edyd.module.usercenter.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.R;
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
 * 功能：账户类型列表
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


    //--------------变量--------------
    private Context context; //上下文对象
    private Common common; //共享对象
    private AccountTypeAdapter accountTypeAdapter; //适配器
    private List<AccountTypeInfoBean> accountTypeInfoBeanList = new ArrayList<AccountTypeInfoBean>(); //账户类型list
    private final static int HANDLER_ACCOUNT_TYPE_CODE = 0x10; //账户类型返回

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
        requestAccountTypeData();
        title.setText("账户类型");
    }

    /**
     * 初始化字段
     */
    private void initFields() {
        context = AccountTypeActivity.this;
        back = (LinearLayout) findViewById(R.id.back);
        accountTypeList = (ListView) findViewById(R.id.common_list);
        title = (TextView) findViewById(R.id.common_list_title);
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        back.setOnClickListener(this);
    }

    /**
     * 请求账户类型数据
     */
    private void requestAccountTypeData() {
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
                    for(int i = 0; i < jsonArray.length(); i++) {
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

    public abstract class AccountTypeResultCallback<T> extends OkHttpClientManager.ResultCallback<T>{
        @Override
        public void onBefore() {
            //请求之前操作
            transitionDialog = new CusProgressDialog(AccountTypeActivity.this, "正在加载...");
            transitionDialog.showDialog();
        }

        @Override
        public void onAfter() {
            //请求之后要做的操作
            transitionDialog.dismissDialog();
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_ACCOUNT_TYPE_CODE://账户类型返回成功
                    accountTypeAdapter = new AccountTypeAdapter(context);
                    accountTypeList.setAdapter(accountTypeAdapter);
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
