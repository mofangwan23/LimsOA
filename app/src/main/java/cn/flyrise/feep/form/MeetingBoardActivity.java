/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-12-31 下午4:23:40
 */
package cn.flyrise.feep.form;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import cn.flyrise.feep.commonality.WebViewShowActivity;
import cn.flyrise.feep.core.network.TokenInject;
import cn.flyrise.feep.form.been.MeetingBoardData;
import org.json.JSONException;

/**
 * 类功能描述：</br>
 * 
 * @author 钟永健
 * @version 1.0</br> 修改时间：2012-12-31</br> 修改备注：</br>
 */
public class MeetingBoardActivity extends WebViewShowActivity {
    private Handler          handler;

    private MeetingBoardData meetingBoardData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (webViewUrl != null) {
            TokenInject.injectToken(wv, baseUrl + webViewUrl);
        }
    }

    @Override
    public void bindData() {
        super.bindData();
        handler = new Handler();
    }

    @Override
    public void webViewSetting(WebView webView) {
        super.webViewSetting(webView);
        webView.addJavascriptInterface(new Controller(), "androidJS");
    }

    /** html控件点击事件的响应 */
    private class Controller {
        @JavascriptInterface
        public void runOnAndroidJavaScript(final String jsonStr) {
            handler.post(new Runnable() {

                @Override
                public void run() {
                    meetingBoardData = new MeetingBoardData();
                    try {
                        meetingBoardData.parseReponseJson(jsonStr);
                    } catch (final JSONException e) {
                        e.printStackTrace();
                    }
                    finishAcitity();
                }

            });
        }
    }

    /** 结束页面 */
    private void finishAcitity() {
        final Intent intent = new Intent();
        intent.putExtra("MeetingBoardData", meetingBoardData);
        setResult(0, intent);
        finish();
    }
}
