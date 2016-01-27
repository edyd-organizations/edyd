package com.oto.edyd.module.usercenter.model;

import java.io.Serializable;

/**
 * 功能：个人信息实体类
 * 文件名：com.oto.edyd.module.usercenter.model.PersonalInfoItem.java
 * 创建时间：2016/1/27
 * 作者：yql
 */
public class PersonalInfoItem implements Serializable {
    private String title;
    private String content;

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
}
