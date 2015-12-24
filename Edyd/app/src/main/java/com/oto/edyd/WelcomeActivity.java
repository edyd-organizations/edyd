package com.oto.edyd;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

/**
 * Created by zfh on 2015/12/24.
 * 新手引导页
 */
public class WelcomeActivity extends FragmentActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        FragmentManager fm = getSupportFragmentManager();
        pager.setAdapter(new FragmentPagerAdapter(fm) {
            @Override
            public Fragment getItem(int position) {
                WelcomeFragment fragment = new WelcomeFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("position", position);
                fragment.setArguments(bundle);
                return fragment;
            }

            @Override
            public int getCount() {
                return 3;
            }
        });
    }
}
