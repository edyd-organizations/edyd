package com.oto.edyd;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


/**
 * 周边停车
 */
public class BoxRimPark extends Activity {
    private WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_web);
        webView = new WebView(this);
        WebSettings wv = webView.getSettings();
        wv.setJavaScriptEnabled(true);
        wv.setAllowFileAccess(true);
        wv.setDomStorageEnabled(true);//允许DCOM
        webView.loadUrl("http://map.baidu.com/mobile/webapp/index/index/");
//		WebSettings webSettings = webView.getSettings();
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                view.loadUrl(url);
                return true;
            }
        });
        //支持获取手势焦点
        webView.requestFocusFromTouch();
        // 加载
        setContentView(webView);

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
