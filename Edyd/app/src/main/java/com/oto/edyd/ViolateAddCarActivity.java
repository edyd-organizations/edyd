package com.oto.edyd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by Administrator on 2015/11/5.
 */
public class ViolateAddCarActivity extends Activity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addcar);
        initui();
    }

    private void initui() {
        LinearLayout chooseCity = (LinearLayout) findViewById(R.id.choose_city);
        chooseCity.setOnClickListener(this);
    }

    public void back(View view){
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.choose_city:


        }
    }
}
