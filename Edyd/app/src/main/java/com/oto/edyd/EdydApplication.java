package com.oto.edyd;


import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.oto.edyd.service.TimerService;
import com.oto.edyd.service.WeatherService;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.thinkland.sdk.android.JuheSDKInitializer;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;


public class EdydApplication extends Application {

	private boolean isRunningTimerService = false;
	private TimerService timerService;
	private List<Activity> activityList = new ArrayList<Activity>();

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		JuheSDKInitializer.initialize(getApplicationContext());
		startTimerService(); //开启定时服务
		this.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
			@Override
			public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
				activityList.add(activity);
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
				if(activityList.size() == 0) {

				}
			}
		});
	}

	/**
	 * 开启定时服务
	 */
	private void startTimerService() {
		Common common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
		if(common.isLogin()) {
			String controlNum = common.getStringByKey("CONTROL_NUM"); //判断是否执行了调度单
			if(controlNum != null && (!controlNum.equals(""))) {
				Intent intent = new Intent(getApplicationContext(), TimerService.class);
				startService(intent);
				bindService(intent, conn, Context.BIND_AUTO_CREATE);
			}
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
