package com.oto.edyd.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yql on 2015/10/7.
 */
public class TimerService extends Service {

    private Timer timer; //定时对象

    @Override
    public void onCreate() {
        super.onCreate();
        Timer timer = new Timer();
        timer.schedule(new TimerGetLongitudeAndLatitude(), 0, 60*1000); //每隔十五秒执行一次
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }

    class TimerGetLongitudeAndLatitude extends TimerTask {
        @Override
        public void run() {
            Message message = new Message();
            message.what = 0x10;
            handler.sendMessage(message);
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0x10) {
                //执行定时操作
                Log.e("TIMER", "timer");
            }
        }
    };

    public class TimerServiceBinder extends Binder {
        //返回本地服务
        public TimerService getService() {
            return TimerService.this;
        }
    }

}
