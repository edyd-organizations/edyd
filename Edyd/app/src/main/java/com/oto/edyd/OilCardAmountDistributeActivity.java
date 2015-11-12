package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
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

import com.oto.edyd.model.OilAmountDistribute;
import com.oto.edyd.model.OilDataBean;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
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
 * Created by yql on 2015/10/28.
 */
public class OilCardAmountDistributeActivity extends Activity implements View.OnClickListener {

    private LinearLayout back; //返回
    private EditText inputCarNumber; //车牌号
    //    private Button search; //搜索
    private TextView amount; //总金额
    //    private TextView averageDistribute; //等额预分配
    private ListView listDistributeUser; //分配用户列表
    private TextView distributeCardNumber; //本次预分配卡数
    private TextView predictionDistributeAmount; //本次预分配金额
    private TextView submit; //提交
    private String submitUrl;
    private Common common;
    List<OilDataBean> sendDataList;
    private OilCardAmountDistributeAdapter adapter;
    String sessionUuid;
    String enterpriseId;
    private Context mActivity;

    private List<OilAmountDistribute> oilAmountDistributeList = new ArrayList<OilAmountDistribute>(); //列表数据

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oil_card_amount_distribute);
        mActivity = this;
        initFields();
        listDistributeUser.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EditText editText = (EditText) view.findViewById(R.id.prediction_distribute_amount);
                editText.requestFocus();
                editText.setFocusable(true);
                // editText.setFocusableInTouchMode(true);
            }

            public void noNothingSelected(AdapterView<?> parent) {
                listDistributeUser.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
            }
        });
    }


    private void initFields() {

        back = (LinearLayout) findViewById(R.id.back);
        inputCarNumber = (EditText) findViewById(R.id.type_car_number);
//        search = (Button) findViewById(R.id.search);
        amount = (TextView) findViewById(R.id.amount);
        listDistributeUser = (ListView) findViewById(R.id.list_distribute_user);
        //获得总金额数据
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        enterpriseId = common.getStringByKey(Constant.ENTERPRISE_ID);
        String OrgCode = null;
        //iqueryOilShengByEnterpriseInfo.json?sessionUuid=&enterpriseId=54&OrgCode=1&sysFullName=bgwl
        final String url = Constant.ENTRANCE_PREFIX + "iqueryOilShengByEnterpriseInfo.json?sessionUuid="
                + sessionUuid + "&enterpriseId=" + enterpriseId + "&OrgCode=" + OrgCode;
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                Toast.makeText(getApplicationContext(), "获取信息失败", Toast.LENGTH_SHORT).show();
            }

            //            {"status":"200","flag":"SUCCESS","message":"操作成功",
