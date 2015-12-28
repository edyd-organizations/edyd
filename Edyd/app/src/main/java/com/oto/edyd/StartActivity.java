package com.oto.edyd;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

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
        SharedPreferences preferences = getSharedPreferences("isFirst", 0);
        final boolean isNew = preferences.getBoolean("isfirst", true);
        //第一次登入新手引导页
        if (isNew) {
            Intent intent = new Intent(StartActivity.this, WelcomeActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
        } else {
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
        }
    }
}
