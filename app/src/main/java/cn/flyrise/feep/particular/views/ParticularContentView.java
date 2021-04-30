package cn.flyrise.feep.particular.views;

import static cn.flyrise.feep.R.id.webView;
import static cn.flyrise.feep.cordova.CordovaContract.CordovaPresenters.COLON_CODE;
import static cn.flyrise.feep.cordova.CordovaContract.CordovaPresenters.JAVASCRIPT;
import static cn.flyrise.feep.cordova.CordovaContract.CordovaPresenters.NOTIFICATION_BEFORE;
import static cn.flyrise.feep.cordova.CordovaContract.CordovaPresenters.NOTIFICATION_LAST;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.flyrise.android.protocol.model.SupplyContent;
import cn.flyrise.android.protocol.model.TrailContent;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.bean.JSControlInfo;
import cn.flyrise.feep.commonality.view.TouchableWebView;
import cn.flyrise.feep.cordova.utils.FEWebViewJsUtil;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.network.TokenInject;
import com.hyphenate.easeui.ui.EaseShowBigImageActivity;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author ZYP
 * @since 2016-10-23 14:28
 * 使用 ViewStub 进行布局的延时加载
 */
public class ParticularContentView extends LinearLayout {

	public final static String HTML_STYLE_IMAGE_CENTER = "<style type='text/css'>" +
			"body{line-height:1.5!important;" +
			"padding:10px!important;" +
			"margin:auto auto!important;" +
			"word-wrap: break-word!important;" +
			"word-break: break-all!important;" +
			"text-align: justify!important;" +
			"text-justify: inter-ideograph!important; }" +
			"img{width:100%!important; height:auto!important; } " +
			"table{max-width:100%!important;}" +
			"</style>";

	public final static String HTML_STYLE_TABLE_BORDER = "<style type='text/css'>" +
			"table{border-collapse:collapse!important;" +
			"border: 1px solid #000!important;} " +
			"th, tr, td {border: 1px solid #000!important;}" +
			"</style>";

	public final static String HTML_STYLE_VIEW_PORT = "<meta name='viewport' content='width=device-width,height=device-height,user-scalable=yes'/>";
	private TouchableWebView mContentWebView;
	private WebViewWatcher mWebViewWatcher;
	private View mSupplementView;
	private View mModifyView;

	private boolean isFeForm = false;
	private boolean isWebViewFinished = false;

	public ParticularContentView(Context context) {
		this(context, null);
	}

