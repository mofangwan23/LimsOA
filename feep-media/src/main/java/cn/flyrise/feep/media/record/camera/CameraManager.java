package cn.flyrise.feep.media.record.camera;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.media.record.CameraActivity;
import java.io.File;

/**
 * Create by cm132 on 2019/7/31 17:21.
 * Describe:拍照管理
 */
public class CameraManager {

	public static final int TAKE_PHOTO_RESULT = 200;

	private Activity mActivity;
	private File cameraFile;

	public CameraManager(Activity activity) {
		this.mActivity = activity;
	}

	public void start(int requestCode) {
		cameraFile = new File(CoreZygote.getPathServices().getImageCachePath(), System.currentTimeMillis() + ".png");
		Intent intent = new Intent(mActivity, CameraActivity.class);
		intent.putExtra("cameravew_path", cameraFile.getAbsolutePath());
		intent.putExtra("cameravew_state", JCameraView.BUTTON_STATE_ONLY_CAPTURE);
		mActivity.startActivityForResult(intent, requestCode);
	}

	public File getFile() {
		return cameraFile;
	}

	public Uri getUri() {
		return Uri.fromFile(cameraFile);
	}

	public boolean isExistPhoto() {
		return cameraFile != null;
	}

	public String getAbsolutePath() {
		return cameraFile.getAbsolutePath();
	}

	public void clear() {
		cameraFile = null;
	}
}
