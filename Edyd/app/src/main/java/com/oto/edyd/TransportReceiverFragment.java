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

/** 收货方主界面
 * Created by yql on 2015/12/1.
 */
public class TransportReceiverFragment extends Fragment implements View.OnClickListener{

    private View transportDriverView;
    public FragmentManager fragmentManager; //fragment管理器

    private RelativeLayout selectTransportRole; //选择运输服务角色
    public TextView enterpriseName; //用户名
    public TextView transportRole; //角色

    private Common common;
    private Common fixedCommon;
    private LinearLayout ll_view_track;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View transportDriverView = inflater.inflate(R.layout.transport_receiver, null);
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
        LinearLayout panorama = (LinearLayout) view.findViewById(R.id.ll_panorama);
        panorama.setOnClickListener(this);//全景图
        //查看轨迹
        ll_view_track = (LinearLayout) view.findViewById(R.id.ll_view_track);
        ll_view_track.setOnClickListener(this);

        LinearLayout ll_historyOrders = (LinearLayout) view.findViewById(R.id.ll_history_orders);
        LinearLayout ll_on_the_wayOrders = (LinearLayout) view.findViewById(R.id.ll_on_the_way_orders);
        ll_historyOrders.setOnClickListener(this);
        ll_on_the_wayOrders.setOnClickListener(this);
        fixedCommon = new Common(getActivity().getSharedPreferences(Constant.FIXED_FILE, Context.MODE_PRIVATE));
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
            case R.id.ll_history_orders://历史订单
                intent=new Intent(getActivity(),ReceivingOrderOperate.class);
                startActivity(intent);
                break;
            case  R.id.ll_on_the_way_orders://在途订单
                intent=new Intent(getActivity(),ReceiveTransitOrderActivity.class);
                startActivity(intent);
                break;
            case  R.id.ll_panorama://全景图
                intent=new Intent(getActivity(),PanoramaActivity.class);
                startActivity(intent);
                break;
            case  R.id.ll_view_track://查看轨迹
                intent = new Intent(getActivity(), TrackListActivity.class);
                String aspectType = fixedCommon.getStringByKey(Constant.TRANSPORT_ROLE);
                intent.putExtra("aspectType", aspectType);
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
        if(txTransportRole!=null && !txTransportRole.equals("")) {
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
