/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-8-28 下午2:49:18
 */

package cn.flyrise.feep.form.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.Keep;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.feep.R;

/**
 * 类功能描述：</br>
 */

/**
 * 继承WebChromeClient类 对js弹出框时间进行处理
 */
@Keep
public class PopupChromeClient extends WebChromeClient {

	/**
	 * 处理alert弹出框
	 */
	@Override
	public boolean onJsAlert(WebView view, String url, final String message, final JsResult result) {
		final Context context = view.getContext();
		final boolean isFinished = ((Activity) context).isFinishing();
		if (isFinished) {
			return true;
		}
		// 对alert的简单封装
		new AlertDialog.Builder(context).setTitle(context.getString(R.string.dialog_default_title)).setMessage(message)
				.setPositiveButton(context.getString(R.string.dialog_button_ok), (arg0, arg1) -> {
					result.confirm();
				}).show();
		LoadingHint.hide();
		return true;
	}

	@Override
	@JavascriptInterface
	public void onProgressChanged(WebView view, int newProgress) {
		super.onProgressChanged(view, newProgress);
		final Context context = view.getContext();
		if (newProgress == 10) {
			LoadingHint.show(context);
		}
		else if (newProgress == 100) {
			if (LoadingHint.isLoading()) {
				LoadingHint.hide();
			}
		}
	}

}
