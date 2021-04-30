package cn.flyrise.feep.fingerprint;

import android.app.Activity;

import cn.flyrise.feep.fingerprint.callback.AuthenticationCallback;
import cn.flyrise.feep.fingerprint.devices.AbstractFingerprint;
import cn.flyrise.feep.fingerprint.devices.AndroidFingerprint;
import cn.flyrise.feep.fingerprint.devices.MeizuFingerprint;
import cn.flyrise.feep.fingerprint.devices.SamsungFingerprint;

/**
 * @author ZYP
 * @since 2017-05-03 14:58
 * 处理指纹识别器， 负责检测设备是否支持指纹、是否已经录入指纹以及指纹的识别。
 */
public class FingerprintIdentifier {

    private AbstractFingerprint mFingerprint;
    private Activity mActivity;
    private AuthenticationCallback mCallback;

    /**
     * 这个构造函数将不会接收到任何指纹处理的回调
     * @param activity
     */
    public FingerprintIdentifier(Activity activity) {
        this(activity, null);
    }

    public FingerprintIdentifier(Activity activity, AuthenticationCallback callback) {
        this.mActivity = activity;
        this.mCallback = callback;
        this.mFingerprint = buildFingerprint();
    }

    public void setAuthenticationCallback(AuthenticationCallback callback) {
        this.mCallback = callback;
        if (this.mFingerprint != null) {
            this.mFingerprint.setAuthenticationCallback(callback);
        }
    }

    /**
     * 开始进行指纹识别
     */
    public void startAuthenticate() {
        if (!isFingerprintEnable()) {
            return;
        }

        mFingerprint.startAuthenticate();
    }

    /**
     * 停止指纹识别
     */
    public void stopAuthenticate() {
        if (mFingerprint != null) {
            mFingerprint.stopAuthenticate();
        }
        if (mCallback != null) {
            mCallback = null;
        }
    }

    /**
     * 销毁指纹识别
     */
    public void destoryAuthenticate() {
        if (mFingerprint != null) {
            mFingerprint.onDestory();
        }
        if (mCallback != null) {
            mCallback = null;
        }
    }


    /**
     * 设备硬件是否支持指纹识别
     */
    public boolean isHardwareEnable() {
        return mFingerprint != null && mFingerprint.isHardwareDetected();
    }

    /**
     * 用户是否已经录入过指纹
     */
    public boolean isEnrolledFingerprints() {
        return mFingerprint != null && mFingerprint.isEnrolledFingerprints();
    }

    /**
     * 指纹是否可用，两个条件：设备支持、以及录入指纹
     */
    public boolean isFingerprintEnable() {
        return isHardwareEnable() && isEnrolledFingerprints();
    }

    /**
     * 构建指纹识别器，优先使用 Android 系统本身的指纹识别，如果不支持，依次使用三星、魅族的指纹识别，
     * 如果最后返回 null，则表示当前设备不支持指纹识别(有可能只是不支持我们 APP ，例如华为)
     */
    private AbstractFingerprint buildFingerprint() {
        AbstractFingerprint fingerprint = new AndroidFingerprint(mActivity, mCallback);
        if (fingerprint.isHardwareDetected() && fingerprint.isEnrolledFingerprints()) {
            return fingerprint;
        }

        fingerprint = new SamsungFingerprint(mActivity, mCallback);
        if (fingerprint.isHardwareDetected() && fingerprint.isEnrolledFingerprints()) {
            return fingerprint;
        }

        fingerprint = new MeizuFingerprint(mActivity, mCallback);
        if (fingerprint.isHardwareDetected() && fingerprint.isEnrolledFingerprints()) {
            return fingerprint;
        }

        return null;
    }
}