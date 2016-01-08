package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zfh on 2015/12/28.
 * 启动页
 */
public class StartActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startactivity);
        //新手引导判断
        Common commonFixed = new Common(getSharedPreferences(Constant.FIXED_FILE, Context.MODE_PRIVATE));
        String isNew = commonFixed.getStringByKey(Constant.FIRST_ENTER); //是否第一次进入
        //第一次登入新手引导页，0-首次登录，1-已有登录
        if (isNew.equals("")) {
            Intent intent = new Intent(StartActivity.this, WelcomeActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            Map<Object, Object> map = new HashMap<Object, Object>();
            map.put(Constant.FIRST_ENTER, "true");
            if(!commonFixed.isSave(map)) {
                commonFixed.showToast(StartActivity.this, "引导页状态保存失败");
            }
            finish();
        } else {
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
        }
    }
}
