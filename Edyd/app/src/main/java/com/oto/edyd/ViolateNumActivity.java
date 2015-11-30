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
        TextView te_money = (TextView) findViewById(R.id.te_money);//罚款
        te_time.setText(String.valueOf(infoList.size()));
        te_fen.setText("扣分"+String.valueOf(sum));
        te_money.setText("罚款"+String.valueOf(amount));
        TextView te_amount = (TextView) findViewById(R.id.te_amount);
        TextView tv_gongji = (TextView) findViewById(R.id.tv_gongji);
        tv_gongji.setText("已经"+String.valueOf(infoList.size())+"条罚款，罚款共计");
        te_amount.setText("￥"+String.valueOf(amount)+"元");

        Mybase mybase=new Mybase();
        mlistview.setAdapter(mybase);
    }
    class Mybase extends BaseAdapter{
        @Override
        public int getCount() {
            return infoList.size();
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
            TextView vio_act = (TextView) view.findViewById(R.id.vio_reason);//违章行为
            TextView vio_handled = (TextView) view.findViewById(R.id.tv_weichuli);//是否处理
            ViolationInfo violationInfo = infoList.get(position);
            vio_act.setText(violationInfo.getAct());
            vio_data.setText(violationInfo.getData());
            vio_fen.setText(violationInfo.getFen()+"分");
            vio_money.setText(violationInfo.getMoney()+"元");
            vio_area.setText(violationInfo.getArea());
            String handled = violationInfo.getHandled();
            if (handled.equals("0")){
                vio_handled.setText("未处理");
            }if (handled.equals("1")){
                vio_handled.setText("处理");
            }if(handled==null){
                vio_handled.setText("未知");
            }
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

