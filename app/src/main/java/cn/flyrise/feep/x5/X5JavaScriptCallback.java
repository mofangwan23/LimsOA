package cn.flyrise.feep.x5;

import android.webkit.JavascriptInterface;

/**
 * @author 社会主义接班人
 * @since 2018-09-18 14:20
 */
public final class X5JavaScriptCallback {

	private final X5BrowserDelegate delegate;

	public X5JavaScriptCallback(X5BrowserDelegate delegate) {
		this.delegate = delegate;
	}

	@JavascriptInterface
	public void runOnAndroidJavaScript(String jsonStr) {
		if (this.delegate != null) {
			try {
				this.delegate.analysisJsonFromJavaScriptCall(jsonStr);
			} catch (Exception exp) {
				exp.printStackTrace();
			}
		}
	}

}
