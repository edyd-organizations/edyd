package com.oto.edyd.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 网络连接问题
 * Created by yql on 2015/9/9.
 */
public class NetWork {

    private Context context; //上下文对象

    public NetWork(Context context) {
        this.context = context;
    }

    /**
     * 检查是否有网络连接
     * @return
     */
    public boolean isHaveInternet(){
        try{
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = manager.getActiveNetworkInfo();
            return (info!=null && info.isConnected());
        } catch (Exception e){
            return false;
        }
    }
}
