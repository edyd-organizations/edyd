package com.oto.edyd;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import com.oto.edyd.module.tts.activity.ShipperHistoryOrderActivity;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.OkHttpClientManager;
import com.oto.edyd.widget.BadgeView;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


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
    private LinearLayout ll_view_track;
    private ImageView iv_shipper_onway;
    private CusProgressDialog loadingDialog;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            dissmissProgressDialog();
            BadgeView badgeView = new BadgeView(getActivity(), iv_shipper_onway);

            int onwayNum = msg.what;
//            int onwayNum = 100;

            if (onwayNum > 99) {
                badgeView.setText("99+");
            } else {
                badgeView.setText(onwayNum+"");
            }
            badgeView.show();

        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.transport_shipper, null);
        init(view); //初始化数据
        return view;
    }

    /**
     * 初始化数据
     *
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
        this.fragmentManager = getActivity().getSupportFragmentManager();
        selectTransportRole = (RelativeLayout) view.findViewById(R.id.select_transport_role);
        enterpriseName = (TextView) view.findViewById(R.id.enterprise_name);
        transportRole = (TextView) view.findViewById(R.id.transport_role);
        LinearLayout panorama = (LinearLayout) view.findViewById(R.id.ll_panorama);
        panorama.setOnClickListener(this);//全景图
        llOnTheWayOrders = (LinearLayout) view.findViewById(R.id.ll_on_the_way_orders);
        content = getActivity();
        fixedCommon = new Common(getActivity().getSharedPreferences(Constant.FIXED_FILE, Context.MODE_PRIVATE));
        common = new Common(getActivity().getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        //发货方历史订单
        ll_history_orders = (LinearLayout) view.findViewById(R.id.ll_history_orders);//发货方历史订单
        ll_history_orders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ShipperHistoryOrderActivity.class);
                startActivity(intent);
            }
        });
        ll_view_track = (LinearLayout) view.findViewById(R.id.ll_view_track);
        ll_view_track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TrackListActivity.class);
                String aspectType = 2 + "";
                intent.putExtra("aspectType", aspectType);
                startActivity(intent);
            }
        });

        //显示在图订单的条数
        iv_shipper_onway = (ImageView) view.findViewById(R.id.iv_shipper_onway);
        requestNum();

    }

    /**
     * 显示在图订单的条数
     */
    private void requestNum() {

        String aspectType = Constant.SHIPPER_ROLE_ID + "";
        String orgCode = common.getStringByKey(Constant.ORG_CODE);
        String enterpriseId = common.getStringByKey(Constant.ENTERPRISE_ID);
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);

//  /v1.1/appSenderAndReceiverOrderCount.json?sessionUuid=&aspectType=1&enterpriseId=53&orgCode=1
        String url = Constant.ENTRANCE_PREFIX_v1 + "appSenderAndReceiverOrderCount.json?sessionUuid=" + sessionUuid +
                "&aspectType=" + aspectType + "&enterpriseId=" + enterpriseId + "&orgCode=" + orgCode;

        //第一次进来显示loading
        showProgressDialog();
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                dissmissProgressDialog();
                Toast.makeText(getActivity(), "获取信息异常", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    if (!jsonObject.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(getActivity(), "返回信息失败", Toast.LENGTH_SHORT).show();
                        dissmissProgressDialog();
                        return;
                    }
                    jsonArray = jsonObject.getJSONArray("rows");
                    int onwayNum = jsonArray.getInt(0);

                    Message msg = Message.obtain();
                    msg.what = onwayNum;
                    handler.sendMessage(msg);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * 显示进度框
     */

    private void showProgressDialog() {
        if (loadingDialog == null) {
            loadingDialog = new CusProgressDialog(getActivity(), "正在获取数据...");
        }
        loadingDialog.getLoadingDialog().show();

    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        loadingDialog.getLoadingDialog().dismiss();
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
            case R.id.ll_panorama://全景图
                String menterpriseId = common.getStringByKey(Constant.ENTERPRISE_ID);
                int enterpriseId = Integer.parseInt(menterpriseId);
                if (enterpriseId == 0) {
                    Toast.makeText(getActivity(), "您没有权限查看", Toast.LENGTH_SHORT).show();
                    return;
                }
                intent = new Intent(getActivity(), PanoramaActivity.class);
                intent.putExtra("aspectType", Constant.SHIPPER_ROLE_ID);
                startActivity(intent);
                break;
        }
    }
}
