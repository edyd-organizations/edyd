package com.oto.edyd.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.oto.edyd.service.TimerService;

import java.util.List;

/**
 * Created by yql on 2015/10/15.
 */
public class ServiceUtil {
    public static boolean isServiceRunning(Context context, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceInfos = activityManager.getRunningServices(Constant.RETRIVE_SERVICE_COUNT);

        if(null == serviceInfos || serviceInfos.size() < 1) {
            return false;
        }

        for(int i = 0; i < serviceInfos.size(); i++) {
            if(serviceInfos.get(i).service.getClassName().contains(className)) {
                isRunning = true;
                break;
            }
        }
        //Log.i("ServiceUtil-AlarmManager", className + " isRunning =  " + isRunning);
        return isRunning;
    }

    /**
     * 定时器，订单状态
     * @param context
     * @param controlId
     * @param controlStatus
     */
    public static void invokeTimerPOIService(Context context, String controlId, String controlStatus){
        //Log.i("ServiceUtil-AlarmManager", "invokeTimerPOIService wac called.." );

        PendingIntent alarmSender = null;
        Intent startIntent = new Intent(context, TimerService.class);
        startIntent.putExtra("control_status", controlStatus);
        startIntent.putExtra("control_id", controlId);
        startIntent.setAction(Constant.ALARM_SERVICE_ACTION);

        try {
            alarmSender = PendingIntent.getService(context, 0, startIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        } catch (Exception e) {
           // Log.i("ServiceUtil-AlarmManager", "failed to start " + e.toString());
        }
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), Constant.ELAPSED_TIME, alarmSender);
    }

    /**
     * 定时器，订单状态
     * @param context
     */
    public static void invokeTimerPOIService(Context context){
        //Log.i("ServiceUtil-AlarmManager", "invokeTimerPOIService wac called.." );
        PendingIntent alarmSender = null;
        Intent startIntent = new Intent(context, TimerService.class);
        startIntent.setAction(Constant.ALARM_SERVICE_ACTION);
        // FLAG_UPDATE_CURRENT
        try {
            alarmSender = PendingIntent.getService(context, 0, startIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        } catch (Exception e) {
            // Log.i("ServiceUtil-AlarmManager", "failed to start " + e.toString());
        }
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), Constant.ELAPSED_TIME, alarmSender);
    }

    public static void cancelAlarmManager(Context context){
        //Log.i("ServiceUtil-AlarmManager", "cancleAlarmManager to start ");
        Intent intent = new Intent(context,TimerService.class);
        intent.setAction(Constant.ALARM_SERVICE_ACTION);
        PendingIntent pendingIntent=PendingIntent.getService(context, 0, intent,PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm=(AlarmManager)context.getSystemService(Activity.ALARM_SERVICE);
        alarm.cancel(pendingIntent);
    }
}
