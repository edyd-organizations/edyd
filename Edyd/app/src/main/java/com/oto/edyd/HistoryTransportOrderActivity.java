package com.oto.edyd;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.LocationSource;
import com.oto.edyd.lib.swiperefresh.PullToRefreshBase;
import com.oto.edyd.lib.swiperefresh.PullToRefreshScrollView;
import com.oto.edyd.service.TimerService;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.OkHttpClientManager;
import com.oto.edyd.widget.MenuListView;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 司机历史订单
 * Created by yql on 2015/9/18.
 */
public class HistoryTransportOrderActivity extends Activity implements View.OnClickListener, AbsListView.OnScrollListener {

    private LinearLayout historyTransportOrderBack; //返回
    private ListView receiveOrderList; //接单
    private CusProgressDialog receiveOrderDialog; //页面切换过度
    private SwipeRefreshLayout mPullToRefreshScrollView = null; //下拉组件
    //private ScrollView mScrollView = null; //滚动视图

    private List<Integer> idList = new ArrayList<Integer>(); //ID集合
    private List<Integer> primaryIdList = new ArrayList<Integer>(); //主键ID集合
    private List<String> orderList = new ArrayList<String>(); //订单集合
    private List<String> dateList = new ArrayList<String>(); //时间集合
    private List<String> senderAddressList = new ArrayList<String>(); //发货方地址集合
    private List<String> receiveAddressList = new ArrayList<String>(); //收货方地址集合
    private List<String> senderList = new ArrayList<String>();//发货人集合
    private List<String> phoneNumberList = new ArrayList<String>(); //发货人联系电话集合
    private List<String> consigneeList = new ArrayList<String>(); //收货人集合
    private List<String> consigneePhoneList = new ArrayList<String>(); //收货人联系电话
    private List<Integer> orderStatusList = new ArrayList<Integer>(); //订单状态

    //private int listSize; //订单数据总条数

    private int visibleLastIndex = 0; //最后可视项索引
    private int visibleItemCount; //当前窗口可见总数
    private ReceiveOrderListAdapter receiveOrderListAdapter; //自定义适配器
    private View loadMoreView; //加载更多
    private Button loadMoreButton;

    private final static int ROWS = 10; //分页加载数据每页10
    private boolean loadFlag = false;
    private Common common;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_transport_order);

        initFields();
        requestData(1, 10, 1); //请求数据
        String receiveOrderContent[] = getResources().getStringArray(R.array.receive_order);
        historyTransportOrderBack.setOnClickListener(this);
        mPullToRefreshScrollView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
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

                Intent intent = new Intent(HistoryTransportOrderActivity.this, OrderDetailHistoryActivity.class);
                intent.putExtra("primaryId", String.valueOf(primaryIdList.get(position)));
                startActivityForResult(intent, 0x21);
            }
        });
    }

    /**
     * 初始化数据
     */
    private void initFields() {
        historyTransportOrderBack = (LinearLayout) findViewById(R.id.history_transport_order_back);
        receiveOrderList = (ListView) findViewById(R.id.receive_order_list);
        //loadMoreView = getLayoutInflater().inflate(R.layout.load_more, null);
        //loadMoreButton = (Button)loadMoreView.findViewById(R.id.load_more_button);
        //receiveOrderList.addFooterView(loadMoreView);
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));

        mPullToRefreshScrollView = (SwipeRefreshLayout)findViewById(R.id.swipe_container);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.history_transport_order_back:
                finish();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        int itemsLastIndex = receiveOrderListAdapter.getCount() - 1; //数据集最后一项的索引
        int lastIndex = itemsLastIndex + 1; //加上底部的loadMoreIndex项
        if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE  && visibleLastIndex ==lastIndex){
            //如果是自动加载,可以在这里放置异步加载数据的代码
            if(loadFlag) {
                loadFlag = false;
                if(lastIndex % 10 == 0) {
                    int page = lastIndex / ROWS + 1;
                    loadOrderData(page, ROWS);
                    loadMoreButton.setText("正在加载...");
                }
            }

        }
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
        this.visibleItemCount = visibleItemCount;
        visibleLastIndex = firstVisibleItem + visibleItemCount - 1;
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

