package com.oto.edyd;


import android.app.Application;

import com.thinkland.sdk.android.JuheSDKInitializer;


public class EdydApplication extends Application {

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		JuheSDKInitializer.initialize(getApplicationContext());
	}


}
