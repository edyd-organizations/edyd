package com.oto.edyd;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by yql on 2015/11/5.
 */
public class OilCardChangeActivity extends Activity implements View.OnClickListener {

    private LinearLayout back; //返回
    private EditText carID; //车牌号
    private Spinner changeItem; //变更项目
    private EditText changeBefore; //变更前
    private EditText changeAfter; //变更后
    private EditText remark; //备注
    private TextView submit; //提交

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oil_card_changed);
        initFields();

        back.setOnClickListener(this);
    }

    /**
     * 初始化数据
     */
    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
        carID = (EditText) findViewById(R.id.car_id);
        changeItem = (Spinner) findViewById(R.id.change_item);
        changeBefore = (EditText) findViewById(R.id.change_before);
        changeAfter = (EditText) findViewById(R.id.change_after);
        remark = (EditText) findViewById(R.id.remark);
        submit = (TextView) findViewById(R.id.submit);

        isSubmitEnable(); //提交按钮是否可用
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
        }
    }

    /**
     * 验证
     */
    private void verify() {
        String carNumber = carID.getText().toString();
        carNumber = carNumber.replace(" ", "");
        if(carNumber != null && carNumber.equals("")) {
            Toast.makeText(getApplicationContext(), "车牌号格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }


    }

    /**
     * 按钮是否可用
     */
    private void isSubmitEnable() {
        carID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!(s == null)) {
                    String inputContent = s.toString();
                    if (inputContent != null && !(inputContent.equals(""))) {
                        String txChangeItem = (String) changeItem.getSelectedItem();
                        if (!(txChangeItem.equals("请选择"))) {
                            String txChangeAfter = changeAfter.getText().toString();
                            if (txChangeAfter != null && !(txChangeAfter.equals(""))) {
                                submit.setEnabled(true); //设置按钮可用
                                submit.setBackgroundResource(R.drawable.border_corner_login_enable);
                            }
                        }
                    } else {
                        submit.setBackgroundResource(R.drawable.border_corner_login);
                        submit.setEnabled(false); //设置按钮不可用
                    }
                }
            }
        });
        changeAfter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!(s == null)) {
                    String inputContent = s.toString();
                    if (inputContent != null && !(inputContent.equals(""))) {
                        String txCarID = (String) carID.getText().toString();
                        if (txCarID != null && !(txCarID.equals(""))) {
                            String txChangeItem = (String) changeItem.getSelectedItem();
                            if (!(txChangeItem.equals("请选择"))) {
                                submit.setEnabled(true); //设置按钮可用
                                submit.setBackgroundResource(R.drawable.border_corner_login_enable);
                            }
                        }
                    } else {
                        submit.setBackgroundResource(R.drawable.border_corner_login);
                        submit.setEnabled(false); //设置按钮不可用
                    }
                }
            }
        });
        changeItem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String txChangeItem = changeItem.getSelectedItem().toString();
                if(!txChangeItem.equals("请选择")) {
                    String txCarID = carID.getText().toString();
                    if (txCarID != null && !(txCarID.equals(""))) {
                        String txChangeAfter = changeAfter.getText().toString();
                        if (txChangeAfter != null && !(txChangeAfter.equals(""))) {
                            submit.setEnabled(true); //设置按钮可用
                            submit.setBackgroundResource(R.drawable.border_corner_login_enable);
                        }
                    }
                } else {
                    submit.setBackgroundResource(R.drawable.border_corner_login);
                    submit.setEnabled(false); //设置按钮不可用
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
