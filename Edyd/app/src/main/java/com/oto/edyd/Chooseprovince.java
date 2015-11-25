package com.oto.edyd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.oto.edyd.R;

import java.util.ArrayList;

public class Chooseprovince extends Activity {
    public String[] textResources; //省份文字资源
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chooseprovince);
        ListView mlistview = (ListView) findViewById(R.id.listView);
        LinearLayout  back = (LinearLayout) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        textResources = this.getResources().getStringArray(R.array.province);
        ArrayAdapter mAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, textResources);
        final ArrayList<String> list=new ArrayList<String>();
        for (int i=0;i<textResources.length;i++){
            list.add(textResources[i]);
        }
        mlistview.setAdapter(mAdapter);
        //点击获取城市转大写拼音
        mlistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(Chooseprovince.this,ChooseCity.class);
                String provice = list.get(position);
                String cy = PinyinToolkit.cn2Pinyin(provice);
                String data1 = cy.toUpperCase();
                if (data1.equals("XC")){
                    data1="XZ";
                }
                if (data1.equals("ZQ")){
                    data1="CQ";
                }
                if (provice.equals("海南")){
                    data1="HAN";
                }else if (provice.equals("湖南")){
                    data1="HUN";
                }
                intent.putExtra("provice", data1);
                Intent hphm = getIntent();
                String hphm1 = hphm.getStringExtra("hphm");
               // intent.putExtra("hphm1", hphm1);
                Intent data=new Intent();
                data.putExtra("hphm1", hphm1);
                //setResult(0, data);
                startActivityForResult(intent, 0x20);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(resultCode) {
            case 0x30:
                setResult(0x20, data);
                finish();
        }
    }
}
