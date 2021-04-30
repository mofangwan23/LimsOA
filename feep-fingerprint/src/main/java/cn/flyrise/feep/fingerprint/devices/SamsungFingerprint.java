package cn.flyrise.feep.fingerprint.devices;

import android.app.Activity;
import cn.flyrise.feep.fingerprint.R;
import cn.flyrise.feep.fingerprint.callback.AuthenticationCallback;
import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;

/**
 * @author ZYP
 * @since 2017-05-03 15:20
 * 针对三星设备的指纹识别
 */
public class SamsungFingerprint extends AbstractFingerprint {

	private SpassFingerprint mSpassFingerprint;
	private Spass mSpass;
	private int mResultCode;

	public SamsungFingerprint(Activity activity, AuthenticationCallback callback) {
		super(activity, callback);
		try {
			mSpass = new Spass();
			mSpass.initialize(mActivity);
			mSpassFingerprint = new SpassFingerprint(activity);
		} catch (Exception exp) {
			mSpass = null;
			mSpassFingerprint = null;
		}
	}

	@Override public void startAuthenticate() {
		try {
			mSpassFingerprint.startIdentify(new SpassFingerprint.IdentifyListener() {

				@Override public void onFinished(int i) {
					mResultCode = i;
				}

				@Override public void onReady() { }

				@Override public void onStarted() { }

				@Override public void onCompleted() {
					switch (mResultCode) {
						case SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS:
						case SpassFingerprint.STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS:
							if (mAuthenticationCallback != null) {
								mAuthenticationCallback.onAuthenticationSucceeded();
							}
							break;
						case SpassFingerprint.STATUS_TIMEOUT_FAILED:
						case SpassFingerprint.STATUS_BUTTON_PRESSED:
						case SpassFingerprint.STATUS_QUALITY_FAILED:
						case SpassFingerprint.STATUS_USER_CANCELLED_BY_TOUCH_OUTSIDE:
						case SpassFingerprint.STATUS_AUTHENTIFICATION_FAILED:
							if (mAuthenticationCallback != null) {
								mAuthenticationCallback.onAuthenticationFailed();
							}
							break;

						default:
							if (mAuthenticationCallback != null) {
								mAuthenticationCallback.onAuthenticationError(7, "尝试次数过多，请稍后重试。");
							}
							break;
					}
				}
			});
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
			if (mSpassFingerprint != null) {
				mSpassFingerprint.cancelIdentify();
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	@Override public boolean isHardwareDetected() {
		return mSpass != null && mSpass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT);
	}

	@Override public boolean isEnrolledFingerprints() {
		return mSpassFingerprint != null && mSpassFingerprint.hasRegisteredFinger();
	}

	@Override public void onDestory() {
		if (mSpassFingerprint != null) {
			mSpassFingerprint = null;
		}
	}
}
