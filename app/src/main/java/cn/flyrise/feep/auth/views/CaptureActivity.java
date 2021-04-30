package cn.flyrise.feep.auth.views;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FEStatusBar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.media.images.ImageSelectionActivity;
import cn.flyrise.feep.utils.ParseCaptureUtils;
import cn.flyrise.feep.utils.RGBLuminanceSource;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.zxing.camera.CameraManager;
import com.zxing.decoding.CaptureActivityHandler;
import com.zxing.decoding.InactivityTimer;
import com.zxing.view.ViewfinderView;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 二维码扫描
 * {"ip":"192.168.10.2","port":"9090","isHttps":false,"isOpenVpn":true,"vpnAddress":"183.6.100.131"
 * ,"vpnPort":"8443","vpnName":"**","vpnPassword":"***"}
 */
public class CaptureActivity extends Activity implements Callback {

	private static final int CODE_IMAGE_SELECTION = 564;
	private final String FEEP_UMENG = "CaptureActivity";
	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;


	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_capture);
		CameraManager.init(this);
		viewfinderView = findViewById(R.id.viewfinder_view);
		setToolBar();
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
	}

	private void setToolBar() {
		findViewById(R.id.toolBarRightTextView).setOnClickListener(v -> {
			Intent intent = new Intent(CaptureActivity.this, ImageSelectionActivity.class);
			intent.putExtra("extra_single_choice", true);
			intent.putExtra("extra_except_path", new String[]{CoreZygote.getPathServices().getUserPath()});
			startActivityForResult(intent, CODE_IMAGE_SELECTION);
		});
		findViewById(R.id.idBack).setOnClickListener(v -> finish());
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			CaptureActivity.this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		super.onResume();
		FEUmengCfg.onActivityResumeUMeng(this, FEEP_UMENG);
		final SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		final SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		}
		else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;
	}

	@Override
	protected void onPause() {
		super.onPause();
		FEUmengCfg.onActivityPauseUMeng(this, FEEP_UMENG);
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (final IOException | RuntimeException ioe) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(CaptureActivity.this, decodeFormats, characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}

	public void handleDecode(Result result) {
		inactivityTimer.onActivity();
		final String resultString = result.getText();
		if ("".equals(resultString)) {
			FEToast.showMessage(getResources().getString(R.string.lbl_text_scan_failed));
		}
		else {
			final Intent resultIntent = new Intent();
			final Bundle bundle = new Bundle();
			bundle.putString("result", resultString);
			resultIntent.putExtras(bundle);
			this.setResult(RESULT_OK, resultIntent);
		}
		this.finish();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CODE_IMAGE_SELECTION && data != null) {
			String imagePath = data.getStringExtra("SelectionData");
			if (!TextUtils.isEmpty(imagePath)) {
				Observable.just(imagePath)
						.map(this::scanningImage)
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(result -> {
							if (result == null) {
								FEToast.showMessage(getResources().getString(R.string.lbl_text_scan_failed));
								return;
							}
							String recode = result.getText();
							Intent d = new Intent();
							d.putExtra(ParseCaptureUtils.CAPTURE_RESULT_DATA, recode);
							this.setResult(RESULT_OK, d);
							finish();
						}, exception -> {
							exception.printStackTrace();
							finish();
						});
			}
		}
	}

	protected Result scanningImage(String path) {
		if (TextUtils.isEmpty(path)) {
			return null;
		}
		// DecodeHintType 和EncodeHintType
		Hashtable<DecodeHintType, String> hints = new Hashtable<>();
		hints.put(DecodeHintType.CHARACTER_SET, "utf-8"); // 设置二维码内容的编码
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true; // 先获取原大小
		Bitmap scanBitmap = BitmapFactory.decodeFile(path, options);
		options.inJustDecodeBounds = false; // 获取新的大小
		int sampleSize = (int) (options.outHeight / (float) 200);

		if (sampleSize <= 0) {
			sampleSize = 1;
		}
		options.inSampleSize = sampleSize;
		scanBitmap = BitmapFactory.decodeFile(path, options);

		RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap);
		BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
		QRCodeReader reader = new QRCodeReader();
		try {
			return reader.decode(bitmap1, hints);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


}