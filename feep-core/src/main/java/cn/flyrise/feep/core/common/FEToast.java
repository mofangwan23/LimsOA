package cn.flyrise.feep.core.common;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.R;

/**
 * @author ZYP
 * @since 2017-02-08 13:21
 */
public class FEToast {

	private static Toast errorToast;
	private static Toast sToast;
	private static String mTempStr;

	public static void showMessage(String text) {
		if (TextUtils.isEmpty(text)) return;
		if (sToast == null) {
			mTempStr = text;
			newToast(CoreZygote.getContext());
		}
		else if (!TextUtils.equals(text, mTempStr)) {//内容不同时，取消当前显示的，弹出最新提示
			cancel();
			newToast(CoreZygote.getContext());
			mTempStr = text;
		}
		TextView tvHint = sToast.getView().findViewById(R.id.toast_hint);
		tvHint.setText(text);
		sToast.show();
	}

	private static void newToast(Context context) {
		sToast = new Toast(context);
		sToast.setDuration(Toast.LENGTH_SHORT);
		sToast.setView(LayoutInflater.from(context).inflate(R.layout.core_view_toast, null));
	}

	public static void showMessage(String text, OnAttachStateChangeListener listener) {
		Toast toast = new Toast(CoreZygote.getContext());
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(LayoutInflater.from(CoreZygote.getContext()).inflate(R.layout.core_view_toast, null));
		if (listener != null) {
			toast.getView().addOnAttachStateChangeListener(listener);
		}
		TextView tvHint = toast.getView().findViewById(R.id.toast_hint);
		tvHint.setText(text);
		toast.show();

	}

	public static void showMessage(int resourceID) {
		Context context = CoreZygote.getContext();
		if (sToast == null) {
			sToast = new Toast(context);
			sToast.setView(LayoutInflater.from(context).inflate(R.layout.core_view_toast, null));
			sToast.setDuration(Toast.LENGTH_SHORT);
		}
		TextView tvHint = sToast.getView().findViewById(R.id.toast_hint);
		tvHint.setText(context.getString(resourceID));
		sToast.show();
	}

	/**
	 * 应用登录页退到后台
	 */
	public static void showLoginError(Context context, @DrawableRes int resource) {
		View view = LayoutInflater.from(context).inflate(R.layout.toast_login_layout, null);
		if (errorToast == null) {
			errorToast = new Toast(context);
			errorToast.setDuration(Toast.LENGTH_LONG);
			errorToast.setView(view);
		}
		ImageView imageView = view.findViewById(R.id.imageview);
		imageView.setImageResource(resource);
		if (context instanceof Activity && ((Activity) context).isFinishing()) {
			return;
		}
		errorToast.show();
	}


	public static void showContentMessage(String text) {
		if (TextUtils.isEmpty(text)) return;
		Toast toast = new Toast(CoreZygote.getContext());
		toast.setView(LayoutInflater.from(CoreZygote.getContext()).inflate(R.layout.toast_content_login_layout, null));
		toast.setDuration(Toast.LENGTH_SHORT);
		TextView tvHint = toast.getView().findViewById(R.id.text);
		tvHint.setText(text);
		toast.show();
	}

	public static void cancel() {
		if (sToast != null) {
			sToast.cancel();
		}
	}

}
