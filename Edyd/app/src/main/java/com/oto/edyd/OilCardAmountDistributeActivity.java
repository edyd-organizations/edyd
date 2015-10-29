package com.oto.edyd;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oil_card_amount_distribute);
        initFields();;

        back.setOnClickListener(this);
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

    /**
     * 请求预分配用户列表
     */
    private void requestDistributeUserList() {
        String url = "";

    }

    /**
     * 自定义适配器
     */
    private class CusAdapter extends BaseAdapter{

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
