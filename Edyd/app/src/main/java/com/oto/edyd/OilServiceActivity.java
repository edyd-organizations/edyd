package com.oto.edyd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.oto.edyd.pay.activity.ChinaMerchantsBankPayActivity;

/**
 * Created by yql on 2015/10/19.
 */
public class OilServiceActivity extends Activity implements View.OnClickListener {

    private LinearLayout back;
    private LinearLayout llMyOilCard; //我的油卡
    private LinearLayout llAmountDistribute; //金额分配
    private LinearLayout llDistributeDetail; //分配明细
    private LinearLayout llTransactionDetail; //交易明细
    private LinearLayout llOilCardApply; //油卡申请
    private LinearLayout llOilCardChange; //油卡变更
    private LinearLayout llOilCardPay; //油卡充值

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oil_service);
        initFields();

        back.setOnClickListener(this);
        llMyOilCard.setOnClickListener(this);
        llAmountDistribute.setOnClickListener(this);
        llDistributeDetail.setOnClickListener(this);
        llTransactionDetail.setOnClickListener(this);
        llOilCardApply.setOnClickListener(this);
        llOilCardChange.setOnClickListener(this);
        llOilCardPay.setOnClickListener(this);
    }

    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
        llMyOilCard = (LinearLayout) findViewById(R.id.ll_my_oil_card);
        llAmountDistribute = (LinearLayout) findViewById(R.id.ll_amount_distribute);
        llDistributeDetail = (LinearLayout) findViewById(R.id.ll_distribute_detail);
        llTransactionDetail = (LinearLayout) findViewById(R.id.ll_transaction_detail);
        llOilCardApply = (LinearLayout) findViewById(R.id.ll_oil_card_apply);
        llOilCardChange = (LinearLayout) findViewById(R.id.ll_oil_card_change);
        llOilCardPay = (LinearLayout) findViewById(R.id.ll_oil_card_pay);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.ll_my_oil_card: //我的油卡
                intent = new Intent(getApplicationContext(), AddOilActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_amount_distribute: //油卡金额分配
                intent = new Intent(getApplicationContext(), OilCardAmountDistributeActivity.class); //油卡金额分配
                startActivity(intent);
                break;
            case R.id.ll_transaction_detail: //交易明细
                intent = new Intent(getApplicationContext(), SelectCardActivity.class); //油卡金额分配
                startActivity(intent);
                break;
            case R.id.ll_distribute_detail: //分配明细
                intent = new Intent(getApplicationContext(), DistributionDetailedSearchActivity.class); //油卡金额分配
                startActivity(intent);
                break;
            case R.id.ll_oil_card_apply: //油卡申请
                intent = new Intent(getApplicationContext(), OilCardApplicationActivity.class); //油卡申请
                startActivity(intent);
                break;
            case R.id.ll_oil_card_change: //油卡变更
                intent = new Intent(getApplicationContext(), OilCardChangeActivity.class); //油卡申请
                startActivity(intent);
                break;
            case R.id.ll_oil_card_pay: //油卡充值
                intent = new Intent(getApplicationContext(), ChinaMerchantsBankPayActivity.class);
                startActivity(intent);
                break;
        }
    }
}
//intent = new Intent(getActivity(), OilCardApplicationActivity.class); //油卡申请
//intent = new Intent(getActivity(), OilCardAmountDistributeActivity.class); //油卡金额分配
//intent = new Intent(getActivity(), AddOilActivity.class); //油卡金额分配
