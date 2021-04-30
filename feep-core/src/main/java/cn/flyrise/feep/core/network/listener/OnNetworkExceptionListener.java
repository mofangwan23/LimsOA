package cn.flyrise.feep.core.network.listener;

/**
 * @author ZYP
 * @since 2017-02-27 10:46
 */
public interface OnNetworkExceptionListener {

    void onNetworkException(boolean reLogin,boolean isLoadLogout, String errorMessage);

}
