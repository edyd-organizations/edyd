package com.oto.edyd.model;

/**
 * Created by liubaozhong
 * 发货方历史列表；
 */
public class ShipperHisOrderBean {
    private String controlNum;
    private int distance;
    private String orderNum;
    private int orderStatus;
    private int primaryId;
    private String receiverAddr;
    private String receiverAddrProviceAndCity;
    private String receiverContactTel;
    private String receiverName;
    private String resevedNum;
    private String senderAddr;
    private String senderAddrProviceAndCity;
    private String senderContactTel;
    private String senderName;
    private String controlDate;

    public String getControlDate() {
        return controlDate;
    }

    public void setControlDate(String controlDate) {
        this.controlDate = controlDate;
    }

    public String getControlNum() {
        return controlNum;
    }

    public void setControlNum(String controlNum) {
        this.controlNum = controlNum;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }

    public int getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(int orderStatus) {
        this.orderStatus = orderStatus;
    }

    public int getPrimaryId() {
        return primaryId;
    }

    public void setPrimaryId(int primaryId) {
        this.primaryId = primaryId;
    }

    public String getReceiverAddr() {
        return receiverAddr;
    }

    public void setReceiverAddr(String receiverAddr) {
        this.receiverAddr = receiverAddr;
    }

    public String getReceiverAddrProviceAndCity() {
        return receiverAddrProviceAndCity;
    }

    public void setReceiverAddrProviceAndCity(String receiverAddrProviceAndCity) {
        this.receiverAddrProviceAndCity = receiverAddrProviceAndCity;
    }

    public String getReceiverContactTel() {
        return receiverContactTel;
    }

    public void setReceiverContactTel(String receiverContactTel) {
        this.receiverContactTel = receiverContactTel;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getResevedNum() {
        return resevedNum;
    }

    public void setResevedNum(String resevedNum) {
        this.resevedNum = resevedNum;
    }

    public String getSenderAddr() {
        return senderAddr;
    }

    public void setSenderAddr(String senderAddr) {
        this.senderAddr = senderAddr;
    }

    public String getSenderAddrProviceAndCity() {
        return senderAddrProviceAndCity;
    }

    public void setSenderAddrProviceAndCity(String senderAddrProviceAndCity) {
        this.senderAddrProviceAndCity = senderAddrProviceAndCity;
    }

    public String getSenderContactTel() {
        return senderContactTel;
    }

    public void setSenderContactTel(String senderContactTel) {
        this.senderContactTel = senderContactTel;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
}
