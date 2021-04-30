package cn.flyrise.feep.core.network.entry;

import java.io.IOException;

import cn.flyrise.feep.core.network.listener.OnProgressUpdateListener;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * @author ZYP
 * @since 2016-09-05 10:28
 * 对 response body 的封装，主要用于下载过程中的进度监听。
 */
public class DownloadResponseBody extends ResponseBody {

    private BufferedSource mBufferedSource;
    private final ResponseBody mResponseBody;
    private final OnProgressUpdateListener mOnProgressUpdateListener;

    public DownloadResponseBody(ResponseBody responseBody, OnProgressUpdateListener onProgressUpdateListener) {
        this.mResponseBody = responseBody;
        this.mOnProgressUpdateListener = onProgressUpdateListener;
    }

    @Override public MediaType contentType() {
        return mResponseBody.contentType();
    }

    @Override public long contentLength() {
        return mResponseBody.contentLength();
    }

    @Override public BufferedSource source() {
        if (mBufferedSource == null) {
            mBufferedSource = Okio.buffer(source(mResponseBody.source()));
        }
        return mBufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            long totalBytesRead = 0L;

            @Override public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                mOnProgressUpdateListener.onProgress(totalBytesRead, mResponseBody.contentLength(), bytesRead == -1);
                return bytesRead;
            }
        };
    }
}
