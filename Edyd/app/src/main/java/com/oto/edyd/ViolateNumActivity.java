package com.oto.edyd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by Administrator on 2015/11/6.
 */
public class ViolateNumActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.violate_num_activity);

        }
    public void toFinepage(View view){
        Intent intent=new Intent(this,FineActivity.class);
        startActivity(intent);

    }
    public void back(View view) {
        finish();
    }
}

