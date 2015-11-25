package com.oto.edyd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.oto.edyd.model.ViolationInfo;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/11/6.
 */
public class ViolateNumActivity extends Activity {
    ArrayList<ViolationInfo> infoList;//违章信息
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.violate_num_activity);
        Intent intent = getIntent();
        infoList = (ArrayList<ViolationInfo>) intent.getSerializableExtra("info");
        init();
        }

    private void init() {
        int sum=0;
        int amount=0;
        for (int i=0;i<infoList.size();i++){
            ViolationInfo violationInfo = infoList.get(i);
            String fen = violationInfo.getFen();
            String money = violationInfo.getMoney();
            sum += Integer.parseInt(fen);
            amount += Integer.parseInt(money);
        }
        ListView mlistview = (ListView) findViewById(R.id.listView2);
        TextView te_time = (TextView) findViewById(R.id.te_time);//显示未处理条数
        TextView te_fen = (TextView) findViewById(R.id.te_fen);//显示扣分
        TextView te_money = (TextView) findViewById(R.id.te_fen);//罚款
        te_time.setText(String.valueOf(infoList.size()));
        te_fen.setText("扣分"+String.valueOf(sum));
        te_money.setText("罚款"+String.valueOf(amount));
    }
    class Mybase extends BaseAdapter{
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
            View view = getLayoutInflater().inflate(R.layout.violate_item, null);
            TextView vio_data = (TextView) view.findViewById(R.id.vio_date);//违章时间
            TextView vio_fen = (TextView) view.findViewById(R.id.jiner);//违章分
            TextView vio_money = (TextView) view.findViewById(R.id.money);//违章罚款
            TextView vio_area = (TextView) view.findViewById(R.id.vio_area);//违章地点
            TextView vio_handled = (TextView) view.findViewById(R.id.tv_weichuli);//是否处理
            return view;
        }
    }
    public void toFinepage(View view){
        Intent intent=new Intent(this,FineActivity.class);
        startActivity(intent);

    }
    public void back(View view) {
        finish();
    }
}

