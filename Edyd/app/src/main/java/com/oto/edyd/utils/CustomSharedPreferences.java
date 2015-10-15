package com.oto.edyd.utils;

import android.content.SharedPreferences;

import java.io.File;
import java.util.Map;

/**
 * Created by yql on 2015/8/27.
 * 封装SharedPreferences操作类
 */
public class CustomSharedPreferences {

    private SharedPreferences sharedPreferences; //偏好类
    public String DATA_URL = "/data/data/";

    public CustomSharedPreferences(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    /**
     * 检索偏好设置中的字符串
     * @param key
     * @return
     */
    public String getPreferencesStringByKey(String key) {
        return sharedPreferences.getString(key, "");
    }

    /**
     * 检索偏好设置中的整数
     * @param key
     * @return
     */
    public int getPreferencesIntByKey(String key) {
        return sharedPreferences.getInt(key, 0);
    }

    /**
     * 检索偏好设置中的浮点数
     * @param key
     * @return
     */
    public Float getPreferencesFloatByKey(String key) {
        return sharedPreferences.getFloat(key, 0);
    }

    /**
     * 保存偏好设置，如果有存在覆盖保存
     * @param map 偏好key value
     */
    public boolean savePreferences(Map<Object, Object> map){
        SharedPreferences.Editor editor = sharedPreferences.edit(); //获取编辑器
        for(Map.Entry<Object, Object> entry : map.entrySet()) {
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();
            editor.putString(key, value);
        }
        return editor.commit(); //提交修改
    }


    /**
     * 检查偏好key是否存在
     * @param strSets
     * @return
     */
    public boolean contains(String...strSets) {
        boolean res = true;
        if(strSets != null) {
            for(String str : strSets) {
                res = res && sharedPreferences.contains(str);
            }
        } else {
            res = false;
        }
        return  res;
    }

    /**
     * 清除账户信息
     */
    public boolean clearAccount() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        return editor.commit();
    }

    /**
     * 删除SharedPreferences文件
     * @param packName
     */
    public boolean deleteSharedPreferencesFile(String packName, String fileName) {
        File file = new File(DATA_URL + packName + "/shared_prefs", fileName);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

}
