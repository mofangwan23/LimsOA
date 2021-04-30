package cn.flyrise.feep.more;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import cn.flyrise.feep.R;
import cn.flyrise.feep.auth.server.setting.ServerSettingActivity;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import java.util.Locale;

public class PrivacyActivity extends BaseActivity {

	private FEToolbar mToolBar;
	private ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_privacy);
		mToolBar.setNavigationVisibility(View.GONE);
		String homeLink = "https://api.flyrise.cn/feep/policy/PrivacyPolicy-zh_CN.html";
		Locale locale = getResources().getConfiguration().locale;
		String language = locale.getLanguage();
		String country = locale.getCountry();
		if ("en".equals(language)) {
			homeLink = "https://api.flyrise.cn/feep/policy/PrivacyPolicy-en.html";
		}
		else if ("TW".equals(country)) {
			homeLink = "https://api.flyrise.cn/feep/policy/PrivacyPolicy-zh_TW.html";
		}
		WebView mWebView = findViewById(R.id.webviewPrivacy);
		progressBar = findViewById(R.id.progressh);
		mWebView.loadUrl(homeLink);

		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				//6.0以下执行
				//网络未连接
				startActivity(new Intent(PrivacyActivity.this, ServerSettingActivity.class));
				finish();
			}

			//处理网页加载失败时
			@Override
			public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
				super.onReceivedError(view, request, error);
				//6.0以上执行
				startActivity(new Intent(PrivacyActivity.this, ServerSettingActivity.class));
				finish();
			}
		});

		mWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				progressBar.setProgress(newProgress);
				if (newProgress == 100) {
					progressBar.setVisibility(View.GONE);
				}
			}


			@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
				if (title.contains("404")) {
					startActivity(new Intent(PrivacyActivity.this, ServerSettingActivity.class));
					finish();
				}
			}
		});

	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		super.toolBar(toolbar);
		mToolBar = toolbar;
		toolbar.setTitle(getString(R.string.privacy_title));
		toolbar.setTitleTextColor(Color.BLACK);
		toolbar.setTitleFakeBoldText();
		toolbar.setRightText(getString(R.string.confirm));
		toolbar.setRightTextColor(Color.parseColor("#FF28B9FF"));
		toolbar.setRightTextClickListener(v -> {
			finish();
		});
	}

	@Override public boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
	}
}
