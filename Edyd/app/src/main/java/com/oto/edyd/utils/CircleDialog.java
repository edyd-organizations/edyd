package com.oto.edyd.utils;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.oto.edyd.R;
import com.oto.edyd.lib.circleprogress.CircleProgress;

/**
 * 功能：圆形loading
 * 文件名：com.oto.edyd.utils.CircleDialog.java
 * 创建时间：2016/1/6
 * 作者：yql
 */
public class CircleDialog {
    private CircleProgress circleProgress; //圆形进度条
    private Dialog circleDialog; //对话框
    private final static long DURATION = 2400; //旋转周期 默认毫秒

    public CircleDialog(Context context, String msg) {
        this.circleDialog = createLoadingDialog(context, msg);
    }
    public CircleDialog(Context context) {
        this.circleDialog = createLoadingDialog(context, "");
    }

    public Dialog createLoadingDialog(Context context, String msg) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View circleView = View.inflate(context, R.layout.circle_progress, null); //得到加载view
        circleProgress = (CircleProgress) circleView.findViewById(R.id.progress); //加载布局
        circleProgress.setDuration(DURATION); //设置旋转周期
        circleDialog = new Dialog(context, R.style.Transparent); //创建自定义样式
        circleDialog.setCancelable(true); //不可以用“返回键”取消
        LinearLayoutCompat.LayoutParams layoutParams = new LinearLayoutCompat.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        circleDialog.setContentView(circleView, layoutParams); //设置布局
        return circleDialog;
    }

    /**
     * 获取Dialog
     * @return
     */
    public Dialog getLoadingDialog() {
        return circleDialog;
    }

    /**
     * 显示Dialog
     */
    public void showDialog() {
        circleDialog.show();
        circleProgress.startAnim();
    }

    /**
     * 关闭Dialog
     */
    public void dismissDialog() {
        circleProgress.stopAnim();
        circleDialog.dismiss();

    }

    /**
     * 设置旋转周期
     * @param duration
     */
    public void setDuration(long duration) {
        circleProgress.setDuration(duration);
    }
}
