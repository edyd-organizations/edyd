package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.model.SelectDepartment;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yql on 2015/11/9.
 */
public class SelectDepartmentActivity extends Activity implements View.OnClickListener{

    private LinearLayout back; //返回
    private ListView departmentList; //部门列表
    private Common common;
    private CusProgressDialog selectDepartmentDialog; //过度
    private TextView tvSearch; //搜索
    private EditText inputDepartment; //部门
    private List<SelectDepartment> departmentNameList = new ArrayList<SelectDepartment>();
    private DepartmentAdapter departmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_department);
        initFields();

        requestDepartmentListData(null);
        back.setOnClickListener(this);
        tvSearch.setOnClickListener(this);
        inputDepartment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString();
                if (content == null || content.equals("")) {
                    requestDepartmentListData(null);
                } else {
                    requestDepartmentListData(content);
                }
            }
        });

        departmentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable("department", departmentNameList.get(position));
                intent.putExtras(bundle);
                setResult(0x22, intent);
                SelectDepartmentActivity.this.finish();
            }
        });
    }

    /**
     * 初始化数据
     */
    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
        departmentList = (ListView) findViewById(R.id.department_list);
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        tvSearch = (TextView) findViewById(R.id.search);
        inputDepartment = (EditText) findViewById(R.id.type_car_number_or_card);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: //返回
                finish();
                break;
            case R.id.search: //搜索
                String department = inputDepartment.getText().toString();
                requestDepartmentListData(department);
                break;
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x14: //设置ListView数据
                    departmentAdapter = new DepartmentAdapter(getApplicationContext());
                    departmentList.setAdapter(departmentAdapter);
                    break;
                case 0x15: //通知listView更新
                    departmentAdapter.notifyDataSetChanged();
                    break;
                case 0x16: //暂无数据
                    departmentAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    /**
     * 请求部门列表数据
     */
    private void requestDepartmentListData(final String enterpriseName) {
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        String enterpriseID = common.getStringByKey(Constant.ENTERPRISE_ID);
        String url = "";
        if(enterpriseName == null || enterpriseName.equals("")) {
            url = Constant.ENTRANCE_PREFIX + "inquireOrganizationsList.json?"+"sessionUuid="+sessionUuid+"&enterpriseId=" + enterpriseID;
        } else {
            url = Constant.ENTRANCE_PREFIX + "inquireOrganizationsList.json?"+"sessionUuid="+sessionUuid+"&enterpriseId=" + enterpriseID +
                    "&enterpriseName=" + enterpriseName;
        }


        OkHttpClientManager.getAsyn(url, new SelectDepartmentResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                JSONObject departmentJSON;
                JSONArray departmentArray;
                JSONObject temp;
                try {
                    departmentJSON = new JSONObject(response);
                    departmentArray = departmentJSON.getJSONArray("rows");
                    Message message = new Message();

                    if(departmentArray.length() == 0) {
                        departmentNameList.clear();
                        message.what = 0x16;
                        handler.sendMessage(message);
                        //Toast.makeText(getApplicationContext(), "暂无数据", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    departmentNameList.clear();
                    for(int i = 0; i < departmentArray.length(); i++) {
                        SelectDepartment selectDepartment = new SelectDepartment();
                        temp = departmentArray.getJSONObject(i);
                        selectDepartment.setOrgId(temp.getString("id"));
                        selectDepartment.setOrgCode(temp.getString("orgCode"));
                        selectDepartment.setOrgType(temp.getString("orgType"));
                        selectDepartment.setTenantId(temp.getString("tenantId"));
                        selectDepartment.setText(temp.getString("text"));
                        departmentNameList.add(selectDepartment);
                    }

                    if(enterpriseName == null) {
                        message.what = 0x14;
                    } else {
                        message.what = 0x15;
                    }
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        }

    /**
     * 自定义适配器
     */
    private class DepartmentAdapter extends BaseAdapter {

        private Context context;
        private LayoutInflater inflater;

        private DepartmentAdapter(Context context) {
            this.context = context;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return departmentNameList.size();
        }

        @Override
        public Object getItem(int position) {
            return departmentNameList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tvText;

            View view = inflater.inflate(R.layout.common_list_single_text_item, null);
            tvText = (TextView) view.findViewById(R.id.common_content);

            tvText.setText(departmentNameList.get(position).getText());
            return view;
        }
    }

    public abstract class SelectDepartmentResultCallback<T> extends OkHttpClientManager.ResultCallback<T>{
        @Override
        public void onBefore() {
            //请求之前操作
           // selectDepartmentDialog = new CusProgressDialog(SelectDepartmentActivity.this, "正在加载...");
           // selectDepartmentDialog.getLoadingDialog().show();
        }

        @Override
        public void onAfter() {
            //请求之后要做的操作
            //selectDepartmentDialog.getLoadingDialog().dismiss();
        }
    }
}
