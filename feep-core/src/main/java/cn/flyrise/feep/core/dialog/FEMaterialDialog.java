package cn.flyrise.feep.core.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ArrayRes;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import cn.flyrise.feep.core.R;
import cn.flyrise.feep.core.common.utils.PixelUtil;

/**
 * @author ZYP
 * @since 2016/8/23
 * <p/>
 * note: 如果没设置其他 Button，则默认只有一个 “确认” button。
 */
public class FEMaterialDialog {

	private View mBottomOperateView;
	private final FeDialog mDialog;

	private FEMaterialDialog(final Builder builder) {
		final View dialogView = builder.dialogView;
		TextView tvTitle = (TextView) dialogView.findViewById(R.id.tvTitle);
		TextView tvContent = (TextView) dialogView.findViewById(R.id.tvContent);

		TextView positiveButton = (TextView) dialogView.findViewById(R.id.tvConfirm);
		TextView negativeButton = (TextView) dialogView.findViewById(R.id.tvCancel);
		TextView neutralButton = (TextView) dialogView.findViewById(R.id.tvDefault);
		mBottomOperateView = dialogView.findViewById(R.id.llBottomOperateView);

		if (builder.withoutTitle) {
			tvTitle.setVisibility(View.GONE);
		}
		else {
			tvTitle.setVisibility(View.VISIBLE);
			if (TextUtils.isEmpty(builder.title)) {
				builder.title = builder.context.getResources().getString(R.string.core_prompt_title);
			}
			tvTitle.setText(builder.title);
		}

		if (!TextUtils.isEmpty(builder.message)) {
			tvContent.setText(builder.message);
		}

		if (builder.isMultiSelect || (builder.items != null && builder.items.length > 0)) {    // 多项菜单
			tvContent.setVisibility(View.GONE);
			ListView listView = (ListView) dialogView.findViewById(R.id.listView);
			listView.setVisibility(View.VISIBLE);
			if (builder.baseAdapter == null) {                      // 如果用户没设置Adapter 则使用默认的 ArrayAdapter...
				builder.baseAdapter = new ArrayAdapter(builder.context, R.layout.core_item_simple_list, builder.items);
			}

			if (builder.withDriver) {
				listView.setDivider(createDividerLine(builder.context));
			}

			listView.setAdapter(builder.baseAdapter);
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if (builder.itemClickListener != null) {
						builder.itemClickListener.onItemClickListener(mDialog, view, position);
					}
				}
			});
		}

		if (!TextUtils.isEmpty(builder.positiveButtonText)) {       // 确定按钮
			showBottomOperateView();
			positiveButton.setVisibility(View.VISIBLE);
			positiveButton.setText(builder.positiveButtonText);
			positiveButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (builder.positiveButtonListener != null) {
						builder.positiveButtonListener.onClick(mDialog);
					}
					mDialog.dismiss();
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
						builder.negativeButtonListener.onClick(mDialog);
					}
				}
			});
		}

		if (!TextUtils.isEmpty(builder.neutralButtonText)) {        // 预留的功能按钮...
			showBottomOperateView();
			neutralButton.setVisibility(View.VISIBLE);
			neutralButton.setText(builder.neutralButtonText);
			neutralButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mDialog.dismiss();
					if (builder.neutralButtonListener != null) {
						builder.neutralButtonListener.onClick(mDialog);
					}
				}
			});
		}

		if (builder.withoutTitle) {
			int padding = PixelUtil.dipToPx(10);
			dialogView.setPadding(padding, padding, padding, padding);
		}

		this.mDialog = new FeDialog(builder.context);
		mDialog.setView(dialogView);
		mDialog.setCancelable(builder.cancelable);
		this.mDialog.setCanceledOnTouchOutside(builder.cancelable);
		this.mDialog.setOnDismissListener(dialog -> {
			if (builder.dismissListener != null)
				builder.dismissListener.onDismissListener(dialog);
		});
	}

	public void show() {
		tryHideSoftKeyBoard();
		if (mDialog != null && !mDialog.isShowing()) {
			if (mDialog.getContext() instanceof Activity && ((Activity) mDialog.getContext()).isFinishing()) {
				return;
			}
			mDialog.show();
		}
	}

	public void dismiss() {
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
		}
	}

	private void tryHideSoftKeyBoard() {                    // Dialog显示的时候，尝试隐藏软键盘
		if (mDialog.getContext() instanceof Activity) {
			View currentFocus = ((Activity) mDialog.getContext()).getCurrentFocus();
			if (currentFocus != null) {
				InputMethodManager imm = (InputMethodManager) mDialog.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				boolean b = imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
			}
		}
	}

	public boolean isShowing() {
		return mDialog != null && mDialog.isShowing();
	}


	private void showBottomOperateView() {
		if (mBottomOperateView.getVisibility() == View.VISIBLE) {
			return;
		}
		mBottomOperateView.setVisibility(View.VISIBLE);
	}

	private Drawable createDividerLine(Context context) {
		Bitmap bitmap = Bitmap.createBitmap(1, 4, Bitmap.Config.ARGB_4444);
		Canvas cv = new Canvas(bitmap);
		cv.drawColor(Color.WHITE);

		Paint paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);        // 设置画笔类型
		paint.setStrokeWidth(PixelUtil.dipToPx(1));                   // 设置画笔粗细

		paint.setColor(0xffc5c4c1);
		cv.drawPoint(1, 1, paint);
		final Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);
		return drawable;
	}

	public static final class Builder {

		private Context context;
		private View dialogView;

		private String title;
		private String message;
		private boolean withDriver;
		private boolean cancelable = true;
		private boolean withoutTitle;

		private String positiveButtonText;
		private OnClickListener positiveButtonListener;

		private String negativeButtonText;
		private OnClickListener negativeButtonListener;

		private String neutralButtonText;
		private OnClickListener neutralButtonListener;

		private DismissListener dismissListener;

		private CharSequence[] items;
		private BaseAdapter baseAdapter;
		private OnItemClickListener itemClickListener;
		private boolean isMultiSelect = false;

		public Builder(Context context) {
			this.context = context;
			this.dialogView = LayoutInflater.from(context).inflate(R.layout.core_view_dialog, null);
		}

		public Builder setTitle(@StringRes int titleId) {
			this.title = context.getResources().getString(titleId);
			return this;
		}

		public Builder setTitle(String title) {
			this.title = title;
			return this;
		}

		public Builder setWithoutTitle(boolean withoutTitle) {
			this.withoutTitle = withoutTitle;
			return this;
		}

		public Builder setWithDivider(boolean withDivider) {
			this.withDriver = withDivider;
			return this;
		}

		public Builder setMessage(@StringRes int messageId) {
			this.message = context.getResources().getString(messageId);
			return this;
		}

		public Builder setMessage(String message) {
			this.message = message;
			return this;
		}

		public Builder setItemAdapter(BaseAdapter adapter) {
			this.baseAdapter = adapter;
			return this;
		}

		public Builder setItems(@ArrayRes int textIds, OnItemClickListener listener) {
			this.items = context.getResources().getStringArray(textIds);
			this.itemClickListener = listener;
			return this;
		}

		public Builder setItems(CharSequence[] items, OnItemClickListener listener) {
			this.items = items;
			this.itemClickListener = listener;
			return this;
		}

		public Builder setMultiItems(boolean isMultiSelect) {
			this.isMultiSelect = isMultiSelect;
			return this;
		}

		public Builder setOnItemClickListener(OnItemClickListener listener) {
			this.itemClickListener = listener;
			return this;
		}

		public Builder setItems(boolean isMultiSelect, CharSequence[] items, OnItemClickListener listener) {
			this.isMultiSelect = isMultiSelect;
			this.items = items;
			this.itemClickListener = listener;
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

		public Builder setNeutralButton(@StringRes int textId, OnClickListener listener) {
			this.neutralButtonText = context.getResources().getString(textId);
			this.neutralButtonListener = listener;
			return this;
		}

		public Builder setNeutralButton(String text, OnClickListener listener) {
			this.neutralButtonText = text;
			this.neutralButtonListener = listener;
			return this;
		}

		public Builder setDismissListener(DismissListener dismissListener) {
			this.dismissListener = dismissListener;
			return this;
		}

		public FEMaterialDialog build() {
			return new FEMaterialDialog(this);
		}
	}

	public interface OnClickListener {

		void onClick(AlertDialog dialog);
	}

	public interface OnItemClickListener {

		void onItemClickListener(AlertDialog dialog, View view, int position);
	}

	public interface DismissListener {

		void onDismissListener(DialogInterface dialog);
	}

}
