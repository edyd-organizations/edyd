package com.oto.edyd;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;

import retrofit.http.HEAD;

/**
 * 功能：运输服务发货方主界面
 * 文件名：com.oto.edyd.TransportShipperFragment.java
 * 创建时间：2015/12/1
 * 作者：yql
 */
public class TransportShipperFragment extends Fragment implements View.OnClickListener {

    private RelativeLayout selectTransportRole; //选择运输服务角色
    public TextView enterpriseName; //用户名
    public TextView transportRole; //角色
    private LinearLayout llOnTheWayOrders; //在途订单
    private View view; //布局view
    public FragmentManager fragmentManager; //fragment管理器
    private Context content; //上下文对象
    private Common common;
    private Common fixedCommon;
    private LinearLayout ll_history_orders;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.transport_shipper, null);
        init(view); //初始化数据
        return view;
    }

    /**
     * 初始化数据
     * @param view
     */
    private void init(View view) {
        initFields(view); //初始化字段
        initListener(); //初始化监听器
        switchTransportRole(); //初始化运输角色
    }

    /**
     * 初始化字段
     */
    private void initFields(View view) {
        this.fragmentManager = ((MainActivity) getActivity()).fragmentManager;
        selectTransportRole = (RelativeLayout) view.findViewById(R.id.select_transport_role);
        enterpriseName = (TextView) view.findViewById(R.id.enterprise_name);
        transportRole = (TextView) view.findViewById(R.id.transport_role);
        llOnTheWayOrders = (LinearLayout) view.findViewById(R.id.ll_on_the_way_orders);
        content = getActivity();
        fixedCommon = new Common(getActivity().getSharedPreferences(Constant.FIXED_FILE, Context.MODE_PRIVATE));
        common = new Common(getActivity().getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));

        ll_history_orders = (LinearLayout) view.findViewById(R.id.ll_history_orders);//发货方历史订单
        ll_history_orders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(),ShipperHistoryOrderActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        selectTransportRole.setOnClickListener(this);
        llOnTheWayOrders.setOnClickListener(this);
    }

    /**
     * 切换运输角色
     */
    private void switchTransportRole() {
        String txEnterpriseName = common.getStringByKey(Constant.ENTERPRISE_NAME);
        enterpriseName.setText(txEnterpriseName);
        String txTransportRole = fixedCommon.getStringByKey(Constant.TRANSPORT_ROLE);
        if (txTransportRole != null && !txTransportRole.equals("")) {
            int transportRoleId = Integer.valueOf(txTransportRole);
            switch (transportRoleId) {
                case Constant.DRIVER_ROLE_ID: //司机
                    transportRole.setText("司机");
                    break;
                case Constant.SHIPPER_ROLE_ID: //发货方
                    transportRole.setText("发货方");
                    break;
                case Constant.RECEIVER_ROLE_ID: //收货方
                    transportRole.setText("收货方");
                    break;
                case Constant.UNDERTAKER_ROLE_ID: //承运方
                    transportRole.setText("承运方");
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.select_transport_role: //选择角色
                intent = new Intent(content, SelectTransportRole.class);
                startActivityForResult(intent, 0x10);
                break;
            case R.id.ll_on_the_way_orders: //在途订单
                intent = new Intent(content, ShipperOrderOperateActivity.class);
                startActivity(intent);
                break;
        }
    }
}
