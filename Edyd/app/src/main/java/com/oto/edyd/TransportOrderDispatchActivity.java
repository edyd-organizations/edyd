package com.oto.edyd;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.model.TransportDispatch;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.OkHttpClientManager;
import com.oto.edyd.widget.CusOnClickListener;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by yql on 2015/11/24.
 */
public class TransportOrderDispatchActivity extends Activity implements View.OnClickListener ,AbsListView.OnScrollListener{

    private LinearLayout back; //返回
    private ListView transportOrderList; //运单list
    private SwipeRefreshLayout mPullToRefreshScrollView = null; //下拉组件
    private TransportOrderAdapter transportOrderAdapter;

    private List<TransportDispatch> transportDispatchList = new ArrayList<TransportDispatch>(); //运单集合
    private int visibleLastIndex = 0; //最后可视项索引
    //private int visibleItemCount; //当前窗口可见总数
    private boolean loadFlag = false;
    private final static int ROWS = 5; //分页加载数据每页10
    private Common common;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transport_order_dispatch);
        initFields();
        requestTransportOderData(1, 1);

        mPullToRefreshScrollView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            /**
             * 刷新要做的操作
             */
            @Override
            public void onRefresh() {
                requestTransportOderData(1, 2);
            }
        });
        back.setOnClickListener(this);
        transportOrderList.setOnScrollListener(this);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        int lastIndex = transportOrderAdapter.getCount(); //数据集最后一项的索引
       //int lastIndex = itemsLastIndex + 1; //加上底部的loadMoreIndex项
        if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE  && visibleLastIndex ==lastIndex){
            //如果是自动加载,可以在这里放置异步加载数据的代码
            if(loadFlag) {
                loadFlag = false;
                if(lastIndex % ROWS == 0) {
                    int page = lastIndex / ROWS + 1;
                    pageRequestTransportOderData(page, ROWS);
                }
            }

        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        //this.visibleItemCount = visibleItemCount;
        visibleLastIndex = firstVisibleItem + visibleItemCount;
    }

    /**
     * 初始化数据
     */
    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
        transportOrderList = (ListView) findViewById(R.id.transport_order_list);
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        mPullToRefreshScrollView = (SwipeRefreshLayout)findViewById(R.id.swipe_container);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: //首次加载
                    transportOrderAdapter = new TransportOrderAdapter(TransportOrderDispatchActivity.this);
                    transportOrderList.setAdapter(transportOrderAdapter);
                    break;
                case 2: //下拉刷新
                    transportOrderAdapter.notifyDataSetChanged(); //通知ListView更新
                    mPullToRefreshScrollView.setRefreshing(false); //停止刷新
                    break;
                case 3: //分页加载
                    transportOrderAdapter.notifyDataSetChanged(); //通知ListView更新
                    break;
            }
        }
    };

    /**
     * 请求运单数据
     * @param page
     * @param loadType
     */
    private void requestTransportOderData(int page, final int loadType) {
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        String url = Constant.ENTRANCE_PREFIX + "getScheduleList.json?sessionUuid=" + sessionUuid +"&page=" + 1 + "&rows=" + ROWS;
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>(){
            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray rowJson;
                try {
                    jsonObject = new JSONObject(response);
                    if(!jsonObject.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(getApplicationContext(), "运单查询异常", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    rowJson = jsonObject.getJSONArray("rows");
                    JSONObject jsonObjectItem = new JSONObject();
                    String startAndEndAddr = "";
                    loadFlag = true;
                    if(loadType == 2) {
                        transportDispatchList.clear();
                    }
                    for (int i = 0; i < rowJson.length(); i++) {
                        TransportDispatch transportDispatch = new TransportDispatch();
                        jsonObjectItem = rowJson.getJSONObject(i);
                        transportDispatch.setPrimaryId(String.valueOf(jsonObjectItem.getInt("primaryId")));
                        transportDispatch.setOrderFlowWaterNumber(jsonObjectItem.getString("orderNum"));
                        startAndEndAddr = jsonObjectItem.getString("senderProvince") + jsonObjectItem.getString("senderCity") +
                                "---" + jsonObjectItem.getString("receiverProvince") + jsonObjectItem.getString("receiverCity");
                        transportDispatch.setStartAndEndAddress(startAndEndAddr);
                        transportDispatch.setPlaceOrderDate(timeFormatDate(jsonObjectItem.getString("orderDate")));
                        transportDispatch.setShipperName(jsonObjectItem.getString("senderName"));
                        transportDispatchList.add(transportDispatch);
                    }

                    Message message = Message.obtain();
                    switch (loadType) {
                        case 1:
                            message.what = 1; //首次加载
                            break;
                        case 2:
                            message.what = 2; //下拉刷新
                            break;
                    }
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void pageRequestTransportOderData(int page, int rows) {
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        String url = Constant.ENTRANCE_PREFIX + "getScheduleList.json?sessionUuid=" + sessionUuid +"&page=" + page + "&rows=" + rows;
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>(){
            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray rowJson;
                try {
                    jsonObject = new JSONObject(response);
                    if(!jsonObject.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(getApplicationContext(), "运单查询异常", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    rowJson = jsonObject.getJSONArray("rows");
                    JSONObject jsonObjectItem = new JSONObject();
                    String startAndEndAddr = "";
                    loadFlag = true;
                    for (int i = 0; i < rowJson.length(); i++) {
                        TransportDispatch transportDispatch = new TransportDispatch();
                        jsonObjectItem = rowJson.getJSONObject(i);
                        transportDispatch.setPrimaryId(String.valueOf(jsonObjectItem.getInt("primaryId")));
                        transportDispatch.setOrderFlowWaterNumber(jsonObjectItem.getString("orderNum"));
                        startAndEndAddr = jsonObjectItem.getString("senderProvince") + jsonObjectItem.getString("senderCity") +
                                "---" + jsonObjectItem.getString("receiverProvince") + jsonObjectItem.getString("receiverCity");
                        transportDispatch.setStartAndEndAddress(startAndEndAddr);
                        transportDispatch.setPlaceOrderDate(timeFormatDate(jsonObjectItem.getString("orderDate")));
                        transportDispatch.setShipperName(jsonObjectItem.getString("senderName"));
                        transportDispatchList.add(transportDispatch);
                    }

                    Message message = Message.obtain();
                    message.what = 3;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: //返回
                finish();
                break;
        }
    }

    private class TransportOrderAdapter extends BaseAdapter{

        private LayoutInflater inflater;

        private TransportOrderAdapter(Context context){
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return transportDispatchList.size();
        }

        @Override
        public Object getItem(int position) {
            return transportDispatchList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;

            if(convertView == null) {
                viewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.transport_order_dispatch_item, null);
                viewHolder.orderFlowWaterNumber = (TextView) convertView.findViewById(R.id.order_flow_water_number);
                viewHolder.tvAddress = (TextView) convertView.findViewById(R.id.tv_address);
                viewHolder.tvTime = (TextView) convertView.findViewById(R.id.tv_time);
                viewHolder.tvShipper = (TextView) convertView.findViewById(R.id.tv_shipper);
                viewHolder.tvOrderDistribute = (TextView) convertView.findViewById(R.id.tv_distribute_order);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            TransportDispatch transportDispatch = transportDispatchList.get(position);
            viewHolder.orderFlowWaterNumber.setText(transportDispatch.getOrderFlowWaterNumber());
            viewHolder.tvAddress.setText(transportDispatch.getStartAndEndAddress());
            viewHolder.tvTime.setText(transportDispatch.getPlaceOrderDate());
            viewHolder.tvShipper.setText(transportDispatch.getShipperName());
            viewHolder.tvOrderDistribute.setOnClickListener(new CusTranOrderDisOnClickListener(transportDispatch.getPrimaryId()));

            return convertView;
        }
    }

    static class ViewHolder{
        TextView orderFlowWaterNumber; //运单流水号
        TextView tvAddress; //起始和结束地址
        TextView tvTime; //下单时间
        TextView tvShipper; //发货方
        TextView tvOrderDistribute; //派单
    }

    /**
     * 将时间转为日期
     * @param time
     * @return
     */
    private String timeFormatDate(String time) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = df.parse(time);
            return df.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "error";
    }

    private class CusTranOrderDisOnClickListener implements View.OnClickListener{

        private String primaryId;
        public CusTranOrderDisOnClickListener(String primaryId) {
            this.primaryId = primaryId;
        }
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_distribute_order: //派单
                    new AlertDialog.Builder(TransportOrderDispatchActivity.this).setTitle("派单")
                            .setMessage("确认派单吗？")
                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setNegativeButton("否", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                    break;
            }
        }
    }
}
