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

/**
 * Created by yql on 2015/12/1.
 */
public class TransportShipperFragment extends Fragment implements View.OnClickListener{

    private View transportDriverView;
    public FragmentManager fragmentManager; //fragment管理器

    private RelativeLayout selectTransportRole; //选择运输服务角色
    public TextView enterpriseName; //用户名
    public TextView transportRole; //角色
    private LinearLayout llOnTheWayOrders; //在途订单

    private Common common;
    private Common globalCommon;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View transportDriverView = inflater.inflate(R.layout.transport_shipper, null);
        initFields(transportDriverView);
        switchTransportRole();

        selectTransportRole.setOnClickListener(this);
        return transportDriverView;
    }

    /**
     * 初始化数据
     */
    private void initFields(View view) {
        this.fragmentManager = ((MainActivity)getActivity()).fragmentManager;
        selectTransportRole = (RelativeLayout) view.findViewById(R.id.select_transport_role);
        enterpriseName = (TextView) view.findViewById(R.id.enterprise_name);
        transportRole = (TextView) view.findViewById(R.id.transport_role);
        llOnTheWayOrders = (LinearLayout) view.findViewById(R.id.ll_on_the_way_orders);

        globalCommon = new Common(getActivity().getSharedPreferences(Constant.GLOBAL_FILE, Context.MODE_PRIVATE));
        common = new Common(getActivity().getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.select_transport_role: //选择角色
                intent = new Intent(getActivity(), SelectTransportRole.class);
                startActivityForResult(intent, 0x10);
                break;
        }
    }

    /**
     * 切换司机角色
     */
    private void switchTransportRole() {
        String txEnterpriseName = common.getStringByKey(Constant.ENTERPRISE_NAME);
        enterpriseName.setText(txEnterpriseName);
        String txTransportRole = globalCommon.getStringByKey(Constant.TRANSPORT_ROLE);
        if(txTransportRole!=null && !txTransportRole.equals("")) {
            int transportRoleId = Integer.valueOf(txTransportRole);
            switch (transportRoleId) {
                case 0: //司机
                    transportRole.setText("司机");
                    break;
                case 1: //发货方
                    transportRole.setText("发货方");
                    break;
                case 2: //收货方
                    transportRole.setText("收货方");
                    break;
                case 3: //承运方
                    transportRole.setText("承运方");
                    break;
            }
        }
    }

}
