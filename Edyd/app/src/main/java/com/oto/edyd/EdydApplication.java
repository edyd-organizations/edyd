package com.oto.edyd;


import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.oto.edyd.service.TimerService;
import com.oto.edyd.service.WeatherService;
import com.thinkland.sdk.android.JuheSDKInitializer;

import java.util.TimerTask;


public class EdydApplication extends Application {

	private boolean isRunningTimerService = false;
	private TimerService timerService;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		JuheSDKInitializer.initialize(getApplicationContext());
		startTimerService(); //开启定时服务
	}

	/**
	 * 开启定时服务
	 */
	private void startTimerService() {
		Intent intent = new Intent(getApplicationContext(), TimerService.class);
		startService(intent);
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
	}
	ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			//timerService = ((TimerService.TimerServiceBinder)service).getService(); //获取Service
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}
	};
}
