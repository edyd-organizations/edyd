package com.oto.edyd.oil.model;

import java.io.Serializable;

/**
 * 功能：招行订单实体
 * 文件名：com.oto.edyd.pay.model.MerchantsBankOrder.java
 * 创建时间：2015/12/14
 * 作者：yql
 */
public class MerchantsBankOrder implements Serializable {

    private String branchId; //商户开户分行号
    private String coNo; //商户号
    private String billNo; //订单号
    private String amount; //金额
    private String date; //日期
    private String expireTimeSpan; //时间跨度，单位（分钟）
    private String enterpriseId; //公司ID
    private String orgCode; //组织ID
    private String accountId; //账号ID
    private String merchantCode; //校验码

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getCoNo() {
        return coNo;
    }

    public void setCoNo(String coNo) {
        this.coNo = coNo;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getExpireTimeSpan() {
        return expireTimeSpan;
    }

    public void setExpireTimeSpan(String expireTimeSpan) {
        this.expireTimeSpan = expireTimeSpan;
    }

    public String getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(String enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getMerchantCode() {
        return merchantCode;
    }

    public void setMerchantCode(String merchantCode) {
        this.merchantCode = merchantCode;
    }
}
