/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-11-6 下午7:42:16
 */

package cn.flyrise.feep.report;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.shared.model.user.UserInfo;
import cn.flyrise.feep.FEApplication;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.util.RemoveAD;
import cn.flyrise.feep.cordova.utils.FEWebViewJsUtil;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.network.TokenInject;
import cn.flyrise.feep.utils.FEWebChromeClient;


public class ReportWebViewActivity extends BaseActivity {

	public static final String TITLE_DATA_KEY = "TITLE_DATA_KEY";
	public static final String URL_DATA_KEY = "URL_DATA_KEY";
	public static final String LOAD_KEY = "LOAD_KEY";
	protected String webViewUrl;

	private String title;

	protected WebView mWebView;

	protected String baseUrl;

	private View errorLayout;

	private TextView error_text;

	protected FEToolbar mToolBar;

	/**
	 * 标志WebView是否加载失败
	 */
	private boolean isLoadFail;
	private boolean isNeedLoad;
	private boolean isWebViewFinished = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.form_new_report_form);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            if (VERSION.SDK_INT == VERSION_CODES.KITKAT && !FEStatusBar.canModifyStatusBar(null)) {
//                return;
//            }
//            AndroidBug5497Workaround.assistActivity(this);
//        }
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		this.mToolBar = toolbar;
	}

	@Override
	public void bindView() {
		super.bindView();
		mWebView = (WebView) findViewById(R.id.new_form_webview);
		errorLayout = findViewById(R.id.error_tip_lyt);
		error_text = (TextView) findViewById(R.id.error_text);
	}

	@Override
	public void bindData() {
		final FEApplication fEApplication = (FEApplication) getApplication();
		final UserInfo userInfo = fEApplication.getUserInfo();
		baseUrl = userInfo.getUrl();
		getIntentData(getIntent());
		this.mToolBar.setTitle(TextUtils.isEmpty(title) ? "" : title);

		webViewSetting(mWebView);
		setViewVisible(true, false);
		if (isNeedLoad) {
			TokenInject.injectToken(mWebView, baseUrl + webViewUrl);
		}
	}

	@Override
	public void bindListener() {
		super.bindListener();
		errorLayout.setOnClickListener(v -> {
			mWebView.reload();
			setViewVisible(true, false);
		});
	}

	/**
	 * WebView设置
	 */
	protected void webViewSetting(WebView webView) {
		final WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setSupportZoom(false);
		webSettings.setUseWideViewPort(false);
		webSettings.setBuiltInZoomControls(false);
		webSettings.setAllowFileAccess(true);
		webSettings.setAllowContentAccess(true);
		webSettings.setDatabaseEnabled(true);
		webSettings.setBlockNetworkImage(true);//先禁用图片的加载
		webSettings.setDefaultTextEncodingName("utf-8");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			webSettings.setAllowFileAccessFromFileURLs(true);
		}
		webSettings.setAppCacheEnabled(true);
		webSettings.setGeolocationEnabled(true);
		webSettings.setDomStorageEnabled(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			try {
				CookieManager cookieManager = CookieManager.getInstance();
				cookieManager.setAcceptThirdPartyCookies(webView, true);
			} catch (Exception ex) {
			}
		}
		webView.setWebChromeClient(new FEWebChromeClient(this, null));
//		webView.setWebChromeClient(new PopupChromeClient());
		webView.setWebViewClient(new MyWebViewClient());
	}

	/**
	 * 获取intent中的数据
	 */
	protected void getIntentData(Intent intent) {
		if (intent != null) {
			webViewUrl = intent.getStringExtra(URL_DATA_KEY);
			title = intent.getStringExtra(TITLE_DATA_KEY);
			isNeedLoad = intent.getBooleanExtra(LOAD_KEY, false);
		}
	}

	/**
	 * 设置View是否可见
	 * @param isLayoutVisible 错误提示的父布局和WebView是否可见
	 * @param isErrorViewVisible 错误提示是否可见
	 */
	protected void setViewVisible(boolean isLayoutVisible, boolean isErrorViewVisible) {
		mWebView.setVisibility(isLayoutVisible ? View.GONE : View.VISIBLE);
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
		public void onReceivedError(WebView view, int errorCode, final String description,
				String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			FELog.i("表单webview加载失败信息：" + description);
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
			if (!isWebViewFinished) {
				isWebViewFinished = true;
				FEWebViewJsUtil.isFromApp(mWebView);
			}
			if (isLoadFail) {
				setViewVisible(true, true);
			}
			else {
				setViewVisible(false, false);
			}
			if (LoadingHint.isLoading()) {
				LoadingHint.hide();
			}
			ReportWebViewActivity.this.onPageFinished();
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
		clearWebViewData(mWebView);
		super.onDestroy();
		if (LoadingHint.isLoading()) {
			LoadingHint.hide();
		}
	}

	private void clearWebViewData(WebView webview) {
		if (mWebView != null && mWebView.getParent() != null) {
			((ViewGroup) mWebView.getParent()).removeView(mWebView);
			mWebView.destroy();
			mWebView = null;
		}
	}

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
}