//        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//        public void otherVersion(TextView orderStatus) {
//            orderStatus.setTranslationX(40); //平移，只有在API版本为11上才能运行
//            orderStatus.setTranslationY(-20);
//        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView orderNumber; //订单号
            TextView orderDate; //订单日期
            TextView startPoint; //起始点
            TextView endPoint; //终点
            TextView shipper; //发货人
            TextView phoneNumber; //发货人联系电话
            TextView dial; //拨打发货人联系电话
            TextView consignee; //收货人
            TextView consigneePhoneNumber; //收货人联系人电话
            TextView consigneeDial; //拨打收货人联系电话
            //TextView receiveOrder; //接单
            ImageView orderStatus; //单子状态

            convertView = inflater.inflate(R.layout.history_order_operation_item, null);

            orderNumber = (TextView) convertView.findViewById(R.id.order_number);
            orderDate = (TextView) convertView.findViewById(R.id.order_date);
            startPoint = (TextView) convertView.findViewById(R.id.receive_order_start_address); //发货地址
            endPoint = (TextView) convertView.findViewById(R.id.receive_order_end_address); //收货地址
            shipper = (TextView) convertView.findViewById(R.id.shipper_name); //发货人
            phoneNumber = (TextView) convertView.findViewById(R.id.phone_number_one); //发货人联系电话
            consignee = (TextView) convertView.findViewById(R.id.consignee); //收货人
            consigneePhoneNumber = (TextView) convertView.findViewById(R.id.consignee_phone_number); //收货人联系电话
            orderStatus = (ImageView) convertView.findViewById(R.id.order_status); //订单状态

            switch (orderStatusList.get(position)) {
//                case 17: //未接单
//                    receiveOrder.setText(getString(R.string.receive_order));
//                    break;
//                case 20: //已接单
//                    orderStatus.setImageResource(R.mipmap.tts_loading_way);
//                    receiveOrder.setText("到达装货");
//                    break;
//                case 30: //	到达装货
//                    orderStatus.setImageResource(R.mipmap.tts_arrived_load);
//                    receiveOrder.setText("装货完成");
//                    break;
//                case 40: //装货完成
//                    orderStatus.setImageResource(R.mipmap.tts_completion_load);
//                    receiveOrder.setText("送货在途");
//                    break;
//                case 50: //送货在途
//                    orderStatus.setImageResource(R.mipmap.tts_delivery_way);
//                    receiveOrder.setText("到达收货");
//                    break;
//                case 60: //到达收货
//                    orderStatus.setImageResource(R.mipmap.tts_arrived_receive);
//                    receiveOrder.setText("收货完成");
//                    break;
                case 99: //收货完成
                    orderStatus.setImageResource(R.mipmap.finished_receive2);
                    break;
            }

            orderNumber.setText(orderList.get(position));
            orderDate.setText(dateList.get(position));
            startPoint.setText(senderAddressList.get(position));
            endPoint.setText(receiveAddressList.get(position));
            shipper.setText(senderList.get(position));
            phoneNumber.setText(phoneNumberList.get(position));
            consignee.setText(consigneeList.get(position));
            consigneePhoneNumber.setText(consigneePhoneList.get(position));
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
                    loadMoreButton.setText("加载更多");
                    break;
            }
        }
    };

    /**
     * 加载数据
     * @param page 第几页
     * @param rows 每页几条
     * @param loadType 加载类型
     */
    private void requestData(int page, int rows, final int loadType) {
        String sessionUUID = getSessionUUID();
        String url = Constant.ENTRANCE_PREFIX + "appQueryHistoricalOrderList.json?sessionUuid="+sessionUUID+"&page="+page+"&rows="+rows;
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
                            dateList.add(sdf.format(date));
                        } else {
                            dateList.add("");
                        }
                        senderAddressList.add(tempJSON.getString("senderAddr"));
                        receiveAddressList.add(tempJSON.getString("receiverAddr"));
                        senderList.add(tempJSON.getString("senderContactPerson"));
                        phoneNumberList.add(tempJSON.getString("senderContactTel"));
                        consigneeList.add(tempJSON.getString("receiverContactPerson"));
                        consigneePhoneList.add(tempJSON.getString("receiverContactTel"));
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
                receiveOrderDialog = new CusProgressDialog(HistoryTransportOrderActivity.this, "正在加载订单数据...");
                receiveOrderDialog.getLoadingDialog().show();
            }
        }

        @Override
        public void onAfter() {
            //请求之后要做的操作

        }
    }

    private class CusOnClickListener implements View.OnClickListener {

        private int position;
        private View view;

        public CusOnClickListener(int position, View view){
            this.position = position;
            this.view = view;
        }
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.receive_order:
                    if((((TextView)v).getText().toString().equals("完成订单"))) {
                        Toast.makeText(getApplicationContext(), "订单已完成，不能操作", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    new AlertDialog.Builder(HistoryTransportOrderActivity.this).setTitle("接单")
                            .setMessage("确认"+((TextView)v).getText().toString()+"吗？")
                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    operationOrder(position, view);
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

    /**
     * 更新订单
     * @param position
     * @param view
     */
    private void operationOrder(final int position, final View view) {

        int controlId = idList.get(position);
        String controlNum = orderList.get(position);
        final int controlStatus = orderStatusList.get(position);
        String sessionUUID = getSessionUUID();
        String url = Constant.ENTRANCE_PREFIX + "appUpdateOrderStatus.json?sessionUuid="+sessionUUID+"&controlId="+controlId+"&controlStatus="+controlStatus+"&controlNum="+controlNum;
        OkHttpClientManager.getAsyn(url, new ReceiveOrderCallback<String>(2) {
            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    if (!jsonObject.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(getApplicationContext(), "接单异常异常", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //Toast.makeText(getApplicationContext(), "成功接单", Toast.LENGTH_SHORT).show();
                    ImageView imageView = (ImageView) view.findViewById(R.id.order_status); //订单状态
                    TextView textView = (TextView) view.findViewById(R.id.receive_order); //订单操作
                    switch (controlStatus) {
                        case 17: //下一个状态
                            imageView.setImageResource(R.mipmap.tts_loading_way2); //装货在途icon
                            textView.setText("到达装货");
                            orderStatusList.set(position, 20);
                            break;
                        case 20:
                            imageView.setImageResource(R.mipmap.tts_arrived_load2); //到达装货icon
                            textView.setText("装货完成");
                            orderStatusList.set(position, 30);
                            break;
                        case 30:
                            imageView.setImageResource(R.mipmap.finished_receive2); //装货完成icon
                            textView.setText("送货在途");
                            orderStatusList.set(position, 40);
                            break;
                        case 40:
                            imageView.setImageResource(R.mipmap.tts_delivery_way2); //送货在途icon
                            textView.setText("到达收货");
                            orderStatusList.set(position, 50);
                            break;
                        case 50: //送货在途
                            imageView.setImageResource(R.mipmap.tts_arrived_receive2); //到达收货icon
                            textView.setText("收货完成");
                            orderStatusList.set(position, 60);
                            break;
                        case 60: //到达收货
                            //imageView.setImageResource(R.mipmap.tts); //收货完成
                            textView.setText("完成订单");
                            textView.setBackgroundResource(R.drawable.border_corner_login);
                            textView.setEnabled(false);
                            imageView.setVisibility(View.GONE);
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        //EdydApplication.timerService.reActivate(TimerService.mListener);

//        TimerService timerService= EdydApplication.timerService;
//        LocationSource.OnLocationChangedListener listener = TimerService.mListener;
//        timerService.stopTimer();
//        timerService.stopTimer();
//        timerService.reActivate(listener);
    }

    /**
     * 获取sessionid
     * @return
     */
    private String getSessionUUID() {
        return common.getStringByKey(Constant.SESSION_UUID);
    }

    /**
     * 去除数据
     */
    private void clearData() {
        idList.clear();
        primaryIdList.clear();
        orderList.clear();
        dateList.clear();
        senderAddressList.clear();
        receiveAddressList.clear();
        senderList.clear();
        phoneNumberList.clear();
        consigneeList.clear();
        consigneePhoneList.clear();
        orderStatusList.clear();
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
                    for(int i = 0; i < jsonArray.length(); i++) {
                        JSONObject tempJSON = jsonArray.getJSONObject(i);
                        idList.add(tempJSON.getInt("ID"));
                        primaryIdList.add(tempJSON.getInt("primaryId"));
                        orderList.add(tempJSON.getString("controlNum"));
                        dateList.add(tempJSON.getString("controlDate"));
                        senderAddressList.add(tempJSON.getString("senderAddr"));
                        receiveAddressList.add(tempJSON.getString("receiverAddr"));
                        senderList.add(tempJSON.getString("senderContactPerson"));
                        phoneNumberList.add(tempJSON.getString("senderContactTel"));
                        consigneeList.add(tempJSON.getString("receiverContactPerson"));
                        consigneePhoneList.add(tempJSON.getString("receiverContactTel"));
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
}
