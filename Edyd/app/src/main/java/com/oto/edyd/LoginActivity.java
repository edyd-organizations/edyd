package com.oto.edyd;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * Created by yql on 2015/8/27.
 * 登入页Activity
 */
public class LoginActivity extends FragmentActivity {

    public FragmentManager loginFragmentManager; //LoginActivity布局管理器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_frame);
        this.loginFragmentManager = getSupportFragmentManager();
        loginFragmentManager.beginTransaction().replace(R.id.common_frame, new LoginFragment()).commit();
    }
}
