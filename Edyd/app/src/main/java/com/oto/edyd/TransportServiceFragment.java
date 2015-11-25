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

import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;

/**
 * Created by yql on 2015/9/21.
 */
public class TransportServiceFragment extends Fragment implements View.OnClickListener{

    private View transportServiceView;
    public FragmentManager fragmentManager; //fragment管理器

    private RelativeLayout selectTransportRole; //选择运输服务角色
    public TextView enterpriseName; //用户名
    public TextView transportRole; //角色
    private LinearLayout distributeOrder; //派单
    private LinearLayout trackSearch; //轨迹查询
    private LinearLayout panorama; //全景图
    private LinearLayout driverInfo; //司机信息
    private ImageView transportReceiveOrder; //接单

    private Common common;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //transportServiceView = inflater.inflate(R.layout.transport_service_main, null);
        transportServiceView = inflater.inflate(R.layout.transport_undertake, null);
        initFields(transportServiceView);

        String txEnterpriseName = common.getStringByKey(Constant.ENTERPRISE_NAME);

        enterpriseName.setText(txEnterpriseName);
        int transportRoleId = Integer.valueOf(common.getStringByKey(Constant.TRANSPORT_ROLE));
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

        selectTransportRole.setOnClickListener(this);
        transportReceiveOrder.setOnClickListener(this);
        distributeOrder.setOnClickListener(this);
        return  transportServiceView;
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch(v.getId()) {
            case R.id.iv_receive_order: //接单
                break;
            case R.id.select_transport_role: //选择角色
                intent = new Intent(getActivity(), SelectTransportRole.class);
                startActivityForResult(intent, 0x10);
                break;
            case R.id.ll_distribute_order: //派单
                intent = new Intent(getActivity(), TransportOrderDispatchActivity.class);
                startActivity(intent);
                break;
        }
    }

    /**
     * 初始化数据
     */
    private void initFields(View view) {
        this.fragmentManager = ((MainActivity)getActivity()).fragmentManager;
        selectTransportRole = (RelativeLayout) view.findViewById(R.id.select_transport_role);
        enterpriseName = (TextView) view.findViewById(R.id.enterprise_name);
        transportRole = (TextView) view.findViewById(R.id.transport_role);
        distributeOrder = (LinearLayout) view.findViewById(R.id.ll_distribute_order);
        trackSearch = (LinearLayout) view.findViewById(R.id.ll_track_search);
        panorama = (LinearLayout) view.findViewById(R.id.ll_panorama);
        driverInfo = (LinearLayout) view.findViewById(R.id.ll_driver_info);
        transportReceiveOrder = (ImageView) view.findViewById(R.id.iv_receive_order);

        common = new Common(getActivity().getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
    }
}
