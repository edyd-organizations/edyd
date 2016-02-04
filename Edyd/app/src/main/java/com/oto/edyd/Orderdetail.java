package com.oto.edyd;

import java.io.Serializable;

/**
 * Created by xhj on 2015/12/9.
 * 收发货人列表订单详情
 */
public class Orderdetail implements Serializable {
    String controlNum;//调度单号
    String distance;//距离
    Integer orderStatus;//订单状态
    String detailedAddress;//详细地址
    String startAddrProviceAndCity;//起点省份
    String stopAddrProviceAndCity;//终点省份
    String ContactTel;//联系电话
    String ContacrName;//联系人
    String orderNum;//订单号
    Long primaryId;//主键ID
    String controlDate;//操作时间

    public String getControlNum() {
        return controlNum;
    }

    public void setControlNum(String controlNum) {
        this.controlNum = controlNum;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public Integer getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(Integer orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getDetailedAddress() {
        return detailedAddress;
    }

    public void setDetailedAddress(String detailedAddress) {
        this.detailedAddress = detailedAddress;
    }

    public String getStartAddrProviceAndCity() {
        return startAddrProviceAndCity;
    }

    public void setStartAddrProviceAndCity(String startAddrProviceAndCity) {
        this.startAddrProviceAndCity = startAddrProviceAndCity;
    }

    public String getStopAddrProviceAndCity() {
        return stopAddrProviceAndCity;
    }

    public void setStopAddrProviceAndCity(String stopAddrProviceAndCity) {
        this.stopAddrProviceAndCity = stopAddrProviceAndCity;
    }

    public String getContactTel() {
        return ContactTel;
    }

    public void setContactTel(String contactTel) {
        ContactTel = contactTel;
    }

    public String getContacrName() {
        return ContacrName;
    }

    public void setContacrName(String contacrName) {
        ContacrName = contacrName;
    }

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }

    public Long getPrimaryId() {
        return primaryId;
    }

    public void setPrimaryId(Long primaryId) {
        this.primaryId = primaryId;
    }

    public String getControlDate() {
        return controlDate;
    }

    public void setControlDate(String controlDate) {
        this.controlDate = controlDate;
    }
}
