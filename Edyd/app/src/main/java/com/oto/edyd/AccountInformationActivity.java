package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;

/**
 * Created by yql on 2015/9/10.
 */
public class AccountInformationActivity extends FragmentActivity {

    public FragmentManager accountInformationFragmentManager; //LoginActivity布局管理器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_frame);
        accountInformationFragmentManager = getSupportFragmentManager();
//        if(isEnterpriseAccountType()) {
//            accountInformationFragmentManager.beginTransaction().replace(R.id.common_frame, new AccountEnterpriseInformation()).commit();
//        } else {
//            accountInformationFragmentManager.beginTransaction().replace(R.id.common_frame, new AccountInformationFragment()).commit();
//        }
        switch(isEnterpriseAccountType()) {
            case 0:
                accountInformationFragmentManager.beginTransaction().replace(R.id.common_frame, new AccountInformationFragment()).commit();
                break;
            case 1:
                accountInformationFragmentManager.beginTransaction().replace(R.id.common_frame, new AccountEnterpriseInformation()).commit();
                break;
            case 2:
                accountInformationFragmentManager.beginTransaction().replace(R.id.common_frame, new AccountInformationFragment()).commit();
                break;
            case 3:
                accountInformationFragmentManager.beginTransaction().replace(R.id.common_frame, new AccountInformationFragment()).commit();
                break;
            case 4:
                break;
        }
    }

    /**
     * 判断是否是企业用户
     * @return
     */
    private int isEnterpriseAccountType() {
        Common common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
       // String role = common.getStringByKey(getString(R.string.role_id));
        String role = common.getStringByKey(getString(R.string.role_id));
        if(role.equals("")){
            Toast.makeText(this, "角色错误", Toast.LENGTH_SHORT).show();
            return 4;
        }
        return Integer.valueOf(role);
    }
}
