package cn.flyrise.feep.cordova.utils;

import android.os.Build;
import android.webkit.WebView;
import cn.flyrise.feep.core.common.FELog;

/**
 * 新建：陈冕;
 * 日期： 2018-6-11-11:07.
 */

public class FEWebViewJsUtil {

	public static void isFromApp(WebView webView) {//js用于区分是否是移动端
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			webView.evaluateJavascript(getData(), FELog::i);
		}
		else {
			webView.loadUrl("javascript:" + getData());
		}
	}

	private static String getData() {
		return "jsBridge.trigger('SetWebHTMLEditorContent',{\"OcToJs_JSON\":{\"actionType\":\"-3\"}})";
	}

}
