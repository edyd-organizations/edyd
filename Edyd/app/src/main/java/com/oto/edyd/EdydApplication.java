package com.oto.edyd;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.oto.edyd.service.TimerService;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.ServiceUtil;
import com.thinkland.sdk.android.JuheSDKInitializer;

import java.util.ArrayList;
import java.util.List;

public class EdydApplication extends Application {

	public static TimerService timerService;
	private List<Activity> activityList = new ArrayList<Activity>();
	private Common common;
	private List<String> controlNumList = new ArrayList<String>(); //用于存储调度单号

	private boolean isServer = false;
	private boolean mIsBound = false; //判断服务是否绑定

	private PendingIntent alarmSender;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.e("M_SERVICE", "Application_onCreate");
		//判断服务是否启动
		JuheSDKInitializer.initialize(getApplicationContext());
		//startTimerService(); //开启定时服务
		ServiceUtil.invokeTimerPOIService(getApplicationContext());
		this.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
			@Override
			public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
				activityList.add(activity);
				if(activityList.size() == 1) {

				}
			}

			@Override
			public void onActivityStarted(Activity activity) {

			}

			@Override
			public void onActivityResumed(Activity activity) {

			}

			@Override
			public void onActivityPaused(Activity activity) {

			}

			@Override
			public void onActivityStopped(Activity activity) {

			}

			@Override
			public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

			}

			@Override
			public void onActivityDestroyed(Activity activity) {
				activityList.remove(activity);
				if (activityList.size() == 0) {
					//
					// timerService.unbindService(conn);
//					if(mIsBound) {
//						//timerService.unbindService(conn);
//						//timerService.stopTimer();
//						unbindService(conn);
//						mIsBound = false;
//						System.exit(0); //结束整个应用程序
//					}

					ServiceUtil.cancelAlarmManager(getApplicationContext());
					System.exit(0); //结束整个应用程序
				}
			}
		});


		//定时器
//		Intent intent = new Intent(getApplicationContext(), TimerService.class);
//		intent.setAction("LOCATION_SERVICE_ACTION");
//		alarmSender = PendingIntent.getService(getApplicationContext(), 0x20, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//		AlarmManager am = (AlarmManager)getSystemService(Activity.ALARM_SERVICE);
	}

	/**
	 * 开启定时服务
	 */
	private void startTimerService() {
		common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
		if(common.isLogin()) {
			Intent intent = new Intent(getApplicationContext(), TimerService.class);
			//startService(intent);
			bindService(intent, conn, Context.BIND_AUTO_CREATE);
			mIsBound = true;
		}
	}

	ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			timerService = ((TimerService.TimerServiceBinder)service).getService(); //获取Service
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}
	};
}
