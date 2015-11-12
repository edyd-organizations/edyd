package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.model.OilCardInfo;
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
 * Created by yql on 2015/11/12.
 */
public class SelectCardActivity extends Activity implements View.OnClickListener {

    private LinearLayout back; //返回
    private EditText blurContent; //模糊文本
    private TextView search; //搜索
    private ListView cardListView; //卡列表

    private Common common;
    private List<OilCardInfo> oilCardInfoSet = new ArrayList<OilCardInfo>(); //油卡集合

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_card);
        initFields();

        back.setOnClickListener(this);
    }

    /**
     * 初始化数据
     */
    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
        blurContent = (EditText) findViewById(R.id.blur_content);
        search = (TextView) findViewById(R.id.search);
        cardListView = (ListView) findViewById(R.id.card_list);

        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: //返回
                finish();
                break;
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x10:
                    break;
        }
    };

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
                    if (!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        //变更列表数据获取失败
                        Toast.makeText(getApplicationContext(), "我的油卡列表获取失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    addOilArray = addOilJSON.getJSONArray("rows");
                    oilCardInfoSet.clear();
                    if (addOilArray.length() > 0) {
                        for (int i = 0; i < addOilArray.length(); i++) {
                            OilCardInfo oilCardInfo = new OilCardInfo();
                            JSONObject jsonObject = addOilArray.getJSONObject(i);
                            oilCardInfo.setCarId(jsonObject.getString("carId"));
                            oilCardInfo.setCardId(jsonObject.getString("cardId"));
                            oilCardInfo.setCardBalance(jsonObject.getString("cardBalance"));
                            oilCardInfo.setOilBindingDateTime(jsonObject.getString("oilBindingDateTime"));
                            oilCardInfoSet.add(oilCardInfo);
                        }
                    }

                    Message message = new Message();
                    message.what = 0x10;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
}
