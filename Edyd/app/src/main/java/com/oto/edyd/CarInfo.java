package com.oto.edyd;

import java.io.Serializable;

/**
 * Created by xhj on 2015/12/1.
 */
public class CarInfo implements Serializable{
    String driverName;//司机姓名
    String controlNum;//调度单号
    String driverTel;//司机电话
    String trunckNum;//车号
    double slat;//纬度
    double slng;//经度
    String order;//调度单状况
    String operTime;//操作时间
    String address;//地址

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getOperTime() {
        return operTime;
    }

    public void setOperTime(String operTime) {
        this.operTime = operTime;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public double getSlng() {
        return slng;
    }

    public void setSlng(double slng) {
        this.slng = slng;
    }

    public double getSlat() {
        return slat;
    }

    public void setSlat(double slat) {
        this.slat = slat;
    }

    public String getControlNum() {
        return controlNum;
    }

    public void setControlNum(String controlNum) {
        this.controlNum = controlNum;
    }

    public String getDriverTel() {
        return driverTel;
    }

    public void setDriverTel(String driverTel) {
        this.driverTel = driverTel;
    }

    public String getTrunckNum() {
        return trunckNum;
    }

    public void setTrunckNum(String trunckNum) {
        this.trunckNum = trunckNum;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
