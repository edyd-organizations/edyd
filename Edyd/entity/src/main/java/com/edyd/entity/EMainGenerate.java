package com.edyd.entity;

import com.edyd.entity.config.Constant;

import java.io.File;
import java.io.IOException;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

/**
 * 功能：数据库实体主生成器
 * 文件名：com.generator.EMainGenerate.java
 * 创建时间：2015/12/25
 * 作者：yql
 */
public class EMainGenerate {

    public static void main(String []args) {
        EMainGenerate generate = new EMainGenerate();
        generate.config();
    }

    /**
     * 配置
     */
    private void config() {
        //Schema(体系结构版本号, 默认包保存路径)
        Schema schema = new Schema(Constant.DB_VERSION, "dao");
        generatorEntity(schema);
    }

    /**
     * 生成实体表配置
     * @param schema 体系结构
     */
    private void generatorEntity(Schema schema) {
        //----------------用户中心模块---------------
        //用户表声明
        Entity userInfo = schema.addEntity("UserInfo");
        //声明属性
        userInfo.addIdProperty(); //添加自增ID
        userInfo.addStringProperty("sessionUUID"); //用户唯一识别码
        userInfo.addStringProperty("mobilePhoneNumber"); //手机号码
        userInfo.addStringProperty("enterpriseId"); //企业ID
        userInfo.addStringProperty("enterpriseName"); //企业名称
        userInfo.addStringProperty("roleId"); //角色ID
        userInfo.addStringProperty("accountId"); //账户ID
        userInfo.addStringProperty("tenantId"); //租户ID

        //----------------生成代码---------------
        //模块创建完成后，用DaoGenerator类生成实体和DAOs系列
        DaoGenerator daoGenerator;
        try {
            daoGenerator = new DaoGenerator(); //实体生成类对象
            //获取项目根路径
            File currentFile = new File("");
            String proRootPath = currentFile.getAbsolutePath() + "\\"; //工程根路径
            daoGenerator.generateAll(schema, proRootPath + Constant.ENTITY_PACKAGE_NAME); //生成实体类和DAOs
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
