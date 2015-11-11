package com.oto.edyd.model;

/**
 * Created by yql on 2015/11/11.
 */
public class AccountAndRole {
    private String textEnterpriseId;
    private String textEnterpriseName;
    private String txOrgCode;

    public String getTextEnterpriseId() {
        return textEnterpriseId;
    }

    public void setTextEnterpriseId(String textEnterpriseId) {
        this.textEnterpriseId = textEnterpriseId;
    }

    public String getTextEnterpriseName() {
        return textEnterpriseName;
    }

    public void setTextEnterpriseName(String textEnterpriseName) {
        this.textEnterpriseName = textEnterpriseName;
    }

    public String getTxOrgCode() {
        return txOrgCode;
    }

    public void setTxOrgCode(String txOrgCode) {
        this.txOrgCode = txOrgCode;
    }

    public String getTxOrgName() {
        return txOrgName;
    }

    public void setTxOrgName(String txOrgName) {
        this.txOrgName = txOrgName;
    }

    private String txOrgName;



}
