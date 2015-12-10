package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;

/**
 * Created by lbz on 2015/12/8.
 * 发货方历史订单详情
 */

public class ShipperHisOrderDetailActivity extends Activity {

    private TextView executeFirstTime, executeFirstDate, //接单时间
            executeSecondTime, executeSecondDate, //到达装货时间
            executeThirdTime, executeThirdDate, //完成装货时间
            executeFourTime, executeFourDate, //在途中时间
            executeFiveTime, executeFiveDate, //到达卸货时间
            executeSixTime, executeSixDate; //完成卸货时间

    private TextView orderNumber; //订单号
    private ImageView orderStatus; //接单状态
    private TextView startPoint; //起始点
    private TextView shipper; //发货人
    private TextView phoneNumber; //发货人联系电话
    private TextView fromAddress;//从哪到哪
    private TextView toAddress;

    private TextView goodsName, //货物名称
            goodsTotalVolume, //货物总体积
            goodsTotalQuantity, //货物总数量
            goodsTotalWeight; //货物总质量
    private Common common;
    private Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipper_order_detail);
        initField();
        initView();
    }

    private void initView() {
        //线形图时间
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

        //详情的第二部分
        orderNumber = (TextView) findViewById(R.id.orderNumView);//订单号
        startPoint = (TextView) findViewById(R.id.tv_addr_detail); //发货地址
        shipper = (TextView) findViewById(R.id.shipper_name); //发货人
        phoneNumber = (TextView) findViewById(R.id.phone_number_one); //发货人联系电话
        fromAddress = (TextView) findViewById(R.id.fromAddress);//从哪到哪里
        toAddress = (TextView) findViewById(R.id.toAddress);
        //货物信息部分
        goodsName = (TextView) findViewById(R.id.goods_name);
        goodsTotalVolume = (TextView) findViewById(R.id.goods_total_volume);
        goodsTotalQuantity = (TextView) findViewById(R.id.goods_total_quantity);
        goodsTotalWeight = (TextView) findViewById(R.id.goods_total_weight);
    }

    private void initField() {
        mActivity=this;
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
    }

    public void back(View view){
        finish();
    }

}
