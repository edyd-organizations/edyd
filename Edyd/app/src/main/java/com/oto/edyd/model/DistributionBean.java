package com.oto.edyd.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2015/11/13.
 */
public class DistributionBean implements Serializable{
    /**
     * [ //多条数据
     "cardBalance" : 卡余额 //Double,
     "oilBindingDateTime" : 油卡绑定时间 //String,
     "carId" : 车牌号 //String,
     "cardId" : 卡号 //String,
     "primaryId" : 主键 //Long
     */
    private String carId;
    private String cardId;
    private String oilBindingDateTime;
    private double cardBalance;

    public DistributionBean() {

    }

    public String getCarId() {
        return carId;
    }

    public void setCarId(String carId) {
        this.carId = carId;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getOilBindingDateTime() {
        return oilBindingDateTime;
    }

    public void setOilBindingDateTime(String oilBindingDateTime) {
        this.oilBindingDateTime = oilBindingDateTime;
    }

    public double getCardBalance() {
        return cardBalance;
    }

    public void setCardBalance(double cardBalance) {
        this.cardBalance = cardBalance;
    }
}
