package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yql on 2015/11/5.
 */
public class OilCardChangeActivity extends Activity implements View.OnClickListener {

    private LinearLayout back; //返回
    private EditText carID; //车牌号
    private Spinner changeItem; //变更项目
    private EditText changeBefore; //变更前
    private EditText changeAfter; //变更后
    private EditText remark; //备注
    private TextView submit; //提交
    private Common common;
    private Map<String, String> itemMap;
    private int txPosition; //位置
    private CusProgressDialog cusProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oil_card_changed);
        initFields();

        back.setOnClickListener(this);
        submit.setOnClickListener(this);
    }

    /**
     * 初始化数据
     */
    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
        carID = (EditText) findViewById(R.id.car_id);
        changeItem = (Spinner) findViewById(R.id.change_item);
        changeBefore = (EditText) findViewById(R.id.change_before);
        changeAfter = (EditText) findViewById(R.id.change_after);
        remark = (EditText) findViewById(R.id.remark);
        submit = (TextView) findViewById(R.id.submit);
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));

        isSubmitEnable(); //提交按钮是否可用
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: //返回
                finish();
                break;
            case R.id.submit: //提交
                verify();
                break;
        }
    }

    /**
     * 验证
     */
    private void verify() {
        String carNumber = carID.getText().toString();
        carNumber = carNumber.replace(" ", "");
        if(carNumber != null && carNumber.equals("")) {
            Toast.makeText(getApplicationContext(), "车牌号格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }

        String txChangeItem = changeItem.getSelectedItem().toString();
        if(txChangeItem.equals("请选择")) {
            Toast.makeText(getApplicationContext(), "请选择变更项目", Toast.LENGTH_SHORT).show();
            return;
        }

        String txChangeAfter = changeAfter.getText().toString();
        if(txChangeAfter == null || txChangeAfter.equals("")) {
            Toast.makeText(getApplicationContext(), "变更资料不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        if(txPosition == 1) {
            Toast.makeText(getApplicationContext(), "挂失备注必填", Toast.LENGTH_SHORT).show();
            return;
        }
        if(txPosition == 4) {
            int length = changeAfter.getText().length();
            if(!(length >= 4 && length <= 6)) {
                Toast.makeText(getApplicationContext(), "密码必须为4到6位整数", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        requestData(carNumber, String.valueOf(txPosition), txChangeAfter);
    }

    /**
     * 按钮是否可用
     */
    private void isSubmitEnable() {
        carID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!(s == null)) {
                    String inputContent = s.toString();
                    if (inputContent != null && !(inputContent.equals(""))) {
                        String txChangeItem = (String) changeItem.getSelectedItem();
                        if (!(txChangeItem.equals("请选择"))) {
                            String txChangeAfter = changeAfter.getText().toString();
                            if (txChangeAfter != null && !(txChangeAfter.equals(""))) {
                                submit.setEnabled(true); //设置按钮可用
                                submit.setBackgroundResource(R.drawable.border_corner_login_enable);
                            }
                        }
                    } else {
                        submit.setBackgroundResource(R.drawable.border_corner_login);
                        submit.setEnabled(false); //设置按钮不可用
                    }
                }
            }
        });
        changeAfter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!(s == null)) {
                    String inputContent = s.toString();
                    if (inputContent != null && !(inputContent.equals(""))) {
                        String txCarID = (String) carID.getText().toString();
                        if (txCarID != null && !(txCarID.equals(""))) {
                            String txChangeItem = (String) changeItem.getSelectedItem();
                            if (!(txChangeItem.equals("请选择"))) {
                                submit.setEnabled(true); //设置按钮可用
                                submit.setBackgroundResource(R.drawable.border_corner_login_enable);
                            }
                        }
                    } else {
                        submit.setBackgroundResource(R.drawable.border_corner_login);
                        submit.setEnabled(false); //设置按钮不可用
                    }
                }
            }
        });
        changeItem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    String carNumber = carID.getText().toString();
                    carNumber = carNumber.replace(" ", "");
                    if (carNumber != null && carNumber.equals("")) {
                        Toast.makeText(getApplicationContext(), "车牌号不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String txChangeItem = changeItem.getSelectedItem().toString();
                    if (!txChangeItem.equals("请选择")) {
                        String txCarID = carID.getText().toString();
                        if (txCarID != null && !(txCarID.equals(""))) {
                            String txChangeAfter = changeAfter.getText().toString();
                            if (txChangeAfter != null && !(txChangeAfter.equals(""))) {
                                submit.setEnabled(true); //设置按钮可用
                                submit.setBackgroundResource(R.drawable.border_corner_login_enable);
                            }
                        }
                        txPosition = position;
                        changeAfter.setText("");
                        requestDropListItem(position); //请求下拉列表类型对应变更前数据
                    } else {
                        submit.setBackgroundResource(R.drawable.border_corner_login);
                        submit.setEnabled(false); //设置按钮不可用
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * 请求数据
     * @param carNumber 车牌号
     * @param txChangeItem 变更项目
     * @param txChangeAfter 变更后
     */
    private void requestData(String carNumber, String txChangeItem, String txChangeAfter) {
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        String txOilID = itemMap.get("oilId");
        String txChangeBefore = changeBefore.getText().toString();
        String txRemark = remark.getText().toString();

        String url = Constant.ENTRANCE_PREFIX + "insertOilChange.json?sessionUuid=" + sessionUuid + "&oilId=" + txOilID + "&cardId=" + carNumber +
                "&changeItem=" + txChangeItem + "&changeBefore=" + txChangeBefore + "&changeAfter=" + txChangeAfter + "&remark=" + txRemark;
        OkHttpClientManager.getAsyn(url, new OilCardChangeResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                JSONObject changeItemJSON;
                JSONArray changeItemArray;
                try {
                    changeItemJSON = new JSONObject(response);
                    String status = changeItemJSON.getString("status");
                    if(!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        //变更列表数据获取失败
                        Toast.makeText(getApplicationContext(), "变更失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(getApplicationContext(), "变更成功", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String str = "";
            switch (msg.what) {
                case 1: //挂失
                    changeAfter.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                    changeBefore.setText("");
                    break;
                case 2: //限车号
                    str = itemMap.get("carId") ;
                    changeAfter.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                    changeBefore.setText(str);
                    break;
                case 3: //限油品
                    str = itemMap.get("oilNum") ;
                    changeAfter.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                    changeBefore.setText(str);
                    break;
                case 4: //卡号密码
                    str = itemMap.get("pwd") ;
                    changeAfter.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                    changeBefore.setText(str);
                    break;
                case 5: //日加油金额
                    str = itemMap.get("everyDayOil") ;
                    changeAfter.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL); //限制小数
                    changeBefore.setText(str);
                    break;
                case 6: //每次加油量
                    str = itemMap.get("everyTimeOil") ;
                    changeAfter.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    changeBefore.setText(str);
                    break;
                case 0x40: //列表无数据
                    changeBefore.setText("");
                    break;
            }
        }
    };

    /**
     * 请求下拉列表类型对应变更前数据
     */
    private void requestDropListItem(final int position) {

        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        String cardID = carID.getText().toString();
        String orgCode = common.getStringByKey(Constant.ORG_CODE);
        String enterpriseID = common.getStringByKey(Constant.ENTERPRISE_ID);


        String url = Constant.ENTRANCE_PREFIX + "inqueryOilCardApp.json?sessionUuid="+sessionUuid+"&carId=" + cardID + "&orgCode=" + orgCode + "&enterpriseId=" + enterpriseID;
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                JSONObject changeItemJSON;
                JSONArray changeItemArray;
                try {
                    changeItemJSON = new JSONObject(response);
                    String status = changeItemJSON.getString("status");
                    if(!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        //变更列表数据获取失败
                        Toast.makeText(getApplicationContext(), "变更列表异常", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Message message = new Message();
                    changeItemArray = changeItemJSON.getJSONArray("rows");
                    if(changeItemArray.length() > 0) {
                        JSONObject jsonObject = changeItemArray.getJSONObject(0);
                        itemMap = new HashMap<String, String>();
                        itemMap.put("carId", jsonObject.getString("carId"));
                        itemMap.put("everyDayOil", jsonObject.getString("everyDayOil"));
                        itemMap.put("everyTimeOil", jsonObject.getString("everyTimeOil"));
                        itemMap.put("oilNum", jsonObject.getString("oilNum"));
                        itemMap.put("pwd", jsonObject.getString("pwd"));
                        itemMap.put("oilId", jsonObject.getString("oilId"));
                        message.what = position;
                    } else {
                        message.what = 0x40;
                    }



                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public abstract class OilCardChangeResultCallback<T> extends OkHttpClientManager.ResultCallback<T>{
        @Override
        public void onBefore() {
            //请求之前操作
            cusProgressDialog = new CusProgressDialog(OilCardChangeActivity.this, "正在变更...");
            cusProgressDialog.getLoadingDialog().show();
        }

        @Override
        public void onAfter() {
            //请求之后要做的操作
            cusProgressDialog.getLoadingDialog().dismiss();
        }
    }
}
