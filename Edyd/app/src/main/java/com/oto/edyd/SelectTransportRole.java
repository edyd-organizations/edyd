package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yql on 2015/11/23.
 */
public class SelectTransportRole extends Activity implements View.OnClickListener{

    private LinearLayout back; //返回
    private ListView roleList; //运输服务角色列表

    private String[] roleArray; //角色集合
    private Common globalCommon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_transport_role);
        initFields();
        roleList.setAdapter(new TransportRoleAdapter(SelectTransportRole.this));
        back.setOnClickListener(this);
        roleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<Object, Object> transportRoleMap = new HashMap<Object, Object>();
                switch (position) {
                    case 0: //司机
                        transportRoleMap.put(Constant.TRANSPORT_ROLE, 0);
                        //保存账户ID
                        if (!globalCommon.isSave(transportRoleMap)) {
                            Toast.makeText(getApplicationContext(), "运输服务角色保存异常", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        setResult(Constant.TRANSPORT_ROLE_CODE);
                        finish();
                        break;
                    case 1: //发货方
                        //transportRoleMap.put(Constant.TRANSPORT_ROLE, 1);
                        Toast.makeText(SelectTransportRole.this, "开发中...", Toast.LENGTH_SHORT).show();
                        break;
                    case 2: //收货方
                        //transportRoleMap.put(Constant.TRANSPORT_ROLE, 2);
                        Toast.makeText(SelectTransportRole.this, "开发中...", Toast.LENGTH_SHORT).show();
                        break;
                    case 3: //承运方
                        transportRoleMap.put(Constant.TRANSPORT_ROLE, 3);
                        //保存账户ID
                        if (!globalCommon.isSave(transportRoleMap)) {
                            Toast.makeText(getApplicationContext(), "运输服务角色保存异常", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        setResult(Constant.TRANSPORT_ROLE_CODE);
                        finish();
                        break;
                }
  /*              //保存账户ID
                if (!globalCommon.isSave(transportRoleMap)) {
                    Toast.makeText(getApplicationContext(), "运输服务角色保存异常", Toast.LENGTH_SHORT).show();
                    return;
                }
                setResult(Constant.TRANSPORT_ROLE_CODE);
                finish();*/
            }
        });
    }

    /**
     * 角色类型
     */
    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
        roleList = (ListView) findViewById(R.id.role_list);
        roleArray = getResources().getStringArray(R.array.transport_role_list);
        globalCommon = new Common(getSharedPreferences(Constant.GLOBAL_FILE, Context.MODE_PRIVATE));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: //返回
                finish();
                break;
        }
    }

    private class TransportRoleAdapter extends BaseAdapter{

        private LayoutInflater inflater;

        public TransportRoleAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return roleArray.length;
        }

        @Override
        public Object getItem(int position) {
            return roleArray[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = inflater.inflate(R.layout.common_list_single_text_item, null);
            TextView content;
            content = (TextView) view.findViewById(R.id.common_content);
            content.setText(roleArray[position]);
            return view;
        }
    }
}
