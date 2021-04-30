package cn.flyrise.feep.fingerprint.callback;

/**
 * @author ZYP
 * @since 2017-05-03 15:47
 * 指纹身份验证回调
 */
public interface AuthenticationCallback {

    /**
     * 指纹验证错误
     */
    void onAuthenticationError(int errorCode, String errorString);

    /**
     * 指纹验证期间遇到了可恢复错误时调用身份验证;
     */
    void onAuthenticationHelp(int helpCode, String helpString);

    /**
     * 指纹验证成功
     */
    void onAuthenticationSucceeded();

    /**
     * 指纹验证失败
     */
    void onAuthenticationFailed();

}
