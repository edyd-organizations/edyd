package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.oto.edyd.model.TrackBean;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by lbz on 2015/12/7.
 * 发货方历史订单列表
 */
public class ShipperHistoryOrderActivity extends Activity {
    private ListView lv_his_order;
    private Common common;
    private SwipeRefreshLayout mPullToRefreshScrollView;//刷新用到的控件
    private CusProgressDialog loadingDialog; //页面切换过度
    private Context mActivity;
    private int page = 1;
    private int rows = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipper_hisorder);
        initField();
        initView();
        requestData( true); //请求数据

    }
    public void back(View view){
        finish();
    }

    private void initView() {
        mPullToRefreshScrollView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            /**
             * 刷新要做的操作
             */
            public void onRefresh() {
                requestData( true);
            }
        });

        lv_his_order.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
//                switch (scrollState) {
//                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
//                        int lastPosition = distributeDetailList.getLastVisiblePosition();
//                        if (lastPosition == allocationBeanlist.size() - 1) {
//                            page++;
//                            getDate(false);
//                        }
//                        break;
//                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }
        });
        lv_his_order.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(mActivity, ReceivingOrderDetail.class);
//                intent.putExtra("primaryId", String.valueOf(primaryIdList.get(position)));
//                intent.putExtra("position", String.valueOf(position));
                startActivityForResult(intent, 0x21);
            }
        });
        lv_his_order.setAdapter(new HisOrderAdapter() );
        lv_his_order.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(mActivity,ShipperHisOrderDetailActivity.class);
                startActivity(intent);
            }
        });
    }

    class HisOrderAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View itemView=View.inflate(mActivity,R.layout.shipper_hisorder_item,null);
            return itemView;
        }
    }
    /**
     * 加载数据

     * @param isFist 是否是第一次
     */
    private void requestData(final boolean isFist) {
        String sessionUUID = common.getStringByKey(Constant.SESSION_UUID);
        String url = Constant.ENTRANCE_PREFIX + "appSenderAndReceiverOrderListHistory.json?sessionUuid=" + sessionUUID + "&page=" + page + "&rows=" + rows;
        //        Common.printErrLog("" + url);
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
//                loadingDialog.getLoadingDialog().dismiss();
                Toast.makeText(getApplicationContext(), "获取信息失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response) {
//                Common.printErrLog("" + response);
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    if (!jsonObject.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(getApplicationContext(), "返回信息失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    jsonArray = jsonObject.getJSONArray("rows");

                    requestDistributeUserList(jsonArray, isFist);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void requestDistributeUserList(JSONArray jsonArray, boolean isFist) {
//        infos.clear();
//        ArrayList<TrackBean> tempList = new ArrayList<TrackBean>();
//        for (int i = 0; i < jsonArray.length(); i++) {
//            JSONObject obj = jsonArray.getJSONObject(i);
//            TrackBean bean = new TrackBean();
//            bean.setPrimaryId(obj.getLong("primaryId"));
//            bean.setControlNum(obj.getString("controlNum"));
//            bean.setTruckNum(obj.getString("truckNum"));
//            bean.setReserveNum(obj.getString("reserveNum"));
//            bean.setOrderDate(obj.getString("orderDate"));
//            tempList.add(bean);
//        }
//        if (tempList.size() == 0) {
//
//            if (isFirst) {
//                //是第一次加载数据
//                Common.showToast(mActivity, "暂无数据");
//            } else {
////                Common.showToast(mActivity, "没有更多数据");
//            }
//        } else {
//            infos.addAll(tempList);
//        }
//        Message message = new Message();
//        message.what = 1;
//        handler.sendMessage(message);
    }

    private void initField() {
        mActivity=this;
        lv_his_order = (ListView) findViewById(R.id.lv_shipper_his_order);
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        mPullToRefreshScrollView = (SwipeRefreshLayout) findViewById(R.id.swipe_container);

    }


}
