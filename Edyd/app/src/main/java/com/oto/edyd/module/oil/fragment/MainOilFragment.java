package com.oto.edyd.module.oil.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.oto.edyd.DistributionDetailedSearchActivity;
import com.oto.edyd.OilCardAmountDistributeActivity;
import com.oto.edyd.OilCardChangeActivity;
import com.oto.edyd.R;
import com.oto.edyd.SelectCardActivity;
import com.oto.edyd.lib.imageindicator.network.NetworkImageIndicatorView;
import com.oto.edyd.module.oil.activity.OilCardApplyActivity;
import com.oto.edyd.module.oil.activity.OilCardPayMainActivity;
import com.oto.edyd.module.oil.activity.OilFillCardActivity;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;

/**
 * 功能：油卡主界面
 * 文件名：com.oto.edyd.module.oil.fragment.OilMainActivity.java
 * 创建时间：2016/1/18
 * 作者：yql
 */
public class MainOilFragment extends Fragment implements View.OnClickListener {
    //--------------基本View控件-----------------
    private LinearLayout myOilCard; //我的油卡
    private LinearLayout amountDistribute; //金额分配
    private LinearLayout distributeDetail; //分配明细
    private LinearLayout transactionDetail; //交易明细
    private LinearLayout oilCardApply; //油卡申请
    private LinearLayout oilCardChange; //油卡变更
    private LinearLayout oilCardPay; //油卡充值
    //--------------变量-----------------
    private Context context;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.oil_service, null);
        init(view);
        return view;
    }

    /**
     * 初始化数据
     * @param view 当前view对象
     */
    private void init(View view) {
        initFields(view); //初始化字段
        initListener(); //初始化监听器
    }

    /**
     * 初始化字段
     */
    private void initFields(View view) {
        myOilCard = (LinearLayout) view.findViewById(R.id.ll_my_oil_card);
        amountDistribute = (LinearLayout) view.findViewById(R.id.ll_amount_distribute);
        distributeDetail = (LinearLayout) view.findViewById(R.id.ll_distribute_detail);
        transactionDetail = (LinearLayout) view.findViewById(R.id.ll_transaction_detail);
        oilCardApply = (LinearLayout) view.findViewById(R.id.ll_oil_card_apply);
        oilCardChange = (LinearLayout) view.findViewById(R.id.ll_oil_card_change);
        oilCardPay = (LinearLayout) view.findViewById(R.id.ll_oil_card_pay);
        context = getActivity();
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        myOilCard.setOnClickListener(this);
        amountDistribute.setOnClickListener(this);
        distributeDetail.setOnClickListener(this);
        transactionDetail.setOnClickListener(this);
        oilCardApply.setOnClickListener(this);
        oilCardChange.setOnClickListener(this);
        oilCardPay.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.ll_my_oil_card: //我的油卡
                intent = new Intent(context, OilFillCardActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_amount_distribute: //油卡金额分配
                intent = new Intent(context, OilCardAmountDistributeActivity.class); //油卡金额分配
                startActivity(intent);
                break;
            case R.id.ll_transaction_detail: //交易明细
                intent = new Intent(context, SelectCardActivity.class); //油卡金额分配
                startActivity(intent);
                break;
            case R.id.ll_distribute_detail: //分配明细
                intent = new Intent(context, DistributionDetailedSearchActivity.class); //油卡金额分配
                startActivity(intent);
                break;
            case R.id.ll_oil_card_apply: //油卡申请
                intent = new Intent(context, OilCardApplyActivity.class); //油卡申请
                startActivity(intent);
                break;
            case R.id.ll_oil_card_change: //油卡变更
                intent = new Intent(context, OilCardChangeActivity.class); //油卡申请
                startActivity(intent);
                break;
            case R.id.ll_oil_card_pay: //油卡充值
                intent = new Intent(context, OilCardPayMainActivity.class);
                startActivity(intent);
                break;
        }
    }
}
