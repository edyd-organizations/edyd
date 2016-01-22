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
import android.view.LayoutInflater;
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

import com.oto.edyd.model.OilCardInfo;
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
 * Created by yql on 2015/11/12.
 */
public class SelectCardActivity extends Activity implements View.OnClickListener, AbsListView.OnScrollListener {

    private LinearLayout back; //返回
    private EditText blurContent; //模糊文本
    //private TextView search; //搜索
    private ListView cardListView; //卡列表
    private SwipeRefreshLayout swipeRefreshLayout;

    private Common common;
    private List<OilCardInfo> oilCardInfoSet = new ArrayList<OilCardInfo>(); //油卡集合
    private SelectCardAdapter selectCardAdapter;
    private CusProgressDialog cusProgressDialog;

    private int visibleLastIndex = 0; //最后可视项索引
    private boolean loadFlag = false;
    private final static int ROWS = 10; //分页加载数据每页10

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_card);
        initFields();

        requestAddOilCardList(1, 10, "", 0);
        back.setOnClickListener(this);
        //search.setOnClickListener(this);
        cardListView.setOnScrollListener(this);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            /**
             * 刷新要做的操作
             */
            @Override
            public void onRefresh() {
                String searchText = blurContent.getText().toString();
                requestAddOilCardList(1, 10, searchText, 1);
            }
        });
        cardListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SelectCardActivity.this, OilTransactionDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("oil_card", oilCardInfoSet.get(position));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        blurContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString();
                if (content == null || content.equals("")) {
                    requestAddOilCardList(1, 10, content, 2);
                } else {
                    requestAddOilCardList(1, 10, content, 2);
                }
            }
        });
    }

    /**
     * 初始化数据
     */
    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
        blurContent = (EditText) findViewById(R.id.blur_content);
        //search = (TextView) findViewById(R.id.search);
        cardListView = (ListView) findViewById(R.id.card_list);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        selectCardAdapter = new SelectCardAdapter(getApplicationContext());

        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: //返回
                finish();
                break;
            case R.id.search: //搜索
                //requestAddOilCardList();
                //String text = blurContent.getText().toString();
                //requestAddOilCardList(text);
                break;
        }
    }
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        int lastIndex = selectCardAdapter.getCount(); //数据集最后一项的索引
        //int lastIndex = itemsLastIndex + 1; //加上底部的loadMoreIndex项
        if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE  && visibleLastIndex ==lastIndex){
            //如果是自动加载,可以在这里放置异步加载数据的代码
            if(loadFlag) {
                loadFlag = false;
                if(lastIndex % ROWS == 0) {
                    int page = lastIndex / ROWS + 1;
                    //pageRequestTransportOderData(page, ROWS);
                    String searchText = blurContent.getText().toString();
                    pageRequestAddOilCardList(page, ROWS, searchText);
                }
            }

        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        visibleLastIndex = firstVisibleItem + visibleItemCount;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x14: //首次加载
                    cardListView.setAdapter(selectCardAdapter);
                    break;
                case 0x15: //下拉刷新
                    selectCardAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false); //停止刷新
                    break;
                case 0x16: //上拉加载
                    selectCardAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

        /**
         * 请求我的加油卡列表, 用于初次加载，和下拉刷新
         * @param page 第几页
         * @param rows 每页几条
         * @param searchText 模糊文本
         * @param loadType 加载类型
         */
        private void requestAddOilCardList(int page, int rows, String searchText, final int loadType) {

            String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
            String enterpriseId = common.getStringByKey(Constant.ENTERPRISE_ID);
            String orgCode = common.getStringByKey(Constant.ORG_CODE);
            String url = "";

            url = Constant.ENTRANCE_PREFIX_v1 + "inqueryOilCardDetailByCarIdOrCardIdApp.json?sessionUuid=" + sessionUuid +
                    "&enterpriseId=" + enterpriseId + "&orgCode=" + orgCode + "&page=" +
                    page + "&rows=" + rows + "&serachText=" + searchText;

            OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
                @Override
                public void onError(Request request, Exception e) {

                }

                @Override
                public void onResponse(String response) {
                    JSONObject addOilJSON;
                    JSONArray addOilArray;
                    try {
                        addOilJSON = new JSONObject(response);
                        String status = addOilJSON.getString("status");
                        Message message = new Message();
                        if (!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                            //卡列表数据获取失败
                            Toast.makeText(getApplicationContext(), "油卡列表获取失败", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        addOilArray = addOilJSON.getJSONArray("rows");
                        if(addOilArray.length() == 0) {
                            oilCardInfoSet.clear();
                            message.what = 0x16;
                            handler.sendMessage(message);
                            Toast.makeText(getApplicationContext(), "暂无数据", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        loadFlag = true;
                        oilCardInfoSet.clear();
                        if (addOilArray.length() > 0) {
                            for (int i = 0; i < addOilArray.length(); i++) {
                                OilCardInfo oilCardInfo = new OilCardInfo();
                                JSONObject jsonObject = addOilArray.getJSONObject(i);
                                oilCardInfo.setCarId(jsonObject.getString("carId"));
                                oilCardInfo.setCardId(jsonObject.getString("cardId"));
                                oilCardInfo.setCardBalance(jsonObject.getString("balance"));
                                oilCardInfo.setTime(jsonObject.getString("detailDT"));
                                oilCardInfoSet.add(oilCardInfo);
                            }
                        }
                        if(loadType == 0) { //首次加载
                            message.what = 0x14;
                        } else { //下拉刷新
                            message.what = 0x15;
                        }
                        handler.sendMessage(message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    /**
     * 上拉加载
     * @param page 第几页
     * @param rows 每页几条
     * @param searchText 模糊文本
     */
    private void pageRequestAddOilCardList(int page, int rows, String searchText) {

        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        String enterpriseId = common.getStringByKey(Constant.ENTERPRISE_ID);
        String orgCode = common.getStringByKey(Constant.ORG_CODE);
        String url = "";

        url = Constant.ENTRANCE_PREFIX_v1 + "inqueryOilCardDetailByCarIdOrCardIdApp.json?sessionUuid=" + sessionUuid +
                "&enterpriseId=" + enterpriseId + "&orgCode=" + orgCode + "&page=" +
                page + "&rows=" + rows + "&serachText=" + searchText;

        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                JSONObject addOilJSON;
                JSONArray addOilArray;
                try {
                    addOilJSON = new JSONObject(response);
                    String status = addOilJSON.getString("status");
                    Message message = new Message();
                    if (!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        //卡列表数据获取失败
                        Toast.makeText(getApplicationContext(), "油卡列表获取失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    addOilArray = addOilJSON.getJSONArray("rows");
                    if(addOilArray.length() == 0) {
                        oilCardInfoSet.clear();
                        message.what = 0x16;
                        handler.sendMessage(message);
                        Toast.makeText(getApplicationContext(), "暂无数据", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    loadFlag = true;
                    if (addOilArray.length() > 0) {
                        for (int i = 0; i < addOilArray.length(); i++) {
                            OilCardInfo oilCardInfo = new OilCardInfo();
                            JSONObject jsonObject = addOilArray.getJSONObject(i);
                            oilCardInfo.setCarId(jsonObject.getString("carId"));
                            oilCardInfo.setCardId(jsonObject.getString("cardId"));
                            oilCardInfo.setCardBalance(jsonObject.getString("balance"));
                            oilCardInfo.setTime(jsonObject.getString("detailDT"));
                            oilCardInfoSet.add(oilCardInfo);
                        }
                    }
                    message.what = 0x16;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 自定义适配器
     */
    private class SelectCardAdapter extends BaseAdapter {

        private Context context;
        private LayoutInflater inflater;

        private SelectCardAdapter(Context context) {
            this.context = context;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return oilCardInfoSet.size();
        }

        @Override
        public Object getItem(int position) {
            return oilCardInfoSet.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;

            if(convertView == null) {
                viewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.oil_transport_detail_search_item, null);
                viewHolder.carNumber = (TextView) convertView.findViewById(R.id.car_number);
                viewHolder.time = (TextView) convertView.findViewById(R.id.transport_date);
                viewHolder.cardNumber = (TextView) convertView.findViewById(R.id.card_number);
                viewHolder.balance = (TextView) convertView.findViewById(R.id.balance);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            OilCardInfo oilCardInfo = oilCardInfoSet.get(position);

            viewHolder.carNumber.setText(oilCardInfo.getCarId());
            viewHolder.time.setText(oilCardInfo.getTime());
            viewHolder.cardNumber.setText(oilCardInfo.getCardId());
            viewHolder.balance.setText(oilCardInfo.getCardBalance());
            return convertView;
        }
    }

    /**
     * ListView Item项对应数据
     */
    static class ViewHolder {
        TextView carNumber; //车牌号
        TextView cardNumber; //卡号
        TextView time; //交易时间时间
        TextView balance; //卡余额
    }

    public abstract class SelectCardResultCallback<T> extends OkHttpClientManager.ResultCallback<T>{
        @Override
        public void onBefore() {
            //请求之前操作
            cusProgressDialog = new CusProgressDialog(SelectCardActivity.this, "正在拼命加载...");
            cusProgressDialog.getLoadingDialog().show();
        }

        @Override
        public void onAfter() {
            //请求之后要做的操作
            cusProgressDialog.getLoadingDialog().dismiss();
        }
    }
}
