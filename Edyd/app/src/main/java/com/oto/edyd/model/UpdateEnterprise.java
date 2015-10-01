package com.oto.edyd.model;

import java.io.Serializable;

/**
 * Created by yql on 2015/9/15.
 */
public class UpdateEnterprise implements Serializable{
    private String intro; //公司简介
    private String address; //公司地址
    private String conMobile; //联系人电话
    private String contacts; //企业联系人
    private String enMobile; //企业联系电话
    private String enterpriseName; //企业名称

    private String title; //标题
    private int position; //索引
    private String content; //内容
    private int confirmStatus; //认证状态

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getConMobile() {
        return conMobile;
    }

    public void setConMobile(String conMobile) {
        this.conMobile = conMobile;
    }

    public String getContacts() {
        return contacts;
    }

    public void setContacts(String contacts) {
        this.contacts = contacts;
    }

    public String getEnMobile() {
        return enMobile;
    }

    public void setEnMobile(String enMobile) {
        this.enMobile = enMobile;
    }

    public String getEnterpriseName() {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName) {
        this.enterpriseName = enterpriseName;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getConfirmStatus() {
        return confirmStatus;
    }

    public void setConfirmStatus(int confirmStatus) {
        this.confirmStatus = confirmStatus;
    }
}
