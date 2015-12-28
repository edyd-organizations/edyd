package com.oto.edyd.module.usercenter.model;

/**
 * 功能：用户信息实体类
 * 文件名：com.oto.edyd.module.usercenter.model.UserInfo.java
 * 创建时间：2015/12/28
 * 作者：yql
 */
public class UserInfo {
    private String sessionUUID; //用户唯一标示
    private String mobilePhoneNumber; //手机号码
    private int enterpriseId; //企业ID
    private String enterpriseName; //企业名称
    private int tenantId; //租户ID
    private int roleId; //角色ID
    private int accountId; //账户类型ID

    public String getSessionUUID() {
        return sessionUUID;
    }

    public void setSessionUUID(String sessionUUID) {
        this.sessionUUID = sessionUUID;
    }

    public String getMobilePhoneNumber() {
        return mobilePhoneNumber;
    }

    public void setMobilePhoneNumber(String mobilePhoneNumber) {
        this.mobilePhoneNumber = mobilePhoneNumber;
    }

    public int getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(int enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    public String getEnterpriseName() {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName) {
        this.enterpriseName = enterpriseName;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }
}
