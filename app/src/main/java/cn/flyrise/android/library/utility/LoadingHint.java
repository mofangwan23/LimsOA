package cn.flyrise.android.library.utility;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.CommonUtil;

/**
 * X x x .
 */
public class LoadingHint {

	private final static String KEY_PROGRESS = "progress";
	private final static String KEY_DISPLAY_TXT = "displayText";

	private static final int MSG_PROGRESS = 1001134;
	private static LoadingHint sInstance;

	private boolean isLoading = false;
	private Dialog mLoadingDialog;
	private TextView mTvProgress;
	private onKeyDownListener mKeyDownListener;
	private DialogInterface.OnDismissListener mDismissListener;

	private Handler mHandler = new Handler() {
		@Override public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (MSG_PROGRESS == msg.what) {
				if (mTvProgress != null) {
					Bundle args = msg.getData();

					int num = args.getInt(KEY_PROGRESS, 0);
					String text = args.getString(KEY_DISPLAY_TXT, CommonUtil.getString(R.string.more_finish_downlaod))
							+ ":" + num + "%";

					mTvProgress.setVisibility(View.VISIBLE);
					mTvProgress.setText(text);
				}
			}
		}
	};

	public static void show(Context context) {
		show(context, false);
	}

	public static void show(Context context, boolean isClick) {
		if (sInstance == null) {
			sInstance = new LoadingHint();
		}

		if (sInstance.isLoading) {
			return;
		}
		sInstance.isLoading = true;
		sInstance.showDialog(context, isClick);
	}

	public static void showProgress(int progress) {
		showProgress(progress, null);
	}

	public static void showProgress(int progress, String text) {
		if (sInstance == null) {
			sInstance = new LoadingHint();
		}

		if (sInstance.mHandler != null) {
			Message msg = sInstance.mHandler.obtainMessage();
			msg.what = MSG_PROGRESS;

			Bundle args = new Bundle();
			args.putInt(KEY_PROGRESS, progress);
			args.putString(KEY_DISPLAY_TXT, text);
			msg.setData(args);

			sInstance.mHandler.sendMessage(msg);
		}
	}

	public static int hide() {
		if (sInstance == null) {
			return 0;
		}

		try {
			sInstance.isLoading = false;
			sInstance.mTvProgress = null;
			if (sInstance.mLoadingDialog != null && sInstance.mLoadingDialog.isShowing()) {
				sInstance.mLoadingDialog.dismiss();
			}
			sInstance.mLoadingDialog = null;
			sInstance.mKeyDownListener = null;
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		return 0;
	}

	public static boolean isLoading() {
		if (sInstance == null) {
			return false;
		}
		return sInstance.isLoading;
	}

	private void showDialog(Context context, boolean isClick) {
		mLoadingDialog = new Dialog(context, R.style.TransparentDialogStyle);
		View view = LayoutInflater.from(context).inflate(R.layout.core_view_loading_dialog, null);
		mLoadingDialog.setContentView(view);
		mLoadingDialog.setOnKeyListener(onKeyListener);                     //监听返回键，主要用于上传附件时关闭线程
		mLoadingDialog.setCanceledOnTouchOutside(isClick);                  //设置点击屏幕dialog不消失
		mTvProgress = (TextView) mLoadingDialog.findViewById(R.id.tvLoadingLabel);
		mLoadingDialog.setOnDismissListener(dialog1 -> {
			if (mDismissListener != null) {
				mDismissListener.onDismiss(dialog1);
				if (sInstance != null) sInstance.mDismissListener = null;
			}
		});
		mLoadingDialog.show();
	}

	private DialogInterface.OnKeyListener onKeyListener = new DialogInterface.OnKeyListener() {
		@Override public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				if (mKeyDownListener != null) {
					mKeyDownListener.onKeyDown(keyCode, event);
				}
				mKeyDownListener = null;
				hide();
				return true;
			}
			return false;
		}
	};

	public static void setOnKeyDownListener(onKeyDownListener listener) {
		if (sInstance != null) {
			sInstance.mKeyDownListener = listener;
		}
	}

	public static void setOnDismissListener(DialogInterface.OnDismissListener listener) {
		if (sInstance != null) {
			sInstance.mDismissListener = listener;
		}
	}

	public interface onKeyDownListener {

		void onKeyDown(int keyCode, KeyEvent event);
	}
}

