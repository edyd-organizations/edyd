package com.oto.edyd.utils;

import android.os.Environment;

import java.io.File;

/**
 * 功能：操作SD卡
 * 文件名：com.oto.edyd.utils.OperateSD.java
 * 创建时间：2015/12/23
 * 作者：yql
 */
public class OperateSDCard {

    /**
     * 检查是否存在SD卡并返回
     * @return true存在 false不存在
     */
    private boolean checkIsExistSDCard() {
        String state = Environment.getExternalStorageState(); //当前设备SD的状态
        if(state.equals(Environment.MEDIA_MOUNTED)) { //判断是否存在SD卡
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取SD卡根路径
     * @return 根路径
     */
    private File getSDCardPath() {
        return Environment.getExternalStorageDirectory(); //获取SD卡根路径
    }
}
