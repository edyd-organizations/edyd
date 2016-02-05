package com.oto.edyd;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import com.oto.edyd.utils.MLocation;
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
 * Created by yql on 2015/9/18.
 * 订单详情页
 */
public class OrderOperateActivity extends Activity implements View.OnClickListener, AbsListView.OnScrollListener {

    private LinearLayout receiveOrderBack; //返回
    private ListView receiveOrderList; //接单
    //private TextView historyOrder; //历史订单
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

//    private List<Double> latList = new ArrayList<Double>(); //纬度集合
//    private List<Double> lngList = new ArrayList<Double>(); //经度集合

    private List<Integer> orderStatusList = new ArrayList<Integer>(); //订单状态

    //用于获得终点坐标
    private List<Long> senderPrimaryIds = new ArrayList<Long>();
    private List<Long> receiverPrimaryIds = new ArrayList<Long>();

    int flag;//接口参数
    private int page;
    //private int listSize; //订单数据总条数

    private int visibleLastIndex = 0; //最后可视项索引
    //private int visibleItemCount; //当前窗口可见总数
    private ReceiveOrderListAdapter receiveOrderListAdapter; //自定义适配器
    //private View loadMoreView; //加载更多

    private final static int ROWS = 10; //分页加载数据每页10
    private boolean loadFlag = false;
    private Common common;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_operation);
        initFields();
        requestData(1, 10, 1); //请求数据
        String receiveOrderContent[] = getResources().getStringArray(R.array.receive_order);
        receiveOrderBack.setOnClickListener(this);
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

                Intent intent = new Intent(OrderOperateActivity.this, OrderDetailActivity.class);
                intent.putExtra("primaryId", String.valueOf(primaryIdList.get(position)));
                intent.putExtra("position", String.valueOf(position));
                startActivityForResult(intent, 0x21);
            }
        });
        //historyOrder.setOnClickListener(this);
    }

    /**
     * 初始化数据
     */
    private void initFields() {
        receiveOrderBack = (LinearLayout) findViewById(R.id.receive_order_back);
        receiveOrderList = (ListView) findViewById(R.id.receive_order_list);
        TextView orderTitle = (TextView) findViewById(R.id.tv_order_detail_title);
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        Intent intent = getIntent();
        flag = intent.getIntExtra("order", 0);
        if (flag == 0) {
            orderTitle.setText("待执行订单");
        } else {
            orderTitle.setText("执行中订单");
        }
        mPullToRefreshScrollView = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.receive_order_back:
                finish();
                break;
//            case R.id.history_order:
//                Intent intent = new Intent(OrderOperateActivity.this, HistoryTransportOrderActivity.class);
//                startActivity(intent);

        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        int lastIndex = receiveOrderListAdapter.getCount(); //数据集最后一项的索引
        //int lastIndex = itemsLastIndex + 1; //加上底部的loadMoreIndex项
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && visibleLastIndex == lastIndex) {
            //如果是自动加载,可以在这里放置异步加载数据的代码
            if (loadFlag) {
                loadFlag = false;
                if (lastIndex % 10 == 0) {
                    page = lastIndex / ROWS + 1;
                    loadOrderData(page, ROWS);
                }
            }
        }
    }

    /**
     * @param view
     * @param firstVisibleItem 当前能看见的第一个列表项ID（从0开始）
     * @param visibleItemCount 当前能看见的列表项个数（小半个也算）
     * @param totalItemCount   列表项共数
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        //this.visibleItemCount = visibleItemCount;
        visibleLastIndex = firstVisibleItem + visibleItemCount;
    }

    private class ReceiveOrderListAdapter extends BaseAdapter {
        private Context context;
        private LayoutInflater inflater;
        private int tOrderStatus = -1;
        private int tPosition = -2;

        public ReceiveOrderListAdapter(Context context) {
            this.context = context;
            this.inflater = LayoutInflater.from(context);
        }

        public void mNotifyDataChanged(int tPosition, int tOrderStatus) {
            this.tPosition = tPosition;
            this.tOrderStatus = tOrderStatus;
            orderStatusList.set(tPosition, tOrderStatus);
            receiveOrderListAdapter.notifyDataSetChanged();
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

            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.order_operation_item, null);
                viewHolder = new ViewHolder();
                viewHolder.orderNumber = (TextView) convertView.findViewById(R.id.order_number);
                viewHolder.orderDate = (TextView) convertView.findViewById(R.id.order_date);
                viewHolder.startPoint = (TextView) convertView.findViewById(R.id.receive_order_start_address); //发货地址
                viewHolder.endPoint = (TextView) convertView.findViewById(R.id.receive_order_end_address); //收货地址
                viewHolder.shipper = (TextView) convertView.findViewById(R.id.shipper_name); //发货人
                viewHolder.tPhoneNumber = (TextView) convertView.findViewById(R.id.phone_number_one); //发货人联系电话
                viewHolder.dial = (TextView) convertView.findViewById(R.id.dialog_one); //拨打电话
                viewHolder.consignee = (TextView) convertView.findViewById(R.id.consignee); //收货人
                viewHolder.consigneePhoneNumber = (TextView) convertView.findViewById(R.id.consignee_phone_number); //收货人联系电话
                viewHolder.consigneeDial = (TextView) convertView.findViewById(R.id.consignee_dial);
                viewHolder.receiveOrder = (TextView) convertView.findViewById(R.id.receive_order); //接单
                viewHolder.orderStatus = (ImageView) convertView.findViewById(R.id.order_status); //订单状态
                viewHolder.mNavigation = (TextView) convertView.findViewById(R.id.Navigation);//导航
                if (flag == 1) {
                    viewHolder.mNavigation.setVisibility(View.VISIBLE);
                }
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            switch (orderStatusList.get(position)) {
                case 17: //未接单
                    viewHolder.receiveOrder.setText(getString(R.string.receive_order));
                    break;
                case 20: //已接单
                    viewHolder.orderStatus.setImageResource(R.mipmap.tts_loading_way);
                    viewHolder.receiveOrder.setText("到达装货");
                    break;
                case 30: //	到达装货
                    viewHolder.orderStatus.setImageResource(R.mipmap.tts_arrived_load);
                    viewHolder.receiveOrder.setText("装货完成");
                    break;
                case 40: //装货完成
                    viewHolder.orderStatus.setImageResource(R.mipmap.tts_completion_load);
                    viewHolder.receiveOrder.setText("送货在途");
                    break;
                case 50: //送货在途
                    viewHolder.orderStatus.setImageResource(R.mipmap.tts_delivery_way);
                    viewHolder.receiveOrder.setText("到达收货");
                    break;
                case 60: //到达收货
                    viewHolder.orderStatus.setImageResource(R.mipmap.tts_arrived_receive);
                    viewHolder.receiveOrder.setText("收货完成");
                    break;
                case 99: //收货完成
                    viewHolder.orderStatus.setImageResource(R.mipmap.finished_receive); //收货完成
                    viewHolder.receiveOrder.setText("完成订单");
                    viewHolder.receiveOrder.setBackgroundResource(R.drawable.border_corner_login);
                    viewHolder.receiveOrder.setEnabled(false);
                    break;
            }

            viewHolder.orderNumber.setText(orderList.get(position));
            viewHolder.orderDate.setText(dateList.get(position));
            viewHolder.startPoint.setText(senderAddressList.get(position));
            viewHolder.endPoint.setText(receiveAddressList.get(position));
            viewHolder.shipper.setText(senderList.get(position));
            viewHolder.tPhoneNumber.setText(phoneNumberList.get(position));
            viewHolder.consignee.setText(consigneeList.get(position));
            viewHolder.consigneePhoneNumber.setText(consigneePhoneList.get(position));
            viewHolder.shipper.setText(senderList.get(position));
            viewHolder.dial.setOnClickListener(new CusOnClickListener(position, convertView));
            viewHolder.consigneeDial.setOnClickListener(new CusOnClickListener(position, convertView));
            viewHolder.receiveOrder.setOnClickListener(new CusOnClickListener(position, convertView));
            viewHolder.mNavigation.setOnClickListener(new CusOnClickListener(position, convertView));
            return convertView;
        }
    }

    static class ViewHolder {
        TextView orderNumber; //订单号
        TextView orderDate; //订单日期
        TextView startPoint; //起始点
        TextView endPoint; //终点
        TextView shipper; //发货人
        TextView tPhoneNumber; //发货人联系电话
        TextView dial; //拨打发货人联系电话
        TextView consignee; //收货人
        TextView consigneePhoneNumber; //收货人联系人电话
        TextView consigneeDial; //拨打收货人联系电话
        TextView receiveOrder; //接单
        ImageView orderStatus; //单子状
        TextView mNavigation;//导航
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    receiveOrderListAdapter = new ReceiveOrderListAdapter(getApplicationContext());
                    receiveOrderList.setAdapter(receiveOrderListAdapter);
                    ;
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
                    break;
            }
        }
    };


    /**
     * 加载数据
     *
     * @param page     第几页
     * @param rows     每页几条
     * @param loadType 加载类型
     */
    private void requestData(int page, int rows, final int loadType) {
        String sessionUUID = getSessionUUID();
        //String url = Constant.ENTRANCE_PREFIX + "appQueryOrderList.json?sessionUuid="+sessionUUID+"&page="+page+"&rows="+rows;
        String url = Constant.ENTRANCE_PREFIX_v1 + "appQueryOrderListByFlag.json?sessionUuid=" + sessionUUID + "&page=" + page + "&rows=" + rows + "&flag=" + flag;
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
                    if (jsonArray.length() == 0) {
                        Common.showToast(OrderOperateActivity.this, "暂无数据");
                    }
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
                        //导航
                        senderPrimaryIds.add(tempJSON.getLong("senderPrimaryId"));
                        receiverPrimaryIds.add(tempJSON.getLong("receiverPrimaryId"));
//                        latList.add(tempJSON.getDouble("lat"));
//                        lngList.add(tempJSON.getDouble("lng"));
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

    public abstract class ReceiveOrderCallback<T> extends OkHttpClientManager.ResultCallback<T> {

        private int loadType;

        public ReceiveOrderCallback(int loadType) {
            this.loadType = loadType;
        }

        @Override
        public void onBefore() {
            //请求之前操作
            if (loadType == 1) {
                receiveOrderDialog = new CusProgressDialog(OrderOperateActivity.this, "正在加载订单数据...");
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

        public CusOnClickListener(int position, View view) {
            this.position = position;
            this.view = view;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.receive_order:
                    if ((((TextView) v).getText().toString().equals("完成订单"))) {
                        Toast.makeText(getApplicationContext(), "订单已完成，不能操作", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    new AlertDialog.Builder(OrderOperateActivity.this).setTitle("接单")
                            .setMessage("确认" + ((TextView) v).getText().toString() + "吗？")
                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    operationOrder(position, view);
                                    if (flag == 0) {
                                        requestData(page, 10, 2); //请求数据
                                        //receiveOrderListAdapter.notifyDataSetChanged();//接单后刷新页面
                                    }

                                }
                            })
                            .setNegativeButton("否", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                    break;
                case R.id.dialog_one:
                    final String content = phoneNumberList.get(position);
                    if (content != null && (!content.equals(""))) {
                        new AlertDialog.Builder(OrderOperateActivity.this).setTitle("拨打电话")
                                .setMessage("确认拨打" + phoneNumberList.get(position).toString() + "吗?")
                                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + content));
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton("否", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "电话号码不能为空", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.consignee_dial:
                    final String consignee = consigneePhoneList.get(position);
                    if (consignee != null && (!consignee.equals(""))) {
                        new AlertDialog.Builder(OrderOperateActivity.this).setTitle("拨打电话")
                                .setMessage("确认拨打" + consigneePhoneList.get(position).toString() + "吗?")
                                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + consignee));
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton("否", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "电话号码不能为空", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.Navigation:
                    Intent intent = new Intent(OrderOperateActivity.this, DriverGPSPathActivity.class);
                    if (orderStatusList.get(position)==20) {
                        //装货在途,导航到装货地
                        intent.putExtra("PrimaryId",senderPrimaryIds.get(position));
                    }else{
                        //导航到卸货地
                        intent.putExtra("PrimaryId",receiverPrimaryIds.get(position));
                    }
                    startActivity(intent);
                    break;
            }
        }
    }

    /**
     * 更新订单
     *
     * @param position
     * @param view
     */
    private void operationOrder(final int position, final View view) {

        TextView textView = (TextView) view.findViewById(R.id.receive_order); //订单操作
        textView.setBackgroundResource(R.drawable.border_corner_order);
        textView.setEnabled(false);

        int controlId = idList.get(position);
        String sessionUUID = getSessionUUID();
        String url = Constant.ENTRANCE_PREFIX + "appAutoUpdateOrderStatus.json?sessionUuid=" + sessionUUID + "&controlId=" + controlId;
        OkHttpClientManager.getAsyn(url, new ReceiveOrderCallback<String>(2) {
            TextView textView = null;
            int controlId = idList.get(position);
            int controlStatus = orderStatusList.get(position);

            @Override
            public void onError(Request request, Exception e) {
                textView = (TextView) view.findViewById(R.id.receive_order); //订单操作
                textView.setBackgroundResource(R.drawable.border_corner_order);
                textView.setEnabled(true);
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    if (!jsonObject.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(getApplicationContext(), "接单异常", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //Toast.makeText(getApplicationContext(), "成功接单", Toast.LENGTH_SHORT).show();
                    ImageView imageView = (ImageView) view.findViewById(R.id.order_status); //订单状态
                    TextView textView = (TextView) view.findViewById(R.id.receive_order); //订单操作
                    MLocation mLocation;
                    switch (controlStatus) {
                        case 17: //下一个状态
                            imageView.setImageResource(R.mipmap.tts_loading_way); //装货在途icon
                            textView.setText("到达装货");
                            orderStatusList.set(position, 20);
                            break;
                        case 20:
                            imageView.setImageResource(R.mipmap.tts_arrived_load); //到达装货icon
                            textView.setText("装货完成");
                            orderStatusList.set(position, 30);
                            break;
                        case 30:
                            imageView.setImageResource(R.mipmap.tts_completion_load); //装货完成icon
                            textView.setText("送货在途");
                            orderStatusList.set(position, 40);
                            break;
                        case 40:
                            imageView.setImageResource(R.mipmap.tts_delivery_way); //送货在途icon
                            textView.setText("到达收货");
                            orderStatusList.set(position, 50);
                            break;
                        case 50: //送货在途
                            imageView.setImageResource(R.mipmap.tts_arrived_receive); //到达收货icon
                            textView.setText("收货完成");
                            orderStatusList.set(position, 60);
                            break;
                        case 60: //到达收货
                            //imageView.setImageResource(R.mipmap.finished_receive); //收货完成
                            //textView.setText("完成订单");
                            //textView.setBackgroundResource(R.drawable.border_corner_login);
                            //textView.setEnabled(false);
                            //imageView.setVisibility(View.GONE);
                            break;
                    }
                    //ServiceUtil.cancelAlarmManager(getApplicationContext());
                    if (!(controlStatus == 60)) {
                        //ServiceUtil.invokeTimerPOIService(getApplicationContext(), String.valueOf(controlId), String.valueOf(controlStatus));
                        textView = (TextView) view.findViewById(R.id.receive_order); //订单操作
                        textView.setBackgroundResource(R.drawable.border_corner_order);
                        textView.setEnabled(true);
                        mLocation = new MLocation(getApplicationContext(), common, String.valueOf(controlId), String.valueOf(orderStatusList.get(position)));
                    } else if (controlStatus == 60) {
                        //ServiceUtil.invokeTimerPOIService(getApplicationContext(), String.valueOf(controlId), String.valueOf(controlStatus));
                        mLocation = new MLocation(getApplicationContext(), common, String.valueOf(controlId), String.valueOf(99));
                        requestData(page, 10, 2); //请求数据
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
//        timerService.startTimer();
//        timerService.reActivate(listener);
    }

    /**
     * 获取sessionid
     *
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

        senderPrimaryIds.clear();
        receiverPrimaryIds.clear();
//        latList.clear();
//        lngList.clear();
    }


    /**
     * 分页加载数据
     *
     * @param page
     * @param rows
     */
    private void loadOrderData(int page, int rows) {
        String sessionUUID = getSessionUUID();
        String url = Constant.ENTRANCE_PREFIX_v1 + "appQueryOrderListByFlag.json?sessionUuid=" + sessionUUID + "&page=" + page + "&rows=" + rows + "&flag=" + flag;
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
                        return;}

                    jsonArray = jsonObject.getJSONArray("rows");
                    for (int i = 0; i < jsonArray.length(); i++) {
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
                        //导航
                        senderPrimaryIds.add(tempJSON.getLong("senderPrimaryId"));
                        receiverPrimaryIds.add(tempJSON.getLong("receiverPrimaryId"));
//                        latList.add(tempJSON.getDouble("lat"));
//                        lngList.add(tempJSON.getDouble("lng"));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 0x15) {
            int position = Integer.valueOf(data.getStringExtra("position"));
            int controlStatus = data.getIntExtra("controlStatus", 0);
            receiveOrderListAdapter.mNotifyDataChanged(position, controlStatus);
        }
    }
}
