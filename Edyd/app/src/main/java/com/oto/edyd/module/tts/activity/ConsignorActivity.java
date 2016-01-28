package com.oto.edyd.module.tts.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.oto.edyd.R;
import com.oto.edyd.TransportReceiverFragment;
import com.oto.edyd.TransportShipperFragment;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;

/**
 * 运输服务我是货主
 * Created by lbz on 2016/1/27.
 */
public class ConsignorActivity extends FragmentActivity implements View.OnClickListener {
    //-------------------基本View控件—————————————
    private TextView enterprise_name;//公司名称
    private RadioButton rb_shipper;//发货方切换按钮
    private RadioButton rb_receiver;//收货方切换按钮
    private TransportShipperFragment shipperFragment;
    private TransportReceiverFragment receiverFragment;
    private TextView tv_transport_role;//显示发货方收货方

    //-------------------变量-----------------------------
    private ConsignorActivity mActivity;
    private Common fixedCommon;
    private Common common;
    private FragmentManager fragmentManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consignor_activity);
        initField();
        initView();
        initShipperFragment();
    }

    private void initShipperFragment() {
        rb_shipper.setTextColor(Color.WHITE);
        shipperFragment = new TransportShipperFragment();
        fragmentManager.beginTransaction().replace(R.id.fl_owner_contain, shipperFragment).commit();
    }

    private void initView() {
        enterprise_name = (TextView) findViewById(R.id.enterprise_name);
        String txEnterpriseName = common.getStringByKey(Constant.ENTERPRISE_NAME);
        enterprise_name.setText(txEnterpriseName);
        tv_transport_role=(TextView)findViewById(R.id.tv_transport_role);

        rb_shipper = (RadioButton) findViewById(R.id.rb_shipper);
        rb_shipper.setOnClickListener(this);
        rb_receiver = (RadioButton) findViewById(R.id.rb_receiver);
        rb_receiver.setOnClickListener(this);
    }

    private void initField() {
        fragmentManager = getSupportFragmentManager();
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        fixedCommon = new Common(getSharedPreferences(Constant.FIXED_FILE, Context.MODE_PRIVATE));
        mActivity = this;
    }

    public void back(View view) {
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rb_shipper:
                switchFooterMenu(1); //切换发货方
                break;
            case R.id.rb_receiver:
                switchFooterMenu(2); //切换收货方
                break;
        }
    }

    private void switchFooterMenu(int index) {
        switch (index) {
            case 1: //发货方
                tv_transport_role.setText("发货方");
                rb_shipper.setTextColor(Color.WHITE);
                rb_receiver.setTextColor(ContextCompat.getColor(ConsignorActivity.this, R.color.user_icon_8));
                if (shipperFragment == null) {
                    //未缓存，创建新对象
                    shipperFragment = new TransportShipperFragment();
                }
                fragmentManager.beginTransaction().replace(R.id.fl_owner_contain, shipperFragment).commitAllowingStateLoss();
                break;
            case 2: //收货方
                //判断首页是否已经缓存
                tv_transport_role.setText("收货方");
                rb_receiver.setTextColor(Color.WHITE);
                rb_shipper.setTextColor(ContextCompat.getColor(ConsignorActivity.this, R.color.user_icon_8));
                if (receiverFragment == null) {
                    //未缓存，创建新对象
                    receiverFragment = new TransportReceiverFragment();
                }
                fragmentManager.beginTransaction().replace(R.id.fl_owner_contain, receiverFragment).commitAllowingStateLoss();
                break;
        }
    }
}
