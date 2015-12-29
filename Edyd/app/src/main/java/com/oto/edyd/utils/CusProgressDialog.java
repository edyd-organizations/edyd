package com.oto.edyd.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oto.edyd.R;

/**
 * Created by yql on 2015/8/31.
 * 自定义对话框
 */
public class CusProgressDialog {

    private Dialog loadingDialog; //自定义对话框

    public CusProgressDialog(Context context, String msg) {
        this.loadingDialog = createLoadingDialog(context, msg);
    }

    public Dialog createLoadingDialog(Context context, String msg) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.loading_dialog, null); //得到加载view
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view); //加载布局
        ImageView loadingDataImage = (ImageView) v.findViewById(R.id.img);
       // TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);
        //加载动画
        Animation loadingDataAnimation = AnimationUtils.loadAnimation(context, R.anim.loading_animation); //加载动画
        LinearInterpolator lin = new LinearInterpolator();
        loadingDataAnimation.setInterpolator(lin);
        loadingDataImage.startAnimation(loadingDataAnimation); //让ImagView显示动画
        //tipTextView.setText(msg); //设置加载信息
        Dialog loadingDialog = new Dialog(context, R.style.LoadingDialog); //创建自定义样式
        loadingDialog.setCancelable(true); //不可以用“返回键”取消
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT)); //设置布局
        return loadingDialog;
    }

    /**
     * 获取Dialog
     * @return
     */
    public Dialog getLoadingDialog() {
        return loadingDialog;
    }

    /**
     * 显示Dialog
     */
    public void showDialog() {
        loadingDialog.show();
    }

    /**
     * 关闭Dialog
     */
    public void dismissDialog() {
        loadingDialog.dismiss();
    }
}
