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
import android.os.PowerManager;
import android.util.Log;

import com.android.http.RequestManager;
import com.android.volley.toolbox.ImageLoader;
import com.oto.edyd.lib.imageindicator.network.NetworkImageCache;
import com.oto.edyd.module.common.activity.NoticeActivity;
import com.oto.edyd.service.TimerService;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.ServiceUtil;
import com.thinkland.sdk.android.JuheSDKInitializer;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EdydApplication extends Application {

	public static TimerService timerService;
	private List<Activity> activityList = new ArrayList<Activity>();
	private Common common;
	private List<String> controlNumList = new ArrayList<String>(); //用于存储调度单号

	private boolean isServer = false;
	private boolean mIsBound = false; //判断服务是否绑定

	private PendingIntent alarmSender;
	PowerManager powerManager = null;
	PowerManager.WakeLock wakeLock = null;
	private static ImageLoader sImageLoader = null;
	private final NetworkImageCache imageCacheMap = new NetworkImageCache();
	public static PushAgent mPushAgent;

	public static ImageLoader getImageLoader() {
		return sImageLoader;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		RequestManager.getInstance().init(EdydApplication.this);
		sImageLoader = new ImageLoader(RequestManager.getInstance()
				.getRequestQueue(), imageCacheMap);

		powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = this.powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Lock");

		//判断服务是否启动
		JuheSDKInitializer.initialize(getApplicationContext());
		//startTimerService(); //开启定时服务
		this.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
			@Override
			public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
				activityList.add(activity);
				if(activityList.size() == 1) {
					//ServiceUtil.cancelAlarmManager(getApplicationContext());
					//ServiceUtil.invokeTimerPOIService(getApplicationContext());
					wakeLock.acquire();
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
					//ServiceUtil.cancelAlarmManager(getApplicationContext());
					wakeLock.release();
					System.exit(0); //结束整个应用程序
				}
			}
		});


		//定时器
//		Intent intent = new Intent(getApplicationContext(), TimerService.class);
//		intent.setAction("LOCATION_SERVICE_ACTION");
//		alarmSender = PendingIntent.getService(getApplicationContext(), 0x20, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//		AlarmManager am = (AlarmManager)getSystemService(Activity.ALARM_SERVICE);

		//友盟消息推送
		mPushAgent = PushAgent.getInstance(getApplicationContext());
		UmengMessageHandler messageHandler = new UmengMessageHandler() {
			@Override
			public void dealWithCustomMessage(Context context, UMessage uMessage) {
				super.dealWithCustomMessage(context, uMessage);
			}

			@Override
			public void dealWithNotificationMessage(Context context, UMessage uMessage) {
				common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
				if(common.isLogin()) {
					super.dealWithNotificationMessage(context, uMessage);
				}
			}

		};
		UmengNotificationClickHandler umengNotificationClickHandler = new UmengNotificationClickHandler() {
			@Override
			public void dealWithCustomAction(Context context, UMessage uMessage) {
				super.dealWithCustomAction(context, uMessage);
			}

			@Override
			public void openActivity(Context context, UMessage uMessage) {
				super.openActivity(context, uMessage);
				Map<String, String> map = uMessage.extra;
				String messageType = map.get("messageType");
				Intent intent;
				if(messageType.equals(Constant.DRIVER_MESSAGE_TYPE)) { //司机消息
					intent = new Intent(context, OrderOperateActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.putExtra("order",0);
					startActivity(intent);
				} else if(messageType.equals(Constant.ENTERPRISE_MESSAGE_TYPE)) {
					intent = new Intent(context, NoticeActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				}

			}
		};
		mPushAgent.setMessageHandler(messageHandler);
		mPushAgent.setNotificationClickHandler(umengNotificationClickHandler);
		mPushAgent.setMergeNotificaiton(false);  //合并通知消息 true始终只会看到一条消息
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
