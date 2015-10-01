package com.oto.edyd;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Created by yql on 2015/9/21.
 */
public class TransportServiceFragment extends Fragment implements View.OnClickListener{

    private View transportServiceView;
    public FragmentManager fragmentManager; //fragment管理器

    private RelativeLayout transportReceiveOrder; //接单

//    private LinearLayout executingOrder; //执行中的订单
//    private Button btCurrentExecuteOrder; //当前执行订单
//    private RelativeLayout searchOrderByCondition; //按条件查询订单
//    private RelativeLayout transportOrder; //订单


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        transportServiceView = inflater.inflate(R.layout.transport_service_main, null);
        initFields(transportServiceView);

        transportReceiveOrder.setOnClickListener(this);
        return  transportServiceView;
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch(v.getId()) {
            case R.id.executing_order:
                break;
            case R.id.bt_current_executing_order:
                break;
            case R.id.search_order_by_condition:
                break;
            case R.id.transport_receive_order:
                intent = new Intent(getActivity(), OrderOperateActivity.class);
                startActivity(intent);
                break;
            case R.id.transport_order:
                break;
        }
    }

    /**
     * 初始化数据
     */
    private void initFields(View view) {
        this.fragmentManager = ((MainActivity)getActivity()).fragmentManager;
        transportReceiveOrder = (RelativeLayout) view.findViewById(R.id.transport_receive_order);

//        executingOrder = (LinearLayout) view.findViewById(R.id.executing_order);
//        btCurrentExecuteOrder = (Button) view.findViewById(R.id.bt_current_executing_order);
//        searchOrderByCondition = (RelativeLayout) view.findViewById(R.id.search_order_by_condition);
//        transportReceiveOrder = (RelativeLayout) view.findViewById(R.id.transport_receive_order);
//        transportOrder = (RelativeLayout) view.findViewById(R.id.transport_order);
    }
}
