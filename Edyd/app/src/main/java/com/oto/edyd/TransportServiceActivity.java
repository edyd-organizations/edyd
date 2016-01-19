package com.oto.edyd;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.oto.edyd.module.tts.fragment.TransportUndertakeFragment;

/**
 * Created by yql on 2015/9/17.
 */
public class TransportServiceActivity extends FragmentActivity{

    public FragmentManager transportServiceFragmentManager; //布局管理器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_frame);
        transportServiceFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = transportServiceFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.common_frame, new TransportUndertakeFragment());
        //fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}