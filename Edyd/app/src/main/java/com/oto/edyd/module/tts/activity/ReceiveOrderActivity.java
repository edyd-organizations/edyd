package com.oto.edyd.module.tts.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.R;
import com.oto.edyd.model.OrderBean;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 *承运方接单
 * Created by lbz on 2015/11/25.
 */
public class ReceiveOrderActivity extends Activity {

    //--------------基本view控件------------------
    private ListView lv_order;//订单列表
    //----------------变量----------------------
    private Common common;
    private OrderAdapter adapter;
    private int page = 1;
    private int rows = 20;
    private String sessionUuid;
    private Context mActivity;//上下文
    private List<OrderBean> orderlist;//订单数据容器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_order);
        initFields();//初始化字段
        initView();
    }

    private void initFields() {
        orderlist = new ArrayList<OrderBean>();
        mActivity = this;
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
    }

    private void initView() {
        lv_order = (ListView) findViewById(R.id.lv_order);
        getDate(true);
        lv_order.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        int lastPosition = lv_order.getLastVisiblePosition();
                        if (lastPosition == orderlist.size() - 1) {
                            page++;
                            getDate(false);
                        }
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }
        });
//        lv_order.setAdapter(new OrderAdapter());
    }

    /**
     *
     * @param isFirst 是否是第一次加载
     */
    private void getDate(final boolean isFirst) {

        //findPendingOrderList.json?sessionUuid=46f85d6bf7eb461cb75d52979dc87e27&page=1&rows=10

        String url = Constant.ENTRANCE_PREFIX + "findPendingOrderList.json?sessionUuid="
                + sessionUuid + "&page=" + page + "&rows=" + rows;

        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                Toast.makeText(getApplicationContext(), "获取信息失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response) {
                Common.printErrLog("........." + response);
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    if (!jsonObject.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(getApplicationContext(), "返回信息失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    jsonArray = jsonObject.getJSONArray("rows");
                    requestDistributeUserList(jsonArray,isFirst);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x12: //油卡金额数据返回执行
                    if (adapter == null) {
                        adapter = new OrderAdapter();
                        lv_order.setAdapter(adapter);
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    };

    private void requestDistributeUserList(JSONArray jsonArray,boolean isFirst) throws JSONException {

        ArrayList<OrderBean> tempList = new ArrayList<OrderBean>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            OrderBean bean = new OrderBean();
            bean.setOrderNum(obj.getString("orderNum"));
            bean.setSenderProvince(obj.getString("senderProvince"));
            bean.setSenderCity(obj.getString("senderCity"));
            bean.setSenderAddr(obj.getString("senderAddr"));
            bean.setSenderContactPerson(obj.getString("senderContactPerson"));
            bean.setSenderContactTel(obj.getString("senderContactTel"));
            bean.setReceiverProvince(obj.getString("receiverProvince"));
            bean.setReceiverCity(obj.getString("receiverCity"));
            bean.setReceiverAddr(obj.getString("receiverAddr"));
            bean.setReceiverContactPerson(obj.getString("receiverContactPerson"));
            bean.setReceiverContactTel(obj.getString("receiverContactTel"));
            bean.setTotalNum(obj.getString("totalNum"));
            bean.setPrimaryId(obj.getLong("primaryId"));
            tempList.add(bean);
        }
        if (tempList.size() == 0) {

            if (isFirst) {
                //是第一次加载数据
                Common.showToast(mActivity, "暂无数据");
            } else {
//                Common.showToast(mActivity, "没有更多数据");
            }
        } else {
            orderlist.addAll(tempList);
        }
        Message message = new Message();
        message.what = 0x12;
        handler.sendMessage(message);

    }

    public void back(View view) {
        finish();

    }

    class OrderAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return orderlist.size();
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
            final OrderBean order = orderlist.get(i);

            View itemView = View.inflate(mActivity, R.layout.recerve_order_item, null);
            //订单号。
            TextView orderNumView = (TextView) itemView.findViewById(R.id.orderNumView);
            orderNumView.setText(order.getOrderNum());
            //起点 终点。
            TextView tv_recerve_from = (TextView) itemView.findViewById(R.id.tv_recerve_from);
            tv_recerve_from.setText(order.getSenderProvince() + order.getSenderCity());

            TextView tv_recerve_to = (TextView) itemView.findViewById(R.id.tv_recerve_to);
            tv_recerve_to.setText(order.getReceiverProvince() + order.getReceiverCity());
            //发货人详细地址
            TextView tv_addr_detail = (TextView) itemView.findViewById(R.id.tv_addr_detail);
            tv_addr_detail.setText(order.getSenderAddr());
            //发货人
            TextView senderContactPersonView = (TextView) itemView.findViewById(R.id.senderContactPersonView);
            senderContactPersonView.setText(order.getSenderContactPerson());
            //发货人电话
            TextView senderContactTelView = (TextView) itemView.findViewById(R.id.senderContactTelView);
            senderContactTelView.setText(order.getSenderContactTel());
            //收货人详细地址
            TextView to_addressdetail = (TextView) itemView.findViewById(R.id.to_addressdetail);
            to_addressdetail.setText(order.getReceiverAddr());
            //收货人
            TextView receiverContactPersonView = (TextView) itemView.findViewById(R.id.receiverContactPersonView);
            receiverContactPersonView.setText(order.getReceiverContactPerson());
            //收货人电话
            TextView receiverContactTelView = (TextView) itemView.findViewById(R.id.receiverContactTelView);
            receiverContactTelView.setText(order.getReceiverContactTel());
            //接单按钮
            TextView btn_receiveorder = (TextView) itemView.findViewById(R.id.btn_receiveorder);
            btn_receiveorder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    builder.setTitle("接单");
                    builder.setMessage("你确定要接单吗？");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            submitResult(order.getPrimaryId());
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    builder.show();
                }
            });
            return itemView;
        }
    }

    private void submitResult(long primaryId) {
        //v1.1/appReceiveSelectedOrdersByOrderPrimaryId.json?sessionUuid=46f85d6bf7eb461cb75d52979dc87e27&primaryId=370

        String submitUrl = Constant.ENTRANCE_PREFIX_v1 + "appReceiveSelectedOrdersByOrderPrimaryId.json?sessionUuid="
                + sessionUuid + "&primaryId=" + primaryId;

        OkHttpClientManager.getAsyn(submitUrl, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                Toast.makeText(getApplicationContext(), "接单失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response) {

                Common.printErrLog("接单" + response);
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(response);
                    if (!jsonObject.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(getApplicationContext(), "提交失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(getApplicationContext(), "提交成功", Toast.LENGTH_SHORT).show();
                    updateDate();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateDate() {

        //findPendingOrderList.json?sessionUuid=46f85d6bf7eb461cb75d52979dc87e27&page=1&rows=10

        String url = Constant.ENTRANCE_PREFIX + "findPendingOrderList.json?sessionUuid="
                + sessionUuid + "&page=1&rows=" +page* rows;

        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                Toast.makeText(getApplicationContext(), "获取信息失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response) {
                Common.printErrLog("........." + response);
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    if (!jsonObject.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(getApplicationContext(), "返回信息失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    jsonArray = jsonObject.getJSONArray("rows");
                    orderlist.clear();
                    requestDistributeUserList(jsonArray, true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
