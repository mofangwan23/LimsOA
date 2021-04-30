package cn.flyrise.feep.fingerprint;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.fingerprint.BiometricManager.OnFingerprintCheckedListener;
import cn.flyrise.feep.fingerprint.callback.BaseAuthenticationCallback;

/**
 * Create by cm132 on 2019/8/8 17:17.
 * Describe:指纹校验界面
 */
public class FingerprintCheckedDialog extends DialogFragment {

	private static final int RE_TIME = 3 * 1000;//验证失败等待时，没3秒重新获取验证
	private static final int RE_TEXT = 2 * 1000;//防止文本闪耀

	private FingerprintIdentifier mFingerprintIdentifier;

	private OnFingerprintCheckedListener listener;
	private Activity activity;
	private TextView mTvHint;
	private TextView mTvPassword;
	private TextView mTvCancel;

	private Handler mHandler = new Handler();

	public FingerprintCheckedDialog setContext(Activity activity) {
		this.activity = activity;
		return this;
	}

	public FingerprintCheckedDialog setListener(OnFingerprintCheckedListener listener) {
		this.listener = listener;
		return this;
	}

	@Override
	public void onStart() {
		super.onStart();

		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		float width = dm.widthPixels * 0.75f;

		Window window = getDialog().getWindow();
		WindowManager.LayoutParams windowParams = window.getAttributes();
		windowParams.dimAmount = 0.7f;
		window.setLayout((int) width, LayoutParams.WRAP_CONTENT);

		window.setAttributes(windowParams);
	}

	@Nullable @Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		getDialog().setOnKeyListener((DialogInterface dialog, int keyCode, KeyEvent event) -> true);
		getDialog().setCanceledOnTouchOutside(false);
		getDialog().setCancelable(false);
		View view = inflater.inflate(R.layout.fingerprint_dialog_layout, container, false);
		init(view);
		return view;
	}

	private void init(View view) {
		mTvHint = view.findViewById(R.id.tv_hint);
		mTvPassword = view.findViewById(R.id.password_check);
		mTvCancel = view.findViewById(R.id.cancel);
		mFingerprintIdentifier = new FingerprintIdentifier(activity, new BaseAuthenticationCallback() {
			@Override
			public void onAuthenticationHelp(int helpCode, String helpString) {
				if (helpCode == 1011 && TextUtils.isEmpty(helpString)) {//华为P30屏幕指纹点取消按钮会回调
					dismiss();
				}
				else if (helpCode == 1021 || TextUtils.isEmpty(helpString)) {//小米手机会导致字符为空
					FELog.i("-->>>" + helpCode);
				}
				else {
					mTvHint.setVisibility(View.INVISIBLE);
				}
			}

			@Override
			public void onAuthenticationError(int errorCode, String errorString) {
				mTvPassword.setVisibility(View.GONE);
				view.findViewById(R.id.line).setVisibility(View.GONE);
				if (errorCode == 7) {
					mTvHint.setTextColor(Color.parseColor("#ffff6262"));
					mTvHint.setVisibility(View.VISIBLE);
					mTvHint.setText(getString(R.string.fp_txt_fingerprint_error));
					mTvPassword.setVisibility(View.VISIBLE);
					mTvCancel.setTextColor(Color.parseColor("#66000000"));
					view.findViewById(R.id.line).setVisibility(View.VISIBLE);
					removeUpdateText();
					mHandler.postDelayed(reFingerprintVerification, RE_TIME);
				}
			}

			@Override
			public void onAuthenticationFailed() {
				removeUpdateText();
				mTvHint.setTextColor(Color.parseColor("#ffff6262"));
				mTvHint.setVisibility(View.VISIBLE);
				mTvHint.setText(R.string.fp_txt_fingerprint_not_match);
				mTvPassword.setVisibility(View.VISIBLE);
				mTvCancel.setTextColor(Color.parseColor("#66000000"));
				view.findViewById(R.id.line).setVisibility(View.VISIBLE);
			}

			@Override
			public void onAuthenticationSucceeded() {
				if (listener != null) {
					dismiss();
					listener.onAuthenticationSucceeded();
				}
			}
		});
		mTvPassword.setOnClickListener(v -> {
			if (listener != null) {
				dismiss();
				listener.onPasswordVerification();
			}
		});
		view.findViewById(R.id.cancel).setOnClickListener(v -> dismiss());
		if (listener != null) listener.onFingerprintEnable(mFingerprintIdentifier.isFingerprintEnable());
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mHandler.postDelayed(reFingerprintVerification, 500);
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		stopChecked();
		removeUpdateText();
		removeVerification();
		if (mFingerprintIdentifier != null) {
			mFingerprintIdentifier.destoryAuthenticate();
			mFingerprintIdentifier = null;
		}
		super.onDismiss(dialog);
	}

	private void startChecked() {
		if (mHandler != null) mHandler.postDelayed(updateFingerprintText, RE_TEXT);
		if (mFingerprintIdentifier != null && mFingerprintIdentifier.isFingerprintEnable()) {
			mFingerprintIdentifier.startAuthenticate();
		}
	}

	private void removeVerification() {
		if (mHandler != null) mHandler.removeCallbacks(reFingerprintVerification);
	}

	private void removeUpdateText() {
		if (mHandler != null) mHandler.removeCallbacks(updateFingerprintText);
	}

	private Runnable reFingerprintVerification = this::startChecked;

	private final Runnable updateFingerprintText = () -> {
		if (mTvHint == null) return;
		mTvHint.setVisibility(View.INVISIBLE);
	};

	private void stopChecked() {
		if (mFingerprintIdentifier != null) {
			mFingerprintIdentifier.stopAuthenticate();
		}
	}
}
