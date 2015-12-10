package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
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
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.oto.edyd.model.DistributionBean;
import com.oto.edyd.model.OilDistributeDetailTime;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liubaozhong
 * 分配明细
 */
public class OilDistributeDetailActivity extends Activity implements View.OnClickListener {

    private LinearLayout back; //返回
    private TextView typeCardOrCarNumber; //卡号或者车牌号
    private TextView search; //查找
    private TextView cardNumber; //卡号
    private TextView carNumber; //车牌号
    private TextView balance; //余额
    private TextView lastTime; //最后加油时间
    private TextView tv_provisions; //备付金余额
    private ExpandableListView distributeListView; //分配明细列表

    private static final int firstLoad = 0;//第一次加载
    private static final int refreshLoad = 1;//刷新加载
    private static final int moreLoad = 2;//下拉加载更多
    private int page = 1;//默认加载的页数
    private int rows = 20;//默认加载的条数

    private TextView tOilCardApply; //油卡申请
    private TextView tAmountDistribute; //金额分配
    DistributionBean bean;
    Map<Integer, OilDistributeDetailTime> oilDistributeDetailTimeMap = new HashMap<Integer, OilDistributeDetailTime>();
    private String orgCode;
    private String sessionUuid;
    private String enterpriseId;
    List<AllocationBean> allocationBeanlist = new ArrayList<AllocationBean>();
    OilCardDistributeDetailAdapter adapter;
    Context mActivity;
    private String cardId;
    private SwipeRefreshLayout swipe_container;

