package com.oto.edyd;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * 功能：登录模块Activity
 * 文件名：com.oto.edyd.LoginActivity.java
 * 创建时间：2015/8/27
 * 作者：yql
 */
public class LoginActivity extends FragmentActivity {

    private FragmentManager eFragmentManager; //Fragment管理器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_frame);
        init(); //初始化数据
        loadLayout(); //加载布局
    }

    /**
     * 初始化数据
     */
    private void init() {
        initFields();
    }

    /**
     * 初始化字段
     */
    private void initFields() {
        eFragmentManager = getSupportFragmentManager();
    }

    /**
     * 加载并初始化数据
     */
    private void loadLayout() {
        eFragmentManager.beginTransaction().replace(R.id.common_frame, new LoginFragment()).commit(); //replace函数参数一为容器，参数二要加载的页面
    }
}
