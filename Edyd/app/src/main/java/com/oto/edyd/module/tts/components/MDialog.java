package com.oto.edyd.module.tts.components;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oto.edyd.R;


/**
 * 功能：三按钮dialog
 * 文件名：com.example.yql.tmp.dialog.MDialog.java
 * 创建时间：2016/2/25
 * 作者：yql
 */
public class MDialog extends Dialog implements View.OnClickListener{

    private Context context; //上下文对象
    private String dialogTitle; //标题
    private String firstButtonText; //第一个按钮显示文字
    private String secondButtonText; //第二个按钮显示文字
    private String thirdButtonText; //第三个按钮显示文字
    private ClickListenerInterface clickListenerInterface; //接口对象

    public MDialog(Context context, String dialogTitle, String firstButtonText, String secondButtonText, String thirdButtonText) {
        super(context);
        this.context = context;
        this.dialogTitle = dialogTitle;
        this.firstButtonText = firstButtonText;
        this.secondButtonText = secondButtonText;
        this.thirdButtonText = thirdButtonText;
    }

    public interface ClickListenerInterface {
        public void firstButtonOperate(); //第一个按钮操作
        public void secondButtonOperate(); //第二个按钮操作
        public void thirdButtonOperate(); //第三个按钮操作
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.driver_order_dialog, null);
        setContentView(view);
        //Dialog loadingDialog = new Dialog(context, R.style.dialog); //创建自定义样式
        //loadingDialog.setCancelable(true); //不可以用“返回键”取消
        //loadingDialog.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        //调整窗体大小
        Window window = getWindow();
        window.setTitle(dialogTitle);
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;
        params.dimAmount = 0.5f; //透明度设置
        params.width = dip2px(context, 250); //将250dp转为px
        window.setAttributes(params);
        window.setBackgroundDrawableResource(R.drawable.border_corner_driver_white);

        //标题
//        TextView title = (TextView) view.findViewById(R.id.title);
//        title.setText(dialogTitle);

        //第一个按钮
        LinearLayout llFirstButton = (LinearLayout) view.findViewById(R.id.ll_first_button);
        TextView tvFirstButton = (TextView) view.findViewById(R.id.tv_first_button);
        tvFirstButton.setText(firstButtonText);

        //第二个按钮
        LinearLayout llSecondButton = (LinearLayout) view.findViewById(R.id.ll_second_button);
        TextView tvSecondButton = (TextView) view.findViewById(R.id.tv_second_button);
        tvSecondButton.setText(secondButtonText + "并拍照");

        //第三个按钮
        LinearLayout llThirdButton = (LinearLayout) view.findViewById(R.id.ll_third_button); //取消

        //监听
        llFirstButton.setOnClickListener(this);
        llSecondButton.setOnClickListener(this);
        llThirdButton.setOnClickListener(this);

    }

    /**
     * 对外接口
     * @param clickListenerInterface
     */
    public void setClickListener(ClickListenerInterface clickListenerInterface) {
        this.clickListenerInterface = clickListenerInterface;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_first_button: //第一个按钮
                clickListenerInterface.firstButtonOperate();
                break;
            case R.id.ll_second_button: //第二个按钮
                clickListenerInterface.secondButtonOperate();
                break;
            case R.id.ll_third_button: //第三个按钮
                clickListenerInterface.thirdButtonOperate();
                break;
        }
    }
}
