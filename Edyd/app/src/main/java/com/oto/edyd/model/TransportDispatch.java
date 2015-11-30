package com.oto.edyd.model;

import java.io.Serializable;

/**
 * Created by yql on 2015/11/24.
 */
public class TransportDispatch implements Serializable{

    private String Id;
    private String primaryId;
    private String orderFlowWaterNumber; //订单流水号
    private String distributeOrderNumber; //调度单号
    private String startAndEndAddress; //起始结束地址
    private String placeOrderDate; //下单日期
    private String shipperName; //发货方名称
    private String arriveTime; //到达时间
    private String goodsName; //货物名称
    private String totalNumber; //总数量
    private String totalWeight; //总体重
    private String totalVolume; //总体积

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

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

    public String getArriveTime() {
        return arriveTime;
    }

    public void setArriveTime(String arriveTime) {
        this.arriveTime = arriveTime;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getTotalNumber() {
        return totalNumber;
    }

    public void setTotalNumber(String totalNumber) {
        this.totalNumber = totalNumber;
    }

    public String getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(String totalWeight) {
        this.totalWeight = totalWeight;
    }

    public String getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(String totalVolume) {
        this.totalVolume = totalVolume;
    }

    public String getDistributeOrderNumber() {
        return distributeOrderNumber;
    }

    public void setDistributeOrderNumber(String distributeOrderNumber) {
        this.distributeOrderNumber = distributeOrderNumber;
    }
}
