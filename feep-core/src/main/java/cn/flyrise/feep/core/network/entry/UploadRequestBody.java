package cn.flyrise.feep.core.network.entry;

import java.io.IOException;

import cn.flyrise.feep.core.network.listener.OnProgressUpdateListener;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * @author ZYP
 * @since 2016-09-05 10:28
 * 对 request body 的封装，主要用于上传过程中的进度监听。
 */
public class UploadRequestBody extends RequestBody {

    private final RequestBody mRequestBody;
    private final OnProgressUpdateListener mOnProgressUpdateListener;
    private BufferedSink bufferedSink;

    public UploadRequestBody(RequestBody requestBody, OnProgressUpdateListener onProgressUpdateListener) {
        this.mRequestBody = requestBody;
        this.mOnProgressUpdateListener = onProgressUpdateListener;
    }

    @Override public MediaType contentType() {
        return mRequestBody.contentType();
    }

    @Override public long contentLength() throws IOException {
        return mRequestBody.contentLength();
    }

    @Override public void writeTo(BufferedSink sink) throws IOException {
        if (bufferedSink == null) {
            bufferedSink = Okio.buffer(sink(sink));
        }

        mRequestBody.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    private Sink sink(Sink sink) {
        return new ForwardingSink(sink) {
            long bytesWritten = 0L;
            long contentLength = 0L;

            @Override public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (contentLength == 0) {
                    contentLength = contentLength();
                }
                bytesWritten += byteCount;
                if (mOnProgressUpdateListener != null) {
                    mOnProgressUpdateListener.onProgress(bytesWritten, contentLength, bytesWritten == contentLength);
                }
            }
        };
    }
}
