package com.oto.edyd.utils;

import android.content.Context;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import com.oto.edyd.R;

/**
 * 监听输入内容是否超出最大长度，并设置光标位置
 * Created by yql on 2015/9/8.
 */
public class MaxLengthWatcher implements TextWatcher {

    private int limitLen = 0; //最大位数
    private EditText editText = null;
    private Context context;

    /**
     * 构造函数
     * @param limitLen 限制数量大小
     * @param editText 输入文本对象
     * @param context 上下文对象
     */
    public MaxLengthWatcher(int limitLen, EditText editText, Context context) {
        this.limitLen = limitLen;
        this.editText = editText;
        this.context = context;
    }

    /**
     * 输入之前
     * @param s
     * @param start
     * @param count
     * @param after
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    /**
     * 正在输入
     * @param s
     * @param start
     * @param before
     * @param count
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // TODO Auto-generated method stub
//        Editable editable = editText.getText();
//        int len = editable.length();
//
//        if(len > maxLen)
//        {
//            int selEndIndex = Selection.getSelectionEnd(editable);
//            String str = editable.toString();
//            //截取新字符串
//            String newStr = str.substring(0,maxLen);
//            editText.setText(newStr);
//            editable = editText.getText();
//
//            //新字符串的长度
//            int newLen = editable.length();
//            //旧光标位置超过字符串长度
//            if(selEndIndex > newLen)
//            {
//                selEndIndex = editable.length();
//            }
//            //设置新光标所在的位置
//            Selection.setSelection(editable, selEndIndex);
        }

    /**
     * 输入之后
     * @param editable
     */
    @Override
    public void afterTextChanged(Editable editable) {
//        int curLen = editable.length();
//        if(curLen < limitLen) {
//            Selection.setSelection(editable, Selection.getSelectionEnd(editable)); //设置新光标所在位置
//            editTextTip(editText.getId());
//        }
    }

    /**
     * 提示
     */
    public void editTextTip(int editTextId) {
        switch (editTextId) {
            case R.id.register_verification_code: //验证码位数验证
                Toast.makeText(context, "验证码位数必须为4位", Toast.LENGTH_SHORT).show();
                break;
            case R.id.register_user_password: //密码位数验证
                Toast.makeText(context, "密码位数不能低于6位", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                break;
        }
    }
}