	public ParticularContentView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ParticularContentView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		inflate(context, R.layout.view_particular_content, this);
		mContentWebView = findViewById(webView);
		initializerWebView();
	}

	private void initializerWebView() {
		mContentWebView.getSettings().setJavaScriptEnabled(true);
		mContentWebView.addJavascriptInterface(new Controller(), "androidJS");

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			mContentWebView.getSettings().setAllowFileAccessFromFileURLs(true);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			mContentWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
			try {
				CookieManager cookieManager = CookieManager.getInstance();
				cookieManager.setAcceptThirdPartyCookies(mContentWebView, true);
			} catch (Exception ex) {
			}
		}

		mContentWebView.getSettings().setAppCacheEnabled(true);
		mContentWebView.getSettings().setDomStorageEnabled(true);
		mContentWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				FELog.i("ShouldOverrideURL = " + url);
				if (mWebViewWatcher != null) {
					if (mWebViewWatcher.shouldUrlIntercept(url, isFeForm)) {
						return true;
					}
				}

				String host = null;
				try {
					host = Uri.parse(url).getHost();
				} catch (Exception exp) {
				}
				if (TextUtils.isEmpty(host)) return super.shouldOverrideUrlLoading(view, url);

				final int index = host.indexOf("-");
				if (index != -1) {
					String s = host.substring(index + 1, host.length());
					String javaScript = NOTIFICATION_BEFORE + s + NOTIFICATION_LAST;    // jsBridge.popNotificationObject('1')

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						view.evaluateJavascript(javaScript, value -> {
						});
					}
					else {
						String result = JAVASCRIPT + COLON_CODE + javaScript;
						view.loadUrl(result);
					}
					return true;
				}
				return super.shouldOverrideUrlLoading(view, url);
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				if (mWebViewWatcher != null) {
					mWebViewWatcher.onWebPageStart();
				}
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				if (!isWebViewFinished) {
					isWebViewFinished = true;
					FEWebViewJsUtil.isFromApp(mContentWebView);
				}
				if (mWebViewWatcher != null) {
					mWebViewWatcher.onWebPageFinished();
				}
				addBigImageViewListener();

			}

			@Override public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				FELog.e("onReceivedError", "failingUrl = " + failingUrl + " , description = " + description);
				super.onReceivedError(view, errorCode, description, failingUrl);
			}

			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {//兼容部分手机https
				FELog.i("onReceived SslError : " + error.getUrl() + ", " + error.toString());
				handler.proceed();
			}
		});

		mContentWebView.requestFocusFromTouch();
	}

	public void setWebViewWatcher(WebViewWatcher webViewWatcher) {
		this.mWebViewWatcher = webViewWatcher;
	}

	/**
	 * 设置详情内容
	 * @param content 详情内容
	 * @param needSupplementStyle 是否需要补充部分 html 样式
	 */
	public void setParticularContent(String url, String content, boolean needSupplementStyle) {
		FELog.i("setParticularContent url = " + url);
		isFeForm = false;
		mContentWebView.removeAllViews();
		mContentWebView.getSettings().setSupportZoom(true);
		mContentWebView.getSettings().setLoadWithOverviewMode(true);
		mContentWebView.getSettings().setBuiltInZoomControls(true);
		mContentWebView.getSettings().setDisplayZoomControls(false);
		if (TextUtils.isEmpty(content)) {
			mContentWebView.loadDataWithBaseURL(url, "", "text/html", "UTF-8", null);
			return;
		}
		if (needSupplementStyle) {
			StringBuilder sb = new StringBuilder(content);
			sb.append(HTML_STYLE_IMAGE_CENTER);
			if (content.contains("<table") || content.contains("</table>")) {
				sb.append(HTML_STYLE_TABLE_BORDER);
			}
			content = sb.toString();
			FELog.i(content);
			mContentWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
			mContentWebView.loadDataWithBaseURL(url, content, "text/html", "UTF-8", null);
			return;
		}
		mContentWebView.getSettings().setLoadWithOverviewMode(true);
		mContentWebView.getSettings().setUseWideViewPort(true);
		content += HTML_STYLE_VIEW_PORT;
		mContentWebView.loadDataWithBaseURL(url, content, "text/html", "UTF-8", null);
	}

	public void setParticularContentByUrl(String url) {
		isFeForm = true;
		FELog.i("URL = " + url);
		mContentWebView.removeAllViews();
		mContentWebView.getSettings().setSupportZoom(true);
		mContentWebView.getSettings().setLoadWithOverviewMode(true);
		mContentWebView.getSettings().setBuiltInZoomControls(true);
		mContentWebView.getSettings().setDisplayZoomControls(false);
		mContentWebView.getSettings().setUseWideViewPort(true);
		mContentWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

		mContentWebView.getSettings().setAllowFileAccess(true);
		mContentWebView.getSettings().setAllowContentAccess(true);
		mContentWebView.getSettings().setDatabaseEnabled(true);
		mContentWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		mContentWebView.getSettings().setGeolocationEnabled(true);
		TokenInject.injectToken(mContentWebView, url + "&FE_FROM_APP=true");
	}

	public void setContentSupplement(final List<SupplyContent> supplyList) {
		if (CommonUtil.isEmptyList(supplyList)) return;
		if (mSupplementView == null) {
			ViewStub supplementStubView = (ViewStub) findViewById(R.id.viewStubContentSupplement);
			mSupplementView = supplementStubView.inflate();
		}
		TextView tvLabel = (TextView) mSupplementView.findViewById(R.id.tvParticularLabel);
		RelativeElegantLayout reLayout = (RelativeElegantLayout) mSupplementView.findViewById(R.id.relativeElegantLayout);
		reLayout.setAdapter(new RelativeElegantAdapter<SupplyContent>(getContext(), R.layout.item_particular_supplement, supplyList) {
			@Override
			public void initItemViews(View view, int position, SupplyContent item) {
				getTextView(view, R.id.tvSendTime).setText(item.getSendTime());
				getTextView(view, R.id.tvSendUser).setText(item.getSendUser());
				getTextView(view, R.id.tvContent).setText(item.getContent());
			}
		});
		tvLabel.setText(String.format(getResources().getString(R.string.supplement_count_tip), supplyList.size()));
	}

	public void setContentModify(List<TrailContent> trailList) {
		if (CommonUtil.isEmptyList(trailList)) return;
		if (mModifyView == null) {
			ViewStub modifyStubView = (ViewStub) findViewById(R.id.viewStubContentModify);
			mModifyView = modifyStubView.inflate();
		}
		TextView tvLabel = (TextView) mModifyView.findViewById(R.id.tvParticularLabel);
		RelativeElegantLayout reLayout = (RelativeElegantLayout) mModifyView.findViewById(R.id.relativeElegantLayout);
		reLayout.setAdapter(new RelativeElegantAdapter<TrailContent>(getContext(), R.layout.item_particular_modify, trailList) {
			@Override
			public void initItemViews(View view, int position, TrailContent item) {
				getTextView(view, R.id.tvSendTime).setText(item.getSendTime());
				getTextView(view, R.id.tvSendUser).setText(item.getSendUser());
				getTextView(view, R.id.tvContent).setText(item.getContent());
			}
		});
		tvLabel.setText(String.format(getResources().getString(R.string.modification_count_tip), trailList.size()));
	}

	public TouchableWebView getWebView() {
		return mContentWebView;
	}

	public interface WebViewWatcher {

		void onWebPageStart();

		void onWebPageFinished();

		boolean shouldUrlIntercept(String url, boolean isFeForm);

		void onJsCallback(int jsControlInfo);
	}

	private class Controller {

		@JavascriptInterface
		public void runOnAndroidJavaScript(String jsonStr) {
			mContentWebView.getHandler().post(() -> {
				if (mWebViewWatcher != null) {
					FELog.e("runOnAndroidJavaScript:" + jsonStr);
					try {
						String iqContent;
						final JSONObject properties = new JSONObject(jsonStr);
						final JSONObject iq = properties.getJSONObject("userInfo");
						iqContent = iq.toString();
						JSControlInfo controlInfo = GsonUtil.getInstance()
								.fromJson(JSControlInfo.formatJsonString(iqContent), JSControlInfo.class);
						mWebViewWatcher.onJsCallback(controlInfo.getUiControlType());
					} catch (Exception e) {
					}
				}
			});
		}

		@JavascriptInterface
		public void showBigImage(String url, String[] images) {
			if (TextUtils.isEmpty(url)) return;
			ArrayList<String> imageList = new ArrayList<>();
			int position = 0;
			if (images != null && images.length >= 1) {
				for (int i = 0; i < images.length; i++) {
					imageList.add(images[i]);
					if (url.equals(images[i])) {
						position = i;
					}
				}
			}
			String serverAddress = "";
			if (url.startsWith("http") || (url.startsWith("record:image/") && url.contains("base64"))) {
				serverAddress = url;
			}
			else {
				serverAddress = CoreZygote.getLoginUserServices().getServerAddress() + url;
			}

			Intent intent = new Intent(mContentWebView.getContext(), BigImageJsActivity.class);
			intent.putExtra("localUrl", serverAddress);
			intent.putExtra("selectPosition", position);
			intent.putStringArrayListExtra("imageList", imageList);
			mContentWebView.getContext().startActivity(intent);
		}
	}

	private void addBigImageViewListener() {
		String javaScript = readJS();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			mContentWebView.evaluateJavascript(javaScript, value -> {
			});
		}
		else {
			mContentWebView.loadUrl("javascript:" + javaScript);
		}
	}

	private String readJS() {
		try {
			InputStream inStream = CoreZygote.getContext().getAssets().open("image_click.txt");
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			byte[] bytes = new byte[1024];
			int len = 0;
			while ((len = inStream.read(bytes)) > 0) {
				outStream.write(bytes, 0, len);
			}
			return outStream.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void stopLoad() {
		if (mContentWebView != null) {
			mContentWebView.stopLoading();
		}
	}

}
