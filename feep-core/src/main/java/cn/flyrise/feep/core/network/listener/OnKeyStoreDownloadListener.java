package cn.flyrise.feep.core.network.listener;

/**
 * @author ZYP
 * @since 2016-09-06 15:56
 */
public interface OnKeyStoreDownloadListener {

    void onKeyStoreDownloadSuccess(String keyStorePath);

    void onKeyStoreDownloadFailed(String errorMessage);

}
