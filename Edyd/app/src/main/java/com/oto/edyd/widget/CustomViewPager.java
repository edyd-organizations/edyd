package com.oto.edyd.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by yql on 2015/8/26.
 * 自定义ViewPager
 */
public class CustomViewPager extends ViewPager {

    private boolean isScrollable = true; //默认能滑动

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(isScrollable){
            return super.onInterceptTouchEvent(ev);
        } else{
            return isScrollable;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(isScrollable){
            return super.onTouchEvent(ev);
        } else{
            return isScrollable;
        }
    }

    public void setIsScrollable(boolean isScrollable) {
        this.isScrollable = isScrollable;
    }
}
