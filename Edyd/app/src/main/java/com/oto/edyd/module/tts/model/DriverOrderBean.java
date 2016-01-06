package com.oto.edyd.module.tts.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能：司机订单实体类
 * 文件名：com.oto.edyd.module.tts.model.DriverOrderBean.java
 * 创建时间：2016/1/5
 * 作者：yql
 */
public class DriverOrderBean {
    private int id; //ID
    private int primary; //主键ID
    private String controlNum; //调度单号
    private String date; //时间
    private String senderAddress; //发货方地址
    private String receiveAddress; //收货方地址
    private String sender; //发货人
    private String sMobilePhoneNumber; //发货人联系电话
    private String receiver; //收货人
    private String rMobilePhoneNumber; //收货人联系电话
    private Double longitude; //经度
    private Double latitude; //纬度
    private int orderStatus; //订单状态

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPrimary() {
        return primary;
    }

    public void setPrimary(int primary) {
        this.primary = primary;
    }

    public String getControlNum() {
        return controlNum;
    }

    public void setControlNum(String controlNum) {
        this.controlNum = controlNum;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public String getReceiveAddress() {
        return receiveAddress;
    }

    public void setReceiveAddress(String receiveAddress) {
        this.receiveAddress = receiveAddress;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getsMobilePhoneNumber() {
        return sMobilePhoneNumber;
    }

    public void setsMobilePhoneNumber(String sMobilePhoneNumber) {
        this.sMobilePhoneNumber = sMobilePhoneNumber;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getrMobilePhoneNumber() {
        return rMobilePhoneNumber;
    }

    public void setrMobilePhoneNumber(String rMobilePhoneNumber) {
        this.rMobilePhoneNumber = rMobilePhoneNumber;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public int getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(int orderStatus) {
        this.orderStatus = orderStatus;
    }
}
