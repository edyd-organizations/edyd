package com.oto.edyd;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by yql on 2015/10/19.
 */
public class InsuranceActivity extends Activity implements View.OnClickListener{
    private LinearLayout back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_insurance);
        initFields();

        back.setOnClickListener(this);
    }

    private void initFields() {
        back = (LinearLayout) findViewById(R.id.insurance_back);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.insurance_back:
                finish();
                break;
        }
    }
}
