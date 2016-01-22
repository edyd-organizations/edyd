package com.oto.edyd;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.lib.imageindicator.AutoPlayManager;
import com.oto.edyd.lib.imageindicator.network.NetworkImageIndicatorView;
import com.oto.edyd.module.common.activity.ComTransportActivity;
import com.oto.edyd.module.oil.activity.OilCardPayMainActivity;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.NetWork;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 功能：首页
 * 文件名：com.oto.edyd.MainIndexFragment.java
 * 创建时间：2016/1/18
 * 作者：yql
 */
public class MainIndexFragment extends Fragment implements View.OnClickListener {
    //-------------基本View控件---------------
    private LinearLayout driver; //我是司机
    private LinearLayout undertaker; //我是承运方
    private LinearLayout ll_cargo_owner;//我是货主
    private LinearLayout oilCardPay; //油卡充值
    private LinearLayout transportInsurance; //货运保险
    private LinearLayout goodsOrder; //商品订单
    private LinearLayout inviteFriends; //邀请朋友
    private NetworkImageIndicatorView imageIndicatorView; //图片指示器

    //-------------变量---------------
    private Context context; //上下文对象
    private boolean isAutoPlay = true; //是否自动播放图片
    private Common commonFixed;
    private Common common;
    private List<String> urlList = new ArrayList<String>(); //幻灯片集合
    private final static int HANDLER_NETWORK_PICTURE_REQUEST_SUCCESS = 0x10; //网络图片请求成功


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_index, null);
        init(view);
        return view;
    }

    /**
     * 初始化数据
     *
     * @param view 当前view对象
     */
    private void init(View view) {
        initFields(view); //初始化字段
        initListener(); //初始化监听器
        requestAdvertiseList(); //请求网络幻灯片图片
        String firstLaunch = commonFixed.getStringByKey("FIRST_LAUNCH");
        if (!TextUtils.isEmpty(firstLaunch)) {
            playSlides(0); //播放幻灯片
        }
    }

    /**
     * 初始化字段
     */
    private void initFields(View view) {
        context = getActivity();
        driver = (LinearLayout) view.findViewById(R.id.driver);
        undertaker = (LinearLayout) view.findViewById(R.id.undertaker);
        oilCardPay = (LinearLayout) view.findViewById(R.id.oil_card_pay);
        ll_cargo_owner = (LinearLayout) view.findViewById(R.id.ll_cargo_owner);
        transportInsurance = (LinearLayout) view.findViewById(R.id.transport_insurance);
        goodsOrder = (LinearLayout) view.findViewById(R.id.goods_order);
        inviteFriends = (LinearLayout) view.findViewById(R.id.invite_friends);
        imageIndicatorView = (NetworkImageIndicatorView) view.findViewById(R.id.network_indicate_view); //图片轮播
        common = new Common(context.getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        commonFixed = new Common(context.getSharedPreferences(Constant.FIXED_FILE, Context.MODE_PRIVATE));
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        driver.setOnClickListener(this);
        undertaker.setOnClickListener(this);
        ll_cargo_owner.setOnClickListener(this);
        oilCardPay.setOnClickListener(this);
        transportInsurance.setOnClickListener(this);
        goodsOrder.setOnClickListener(this);
        inviteFriends.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.driver: //我是司机
                intent = new Intent(context, ComTransportActivity.class);
                intent.putExtra(Constant.TRANSPORT_ROLE, Constant.DRIVER_ROLE_ID); //司机标志
                intent.putExtra("transport_title", "司机");
                startActivity(intent);
                break;
            case R.id.undertaker: //我是承运方
                String enterpriseId = common.getStringByKey(Constant.ENTERPRISE_ID);
                if (enterpriseId.equals("0")) {
                    common.showToast(context, "个人角色无权限访问");
                    return;
                }
                intent = new Intent(context, ComTransportActivity.class);
                intent.putExtra(Constant.TRANSPORT_ROLE, Constant.UNDERTAKER_ROLE_ID); //承运方标志
                intent.putExtra("transport_title", "承运方");
                startActivity(intent);
                break;
            case R.id.ll_cargo_owner: //我是发货方
                String enterId = common.getStringByKey(Constant.ENTERPRISE_ID);
                if ("0".equals(enterId)) {
                    common.showToast(context, "个人角色无权限访问");
                    return;
                }
                intent = new Intent(context, ComTransportActivity.class);
                intent.putExtra(Constant.TRANSPORT_ROLE, Constant.SHIPPER_ROLE_ID); //发货标志
                intent.putExtra("transport_title", "发货方");
                startActivity(intent);
                break;
            case R.id.oil_card_pay: //油卡充值
                intent = new Intent(context, OilCardPayMainActivity.class);
                startActivity(intent);
                break;
            case R.id.transport_insurance: //货运保险

                break;
            case R.id.goods_order: //商品订单

                break;
            case R.id.invite_friends: //邀请朋友

                break;
        }
    }

    /**
     * 请求网络幻灯片图片
     */
    private void requestAdvertiseList() {
        String url = Constant.ENTRANCE_PREFIX_v1 + "inquireAdsListByType.json?type=6";
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {

            @Override
            public void onError(Request request, Exception e) {
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                JSONObject item;
                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if (!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(getActivity(), "加载网络图片失败", Toast.LENGTH_SHORT).show();
                    }
                    jsonArray = jsonObject.getJSONArray("rows");
                    String networkUrl = "";
                    for (int i = 0; i < jsonArray.length(); i++) {
                        item = jsonArray.getJSONObject(i);
                        String strItem = item.getString("picture");
                        strItem = strItem.replaceAll("\n", "");
                        strItem = strItem.replaceAll("\r", "");
                        urlList.add(strItem);
                        if (i == jsonArray.length() - 1) {
                            networkUrl += strItem;
                        } else {
                            networkUrl += strItem + ";";
                        }
                    }

                    Message message = Message.obtain();
                    message.what = HANDLER_NETWORK_PICTURE_REQUEST_SUCCESS;
                    message.obj = networkUrl;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 图片轮播
     */
    private void playSlides(int type) {
        List<String> networkList;
        if (type == 0) {
            String networkStr = commonFixed.getStringByKey("SLIDE_URL");
            String arrayUrl[] = networkStr.split(";");
            networkList = java.util.Arrays.asList(arrayUrl);
        } else {
            networkList = urlList;
        }

        imageIndicatorView.setupLayoutByImageUrl(networkList);
        urlList.clear();
        imageIndicatorView.show();
        //是否启动自动轮播
        if (isAutoPlay) {
            autoPlay();
        }
    }

    /**
     * 自动播放图片
     */
    private void autoPlay() {
        AutoPlayManager autoBrocastManager = new AutoPlayManager(imageIndicatorView);
        autoBrocastManager.setBroadcastEnable(true);
        autoBrocastManager.setBroadCastTimes(5);//loop times
        autoBrocastManager.setBroadcastTimeIntevel(5 * 1000, 10 * 1000);//set first play time and interval
        autoBrocastManager.loop();
    }

    /**
     * 线程通讯
     */
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_NETWORK_PICTURE_REQUEST_SUCCESS: //网络图片请求成功
                    String networkStr = (String) msg.obj;
                    Map<Object, Object> map = new HashMap<Object, Object>();
                    map.put("SLIDE_URL", networkStr);
                    if (!commonFixed.isSave(map)) {
                        commonFixed.showToast(getActivity(), "幻灯片地址保存异常");
                    }
                    String firstLaunch = commonFixed.getStringByKey("FIRST_LAUNCH");
                    if (TextUtils.isEmpty(firstLaunch)) {
                        playSlides(1); //播放幻灯片
                    }
                    break;
            }
        }
    };

