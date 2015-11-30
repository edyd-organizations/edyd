package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2015/11/5.
 */
public class ViolateAddCarActivity extends Activity implements View.OnClickListener {
    private Common common;
    private String sessionUuid;
    String account_id;
    String url;
    TextView et_car;   //车型
    String hphm;//车牌号码
    String city;//页面显示的城市
    String classno;//车架号
    String engineno;//发动车号
    TextView te_cicy;//城市
    String classno_;
    String mclass_;//是否需要车架号
    String engineno_;
    String mengine_;//是否需要发动机号
    EditText et_classno;
    EditText et_engineno;
    EditText ed_carnumber;
    String ho;
    String Violation;//违章查询接口
    Button sumbit;
    private String cityName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addcar);
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        account_id = common.getStringByKey("ACCOUNT_ID");
        initui();
    }

    private void initui() {
        LinearLayout chooseCity = (LinearLayout) findViewById(R.id.choose_city);
        sumbit = (Button) findViewById(R.id.submit);
        et_car = (TextView) findViewById(R.id.et_car);
        te_cicy = (TextView) findViewById(R.id.te_cicy);
        et_classno = (EditText) findViewById(R.id.et_classno);
        et_engineno = (EditText) findViewById(R.id.et_engineno);
        chooseCity.setOnClickListener(this);
        sumbit.setOnClickListener(this);
    }
    public void back(View view){
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.choose_city:
               Intent intent=new Intent(this,Chooseprovince.class);
                intent.putExtra("hphm",hphm);
                startActivityForResult(intent, 0x10);
                break;
            case R.id.submit:
                queryServr();//检验车辆是否存在

        }
    }
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x40: //车辆已存在
                    Toast.makeText(getApplicationContext(), "车辆已经注册", Toast.LENGTH_SHORT).show();
                    break;
                case 0x41: //不存在
                    checkout();//检验
                    break;
                case 0x5:
                    requestdata();//保存到服务器。
                    break;
            }
        }
    };
    private void queryServr() {
        Violation= Constant.ENTRANCE_PREFIX_v1 +"inquireAppViolation.json?"+"&accountId="+account_id+"&sessionUuid="+sessionUuid;//查询违章记录
        OkHttpClientManager.getAsyn(Violation, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                Toast.makeText(ViolateAddCarActivity.this, "失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response) {
                JSONObject accountTypeJson;
                JSONArray accountTypeArray;
                try {
                    accountTypeJson = new JSONObject(response);
                    String status = accountTypeJson.getString("status");
                    if (!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        //请求失败
                      //  Toast.makeText(ViolateAddCarActivity.this, Constant.ACCOUNT_TYPE_INFO_REQUEST_FAIL, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    accountTypeArray = accountTypeJson.getJSONArray("rows");
                    Message message = Message.obtain();
                    if (accountTypeArray.length() > 0) {
                        message.what = 0x40; //车辆已经存在
                    } else {
                        message.what = 0x41; //车辆不存在
                    }
                    handler.sendMessage(message);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void checkout() {
        checknumber();//车牌号码匹配
        //checkclassno();//车架号和发动机号
    }

    private void checkclassno() {
       // int clas=Integer.parseInt(classno_);//
        //int eng=Integer.parseInt(engineno_);
        classno = et_classno.getText().toString();
        engineno = et_engineno.getText().toString();
        String clas = String.valueOf(classno.length());
        String eng = String.valueOf(engineno.length());
       if (!(classno_.equals("0"))) {
           if (classno != null && classno.equals("")) {
               Toast.makeText(getApplicationContext(), "车架号不能为空", Toast.LENGTH_SHORT).show();
               return;
           }
           if (!(clas.equals(classno_))) {
               Toast.makeText(getApplicationContext(), "车架号格式不对", Toast.LENGTH_SHORT).show();
               Log.e("classno_", classno_);
               return;
           }
       }
        if (!(engineno_.equals("0"))){
            if (engineno!=null&&engineno.equals("")){
                Toast.makeText(getApplicationContext(), "发动机号号不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!(eng.equals(engineno_))){
                Toast.makeText(getApplicationContext(), "发动机号格式不对", Toast.LENGTH_SHORT).show();
                return;
            }

        }
        requestport();
      //  requestdata();//保存到服务器。
    }

    private void requestport() {
        Violation = "http://v.juhe.cn/wz/query?city="+city+"&hphm="+hphm+"&engineno="+engineno+
                "&key="+"cda8761c6deea0aed83997866754474c";
        OkHttpClientManager.getAsyn(Violation, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                Toast.makeText(ViolateAddCarActivity.this, "失败", Toast.LENGTH_LONG).show();
                return;
            }
            @Override
            public void onResponse(String response) {
                Message message =Message.obtain();
                message.what=0x5;
                handler.sendMessage(message);

            }
        });
    }

    private void requestdata() {
        url =Constant.ENTRANCE_PREFIX_v1 +"insertAppViolation.json?hpzl="+"02"+"&sessionUuid="
                +sessionUuid+"&engineno="+engineno+"&classno="+classno+"&hphm="+hphm+"&city="+city+"&accountId="+account_id;//保存
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                Toast.makeText(ViolateAddCarActivity.this, "失败", Toast.LENGTH_LONG).show();
            }
            @Override
            public void onResponse(String response) {
                finish();
            }
        });
    }


    private void checknumber() {
        ed_carnumber = (EditText) findViewById(R.id.ed_carnumber);
        hphm = ed_carnumber.getText().toString();
        ho = hphm;
        if ( hphm != null && hphm.equals("")) {
            Toast.makeText(getApplicationContext(), "车牌号不能为空", Toast.LENGTH_SHORT).show();
            return;
        }else{
            Pattern pattern = Pattern.compile("^[\\u4e00-\\u9fa5]{1}[A-Z]{1}[A-Z_0-9]{5}$");
            Matcher matcher = pattern.matcher(hphm);
            if (!matcher.matches()) {
                Toast.makeText(getApplicationContext(), "车牌号格式不正确", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        checkclassno();//车架号和发动机号
    }

   @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       switch (resultCode) {
           case 0x20: //城市返回值获取
               cityName = data.getStringExtra("city");
               //  String hphm1 = intent.getStringExtra("hphm1");
               classno_ = data.getStringExtra("classno");
               engineno_ = data.getStringExtra("engineno");
               city = data.getStringExtra("city_code");
               mclass_ = data.getStringExtra("class");
               mengine_= data.getStringExtra("engine");
               te_cicy.setText(cityName);
              cityName = te_cicy.getText().toString();
             if (cityName!=null){
               te_cicy.setText(cityName);
             }if (cityName!=null){
               et_classno.setEnabled(true); //设置按钮可用
               et_engineno.setEnabled(true); //设置按钮可用
               sumbit.setEnabled(true);
               sumbit.setBackgroundResource(R.drawable.border_corner_login_enable);
               if (mclass_.equals("0")){
                et_classno.setHint("可不填");
            }else if ("0".equals(classno)){
                et_classno.setHint("请输入车架号后6位");
             }
            else {
                et_classno.setHint("请输入车架号后"+classno_+"位");
             }
            if (mengine_.equals("0")){
                et_engineno.setHint("可不填");
            }else if (engineno_.equals("0")){
                et_engineno.setHint("请输入发动机后6位");
            }
            else {
                et_engineno.setHint("请输入发动机号后"+engineno_+"位");
            }
          }
               break;
       }
    }
}
