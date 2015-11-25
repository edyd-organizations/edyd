package com.oto.edyd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.oto.edyd.model.ViolationInfo;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Administrator on 2015/11/5.
 */
public class VioDetailActivity extends Activity {
    ImageView img_number;//违章分数图
    String mnumber;//违章分
    TextView  te_money;//扣分
    TextView te_time;//违章条数
    ArrayList<ViolationInfo> infoList;//违章信息
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.violate_detail_activity);

        init();
        initdata();
    }

    private void init() {
        Intent intent = getIntent();
        infoList = (ArrayList<ViolationInfo>) intent.getSerializableExtra("info");
        img_number = (ImageView) findViewById(R.id.img_number);
        int sum=0;
        int amount=0;
        for (int i=0;i<infoList.size();i++){
            ViolationInfo violationInfo = infoList.get(i);
            String fen = violationInfo.getFen();
            String money = violationInfo.getMoney();
            sum += Integer.parseInt(fen);
            amount += Integer.parseInt(money);
        }

        TextView te_number = (TextView) findViewById(R.id.te_number);
        te_number.setText(String.valueOf(sum));
        mnumber = te_number.getText().toString();
        te_money = (TextView) findViewById(R.id.te_money);
        te_money.setText("￥"+String.valueOf(amount));
        te_time = (TextView) findViewById(R.id.te_time);
        te_time.setText(String.valueOf(infoList.size()));
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = sdf.format(date);
        TextView UpdateTime = (TextView) findViewById(R.id.UpdateTime);
        UpdateTime.setText(dateStr);
        // te_money.setText(info.getMoney());
    }

    private void initdata() {
        imageset();
    }

    private void imageset() {
        switch (mnumber){//根据分数替换图片
            case "0":
                img_number.setImageResource(R.mipmap.twelve_vio);
                break;
            case "1":
                img_number.setImageResource(R.mipmap.eleven_vio);
                break;
            case "2":
                img_number.setImageResource(R.mipmap.ten_vio);
                break;
            case "3":
                img_number.setImageResource(R.mipmap.nine_vio);
                break;
            case "4":
                img_number.setImageResource(R.mipmap.eight_vio);
                break;
            case "5":
                img_number.setImageResource(R.mipmap.seven_vio);
                break;
            case "6":
                img_number.setImageResource(R.mipmap.six_vio);
                break;
            case "7":
                img_number.setImageResource(R.mipmap.five_vio);
                break;
            case "8":
                img_number.setImageResource(R.mipmap.four_vio);
                break;
            case "9":
                img_number.setImageResource(R.mipmap.three_vio);
                break;
            case "10":
                img_number.setImageResource(R.mipmap.two_vio);
                break;
            case "11":
                img_number.setImageResource(R.mipmap.one_vio);
                break;
        }
    }

    public void back(View view){
        finish();
    }
    public void toViolateNum(View view){
        Intent intent=new Intent(this,ViolateNumActivity.class);
        Bundle bundle=new Bundle();
        bundle.putSerializable("info",infoList);
        intent.putExtras(bundle);
        startActivity(intent);

}
        }
