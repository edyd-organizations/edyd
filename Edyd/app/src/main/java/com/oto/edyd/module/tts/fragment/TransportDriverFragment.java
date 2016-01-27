package com.oto.edyd.module.tts.fragment;

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

import com.oto.edyd.R;
import com.oto.edyd.SelectTransportRole;
import com.oto.edyd.module.tts.activity.DriverExecutingOrderActivity;
import com.oto.edyd.module.tts.activity.DriverHistoryOrderActivity;
import com.oto.edyd.module.tts.activity.DriverWaitExecuteOrderActivity;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;

/**
 * 功能：运输服务-司机主界面
 * 文件名：com.oto.edyd.TransportDriverFragment.java
 * 创建时间：2015/12/01
 * 作者：yql
 */
public class TransportDriverFragment extends Fragment implements View.OnClickListener {
    //------------基本View控件---------------
    public TextView enterpriseName; //用户名
    public TextView transportRole; //角色
    private LinearLayout llWaitingExecuteOrders; //待执行订单
    private LinearLayout llHistoryOrders; //历史订单
    private LinearLayout llExecutingOrders; //执行中的订单
    //------------变量---------------
    public FragmentManager fragmentManager; //fragment管理器
    private Common common;
    private Common fixedCommon;
    private Context context;
    //private RelativeLayout selectTransportRole; //选择运输服务角色

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.transport_driver, null);
        init(view);
        return view;
    }

    /**
     * 初始化数据
     * @param view
     */
    private void init(View view) {
        initFields(view); //初始化字段
        initListener(); //初始化监听器
        switchTransportRole();
    }

    /**
     * 初始化字段
     */
    private void initFields(View view) {
        context = getActivity();
        this.fragmentManager = getActivity().getSupportFragmentManager();
        enterpriseName = (TextView) view.findViewById(R.id.enterprise_name);
//        transportRole = (TextView) view.findViewById(R.id.transport_role);
        llWaitingExecuteOrders = (LinearLayout) view.findViewById(R.id.ll_waiting_execute_orders);
        llHistoryOrders = (LinearLayout) view.findViewById(R.id.ll_history_orders);
        llExecutingOrders = (LinearLayout) view.findViewById(R.id.ll_executing_orders);
        //selectTransportRole = (RelativeLayout) view.findViewById(R.id.select_transport_role);
        fixedCommon = new Common(getActivity().getSharedPreferences(Constant.FIXED_FILE, Context.MODE_PRIVATE));
        common = new Common(getActivity().getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        llWaitingExecuteOrders.setOnClickListener(this);
        llHistoryOrders.setOnClickListener(this);
        llExecutingOrders.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
//            case R.id.select_transport_role: //选择角色
//                String enterpriseId = common.getStringByKey(Constant.ENTERPRISE_ID);
//                if(enterpriseId.equals("0")) {
//                    common.showToast(context, "个人不能切换角色");
//                    return;
//                } else {
//                    intent = new Intent(getActivity(), SelectTransportRole.class);
//                    startActivityForResult(intent, 0x10);
//                }
//                break;
            case R.id.ll_waiting_execute_orders: //待执行订单
                intent = new Intent(context, DriverWaitExecuteOrderActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_history_orders: //历史订单
                intent = new Intent(context, DriverHistoryOrderActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_executing_orders: //执行订单
                intent = new Intent(context, DriverExecutingOrderActivity.class);
                startActivity(intent);
                break;
        }
    }

    /**
     * 切换司机角色
     */
    private void switchTransportRole() {
        String txEnterpriseName = common.getStringByKey(Constant.ENTERPRISE_NAME);
        enterpriseName.setText(txEnterpriseName);
        String txTransportRole = fixedCommon.getStringByKey(Constant.TRANSPORT_ROLE);
        if (txTransportRole != null && !txTransportRole.equals("")) {
            int transportRoleId = Integer.valueOf(txTransportRole);
//            switch (transportRoleId) {
//                case Constant.DRIVER_ROLE_ID: //司机
//                    transportRole.setText("司机");
//                    break;
//                case Constant.SHIPPER_ROLE_ID: //发货方
//                    transportRole.setText("发货方");
//                    break;
//                case Constant.RECEIVER_ROLE_ID: //收货方
//                    transportRole.setText("收货方");
//                    break;
//                case Constant.UNDERTAKER_ROLE_ID: //承运方
//                    transportRole.setText("承运方");
//                    break;
//            }
        }
    }
}