//    @Override
//    public void onClick(View v) {
//        Common common;
//        switch (v.getId()) {
//            case R.id.weather_main: //本地天气
//                //weatherProgressDialog.getLoadingDialog().show();
//                //判断网络是否有网络
//                NetWork netWork = new NetWork(getContext());
//                if(!netWork.isHaveInternet()){
//                    //无网络访问
//                    Toast.makeText(getActivity().getApplicationContext(), Constant.NOT_INTERNET_CONNECT, Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                intent = new Intent(getActivity(), WeatherActivity.class);
//                startActivity(intent);
//                break;
//            case R.id.position_main: //本地位置
//                intent = new Intent(getActivity(), MultyLocationActivity.class);
//                startActivity(intent);
//                break;
//            case R.id.oil_server_main: //油品服务
////                intent = new Intent(getActivity(), OilServiceActivity.class);
////                startActivity(intent);
//                common = new Common(getActivity().getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
//                if (!common.isLogin()) {
//                    Toast.makeText(getActivity(), "用户未登录，请先登录", Toast.LENGTH_LONG).show();
//                    //intent = new Intent(MainActivity.this, LoginActivity.class);
//                    return;
//                }
//                intent = new Intent(getActivity(), OilServiceActivity.class);
//                startActivity(intent);
//                break;
//            case R.id.guidepost_main: //我的车辆订单
////                intent = new Intent(getActivity(), OrderOperateActivity.class);
////                startActivity(intent);
//                common = new Common(getActivity().getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
//                if (!common.isLogin()) {
//                    Toast.makeText(getActivity(), "用户未登录，请先登录", Toast.LENGTH_LONG).show();
//                    //intent = new Intent(MainActivity.this, LoginActivity.class);
//                    //startActivity(intent);
//                    return;
//                }
//                intent = new Intent(getActivity(), OrderOperateActivity.class);
//                startActivity(intent);
//                break;
//            case R.id.order_main: //商品订单
//                intent = new Intent(getActivity().getApplicationContext(), WaitBuild.class);
//                intent.putExtra("wait_title", "商品订单");
//                startActivity(intent);
//                break;
//            case R.id.collection_main: //我的收藏
//                intent = new Intent(getActivity().getApplicationContext(), WaitBuild.class);
//                intent.putExtra("wait_title", "我的收藏");
//                startActivity(intent);
//                break;
//            case R.id.order_today: //今日订单
////                intent = new Intent(getActivity(), OrderOperateActivity.class);
////                startActivity(intent);
//                common = new Common(getActivity().getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
//                if (!common.isLogin()) {
//                    Toast.makeText(getActivity(), "用户未登录，请先登录", Toast.LENGTH_LONG).show();
//                    //intent = new Intent(MainActivity.this, LoginActivity.class);
//                    //startActivity(intent);
//                    return;
//                }
//                intent = new Intent(getActivity(), OrderOperateActivity.class);
//                startActivity(intent);
//                break;
//            case R.id.panorama: //全景图
//                intent = new Intent(getActivity().getApplicationContext(), WaitBuild.class);
//                intent.putExtra("wait_title", "全景图");
//                startActivity(intent);
//                break;
//            case R.id.discount_main: //打折
//                intent = new Intent(getActivity().getApplicationContext(), WaitBuild.class);
//                intent.putExtra("wait_title", "优惠");
//                startActivity(intent);
//                break;
//        }
//    }
}
