package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yql on 2015/11/9.
 */
public class SelectDepartmentActivity extends Activity implements View.OnClickListener{

    private LinearLayout back; //返回
    private ListView departmentList; //部门列表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_department);
        initFields();

      //  departmentList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1));
        departmentList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1,getData()));
        back.setOnClickListener(this);
    }

    /**
     * 初始化数据
     */
    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
        departmentList = (ListView) findViewById(R.id.department_list);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: //返回
                finish();
                break;
        }
    }

    /**
     * 请求部门列表数据
     */
    private void requestDepartmentListData() {

    }

    /**
     * 设置适配器数据
     */
    private List<String> getData() {
        List<String> departmentList = new ArrayList<String>();
        departmentList.add("开发部");
        departmentList.add("技术部");
        departmentList.add("财务部");
        departmentList.add("后勤部");
        return departmentList;
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
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return null;
        }
    }
}
