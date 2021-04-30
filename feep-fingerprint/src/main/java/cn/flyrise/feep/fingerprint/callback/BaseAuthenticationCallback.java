package cn.flyrise.feep.fingerprint.callback;

/**
 * @author ZYP
 * @since 2017-05-03 15:51
 */
public class BaseAuthenticationCallback implements AuthenticationCallback {

    @Override public void onAuthenticationError(int errorCode, String errorString) { }

    @Override public void onAuthenticationHelp(int helpCode, String helpString) { }

    @Override public void onAuthenticationSucceeded() { }

    @Override public void onAuthenticationFailed() { }
}
