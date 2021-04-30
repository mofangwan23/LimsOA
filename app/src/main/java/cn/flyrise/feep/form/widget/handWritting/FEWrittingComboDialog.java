package cn.flyrise.feep.form.widget.handWritting;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 新建：陈冕;
 * 日期： 2018-6-8-13:39.
 */

public class FEWrittingComboDialog extends DialogFragment {

	private FEWrittingCombo mWrittingCombo; // 手写控件
	private OnWrittingComboConfirmListener mConfirmListener;
	private OnWrittingComboCancelListener mCancelListener;

	public FEWrittingComboDialog setConfirmListener(OnWrittingComboConfirmListener listener) {
		this.mConfirmListener = listener;
		return this;
	}

	public FEWrittingComboDialog setCancelListener(OnWrittingComboCancelListener listener) {
		this.mCancelListener = listener;
		return this;
	}

	@Nullable @Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		View view = inflater.inflate(R.layout.writting_combo_dialog_layout, container, false);
		bindView(view);
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog != null) {
			DisplayMetrics dm = new DisplayMetrics();
			getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
			dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		}
	}

	@SuppressLint("SimpleDateFormat")
	private void bindView(View view) {
		mWrittingCombo = view.findViewById(R.id.feWritingCombo);
		view.findViewById(R.id.tvConfirm).setOnClickListener(v -> {
			final String datePrefix = new SimpleDateFormat("yyyyMMdd").format(new Date());
			final String GUID = UUID.randomUUID().toString();
			mWrittingCombo.saveBitmapToFile(getWrittingPaht(), datePrefix + GUID + ".png");
			if (mConfirmListener != null) mConfirmListener.onWrittingComboConfirm(datePrefix + GUID + ".png");
			dismiss();
		});

		view.findViewById(R.id.tvCancel).setOnClickListener(v -> {
			if (mCancelListener != null) mCancelListener.onWrittingComboCancel();
			dismiss();
		});
	}

	private String getWrittingPaht() {
		return CoreZygote.getPathServices().getTempFilePath() + "/handwrittenFiles";
	}

	@Override
	public void dismiss() {
		if (mWrittingCombo != null) {
			mWrittingCombo.recycleAllBitmaps();
			mWrittingCombo.destroyDrawingCache();
		}
		super.dismiss();
	}

	public interface OnWrittingComboConfirmListener {

		void onWrittingComboConfirm(String path);//确定
	}

	public interface OnWrittingComboCancelListener {

		void onWrittingComboCancel();//取消
	}
}
