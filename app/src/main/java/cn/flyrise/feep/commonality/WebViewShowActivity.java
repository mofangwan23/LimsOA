/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-11-6 下午7:42:16
 */

package cn.flyrise.feep.commonality;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.util.RemoveAD;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.network.TokenInject;
import cn.flyrise.feep.form.util.PopupChromeClient;

/**
 * 类功能描述：用WebView加载传过来的Url（比如：会议看板、表单流程图）</br> 该类处理了从网页JS传过来的数据见{@link MyWebViewClient }</br>
 *
 * @author 钟永健
 * @version 1.0</br> 修改时间：2012-11-6</br> 修改备注：</br>
 */
public class WebViewShowActivity extends BaseActivity {

    private RelativeLayout rootLayout;

    protected String webViewUrl;

    private String title;

    protected WebView wv;

    protected String baseUrl;

    private View errorLayout;

    private TextView error_text;

    protected FEToolbar mToolBar;

    /**
     * 标志WebView是否加载失败
     */
    private boolean isLoadFail;

    private boolean isNeedLoad;

    private FEOrientoinListener orientoinListener;
    private static final String TAG = "WebViewShowActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_new_form);
    }

    @Override
    protected void toolBar(FEToolbar toolbar) {
        this.mToolBar = toolbar;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !DevicesUtil.isSpecialDevice()) {
            int statusBarHeight = DevicesUtil.getStatusBarHeight(this);
            this.mToolBar.setPadding(0, statusBarHeight, 0, 0);
        }
    }

    @Override
    public void bindView() {
        wv = (WebView) findViewById(R.id.new_form_webview);
        errorLayout = findViewById(R.id.error_tip_lyt);
        error_text = (TextView) findViewById(R.id.error_text);
        rootLayout = (RelativeLayout)findViewById(R.id.rl_root);
    }

    @Override
    public void bindData() {
        baseUrl = CoreZygote.getLoginUserServices().getServerAddress();
        getIntentData(getIntent());
        this.mToolBar.setTitle(TextUtils.isEmpty(title) ? "" : title);

        webViewSetting(wv);
        setViewVisible(true, false);
        if (isNeedLoad) {
            TokenInject.injectToken(wv, baseUrl + webViewUrl);
        }

        orientoinListener = new FEOrientoinListener(this);
        orientoinListener.enable();
    }

    @Override
    public void bindListener() {
        errorLayout.setOnClickListener(v -> {
            wv.reload();
            setViewVisible(true, false);
        });
    }

    /**
     * WebView设置
     */
    protected void webViewSetting(WebView webView) {
        final WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLightTouchEnabled(true);
        // 设置不可双指放大
        webSettings.setSupportZoom(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        //不显示webview缩放按钮
        webSettings.setDisplayZoomControls(false);
        webView.setVerticalScrollbarOverlay(true);
        webView.setVerticalScrollBarEnabled(true);
        webView.requestFocusFromTouch();
        webView.setWebChromeClient(new PopupChromeClient());
        webView.setWebViewClient(new MyWebViewClient());
    }

    /**
     * 获取intent中的数据
     */
    protected void getIntentData(Intent intent) {
        if (intent != null) {
            webViewUrl = intent.getStringExtra(K.form.URL_DATA_KEY);
            title = intent.getStringExtra(K.form.TITLE_DATA_KEY);
            isNeedLoad = intent.getBooleanExtra(K.form.LOAD_KEY, false);
        }
    }

    /**
     * 设置View是否可见
     *
     * @param isLayoutVisible    错误提示的父布局和WebView是否可见
     * @param isErrorViewVisible 错误提示是否可见
     */
    protected void setViewVisible(boolean isLayoutVisible, boolean isErrorViewVisible) {
        wv.setVisibility(isLayoutVisible ? View.GONE : View.VISIBLE);
        errorLayout.setVisibility(isLayoutVisible ? View.VISIBLE : View.GONE);
        error_text.setVisibility(isErrorViewVisible ? View.VISIBLE : View.GONE);
        if (!isLayoutVisible) {
            onPageLoadSucceed();
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            String host = null;
            try {
                host = Uri.parse(url).getHost();
            } catch (final Exception e) {
                e.printStackTrace();
            }
            if (view == null || host == null) {
                return super.shouldOverrideUrlLoading(view, url);
            }
            if ("NotificationReady".equals(host)) {
                return true;
            }
            else if (host.contains("PostNotificationWithId")) { // JS通知Android有数据需要过去取
                final int index = host.indexOf("-");
                if (index != -1) {
                    final String s = host.substring(index + 1, host.length());
                    FELog.i("webviewshow", "--->>>>webview-listener--" + s);
                    // 调用JS的popNotificationObject函数取出数据，
                    // IOS中该数据会直接返回，但是在Android中该数据通过回调函数回传
                    // 回调函数注册方式：webView.addJavascriptInterface(new Controller(), "androidJS");
                    String javaScript = "jsBridge.popNotificationObject(" + s + ")";

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        view.evaluateJavascript(javaScript, value -> {
                            // Android 4.4 以上的适配...
                        });
                    }
                    else {
                        view.loadUrl("javascript:" + javaScript);
                    }

                }
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, final String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            setViewVisible(true, true);
            isLoadFail = true;
            if (LoadingHint.isLoading()) {
                LoadingHint.hide();
            }
        }

        /**
         * onPageFinished指页面加载完成,完成后取消计时器
         */
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            view.requestFocus();
            if (isLoadFail) {
                setViewVisible(true, true);
            }
            else {
                setViewVisible(false, false);
            }
            if (LoadingHint.isLoading()) {
                LoadingHint.hide();
            }
            WebViewShowActivity.this.onPageFinished();
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            if (RemoveAD.contentAD(url)) {
                return new WebResourceResponse(null, null, null);
            }
            return null;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (LoadingHint.isLoading()) {
            LoadingHint.hide();
        }
        if (orientoinListener != null) {
            orientoinListener.disable();
        }
    }

//    @Override
//    public void finish() {
//        ViewGroup view = (ViewGroup) getWindow().getDecorView();
//        view.removeAllViews();
//        super.finish();
//    }

    /**
     * 网页加载完后调用此方法
     */
    protected void onPageFinished() {

    }

    /**
     * 网页加载成功后调用此方法
     */
    protected void onPageLoadSucceed() {

    }

    class FEOrientoinListener extends OrientationEventListener {

        FEOrientoinListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            int screenOrientation = getResources().getConfiguration().orientation;
            Log.e(TAG, orientation + "");
            if (((orientation >= 0) && (orientation < 45)) || (orientation > 315)) {//设置竖屏
                if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT && orientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }
            else if (orientation > 225 && orientation < 315) { //设置横屏
                if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }
            else if (orientation > 45 && orientation < 135) {// 设置反向横屏
                if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                }
            }
            else if (orientation > 135 && orientation < 225) {
                if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                }
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged :" + newConfig.orientation);

    }

}
