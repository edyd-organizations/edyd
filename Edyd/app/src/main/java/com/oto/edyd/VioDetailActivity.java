package com.oto.edyd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by Administrator on 2015/11/5.
 */
public class VioDetailActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.violate_detail_activity);
    }

    public void back(View view){
        finish();
    }
    public void toViolateNum(View view){
        Intent intent=new Intent(this,ViolateNumActivity.class);
        startActivity(intent);

}
        }
