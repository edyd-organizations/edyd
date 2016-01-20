package com.oto.edyd.model;

import java.io.Serializable;

/**
 * Created by yql on 2015/11/9.
 */
public class SelectDepartment implements Serializable{
    private String orgId; //ID
    private String orgCode; //组织代码
    private String orgType; //组织类型
    private String tenantId; //租户ID
    private String text; //公司名称

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public String getOrgType() {
        return orgType;
    }

    public void setOrgType(String orgType) {
        this.orgType = orgType;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
