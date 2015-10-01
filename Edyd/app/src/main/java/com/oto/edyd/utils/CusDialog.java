package com.oto.edyd.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by yql on 2015/9/21.
 */
public class CusDialog {

    private String title; //标题
    private String message; //消息
    private String positive; //确定标题
    private String negative; //取消标题
    private Dialog dialog; //对话框对象
    private Context context; //上下文对象

    public CusDialog(Context context, String title, String message, String positive, String negative) {
        this.title = title;
        this.message = message;
        this.positive = positive;
        this. negative = negative;
        this.context = context;
    }

    /**
     * 显示dialog
     */
    private void showDialog() {
        dialog = new AlertDialog.Builder(context).setTitle(title)
                .setMessage(message)
                .setPositiveButton(positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton(negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    /**
     *退出dialog
     */
    private void dismissDialog() {
        dialog.dismiss();
    }
}
