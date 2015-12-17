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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;

/**
 * Created by yql on 2015/12/1.
 */
public class TransportDriverFragment extends Fragment implements View.OnClickListener {

    private View transportDriverView;
    public FragmentManager fragmentManager; //fragment管理器

    private RelativeLayout selectTransportRole; //选择运输服务角色
    public TextView enterpriseName; //用户名
    public TextView transportRole; //角色
    private LinearLayout llWaitingExecuteOrders; //待执行订单
    private LinearLayout llHistoryOrders; //历史订单
    private LinearLayout llExecutingOrders; //执行中的订单
    //private LinearLayout llViewTrack; //查看轨迹
    //private ImageView ivReceiveOrder; //接单

    private Common common;
    private Common fixedCommon;
    private Context context;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View transportDriverView = inflater.inflate(R.layout.transport_driver, null);
        initFields(transportDriverView);
        switchTransportRole();

        selectTransportRole.setOnClickListener(this);
        llWaitingExecuteOrders.setOnClickListener(this);
        llHistoryOrders.setOnClickListener(this);
        llExecutingOrders.setOnClickListener(this);
        //llViewTrack.setOnClickListener(this);
        //ivReceiveOrder.setOnClickListener(this);
        return transportDriverView;
    }

    /**
     * 初始化数据
     */
    private void initFields(View view) {
        this.fragmentManager = ((MainActivity) getActivity()).fragmentManager;
        selectTransportRole = (RelativeLayout) view.findViewById(R.id.select_transport_role);
        enterpriseName = (TextView) view.findViewById(R.id.enterprise_name);
        transportRole = (TextView) view.findViewById(R.id.transport_role);
        llWaitingExecuteOrders = (LinearLayout) view.findViewById(R.id.ll_waiting_execute_orders);
        llHistoryOrders = (LinearLayout) view.findViewById(R.id.ll_history_orders);
        llExecutingOrders = (LinearLayout) view.findViewById(R.id.ll_executing_orders);
        //llViewTrack = (LinearLayout) view.findViewById(R.id.ll_view_track);
        //ivReceiveOrder = (ImageView) view.findViewById(R.id.iv_receive_order);

        fixedCommon = new Common(getActivity().getSharedPreferences(Constant.FIXED_FILE, Context.MODE_PRIVATE));
        common = new Common(getActivity().getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        context = getActivity();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.select_transport_role: //选择角色
                String enterpriseId = common.getStringByKey(Constant.ENTERPRISE_ID);
                if(enterpriseId.equals("0")) {
                    common.showToast(context, "个人不能切换角色");
                    return;
                } else {
                    intent = new Intent(getActivity(), SelectTransportRole.class);
                    startActivityForResult(intent, 0x10);
                }
                break;
//            case R.id.iv_receive_order: //接单
//                intent = new Intent(getActivity().getApplicationContext(), OrderOperateActivity.class);
//                startActivity(intent);
//                break;
            case R.id.ll_waiting_execute_orders: //待执行订单
                intent = new Intent(getActivity().getApplicationContext(), OrderOperateActivity.class);
                intent.putExtra("order",0);
                startActivity(intent);
                break;
            case R.id.ll_history_orders: //历史订单
                intent = new Intent(getActivity().getApplicationContext(), HistoryTransportOrderActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_executing_orders: //执行订单
                intent = new Intent(getActivity().getApplicationContext(), OrderOperateActivity.class);
                intent.putExtra("order",1);
                startActivity(intent);
                break;
//            case R.id.ll_view_track: //查看轨迹
//               Toast.makeText(getActivity().getApplicationContext(), "你没有权限", Toast.LENGTH_SHORT).show();
//                /*intent = new Intent(getActivity(), TrackListActivity.class);
//                String aspectType = fixedCommon.getStringByKey(Constant.TRANSPORT_ROLE);
//                intent.putExtra("aspectType", aspectType);
//                startActivity(intent);*/
//                break;
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
}
