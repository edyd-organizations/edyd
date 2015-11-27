package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.model.ViolationInfo;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/11/4.
 */
public class ViolateCheckActivity extends Activity implements View.OnClickListener {
    private Common common;
    private String sessionUuid;
    String url;
    String account_id;
    String Violation;//违章查询接口
    private RelativeLayout violate_detail;
    private Context mActivity;
    String number;//违章记录
    long accountId;//账号ID
    long primaryId;//主键id
    String city;//城市代码
    String hphm;//车牌号
    String classno;//车架号
    String engineno;//发动机号
    String hpzl;//车牌类型
    TextView car_number;
    TextView cityName;
    TextView te_number;
    ViolationInfo violation;//违章信息对象
    ArrayList<ViolationInfo> violationList=new ArrayList<ViolationInfo>();//违章列表
    CusProgressDialog dialog;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.violate_check_activity);
      /*  mActivity=this;
        intiView();
        initdata();*/


    }
    @Override
    protected void onStart() {
        super.onStart();
        mActivity=this;
       dialog = new CusProgressDialog(this,"正在加载数据");
        dialog.showDialog();
     /*   common = new Common(this.getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        if (!common.isLogin()) {
            dialog.dismissDialog();
            return;
        }*/
        intiView();
        initdata();
    }
    private void initdata() {
        requestServer();//查询服务器
        //requestPort();//请求接口

    }
/*   @Override
   public boolean onKeyDown(int keyCode, KeyEvent event) {
       if(keyCode == KeyEvent.KEYCODE_BACK){
           dialog.dismissDialog();;
           return false;
       }
       return super.onKeyDown(keyCode, event);
   }*/
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x10: //
                    if (city==null){
                        return;
                    }
                    requestPort();
                    break;
             case 0x20:
                    te_number.setText(String.valueOf(violationList.size()));
                 String s = te_number.getText().toString().trim();
                 if (!("0".equals(s))){
                     findViewById(R.id.no_vio).setVisibility(View.GONE);
                     findViewById(R.id.imagen).setVisibility(View.GONE);
                 }
            }
        }
    };
    private void requestPort() {
            Violation = "http://v.juhe.cn/wz/query?city="+city+"&hphm="+hphm+"&engineno="+engineno+
                    "&key="+"cda8761c6deea0aed83997866754474c";
            OkHttpClientManager.getAsyn(Violation, new OkHttpClientManager.ResultCallback<String>() {
                @Override
                public void onError(Request request, Exception e) {
                    Toast.makeText(ViolateCheckActivity.this, "失败", Toast.LENGTH_SHORT).show();
                    return;
                }

                @Override
                public void onResponse(String response) {
                    //Toast.makeText(ViolateCheckActivity.this, "接口请求成功", Toast.LENGTH_SHORT).show();
                    JSONObject accountTypeJson;
                    try {
                        accountTypeJson=new JSONObject(response);
                        JSONObject result = accountTypeJson.getJSONObject("result");
                        JSONArray lists = result.getJSONArray("lists");
                        violationList.clear();
                        for (int i=0;i<lists.length();i++){
                            JSONObject mlist = lists.getJSONObject(i);
                            String date = mlist.getString("date");//违章时间
                            String area = mlist.getString("area");//违章地点
                            String act = mlist.getString("act");//违章行为
                            String fen = mlist.getString("fen");//违章扣分
                            String money = mlist.getString("money");//违章罚款
                            String handled = mlist.getString("handled");//是否处理
                            violation = new ViolationInfo();
                            violation.setData(date);
                            violation.setArea(area);
                            violation.setAct(act);
                            violation.setFen(fen);
                            violation.setMoney(money);
                            violation.setHandled(handled);
                            violationList.add(violation);
                        }
                       Message message =Message.obtain();
                        message.what = 0x20;
                        handler.sendMessage(message);
                        dialog.dismissDialog();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    private void requestServer() {
        url= Constant.ENTRANCE_PREFIX_v1 +"inquireAppViolation.json?"+"&accountId="+account_id+"&sessionUuid="+sessionUuid;//查询违章记录
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                Toast.makeText(ViolateCheckActivity.this, "失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response) {
                //Toast.makeText(ViolateCheckActivity.this, "服务器查询成功", Toast.LENGTH_SHORT).show();
                JSONObject accountTypeJson;
                JSONArray accountTypeArray;
                try {
                    accountTypeJson=new JSONObject(response);
                    String status = accountTypeJson.getString("status");
                    if(!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        //请求失败
                       // Toast.makeText(ViolateCheckActivity.this, Constant.ACCOUNT_TYPE_INFO_REQUEST_FAIL, Toast.LENGTH_SHORT).show();
                        dialog.dismissDialog();
                        return;
                    }
                    accountTypeArray = accountTypeJson.getJSONArray("rows");
                    if (accountTypeArray.length()==0){
                        dialog.dismissDialog();
                    }
                    JSONObject rowsJson = accountTypeArray.getJSONObject(0);
                    accountId = rowsJson.getLong("accountId");
                    primaryId = rowsJson.getLong("primaryId");
                    city = rowsJson.getString("city");
                    hphm = rowsJson.getString("hphm");
                    classno = rowsJson.getString("classno");
                    engineno = rowsJson.getString("engineno");
                    hpzl = rowsJson.getString("hpzl");
                    cityName.setText(city);
                    car_number.setVisibility(View.VISIBLE);
                    car_number.setText(hphm);
                    Message message =Message.obtain();
                    message.what = 0x10;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void intiView() {
        violate_detail = (RelativeLayout) findViewById(R.id.violate_detail);
        te_number = (TextView) findViewById(R.id.te_number);
        cityName = (TextView) findViewById(R.id.city_violate);
        car_number = (TextView) findViewById(R.id.car_number);
        car_number.setVisibility(View.INVISIBLE);
        number = te_number.getText().toString();
        violate_detail.setOnClickListener(this);
        common = new Common(this.getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        account_id = common.getStringByKey("ACCOUNT_ID");
    }

    public void back(View view) {
        finish();
    }

    public void addCar(View view) {
/*        common = new Common(this.getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        if (!common.isLogin()) {
            Toast.makeText(this, "用户未登录，请先登录", Toast.LENGTH_LONG).show();
            return;
        }*/
        if (city!=null){
            Toast.makeText(this, "您已经绑定过车辆", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(this, ViolateAddCarActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.violate_detail:
                //没有违章记录的时候
                //if ("0".equals(te_number)){
                if ((city==null)){
                    return;
                }
                Intent intent=new Intent(mActivity,VioDetailActivity.class);
                Bundle bundle=new Bundle();
                bundle.putSerializable("info",violationList);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
        }
    }
}
