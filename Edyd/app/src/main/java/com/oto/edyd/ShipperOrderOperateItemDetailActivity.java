package com.oto.edyd;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 功能：发货方订单列表项详情
 * 文件名：com.oto.edyd.ShipperOrderOperateItemActivity.java
 * 创建时间：2015/12/8
 * 作者：yql
 */
public class ShipperOrderOperateItemDetailActivity extends Activity implements View.OnClickListener {

    private LinearLayout back; //返回
    private TextView tvFirstTime, //接单
            tvSecondTime, //到达装货
            tvThirdTime, //装货完成
            tvFourTime, //送货在途
            tvFiveTime, //到达收货
            tvSixTime; //收货完成
    private ImageView executeFirstPoint, //接单点
            executeSecondPoint, //到达装货点
            executeThirdPoint, //装货完成点
            executeFourPoint, //送货在途点
            executeFivePoint, //到达收货点
            executeSixPoint; //收货完成点
    private TextView executeFirstTime, executeFirstDate, //接单时间
            executeSecondTime, executeSecondDate, //到达装货时间
            executeThirdTime, executeThirdDate, //完成装货时间
            executeFourTime, executeFourDate, //在途中时间
            executeFiveTime, executeFiveDate, //到达卸货时间
            executeSixTime, executeSixDate; //完成卸货时间
    private TextView executeFirstLine, //第一条线
            executeSecondLine, //第二条线
            executeThirdLine, //第三条线
            executeFourLine, //第四条线
            executeFiveLine, //第五条线
            executeSixLine, //第六条线
            executeSevenLine, //第七条线
            executeEightLine, //第八条线
            executeNineLine, //第九条线
            executeTenLine; //第十条线

    private TextView orderNumber; //订单号
    private TextView distance; //距离装货地
    private ImageView orderStatus; //接单状态
    private TextView startAndEndAddress; //起始结束地址
    private TextView endAddress; //结束地址
    private TextView viewTrack; //查看轨迹
    private TextView consignee; //收货人
    private TextView consigneePhoneNumber; //收货人联系人电话

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shipper_order_operate_item_detail);
        init(); //初始化数据
        requestShipperOrderDetail(); //请求发货方订单详情数据
    }

    /**
     * 初始化数据
     */
    private void init() {
        initFields(); //初始化字段
        initListener(); //初始化监听器
    }

    /**
     * 初始化字段
     */
    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);

        executeFirstPoint = (ImageView) findViewById(R.id.execute_first_point);
        executeSecondPoint = (ImageView) findViewById(R.id.execute_second_point);
        executeThirdPoint = (ImageView) findViewById(R.id.execute_third_point);
        executeFourPoint = (ImageView) findViewById(R.id.execute_four_point);
        executeFivePoint = (ImageView) findViewById(R.id.execute_five_point);
        executeSixPoint = (ImageView) findViewById(R.id.execute_six_point);

        executeFirstTime = (TextView) findViewById(R.id.execute_first_time);
        executeFirstDate = (TextView) findViewById(R.id.execute_first_date);
        executeSecondTime = (TextView) findViewById(R.id.execute_second_time);
        executeSecondDate = (TextView) findViewById(R.id.execute_second_date);
        executeThirdTime = (TextView) findViewById(R.id.execute_third_time);
        executeThirdDate = (TextView) findViewById(R.id.execute_third_date);
        executeFourTime = (TextView) findViewById(R.id.execute_four_time);
        executeFourDate = (TextView) findViewById(R.id.execute_four_date);
        executeFiveTime = (TextView) findViewById(R.id.execute_five_time);
        executeFiveDate = (TextView) findViewById(R.id.execute_five_date);
        executeSixTime = (TextView) findViewById(R.id.execute_six_time);
        executeSixDate = (TextView) findViewById(R.id.execute_six_date);

        executeFirstLine = (TextView) findViewById(R.id.execute_first_line);
        executeSecondLine = (TextView) findViewById(R.id.execute_second_line);
        executeThirdLine = (TextView) findViewById(R.id.execute_third_line);
        executeFourLine = (TextView) findViewById(R.id.execute_four_line);
        executeFiveLine = (TextView) findViewById(R.id.execute_five_line);
        executeSixLine = (TextView) findViewById(R.id.execute_six_line);
        executeSevenLine = (TextView) findViewById(R.id.execute_seven_line);
        executeEightLine = (TextView) findViewById(R.id.execute_eight_line);
        executeNineLine = (TextView) findViewById(R.id.execute_nine_line);
        executeTenLine = (TextView) findViewById(R.id.execute_ten_line);

        tvFirstTime = (TextView) findViewById(R.id.tv_first_time);
        tvSecondTime = (TextView) findViewById(R.id.tv_second_time);
        tvThirdTime = (TextView) findViewById(R.id.tv_third_time);
        tvFourTime = (TextView) findViewById(R.id.tv_four_time);
        tvFiveTime = (TextView) findViewById(R.id.tv_five_time);
        tvSixTime = (TextView) findViewById(R.id.tv_six_time);

        orderNumber = (TextView) findViewById(R.id.order_number);
        distance = (TextView) findViewById(R.id.distance_load_goods);
        orderStatus = (ImageView) findViewById(R.id.order_status);
        startAndEndAddress = (TextView) findViewById(R.id.start_and_end_address);
        endAddress = (TextView) findViewById(R.id.end_address);
        viewTrack = (TextView) findViewById(R.id.view_track);
        consignee = (TextView) findViewById(R.id.receiver);
        consigneePhoneNumber = (TextView) findViewById(R.id.consignee_phone_number); //收货人联系电话
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
                break;
        }
    }

    /**
     * 请求发货方订单详情数据
     */
    private void requestShipperOrderDetail() {
        String url = "";
    }

    /**
     * 设置订单详情数据
     */
    private void setShipperOrderDetail() {}
}
