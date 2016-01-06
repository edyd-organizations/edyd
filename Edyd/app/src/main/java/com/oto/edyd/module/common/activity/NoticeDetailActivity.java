package com.oto.edyd.module.common.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.oto.edyd.R;
import com.oto.edyd.module.common.model.Notice;

/**
 * 功能：公告消息详情
 * 文件名：com.oto.edyd.module.common.activity.NoticeDetailActivity.java
 * 创建时间：2016/1/4
 * 作者：yql
 */
public class NoticeDetailActivity extends Activity implements View.OnClickListener{

    //-----------基本View控件--------------
    private LinearLayout back; //返回
    private TextView sender; //发件人
    private TextView time; //发送时间
    private TextView title; //标题
    private TextView content; //正文

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notice_detail);
        init();
    }

    /**
     * 初始化数据
     */
    private void init() {
        initFields();
        initListener();
        Bundle bundle = getIntent().getExtras();
        Notice notice = (Notice) bundle.get("notice");
        sender.setText(notice.getSender());
        time.setText(notice.getTime());
        title.setText(notice.getTitle());
        content.setText(notice.getContent());
    }

    /**
     * 初始化字段
     */
    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
        sender = (TextView) findViewById(R.id.sender);
        time = (TextView) findViewById(R.id.time);
        title = (TextView) findViewById(R.id.title);
        content = (TextView) findViewById(R.id.content);
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: //返回
                finish();
        }
    }
}
