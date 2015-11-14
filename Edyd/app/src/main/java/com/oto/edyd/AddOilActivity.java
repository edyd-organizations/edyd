package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.model.OilAmountDistribute;
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
 * Created by yql on 2015/11/2.
 */
public class AddOilActivity extends Activity implements View.OnClickListener {

    private LinearLayout back; //返回
    private TextView accountBalance; //账户余额
    private TextView totalNumber; //总卡数
    private ListView cardNumberList; //卡数列表
    private TextView apply; //申请
    private TextView accountDistribute; //金额分配

    private TextView tOilCardApply; //油卡申请
    private TextView tAmountDistribute; //金额分配

    private List<OilCardInfo> addOilCards = new ArrayList<OilCardInfo>(); //我的加油卡数量

    private Common common;
    private CusProgressDialog cusProgressDialog;

    private double totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_add_oil);
        initFields();

//        requestAddOilCardList(); //请求订单数据
        requestAmount(); //请求金额

        back.setOnClickListener(this);
        tOilCardApply.setOnClickListener(this);
        tAmountDistribute.setOnClickListener(this);
        cardNumberList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                OilCardInfo oilCardInfo = addOilCards.get(position);
                Bundle bundle = new Bundle();
                bundle.putSerializable("oil_card_info", oilCardInfo);
                Intent intent = new Intent(getApplicationContext(), OilCardAddDetailActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    /**
     * 初始化数据
     */
    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
        accountBalance = (TextView) findViewById(R.id.oil_card_account_balance);
        totalNumber = (TextView) findViewById(R.id.oil_card_total_number);
        cardNumberList = (ListView) findViewById(R.id.card_number_list);
        apply = (TextView) findViewById(R.id.oil_card_apply);
        accountDistribute = (TextView) findViewById(R.id.oil_card_account_distribute);
        tOilCardApply = (TextView) findViewById(R.id.oil_card_apply);
        tAmountDistribute = (TextView) findViewById(R.id.oil_card_account_distribute);
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
    }

    /**
     * 捕获点击事件，并处理
     * @param v
     */
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

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x11:
                    requestAddOilCardList();
                    break;
                case 0x12: //油卡金额数据返回执行
                    accountBalance.setText(String.valueOf(totalAmount));
                    totalNumber.setText(String.valueOf(addOilCards.size()));
                    cardNumberList.setAdapter(new AddOilAdapter(getApplicationContext()));
                    cusProgressDialog.getLoadingDialog().dismiss();
                    break;
            }
        }
    };

    /**
     * 请求总金额
     */
    private void requestAmount() {

        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        String enterpriseId = common.getStringByKey(Constant.ENTERPRISE_ID);
        String orgCode = common.getStringByKey(Constant.ORG_CODE);

        String url = Constant.ENTRANCE_PREFIX + "iqueryOilShengByEnterpriseInfo.json?sessionUuid=" +sessionUuid+ "&enterpriseId=" + enterpriseId +
                "&OrgCode=" + orgCode;

        OkHttpClientManager.getAsyn(url, new AddOilCardResultCallback<String>() {
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
                    if(!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        //变更列表数据获取失败
                        Toast.makeText(getApplicationContext(), "我的油卡金额异常", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    addOilArray = addOilJSON.getJSONArray("rows");
                    if(addOilArray.length() > 0) {
                        totalAmount = (double)addOilArray.get(0);
                    }
                    Message message = new Message();
                    message.what = 0x11;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        }

    /**
     * 请求我的加油卡列表
     */
    private void requestAddOilCardList() {

        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        String enterpriseId = common.getStringByKey(Constant.ENTERPRISE_ID);
        String orgCode = common.getStringByKey(Constant.ORG_CODE);

        String url = Constant.ENTRANCE_PREFIX + "inqueryOilBindingListInEnterprise.json?sessionUuid=" + sessionUuid+
                "&enterpriseId=" + enterpriseId + "&OrgCode=" + orgCode;

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
                    if(!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        //变更列表数据获取失败
                        Toast.makeText(getApplicationContext(), "我的油卡列表获取失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    addOilArray = addOilJSON.getJSONArray("rows");
                    addOilCards.clear();
                    if(addOilArray.length() > 0) {
                        for(int i = 0; i < addOilArray.length(); i++) {
                            OilCardInfo oilCardInfo = new OilCardInfo();
                            JSONObject jsonObject = addOilArray.getJSONObject(i);
                            oilCardInfo.setCarId(jsonObject.getString("carId"));
                            oilCardInfo.setCardId(jsonObject.getString("cardId"));
                            oilCardInfo.setCardBalance(jsonObject.getString("cardBalance"));
                            oilCardInfo.setOilBindingDateTime(jsonObject.getString("oilBindingDateTime"));
                            addOilCards.add(oilCardInfo);
                        }
                    }else {
                        Toast.makeText(getApplicationContext(), "暂无数据", Toast.LENGTH_SHORT).show();
                    }

                    Message message = new Message();
                    message.what = 0x12;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


//        Message message = new Message();
//        message.what = 0x12;
//        handler.sendMessage(message);

    }

    /**
     * 自定义适配器
     */
    private class AddOilAdapter extends BaseAdapter {

        private Context context;
        private LayoutInflater inflater;

        private AddOilAdapter(Context context) {
            this.context = context;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return addOilCards.size();
        }

        @Override
        public Object getItem(int position) {
            return addOilCards.get(position);
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

            OilCardInfo oilCardInfo = addOilCards.get(position);

            viewHolder.carNumber.setText(oilCardInfo.getCarId());
            viewHolder.time.setText(oilCardInfo.getOilBindingDateTime());
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


    public abstract class AddOilCardResultCallback<T> extends OkHttpClientManager.ResultCallback<T>{
        @Override
        public void onBefore() {
            //请求之前操作
            cusProgressDialog = new CusProgressDialog(AddOilActivity.this, "正在拼命加载数据...");
            cusProgressDialog.getLoadingDialog().show();
        }

        @Override
        public void onAfter() {
            //请求之后要做的操作
            //cusProgressDialog.getLoadingDialog().dismiss();
        }
    }

}
