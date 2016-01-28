package com.oto.edyd.module.usercenter.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.oto.edyd.AccountEnterpriseInformation;
import com.oto.edyd.AccountInformationFragment;
import com.oto.edyd.R;
import com.oto.edyd.module.usercenter.fragment.PersonalInfoFragment;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;

/**
 * 功能：显示账户信息（包括个人和企业信息）
 * 文件名：com.oto.edyd.module.usercenter.activity.AccountInformationActivity.java
 * 创建时间：2015/9/10.
 * 作者：yql
 */
public class AccountInformationActivity extends FragmentActivity{
    private FragmentManager eFragmentManager; //布局管理器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_frame);
        init(); //初始化数据
    }

    /**
     * 初始化数据
     */
    private void init() {
        initFields();
        switchAccountTypeInfo(); //切换角色
    }

    /**
     * 初始化字段
     */
    private void initFields() {
        eFragmentManager = getSupportFragmentManager();
    }

    /**
     * 切换账户类型
     */
    private void switchAccountTypeInfo() {
        Common common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        String roleId = common.getStringByKey("role_id");
        if(roleId.equals("")){
            common.showToast(AccountInformationActivity.this, "角色切换错误");
            return;
        }
        switch(Integer.valueOf(roleId)) {
            case 0: //超级管理员
                eFragmentManager.beginTransaction().replace(R.id.common_frame, new AccountInformationFragment()).commit();
                break;
            case 1: //企业管理员
                eFragmentManager.beginTransaction().replace(R.id.common_frame, new AccountEnterpriseInformation()).commit();
                break;
            case 2: //企业员工
                eFragmentManager.beginTransaction().replace(R.id.common_frame, new AccountInformationFragment()).commit();
                break;
            case 3: //个人信息
                eFragmentManager.beginTransaction().replace(R.id.common_frame, new AccountInformationFragment()).commit();
                break;
        }
    }
}
