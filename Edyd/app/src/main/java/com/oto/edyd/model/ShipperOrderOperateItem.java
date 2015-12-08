package com.oto.edyd.model;

/**
 * 功能：发货方和收货方订单列表项实体
 * 文件名：com.oto.edyd.ShipperOrderOperateActivity.java
 * 创建时间：2015/12/3
 * 作者：yql
 */
public class ShipperOrderOperateItem {

    private String primaryId; //订单主键
    private String orderNumber; //订单号
    private String distance; //距离装货地
    private String startAndEndAddress; //起始和终点地址
    private String endAddress; //终点地址
    private String receiver; //收货人
    private String phoneNumber; //联系电话
    private String orderStatus; //订单状态

    public String getPrimaryId() {
        return primaryId;
    }

    public void setPrimaryId(String primaryId) {
        this.primaryId = primaryId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getStartAndEndAddress() {
        return startAndEndAddress;
    }

    public void setStartAndEndAddress(String startAndEndAddress) {
        this.startAndEndAddress = startAndEndAddress;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
}
