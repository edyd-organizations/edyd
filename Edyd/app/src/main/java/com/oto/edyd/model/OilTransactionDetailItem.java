package com.oto.edyd.model;

/**
 * Created by yql on 2015/11/3.
 */
public class OilTransactionDetailItem {

    private String oilCategory; //油品分类
    private String unitPrice; //单价
    private String addOilQuantity; //加油量
    private String cardBalance; //卡余额
    private String address; //地址

    public String getOilCategory() {
        return oilCategory;
    }

    public void setOilCategory(String oilCategory) {
        this.oilCategory = oilCategory;
    }

    public String getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(String unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getAddOilQuantity() {
        return addOilQuantity;
    }

    public void setAddOilQuantity(String addOilQuantity) {
        this.addOilQuantity = addOilQuantity;
    }

    public String getCardBalance() {
        return cardBalance;
    }

    public void setCardBalance(String cardBalance) {
        this.cardBalance = cardBalance;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
