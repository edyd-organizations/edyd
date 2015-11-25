package com.oto.edyd.model;

/**
 * Created by yql on 2015/11/24.
 */
public class TransportDispatch {
    private String primaryId; //主键ID
    private String orderFlowWaterNumber; //订单流水号
    private String startAndEndAddress; //起始结束地址
    private String placeOrderDate; //下单日期
    private String shipperName; //发货方名称

    public String getPrimaryId() {
        return primaryId;
    }

    public void setPrimaryId(String primaryId) {
        this.primaryId = primaryId;
    }

    public String getOrderFlowWaterNumber() {
        return orderFlowWaterNumber;
    }

    public void setOrderFlowWaterNumber(String orderFlowWaterNumber) {
        this.orderFlowWaterNumber = orderFlowWaterNumber;
    }

    public String getStartAndEndAddress() {
        return startAndEndAddress;
    }

    public void setStartAndEndAddress(String startAndEndAddress) {
        this.startAndEndAddress = startAndEndAddress;
    }

    public String getPlaceOrderDate() {
        return placeOrderDate;
    }

    public void setPlaceOrderDate(String placeOrderDate) {
        this.placeOrderDate = placeOrderDate;
    }

    public String getShipperName() {
        return shipperName;
    }

    public void setShipperName(String shipperName) {
        this.shipperName = shipperName;
    }
}
