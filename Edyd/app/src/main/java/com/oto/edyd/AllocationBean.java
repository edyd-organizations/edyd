package com.oto.edyd;

/**
 * Created by Administrator on 2015/11/13.
 */
public class AllocationBean {
    /**

     "carId" : 车号 //String,
     "cardId" : 卡号 //String,
     "achieveTime" : 完成时间 //String,
     "applyTime" : 申请时间 //String,
     "provisionsMoney" : 分配备付金余额 //Double,

     */
    private String carId;
    private String cardId;
    private String achieveTime;//完成时间 //String,
    private String applyTime;
    private String provisionsMoney;

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

    public String getAchieveTime() {
        return achieveTime;
    }

    public void setAchieveTime(String achieveTime) {
        this.achieveTime = achieveTime;
    }

    public String getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(String applyTime) {
        this.applyTime = applyTime;
    }

    public String getProvisionsMoney() {
        return provisionsMoney;
    }

    public void setProvisionsMoney(String provisionsMoney) {
        this.provisionsMoney = provisionsMoney;
    }
}
