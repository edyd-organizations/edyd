package com.oto.edyd.pay.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.oto.edyd.R;
import com.oto.edyd.utils.CusProgressDialog;

/**
 * 功能：确认支付WebView
 * 文件名：com.oto.edyd.ConfirmPayActivity.java
 * 创建时间：2015/12/14
 * 作者：yql
 */
public class ConfirmPayActivity extends Activity {

    private WebView webView; //网页容器
    private CusProgressDialog transitionDialog; //加载过度

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_pay);
        //String url = "https://netpay.cmbchina.com/netpayment/BaseHttp.dll?MfcISAPICommand=TestPrePayWAP&BranchID=0592&CoNo=002968&BillNo=2015121402"+
          //      "&Amount=0.01&Date=20151214";

        String url = "https://netpay.cmbchina.com/netpayment/BaseHttp.dll?MfcISAPICommand=PrePayWAP&BranchID=0592&CoNo=002968&BillNo=2015121403&Amount=0.01&\n" +
                "Date=20151214&ExpireTimeSpan=3600&MerchantUrl=http://www.edyd.cn/callback/updateBillStatus.json";
        init(url);
    }

    /**
     * 初始化数据
     * @param url 网址
     */
    private void init(String url) {
        initFields(); //初始化字段
        initWebView(url);
    }

    /**
     * 初始化字段
     */
    private void initFields() {
        webView = (WebView) findViewById(R.id.web_view);
        transitionDialog = new CusProgressDialog(ConfirmPayActivity.this, "正在加载...");
    }

    /**
     * 初始化WebView容器
     * @param url 地址
     */
    private void initWebView(String url) {
        //启动过度画面
        transitionDialog.showDialog();
        //设置支持JavaScript
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        //加载网页
        webView.loadUrl(url);
        //覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                if (url.startsWith("scheme:") || url.startsWith("scheme:")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
                return false;
            }
        });
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if(newProgress == 100) {
                    //网页加载完毕
                    transitionDialog.dismissDialog();
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if(keyCode == KeyEvent.KEYCODE_BACK) {
//            if(webView.canGoBack()) {
//                int steps = 0 - (webView.copyBackForwardList().getSize() - 1);
//                webView.goBackOrForward(steps);
//                return true;
//            } else {
//                finish(); //结束当前Activity
//            }
//        }
        return super.onKeyDown(keyCode, event);
    }
}
