package cn.flyrise.feep.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Keep;
import android.util.Log;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;

import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.dialog.FEMaterialDialog.OnClickListener;
import com.tencent.bugly.crashreport.CrashReport;

@Keep
public class FEWebChromeClient extends WebChromeClient {

	// For 5.0 以下版本
	private ValueCallback<Uri> mUploadMessage;

	// For 5.0 及以上版本
	private ValueCallback<Uri[]> mUploadMessages;

	public static final int REQUEST_SELECT_FILE = 0x00001789;
	public final static int FILECHOOSER_RESULTCODE = 0x00001790;
	private final Activity mActivity;
	private final ProgressBar progressh;

	public FEWebChromeClient(Activity activity, ProgressBar progressBar) {
		this.mActivity = activity;
		this.progressh = progressBar;
	}

	public ValueCallback<Uri[]> getUploadMessages() {
		return mUploadMessages;
	}

	public void setUploadMessages(ValueCallback<Uri[]> uploadMessages) {
		this.mUploadMessages = uploadMessages;
	}

	public ValueCallback<Uri> getUploadMessage() {
		return mUploadMessage;
	}

	public void setUploadMessage(ValueCallback<Uri> uploadMessage) {
		this.mUploadMessage = uploadMessage;
	}

	@Override
	public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
		callback.invoke(origin, true, false);
		super.onGeolocationPermissionsShowPrompt(origin, callback);
	}


	@Override
	public void onProgressChanged(WebView view, int newProgress) {
		CrashReport.setJavascriptMonitor(view, true);
		super.onProgressChanged(view, newProgress);
		if (newProgress == 100 && progressh != null) {
			progressh.setVisibility(View.GONE);
		}
		if (newProgress >= 100) {
			view.getSettings().setBlockNetworkImage(false);
		}
	}

	// For Lollipop 5.0+ Devices
	public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
		mUploadMessages = filePathCallback;
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("*/*");
		try {
			mActivity.startActivityForResult(intent, REQUEST_SELECT_FILE);
		} catch (ActivityNotFoundException e) {
			mUploadMessages = null;
			Toast.makeText(mActivity.getApplicationContext(), "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}

	//For Android 4.1 only
	protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
		mUploadMessage = uploadMsg;
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("*/*");
		mActivity.startActivityForResult(Intent.createChooser(intent, "File Browser"), FILECHOOSER_RESULTCODE);
	}

	protected void openFileChooser(ValueCallback uploadMsg, String acceptType) {
		mUploadMessage = uploadMsg;
		Intent i = new Intent(Intent.ACTION_GET_CONTENT);
		i.addCategory(Intent.CATEGORY_OPENABLE);
		i.setType("*/*");
		mActivity.startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
	}

	protected void openFileChooser(ValueCallback<Uri> uploadMsg) {
		mUploadMessage = uploadMsg;
		Intent i = new Intent(Intent.ACTION_GET_CONTENT);
		i.addCategory(Intent.CATEGORY_OPENABLE);
		i.setType("*/*");
		mActivity.startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
	}

	@Override
	public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
		new FEMaterialDialog.Builder(mActivity).setMessage(message)
				.setCancelable(true)
				.setPositiveButton(null, Dialog::dismiss).build().show();
		result.cancel();
		return true;
	}
}