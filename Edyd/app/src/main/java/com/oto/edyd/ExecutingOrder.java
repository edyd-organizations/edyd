package com.oto.edyd;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by yql on 2015/9/16.
 */
public class ExecutingOrder extends Activity implements View.OnClickListener{

    private LinearLayout executingOrderBack; //返回
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.executing_order);
        initFields();

        executingOrderBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.executing_order_back:
                finish();
                break;
        }
    }
    private void initFields() {
        executingOrderBack = (LinearLayout) findViewById(R.id.executing_order_back);
    }
}