    private CusProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oil_card_distribute_detail);
        mActivity = this;
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        bean = (DistributionBean) bundle.getSerializable("detailBean");
        if (bean != null) {
            Common common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
            orgCode = common.getStringByKey(Constant.ORG_CODE);
            sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
            enterpriseId = common.getStringByKey(Constant.ENTERPRISE_ID);
            cardId = bean.getCardId();

            initFields(); //初始化数据
            getDate(firstLoad);

            back.setOnClickListener(this);
            tOilCardApply.setOnClickListener(this);
            tAmountDistribute.setOnClickListener(this);

        }
    }



    /**
     * @param loadType 加载的类型
     */

    private void getDate(final int loadType) {
        /**
         * inqueryOilBalanceDetailList.json?sessionUuid=&page=1&rows=8&
         */
        String url = Constant.ENTRANCE_PREFIX + "inqueryOilBalanceDetailList.json?sessionUuid="
                + sessionUuid + "&enterpriseId=" + enterpriseId + "&cardId=" + cardId + "&orgCode=" + orgCode
                + "&page=" + page + "&rows=" + rows;
        if (loadType==firstLoad) {
            loadingDialog = new CusProgressDialog(mActivity, "正在获取数据...");
            loadingDialog.getLoadingDialog().show();
        }

        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                Toast.makeText(getApplicationContext(), "获取信息失败", Toast.LENGTH_SHORT).show();
                loadingDialog.getLoadingDialog().dismiss();
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    if (!jsonObject.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(getApplicationContext(), "返回信息失败", Toast.LENGTH_SHORT).show();
                        loadingDialog.getLoadingDialog().dismiss();
                        return;
                    }
                    jsonArray = jsonObject.getJSONArray("rows");

                    requestDistributeUserList(jsonArray, loadType);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        swipe_container.setRefreshing(false);
    }

    /**
     * 数据初始化
     */
    private void initFields() {
        swipe_container = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipe_container.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            /**
             * 刷新要做的操作
             */
            @Override
            public void onRefresh() {
                getDate(refreshLoad);
            }
        });

        back = (LinearLayout) findViewById(R.id.back);
        typeCardOrCarNumber = (TextView) findViewById(R.id.type_car_number_or_card);
        search = (TextView) findViewById(R.id.search);
        cardNumber = (TextView) findViewById(R.id.card_number);
        cardNumber.setText(bean.getCardId());
        carNumber = (TextView) findViewById(R.id.car_number);
        carNumber.setText(bean.getCarId());
        balance = (TextView) findViewById(R.id.balance);
        balance.setText(bean.getCardBalance() + "");
        lastTime = (TextView) findViewById(R.id.last_time);
        lastTime.setText(bean.getOilBindingDateTime());
        tv_provisions = (TextView) findViewById(R.id.tv_provisions);
        tv_provisions.setText(bean.getProvisionsMoney() + "");
        tOilCardApply = (TextView) findViewById(R.id.oil_card_apply);
        tAmountDistribute = (TextView) findViewById(R.id.oil_card_account_distribute);

        distributeListView = (ExpandableListView) findViewById(R.id.distribute_detail_list);
        distributeListView.setGroupIndicator(null);
        distributeListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        int lastPosition = distributeListView.getLastVisiblePosition();
                        if (lastPosition == allocationBeanlist.size() - 1) {
                            page++;
                            getDate(moreLoad);
                        }
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }
        });

    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.back: //返回
                finish();
                break;
            case R.id.oil_card_apply: //油卡申请
                intent = new Intent(getApplicationContext(), OilCardApplicationActivity.class); //油卡金额分配
                startActivity(intent);
                break;
            case R.id.oil_card_account_distribute: //金额分配
                intent = new Intent(getApplicationContext(), OilCardAmountDistributeActivity.class); //油卡金额分配
                startActivity(intent);
                break;
        }
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x12: //油卡金额数据返回执行
                    loadingDialog.getLoadingDialog().dismiss();
                    if (adapter == null) {
                        adapter = new OilCardDistributeDetailAdapter(mActivity);
                        distributeListView.setAdapter(adapter);
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    };

    /**
     * 请求预分配用户列表
     */
    private void requestDistributeUserList(JSONArray jsonArray, int loadType) throws JSONException {
        ArrayList<AllocationBean> tempList = new ArrayList<AllocationBean>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            AllocationBean bean = new AllocationBean();

            bean.setCardId(obj.getString("cardId"));
            bean.setCarId(obj.getString("carId"));
            bean.setAchieveTime(obj.getString("achieveTime"));
            bean.setApplyTime(obj.getString("applyTime"));
            bean.setProvisionsMoney(obj.getString("provisionsMoney"));
            tempList.add(bean);
        }
        switch (loadType){
            case firstLoad:
                //是第一次加载数据
                if(tempList.size()==0){
                Common.showToast(mActivity, "暂无数据");
                }
                allocationBeanlist.addAll(tempList);
                break;
            case refreshLoad:
                //如果是刷新加载
                allocationBeanlist.clear();
                allocationBeanlist.addAll(tempList);
                reSetPage();
                break;
            case moreLoad:
                allocationBeanlist.addAll(tempList);
                break;
        }
        Message message = new Message();
        message.what = 0x12;
        handler.sendMessage(message);

    }

    /**
     * 重置页数
     */
    private void reSetPage() {
        page=1;
    }

    private class OilCardDistributeDetailAdapter extends BaseExpandableListAdapter {

        private Context context;
        private LayoutInflater inflater;

        private OilCardDistributeDetailAdapter(Context context) {
            this.context = context;
            inflater = LayoutInflater.from(context);
        }

        /**
         * 获取组的个数
         *
         * @return
         */
        @Override
        public int getGroupCount() {
            return allocationBeanlist.size();
        }

        /**
         * 获取指定组中的子元素个数
         *
         * @param groupPosition
         * @return
         */
        @Override
        public int getChildrenCount(int groupPosition) {
            return 1;
        }

        /**
         * 获取指定组中的数据
         *
         * @param groupPosition
         * @return
         */
        @Override
        public Object getGroup(int groupPosition) {
            return allocationBeanlist.get(groupPosition);
        }

        /**
         * 获取指定组中的指定子元素数据。
         *
         * @param groupPosition
         * @param childPosition
         * @return
         */
        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return oilDistributeDetailTimeMap.get(childPosition);
        }

        /**
         * 获取指定组中的指定子元素ID，这个ID在组里一定是唯一的。
         *
         * @param groupPosition
         * @param childPosition
         * @return
         */
        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        /**
         * 获取指定组的ID，这个组ID必须是唯一的。
         *
         * @param groupPosition
         * @return
         */
        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        /**
         * 获取显示指定组的视图对象。
         *
         * @param groupPosition
         * @param isExpanded
         * @param convertView
         * @param parent
         * @return
         */
        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            TextView tCarNumber; //车牌号
            TextView tCardNumber; //卡号
            TextView tAmount; //金额
            ImageView iUpOrDown;// 图标指示器

            View view = inflater.inflate(R.layout.oil_card_distribute_detail_parent_item, null);
            tCarNumber = (TextView) view.findViewById(R.id.car_number);
            tCardNumber = (TextView) view.findViewById(R.id.card_number);
            tAmount = (TextView) view.findViewById(R.id.balance);
            iUpOrDown = (ImageView) view.findViewById(R.id.ic_up_or_down);
            AllocationBean bean = allocationBeanlist.get(groupPosition);
            tCarNumber.setText(bean.getCarId());
            tCardNumber.setText(bean.getCardId());
            tAmount.setText(bean.getProvisionsMoney());
            //是否展开设置不同的图片
            if (isExpanded) {
                iUpOrDown.setImageResource(R.mipmap.up_arrow);
            } else {
                iUpOrDown.setImageResource(R.mipmap.down_arrow);
            }
            return view;
        }

        /**
         * 获取一个视图对象，显示指定组中的指定子元素数据
         *
         * @param groupPosition
         * @param childPosition
         * @param isLastChild
         * @param convertView
         * @param parent
         * @return
         */
        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            TextView tApplyTime; //申请时间
            TextView tCompletedTime; //完成时间

            View view = inflater.inflate(R.layout.oil_card_distribute_detail_child_item, null);
            tApplyTime = (TextView) view.findViewById(R.id.apply_time);
            tCompletedTime = (TextView) view.findViewById(R.id.completed_time);
            AllocationBean bean = allocationBeanlist.get(groupPosition);
            tApplyTime.setText(bean.getApplyTime());
            tCompletedTime.setText(bean.getAchieveTime());
            return view;
        }

        /**
         * 组和子元素是否持有稳定的ID,也就是底层数据的改变不会影响到它们
         *
         * @return
         */
        @Override
        public boolean hasStableIds() {
            return true;
        }

        /**
         * 是否选中指定位置上的子元素。
         *
         * @param groupPosition
         * @param childPosition
         * @return
         */
        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
