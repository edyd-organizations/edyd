package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by Administrator on 2015/11/4.
 */
public class ViolateCheckActivity extends Activity implements View.OnClickListener {

    private RelativeLayout violate_detail;
    private Context mActivity;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.violate_check_activity);
        mActivity=this;
        intiView();
    }

    private void intiView() {
        violate_detail = (RelativeLayout) findViewById(R.id.violate_detail);
        violate_detail.setOnClickListener(this);

    }

    public void back(View view) {
        finish();
    }

    public void addCar(View view) {
        Intent intent = new Intent(this, ViolateAddCarActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.violate_detail:
                Intent intent=new Intent(mActivity,VioDetailActivity.class);
                startActivity(intent);
                break;
        }
    }
}
