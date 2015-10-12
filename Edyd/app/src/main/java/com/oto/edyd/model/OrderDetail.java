package com.oto.edyd.model;

import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yql on 2015/9/30.
 */
public class OrderDetail implements Serializable{

    private int controlId; //订单ID
    private String controlNum; //订单号
    private String orderDate; //订单日期
    private List<Integer> orderStatusLists; //订单状态
    private String startPoint; //起始点
    private String endPoint; ; //终点
    private String shipper; //发货人
    private String phoneNumber; //发货人联系电话
    private String consignee; //收货人
    private String consigneePhoneNumber; //收货人联系人电话
    private List<String > goodNameLists; //货物名称集
    private String goodsTotalVolume; //货物总体积
    private String goodsTotalQuantity; //货物总数量
    private String goodsTotalWeight; //货物总质量

    List<OrderPerTime> orderPerTimeList; //时间集

    public int getControlId() {
        return controlId;
    }

    public void setControlId(int controlId) {
        this.controlId = controlId;
    }

    public String getControlNum() {
        return controlNum;
    }

    public void setControlNum(String controlNum) {
        this.controlNum = controlNum;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
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

    public List<String> getGoodNameLists() {
        return goodNameLists;
    }

    public void setGoodNameLists(List<String> goodNameLists) {
        this.goodNameLists = goodNameLists;
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

    public List<OrderPerTime> getOrderPerTimeList() {
        return orderPerTimeList;
    }

    public void setOrderPerTimeList(List<OrderPerTime> orderPerTimeList) {
        this.orderPerTimeList = orderPerTimeList;
    }

    public List<Integer> getOrderStatusLists() {
        return orderStatusLists;
    }

    public void setOrderStatusLists(List<Integer> orderStatusLists) {
        this.orderStatusLists = orderStatusLists;
    }
}
