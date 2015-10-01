package com.oto.edyd.model;

import android.widget.TextView;

/**
 * Created by yql on 2015/9/30.
 */
public class OrderDetail {

    private int controlId; //订单ID
    private int controlStatus; //订单状态
    private String controlNum; //订单号
    private String orderNumber; //订单号
    private String orderDate; //订单日期
    private int orderStatus; //订单状态
    private String startPoint; //起始点
    private String endPoint; ; //终点
    private String shipper; //发货人
    private String phoneNumber; //发货人联系电话
    private String consignee; //收货人
    private String consigneePhoneNumber; //收货人联系人电话
    private String goodsName; //货物名称
    private String goodsTotalVolume; //货物总体积
    private String goodsTotalQuantity; //货物总数量
    private String goodsTotalWeight; //货物总质量

    private String executeFirstTime; //接单时间
    private String executeFirstDate;
    private String executeSecondTime; //到达装货时间
    private String executeSecondDate;
    private String executeThirdTime; //完成装货时间
    private String executeThirdDate;
    private String executeFourTime; //在途中时间
    private String executeFourDate;
    private String executeFiveTime; //到达卸货时间
    private String executeFiveDate;
    private String executeSixTime; //完成卸货时间
    private String executeSixDate;


    public int getControlId() {
        return controlId;
    }

    public void setControlId(int controlId) {
        this.controlId = controlId;
    }

    public int getControlStatus() {
        return controlStatus;
    }

    public void setControlStatus(int controlStatus) {
        this.controlStatus = controlStatus;
    }

    public String getControlNum() {
        return controlNum;
    }

    public void setControlNum(String controlNum) {
        this.controlNum = controlNum;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public int getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(int orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(String startPoint) {
        this.startPoint = startPoint;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getShipper() {
        return shipper;
    }

    public void setShipper(String shipper) {
        this.shipper = shipper;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getConsignee() {
        return consignee;
    }

    public void setConsignee(String consignee) {
        this.consignee = consignee;
    }

    public String getConsigneePhoneNumber() {
        return consigneePhoneNumber;
    }

    public void setConsigneePhoneNumber(String consigneePhoneNumber) {
        this.consigneePhoneNumber = consigneePhoneNumber;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getGoodsTotalVolume() {
        return goodsTotalVolume;
    }

    public void setGoodsTotalVolume(String goodsTotalVolume) {
        this.goodsTotalVolume = goodsTotalVolume;
    }

    public String getGoodsTotalQuantity() {
        return goodsTotalQuantity;
    }

    public void setGoodsTotalQuantity(String goodsTotalQuantity) {
        this.goodsTotalQuantity = goodsTotalQuantity;
    }

    public String getGoodsTotalWeight() {
        return goodsTotalWeight;
    }

    public void setGoodsTotalWeight(String goodsTotalWeight) {
        this.goodsTotalWeight = goodsTotalWeight;
    }

    public String getExecuteFirstTime() {
        return executeFirstTime;
    }

    public void setExecuteFirstTime(String executeFirstTime) {
        this.executeFirstTime = executeFirstTime;
    }

    public String getExecuteFirstDate() {
        return executeFirstDate;
    }

    public void setExecuteFirstDate(String executeFirstDate) {
        this.executeFirstDate = executeFirstDate;
    }

    public String getExecuteSecondTime() {
        return executeSecondTime;
    }

    public void setExecuteSecondTime(String executeSecondTime) {
        this.executeSecondTime = executeSecondTime;
    }

    public String getExecuteSecondDate() {
        return executeSecondDate;
    }

    public void setExecuteSecondDate(String executeSecondDate) {
        this.executeSecondDate = executeSecondDate;
    }

    public String getExecuteThirdTime() {
        return executeThirdTime;
    }

    public void setExecuteThirdTime(String executeThirdTime) {
        this.executeThirdTime = executeThirdTime;
    }

    public String getExecuteThirdDate() {
        return executeThirdDate;
    }

    public void setExecuteThirdDate(String executeThirdDate) {
        this.executeThirdDate = executeThirdDate;
    }

    public String getExecuteFourTime() {
        return executeFourTime;
    }

    public void setExecuteFourTime(String executeFourTime) {
        this.executeFourTime = executeFourTime;
    }

    public String getExecuteFourDate() {
        return executeFourDate;
    }

    public void setExecuteFourDate(String executeFourDate) {
        this.executeFourDate = executeFourDate;
    }

    public String getExecuteFiveTime() {
        return executeFiveTime;
    }

    public void setExecuteFiveTime(String executeFiveTime) {
        this.executeFiveTime = executeFiveTime;
    }

    public String getExecuteFiveDate() {
        return executeFiveDate;
    }

    public void setExecuteFiveDate(String executeFiveDate) {
        this.executeFiveDate = executeFiveDate;
    }

    public String getExecuteSixTime() {
        return executeSixTime;
    }

    public void setExecuteSixTime(String executeSixTime) {
        this.executeSixTime = executeSixTime;
    }

    public String getExecuteSixDate() {
        return executeSixDate;
    }

    public void setExecuteSixDate(String executeSixDate) {
        this.executeSixDate = executeSixDate;
    }
}
