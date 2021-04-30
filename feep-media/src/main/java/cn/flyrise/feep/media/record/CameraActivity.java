package cn.flyrise.feep.media.record;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.FileUtil;
import cn.flyrise.feep.media.R;
import cn.flyrise.feep.media.record.camera.JCameraView;
import cn.flyrise.feep.media.record.camera.listener.JCameraListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 拍照录像主界面
 */
public class CameraActivity extends AppCompatActivity {

	private JCameraView jCameraView;
	private String path;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_main);
		jCameraView = findViewById(R.id.jcameraview);
		path = getIntent() != null ? getIntent().getStringExtra("cameravew_path") : CoreZygote.getPathServices().getMediaPath();
		jCameraView.setSaveVideoPath(TextUtils.isEmpty(path) ? CoreZygote.getPathServices().getMediaPath() : path);    //设置视频保存路径
		if (getIntent() != null)
			jCameraView.setBtnState(getIntent().getIntExtra("cameravew_state", JCameraView.BUTTON_STATE_ONLY_RECORDER));
		jCameraView.setJCameraLisenter(new JCameraListener() {
			@Override
			public void captureSuccess(final Bitmap bitmap) {
				if (TextUtils.isEmpty(path) || bitmap == null) return;
				new Thread(() -> {
					startOutputBitmap(bitmap);
					runOnUiThread(() -> {
						Intent intent = new Intent();
						setResult(Activity.RESULT_OK, intent);
						finish();
					});
				}).start();
			}

			@Override
			public void recordSuccess(String url, Bitmap firstFrame) {
				Intent intent = new Intent();
				intent.putExtra("record_video", url);
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		});
		jCameraView.setLeftClickListener(this::finish);
	}

	private void startOutputBitmap(Bitmap bitmap) {
		try {
			File file = new File(path);
			if(file.exists())file.delete();
			FileUtil.newFile(file);
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			bitmap.compress(CompressFormat.JPEG, 100, fileOutputStream);
			fileOutputStream.flush();
			fileOutputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onStart() {
		super.onStart();
		//全屏显示
		if (Build.VERSION.SDK_INT >= 19) {
			View decorView = getWindow().getDecorView();
			decorView.setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_FULLSCREEN
							| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}
		else {
			View decorView = getWindow().getDecorView();
			int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
			decorView.setSystemUiVisibility(option);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		jCameraView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		jCameraView.onPause();
	}
}
