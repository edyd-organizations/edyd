package com.oto.edyd.widget;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

/**
 * Created by yql on 2015/9/21.
 */
public class CusOnClickListener implements View.OnClickListener {

    private Context context;
    private TextView textView;
    private int position;

    public CusOnClickListener(Context context, TextView textView, int position){
        this.textView = textView;
        this.position = position;
    }
    @Override
    public void onClick(View v) {
        //textView.setOnClickListener();
    }
}
