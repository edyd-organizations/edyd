package com.oto.edyd.model;

import java.io.Serializable;

/**
 * Created by xhj on 2015/11/24.
 */
public class ViolationInfo implements Serializable{
    private String data; //违章时间
    private String area; //违章地点
    private String act; //违章行为
    private String fen; //违章扣分
    private String money; //违章罚款
    private String handled; //是否处理

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getAct() {
        return act;
    }

    public void setAct(String act) {
        this.act = act;
    }

    public String getFen() {
        return fen;
    }

    public void setFen(String fen) {
        this.fen = fen;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getHandled() {
        return handled;
    }

    public void setHandled(String handled) {
        this.handled = handled;
    }

}
