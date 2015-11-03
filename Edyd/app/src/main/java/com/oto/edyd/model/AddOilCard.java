package com.oto.edyd.model;

/**
 * Created by yql on 2015/11/2.
 * 我的加油卡
 */
public class AddOilCard {
    private String carNumber; //车牌号
    private String cardNumber; //卡号
    private String time; //时间
    private String balance; //金额

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
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
