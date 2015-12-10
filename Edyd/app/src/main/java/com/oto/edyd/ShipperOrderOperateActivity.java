package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import com.oto.edyd.model.ShipperOrderOperateItem;
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
 * 功能：运输服务发货方在途订单
 * 文件名：com.oto.edyd.ShipperOrderOperateActivity.java
 * 创建时间：2015/12/3.
 * 作者：yql
 */
public class ShipperOrderOperateActivity extends Activity implements View.OnClickListener, AbsListView.OnScrollListener {

    private LinearLayout back; //返回
    private ListView shipperOrderLists; //发货方订单操作列表
    private SwipeRefreshLayout swipeContainer; //下拉刷新控件
    private EditText etSearchText; //查询内容
    private List<ShipperOrderOperateItem> shipperOrderOperateItemList = new ArrayList<ShipperOrderOperateItem>(); //订单集合
    private int visibleLastIndex = 0; //最后可视项索引
    private boolean loadFlag = false;
    private final static int ROWS = 10; //分页加载数据每页10
    private ShipperOrderOperateAdapter adapter; //适配器对象
    private Common common; //share对象
    private Context context; //上下文对象
    private CusProgressDialog transitionDialog; //过度画面
    private final static int SHIPPER_ORDER_LIST_FIRST_REQUEST_SUCCESS_CODE = 0x10; //初次请求订单列表返回码
    private final static int SHIPPER_ORDER_LIST_DOWN_LOAD_REQUEST_SUCCESS_CODE = 0x11; //下拉刷新请求返回码
    private final static int SHIPPER_ORDER_LIST_UP_LOAD_REQUEST_SUCCESS_CODE = 0x12; //上拉刷新请求成功返回码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shipper_order_operate);
        init(); //初始化数据
        requestShipperOrderOperateData(null); //请求发货方订单数据
    }

    /**
     * 初始化数据
     */
    private void init() {
        initFields();
        initListener();
    }

    /**
     * 初始化字段
     */
    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
        shipperOrderLists = (ListView) findViewById(R.id.shipper_order_lists);
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        etSearchText = (EditText) findViewById(R.id.search_content);
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        context = ShipperOrderOperateActivity.this;
        adapter = new ShipperOrderOperateAdapter(context);
    }

    /**
     * 初始化监听事件
     */
    private void initListener() {
        back.setOnClickListener(this);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //刷新操作
                requestShipperOrderOperateData("");
            }
        });
        shipperOrderLists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ShipperOrderOperateItem orderItem = shipperOrderOperateItemList.get(position);
                Intent intent = new Intent(ShipperOrderOperateActivity.this, ShipperOrderOperateItemDetailActivity.class);
                startActivity(intent);
            }
        });
        etSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String searchText = s.toString();
                if(TextUtils.isEmpty(searchText)) {

                } else {

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

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        int lastIndex = adapter.getCount(); //数据集最后一项的索引
        if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE  && visibleLastIndex ==lastIndex){
            //如果是自动加载,可以在这里放置异步加载数据的代码
            if(loadFlag) {
                loadFlag = false;
                if(lastIndex % ROWS == 0) {
                    int page = lastIndex / ROWS + 1;
                    //pageRequestTransportOderData(page, ROWS);
                }
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        visibleLastIndex = firstVisibleItem + visibleItemCount;
    }

    /**
     * 消息传递
     */
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHIPPER_ORDER_LIST_FIRST_REQUEST_SUCCESS_CODE: //初期加载订单数据返回
                    shipperOrderLists.setAdapter(adapter);
                    break;
                case SHIPPER_ORDER_LIST_DOWN_LOAD_REQUEST_SUCCESS_CODE: //下拉刷新数据返回
                    adapter.notifyDataSetChanged(); //通知
                    swipeContainer.setRefreshing(false); //停止刷新
                    break;
                case SHIPPER_ORDER_LIST_UP_LOAD_REQUEST_SUCCESS_CODE: //上拉刷新数据返回
                    break;
            }
        }
    };

    /**
     * 请求发货方在途订单
     * @param searchText 查询文本
     */
    private void requestShipperOrderOperateData (final String searchText) {
        Common fixedCommon = new Common(getSharedPreferences(Constant.FIXED_FILE, Context.MODE_PRIVATE)); //保存固定不变信息
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID); //用户唯一标示
        String aspectType = fixedCommon.getStringByKey(Constant.TRANSPORT_ROLE); //运输服务角色ID
        String orgCode = common.getStringByKey(Constant.ORG_CODE); //组织ID
        String enterpriseId = common.getStringByKey(Constant.ENTERPRISE_ID); //企业ID

        String url;
        if(searchText == null || searchText.equals("")) {
            url = Constant.ENTRANCE_PREFIX_v1 + "appSenderAndReceiverOrderList.json?sessionUuid=" + sessionUuid + "&aspectType=" +
                    aspectType + "&enterpriseId=" + enterpriseId + "&orgCode=" + orgCode + "&page=1" + "&rows=10" + "&serachParames=";
        } else {
            url = Constant.ENTRANCE_PREFIX_v1 + "appSenderAndReceiverOrderList.json?sessionUuid=" + sessionUuid + "&aspectType=" +
                    aspectType + "&enterpriseId=" + enterpriseId + "&orgCode=" + orgCode + "&page=1" + "&rows=10" + "&serachParames=" + searchText;
        }


        OkHttpClientManager.getAsyn(url, new ShipperOrderOperateResultCallback<String>(searchText) {
            @Override
            public void onError(Request request, Exception e) {
                common.showToast(context, "请求异常");
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                JSONObject item;
                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status"); //返回状态
                    if (!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        //状态不等于200请求失败
                        common.showToast(context, "订单数据请求失败");
                        return;
                    }
                    shipperOrderOperateItemList.clear();
                    jsonArray = jsonObject.getJSONArray("rows");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        item = jsonArray.getJSONObject(i);
                        ShipperOrderOperateItem shipperOrderOperateItem = new ShipperOrderOperateItem();
                        shipperOrderOperateItem.setPrimaryId(item.getString("primaryId"));
                        shipperOrderOperateItem.setOrderNumber(item.getString("controlNum")); //订单号
                        shipperOrderOperateItem.setDistance(String.valueOf(item.getDouble("distance"))); //距离
                        String startAndEndAddress = item.getString("senderAddrProviceAndCity") + "——" + item.getString("receiverAddrProviceAndCity");
                        shipperOrderOperateItem.setStartAndEndAddress(startAndEndAddress); //起始和结束地址
                        shipperOrderOperateItem.setAddress(item.getString("receiverAddr"));
                        shipperOrderOperateItem.setReceiver(item.getString("receiverName")); //收货人名字
                        shipperOrderOperateItem.setPhoneNumber(item.getString("receiverContactTel")); //收货人联系方式
                        shipperOrderOperateItem.setOrderStatus(item.getInt("orderStatus")); //订单状态
                        shipperOrderOperateItemList.add(shipperOrderOperateItem);
                    }

                    Message message = Message.obtain();
                    if (searchText == null) {
                        //初次加载
                        message.what = SHIPPER_ORDER_LIST_FIRST_REQUEST_SUCCESS_CODE;
                    } else {
                        //下拉刷新
                        message.what = SHIPPER_ORDER_LIST_DOWN_LOAD_REQUEST_SUCCESS_CODE;
                    }
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 订单列表适配器
     */
    private class ShipperOrderOperateAdapter extends BaseAdapter{

        private LayoutInflater inflater;

        public ShipperOrderOperateAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return shipperOrderOperateItemList.size();
        }

        @Override
        public Object getItem(int position) {
            return shipperOrderOperateItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;

            //通过复用convertView来提高ListView性能
            if(convertView == null) {
                //如果convertView为空则创建
                convertView = inflater.inflate(R.layout.shipper_order_operate_item, null);
                //将布局中的对象存储在viewHolder中，避免再次查找，提高性能
                viewHolder = new ViewHolder();
                viewHolder.orderFlowNunber = (TextView) convertView.findViewById(R.id.order_flow_number);
                viewHolder.orderDistance = (TextView) convertView.findViewById(R.id.distance_load_goods);
                viewHolder.orderStatus = (ImageView) convertView.findViewById(R.id.order_status);
                viewHolder.startAndEndAddress = (TextView) convertView.findViewById(R.id.start_and_end_address);
                viewHolder.endAddress = (TextView) convertView.findViewById(R.id.end_address);
                viewHolder.receiver = (TextView) convertView.findViewById(R.id.receiver);
                viewHolder.phoneNunber = (TextView) convertView.findViewById(R.id.consignee_phone_number);
                //将viewHolder存储在convertView中
                convertView.setTag(viewHolder);
            } else {
                //converView不为空，取出viewHolder
                viewHolder = (ViewHolder) convertView.getTag();
            }

            //设置数据
            ShipperOrderOperateItem shipperOrderOperateItem = shipperOrderOperateItemList.get(position);
            viewHolder.orderFlowNunber.setText(shipperOrderOperateItem.getOrderNumber());
            viewHolder.orderDistance.setText(shipperOrderOperateItem.getDistance());
            switch (shipperOrderOperateItem.getOrderStatus()) {
                case 17: //未接单
                    break;
                case 20: //已接单
                    viewHolder.orderStatus.setImageResource(R.mipmap.tts_loading_way);
                    break;
                case 30: //	到达装货
                    viewHolder.orderStatus.setImageResource(R.mipmap.tts_arrived_load);
                    break;
                case 40: //装货完成
                    viewHolder.orderStatus.setImageResource(R.mipmap.tts_completion_load);
                    break;
                case 50: //送货在途
                    viewHolder.orderStatus.setImageResource(R.mipmap.tts_delivery_way);
                    break;
                case 60: //到达收货
                    viewHolder.orderStatus.setImageResource(R.mipmap.tts_arrived_receive);
                    break;
                case 99: //收货完成
                    viewHolder.orderStatus.setImageResource(R.mipmap.finished_receive); //收货完成
                    break;
            }
            viewHolder.startAndEndAddress.setText(shipperOrderOperateItem.getStartAndEndAddress());
            viewHolder.endAddress.setText(shipperOrderOperateItem.getAddress());
            viewHolder.receiver.setText(shipperOrderOperateItem.getReceiver());
            viewHolder.phoneNunber.setText(shipperOrderOperateItem.getPhoneNumber());
            return convertView;
        }
    }

    /**
     * 缓存list item对象
     */
    static class ViewHolder{
        TextView orderFlowNunber; //订单流水号
        TextView orderDistance; //距离装货地
        ImageView orderStatus; //订单状态
        TextView startAndEndAddress; //其实和结束地址
        TextView endAddress; //结束地址
        TextView receiver; //收货人
        TextView phoneNunber; //收货人联系电话
    }

    public abstract class ShipperOrderOperateResultCallback<T> extends OkHttpClientManager.ResultCallback<T>{

        private String loadType;

        public ShipperOrderOperateResultCallback(String loadType) {
            this.loadType = loadType;
        }

        @Override
        public void onBefore() {
            //请求之前操作
            if(loadType == null) {
                transitionDialog = new CusProgressDialog(ShipperOrderOperateActivity.this, "正在拼命加载...");
                transitionDialog.getLoadingDialog().show();
            }
        }

        @Override
        public void onAfter() {
            //请求之后要做的操作
            if(loadType == null) {
                transitionDialog.getLoadingDialog().dismiss();
            }
        }
    }
}
