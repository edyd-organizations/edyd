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

import com.oto.edyd.model.AddOilCard;
import com.oto.edyd.model.OilAmountDistribute;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yql on 2015/11/2.
 */
public class AddOilActivity extends Activity implements View.OnClickListener {

    private LinearLayout back; //返回
    private TextView accountBalance; //账户余额
    private TextView totalNumber; //总卡数
    private ListView cardNumberList; //卡数列表
    private TextView apply; //申请
    private TextView accountDistribute; //金额分配

    private TextView tOilCardApply; //油卡申请
    private TextView tAmountDistribute; //金额分配

    private List<AddOilCard> addOilCards = new ArrayList<AddOilCard>(); //我的加油卡数量

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_add_oil);
        initFields();

        requestAddOilCardList(); //请求订单数据
        back.setOnClickListener(this);
        tOilCardApply.setOnClickListener(this);
        tAmountDistribute.setOnClickListener(this);

        cardNumberList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), OilCardAddDetailActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 初始化数据
     */
    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
        accountBalance = (TextView) findViewById(R.id.oil_card_account_balance);
        totalNumber = (TextView) findViewById(R.id.oil_card_total_number);
        cardNumberList = (ListView) findViewById(R.id.card_number_list);
        apply = (TextView) findViewById(R.id.oil_card_apply);
        accountDistribute = (TextView) findViewById(R.id.oil_card_account_distribute);
        tOilCardApply = (TextView) findViewById(R.id.oil_card_apply);
        tAmountDistribute = (TextView) findViewById(R.id.oil_card_account_distribute);
    }

    /**
     * 捕获点击事件，并处理
     * @param v
     */
    @Override
    public void onClick(View v) {
        Intent intent;

        switch (v.getId()) {
            case R.id.back: //返回
                finish();
                break;
            case R.id.oil_card_apply: //油卡申请
                intent = new Intent(getApplicationContext(), OilCardApplicationActivity.class); //油卡金额分配
                startActivity(intent);
                break;
            case R.id.oil_card_account_distribute: //金额分配
                intent = new Intent(getApplicationContext(), OilCardAmountDistributeActivity.class); //油卡金额分配
                startActivity(intent);
                break;

        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x12: //油卡金额数据返回执行
                    cardNumberList.setAdapter(new AddOilAdapter(getApplicationContext()));
                    break;
            }
        }
    };

    /**
     * 请求我的加油卡列表
     */
    private void requestAddOilCardList() {
        //假数据
        for(int i = 0; i < 100; i++) {
            AddOilCard addOilCard = new AddOilCard();
            addOilCard.setCarNumber("闽F" + i);
            addOilCard.setCardNumber("38683958" + i);
            addOilCard.setTime("2015-11-2 11:10:50");
            addOilCard.setBalance("43"+i);
            addOilCards.add(addOilCard);
        }

        Message message = new Message();
        message.what = 0x12;
        handler.sendMessage(message);
    }

    /**
     * 自定义适配器
     */
    private class AddOilAdapter extends BaseAdapter {

        private Context context;
        private LayoutInflater inflater;

        private AddOilAdapter(Context context) {
            this.context = context;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return addOilCards.size();
        }

        @Override
        public Object getItem(int position) {
            return addOilCards.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;

            if(convertView == null) {
                viewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.my_add_oil_item, null);
                viewHolder.carNumber = (TextView) convertView.findViewById(R.id.car_number);
                viewHolder.time = (TextView) convertView.findViewById(R.id.time);
                viewHolder.cardNumber = (TextView) convertView.findViewById(R.id.card_number);
                viewHolder.balance = (TextView) convertView.findViewById(R.id.balance);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            AddOilCard addOilCard = addOilCards.get(position);

            viewHolder.carNumber.setText(addOilCard.getCarNumber());
            viewHolder.time.setText(addOilCard.getTime());
            viewHolder.cardNumber.setText(addOilCard.getCardNumber());
            viewHolder.balance.setText(addOilCard.getBalance());

            return convertView;
        }
    }

    /**
     * ListView Item项对应数据
     */
    static class ViewHolder {
        TextView carNumber; //车牌号
        TextView time; //时间
        TextView cardNumber; //车牌号
        TextView balance; //金额
    }

}
