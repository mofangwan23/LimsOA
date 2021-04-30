package cn.flyrise.feep.fingerprint.devices;

import android.app.Activity;

import cn.flyrise.feep.fingerprint.callback.AuthenticationCallback;

/**
 * @author ZYP
 * @since 2017-05-03 15:19
 */
public abstract class AbstractFingerprint {

    /**
     * 开启指纹识别时，程序出异常了.
     */
    public static final int MSG_START_AUTHENTICATION_ERROR = 10001;

    protected AuthenticationCallback mAuthenticationCallback;
    protected Activity mActivity;

    public AbstractFingerprint(Activity activity, AuthenticationCallback callback) {
        this.mActivity = activity;
        this.mAuthenticationCallback = callback;
    }

    public void setAuthenticationCallback(AuthenticationCallback callback) {
        this.mAuthenticationCallback = callback;
    }

    /**
     * 开始指纹识别
     */
    public abstract void startAuthenticate();

    /**
     * 停止指纹识别
     */
    public abstract void stopAuthenticate();

    /**
     * 设备硬件是否支持指纹识别
     */
    public abstract boolean isHardwareDetected();

    /**
     * 用户是否已经录入指纹
     */
    public abstract boolean isEnrolledFingerprints();

    /**
     * 用户退出清空指纹识别
     * */
    public abstract void onDestory();

}
