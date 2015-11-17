package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.CharacterPickerDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.model.DistributionBean;
import com.oto.edyd.model.OilAmountDistribute;
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
 * Created by Administrator on 2015/11/12.
 */
public class DistributionDetailedSearchActivity extends Activity {

    private ListView listview_result;//显示搜索结果列表
    private EditText input_number_or_card;//输入框，输入车牌号
    private List<DistributionBean> oilDistributeList;
    private String orgCode;
    private String sessionUuid;
    private String enterpriseId;
    private DistributeDetailAdapter adapter;
    private Context mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seek_oil_card_distribute_detail);
        mActivity = this;
        oilDistributeList = new ArrayList<DistributionBean>();
        Common common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        orgCode = common.getStringByKey(Constant.ORG_CODE);
        sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        enterpriseId = common.getStringByKey(Constant.ENTERPRISE_ID);
        initView();
        //首次查询信息；
        seachCar("");

        adapter = new DistributeDetailAdapter();
        listview_result.setAdapter(adapter);
        listview_result.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(mActivity, OilDistributeDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("detailBean", oilDistributeList.get(i));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    private void initView() {
        listview_result = (ListView) findViewById(R.id.list_distribute_user_seek);
        input_number_or_card = (EditText) findViewById(R.id.input_number_or_card);
        input_number_or_card.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                seachCar(s.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void searchDistribution(View view) {

    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x12: //油卡金额数据返回执行
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    /**
     * 请求预分配用户列表
     */
    private void requestDistributeUserList(JSONArray jsonArray) throws JSONException {
        oilDistributeList.clear();
        /**
         * "cardBalance" : 卡余额 //Double,
         "oilBindingDateTime" : 油卡绑定时间 //String,
         "carId" : 车牌号 //String,
         "cardId" : 卡号 //String,
         */

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            DistributionBean bean = new DistributionBean();
            bean.setCardId(obj.getString("cardId"));
            bean.setCarId(obj.getString("carId"));
            bean.setCardBalance(obj.getDouble("cardBalance"));
            bean.setOilBindingDateTime(obj.getString("oilBindingDateTime"));
            oilDistributeList.add(bean);
        }
        Message message = new Message();
        message.what = 0x12;
        handler.sendMessage(message);
    }

    private void seachCar(String cardId) {
//        String cardId = input_number_or_card.getText().toString().trim();

        String url = Constant.ENTRANCE_PREFIX + "inqueryOilBindingListInEnterpriseApp.json?sessionUuid="
                + sessionUuid + "&enterpriseId=" + enterpriseId + "&cardId=" + cardId + "&OrgCode=" + orgCode;
        Common.printErrLog(url);
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                Toast.makeText(getApplicationContext(), "获取信息失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response) {

                Common.printErrLog("获取查询信息" + response);
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    if (!jsonObject.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(getApplicationContext(), "获取查询信息失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    jsonArray = jsonObject.getJSONArray("rows");
                    requestDistributeUserList(jsonArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void back(View view) {
        finish();
    }

    class DistributeDetailAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return oilDistributeList.size();
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
        public View getView(int position, View view, ViewGroup viewGroup) {
            DistributionBean bean = oilDistributeList.get(position);
            View itemView = View.inflate(mActivity, R.layout.distribute_search, null);
            TextView car_id = (TextView) itemView.findViewById(R.id.car_id);
            TextView card_id = (TextView) itemView.findViewById(R.id.card_id);
            TextView time_band = (TextView) itemView.findViewById(R.id.time_band);
            TextView balance_mon = (TextView) itemView.findViewById(R.id.balance_mon);
            car_id.setText(bean.getCarId());
            card_id.setText(bean.getCardId());
            time_band.setText(bean.getOilBindingDateTime());
            balance_mon.setText(bean.getCardBalance() + "");
            return itemView;
        }
    }
}
