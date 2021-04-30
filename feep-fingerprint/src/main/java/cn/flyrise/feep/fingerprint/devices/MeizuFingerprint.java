package cn.flyrise.feep.fingerprint.devices;

import android.app.Activity;

import com.fingerprints.service.FingerprintManager;

import cn.flyrise.feep.fingerprint.R;
import cn.flyrise.feep.fingerprint.callback.AuthenticationCallback;

/**
 * @author ZYP
 * @since 2017-05-03 15:21
 * 针对魅族设备的指纹识别
 */
public class MeizuFingerprint extends AbstractFingerprint {

    private FingerprintManager mMeizuFingerprintManager;
    private boolean isHardwareDetected;
    private boolean isEnrolledFingerprints;

    public MeizuFingerprint(Activity activity, AuthenticationCallback callback) {
        super(activity, callback);
        try {
            mMeizuFingerprintManager = FingerprintManager.open();
            if (mMeizuFingerprintManager != null) {
                isHardwareDetected = true;
                int[] fingerprintIds = mMeizuFingerprintManager.getIds();
                isEnrolledFingerprints = fingerprintIds != null && fingerprintIds.length > 0;
            }
        } catch (Exception exp) {
            isHardwareDetected = false;
            isEnrolledFingerprints = false;
            mMeizuFingerprintManager = null;
        }
        releaseMeizuFingerprintManager();
    }

    @Override public void startAuthenticate() {
        try {
            mMeizuFingerprintManager = FingerprintManager.open();
            mMeizuFingerprintManager.startIdentify(new FingerprintManager.IdentifyCallback() {
                @Override public void onIdentified(int i, boolean b) {
                    if (mAuthenticationCallback != null) {
                        mAuthenticationCallback.onAuthenticationSucceeded();
                    }
                }

                @Override public void onNoMatch() {
                    if (mAuthenticationCallback != null) {
                        mAuthenticationCallback.onAuthenticationFailed();
                    }
                }
            }, mMeizuFingerprintManager.getIds());
        } catch (Exception exp) {
            releaseMeizuFingerprintManager();
            if (mAuthenticationCallback != null) {
                mAuthenticationCallback.onAuthenticationError(MSG_START_AUTHENTICATION_ERROR, mActivity.getString(R.string.unable_user_fingerprint));
            }
        }
    }

    @Override public void stopAuthenticate() {
        releaseMeizuFingerprintManager();
    }

    @Override public boolean isHardwareDetected() {
        return this.isHardwareDetected;
    }

    @Override public boolean isEnrolledFingerprints() {
        return this.isEnrolledFingerprints;
    }

    @Override public void onDestory() {
        if(mMeizuFingerprintManager!=null){
            mMeizuFingerprintManager=null;
        }
    }

    private void releaseMeizuFingerprintManager() {
        try {
            if (mMeizuFingerprintManager != null) {
                mMeizuFingerprintManager.release();
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }
}
