package com.oto.edyd.model;

/**
 * Created by Administrator on 2015/11/26.
 */
public class OrderBean {
    private String orderNum;//订单号
    private String senderProvince;//发货所在省
    private String senderCity;//发货所在的城市
    private String senderAddr;//地址
    private String senderContactPerson;//发货人
    private String senderContactTel;//电话
    private String receiverProvince;
    private String receiverCity;
    private String receiverAddr;
    private String receiverContactPerson;
    private String receiverContactTel;
    private String totalNum;//总数量
    private long primaryId;

    public long getPrimaryId() {
        return primaryId;
    }

    public void setPrimaryId(long primaryId) {
        this.primaryId = primaryId;
    }

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }

    public String getSenderProvince() {
        return senderProvince;
    }

    public void setSenderProvince(String senderProvince) {
        this.senderProvince = senderProvince;
    }

    public String getSenderCity() {
        return senderCity;
    }

    public void setSenderCity(String senderCity) {
        this.senderCity = senderCity;
    }

    public String getSenderAddr() {
        return senderAddr;
    }

    public void setSenderAddr(String senderAddr) {
        this.senderAddr = senderAddr;
    }

    public String getSenderContactPerson() {
        return senderContactPerson;
    }

    public void setSenderContactPerson(String senderContactPerson) {
        this.senderContactPerson = senderContactPerson;
    }

    public String getSenderContactTel() {
        return senderContactTel;
    }

    public void setSenderContactTel(String senderContactTel) {
        this.senderContactTel = senderContactTel;
    }

    public String getReceiverProvince() {
        return receiverProvince;
    }

    public void setReceiverProvince(String receiverProvince) {
        this.receiverProvince = receiverProvince;
    }

    public String getReceiverCity() {
        return receiverCity;
    }

    public void setReceiverCity(String receiverCity) {
        this.receiverCity = receiverCity;
    }

    public String getReceiverAddr() {
        return receiverAddr;
    }

    public void setReceiverAddr(String receiverAddr) {
        this.receiverAddr = receiverAddr;
    }

    public String getReceiverContactPerson() {
        return receiverContactPerson;
    }

    public void setReceiverContactPerson(String receiverContactPerson) {
        this.receiverContactPerson = receiverContactPerson;
    }

    public String getReceiverContactTel() {
        return receiverContactTel;
    }

    public void setReceiverContactTel(String receiverContactTel) {
        this.receiverContactTel = receiverContactTel;
    }

    public String getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(String totalNum) {
        this.totalNum = totalNum;
    }
}
