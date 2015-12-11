package com.oto.edyd.utils;

import java.text.DecimalFormat;

/**
 * Created by yql on 2015/10/9.
 */
public class NumberFormat {

    private String INPUT_DOUBLE_NUMBER = "00";
    /**
     * 格式化两位数字，如果是1~9则数字前面补零
     * @param obj
     * @return
     */
    public String outputDoubleNumber(Object obj) {
        return new DecimalFormat(INPUT_DOUBLE_NUMBER).format(obj);
    }
}
