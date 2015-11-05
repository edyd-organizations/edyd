package com.oto.edyd;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by yql on 2015/11/5.
 */
public class OilCardAddDetailActivity extends Activity implements View.OnClickListener {

    private LinearLayout back; //返回

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_oil_card_detail);
        initFields();

        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: //返回
                finish();
                break;
        }
    }
    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
    }
}
