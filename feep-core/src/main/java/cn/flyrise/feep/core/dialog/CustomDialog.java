package cn.flyrise.feep.core.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import cn.flyrise.feep.core.R;

public class CustomDialog extends Dialog {

	public CustomDialog(@NonNull Context context) {
		super(context);
	}

	public CustomDialog(@NonNull Context context, int themeResId) {
		super(context, themeResId);
	}

	protected CustomDialog(@NonNull Context context, boolean cancelable,
			@Nullable OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	public static class Builder {

		private Context context;
		private String title;
		private String message;
		private String positiveButtonText;
		private String negativeButtonText;
		private View contentView;
		private boolean cancelable;
		private OnClickListener positiveButtonClickListener;
		private OnClickListener negativeButtonClickListener;

		public Builder(Context context) {
			this.context = context;
		}

		public Builder setTitle(String title) {
			this.title = title;
			return this;
		}

		public Builder setMessage(String messge) {
			this.message = messge;
			return this;
		}

		private Builder setConvertView(View contentView) {
			this.contentView = contentView;
			return this;
		}

		public Builder setCancelable(boolean cancelable){
			this.cancelable = cancelable;
			return this;
		}

		public Builder setPositiveButtonText(String positiveButtonText, OnClickListener listener) {
			this.positiveButtonText = positiveButtonText;
			this.positiveButtonClickListener = listener;
			return this;
		}

		public Builder setNegativeButtonText(String negativeButtonText, OnClickListener listener) {
			this.negativeButtonText = negativeButtonText;
			this.negativeButtonClickListener = listener;
			return this;
		}

		public CustomDialog create() {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final CustomDialog dialog = new CustomDialog(context, R.style.my_custom_dialog);
			View layout = inflater.inflate(R.layout.core_custom_comfirm_dialog, null);
			dialog.addContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			TextView tvTitle = layout.findViewById(R.id.tv_custom_dialog_title);
			TextView tvContent = layout.findViewById(R.id.tv_custom_dialog_content);
			TextView tvNative = layout.findViewById(R.id.tv_custom_dialog_native);
			TextView tvPositive = layout.findViewById(R.id.tv_custom_dialog_positive);
			View longLine = layout.findViewById(R.id.view_long_line_custom_dialog);
			View shortLine = layout.findViewById(R.id.view_short_line_custom_dialog);

			if (!TextUtils.isEmpty(title) && tvTitle != null) {
				tvTitle.setVisibility(View.VISIBLE);
				tvTitle.setText(title);
			}

			if (!TextUtils.isEmpty(message) && tvContent != null) {
				tvContent.setText(message);
			}

			if (!TextUtils.isEmpty(negativeButtonText) && tvNative != null) {
				tvNative.setVisibility(View.VISIBLE);
				tvNative.setText(negativeButtonText);
				tvNative.setOnClickListener(new View.OnClickListener() {
					@Override public void onClick(View v) {
						dialog.dismiss();
						if (negativeButtonClickListener!=null){
							negativeButtonClickListener.onClick(dialog,DialogInterface.BUTTON_NEGATIVE);
						}
					}
				});
				if (shortLine != null) {
					shortLine.setVisibility(View.VISIBLE);
				}
			}

			if ( tvPositive != null) {
				if (!TextUtils.isEmpty(positiveButtonText)){
					tvPositive.setText(positiveButtonText);
				}
				tvPositive.setOnClickListener(new View.OnClickListener() {
					@Override public void onClick(View v) {
						dialog.dismiss();
						if (positiveButtonClickListener!=null){
							positiveButtonClickListener.onClick(dialog,DialogInterface.BUTTON_POSITIVE);
						}
					}
				});
			}

			dialog.setContentView(layout);
			dialog.setCancelable(cancelable);
			dialog.setCanceledOnTouchOutside(cancelable);
			return dialog;
		}


	}

	public void showDialog(){
		Window dialogWindow = getWindow();
		if (dialogWindow!=null){
			WindowManager m = dialogWindow.getWindowManager();
			Display d = m.getDefaultDisplay(); // 获取屏幕宽、高
			WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
			// 设置宽度
			p.width = (int) (d.getWidth() * 0.80); // 宽度设置为屏幕的0.90
			p.gravity = Gravity.CENTER;//设置位置
			dialogWindow.setAttributes(p);
		}
		show();
	}
}
