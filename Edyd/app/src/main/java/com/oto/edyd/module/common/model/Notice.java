package com.oto.edyd.module.common.model;

import java.io.Serializable;

/**
 * 功能：消息实体类
 * 文件名：com.oto.edyd.module.common.model.Notice.java
 * 创建时间：2016/1/4
 * 作者：yql
 */
public class Notice implements Serializable{
    private String sender; //发件人
    private String title; //标题
    private String content; //内容
    private String time; //发送时间

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
