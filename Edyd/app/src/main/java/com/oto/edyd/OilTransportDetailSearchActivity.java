package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * Created by yql on 2015/12/1.
 */
public class OilTransportDetailSearchActivity extends Activity implements View.OnClickListener {
    private LinearLayout back; //返回
    private EditText blurContent; //模糊文本
    private TextView search; //搜索
    private ListView cardListView; //卡列表
    private Common common;
    private List<OilCardInfo> oilCardInfoSet = new ArrayList<OilCardInfo>(); //油卡集合
    private SelectCardAdapter selectCardAdapter;
    private CusProgressDialog cusProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_card);
        initFields();

        requestAddOilCardList(null);
        back.setOnClickListener(this);
        search.setOnClickListener(this);
        cardListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(OilTransportDetailSearchActivity.this, OilTransactionDetailActivity.class);
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
                    requestAddOilCardList(null);
                } else {
                    requestAddOilCardList(content);
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
        search = (TextView) findViewById(R.id.search);
        cardListView = (ListView) findViewById(R.id.card_list);
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
                String text = blurContent.getText().toString();
                requestAddOilCardList(text);
                break;
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x14:
                    cardListView.setAdapter(selectCardAdapter);
                    break;
                case 0x15: //暂无数据
                    selectCardAdapter.notifyDataSetChanged();
                    break;
                case 0x16: //暂无数据
                    selectCardAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    /**
     * 请求我的加油卡列表
     */
    private void requestAddOilCardList(final String text) {

        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        String enterpriseId = common.getStringByKey(Constant.ENTERPRISE_ID);
        String orgCode = common.getStringByKey(Constant.ORG_CODE);
        String url = "";

        if(text == null || text.equals("")) {
            url = Constant.ENTRANCE_PREFIX + "inqueryOilBindingListInEnterpriseApp.json?sessionUuid=" + sessionUuid +
                    "&enterpriseId=" + enterpriseId + "&OrgCode=" + orgCode;
        } else {
            url = Constant.ENTRANCE_PREFIX + "inqueryOilBindingListInEnterpriseApp.json?"+"sessionUuid="+sessionUuid+"&enterpriseId=" + enterpriseId +
                    "&OrgCode=" + orgCode + "&cardId=" + text;
        }


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
                    if (addOilArray.length() == 0) {
                        oilCardInfoSet.clear();
                        message.what = 0x16;
                        handler.sendMessage(message);
                        Toast.makeText(getApplicationContext(), "暂无数据", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    oilCardInfoSet.clear();
                    if (addOilArray.length() > 0) {
                        for (int i = 0; i < addOilArray.length(); i++) {
                            OilCardInfo oilCardInfo = new OilCardInfo();
                            JSONObject jsonObject = addOilArray.getJSONObject(i);
                            oilCardInfo.setCarId(jsonObject.getString("carId"));
                            oilCardInfo.setCardId(jsonObject.getString("cardId"));
                            oilCardInfo.setCardBalance(jsonObject.getString("cardBalance"));
                            //oilCardInfo.setOilBindingDateTime(jsonObject.getString("oilBindingDateTime"));
                            oilCardInfoSet.add(oilCardInfo);
                        }
                    }
                    if (text == null) {
                        message.what = 0x14;
                    } else {
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
                convertView = inflater.inflate(R.layout.my_add_oil_item, null);
                viewHolder.carNumber = (TextView) convertView.findViewById(R.id.car_number);
                viewHolder.time = (TextView) convertView.findViewById(R.id.time);
                viewHolder.cardNumber = (TextView) convertView.findViewById(R.id.card_number);
                viewHolder.balance = (TextView) convertView.findViewById(R.id.balance);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            OilCardInfo oilCardInfo = oilCardInfoSet.get(position);

            viewHolder.carNumber.setText(oilCardInfo.getCarId());
            //viewHolder.time.setText(oilCardInfo.getOilBindingDateTime());
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
        TextView time; //时间
        TextView cardNumber; //车牌号
        TextView balance; //金额
    }

    public abstract class SelectCardResultCallback<T> extends OkHttpClientManager.ResultCallback<T>{
        @Override
        public void onBefore() {
            //请求之前操作
            cusProgressDialog = new CusProgressDialog(OilTransportDetailSearchActivity.this, "正在拼命加载...");
            cusProgressDialog.getLoadingDialog().show();
        }

        @Override
        public void onAfter() {
            //请求之后要做的操作
            cusProgressDialog.getLoadingDialog().dismiss();
        }
    }
}
