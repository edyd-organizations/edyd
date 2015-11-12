package com.oto.edyd.model;

/**
 * 油卡金额分配
 * Created by yql on 2015/10/30.
 */
public class OilAmountDistribute {

    private String cardUser; //卡用户
    private String carNumber; //车牌号
    private String cardNumber; //卡号
    private String amount; //金额
    private String releationId;//关联id
    private String oilCardId;

    public String getOilCardId() {
        return oilCardId;
    }

    public void setOilCardId(String oilCardId) {
        this.oilCardId = oilCardId;
    }

    public String getReleationId() {
        return releationId;
    }

    public void setReleationId(String releationId) {
        this.releationId = releationId;
    }

    public String getCardUser() {
        return cardUser;
    }

    public void setCardUser(String cardUser) {
        this.cardUser = cardUser;
    }

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

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
