package cn.flyrise.feep.core.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.StringRes;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import cn.flyrise.feep.core.R;

/**
 * klc 2016/09/09
 */
public class FEMaterialEditTextDialog {

	private View mBottomOperateView;
	private final AlertDialog mDialog;

	private FEMaterialEditTextDialog(final Builder builder) {
		final View dialogView = builder.dialogView;
		final EditText editText = (EditText) dialogView.findViewById(R.id.txt_filename);
		final TextView tvTitle = dialogView.findViewById(R.id.tvTitle);

		final CheckBox checkBox = (CheckBox) dialogView.findViewById(R.id.checkBox);

		final TextView positiveButton = (TextView) dialogView.findViewById(R.id.tvConfirm);
		final TextView negativeButton = (TextView) dialogView.findViewById(R.id.tvCancel);
		mBottomOperateView = dialogView.findViewById(R.id.llBottomOperateView);

		if (TextUtils.isEmpty(builder.title)) {
			tvTitle.setVisibility(View.GONE);
		}
		else {
			tvTitle.setVisibility(View.VISIBLE);
			tvTitle.setText(builder.title);
		}

		editText.setHint(builder.hint);
		editText.setText(builder.defaultText);
		if (builder.inputType != -1) {
			editText.setInputType(builder.inputType);
		}
		if (builder.maxSize != -1) {
			editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(builder.maxSize)});
		}
		if (TextUtils.isEmpty(builder.defaultText)) {
			positiveButton.setEnabled(false);
		}
		else {
			positiveButton.setEnabled(true);
			positiveButton.setTextColor(builder.context.getResources().getColor(R.color.core_default_accent_color));
		}
		positiveButton.setTextColor(Color.GRAY);

		if (TextUtils.isEmpty(builder.checkBoxText)) {
			checkBox.setVisibility(View.GONE);
		}
		else {
			checkBox.setVisibility(View.VISIBLE);
			checkBox.setText(builder.checkBoxText);
		}

		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				String input = s.toString().trim();
				if (TextUtils.isEmpty(input)) {
					positiveButton.setEnabled(false);
					positiveButton.setTextColor(Color.GRAY);
				}
				else {
					positiveButton.setEnabled(true);
					positiveButton.setTextColor(builder.context.getResources().getColor(R.color.core_default_accent_color));
				}
			}
		});

		if (!TextUtils.isEmpty(builder.positiveButtonText)) {       // 确定按钮
			showBottomOperateView();
			positiveButton.setVisibility(View.VISIBLE);
			positiveButton.setText(builder.positiveButtonText);
			positiveButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mDialog.dismiss();
					if (builder.positiveButtonListener != null) {
						builder.positiveButtonListener.onClick(mDialog, editText.getText().toString().trim(), checkBox.isChecked());
					}
				}
			});
		}

		if (!TextUtils.isEmpty(builder.negativeButtonText)) {       // 取消按钮
			showBottomOperateView();
			negativeButton.setVisibility(View.VISIBLE);
			negativeButton.setText(builder.negativeButtonText);
			negativeButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mDialog.dismiss();
					if (builder.negativeButtonListener != null) {
						builder.negativeButtonListener.onClick(mDialog, editText.getText().toString().trim(), false);
					}
				}
			});
		}

		this.mDialog = new AlertDialog
				.Builder(builder.context)
				.setView(dialogView)
				.setCancelable(builder.cancelable)
				.create();
		this.mDialog.setCanceledOnTouchOutside(builder.cancelable);
	}

	public void show() {
		tryHideSoftKeyBoard();
		if (!mDialog.isShowing()) {
			mDialog.show();
		}
	}

	private void tryHideSoftKeyBoard() {                    // Dialog显示的时候，尝试隐藏软键盘
		if (mDialog.getContext() instanceof Activity) {
			View currentFocus = ((Activity) mDialog.getContext()).getCurrentFocus();
			if (currentFocus != null) {
				InputMethodManager imm = (InputMethodManager) mDialog.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
			}
		}
	}

	private void showBottomOperateView() {
		if (mBottomOperateView.getVisibility() == View.VISIBLE) {
			return;
		}
		mBottomOperateView.setVisibility(View.VISIBLE);
	}

	public static final class Builder {

		private Context context;
		private View dialogView;

		private String title;
		private String hint;
		private String defaultText;
		private int maxSize;

		private boolean cancelable = true;
		private String checkBoxText;

		private String positiveButtonText;
		private OnClickListener positiveButtonListener;

		private String negativeButtonText;
		private OnClickListener negativeButtonListener;

		private int inputType = -1;

		public Builder(Context context) {
			this.context = context;
			this.dialogView = LayoutInflater.from(context).inflate(R.layout.view_edittext_dialog, null);
			this.maxSize = -1;
		}

		public Builder setTitle(String title) {
			this.title = title;
			return this;
		}

		public Builder setHint(String hint) {
			this.hint = hint;
			return this;
		}

		public Builder setCancelable(boolean isCancelable) {
			this.cancelable = isCancelable;
			return this;
		}

		public Builder setPositiveButton(@StringRes int textId, OnClickListener listener) {
			this.positiveButtonText = context.getResources().getString(textId);
			this.positiveButtonListener = listener;
			return this;
		}

		public Builder setPositiveButton(String text, OnClickListener listener) {
			if (text == null) {
				text = context.getResources().getString(R.string.core_btn_positive);
			}
			this.positiveButtonText = text;
			this.positiveButtonListener = listener;
			return this;
		}

		public Builder setNegativeButton(@StringRes int textId, OnClickListener listener) {
			this.negativeButtonText = context.getResources().getString(textId);
			this.negativeButtonListener = listener;
			return this;
		}

		public Builder setNegativeButton(String text, OnClickListener listener) {
			if (text == null) {
				text = context.getResources().getString(R.string.core_btn_negative);
			}
			this.negativeButtonText = text;
			this.negativeButtonListener = listener;
			return this;
		}

		public Builder setCheckBoxText(String checkBoxText) {
			this.checkBoxText = checkBoxText;
			return this;
		}

		public Builder setDefaultText(String defaultText) {
			this.defaultText = defaultText;
			return this;
		}

		public Builder setMaxSize(int maxSize) {
			this.maxSize = maxSize;
			return this;
		}

		public Builder setInputType(int inputType) {
			this.inputType = inputType;
			return this;
		}

		public FEMaterialEditTextDialog build() {
			return new FEMaterialEditTextDialog(this);
		}
	}

	public interface OnClickListener {

		void onClick(AlertDialog dialog, String input, boolean check);
	}
}
