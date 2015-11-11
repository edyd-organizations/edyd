package com.oto.edyd.model;

/**
 * Created by yql on 2015/11/11.
 */
public class Role {
    private int orgCode; //组织代码
    private String roleName; //组织管理员

    public int getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(int orgCode) {
        this.orgCode = orgCode;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
