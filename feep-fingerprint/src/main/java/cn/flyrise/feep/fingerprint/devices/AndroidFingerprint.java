package cn.flyrise.feep.fingerprint.devices;

import android.app.Activity;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;
import cn.flyrise.feep.fingerprint.R;
import cn.flyrise.feep.fingerprint.callback.AuthenticationCallback;

/**
 * @author ZYP
 * @since 2017-05-03 15:20
 * Android 系统原生的指纹识别
 */
public class AndroidFingerprint extends AbstractFingerprint {

	private FingerprintManagerCompat mFingerprintManager;
	private CancellationSignal mCancellationSignal;

	public AndroidFingerprint(Activity activity, AuthenticationCallback callback) {
		super(activity, callback);
		try {
			mFingerprintManager = FingerprintManagerCompat.from(activity);
		} catch (Exception exp) {
			mFingerprintManager = null;
		}
	}

	@Override public void startAuthenticate() {
		try {
			mFingerprintManager.authenticate(null, 0, mCancellationSignal = new CancellationSignal(),
					new FingerprintManagerCompat.AuthenticationCallback() {
						@Override public void onAuthenticationError(int errMsgId, CharSequence errString) {
							if (mAuthenticationCallback != null) {
								mAuthenticationCallback.onAuthenticationError(errMsgId, errString != null ? errString.toString() : "");
							}
						}

						@Override public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
							if (mAuthenticationCallback != null) {
								mAuthenticationCallback.onAuthenticationHelp(helpMsgId, helpString != null ? helpString.toString() : "");
							}
						}

						@Override public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
							if (mAuthenticationCallback != null) {
								mAuthenticationCallback.onAuthenticationSucceeded();
							}
						}

						@Override public void onAuthenticationFailed() {
							if (mAuthenticationCallback != null) {
								mAuthenticationCallback.onAuthenticationFailed();
							}
						}
					}, null);
		} catch (Exception exp) {
			stopAuthenticate();
			if (mAuthenticationCallback != null) {
				mAuthenticationCallback
						.onAuthenticationError(MSG_START_AUTHENTICATION_ERROR, mActivity.getString(R.string.unable_user_fingerprint));
			}
		}
	}

	@Override public void stopAuthenticate() {
		try {
			if (mCancellationSignal != null) {
				mCancellationSignal.cancel();
				mCancellationSignal = null;
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	@Override public boolean isHardwareDetected() {
		return mFingerprintManager != null && mFingerprintManager.isHardwareDetected();
	}

	@Override public boolean isEnrolledFingerprints() {
		return mFingerprintManager != null && mFingerprintManager.hasEnrolledFingerprints();
	}

	@Override
	public void onDestory() {
		if (mFingerprintManager != null) {
			mFingerprintManager = null;
		}
	}
}
