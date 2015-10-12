package com.oto.edyd.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.support.v4.net.ConnectivityManagerCompat;

import java.util.Map;

/**
 * Created by yql on 2015/8/27.
 * APP公共基础类
 */
public class Common {

    private CustomSharedPreferences cusSharedPreferences;

    public Common(){}

    public Common(SharedPreferences sharedPreferences) {
        this.cusSharedPreferences = new CustomSharedPreferences(sharedPreferences);
    }

    /**
     * 验证用户是否登入
     * @return
     */
    public boolean isLogin() {
        return cusSharedPreferences.contains(Constant.SESSION_UUID);
    }

    /**
     * 验证是否保存成功
     * @param map
     * @return
     */
    public boolean isSave(Map<Object, Object> map) {
        return cusSharedPreferences.savePreferences(map);
    }

    /**
     * 获取偏好中的字符串值
     * @param key
     * @return
     */
    public String getStringByKey (String key) {
        return cusSharedPreferences.getPreferencesStringByKey(key);
    }
    public int getIntByKey(String key) {
        return cusSharedPreferences.getPreferencesIntByKey(key);
    }

    /**
     * 清除账户信息
     * @return
     */
    public boolean isClearAccount() {
        return cusSharedPreferences.clearAccount();
    }

    /**
     * 判断网络连通性
     * @param context
     * @return
     */
    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager netWorkManager = (ConnectivityManager )context.getSystemService(context.CONNECTIVITY_SERVICE);
        if(netWorkManager == null ){
            return false;
        }
        boolean isActive = netWorkManager.getActiveNetworkInfo().isAvailable();
        return isActive;
    }
}
