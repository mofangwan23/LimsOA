
package cn.flyrise.feep.collaboration.activity;

import android.os.Bundle;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;

import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.StringCallback;
import cn.flyrise.feep.core.base.views.FEToolbar;

/**
 * 类功能描述:</br> 显示相关联的协同
 * @author zms
 * @version 1.0</br> 修改时间：2012-7-26</br> 修改备注：</br>
 */
public class AssociateCollaborationActivity extends BaseActivity {

	public static final String ACTION_ASSOCIATE_URL = "ACTION_ASSOCIATE_URL";
	private WebView mWebView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.collaboration_associate_content);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		toolbar.setTitle(R.string.associate_collaboration_title);
	}

	@Override
	public void bindView() {
		mWebView = (WebView) findViewById(R.id.content);
		mWebView.getSettings().setJavaScriptEnabled(true);// 可用JS
		mWebView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
		mWebView.getSettings().setUseWideViewPort(true);
		mWebView.getSettings().setLoadWithOverviewMode(true);
		mWebView.getSettings().setSupportZoom(true);
		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.getSettings().setDisplayZoomControls(false);
	}

	@Override
	public void bindData() {
		String url = getIntent().getStringExtra(ACTION_ASSOCIATE_URL);
		String serverAddress = CoreZygote.getLoginUserServices().getServerAddress();
		if (!url.startsWith("http")) {
			url = serverAddress + url;
		}
		LoadingHint.show(this);

		FEHttpClient.getInstance().post(url, null, new StringCallback(this) {
			@Override
			public void onCompleted(String result) {
				LoadingHint.hide();
				mWebView.loadDataWithBaseURL(serverAddress, result, "text/html", "UTF-8", null);
			}

			@Override
			public void onFailure(RepositoryException repositoryException) {
				LoadingHint.hide();
			}
		});

	}
}
