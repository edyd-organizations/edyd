package com.oto.edyd.model;

/**
 * Created by yql on 2015/12/3.
 */
public class ShipperOrderOperateItem {

    private String orderFlowNumber; //订单流水号
    private String distance; //距离装货地
    private String startAndEndAddress; //起始和终点地址
    private String endAddress; //终点地址
    private String receiver; //收货人
    private String phoneNumber; //联系电话
    private String orderStatus; //订单状态

    public String getOrderFlowNumber() {
        return orderFlowNumber;
    }

    public void setOrderFlowNumber(String orderFlowNumber) {
        this.orderFlowNumber = orderFlowNumber;
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
