package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yql on 2015/10/27.
 */
public class OilCardApplicationActivity extends Activity implements View.OnClickListener{

    private LinearLayout back; //返回

    private EditText carId; //车牌号
    private EditText setPassword; //设置密码
    private EditText phoneNumber; //联系人电话
    private EditText limitOil; //限定油品
    private EditText limitAddOilAmount; //限定每次加油量
    private EditText limitDailyOilAmount; //限日加油金额
    private Button submit; //提交
    private CheckBox isNeedPassword; //是否需要密码
    private CheckBox isSmsRemind; //是否需要短信提醒

    private CusProgressDialog oilDialog; //过度

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oil_card_application);
        initFields();

        submit.setOnClickListener(this);
        back.setOnClickListener(this);
    }

    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
        carId = (EditText) findViewById(R.id.car_id);
        setPassword = (EditText) findViewById(R.id.set_password);
        phoneNumber = (EditText) findViewById(R.id.phone_number);
        limitOil = (EditText) findViewById(R.id.limit_oil);
        limitAddOilAmount = (EditText) findViewById(R.id.limit_add_oil_amount);
        limitDailyOilAmount = (EditText) findViewById(R.id.limit_daily_oil_amount);
        isNeedPassword = (CheckBox) findViewById(R.id.is_need_password);
        isSmsRemind = (CheckBox) findViewById(R.id.is_sms_remind);
        submit = (Button) findViewById(R.id.submit);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submit:
                verify();
                break;
            case R.id.back:
                finish();
        }
    }

    /**
     * 校验表单
     */
    private void verify() {
        boolean isCheckedNeedPassword = isNeedPassword.isChecked(); //检查是否需要密码
        boolean isCheckedSmsRemind = isSmsRemind.isChecked(); //是否短信提醒
        String carNumber = carId.getText().toString();

        carNumber = carNumber.replace(" ", "");
        if(carNumber != null && carNumber.equals("")) {
            Toast.makeText(getApplicationContext(), "车牌号格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }

        if(isCheckedNeedPassword) {
            String password = (setPassword.getText().toString()).trim();
            password = password.replaceAll(" ", "");
            if(password != null && password.equals("")) {
                Toast.makeText(getApplicationContext(), "密码已设置不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if(isCheckedSmsRemind) {
            String phone = (phoneNumber.getText().toString()).trim();
            phone = phone.replaceAll(" ", "");
            if(phone != null && phone.equals("")) {
                Toast.makeText(getApplicationContext(), "手机已设置不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        applyOilCard();

    }
    /**
     * 申请油卡
     */
    private void applyOilCard() {
        int is_sms, is_need_password;
        String car_id = carId.getText().toString();
        String set_password = setPassword.getText().toString();
        String mobile = phoneNumber.getText().toString();
        String limit_oil = limitOil.getText().toString();
        String every_add_oil = limitAddOilAmount.getText().toString();
        String every_daily_oil_amount = limitDailyOilAmount.getText().toString();
        if(isSmsRemind.isChecked()) {
            is_sms = 1;
        } else {
            is_sms = 2;
        }

        if(isNeedPassword.isChecked()) {
            is_need_password = 1;
        } else {
            is_need_password = 2;
        }

        Common common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        String account_id = common.getStringByKey("ACCOUNT_ID");
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);

        String url = Constant.ENTRANCE_PREFIX + "insertOilCard.json?carId="+car_id+"&sessionUuid="+sessionUuid+"&setPwd="+String.valueOf(is_need_password)+"&pwd="+set_password+
                "&setMessage="+is_sms+"&mobile="+mobile+"&oilNum="+limit_oil+"&everyTimeOil="+every_add_oil+"&everyDayOil="+every_daily_oil_amount+
                "&realAccountId="+account_id+"&licencePic=null";

        OkHttpClientManager.getAsyn(url, new ApplyResultCallback<String>() {

            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                //请求成功
                JSONObject oilSON;
                try {
                    oilSON = new JSONObject(response);
                    if (!oilSON.getString("status").equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        //验证申请油品是否成功
                        Toast.makeText(getApplicationContext(), Constant.INVALID_USERNAME_PASSWORD, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(getApplicationContext(),"油品申请操作成功", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public abstract class ApplyResultCallback<T> extends OkHttpClientManager.ResultCallback<T>{
        @Override
        public void onBefore() {
            //请求之前操作
            oilDialog = new CusProgressDialog(OilCardApplicationActivity.this, "正在申请...");
            oilDialog.getLoadingDialog().show();
        }

        @Override
        public void onAfter() {
            //请求之后要做的操作
            oilDialog.getLoadingDialog().dismiss();
        }
    }
}
