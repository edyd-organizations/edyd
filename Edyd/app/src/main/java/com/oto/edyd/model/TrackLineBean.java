package com.oto.edyd.model;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/12/2.
 */
public class TrackLineBean {
    private String controlNum;
    private String driverName;
    private String driverTel;
    private String receiverAddr;
    private String receiverContactPerson;
    private String receiverContactTel;
    private double receiverLat;
    private double receiverLng;
    private String receiverName;
    private String senderAddr;
    private String senderContactPerson;
    private String senderContactTel;
    private double senderLat;
    private double senderLng;
    private String trunckNum;

    private ArrayList<TrackPointBean> traceInfo;

    private String senderName;

    public String getTrunckNum() {
        return trunckNum;
    }
    public void setTrunckNum(String trunckNum) {
        this.trunckNum = trunckNum;
    }


    public String getControlNum() {
        return controlNum;
    }

    public void setControlNum(String controlNum) {
        this.controlNum = controlNum;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverTel() {
        return driverTel;
    }

    public void setDriverTel(String driverTel) {
        this.driverTel = driverTel;
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

    public double getReceiverLat() {
        return receiverLat;
    }

    public void setReceiverLat(double receiverLat) {
        this.receiverLat = receiverLat;
    }

    public double getReceiverLng() {
        return receiverLng;
    }

    public void setReceiverLng(double receiverLng) {
        this.receiverLng = receiverLng;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
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

    public double getSenderLng() {
        return senderLng;
    }

    public void setSenderLng(double senderLng) {
        this.senderLng = senderLng;
    }

    public double getSenderLat() {
        return senderLat;
    }

    public void setSenderLat(double senderLat) {
        this.senderLat = senderLat;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public ArrayList<TrackPointBean> getTraceInfo() {
        return traceInfo;
    }

    public void setTraceInfo(ArrayList<TrackPointBean> traceInfo) {
        this.traceInfo = traceInfo;
    }


}
