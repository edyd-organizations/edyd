package com.oto.edyd.model;

/**
 * Created by yql on 2015/11/3.
 */
public class OilTransactionDetailItem {

    private String oilType; //油品种类
    private String unitPrice; //单价
    private String addOilNumber; //加油量
    private String cardBalance; //卡余额
    private String transactionAddress; //交易地点

    public String getOilType() {
        return oilType;
    }

    public void setOilType(String oilType) {
        this.oilType = oilType;
    }

    public String getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(String unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getAddOilNumber() {
        return addOilNumber;
    }

    public void setAddOilNumber(String addOilNumber) {
        this.addOilNumber = addOilNumber;
    }

    public String getCardBalance() {
        return cardBalance;
    }

    public void setCardBalance(String cardBalance) {
        this.cardBalance = cardBalance;
    }

    public String getTransactionAddress() {
        return transactionAddress;
    }

    public void setTransactionAddress(String transactionAddress) {
        this.transactionAddress = transactionAddress;
    }
}
