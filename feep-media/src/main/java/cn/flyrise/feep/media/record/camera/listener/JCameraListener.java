package cn.flyrise.feep.media.record.camera.listener;

import android.graphics.Bitmap;

public interface JCameraListener {

    void captureSuccess(Bitmap bitmap);

    void recordSuccess(String url, Bitmap firstFrame);

}
