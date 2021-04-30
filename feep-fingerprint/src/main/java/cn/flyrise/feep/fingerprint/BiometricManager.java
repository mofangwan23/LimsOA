package cn.flyrise.feep.fingerprint;

import android.app.Activity;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.biometrics.BiometricPrompt.AuthenticationCallback;
import android.hardware.biometrics.BiometricPrompt.AuthenticationResult;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.CancellationSignal;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import cn.flyrise.feep.core.common.FEToast;

/**
 * Create by cm132 on 2019/8/26 10:47.
 * Describe:系统28以后使用生物识别
 */
public class BiometricManager {

	private FingerprintCheckedDialog mDialog;
	private BiometricPrompt mBiometric;
	private CancellationSignal mCancellation;
	private BiometricPrompt.AuthenticationCallback mCallback;
	private Activity mContext;
	private OnFingerprintCheckedListener mListanter;

	public BiometricManager(Activity context, OnFingerprintCheckedListener listener) {
		this.mContext = context;
		this.mListanter = listener;
	}

	public void restartBiometric() {
		if (mDialog == null || mBiometric == null) {
			startBiometric();
		}
	}

	public void startBiometric() {
//		if (VERSION.SDK_INT >= VERSION_CODES.P) {
//			showBiometric();
//		}
//		else {
			showDialog();
//		}
	}

	public void cancelBiometric() {
//		if (VERSION.SDK_INT >= VERSION_CODES.P) {
//			if (mCancellation != null) {
//				mCancellation.cancel();
//				mCallback = null;
//				mCancellation = null;
//				mBiometric = null;
//			}
//		}
//		else {
			if (mDialog != null) {
				mDialog.dismiss();
				mDialog = null;
			}
//		}
	}

	@RequiresApi(api = VERSION_CODES.P)
	private void showBiometric() {
		mBiometric = new BiometricPrompt.Builder(mContext)
				.setTitle("指纹认证")
				.setNegativeButton("取消", mContext.getMainExecutor(), (dialog, which) -> {

				})
				.build();
		mCancellation = new CancellationSignal();
		mCancellation.setOnCancelListener(() -> {

		});
		mCallback = new AuthenticationCallback() {
			@Override
			public void onAuthenticationError(int errorCode, CharSequence errString) {
				super.onAuthenticationError(errorCode, errString);
				if (errorCode == 7) {
					FEToast.showMessage(mContext.getString(R.string.fp_txt_fingerprint_error));
				}
			}

			@Override
			public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
				super.onAuthenticationHelp(helpCode, helpString);
				if (helpCode == 1011 && TextUtils.isEmpty(helpString)) {//华为P30屏幕指纹点取消按钮会回调
					cancelBiometric();
				}
			}

			@Override
			public void onAuthenticationSucceeded(AuthenticationResult result) {
				super.onAuthenticationSucceeded(result);
				if (mListanter != null) mListanter.onAuthenticationSucceeded();
			}

			@Override
			public void onAuthenticationFailed() {
				super.onAuthenticationFailed();
			}
		};
		mBiometric.authenticate(mCancellation, mContext.getMainExecutor(), mCallback);
	}

	private void showDialog() {
		mDialog = new FingerprintCheckedDialog().setContext(mContext).setListener(mListanter);
		if (!mDialog.isAdded()) {
			mDialog.show(((AppCompatActivity) mContext).getSupportFragmentManager(), "fingerprintDialog");
		}
	}

	public interface OnFingerprintCheckedListener {

		void onAuthenticationSucceeded();//指纹验证成功

		void onFingerprintEnable(boolean isFingerprintEnable);//查看指纹是否开启

		void onPasswordVerification();//使用密码登录
	}
}