//                    "action":"iqueryOilShengByEnterpriseInfo","version":"v1.0","format":"json","rows":[2000]}
            @Override
            public void onResponse(String response) {
                Common.printErrLog("金额数据" + response);
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    if (!jsonObject.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(getApplicationContext(), getString(R.string.pull_orgcode_exception), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    jsonArray = jsonObject.getJSONArray("rows");
                    int mon = (Integer) jsonArray.get(0);
                    amount.setText(mon + "");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        //获取车牌信息
        // inqueryOilBalanceExprot.json?sessionUuid=6a7ca347b9914bcb909b54a88b4d555d&enterpriseId=54&OrgCode=1
        String carUrl = Constant.ENTRANCE_PREFIX + "inqueryOilBalanceExprot.json?sessionUuid="
                + sessionUuid + "&enterpriseId=" + enterpriseId + "&carId=" + "";
//        String carUrl = Constant.ENTRANCE_PREFIX + "inqueryOilBalanceExprot.json?sessionUuid="
//                + sessionUuid + "&enterpriseId=" + enterpriseId + "&OrgCode=" + OrgCode;

        OkHttpClientManager.getAsyn(carUrl, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                Toast.makeText(getApplicationContext(), "获取信息失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response) {
                Common.printErrLog("获取车牌信息" + response);
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    if (!jsonObject.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(getApplicationContext(), "获取车牌失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    jsonArray = jsonObject.getJSONArray("rows");
                    requestDistributeUserList(jsonArray);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        //提交url
        submitUrl = Constant.ENTRANCE_PREFIX + "inquerePredistribution.json";


        distributeCardNumber = (TextView) findViewById(R.id.current_distribute_card_number);
        predictionDistributeAmount = (TextView) findViewById(R.id.current_prediction_distribute_amount);
        //提交
        submit = (TextView) findViewById(R.id.submit);
        submit.setOnClickListener(this);
        back.setOnClickListener(this);
    }

    public void searchCarName(View view) {
        String carNumber = inputCarNumber.getText().toString().trim();
        //查找
        // inqueryOilBalanceExprot.json?sessionUuid=6a7ca347b9914bcb909b54a88b4d555d&enterpriseId=0&carId=%E9%97%BDD39698
        String url = Constant.ENTRANCE_PREFIX + "inqueryOilBalanceExprot.json?sessionUuid="
                + sessionUuid + "&enterpriseId=" + enterpriseId + "&carId=" + carNumber;
        Common.printLog("aaaaaaaaaaaaa" + url);
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
                    oilAmountDistributeList.clear();
                    requestDistributeUserList(jsonArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void clearAll(View view) {
        for (OilAmountDistribute oad : oilAmountDistributeList) {
            oad.setAmount(null);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.back: //返回
                finish();
                break;
            case R.id.submit: //提交
                Map<String, String> params = new HashMap<String, String>();
                params.put("sessionUuid", sessionUuid);
                sendDataList = new ArrayList<OilDataBean>();
                fillSendData(sendDataList);
                String sendData = common.createJsonString(sendDataList);
                Common.printErrLog("bbbbbbbbbb" + sendData);
                params.put("sendData", sendData);
                Common.printErrLog("submitUrl" + submitUrl);
                OkHttpClientManager.postAsyn(submitUrl, params, new OkHttpClientManager.ResultCallback<String>() {
                    @Override
                    public void onError(Request request, Exception e) {
                        Toast.makeText(mActivity, "提交失败", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonObject;
                        try {
                            jsonObject = new JSONObject(response);
                            if (!jsonObject.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                                Toast.makeText(getApplicationContext(), "提交失败", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Toast.makeText(getApplicationContext(), "提交成功", Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;
        }
    }

    private void fillSendData(List<OilDataBean> list) {
//        private List<OilAmountDistribute> oilAmountDistributeList
        for (OilAmountDistribute oad : oilAmountDistributeList) {
            OilDataBean bean = new OilDataBean();
            bean.setReleationId(oad.getReleationId());
            bean.setOilCardId(oad.getOilCardId());
            bean.setCardId(oad.getCardNumber());
            bean.setAllocatedamount("3");
            list.add(bean);
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x12: //油卡金额数据返回执行
                    if (adapter == null) {
                        adapter = new OilCardAmountDistributeAdapter(OilCardAmountDistributeActivity.this);
                        listDistributeUser.setAdapter(adapter);
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
    private void requestDistributeUserList(JSONArray jsonArray) throws JSONException {

        //假数据
//        for (int i = 0; i < 5; i++) {
//            OilAmountDistribute oilAmountDistribute = new OilAmountDistribute();
//            oilAmountDistribute.setCardUser("米诺" + i);
//            oilAmountDistribute.setCardNumber("闽F324" + i);
//            oilAmountDistribute.setCarNumber("535734673" + i);
//            oilAmountDistributeList.add(oilAmountDistribute);
//        }
        //{"ID":50,"accountId":0,"cardBalance":1502.31,"cardHolder":"闽DSM908","cardId":"1000113500003339072","checkCode":"",
        // "effTime":"2015-11-11 10:27:50","expTime":"","exportStatus":0,"fullName":"","integralBalance":0,"integralProvisions":0,
        // "oilId":331,"oilShengId":0,"primaryId":51,"provisionsMoney":0,"status":1}
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            OilAmountDistribute oilAmountDistribute = new OilAmountDistribute();
            oilAmountDistribute.setCardNumber(obj.getString("cardId"));
            oilAmountDistribute.setCarNumber(obj.getString("cardHolder"));
            oilAmountDistribute.setOilCardId(obj.getString("oilId"));
            oilAmountDistribute.setReleationId(obj.getString("ID"));
            oilAmountDistributeList.add(oilAmountDistribute);
        }
        Message message = new Message();
        message.what = 0x12;
        handler.sendMessage(message);
    }

    /**
     * 设置操作数据
     */
    private void setOperationData() {

    }

    /**
     * 自定义适配器
     */
    private class OilCardAmountDistributeAdapter extends BaseAdapter {

        private Context context;
        private LayoutInflater inflater;

        private OilCardAmountDistributeAdapter(Context context) {
            this.context = context;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return oilAmountDistributeList.size();
        }

        @Override
        public Object getItem(int position) {
            return oilAmountDistributeList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //item数据
            final OilAmountDistribute oilAmountDistribute = oilAmountDistributeList.get(position);
            TextView tCardUser; //卡用户
            TextView tCarNumber; //车牌号
            TextView tCardNumber; //卡号
            EditText tAmount; //金额
            View view = inflater.inflate(R.layout.distribute_user_item, null);
            tCardUser = (TextView) view.findViewById(R.id.distribute_name);
            tCarNumber = (TextView) view.findViewById(R.id.car_number);
            tCardNumber = (TextView) view.findViewById(R.id.card_id);
            //设置监听
            tAmount = (EditText) view.findViewById(R.id.prediction_distribute_amount);
            tAmount.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                    //设置预分配卡数
                    oilAmountDistribute.setAmount(s.toString());
                    sumCardNum();
                    sumMoneyNum();
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });


            tCardUser.setText(oilAmountDistribute.getCardUser());
            tCarNumber.setText(oilAmountDistribute.getCarNumber());
            tCardNumber.setText(oilAmountDistribute.getCardNumber());
            tAmount.setText(oilAmountDistribute.getAmount());
            return view;
        }
    }

    /**
     * 设置分配总金额
     */
    private void sumMoneyNum() {
        int mon = 0;
        for (OilAmountDistribute oad : oilAmountDistributeList) {
            String amount = oad.getAmount();
            if (!TextUtils.isEmpty(amount)) {
                mon=mon+Integer.parseInt(amount);
            }
        }
        predictionDistributeAmount.setText(mon+"");

    }

    private void sumCardNum() {
        int i = 0;
        for (OilAmountDistribute oad : oilAmountDistributeList) {
            if (!TextUtils.isEmpty(oad.getAmount())) {
                i++;
            }
        }
        distributeCardNumber.setText(i + "");

    }
}
