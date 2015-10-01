package com.oto.edyd;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Window;

/**
 * Created by yql on 2015/9/2.
 */
public class SetUpActivity extends FragmentActivity {

    public FragmentManager setUpFragmentManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); //无标题
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_frame);
        initFields(); //初始化数据

        setUpFragmentManager.beginTransaction().replace(R.id.common_frame, new SetUpMainFragment()).commit();
    }


    public void initFields() {
        setUpFragmentManager = getSupportFragmentManager();
    }
}
