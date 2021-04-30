package cn.flyrise.feep.core.dialog;

import android.app.AlertDialog;
import android.content.Context;

/**
 * Create by cm132 on 2018/12/28.
 * Describe:
 */
public class FeDialog extends AlertDialog {

	protected FeDialog(Context context) {
		super(context);
	}

	protected FeDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	protected FeDialog(Context context, int themeResId) {
		super(context, themeResId);
	}

	@Override public void show() {
		try {
			super.show();
		} catch (IllegalStateException e) {

		}
	}
}
