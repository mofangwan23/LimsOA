package cn.flyrise.feep.x5;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author 社会主义接班人
 * @since 2018-09-18 13:46
 */
public class X5WebViewClient extends WebViewClient {

	private final X5BrowserDelegate delegate;
	private boolean isNewForm = false;
	private static final Set<String> adDomain = new HashSet<>(Arrays.asList("a.baidu.com",
			"baidutv.baidu.com", "bar.baidu.com", "c.baidu.com", "cjhq.baidu.com",
			"drmcmm.baidu.com", "e.baidu.com", "eiv.baidu.com", "hc.baidu.com",
			"hm.baidu.com", "ma.baidu.com", "nsclick.baidu.com", "spcode.baidu.com",
			"tk.baidu.com", "union.baidu.com", "ucstat.baidu.com", "utility.baidu.com",
			"utk.baidu.com", "focusbaiduafp.allyes.com", "a.baidu.com",
			"adm.baidu.com", "baidutv.baidu.com", "banlv.baidu.com",
			"bar.baidu.com", "c.baidu.com", "cb.baidu.com", "cbjs.baidu.com",
			"cjhq.baidu.com", "cpro.baidu.com", "dl.client.baidu.com", "drmcmm.baidu.com",
			"dzl.baidu.com", "e.baidu.com", "eiv.baidu.com", "gimg.baidu.com",
			"guanjia.baidu.com", "hc.baidu.com", "hm.baidu.com", "iebar.baidu.com",
			"ikcode.baidu.com", "ma.baidu.com", "neirong.baidu.com", "nsclick.baidu.com",
			"pos.baidu.com", "s.baidu.com", "sobar.baidu.com", "sobartop.baidu.com",
			"spcode.baidu.com", "tk.baidu.com", "tkweb.baidu.com", "tongji.baidu.com",
			"toolbar.baidu.com", "tracker.baidu.com", "ucstat.baidu.com", "ulic.baidu.com",
			"union.baidu.com", "unstat.baidu.com", "utility.baidu.com", "utk.baidu.com",
			"ubmcmm.baidustatic.com", "wangmeng.baidu.com", "wm.baidu.com"));

	public X5WebViewClient(X5BrowserDelegate delegate, boolean isNewForm) {
		this.delegate = delegate;
		this.isNewForm = isNewForm;
	}

	@Override public boolean shouldOverrideUrlLoading(WebView webView, String s) {
		return delegate.shouldOverrideUrlLoading(s);
	}

	@Override public void onPageFinished(WebView webView, String s) {
		super.onPageFinished(webView, s);
		// 不管三七二十一，直接调用 H5 的方法咯~
		StringBuilder sb = new StringBuilder();
		sb.append("jsBridge.trigger('SetWebHTMLEditorContent',{\"OcToJs_JSON\":{");
		if (isNewForm) {
			sb.append("\"actionType\":\"-4\"");
		}
		else {
			sb.append("\"actionType\":\"-3\"");
		}
		sb.append("}})");
		if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
			webView.evaluateJavascript(sb.toString(), null);
		}
		else {
			webView.loadUrl("javascript:" + sb.toString());
		}
	}

	@Override public WebResourceResponse shouldInterceptRequest(WebView webView, String s) {
		if (adDomain.contains(s)) {
			return new WebResourceResponse(null, null, null);
		}
		return super.shouldInterceptRequest(webView, s);
	}

	@Override public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
		sslErrorHandler.proceed();
	}
}
