package com.oto.edyd.module.common.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oto.edyd.R;
import com.oto.edyd.TransportShipperFragment;
import com.oto.edyd.module.tts.fragment.TransportDriverFragment;
import com.oto.edyd.module.tts.fragment.TransportUndertakeFragment;
import com.oto.edyd.utils.Constant;

/**
 * 功能：公共Activity
 * 文件名：com.oto.edyd.module.common.activity.ComTransportActivity.java
 * 创建时间：2016/1/18
 * 作者：yql
 */
public class ComTransportActivity extends FragmentActivity implements View.OnClickListener {
    //--------------基本View控件--------------
    private LinearLayout back; //返回
    private TextView title; //标题

    //-------------变量-----------------------
    private FragmentManager setUpFragmentManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); //无标题
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_transport);
        init(); //初始化数据
    }

    /**
     * 初始化数据
     */
    private void init() {
        initFields();
        initListener();
        Intent intent = getIntent();
        int transportRole = intent.getIntExtra(Constant.TRANSPORT_ROLE, -1);
        String txTitle = intent.getStringExtra("transport_title");
        title.setText(txTitle);
        switchTransportRole(transportRole); //切换运输角色
    }

    /**
     * 初始化字段
     */
    public void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
        title = (TextView) findViewById(R.id.title);
        setUpFragmentManager = getSupportFragmentManager();
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        back.setOnClickListener(this);
    }

    /**
     * 切换运输角色
     * @param transportRole
     */
    private void switchTransportRole(int transportRole) {
        switch (transportRole) {
            case Constant.DRIVER_ROLE_ID: //司机
                setUpFragmentManager.beginTransaction().replace(R.id.common_frame, new TransportDriverFragment()).commit();
                break;
            case Constant.UNDERTAKER_ROLE_ID: //承运方
                setUpFragmentManager.beginTransaction().replace(R.id.common_frame, new TransportUndertakeFragment()).commit();
                break;
            case Constant.SHIPPER_ROLE_ID: //发货方
                setUpFragmentManager.beginTransaction().replace(R.id.common_frame, new TransportShipperFragment()).commit();
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: //返回
                finish();
                break;
        }
    }
}
