package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.oto.edyd.model.ShipperHisOrderBean;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by lbz on 2015/12/7.
 * 发货方历史订单列表
 */
public class ShipperHistoryOrderActivity extends Activity {
    private ListView lv_his_order;
    private Common common;
    private CusProgressDialog loadingDialog; //页面切换过度
    private Context mActivity;
    private static final int firstLoad = 0;//第一次加载
    private static final int refreshLoad = 1;//刷新加载
    private static final int moreLoad = 2;//下拉加载更多
    private static final int searchLoad = 3;//查询加载
    private int page = 1;//默认加载的页数
    private int rows = 20;//默认加载的条数
    private HisOrderAdapter adapter;
    private SwipeRefreshLayout swipe_container;//刷新用到的控件
    private Gson gson;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x12: //油卡金额数据返回执行
                    //加载完成隐藏loading
                    dimissLoading();

                    if (adapter == null) {
                        adapter = new HisOrderAdapter();
                        lv_his_order.setAdapter(adapter);
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    };

    /**
     * 取消加载动画
     */
    private void dimissLoading() {
        loadingDialog.getLoadingDialog().dismiss();
        swipe_container.setRefreshing(false);
    }

    private ArrayList<ShipperHisOrderBean> infos;//数据源
    private EditText et_input_seek;
    private LinearLayout ll_clear_text;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipper_hisorder);
        initField();
        initView();
        requestData(firstLoad, ""); //请求数据

    }

    public void back(View view) {
        finish();
    }


    private void initView() {

        //分页滚动监听
        lv_his_order.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        int lastPosition = lv_his_order.getLastVisiblePosition();
                        if (lastPosition == infos.size() - 1) {
                            page++;
                            requestData(moreLoad, "");
                        }
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }
        });

        lv_his_order.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(mActivity, ShipperHisOrderDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("detailBean", infos.get(position));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    class HisOrderAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return infos.size();
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
            View itemView = View.inflate(mActivity, R.layout.shipper_hisorder_item, null);
            TextView tv_controlNum = (TextView) itemView.findViewById(R.id.tv_controlNum);
            TextView tv_senderAddrProviceAndCity = (TextView) itemView.findViewById(R.id.tv_senderAddrProviceAndCity);
            TextView tv_receiverAddrProviceAndCity = (TextView) itemView.findViewById(R.id.tv_receiverAddrProviceAndCity);
            TextView tv_receiverAddr = (TextView) itemView.findViewById(R.id.tv_receiverAddr);
            TextView tv_receiverName = (TextView) itemView.findViewById(R.id.tv_receiverName);
            TextView tv_receiverContactTel = (TextView) itemView.findViewById(R.id.tv_receiverContactTel);
            TextView tv_controlDate = (TextView) itemView.findViewById(R.id.tv_controlDate);

            ShipperHisOrderBean bean = infos.get(i);
            tv_controlNum.setText(bean.getControlNum());
            tv_senderAddrProviceAndCity.setText(bean.getSenderAddrProviceAndCity());
            tv_receiverAddrProviceAndCity.setText(bean.getReceiverAddrProviceAndCity());
            tv_receiverAddr.setText(bean.getReceiverAddr());
            tv_receiverName.setText(bean.getReceiverName());
            tv_receiverContactTel.setText(bean.getReceiverContactTel());
            tv_controlDate.setText(bean.getControlDate());

            return itemView;
        }
    }

    /**
     * 加载数据
     *
     * @param loadType 是否是第一次
     */
    private void requestData(final int loadType, String serachParames) {

        Common fixedCommon = new Common(getSharedPreferences(Constant.FIXED_FILE, Context.MODE_PRIVATE));
        String aspectType = fixedCommon.getStringByKey(Constant.TRANSPORT_ROLE);
        String orgCode = common.getStringByKey(Constant.ORG_CODE);
        String enterpriseId = common.getStringByKey(Constant.ENTERPRISE_ID);
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
//        v1.1/appSenderAndReceiverOrderListHistory.json?sessionUuid=879425d835d34ac183dddddf831ecdc7&aspectType=2&enterpriseId=52&orgCode=1
        String url = Constant.ENTRANCE_PREFIX_v1 + "appSenderAndReceiverOrderListHistory.json?sessionUuid=" + sessionUuid +
                "&aspectType=" + aspectType + "&page=" + page + "&rows=" + rows + "&enterpriseId=" + enterpriseId + "&orgCode=" + orgCode
                + "&serachParames=" + serachParames;

        //第一次进来显示loading
        if (loadType == firstLoad) {
            loadingDialog = new CusProgressDialog(mActivity, "正在获取数据...");
            loadingDialog.getLoadingDialog().show();
        }
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                dimissLoading();
                Toast.makeText(getApplicationContext(), "获取信息失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    if (!jsonObject.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(getApplicationContext(), "返回信息失败", Toast.LENGTH_SHORT).show();
                        dimissLoading();
                        return;
                    }
                    jsonArray = jsonObject.getJSONArray("rows");

                    fillList(jsonArray, loadType);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void fillList(JSONArray jsonArray, int loadType) throws JSONException {

        ArrayList<ShipperHisOrderBean> tempList = new ArrayList<ShipperHisOrderBean>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            ShipperHisOrderBean bean = gson.fromJson(obj.toString(), ShipperHisOrderBean.class);
            tempList.add(bean);
        }
        switch (loadType) {
            case firstLoad:
                //是第一次加载数据
                if (tempList.size() == 0) {
                    Common.showToast(mActivity, "暂无数据");
                } else {
                    infos.addAll(tempList);
                }
                break;
            case refreshLoad:
                //如果是刷新加载
                reSetPage();
                infos.clear();
                infos.addAll(tempList);
                break;
            case moreLoad:
                infos.addAll(tempList);
                break;
            case searchLoad:
                //查询加载
                infos.clear();
                infos.addAll(tempList);
                reSetPage();
                break;
        }
        Message message = Message.obtain();
        message.what = 0x12;
        handler.sendMessage(message);

    }

    /**
     * 重置页数
     */
    private void reSetPage() {
        page = 1;
    }

    private void initField() {
        mActivity = this;
        lv_his_order = (ListView) findViewById(R.id.lv_shipper_his_order);
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        gson = new Gson();
        swipe_container = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipe_container.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            /**
             * 刷新要做的操作
             */
            @Override
            public void onRefresh() {
                requestData(refreshLoad, "");
            }
        });
        infos = new ArrayList<ShipperHisOrderBean>();
        et_input_seek = (EditText) findViewById(R.id.et_input_seek);
        et_input_seek.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (s.toString().length() != 0) {
                    //当有文本是显示
                    ll_clear_text.setVisibility(View.VISIBLE);
                } else {
                    ll_clear_text.setVisibility(View.INVISIBLE);
                }
                requestData(searchLoad, s.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        ll_clear_text = (LinearLayout) findViewById(R.id.ll_clear_text);
        ll_clear_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_input_seek.setText("");
            }
        });
    }
}
