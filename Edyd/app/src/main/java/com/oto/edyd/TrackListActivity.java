package com.oto.edyd;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

/**
 * Created by Administrator on 2015/11/30.
 */
public class TrackListActivity extends Activity {
    private ListView lv_track;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.track_list_activity);
        initfield();
    }

    private void initfield() {
        lv_track= (ListView) findViewById(R.id.lv_track);

    }

    public void back(View view){
        finish();
    }
}
