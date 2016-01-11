package com.oto.edyd.module.tts.model;

/**
 * 功能：执行中的订单订单实体
 * 文件名：com.oto.edyd.module.tts.model.DriverExecutingOrderBean.java
 * 创建时间：2016/1/11
 * 作者：yql
 */
public class DriverExecutingOrderBean extends DriverOrderBean{
    private long senderPrimaryId; //装货地经纬度ID
    private long receiverPrimaryId; //卸货方经纬度ID

    public long getSenderPrimaryId() {
        return senderPrimaryId;
    }

    public void setSenderPrimaryId(long senderPrimaryId) {
        this.senderPrimaryId = senderPrimaryId;
    }

    public long getReceiverPrimaryId() {
        return receiverPrimaryId;
    }

    public void setReceiverPrimaryId(long receiverPrimaryId) {
        this.receiverPrimaryId = receiverPrimaryId;
    }
}
