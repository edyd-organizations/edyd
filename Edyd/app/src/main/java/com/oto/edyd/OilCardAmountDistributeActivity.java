package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.oto.edyd.model.OilAmountDistribute;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yql on 2015/10/28.
 */
public class OilCardAmountDistributeActivity extends Activity implements View.OnClickListener{

    private LinearLayout back; //返回
    private EditText carNumber; //车牌号
    private Button search; //搜索
    private TextView amount; //总金额
    private TextView averageDistribute; //等额预分配
    private ListView listDistributeUser; //分配用户列表
    private TextView distributeCardNumber; //本次预分配卡数
    private TextView predictionDistributeAmount; //本次预分配金额
    private TextView clear; //清空
    private TextView submit; //提交

    private List<OilAmountDistribute> oilAmountDistributeList = new ArrayList<OilAmountDistribute>(); //列表数据

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oil_card_amount_distribute);
        initFields();;
        requestDistributeUserList(); //请求列表数据

        back.setOnClickListener(this);
        listDistributeUser.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EditText editText = (EditText) view.findViewById(R.id.prediction_distribute_amount);
                editText.requestFocus();
                editText.setFocusable(true);
               // editText.setFocusableInTouchMode(true);
            }
            public void noNothingSelected(AdapterView<?> parent) {
                listDistributeUser.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
            }


        });
    }


    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
        carNumber = (EditText) findViewById(R.id.type_car_number);
        search = (Button) findViewById(R.id.search);
        amount = (TextView) findViewById(R.id.amount);
        averageDistribute = (TextView) findViewById(R.id.average_distribute);
        listDistributeUser = (ListView) findViewById(R.id.list_distribute_user);
        distributeCardNumber = (TextView) findViewById(R.id.current_distribute_card_number);
        predictionDistributeAmount = (TextView) findViewById(R.id.current_prediction_distribute_amount);
        clear = (TextView) findViewById(R.id.clear);
        submit = (TextView) findViewById(R.id.submit);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: //返回
                finish();
                break;
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x12: //油卡金额数据返回执行
                    listDistributeUser.setAdapter(new OilCardAmountDistributeAdapter(getApplicationContext()));
                    break;
            }
        }
    };

    /**
     * 请求预分配用户列表
     */
    private void requestDistributeUserList() {

        //假数据
        for(int i = 0; i < 5; i++) {
            OilAmountDistribute oilAmountDistribute = new OilAmountDistribute();
            oilAmountDistribute.setCardUser("米诺"+i);
            oilAmountDistribute.setCardNumber("闽F324" + i);
            oilAmountDistribute.setCarNumber("535734673" + i);
            oilAmountDistributeList.add(oilAmountDistribute);
        }


        Message message = new Message();
        message.what = 0x12;
        handler.sendMessage(message);
    }

    /**
     * 设置操作数据
     */
    private void setOperationData() {

    }

    /**
     * 自定义适配器
     */
    private class OilCardAmountDistributeAdapter extends BaseAdapter{

        private Context context;
        private LayoutInflater inflater;

        private OilCardAmountDistributeAdapter(Context context) {
            this.context = context;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return oilAmountDistributeList.size();
        }

        @Override
        public Object getItem(int position) {
            return oilAmountDistributeList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TextView tCardUser; //卡用户
            TextView tCarNumber; //车牌号
            TextView tCardNumber; //卡号
            EditText tAmount; //金额

            View view = inflater.inflate(R.layout.distribute_user_item, null);
            tCardUser = (TextView) view.findViewById(R.id.distribute_name);
            tCarNumber = (TextView) view.findViewById(R.id.car_number);
            tCardNumber = (TextView) view.findViewById(R.id.card_id);
            tAmount = (EditText) view.findViewById(R.id.prediction_distribute_amount);

            OilAmountDistribute oilAmountDistribute = oilAmountDistributeList.get(position);
            tCardUser.setText(oilAmountDistribute.getCardUser());
            tCarNumber.setText(oilAmountDistribute.getCarNumber());
            tCardNumber.setText(oilAmountDistribute.getCardNumber());
            tAmount.setText(oilAmountDistribute.getAmount());

            return view;
        }
    }
}
