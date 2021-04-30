package cn.flyrise.feep.cordova.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import cn.flyrise.feep.core.common.FELog;
import org.apache.cordova.CordovaWebView;

/**
 * @author ZYP
 * @since 2017-11-20 18:32
 */
public class XCordovaWebView extends CordovaWebView {

	private WPSActionReceiver mActionReceiver;

	public XCordovaWebView(Context context) {
		super(context);
		mActionReceiver = registerWPSAction(context);
	}

	public XCordovaWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mActionReceiver = registerWPSAction(context);
	}

	public XCordovaWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mActionReceiver = registerWPSAction(context);
	}

	@Override public boolean onKeyUp(int keyCode, KeyEvent event) {
		FELog.i(">>> onKeyUp ");
		return super.onKeyUp(keyCode, event);
	}

	@Override public boolean onKeyDown(int keyCode, KeyEvent event) {
		FELog.i("<<< onKeyDown ");
		return super.onKeyDown(keyCode, event);
	}

	private WPSActionReceiver registerWPSAction(Context context) {
		WPSActionReceiver receiver = new WPSActionReceiver();
		IntentFilter filter = new IntentFilter("cn.wps.moffice.file.close");
		context.registerReceiver(receiver, filter);
		return receiver;
	}


	private class WPSActionReceiver extends BroadcastReceiver {
		@Override public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (TextUtils.equals(action, "cn.wps.moffice.file.close")) {
				FELog.i("监听到 WPS 返回键 Action : " + action);
			}
		}
	}

	@Override public void handleDestroy() {
		super.handleDestroy();
		if (mActionReceiver != null) {
			getContext().unregisterReceiver(mActionReceiver);
		}
	}
}
