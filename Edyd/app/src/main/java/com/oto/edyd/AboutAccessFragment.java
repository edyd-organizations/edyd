package com.oto.edyd;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.oto.edyd.utils.Constant;

/**
 * Created by yql on 2015/9/6.
 */
public class AboutAccessFragment extends Fragment {

    private View aboutAccessFragmentView;
    private FragmentManager setUpFragmentManager;
    private WebView acWebView; //快速访问WebView

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        aboutAccessFragmentView = inflater.inflate(R.layout.about_access, null);
        initFields(aboutAccessFragmentView);
        acWebView.loadUrl(Constant.EDYD); //多一点官网
        //浏览器支持
        WebSettings webSettings = acWebView.getSettings();
        webSettings.setJavaScriptEnabled(true); //支持JS
        webSettings.setSupportZoom(true); //支持缩放
        webSettings.setBuiltInZoomControls(true); //设置出现缩放工具
        //扩大比例的缩放
        webSettings.setUseWideViewPort(true);
        //自适应屏幕
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setLoadWithOverviewMode(true);
        //覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
        acWebView.setWebViewClient(new MyWebViewClient());
        return aboutAccessFragmentView;
    }

    /**
     * 初始化数据
     */
    private void initFields(View view) {
        this.setUpFragmentManager = ((SetUpActivity)getActivity()).setUpFragmentManager;
        acWebView = (WebView) view.findViewById(R.id.wv_about_access);
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true; //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
        }
    }
}
