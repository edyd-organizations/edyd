package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yql on 2015/11/27.
 */
public class SelectCarActivity extends Activity implements View.OnClickListener {

    private LinearLayout back; //返回
    private TextView save; //保存
    private EditText tvCarNumber; //车牌号
    private ListView carNumberList; //车牌列表

    private int sPosition;
    private Common common;
    private List<CarInfo> carInfoList = new ArrayList<CarInfo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_car_number);
        initFields();
        requestCarNumberData(); //请求车牌号
        back.setOnClickListener(this);
        save.setOnClickListener(this);
        tvCarNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null) {
                    String content = s.toString();
                    if (content.equals("")) {
                        save.setEnabled(false);
                        save.setTextColor(Color.rgb(153, 153, 153));
                    } else {
                        save.setEnabled(true);
                        save.setTextColor(Color.WHITE);
                    }
                }
            }
        });
        carNumberList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CarInfo carInfo = carInfoList.get(position);
                Intent intent = new Intent();
                intent.putExtra("primary_id", carInfo.getPrimaryId());
                intent.putExtra("car_number", carInfo.getCarNumber());
                setResult(0x20, intent);
                finish();
            }
        });
    }

    /**
     * 初始化数据
     */
    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
        save = (TextView) findViewById(R.id.save_car_number);
        tvCarNumber = (EditText) findViewById(R.id.tv_car_number);
        carNumberList = (ListView) findViewById(R.id.car_number_list);
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: //返回
                finish();
                break;
            case R.id.save_car_number: //保存车辆
                String carNumber = tvCarNumber.getText().toString();
                Pattern pattern = Pattern.compile("^[\\u4e00-\\u9fa5]{1}[A-Z]{1}[A-Z_0-9]{5}$");
                Matcher matcher = pattern.matcher(carNumber);
                if (!matcher.matches()) {
                    Toast.makeText(getApplicationContext(), "车牌号格式不正确", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra("car_number", carNumber);
                setResult(0x21, intent);
                finish();
                break;
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x13: //车牌号返回
                    carNumberList.setAdapter(new SelectCarAdapter(SelectCarActivity.this));
                    break;
            }
        }
    };

    /**
     * 请求车牌号
     */
    private void requestCarNumberData() {
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        String url = Constant.ENTRANCE_PREFIX + "getTruckNumberByTenantId.json?sessionUuid=" + sessionUuid;
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                JSONObject itemJsonObject;
                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if (!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        //角色类型请求失败
                        //Toast.makeText(getApplicationContext(), "角色用户请求失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    jsonArray = jsonObject.getJSONArray("rows");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        itemJsonObject = jsonArray.getJSONObject(i);
                        CarInfo carInfo = new CarInfo();
                        carInfo.setPrimaryId(String.valueOf(itemJsonObject.getInt("primaryId")));
                        carInfo.setCarNumber(itemJsonObject.getString("truckNum"));
                        carInfoList.add(carInfo);
                    }

                    Message message = Message.obtain();
                    message.what = 0x13;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private class SelectCarAdapter extends BaseAdapter{

        private LayoutInflater inflater;

        private SelectCarAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return carInfoList.size();
        }

        @Override
        public Object getItem(int position) {
            return carInfoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null) {
                convertView = inflater.inflate(R.layout.common_list_single_text_item, null);
                viewHolder = new ViewHolder();
                viewHolder.content = (TextView) convertView.findViewById(R.id.common_content);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.content.setText(carInfoList.get(position).getCarNumber());
            return convertView;
        }
    }

    static class ViewHolder{
        TextView content;
    }

    private class CarInfo{
        private String primaryId;
        private String carNumber;

        public String getPrimaryId() {
            return primaryId;
        }

        public void setPrimaryId(String primaryId) {
            this.primaryId = primaryId;
        }

        public String getCarNumber() {
            return carNumber;
        }

        public void setCarNumber(String carNumber) {
            this.carNumber = carNumber;
        }
    }
}
