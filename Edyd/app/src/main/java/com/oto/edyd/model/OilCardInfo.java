package com.oto.edyd.model;

import java.io.Serializable;

/**
 * Created by yql on 2015/11/12.
 */
public class OilCardInfo implements Serializable{
    private String carId; //车牌号
    private String cardBalance; //卡余额
    private String cardId; //卡号
    private String spareMoney; //备付金余额
    private String time; //油卡绑定事件

    public String getCarId() {
        return carId;
    }

    public void setCarId(String carId) {
        this.carId = carId;
    }

    public String getCardBalance() {
        return cardBalance;
    }

    public void setCardBalance(String cardBalance) {
        this.cardBalance = cardBalance;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getSpareMoney() {
        return spareMoney;
    }

    public void setSpareMoney(String spareMoney) {
        this.spareMoney = spareMoney;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
