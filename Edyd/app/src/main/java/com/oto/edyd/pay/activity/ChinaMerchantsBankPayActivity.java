package com.oto.edyd.pay.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.oto.edyd.ConfirmPayActivity;
import com.oto.edyd.R;

/**
 * 功能：招商银行支付
 * 文件名：com.oto.edyd.ChinaMerchantsBankPayActivity.java
 * 创建时间：2015/12/14
 * 作者：yql
 */
public class ChinaMerchantsBankPayActivity extends Activity implements View.OnClickListener {

    private EditText etAccount; //金额
    private Button btPay; //支付按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.china_merchants_bank_pay);
        init(); //初始化数据
    }

    /**
     * 初始化数据
     */
    private void init() {
        initFields();
        initListener();
    }

    /**
     * 初始化字段
     */
    private void initFields() {
        etAccount = (EditText) findViewById(R.id.account);
        btPay = (Button) findViewById(R.id.pay);
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        btPay.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pay: //支付
                Intent intent = new Intent(ChinaMerchantsBankPayActivity.this, ConfirmPayActivity.class);
                startActivity(intent);
                break;
        }

    }
}
