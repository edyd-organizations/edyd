package com.oto.edyd.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2015/12/1.
 */
public class TrackBean implements Serializable{
    private long primaryId;
    private String controlNum;
    private String truckNum;
    private String reserveNum;
    private String orderDate;
    private int controlStatus;

    public TrackBean() {

    }

    public int getControlStatus() {
        return controlStatus;
    }

    public void setControlStatus(int controlStatus) {
        this.controlStatus = controlStatus;
    }

    public String getReserveNum() {
        return reserveNum;
    }

    public void setReserveNum(String reserveNum) {
        this.reserveNum = reserveNum;
    }


    public long getPrimaryId() {
        return primaryId;
    }

    public void setPrimaryId(long primaryId) {
        this.primaryId = primaryId;
    }

    public String getControlNum() {
        return controlNum;
    }

    public void setControlNum(String controlNum) {
        this.controlNum = controlNum;
    }

    public String getTruckNum() {
        return truckNum;
    }

    public void setTruckNum(String truckNum) {
        this.truckNum = truckNum;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }
}
