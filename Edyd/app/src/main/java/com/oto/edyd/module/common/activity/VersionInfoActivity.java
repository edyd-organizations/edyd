package com.oto.edyd.module.common.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oto.edyd.R;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 功能：版本信息，包括预览版本信息和检查更新功能
 * 文件名：com.oto.edyd.module.common.activity.VersionInfoActivity.java
 * 创建时间：2016/1/13
 * 作者：yql
 */
public class VersionInfoActivity extends Activity implements View.OnClickListener {

    //-------------基本view控件----------------
    private LinearLayout back; //返回
    private TextView versionName; //版本名
    private TextView checkUpdate; //检测更新
    private TextView updateInfo; //更新信息

    //-------------变量----------------
    private Common common; //共享文件对象LOGIN_PREFERENCES_FILE
    private Context context; //上下文对象
    private final static int HANDLER_REMOTE_VERSION_CODE = 0x10; //远程版本号返回码
    private final static int HANDLER_CHECK_VERSION_CODE = 0x11; //远程版本号返回码
    private final static int DEVICE_TYPE = 1; //1代表获取Android更新信息
    private VersionInfo versionInfo; //版本信息
    private CusProgressDialog transitionDialog; //过渡

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.version_info);
        init(); //初始化数据
    }

    /**
     * 初始化数据
     */
    private void init() {
        initFields(); //初始化字段
        initListener(); //初始化监听器
        versionName.setText(getVersionName());
        getRemoteVersionName(1);
    }

    /**
     * 初始化字段
     */
    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
        versionName = (TextView) findViewById(R.id.version_name);
        checkUpdate = (TextView) findViewById(R.id.check_update);
        updateInfo = (TextView) findViewById(R.id.update_content);
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, MODE_PRIVATE));
        context= VersionInfoActivity.this;
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        back.setOnClickListener(this);
        checkUpdate.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: //返回
                finish();
                break;
            case R.id.check_update: //检查更新
                getRemoteVersionName(2);
                break;
        }

    }

    /**
     * 获取远程版本号
     * @param requestSequence 请求次序
     */
    private void getRemoteVersionName(int requestSequence) {
        //type=1代表Android端版本号
        String url = Constant.ENTRANCE_PREFIX_v1 + "inqueryAppVersion.json?type=" + DEVICE_TYPE;
        OkHttpClientManager.getAsyn(url, new VersionInfoResultCallback<String>(requestSequence) {

            @Override
            public void onError(Request request, Exception e) {
                common.showToast(context, "远程版本号获取异常");
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                JSONObject item;
                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if (!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        common.showToast(context, "远程版本号失败");
                    }
                    jsonArray = jsonObject.getJSONArray("rows");
                    if(jsonArray.length() == 0) {
                        common.showToast(context, "无版本信息");
                        return;
                    }
                    item = jsonArray.getJSONObject(0);
                    versionInfo = new VersionInfo();
                    versionInfo.setUpdateVersion(item.getString("updateVersion"));
                    versionInfo.setUpdateTime(item.getString("updateDate"));
                    versionInfo.setUpdateContent(formatUpdateContent(item.getString("updateContent")));
                    Message message = Message.obtain();
                    if(this.requestSequence == 1) {
                        message.what = HANDLER_REMOTE_VERSION_CODE;
                    }  else {
                        message.what = HANDLER_CHECK_VERSION_CODE;
                    }
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 重写ResultCallback接口，操作访问网络前和网络后需要实现的动作
     * @param <T>
     */
    abstract class VersionInfoResultCallback<T> extends OkHttpClientManager.ResultCallback<T> {

        public int requestSequence; //请求次序
        private final static int START_ACCESS = 1; //首次访问
        //private final static int END_ACCESS = 2; //末次访问

        /**
         * @param requestSequence 网络请求次序
         */
        public VersionInfoResultCallback(int requestSequence) {
            this. requestSequence = requestSequence;
        }

        @Override
        public void onBefore() {
            //请求之前操作
            if(requestSequence == START_ACCESS) {
                //首次访问
                transitionDialog = new CusProgressDialog(context, "正在登录...");
                transitionDialog.showDialog();
            }
        }

        @Override
        public void onAfter() {
            //请求之后要做的操作
            if(requestSequence == START_ACCESS) {
                //末次访问
                transitionDialog.dismissDialog();
            }
        }
    }

    /**
     * handler通讯
     */
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_REMOTE_VERSION_CODE: //远程版本号返回成功
                    updateInfo.setText(versionInfo.getUpdateContent());
                    break;
                case HANDLER_CHECK_VERSION_CODE: //检查版本号
                    isNeedUpdate(versionInfo.getUpdateVersion());
                    break;
            }
        }
    };

    /**
     * 检查是否需要更新
     */
    private void isNeedUpdate(String versionName) {
        int currentVersionCode = Integer.valueOf(getVersionName().replaceAll("\\.", ""));
        int remoteVersionCode = Integer.valueOf(versionName.replaceAll("\\.", ""));
        if(currentVersionCode >= remoteVersionCode) {
            common.showToast(context, "已是最新版本");
            return;
        } else

        new AlertDialog.Builder(context).setTitle("更新")
                .setMessage("确认更新吗？")
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra("web_url", Constant.FIR_IM_ANDROID);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    /**
     * 获取版本名
     */
    private String getVersionName() {
        String versionName; //版本名
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            versionName = info.versionName;
            return versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 格式化数据
     * @param content
     * @return
     */
    private String formatUpdateContent(String content) {
        String fContent = "";
        if(content != null && !content.equals("")) {
            String cArray[] = content.split(" ");
            for(int i = 0; i < cArray.length; i++) {
                fContent += cArray[i] + "\n";
            }
        } else {
            fContent = "提示：暂无版本更新信息";
        }
        return fContent;
    }

    private class VersionInfo {
        private String updateVersion; //版本号
        private String updateTime; //更新时间
        private String updateContent; //更新内容

        public String getUpdateVersion() {
            return updateVersion;
        }

        public void setUpdateVersion(String updateVersion) {
            this.updateVersion = updateVersion;
        }

        public String getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(String updateTime) {
            this.updateTime = updateTime;
        }

        public String getUpdateContent() {
            return updateContent;
        }

        public void setUpdateContent(String updateContent) {
            this.updateContent = updateContent;
        }
    }
}


