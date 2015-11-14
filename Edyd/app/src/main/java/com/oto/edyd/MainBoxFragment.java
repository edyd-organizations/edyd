package com.oto.edyd;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by yql on 2015/8/26.
 */
public class MainBoxFragment extends Fragment implements View.OnClickListener {

    private View boxView;
    private ImageView surroundingOil; //周边加油
    private ImageView surroundingSparking; //周边停车场
    private ImageView violate_check; //违章查询
    private ImageView violatePayment; //违章缴费
    private ImageView vehicleNavigation; //车辆导航
    private ImageView phonePay; //手机充值


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        boxView = inflater.inflate(R.layout.main_box, null);
        initFields();

        violate_check.setOnClickListener(this);
        surroundingOil.setOnClickListener(this);
        surroundingSparking.setOnClickListener(this);
        vehicleNavigation.setOnClickListener(this);
        violatePayment.setOnClickListener(this);
        phonePay.setOnClickListener(this);
        return boxView;
    }

    /**
     * 初始化数据
     */
    private void initFields() {
        surroundingOil = (ImageView) boxView.findViewById(R.id.surrounding_oil);
        surroundingSparking = (ImageView) boxView.findViewById(R.id.surrounding_sparking);
        violate_check = (ImageView) boxView.findViewById(R.id.violate_check);
        violatePayment = (ImageView) boxView.findViewById(R.id.violate_payment);
        vehicleNavigation = (ImageView) boxView.findViewById(R.id.vehicle_navigation);
        phonePay = (ImageView) boxView.findViewById(R.id.phone_pay);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.surrounding_oil: //周边加油
                intent = new Intent(getActivity(),WaitBuild.class);
                intent.putExtra("wait_title", "周边加油");
                startActivity(intent);
                break;
            case R.id.surrounding_sparking: //周边停车
                intent = new Intent(getActivity(),WaitBuild.class);
                intent.putExtra("wait_title", "周边停车");
                startActivity(intent);
                break;
            case R.id.violate_check: //违章查询
                intent = new Intent(getActivity(),ViolateCheckActivity.class);
                startActivity(intent);
                break;
            case R.id.violate_payment: //违章缴费
                intent = new Intent(getActivity(),WaitBuild.class);
                intent.putExtra("wait_title", "违章缴费");
                startActivity(intent);
                break;
            case R.id.vehicle_navigation: //车辆导航
                intent = new Intent(getActivity(),WaitBuild.class);
                intent.putExtra("wait_title", "车辆导航");
                startActivity(intent);
                break;
            case R.id.phone_pay: //手机充值
                intent = new Intent(getActivity(),WaitBuild.class);
                intent.putExtra("wait_title", "手机充值");
                startActivity(intent);
                break;
        }
    }
}