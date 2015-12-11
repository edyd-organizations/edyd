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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.oto.edyd.model.Driver;
import com.oto.edyd.model.OilCardInfo;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yql on 2015/11/26.
 */
public class SelectDriverActivity extends Activity implements View.OnClickListener ,AbsListView.OnScrollListener {

    private LinearLayout back; //返回
    private EditText blurContent; //查询
    private ListView driverList; //司机列表

    private Common common;
    private String sessionUuid;

    private List<Driver> driverSet = new ArrayList<Driver>(); //司机信息集合
    private static final int ROWS = 10;
    private SelectDriverAdapter selectDriverAdapter;
    private int visibleLastIndex = 0; //最后可视项索引
    private boolean loadFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_driver);
        initFields();
        requestDriverList(1, "", 1); //请求司机列表
        back.setOnClickListener(this);
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
                    requestDriverList(1, "", 2);
                } else {
                    requestDriverList(1, content, 2);
                }
            }
        });

        driverList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               // requestDriverPhoneNumber(driverSet.get(position));
                Bundle bundle = new Bundle();
                bundle.putSerializable("driver", driverSet.get(position));
                //Driver driver = (Driver) bundle.getSerializable("driver");
                Intent intent = new Intent();
                intent.putExtras(bundle);
                setResult(0x30, intent);
                finish();
            }
        });
    }

    /**
     * 初始化数据
     */
    private void initFields(){
        back = (LinearLayout) findViewById(R.id.back);
        blurContent = (EditText) findViewById(R.id.blur_content);
        driverList = (ListView) findViewById(R.id.driver_list);
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: //返回
                finish();
                break;
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x18: //司机信息返回
                    selectDriverAdapter = new SelectDriverAdapter(SelectDriverActivity.this);
                    driverList.setAdapter(selectDriverAdapter);
                    break;
                case 0x19: //模糊查询返回
                    selectDriverAdapter.notifyDataSetChanged();
                    break;
                case 0x20: //上拉加载
                    selectDriverAdapter.notifyDataSetChanged();
                    break;
                case 0x21: //返回司机号码
//                    Bundle bundle = msg.getData();
//                    //Driver driver = (Driver) bundle.getSerializable("driver");
//                    Intent intent = new Intent();
//                    intent.putExtras(bundle);
//                    setResult(0x30, intent);
//                    finish();
                    break;
            }
        }
    };

    /**
     * 请求司机信息
     */
    private void requestDriverList(int page, String driverName, final int type) {
        String url = "";
        if(driverName != null && driverName.equals("")) {
            url = Constant.ENTRANCE_PREFIX_v1 + "inquireTruckDriver.json?sessionUuid=" + sessionUuid +"&page=" + page + "&rows=" + ROWS;
        } else {
            url = Constant.ENTRANCE_PREFIX_v1 + "inquireTruckDriver.json?sessionUuid=" + sessionUuid + "&cardPeopleName=" + driverName +
                    "&page=" + page + "&rows=" + ROWS;
        }

        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if(!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        //角色类型请求失败
                        //Toast.makeText(getApplicationContext(), "角色用户请求失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    jsonArray = jsonObject.getJSONArray("rows");
                    JSONObject jsonObjectItem;
                    loadFlag = true;
                    driverSet.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObjectItem = jsonArray.getJSONObject(i);
                        Driver driver = new Driver();
                        driver.setDriverId(jsonObjectItem.getString("driverId"));
                        driver.setCarId(jsonObjectItem.getString("truckNumber"));
                        driver.setIdentityCard(jsonObjectItem.getString("cardId"));
                        driver.setDriverName(jsonObjectItem.getString("realName"));
                        driver.setDriverPhoneNumber(jsonObjectItem.getString("driverMobile"));
                        driverSet.add(driver);
                    }

                    Message message = Message.obtain();
                    if (type == 1) {
                        message.what = 0x18;
                    } else if(type == 2) {
                        message.what = 0x19;
                    }
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 请求司机信息
     */
    private void requestUpLoadDriverList(int page, String driverName, final int type) {
        String url = "";
        if(driverName != null && driverName.equals("")) {
            url = Constant.ENTRANCE_PREFIX_v1 + "inquireTruckDriver.json?sessionUuid=" + sessionUuid +"&page=" + page + "&rows=" + ROWS;
        } else {
            url = Constant.ENTRANCE_PREFIX_v1 + "inquireTruckDriver.json?sessionUuid=" + sessionUuid + "&cardPeopleName=" + driverName +
                    "&page=" + page + "&rows=" + ROWS;
        }

        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if(!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        //角色类型请求失败
                        //Toast.makeText(getApplicationContext(), "角色用户请求失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    jsonArray = jsonObject.getJSONArray("rows");
                    JSONObject jsonObjectItem;
                    loadFlag = true;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObjectItem = jsonArray.getJSONObject(i);
                        Driver driver = new Driver();
                        driver.setDriverId(jsonObjectItem.getString("driverId"));
                        driver.setCarId(jsonObjectItem.getString("truckNumber"));
                        driver.setIdentityCard(jsonObjectItem.getString("cardId"));
                        driver.setDriverName(jsonObjectItem.getString("realName"));
                        driverSet.add(driver);
                    }

                    Message message = Message.obtain();
                    message.what = 0x20;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        int lastIndex = selectDriverAdapter.getCount(); //数据集最后一项的索引
        //int lastIndex = itemsLastIndex + 1; //加上底部的loadMoreIndex项
        if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE  && visibleLastIndex ==lastIndex){
            //如果是自动加载,可以在这里放置异步加载数据的代码
            if(loadFlag) {
                loadFlag = false;
                if(lastIndex % ROWS == 0) {
                    int page = lastIndex / ROWS + 1;
                    String driverName = blurContent.getText().toString();
                    if(driverName == null) {
                        driverName = "";
                    }
                    requestUpLoadDriverList(page, driverName, 3);
                }
            }

        }
    }

    /**
     * 请求司机号码
     */
    private void requestDriverPhoneNumber(final Driver driver) {
        String url = Constant.ENTRANCE_PREFIX_v1 + "inquireDriverPageListDetail.json?sessionUuid=" + sessionUuid + "&ID=" + driver.getDriverId();

        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if(!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        //角色类型请求失败
                        //Toast.makeText(getApplicationContext(), "角色用户请求失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    jsonArray = jsonObject.getJSONArray("rows");
                    JSONObject jsonObjectItem = jsonArray.getJSONObject(0);
                    driver.setDriverPhoneNumber(jsonObjectItem.getString("mobile"));
                    Message message = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("driver", driver);
                    message.setData(bundle);
                    message.what = 0x21;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        //this.visibleItemCount = visibleItemCount;
        visibleLastIndex = firstVisibleItem + visibleItemCount;
    }

    /**
     * 自定义适配器
     */
    private class SelectDriverAdapter extends BaseAdapter {

        private Context context;
        private LayoutInflater inflater;

        private SelectDriverAdapter(Context context) {
            this.context = context;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return driverSet.size();
        }

        @Override
        public Object getItem(int position) {
            return driverSet.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;

            if(convertView == null) {
                viewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.select_driver_list_item, null);
                viewHolder.driverName = (TextView) convertView.findViewById(R.id.driver_name);
                viewHolder.carNumber = (TextView) convertView.findViewById(R.id.car_number);
                viewHolder.identityCard = (TextView) convertView.findViewById(R.id.identity_card);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Driver driver = driverSet.get(position);

            viewHolder.driverName.setText(driver.getDriverName());
            viewHolder.carNumber.setText(driver.getCarId());
            viewHolder.identityCard.setText(driver.getIdentityCard());
            return convertView;
        }
    }

    static class ViewHolder{
        TextView driverName;
        TextView carNumber;
        TextView identityCard;
    }
}
