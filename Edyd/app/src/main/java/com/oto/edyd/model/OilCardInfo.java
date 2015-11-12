package com.oto.edyd.model;

import java.io.Serializable;

/**
 * Created by yql on 2015/11/12.
 */
public class OilCardInfo implements Serializable{
    private String carId; //车牌号
    private String cardBalance; //卡余额
    private String cardId; //卡号
    private String oilBindingDateTime; //油卡绑定时间

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

    public String getOilBindingDateTime() {
        return oilBindingDateTime;
    }

    public void setOilBindingDateTime(String oilBindingDateTime) {
        this.oilBindingDateTime = oilBindingDateTime;
    }
}
