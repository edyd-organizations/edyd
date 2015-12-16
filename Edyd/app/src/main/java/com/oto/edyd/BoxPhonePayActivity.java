package com.oto.edyd;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
/**
 * Created by xhj on 2015/12/14.
 * 手机充值页面
 */
public class BoxPhonePayActivity extends Activity {
    private WebView mWebView;
    String data = "https://h5.m.taobao.com/app/cz/cost.html?locate=icon-5&spm=a215s.7406091.1.icon-5&scm=2027.1.3.1003&qq-pf-to=pcqq.discussion";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box_rim_park);
        mWebView = (WebView) findViewById(R.id.webView1);
        // 设置基本属性
        setWeb();
        // 加载html数据
      /*  mWebView.loadDataWithBaseURL
                (null, data, "text/html", "utf-8", null);*/
        mWebView.loadUrl(data);
    }
    private void setWeb() {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setAllowFileAccess(true);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
    }
}
