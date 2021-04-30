package cn.flyrise.feep.core.network.listener;

/**
 * @author ZYP
 * @since 2016-09-05 09:02
 * 用于上传/下载的进度监听。
 */
public interface OnProgressUpdateListener {

    void onStart();

    void onProgress(long currentBytes, long contentLength, boolean done);

    void onPause(long downloadSize, long contentLength);

    void onCancel();

    void onFailed(Throwable ex);

    void onSuccess(String jsonBody);

}
