package cn.flyrise.feep.core.network.listener;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

import cn.flyrise.feep.core.network.entry.Progress;

/**
 * @author ZYP
 * @since 2016-09-05 23:04
 * 上传/下载的进度监听
 */
public abstract class OnProgressUpdateListenerImpl implements OnProgressUpdateListener {

    private UIProgressHandler mHandler = new UIProgressHandler(this);

    public void onPreExecute() { }                                              // 开始上传/下载时回调

    public void onFailExecute(Throwable ex) { }                                 // 上传/下载失败时回调

    public void onPostExecute(String jsonBody) { }                            // 上传/下载完成时回调

    public void onPauseExecute(long downloadSize, long contentLength) { }       // 暂停任务时回调，仅下载

    public void onCancelExecute() { }                                           // 取消任务时回掉，仅上传

    public abstract void onProgressUpdate(long currentBytes, long contentLength, boolean done);

    @Override public final void onStart() {
        Message message = mHandler.obtainMessage();
        message.what = UIProgressHandler.CODE_START;
        mHandler.sendMessage(message);
    }

    @Override public final void onFailed(Throwable ex) {
        Message message = mHandler.obtainMessage();
        message.what = UIProgressHandler.CODE_FAILED;
        message.obj = ex;
        mHandler.sendMessage(message);
    }

    @Override public final void onPause(long downloadSize, long contentLength) {
        Message message = mHandler.obtainMessage();
        message.what = UIProgressHandler.CODE_PAUSE;
        message.obj = new long[] { downloadSize, contentLength };
        mHandler.sendMessage(message);
    }

    @Override public final void onProgress(long currentBytes, long contentLength, boolean done) {
        Message progressMsg = mHandler.obtainMessage();
        progressMsg.what = UIProgressHandler.CODE_PROGRESS;
        progressMsg.obj = new Progress(currentBytes, contentLength, done);
        mHandler.sendMessage(progressMsg);
    }

    @Override public final void onCancel() {
        Message cancelMsg = mHandler.obtainMessage();
        cancelMsg.what = UIProgressHandler.CODE_CANCEL;
        mHandler.sendMessage(cancelMsg);
    }

    @Override public final void onSuccess(String jsonBody) {
        Message doneMsg = mHandler.obtainMessage();
        doneMsg.what = UIProgressHandler.CODE_DONE;
        doneMsg.obj = jsonBody;
        mHandler.sendMessage(doneMsg);
    }

    private class UIProgressHandler extends Handler {

        public static final int CODE_START = 0x01;
        public static final int CODE_PROGRESS = 0x02;
        public static final int CODE_DONE = 0x03;
        public static final int CODE_FAILED = 0x04;
        public static final int CODE_PAUSE = 0x05;
        public static final int CODE_CANCEL = 0x06;

        private final WeakReference<OnProgressUpdateListenerImpl> mUIProgressListenerWeakReference;

        public UIProgressHandler(OnProgressUpdateListenerImpl progressListener) {
            super(Looper.getMainLooper());
            mUIProgressListenerWeakReference = new WeakReference<>(progressListener);
        }

        @Override public void handleMessage(Message msg) {
            OnProgressUpdateListenerImpl listener = mUIProgressListenerWeakReference.get();
            switch (msg.what) {
                case CODE_START:
                    if (listener != null) {
                        listener.onPreExecute();
                    }
                    break;
                case CODE_PROGRESS:
                    if (listener != null) {
                        Progress progress = (Progress) msg.obj;
                        listener.onProgressUpdate(progress.currentBytes, progress.contentLength, progress.done);
                    }
                    break;
                case CODE_DONE:
                    if (listener != null) {
                        String jsonBody = (String) msg.obj;
                        listener.onPostExecute(jsonBody);
                    }
                    break;
                case CODE_FAILED:
                    if (listener != null) {
                        Throwable ex = (Throwable) msg.obj;
                        listener.onFailExecute(ex);
                    }
                    break;
                case CODE_PAUSE:
                    if (listener != null) {
                        long[] sizes = (long[]) msg.obj;
                        listener.onPauseExecute(sizes[0], sizes[1]);
                    }
                    break;
                case CODE_CANCEL:
                    if (listener != null) {
                        listener.onCancelExecute();
                    }
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }
}
