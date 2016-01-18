package com.oto.edyd.module.common.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oto.edyd.R;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;

/**
 * 功能：访问网页公共webView
 * 文件名：com.oto.edyd.module.common.activity.WebviewActivity.java
 * 创建时间：2016/1/13
 * 作者：yql
 */
public class WebViewActivity extends Activity{
    //-------------基本view控件----------------
    private WebView webView; //webView容器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_webview);
        init();
    }

    /**
     * 初始化字段
     */
    private void initFields() {
        webView = (WebView) findViewById(R.id.common_webview);
    }

    /**
     * 初始化数据
     */
    private void init() {
        initFields(); //初始化字段
        String webURL = getIntent().getStringExtra("web_url");
        if (webURL !=null && !webURL.equals("")) {
            webView.loadUrl(webURL); //多一点官网
            //浏览器支持
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true); //支持JS
            webSettings.setSupportZoom(true); //支持缩放
            webSettings.setBuiltInZoomControls(true); //设置出现缩放工具
            //扩大比例的缩放
            webSettings.setUseWideViewPort(true);
            //自适应屏幕
            webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            webSettings.setLoadWithOverviewMode(true);
            //覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
            webView.setWebViewClient(new CommonWebViewClient());
        }
    }

    private class CommonWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true; //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
        }
    }
}
