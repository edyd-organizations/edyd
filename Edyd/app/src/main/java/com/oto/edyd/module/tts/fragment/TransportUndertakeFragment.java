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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.CarrierHisOrderActivity;
import com.oto.edyd.PanoramaActivity;
import com.oto.edyd.R;
import com.oto.edyd.ReceiveOrderActivity;
import com.oto.edyd.SelectTransportRole;
import com.oto.edyd.TrackListActivity;
import com.oto.edyd.TransportOrderDispatchActivity;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;

/**
 * 功能：运输服务-承运方主界面
 * 文件名：com.oto.edyd.TransportDriverFragment.java
 * 创建时间：2015/9/21
 * 作者：yql
 */
public class TransportUndertakeFragment extends Fragment implements View.OnClickListener{
    //---------------基本View控件---------------
    public TextView enterpriseName; //用户名
    public TextView transportRole; //角色
    private LinearLayout distributeOrder; //派单
    private LinearLayout trackSearch; //轨迹查询
    private LinearLayout panorama; //全景图
    private LinearLayout driverInfo; //司机信息
    private ImageView transportReceiveOrder; //接单
    //---------------变量---------------
    private Context context; //上下文对象
    public FragmentManager fragmentManager; //fragment管理器
    private Common fixedCommon;
    private Common common;
    //private RelativeLayout selectTransportRole; //选择运输服务角色

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.transport_undertake, null);
        init(view);
        return  view;
    }

    /**
     * 初始化数据
     * @param view
     */
    private void init(View view) {
        initFields(view);
        initListeners();
        switchTransportRole();
    }

    /**
     * 初始化字段
     */
    private void initFields(View view) {
        enterpriseName = (TextView) view.findViewById(R.id.enterprise_name);
        transportRole = (TextView) view.findViewById(R.id.transport_role);
        distributeOrder = (LinearLayout) view.findViewById(R.id.ll_distribute_order);
        trackSearch = (LinearLayout) view.findViewById(R.id.ll_track_search);
        panorama = (LinearLayout) view.findViewById(R.id.ll_panorama);
        driverInfo = (LinearLayout) view.findViewById(R.id.ll_driver_info);
        transportReceiveOrder = (ImageView) view.findViewById(R.id.iv_receive_order);
        context = getActivity();
        fragmentManager = getActivity().getSupportFragmentManager();
        fixedCommon = new Common(getActivity().getSharedPreferences(Constant.FIXED_FILE, Context.MODE_PRIVATE));
        common = new Common(getActivity().getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
    }

    /**
     * 初始化监听器
     */
    private void initListeners() {
        trackSearch.setOnClickListener(this);
        transportReceiveOrder.setOnClickListener(this);
        distributeOrder.setOnClickListener(this);
        panorama.setOnClickListener(this);
        driverInfo.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch(v.getId()) {
            case R.id.iv_receive_order: //接单
                intent = new Intent(getActivity(),ReceiveOrderActivity.class);
                startActivity(intent);
                break;
            case R.id.select_transport_role: //选择角色
                intent = new Intent(getActivity(), SelectTransportRole.class);
                startActivityForResult(intent, 0x10);
                break;
            case R.id.ll_distribute_order: //派单
                intent = new Intent(getActivity(), TransportOrderDispatchActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_panorama://全景图
                String menterpriseId = common.getStringByKey(Constant.ENTERPRISE_ID);
                int enterpriseId = Integer.parseInt(menterpriseId );
                if (enterpriseId==0){
                    Toast.makeText(getActivity(),"您没有权限查看",Toast.LENGTH_SHORT).show();
                    return;
                }
                intent = new Intent(getActivity(), PanoramaActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_driver_info: //历史订单
                intent=new Intent(getActivity(),CarrierHisOrderActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_track_search:
                String seId = common.getStringByKey(Constant.ENTERPRISE_ID);
                int enId = Integer.parseInt(seId );
                if (enId==0){
                    Toast.makeText(getActivity(),"您没有权限查看",Toast.LENGTH_SHORT).show();
                    return;
                }
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
