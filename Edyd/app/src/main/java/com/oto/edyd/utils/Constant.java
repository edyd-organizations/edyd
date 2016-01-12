package com.oto.edyd.utils;

import com.amap.api.maps.model.LatLng;

/**
 * Created by yql on 2015/8/27.
 */
public class Constant {

    //地理位置
    public final static double LATITUDE = 24.5004978985;
    public final static double LONGITUDE = 118.0877469228;
    public static final LatLng XIAMEN = new LatLng(24.5004978985, 118.0877469228);// 厦门市经纬度

    //存储文件
    public final static String USER_NAME = "user_name"; //偏好设置用户名key
    public final static String PASSWORD = "password"; //偏好设置密码key
    public final static String ENTERPRISE_ID = "enterprise_id"; //企业ID
    public final static String ACCOUNT_ID = "ACCOUNT_ID"; //账户ID
    public final static String TENANT_ID = "tenant_id"; //租户ID
    public final static String ORG_CODE = "org_code"; //组织ID
    public final static String ROLE_NAME = "role_name"; //组织名称
    public final static String ENTERPRISE_NAME = "enterprise_name"; //企业名称
    public final static String LOGIN_PREFERENCES_FILE = "login.xml"; //登入信息保存文件
    public final static String GLOBAL_FILE = "global.xml"; //全局偏好文件设置
    public final static String USER_INFO_FILE = "users.xml"; //用户信息
    public final static String FIXED_FILE = "fixed_file"; //不清理数据
    public final static String SESSION_UUID = "session_uuid";
    public final static String FIRST_ENTER = "first_enter";
    //public final static String ACCOUNT_ID = "account_id";

    //网络地址
    public final static String ENTRANCE_PREFIX = "http://120.24.236.223/api/v1.0/"; //接口前缀地址
//    public final static String ENTRANCE_PREFIX = "http://www.edyd.cn/api/v1.0/"; //接口前缀地址

    public final static String ENTRANCE_PREFIX_v1 = "http://120.24.236.223/api/v1.1/"; //接口前缀地址
//    public final static String ENTRANCE_PREFIX_v1 = "http://www.edyd.cn/api/v1.1/"; //接口前缀地址

    public final static String CMB_CALLBACK_ADDRESS = "http://120.24.236.223/callback/updateBillStatus.json";
   // public final static String CMB_CALLBACK_ADDRESS = "http://www.edyd.cn/callback/updateBillStatus.json";

    public final static String EDYD = "http://www.edyd.cn/";
    //系统状态码
    public final static String LOGIN_SUCCESS_STATUS = "200";  //请求成功
    public final static String NOT_SETTING_SESSION_ID = "401"; //没有设置会话ID
    public final static String USER_ALREADY_EXIST = "504"; //用户已存在
    public final static String USER_REGISTER_EXCEPTION = "700"; //用户注册异常
    public final static int NETWORK_EXCEPTION = 701; //网络异常

    //短信运营商状态吗
    public final static String INVALID_MOBILE_PHONE = "467"; //无效手机号码
    public final static String INVALID_VERIFICATION_CODE = "468"; //无效验证码


    //Activity返回码
    public final static int ACTIVITY_RETURN_CODE = 0x10; //Activity返回码
    public final static int LOGIN_ACTIVITY_RETURN_CODE = 1; //LOGIN_ACTIVITY返回码
    public final static int REGISTER_ACTIVITY_RETURN_CODE = 2; //REGISTER_ACTIVITY返回码
    public final static int ACCOUNT_TYPE_RESULT_CODE = 3; //账户类型返回码
    public final static int TRANSPORT_ROLE_CODE = 4; //运输服务角色选择返回码

    //等待时间
    public final static int WAITING_TIME_VERIFICATION = 60; //获取验证码等待时间
    public final static int THREAD_WAITING_TIME = 1000; //线程等待时间
    public final static String RETURN_SUCCESS = "SUCCESS"; //Android异步线程执行成功
    public final static String THREAD_CANCEL = "CANCEL"; //Android异步线程停止

    //switch 码
    public final static int loginThreadResultCode = 700;

    //系统变量长度
    public final static int VERIFICATION_LENGTH = 4; //验证码长度
    public final static int PASSWORD_LENGTH = 6; //密码长度

    //账户类型
    public final static int ENTERPRISE_TYPE_PERSONAL = 0; //个人信息

    //角色类型
    public final static int SUPER_ADMIN = 0;//超级管理员
    public final static int ENTER_ADMIN = 1; //企业管理员
    public final static int ENTER_STAFF = 2; //企业员工
    public final static int PERSON = 3; //个人信息

