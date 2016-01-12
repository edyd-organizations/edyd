package com.oto.edyd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.bean.StatusCode;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.QZoneShareContent;
import com.umeng.socialize.media.SinaShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.sso.UMSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

/**
 * Created by Administrator on 2015/11/3.
 */
public class SocialSharedActivity extends Activity implements View.OnClickListener {
    private LinearLayout back; //返回
    private TextView waitBuildTitle; //标题

    private ImageButton wechat;
    private ImageButton wechat_circle;
    private ImageButton qq;
    private ImageButton qzone;
    private ImageButton sine;
    private UMSocialService mController = UMServiceFactory.getUMSocialService(Constant.DESCRIPTOR);
    private Activity mActivity;
    private SHARE_MEDIA mPlatform = SHARE_MEDIA.SINA;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_shared);
        String title = getIntent().getStringExtra("wait_title");
        mActivity = this;
        initView();
        waitBuildTitle.setText(title);
        back.setOnClickListener(this);
        // 配置需要分享的相关平台
        configPlatforms();
        // 设置分享的内容
        setShareContent();


    }

    private void setShareContent() {
        UMImage urlImage = new UMImage(this, R.mipmap.shared_logo);
        String appUploadUrl = "http://fir.im/duoyidianAndroid?utm_source=fir&utm_medium=qr";

        SinaShareContent sinaContent = new SinaShareContent();
        sinaContent.setShareContent("厦门多一点智能物流科技有限公司");
        sinaContent.setShareImage(urlImage);
        sinaContent.setTitle("多一点");
        sinaContent.setTargetUrl(appUploadUrl);
        sinaContent.setAppWebSite("多一点智能物流科技有限公司");
        mController.setShareMedia(sinaContent);


        QZoneShareContent qZone=new QZoneShareContent();
        qZone.setShareContent("厦门多一点智能物流科技有限公司");
        qZone.setShareImage(urlImage);
        qZone.setTitle("多一点");
        qZone.setTargetUrl(appUploadUrl);
        qZone.setAppWebSite("多一点智能物流科技有限公司");
        mController.setShareMedia(qZone);


//        mController.setShareContent("厦门多一点智能物流科技有限公司");

        QQShareContent qqShareContent = new QQShareContent();
        qqShareContent.setShareContent("厦门多一点智能物流科技有限公司");
        qqShareContent.setTitle("多一点");
        qqShareContent.setShareMedia(urlImage);
        qqShareContent.setTargetUrl(appUploadUrl);
        mController.setShareMedia(qqShareContent);


        //分享微信好友

        WeiXinShareContent weixinContent = new WeiXinShareContent();
        weixinContent
                .setShareContent("厦门多一点智能物流科技有限公司app");
        weixinContent.setTitle("多一点");
        weixinContent.setTargetUrl(appUploadUrl);
        weixinContent.setShareMedia(urlImage);
        mController.setShareMedia(weixinContent);

        // 设置朋友圈分享的内容
        CircleShareContent circleMedia = new CircleShareContent();
        circleMedia
                .setShareContent("厦门多一点智能物流科技有限公司app");
        circleMedia.setTitle("多一点");
        circleMedia.setShareMedia(urlImage);
        // circleMedia.setShareMedia(uMusic);
        // circleMedia.setShareMedia(video);
        circleMedia.setTargetUrl(appUploadUrl);
        mController.setShareMedia(circleMedia);
    }


    /**
     * 配置分享平台参数</br>
     */
    private void configPlatforms() {
        // 添加新浪SSO授权
        addSinaPlatform();
        // 添加QQ、QZone平台
        addQQQZonePlatform();
        // 添加微信、微信朋友圈平台
        addWXPlatform();
    }

    private void addSinaPlatform() {
        SinaSsoHandler sinaHandler= new SinaSsoHandler(mActivity);
        sinaHandler.addToSocialSDK();
        mController.getConfig().setSsoHandler(sinaHandler);
    }

    private void addQQQZonePlatform() {
        String appUploadUrl = "http://fir.im/duoyidianAndroid?utm_source=fir&utm_medium=qr";
        String appId = "1104934650";
        String appKey = "e6owi62LDhnKTKMu";
        // 添加QQ支持, 并且设置QQ分享内容的target url
        UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(mActivity,
                appId, appKey);
        qqSsoHandler.setTargetUrl(appUploadUrl);
        qqSsoHandler.addToSocialSDK();

        // 添加QZone平台
        QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(mActivity, appId, appKey);
        qZoneSsoHandler.setTargetUrl(appUploadUrl);
        qZoneSsoHandler.addToSocialSDK();
    }

    /**
     * @return
     * @功能描述 : 添加微信平台分享
     */
    private void addWXPlatform() {
        // 注意：在微信授权的时候，必须传递appSecret
        // wx967daebe835fbeac是你在微信开发平台注册应用的AppID, 这里需要替换成你注册的AppID
        String appId = "wx2b252204fbfa14ca";
        String appSecret = "5a04a3a97694f819c7295a4bb9c83de8";
        // 添加微信平台
        UMWXHandler wxHandler = new UMWXHandler(mActivity, appId, appSecret);
        wxHandler.addToSocialSDK();

        // 支持微信朋友圈
        UMWXHandler wxCircleHandler = new UMWXHandler(mActivity, appId, appSecret);
        wxCircleHandler.setToCircle(true);
        wxCircleHandler.addToSocialSDK();
    }

    private void initView() {
        wechat = (ImageButton) findViewById(R.id.wechat);
        wechat.setOnClickListener(this);
        wechat_circle = (ImageButton) findViewById(R.id.wechat_circle);
        wechat_circle.setOnClickListener(this);
        qq = (ImageButton) findViewById(R.id.qq);
        qq.setOnClickListener(this);
        qzone = (ImageButton) findViewById(R.id.qzone);
        qzone.setOnClickListener(this);
        sine = (ImageButton) findViewById(R.id.sine);
        sine.setOnClickListener(this);
        back = (LinearLayout) findViewById(R.id.back);
        waitBuildTitle = (TextView) findViewById(R.id.wait_build_title);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**使用SSO授权必须添加如下代码 */
        UMSsoHandler ssoHandler = mController.getConfig().getSsoHandler(requestCode);
        if (ssoHandler != null) {
            ssoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.wechat:
                performShare(SHARE_MEDIA.WEIXIN);
                break;
            case R.id.wechat_circle:
                performShare(SHARE_MEDIA.WEIXIN_CIRCLE);
                break;
            case R.id.qq:
                performShare(SHARE_MEDIA.QQ);
                break;
            case R.id.qzone:
                performShare(SHARE_MEDIA.QZONE);
                break;
            case R.id.sine:
               performShare(SHARE_MEDIA.SINA);
                break;
            case R.id.back:
                finish();
                break;
            default:
                break;
        }
    }

    /**
     * 直接分享，底层分享接口。如果分享的平台是新浪、腾讯微博、豆瓣、人人，则直接分享，无任何界面弹出； 其它平台分别启动客户端分享</br>
     */
    private void directShare() {
        mController.directShare(mActivity, mPlatform, new SocializeListeners.SnsPostListener() {

            @Override
            public void onStart() {

            }
            @Override
            public void onComplete(SHARE_MEDIA platform, int eCode, SocializeEntity entity) {
                String showText = "分享成功";
                if (eCode != StatusCode.ST_CODE_SUCCESSED) {
                    showText = "分享失败 [" + eCode + "]";
                }
                Toast.makeText(mActivity, showText, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performShare(SHARE_MEDIA platform) {
        mController.postShare(mActivity, platform, new SocializeListeners.SnsPostListener() {
            @Override
            public void onStart() {
            }
            @Override
            public void onComplete(SHARE_MEDIA platform, int eCode, SocializeEntity entity) {
                String showText = platform.toString();
                if (eCode == StatusCode.ST_CODE_SUCCESSED) {
                    showText += "平台分享成功";
                } else {
                    showText += "平台分享失败";
                }
                Common.showToast(mActivity,showText);
            }
        });
    }
}