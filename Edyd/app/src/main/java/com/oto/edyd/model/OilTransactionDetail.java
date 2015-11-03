package com.oto.edyd.model;

/**
 * Created by yql on 2015/11/3.
 */
public class OilTransactionDetail {
    private String car; //车牌号
    private String card;// 卡号
    private String time; //状态
    private String balance; //金额

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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }
}
