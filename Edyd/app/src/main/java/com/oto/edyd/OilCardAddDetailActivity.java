package com.oto.edyd;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oto.edyd.model.OilCardInfo;

/**
 * Created by yql on 2015/11/5.
 */
public class OilCardAddDetailActivity extends Activity implements View.OnClickListener {

    private LinearLayout back; //返回
    private TextView cardNumber; //卡号
    private TextView carNumber; //车牌号
    private TextView balance; //余额
    private TextView oilBindTime; //油卡绑定时间
    private TextView spareMoney; //备付金余额

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_oil_card_detail);
        initFields();

        Bundle bundle = getIntent().getExtras();
        OilCardInfo oilCardInfo = (OilCardInfo) bundle.get("oil_card_info");
        cardNumber.setText(oilCardInfo.getCardId());
        carNumber.setText(oilCardInfo.getCarId());
        balance.setText(oilCardInfo.getCardBalance());
        oilBindTime.setText(oilCardInfo.getTime());
        spareMoney.setText(oilCardInfo.getSpareMoney());
        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: //返回
                finish();
                break;
        }
    }

    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
        cardNumber = (TextView) findViewById(R.id.card_number);
        carNumber = (TextView) findViewById(R.id.car_number);
        balance = (TextView) findViewById(R.id.balance);
        oilBindTime = (TextView) findViewById(R.id.oil_bind_time);
        spareMoney = (TextView) findViewById(R.id.spare_money);
    }
}
