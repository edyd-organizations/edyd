package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
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
import java.util.ArrayList;
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
    private int visibleLastIndex = 0; //最后可视项索引
    private final static int ROWS = 10; //分页加载数据每页10
    ImageView imageDelete;//清空搜索
    List<Orderdetail> addInfo= new ArrayList<Orderdetail>();
    private EditText et_input_ordernum;
    String enterpriseId;
    String orgCode;
    String aspectType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_transit_order);
        initFields();
        requestData(null); //请求数据
        mPullToRefreshScrollView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            /**
             * 刷新要做的操作
             */
            @Override
            public void onRefresh() {
                String searchText = et_input_ordernum.getText().toString();
                if(TextUtils.isEmpty(searchText)) {
                    requestData("");
                } else {
                    requestData(searchText);
                }
            }
        });

        receiveOrderList.setOnScrollListener(this);
        receiveOrderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(ReceiveTransitOrderActivity.this, ReceivingOrderDetail.class);
                Orderdetail orderdetail = addInfo.get(position);
                Bundle bundle = new Bundle();
                bundle.putSerializable("orderdetail", orderdetail);
                intent.putExtras(bundle);
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
        imageDelete = (ImageView) findViewById(R.id.delete);
        imageDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_input_ordernum.setText("");
            }
        });
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        mPullToRefreshScrollView = (SwipeRefreshLayout)findViewById(R.id.swipe_container);
        et_input_ordernum = (EditText) findViewById(R.id.et_input_ordernum);
        et_input_ordernum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                //     fillDate(searchLoad, s.toString());
                String searchText = s.toString();
                if (searchText.length() > 0) {
                    imageDelete.setVisibility(View.VISIBLE);
                    requestData(searchText);
                } else {
                    imageDelete.setVisibility(View.INVISIBLE);
                    requestData("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
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
                    String searchText = et_input_ordernum.getText().toString();
                    pageLoadOrderData(page, ROWS, searchText);
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
     * @param  searchText
     */
    private void pageLoadOrderData(int page, int rows, String searchText) {
        String sessionUUID = getSessionUUID();
        String url = Constant.ENTRANCE_PREFIX_v1 + "appSenderAndReceiverOrderList.json?sessionUuid="+sessionUUID+"&page="+page+"&rows="+rows
                +"&aspectType="+aspectType+"&enterpriseId="+enterpriseId+"&orgCode="+orgCode;
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
                        Orderdetail orderdetail=new Orderdetail();
                        orderdetail.setDistance(tempJSON.getDouble("distance"));
                        orderdetail.setControlNum(tempJSON.getString("controlNum"));//调度单
                        orderdetail.setOrderStatus(tempJSON.getInt("orderStatus"));
                        orderdetail.setStartAddrProviceAndCity(tempJSON.getString("senderAddrProviceAndCity"));//发货人省份
                        orderdetail.setStopAddrProviceAndCity(tempJSON.getString("receiverAddrProviceAndCity"));//收货人省份
                        orderdetail.setDetailedAddress(tempJSON.getString("senderAddr"));//详细地址
                        orderdetail.setContacrName(tempJSON.getString("senderName"));//发货人
                        orderdetail.setContactTel(tempJSON.getString("senderContactTel"));//发货人电话
                        orderdetail.setPrimaryId(tempJSON.getLong("primaryId"));//主键ID
                        orderdetail.setControlDate(tempJSON.getString("controlDate"));//时间
                        addInfo.add(orderdetail);
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
        addInfo.clear();
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
     * @param serachParames 查询文本
     */
    private void requestData(final String serachParames) {
        Common fixedCommon = new Common(getSharedPreferences(Constant.FIXED_FILE, Context.MODE_PRIVATE));
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        //aspectType = fixedCommon.getStringByKey(Constant.TRANSPORT_ROLE);
        aspectType = "1";
        orgCode = common.getStringByKey(Constant.ORG_CODE);
        enterpriseId = common.getStringByKey(Constant.ENTERPRISE_ID);
        String sessionUUID = getSessionUUID();
        String url = "";
        if(serachParames == null) {
            url = Constant.ENTRANCE_PREFIX_v1 + "appSenderAndReceiverOrderList.json?sessionUuid="+sessionUUID+"&page=1"+"&rows=10"
                    +"&aspectType="+aspectType+"&enterpriseId="+enterpriseId+"&orgCode="+orgCode;
        } else {
            url = Constant.ENTRANCE_PREFIX_v1 + "appSenderAndReceiverOrderList.json?sessionUuid="+sessionUUID+"&page=1"+"&rows=10"
                    +"&aspectType="+aspectType+"&enterpriseId="+enterpriseId+"&orgCode="+orgCode+"&serachParames="+serachParames;
        }

        OkHttpClientManager.getAsyn(url, new ReceiveOrderCallback<String>(serachParames) {
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
                    if(jsonArray.length() == 0&&serachParames==null) {
                        Toast.makeText(ReceiveTransitOrderActivity.this, "暂无数据", Toast.LENGTH_SHORT).show();
                    }
                    addInfo.clear();
                    ArrayList<Orderdetail> orderList = new ArrayList<Orderdetail>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject tempJSON = jsonArray.getJSONObject(i);
                        Orderdetail orderdetail=new Orderdetail();
                        orderdetail.setDistance(tempJSON.getDouble("distance"));//距离
                        orderdetail.setControlNum(tempJSON.getString("controlNum"));//调度单
                        orderdetail.setOrderStatus(tempJSON.getInt("orderStatus"));
                        orderdetail.setStartAddrProviceAndCity(tempJSON.getString("senderAddrProviceAndCity"));//发货人省份
                        orderdetail.setStopAddrProviceAndCity(tempJSON.getString("receiverAddrProviceAndCity"));//收货人省份
                        orderdetail.setDetailedAddress(tempJSON.getString("senderAddr"));//详细地址
                        orderdetail.setContacrName(tempJSON.getString("senderName"));//发货人
                        orderdetail.setContactTel(tempJSON.getString("senderContactTel"));//发货人电话
                        orderdetail.setPrimaryId(tempJSON.getLong("primaryId"));//主键ID
                        orderdetail.setControlDate(tempJSON.getString("controlDate"));//时间
                        orderList.add(orderdetail);
                        addInfo.add(orderdetail);
                    }
                    Message message = Message.obtain();
                    if(serachParames == null) {
                        //首次加载
                        message.what = 1;
                    } else {
                        //下拉加载
                        message.what = 2;
                    }
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public abstract class ReceiveOrderCallback<T> extends OkHttpClientManager.ResultCallback<T>{

        private String searchText;

        public ReceiveOrderCallback(String searchText) {
            this.searchText = searchText;
        }
        @Override
        public void onBefore() {
            //请求之前操作
            if(searchText == null) {
                receiveOrderDialog = new CusProgressDialog(ReceiveTransitOrderActivity.this, "正在加载订单数据...");
                receiveOrderDialog.getLoadingDialog().show();
            }
        }

        @Override
        public void onAfter() {
            //请求之后要做的操作
            if(searchText == null) {
                receiveOrderDialog.dismissDialog();
            }
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
            return addInfo.size();
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
            TextView startProvince;//开始省份
            TextView stopProvince;//终点省份
            TextView startPoint; //发货地址
            TextView shipper; //发货人
            TextView phoneNumber; //发货人联系电话*/
            ImageView orderStatus; //单子状态
            convertView = inflater.inflate(R.layout.receiving_order_operation_item, null);
            Orderdetail orderdetail = addInfo.get(position);
            orderNumber = (TextView) convertView.findViewById(R.id.orderNumView);
            tvDistance = (TextView) convertView.findViewById(R.id.tv_distance);
            startProvince = (TextView) convertView.findViewById(R.id.from_to);
            stopProvince = (TextView) convertView.findViewById(R.id.from);
            startPoint = (TextView) convertView.findViewById(R.id.tv_addr_detail); //发货地址
            shipper = (TextView) convertView.findViewById(R.id.shipper_name); //发货人
            phoneNumber = (TextView) convertView.findViewById(R.id.phone_number_one); //发货人联系电话*/
            orderStatus = (ImageView) convertView.findViewById(R.id.order_status); //订单状态
            switch (orderdetail.getOrderStatus()) {
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
            double distance = orderdetail.getDistance();
            if (distance==0){
                tvDistance.setVisibility(View.INVISIBLE);
            }else{
                tvDistance.setText("距离装货地"+orderdetail.getDistance()+"米");
                tvDistance.setTextColor(Color.RED);
            }
            orderNumber.setText(orderdetail.getControlNum());
            startProvince.setText(orderdetail.getStartAddrProviceAndCity());
            stopProvince.setText(orderdetail.getStopAddrProviceAndCity());
            startPoint.setText(orderdetail.getDetailedAddress());
            shipper.setText(orderdetail.getContacrName());
            phoneNumber.setText(orderdetail.getContactTel());
            return convertView;
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: //首次加载
                    receiveOrderListAdapter = new ReceiveOrderListAdapter(getApplicationContext());
                    receiveOrderList.setAdapter(receiveOrderListAdapter);
                    break;
                case 2:
                    receiveOrderListAdapter.notifyDataSetChanged();
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
