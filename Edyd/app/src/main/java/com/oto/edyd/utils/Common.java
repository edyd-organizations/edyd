package com.oto.edyd.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.support.v4.net.ConnectivityManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.oto.edyd.model.TrackLineBean;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * 获取boolean值
     * @return
     */
    public boolean getBooleanByKey(String key) {
        return cusSharedPreferences.getPreferencesBooleanByKey(key);
    }

    /**
     * 清除账户信息
     * @return
     */
    public boolean isClearAccount() {
        return cusSharedPreferences.clearAccount();
    }

    /**
     * 删除SharedPreferences文件
     * @param packName
     */
    public boolean isDeleteSharedPreferencesFile(String packName, String fileName) {
        return cusSharedPreferences.deleteSharedPreferencesFile(packName, fileName);
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



    public static void printLog(String content) {
        if (content != null) {
            Log.i(android.os.Build.MODEL, content);
        }

    }

    public static void printErrLog(String content) {
        if (content != null) {
                Log.e(android.os.Build.MODEL, content);
        }
    }

    public static void printLog(String tag, String content) {
        if (content != null) {
            Log.i(tag, content);
        }
    }



    public static boolean checkDataIsJson(String value) {
        try {
            new JSONObject(value);
        } catch (JSONException e) {
            return false;
        }
        return true;
    }
    public static String createJsonString(Object value)
    {
        Gson gson = new Gson();
        String str = gson.toJson(value);
        return str;
    }

    public static String readFromStream(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = is.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        is.close();
        String result = baos.toString();
        baos.close();
        return result;
    }

    public static void showToast(Context context, String Msg) {
        Toast.makeText(context.getApplicationContext(), Msg, Toast.LENGTH_SHORT).show();
    }

    public static void showToastlong(Context context, String Msg) {
        Toast.makeText(context.getApplicationContext(), Msg, Toast.LENGTH_LONG).show();
    }

    public static String getStringByUrl(String urlStr) {
        HttpURLConnection urlConnection = null;
        String result = "";

        try {
            URL url = new URL(urlStr);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(30000);
            urlConnection.setReadTimeout(30000);
            InputStream in = new BufferedInputStream(
                    urlConnection.getInputStream());
            result = readFromStream(in);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return result;
    }

    public static boolean hasSdcard() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isEmail(String eml) {
        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern eml_p = Pattern.compile(str);
        Matcher eml_m = eml_p.matcher(eml);

        return eml_m.matches();
    }


    public static boolean isPhoneNum(String phone) {
        Pattern p = Pattern.compile("^(13|15|18|14)[0-9]{9}$");
        Matcher m = p.matcher(phone);
        return m.find();
    }

    /**
     * 把json字符窜转化成对象；
     * @param jsonData
     * @return
     */
    static public TrackLineBean readJsonToCommandObject(String jsonData) {
        if (jsonData == null)
            return null;
        if (checkDataIsJson(jsonData) == false)
            return null;
        Gson gson = new Gson();
        TrackLineBean commandObject = null;
        try {
            commandObject = gson.fromJson(jsonData, TrackLineBean.class);
        } catch (Exception ex) {
            Common.printErrLog("解析jsonData=" + jsonData);
            ex.printStackTrace();
        }
        return commandObject;
    }

}