    //运输角色 0-司机； 2-发货方；1-收货方；3-承运方 默认为司机
    public final static String  TRANSPORT_ROLE = "transport_role";
    public final static int DRIVER_ROLE_ID = 0; //司机
    public final static int RECEIVER_ROLE_ID = 1; //收货方
    public final static int SHIPPER_ROLE_ID = 2; //发货方
    public final static int UNDERTAKER_ROLE_ID = 3; //承运方

    //消息类型
    public final static String DRIVER_MESSAGE_TYPE = "1"; //司机待执行订单
    public final static String ENTERPRISE_MESSAGE_TYPE = "2"; //企业消息

    //司机订单执行状态
    public final static String WAIT_EXECUTE_STATUS = "0"; //待执行订单
    public final static String EXECUTING_STATUS = "1"; //执行中的订单

    //设备类型 1-Android设备；2-iOS设备
    public final static String DEVICE_TYPE = "1"; //Android设备

    //用户类型, 0-司机
    public final static String TYPE_CODE = "type_code";

    //加载次序
    public final static int FIRST_LOAD = 1; //首次加载
    public final static int SECOND_LOAD = 2; //第二次加载
    public final static int THIRD_LOAD = 3; //第三次加载
    public final static int FOUR_LOAD = 4; //第四次加载

    //认证类别
    public final static String NOT_AUTHENTICATED = "未认证"; //0
    public final static String FAIL_AUTHENTICATED = "认证失败"; //1
    public final static String HAS_BEEN_AUTHENTICATED = "已认证"; //2

    //男女
    public final static String UNKNOWN_SEX = "未知";
    public final static String MALE = "男";
    public final static String FEMALE = "女";
    public final static String SECRECY_SEX ="保密";

    //Toast提示
    public final static String NOT_INTERNET_CONNECT = "网络不可用";
    public final static String INTERNET_REQUEST_ABNORMAL = "网络异常";
    public final static String INVALID_USERNAME_PASSWORD = "用户名和密码错误";
    public final static String NOT_NULL_USERNAME = "用户名不能为空";
    public final static String NOT_NULL_PASSWORD = "密码不能为空";
    public final static String USER_INFO_SAVE_FAIL = "用户信息保存失败";
    public final static String ACCOUNT_TYPE_INFO_REQUEST_FAIL = "账户类型信息请求失败";
    public final static String ACCOUNT_TYPE_INFO_SAVE_FAIL = "账户类型信息保存失败";
    public final static String CONFIRM_USER_INFO_FAIL = "验证用户信息失败";

    //服务总集
    //短信
    public final static String APPKEY = "98c434e66c87"; // 填写从短信SDK应用后台注册得到的APPKEY
    public final static String APPSECRET = "63d9051492bf34576212710e76e7f94d";// 填写从短信SDK应用后台注册得到的APPSECRET


    public static final int ELAPSED_TIME = 10 * 60 * 1000;
    public static final int RETRIVE_SERVICE_COUNT = 50;
    public static final int ELAPSED_TIME_DELAY = 2*60*1000;//get GPS delayed
    public static final int BROADCAST_ELAPSED_TIME_DELAY = 2*60*1000;
    public static final String WORKER_SERVICE = "com.coder80.timer.service.WorkService";
    public static final String TIMER_SERVICE = "com.oto.edyd.service.TimerService";
    public static final String ALARM_SERVICE_ACTION = "com.oto.edyd.service.TimerService.action";


    //友盟
    public static final String DESCRIPTOR = "com.umeng.share";
    private static final String TIPS = "请移步官方网站 ";
    private static final String END_TIPS = ", 查看相关说明.";
    public static final String TENCENT_OPEN_URL = TIPS + "http://wiki.connect.qq.com/android_sdk使用说明"
            + END_TIPS;
    public static final String PERMISSION_URL = TIPS + "http://wiki.connect.qq.com/openapi权限申请"
            + END_TIPS;

    //-----------------------------正则表达式---------------------
    //手机格式
    public static final String MATCH_MOBILE_PHONE = "^(0|86|17951)?(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])[0-9]{8}$";
    //注册密码格式
    public static final String MATCH_REGISTER_PASSWORD = "^(?!\\D+$)(?![^a-zA-Z]+$)\\S{6,20}$";
    //修改密码格式
    public static final String MATCH_MODIFY_PASSWORD = "^(?!\\D+$)(?![^a-zA-Z]+$)\\S{6,20}$";


}
