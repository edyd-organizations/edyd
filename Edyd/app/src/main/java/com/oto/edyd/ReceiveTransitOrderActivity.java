package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
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
import java.util.List;

/**
 * 收货方在途订单
 */
public class ReceiveTransitOrderActivity extends Activity implements View.OnClickListener, AbsListView.OnScrollListener{
    private LinearLayout historyTransportOrderBack; //返回
    private ListView receiveOrderList; //接单
    private CusProgressDialog receiveOrderDialog; //页面切换过度
    private SwipeRefreshLayout mPullToRefreshScrollView = null; //下拉组件
    private ReceiveOrderListAdapter receiveOrderListAdapter; //自定义适配器
    private Common common;
    private boolean loadFlag = false;
    private List<Integer> idList = new ArrayList<Integer>(); //ID集合
    private List<Integer> primaryIdList = new ArrayList<Integer>(); //主键ID集合
    private List<String> orderList = new ArrayList<String>(); //订单集合
    private List<String> senderAddressList = new ArrayList<String>(); //发货方地址集合
    private List<String> senderList = new ArrayList<String>();//发货人集合
    private List<String> phoneNumberList = new ArrayList<String>(); //发货人联系电话集合
    private List<Integer> orderStatusList = new ArrayList<Integer>(); //订单状态
    private int visibleLastIndex = 0; //最后可视项索引
    private final static int ROWS = 10; //分页加载数据每页10
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_transit_order);
        initFields();
        requestData(1, 10, 1); //请求数据
        mPullToRefreshScrollView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            /**
             * 刷新要做的操作
             */
            @Override
            public void onRefresh() {
                requestData(1, 10, 2);
            }
        });

        receiveOrderList.setOnScrollListener(this);
        receiveOrderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(ReceiveTransitOrderActivity.this, ReceivingOrderDetail.class);
                intent.putExtra("primaryId", String.valueOf(primaryIdList.get(position)));
                intent.putExtra("position", String.valueOf(position));
                startActivityForResult(intent, 0x21);
            }
        });
    }
    /**
     * 初始化数据
     */
    private void initFields() {
        receiveOrderList = (ListView) findViewById(R.id.receive_order_list);
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        mPullToRefreshScrollView = (SwipeRefreshLayout)findViewById(R.id.swipe_container);

    }
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        int lastIndex = receiveOrderListAdapter.getCount(); //数据集最后一项的索引
        //int lastIndex = itemsLastIndex + 1; //加上底部的loadMoreIndex项
        if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE  && visibleLastIndex ==lastIndex){
            //如果是自动加载,可以在这里放置异步加载数据的代码
            if(loadFlag) {
                loadFlag = false;
                if(lastIndex % 10 == 0) {
                    int page = lastIndex / ROWS + 1;
                    loadOrderData(page, ROWS);
                }
            }
        }
    }

    /**
     * 获取sessionid
     * @return
     */
    private String getSessionUUID() {
        return common.getStringByKey(Constant.SESSION_UUID);
    }

    /**
     * 分页加载数据
     * @param page
     * @param rows
     */
    private void loadOrderData(int page, int rows) {
        String sessionUUID = getSessionUUID();
        String url = Constant.ENTRANCE_PREFIX + "appQueryOrderList.json?sessionUuid="+sessionUUID+"&page="+page+"&rows="+rows;
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                Toast.makeText(getApplicationContext(), "获取信息失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    if (!jsonObject.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(getApplicationContext(), getString(R.string.pull_info_exception), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    jsonArray = jsonObject.getJSONArray("rows");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject tempJSON = jsonArray.getJSONObject(i);
                        idList.add(tempJSON.getInt("ID"));
                        primaryIdList.add(tempJSON.getInt("primaryId"));
                        orderList.add(tempJSON.getString("controlNum"));
                        //dateList.add(tempJSON.getString("controlDate"));
                        senderAddressList.add(tempJSON.getString("senderAddr"));
                        //receiveAddressList.add(tempJSON.getString("receiverAddr"));
                        senderList.add(tempJSON.getString("senderContactPerson"));
                        phoneNumberList.add(tempJSON.getString("senderContactTel"));
                        //consigneeList.add(tempJSON.getString("receiverContactPerson"));
                        // consigneePhoneList.add(tempJSON.getString("receiverContactTel"));
                        orderStatusList.add(tempJSON.getInt("controlStatus"));
                    }
                    Message message = new Message();
                    message.what = 3;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 去除数据
     */
    private void clearData() {
        idList.clear();
        primaryIdList.clear();
        orderList.clear();
        senderAddressList.clear();
        senderList.clear();
        phoneNumberList.clear();
        orderStatusList.clear();
    }
    /**
     *
     * @param view
     * @param firstVisibleItem 当前能看见的第一个列表项ID（从0开始）
     * @param visibleItemCount 当前能看见的列表项个数（小半个也算）
     * @param totalItemCount 列表项共数
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        visibleLastIndex = firstVisibleItem + visibleItemCount;
    }

    /**
     * 加载数据
     * @param page 第几页
     * @param rows 每页几条
     * @param loadType 加载类型
     */
    private void requestData(int page, int rows, final int loadType) {
        String sessionUUID = getSessionUUID();
        String url = Constant.ENTRANCE_PREFIX + "appQueryOrderList.json?sessionUuid="+sessionUUID+"&page="+page+"&rows="+rows;
        OkHttpClientManager.getAsyn(url, new ReceiveOrderCallback<String>(loadType) {
            @Override
            public void onError(Request request, Exception e) {
                Toast.makeText(getApplicationContext(), "获取订单数据异常", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);

                    if (!jsonObject.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(getApplicationContext(), getString(R.string.pull_info_exception), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    loadFlag = true;
                    jsonArray = jsonObject.getJSONArray("rows");
                    if(jsonArray.length() == 0) {
                        Toast.makeText(ReceiveTransitOrderActivity.this, "暂无数据", Toast.LENGTH_SHORT).show();
                    }
                    //listSize = jsonArray.length();
                    if (loadType == 2) {
                        clearData();
                    }
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject tempJSON = jsonArray.getJSONObject(i);
                        idList.add(tempJSON.getInt("ID"));
                        primaryIdList.add(tempJSON.getInt("primaryId"));
                        orderList.add(tempJSON.getString("controlNum"));
                        String tDate = tempJSON.getString("controlDate");
                        if (tDate != null && !(tDate.equals(""))) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            Date date = null;
                            try {
                                date = sdf.parse(tempJSON.getString("controlDate"));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        } else {
                        }
                        senderAddressList.add(tempJSON.getString("senderAddr"));
                        senderList.add(tempJSON.getString("senderContactPerson"));
                        phoneNumberList.add(tempJSON.getString("senderContactTel"));
                        orderStatusList.add(tempJSON.getInt("controlStatus"));
                    }

                    Message message = new Message();
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
    public abstract class ReceiveOrderCallback<T> extends OkHttpClientManager.ResultCallback<T>{

        private int loadType;

        public ReceiveOrderCallback(int loadType) {
            this.loadType = loadType;
        }
        @Override
        public void onBefore() {
            //请求之前操作
            if(loadType == 1) {
                receiveOrderDialog = new CusProgressDialog(ReceiveTransitOrderActivity.this, "正在加载订单数据...");
                receiveOrderDialog.getLoadingDialog().show();
            }
        }

        @Override
        public void onAfter() {
            //请求之后要做的操作

        }
    }

    public void back(View view){//返回
        finish();
    }
    private class ReceiveOrderListAdapter extends BaseAdapter {

        private Context context;
        private LayoutInflater inflater;

        public ReceiveOrderListAdapter(Context context) {
            this.context = context;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return idList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView orderNumber; //订单号
            TextView tvDistance; //距离
            TextView fromTo;//从哪里到哪里
            TextView startPoint; //发货地址
            TextView shipper; //发货人
            TextView phoneNumber; //发货人联系电话*/
            ImageView orderStatus; //单子状态

            convertView = inflater.inflate(R.layout.receiving_order_operation_item, null);

            orderNumber = (TextView) convertView.findViewById(R.id.orderNumView);
            tvDistance = (TextView) convertView.findViewById(R.id.tv_distance);
            fromTo = (TextView) convertView.findViewById(R.id.from_to);
            startPoint = (TextView) convertView.findViewById(R.id.tv_addr_detail); //发货地址
            shipper = (TextView) convertView.findViewById(R.id.shipper_name); //发货人
            phoneNumber = (TextView) convertView.findViewById(R.id.phone_number_one); //发货人联系电话*/
            orderStatus = (ImageView) convertView.findViewById(R.id.order_status); //订单状态
            //  orderStatus.setImageResource(R.mipmap.finished_receive);
            switch (orderStatusList.get(position)) {
                case 20: //已接单
                    orderStatus.setImageResource(R.mipmap.tts_loading_way);
                    break;
                case 30: //	到达装货
                    orderStatus.setImageResource(R.mipmap.tts_arrived_load);
                    break;
                case 40: //装货完成
                    orderStatus.setImageResource(R.mipmap.tts_completion_load);
                    break;
                case 50: //送货在途
                    orderStatus.setImageResource(R.mipmap.tts_delivery_way);
                    break;
                case 60: //到达收货
                    orderStatus.setImageResource(R.mipmap.tts_arrived_receive);
                    break;
                case 99: //收货完成
                    orderStatus.setImageResource(R.mipmap.finished_receive);
                    break;
            }
            tvDistance.setText("距离装货地3000米");
            tvDistance.setTextColor(Color.RED);
            orderNumber.setText(orderList.get(position));
            startPoint.setText(senderAddressList.get(position));
            shipper.setText(senderList.get(position));
            phoneNumber.setText(phoneNumberList.get(position));
            shipper.setText(senderList.get(position));
            phoneNumber.setText(phoneNumberList.get(position));
            return convertView;
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    receiveOrderListAdapter = new ReceiveOrderListAdapter(getApplicationContext());
                    receiveOrderList.setAdapter(receiveOrderListAdapter);
                    receiveOrderDialog.getLoadingDialog().dismiss();
                    //setListViewHeightBasedOnChildren(receiveOrderList);
                    break;
                case 2:
                    receiveOrderListAdapter = new ReceiveOrderListAdapter(getApplicationContext());
                    receiveOrderList.setAdapter(receiveOrderListAdapter);
                    mPullToRefreshScrollView.setRefreshing(false); //停止刷新
                    break;
                case 3:
                    receiveOrderListAdapter.notifyDataSetChanged(); //通知ListView更新
                    //loadMoreButton.setText("加载更多");
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {

    }
}
