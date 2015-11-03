package com.oto.edyd.model;

/**
 * Created by yql on 2015/11/3.
 */
public class OilDistributeDetail {

    private String car; //车牌号
    private String card;// 卡号
    private String balance; //金额
    private String status; //状态

    public String getCar() {
        return car;
    }

    public void setCar(String car) {
        this.car = car;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
