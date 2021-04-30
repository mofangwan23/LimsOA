package cn.flyrise.feep.x5;

import static cn.flyrise.feep.x5.X5BrowserFragment.CODE_SELECT_FILE_V4;
import static cn.flyrise.feep.x5.X5BrowserFragment.CODE_SELECT_FILE_V5;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient.FileChooserParams;
import android.widget.Toast;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import com.tencent.smtt.export.external.interfaces.GeolocationPermissionsCallback;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;

/**
 * @author 社会主义接班人
 * @since 2018-09-18 11:28
 */
public class X5WebChromeClient extends WebChromeClient {

	private ValueCallback<Uri> mUploadMessage;          // For 5.0 以下版本
	private ValueCallback<Uri[]> mUploadMessages;       // For 5.0 及以上版本
	private Activity activity;

	private OnProgressChangeListener progressChangeListener;

	public X5WebChromeClient(Activity activity) {
		this.activity = activity;
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

	public void setOnProgressChangeListener(OnProgressChangeListener listener) {
		this.progressChangeListener = listener;
	}

	@Override public void onGeolocationPermissionsShowPrompt(String s, GeolocationPermissionsCallback geolocationPermissionsCallback) {
		geolocationPermissionsCallback.invoke(s, true, false);
		super.onGeolocationPermissionsShowPrompt(s, geolocationPermissionsCallback);
	}

	@Override public void onProgressChanged(WebView webView, int i) {
		super.onProgressChanged(webView, i);
		if (progressChangeListener != null) {
			progressChangeListener.onProgressChanged(i);
		}
	}

	// For Lollipop 5.0+ Devices
	@Override
	public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
		mUploadMessages = filePathCallback;
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("*/*");
		try {
			activity.startActivityForResult(intent, CODE_SELECT_FILE_V5);
		} catch (ActivityNotFoundException e) {
			mUploadMessages = null;
			Toast.makeText(activity.getApplicationContext(), "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}

	//For Android 4.1 only
	@Override
	public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
		mUploadMessage = uploadMsg;
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("*/*");
		activity.startActivityForResult(Intent.createChooser(intent, "File Browser"), CODE_SELECT_FILE_V4);
	}

	protected void openFileChooser(ValueCallback uploadMsg, String acceptType) {
		mUploadMessage = uploadMsg;
		Intent i = new Intent(Intent.ACTION_GET_CONTENT);
		i.addCategory(Intent.CATEGORY_OPENABLE);
		i.setType("*/*");
		activity.startActivityForResult(Intent.createChooser(i, "File Browser"), CODE_SELECT_FILE_V4);
	}

	protected void openFileChooser(ValueCallback<Uri> uploadMsg) {
		mUploadMessage = uploadMsg;
		Intent i = new Intent(Intent.ACTION_GET_CONTENT);
		i.addCategory(Intent.CATEGORY_OPENABLE);
		i.setType("*/*");
		activity.startActivityForResult(Intent.createChooser(i, "File Chooser"), CODE_SELECT_FILE_V4);
	}

	@Override public boolean onJsAlert(WebView webView, String s, String s1, JsResult jsResult) {
		new FEMaterialDialog.Builder(activity)
				.setMessage(s)
				.setCancelable(true)
				.setPositiveButton(null, Dialog::dismiss)
				.build()
				.show();
		jsResult.cancel();
		return true;
	}

	public interface OnProgressChangeListener {

		void onProgressChanged(int progress);
	}
}
