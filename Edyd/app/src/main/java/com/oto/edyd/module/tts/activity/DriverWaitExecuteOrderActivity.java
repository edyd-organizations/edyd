package com.oto.edyd.module.tts.activity;

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

import com.oto.edyd.OrderDetailActivity;
import com.oto.edyd.R;
import com.oto.edyd.module.tts.model.DriverOrderBean;
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
 * 功能：司机-待执行订单
 * 文件名：com.oto.edyd.module.tts.activity.DriverWaitExecuteOrderActivity.java
 * 创建时间：2016/1/5
 * 作者：yql
 */
public class DriverWaitExecuteOrderActivity extends Activity implements View.OnClickListener, AbsListView.OnScrollListener {
    //--------基本View控件---------
    private LinearLayout back; //返回
    private ListView receiveOrderList; //接单
    private SwipeRefreshLayout swipeRefreshLayout = null; //下拉组件

    //--------变量---------
    private Common common; //login.xml偏好文件
    private Context context; //上下文对象
    private CusProgressDialog dialog; //对话框
    private WaitExecuteOrderListAdapter waitExecuteOrderListAdapter; //待执行订单列表适配器
    private List<DriverOrderBean> driverOrderBeanList = new ArrayList<DriverOrderBean>();
    private final static int ROWS = 10; //每页条数
    private int visibleLastIndex = 0; //最后可视项索引
    private boolean loadFlag = false;
    private int page; //记录当前页
    private final static int HANDLER_WAIT_ORDER_REQUEST_CODE = 0x10; //待执行订单成功返回
    private final static int HANDLER_PULL_DOWN_REQUEST_CODE = 0x11; //下拉刷新成功返回
    private final static int HANDLER_UP_DOWN_REQUEST_CODE = 0x12; //上拉加载
    private final static int HANDLER_UPDATE_ORDER_CODE = 0x13; //订单更新成功返回码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_operation);
        init(); //数据初始化
    }

    /**
     * 初始化数据
     */
    private void init() {
        initFields();
        initListener();
        requestWaitExecuteOrderData(1, ROWS, Constant.FIRST_LOAD);
    }

    /**
     * 初始化字段
     */
    private void initFields() {
        back = (LinearLayout) findViewById(R.id.receive_order_back);
        receiveOrderList = (ListView) findViewById(R.id.receive_order_list);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_container);
        context = DriverWaitExecuteOrderActivity.this;
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        back.setOnClickListener(this);
        receiveOrderList.setOnScrollListener(this);
        receiveOrderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //订单详情
                DriverOrderBean driverOrderBean = driverOrderBeanList.get(position);
                Intent intent = new Intent(context, OrderDetailActivity.class);
                intent.putExtra("primaryId", String.valueOf(driverOrderBean.getPrimary()));
                startActivity(intent);
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            /**
             * 刷新要做的操作
             */
            @Override
            public void onRefresh() {
                requestWaitExecuteOrderData(1, ROWS, Constant.SECOND_LOAD);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.receive_order_back: //返回
                finish();
                break;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        int lastIndex = waitExecuteOrderListAdapter.getCount(); //数据集最后一项的索引
        if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE  && visibleLastIndex ==lastIndex){
            //如果是自动加载,可以在这里放置异步加载数据的代码
            if(loadFlag) {
                loadFlag = false;
                if(lastIndex % ROWS == 0) {
                    page = lastIndex / ROWS + 1;
                    requestWaitExecuteOrderData(page, ROWS, Constant.THIRD_LOAD);
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
        visibleLastIndex = firstVisibleItem + visibleItemCount;
    }

    /**
     * 请求待执行订单
     * @param page
     * @param rows
     * @param requestSequence
     */
    private void requestWaitExecuteOrderData(int page, int rows, int requestSequence) {
        String sessionUUID = common.getStringByKey(Constant.SESSION_UUID);

        String url = Constant.ENTRANCE_PREFIX_v1 + "appQueryOrderListByFlag.json?sessionUuid="+sessionUUID+"&page="+page+"&rows="+rows+"&flag="+Constant.WAIT_EXECUTE_STATUS;
        OkHttpClientManager.getAsyn(url, new WaitExecuteOrderCallback<String>(requestSequence) {
            @Override
            public void onError(Request request, Exception e) {
                common.showToast(context, "请求待执行订单异常");
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    jsonObject = new JSONObject(response);
                    if (!jsonObject.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        common.showToast(context, "请求待执行订单失败");
                        return;
                    }
                    jsonArray = jsonObject.getJSONArray("rows");
                    loadFlag = true;
                    if(this.requestSequence != 3) {
                        driverOrderBeanList.clear();
                        if (jsonArray.length() == 0) {
                            common.showToast(context, "暂无数据");
                        }
                    }
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject item = jsonArray.getJSONObject(i);
                        DriverOrderBean driverOrderBean = new DriverOrderBean();
                        driverOrderBean.setId(item.getInt("ID"));
                        driverOrderBean.setPrimary(item.getInt("primaryId"));
                        driverOrderBean.setControlNum(item.getString("controlNum"));
                        String tDate = item.getString("controlDate");
                        if (tDate != null && !(tDate.equals(""))) {
                            Date date = null;
                            try {
                                date = sdf.parse(tDate);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            driverOrderBean.setDate(sdf.format(date));
                        } else {
                            driverOrderBean.setDate("");
                        }
                        driverOrderBean.setSenderAddress(item.getString("senderAddr"));
                        driverOrderBean.setReceiveAddress(item.getString("receiverAddr"));
                        driverOrderBean.setSender(item.getString("senderContactPerson"));
                        driverOrderBean.setsMobilePhoneNumber(item.getString("senderContactTel"));
                        driverOrderBean.setReceiver(item.getString("receiverContactPerson"));
                        driverOrderBean.setrMobilePhoneNumber(item.getString("receiverContactTel"));
                        driverOrderBean.setOrderStatus(item.getInt("controlStatus"));
                        driverOrderBean.setLongitude(item.getDouble("lng"));
                        driverOrderBean.setLatitude(item.getDouble("lat"));
                        driverOrderBeanList.add(driverOrderBean);
                    }

                    Message message = Message.obtain();
                    switch (this.requestSequence) {
                        case Constant.FIRST_LOAD: //首次加载
                            message.what = HANDLER_WAIT_ORDER_REQUEST_CODE; //首次加载
                            break;
                        case Constant.SECOND_LOAD: //下拉刷新
                            message.what = HANDLER_PULL_DOWN_REQUEST_CODE;
                            break;
                        case Constant.THIRD_LOAD: //上拉加载
                            message.what = HANDLER_UP_DOWN_REQUEST_CODE;
                            break;
                    }
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 更新待执行订单
     * @param controlId 订单ID
     */
    private void updateWaitExecuteOrder(final String controlId) {
        String sessionUUID = common.getStringByKey(Constant.SESSION_UUID);

        String url = Constant.ENTRANCE_PREFIX + "appAutoUpdateOrderStatus.json?sessionUuid=" + sessionUUID + "&controlId=" + controlId;
        OkHttpClientManager.getAsyn(url, new WaitExecuteOrderCallback<String>(Constant.FOUR_LOAD) {

            @Override
            public void onError(Request request, Exception e) {
                common.showToast(context, "待执行订单更新异常");
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                MLocation mLocation;
                try {
                    jsonObject = new JSONObject(response);
                    if (!jsonObject.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        common.showToast(context, "待执行订单更新失败");
                        return;
                    }
                    mLocation = new MLocation(getApplicationContext(), common, controlId, "20"); //订单发送经纬度，待执行订单状态传20
                    Message message = Message.obtain();
                    message.what = HANDLER_UPDATE_ORDER_CODE;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * 线程通讯
     */
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_WAIT_ORDER_REQUEST_CODE: //待执行订单成功返回
                    waitExecuteOrderListAdapter = new WaitExecuteOrderListAdapter(context);
                    receiveOrderList.setAdapter(waitExecuteOrderListAdapter);
                    break;
                case HANDLER_PULL_DOWN_REQUEST_CODE: //下拉刷新成功返回
                    waitExecuteOrderListAdapter.notifyDataSetChanged(); //通知更新
                    swipeRefreshLayout.setRefreshing(false);
                    break;
                case HANDLER_UP_DOWN_REQUEST_CODE: //上拉加载
                    waitExecuteOrderListAdapter.notifyDataSetChanged(); //通知ListView更新// 载
                    break;
                case HANDLER_UPDATE_ORDER_CODE: //订单更新成功返回
                    int dataSize = driverOrderBeanList.size();
                    //控制页数
                    if(page > 1 && dataSize % 10 ==1) {
                        page = page - 1;
                        requestWaitExecuteOrderData(page, ROWS, Constant.SECOND_LOAD);
                    } else {
                        requestWaitExecuteOrderData(page, ROWS, Constant.SECOND_LOAD);
                    }
                    break;
            }
        }
    };

    private class WaitExecuteOrderListAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        public WaitExecuteOrderListAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return driverOrderBeanList.size();
        }

        @Override
        public Object getItem(int position) {
            return driverOrderBeanList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if(convertView == null) {
                convertView = inflater.inflate(R.layout.wait_execute_order_item, null);
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
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            DriverOrderBean driverOrderBean = driverOrderBeanList.get(position);
            viewHolder.receiveOrder.setText(getString(R.string.receive_order));
            viewHolder.orderNumber.setText(driverOrderBean.getControlNum());
            viewHolder.orderDate.setText(driverOrderBean.getDate());
            viewHolder.startPoint.setText(driverOrderBean.getSenderAddress());
            viewHolder.endPoint.setText(driverOrderBean.getReceiveAddress());
            viewHolder.shipper.setText(driverOrderBean.getSender());
            viewHolder.tPhoneNumber.setText(driverOrderBean.getsMobilePhoneNumber());
            viewHolder.consignee.setText(driverOrderBean.getReceiver());
            viewHolder.consigneePhoneNumber.setText(driverOrderBean.getrMobilePhoneNumber());
            viewHolder.dial.setOnClickListener(new CusOnClickListener(driverOrderBean, convertView));
            viewHolder.consigneeDial.setOnClickListener(new CusOnClickListener(driverOrderBean, convertView));
            viewHolder.receiveOrder.setOnClickListener(new CusOnClickListener(driverOrderBean, convertView));
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
        ImageView orderStatus; //订单状态
    }

    private class CusOnClickListener implements View.OnClickListener {
        private DriverOrderBean driverOrderBean; //订单实体
        private View view; //当前订单view对象

        public CusOnClickListener(DriverOrderBean driverOrderBean, View view){
            this.driverOrderBean = driverOrderBean;
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
                    new AlertDialog.Builder(context).setTitle("接单")
                            .setMessage("确认"+((TextView)v).getText().toString()+"吗？")
                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    updateWaitExecuteOrder(String.valueOf(driverOrderBean.getId()));
                                }
                            })
                            .setNegativeButton("否", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                    break;
                case R.id.dialog_one:
                    final String content = driverOrderBean.getsMobilePhoneNumber();
                    if(content != null && (!content.equals(""))) {
                        new AlertDialog.Builder(context).setTitle("拨打电话")
                                .setMessage("确认拨打"+content+"吗?")
                                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + content));
                                        try {
                                            startActivity(intent);
                                        } catch (Exception e) {

                                        }
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
                    final String consignee = driverOrderBean.getrMobilePhoneNumber();
                    if(consignee != null && (!consignee.equals(""))) {
                        new AlertDialog.Builder(context).setTitle("拨打电话")
                                .setMessage("确认拨打"+consignee.toString()+"吗?")
                                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + consignee));
                                        try {
                                            startActivity(intent);
                                        } catch (Exception e) {

                                        }
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
            }
        }
    }

    private abstract class WaitExecuteOrderCallback<T> extends OkHttpClientManager.ResultCallback<T>{
        public int requestSequence; //访问次序

        public WaitExecuteOrderCallback(int requestSequence) {
            this.requestSequence = requestSequence;
        }
        @Override
        public void onBefore() {
            //请求之前操作
            if(requestSequence == 1) {
                dialog = new CusProgressDialog(context);
                dialog.showDialog();
            }
        }

        @Override
        public void onAfter() {
            //请求之后要做的操作
            if(requestSequence == 1) {
                dialog.dismissDialog();
            }
        }
    }

}
