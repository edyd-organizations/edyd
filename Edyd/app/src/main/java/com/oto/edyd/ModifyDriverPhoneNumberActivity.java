package com.oto.edyd;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yql on 2015/11/26.
 */
public class ModifyDriverPhoneNumberActivity extends Activity implements View.OnClickListener {

    private LinearLayout back; //返回
    private TextView title; //标题
    private TextView save; //保存
    private EditText content; //修改内容

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_enter_info);
        initFields();

        Intent intent = getIntent();
        String txContent = intent.getStringExtra("content");
        title.setText("修改司机电话");
        content.setText(txContent);

        back.setOnClickListener(this);
        save.setOnClickListener(this);
        content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                save.setEnabled(true);
                save.setTextColor(Color.WHITE);
            }
        });

    }

    /**
     * 初始化数据
     */
    private void initFields() {
        back = (LinearLayout) findViewById(R.id.modify_enter_info_back);
        title = (TextView) findViewById(R.id.tv_enter_info_title);
        save = (TextView) findViewById(R.id.bt_enter_info_save);
        content = (EditText) findViewById(R.id.enter_info_item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.modify_enter_info_back: //返回
                finish();
                break;
            case R.id.bt_enter_info_save://保存
                String mPhone = (content.getText().toString()).trim();
                mPhone = mPhone.replaceAll(" ", "");
                if(mPhone != null && mPhone.equals("")) {
                    Toast.makeText(getApplicationContext(), "手机号码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Pattern pattern = Pattern.compile("^(0|86|17951)?(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])[0-9]{8}$"); //匹配手机
                    Matcher matcher = pattern.matcher(mPhone);
                    if(!matcher.matches()) {
                        Toast.makeText(getApplicationContext(), "手机号码格式不对", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                Intent intent = new Intent();
                String txContent = content.getText().toString();
                intent.putExtra("driver_name", txContent);
                setResult(0x40, intent);
                finish();
                break;
        }
    }
}
