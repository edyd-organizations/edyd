package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.oto.edyd.model.DistributionBean;
import com.oto.edyd.model.TrackBean;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;

/**
 * Created by Administrator on 2015/12/1.
 */
public class ShowTrackActivity extends Activity{
    private Context mActivity;
    private TrackBean bean;
    private String sessionUuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.track_map);
        mActivity = this;
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        bean = (TrackBean) bundle.getSerializable("detailBean");
        if (bean != null) {
            initFields(); //初始化数据



        }
    }

    private void initFields() {
        Common common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
    }
}
