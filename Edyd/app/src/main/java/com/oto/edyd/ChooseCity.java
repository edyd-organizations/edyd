package com.oto.edyd;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ChooseCity extends Activity {
    //接口数据
    public String url="http://v.juhe.cn/wz/citys?key=cda8761c6deea0aed83997866754474c";
    ListView tlistview;
    Intent intent;
    Mybase mybase;
    CusProgressDialog dialog;
    ArrayList<String> mlist=new ArrayList<String>();//城市的容器
    ArrayList<String> mlistcode=new ArrayList<String>();//城市代码容器
    ArrayList<String> mengineno=new ArrayList<String>();//发动机号容器
    ArrayList<String> mclassno=new ArrayList<String>();//车架号容器
    ArrayList<String> mclass=new ArrayList<String>();//是否需要车架号
    ArrayList<String> mengine=new ArrayList<String>();//是否需要发动机号
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mybase = new Mybase();
            tlistview.setAdapter(mybase);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_city);
        dialog = new CusProgressDialog(this,"正在加载数据");
        dialog.showDialog();
        LinearLayout back = (LinearLayout) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        init();
        initdata();
    }

    private void initdata() {
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {

            @Override
            public void onError(Request request, Exception e) {
//                Toast.makeText(ChooseCity.this, "下载失败", Toast.LENGTH_LONG).show();
                Common.showToast(ChooseCity.this, "下载失败");
            }

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject resule = jsonObject.getJSONObject("result");
                    intent = getIntent();
                    String data = intent.getStringExtra("provice");
                    JSONObject jsonObject1 = resule.getJSONObject(data);
                    JSONArray citys = jsonObject1.getJSONArray("citys");
                    mlist.clear();
                    mlistcode.clear();
                    mclassno.clear();
                    mengineno.clear();
                    mclass.clear();
                    mengine.clear();
                    for (int i = 0; i < citys.length(); i++) {
                        JSONObject jsonObject2 = citys.getJSONObject(i);
                        String city_name = jsonObject2.getString("city_name");
                        String city_code = jsonObject2.getString("city_code");
                        String engineno = jsonObject2.getString("engineno");
                        String classno = jsonObject2.getString("classno");
                        String engine = jsonObject2.getString("engine");//是否需要发动机号
                        String isclass = jsonObject2.getString("class");//是否需要车架号
                        mlist.add(city_name);
                        mlistcode.add(city_code);
                        mengineno.add(engineno);
                        mclassno.add(classno);
                        mclass.add(isclass);
                        mengine.add(engine);
                    }
                    Message msg = Message.obtain();
                    msg.what = 1;
                    handler.sendMessage(msg);
                    dialog.dismissDialog();
                } catch (JSONException e) {
//                    Toast.makeText(ChooseCity.this, "暂时未开通查询", Toast.LENGTH_SHORT).show();
                    Common.showToast(ChooseCity.this, "暂时未开通查询");
                    dialog.dismissDialog();
                    e.printStackTrace();
                }
            }
        });
    }

    private void init() {
        tlistview = (ListView) findViewById(R.id.listView_city);
        tlistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String choosecity = mlist.get(position);//点击城市
                String citycode = mlistcode.get(position);//城市代码
                String engineno_ = mengineno.get(position);//发动机号
                String mclassno_ = mclassno.get(position);//车架号
                String mclass_ = mclass.get(position);//是否需要车架号
                String mengine_ = mengine.get(position);//是否需要发动机号
                Intent data=new Intent();
                data.putExtra("city",choosecity);
                data.putExtra("city_code",citycode);
                data.putExtra("classno",mclassno_);
                data.putExtra("engineno",engineno_);
                data.putExtra("class",mclass_);
                data.putExtra("engine",mengine_);
              /*  Intent hphm = getIntent();
                String hphm1 = hphm.getStringExtra("hphm");
                data.putExtra("hphm1",hphm1);*/
                //startActivity(data);
                setResult(0x30, data);
                finish();
            }


        });
    }
    class Mybase extends BaseAdapter {
        @Override
        public int getCount() {
            return mlist.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View layout = getLayoutInflater().inflate(R.layout.choose_city_item, null);
            TextView ntext = (TextView) layout.findViewById(R.id.moren);
            String xx = mlist.get(position);
            if (mlist.size()==0){
                ntext.setText("城市还未开通");
            }
            ntext.setText(xx);
            return layout;
        }
    }
}
