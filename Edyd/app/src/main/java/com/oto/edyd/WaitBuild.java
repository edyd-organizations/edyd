package com.oto.edyd;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by yql on 2015/10/19.
 */
public class WaitBuild extends Activity implements View.OnClickListener{

    private LinearLayout back; //返回
    private TextView waitBuildTitle; //标题

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wait_build);
        initFields();
        String title = getIntent().getStringExtra("wait_title");
        waitBuildTitle.setText(title);
        back.setOnClickListener(this);
    }

    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
        waitBuildTitle = (TextView) findViewById(R.id.wait_build_title);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
        }
    }
}
