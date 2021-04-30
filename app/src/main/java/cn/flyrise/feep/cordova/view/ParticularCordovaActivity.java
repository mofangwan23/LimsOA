package cn.flyrise.feep.cordova.view;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.R;
import cn.flyrise.feep.auth.views.CaptureActivity;
import cn.flyrise.feep.commonality.view.CordovaButton;
import cn.flyrise.feep.cordova.CordovaContract;
import cn.flyrise.feep.cordova.utils.CordovaShowUtils;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.common.CordovaShowInfo;
import cn.flyrise.feep.core.common.X.JSControlType;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import cn.flyrise.feep.media.record.camera.CameraManager;
import cn.squirtlez.frouter.FRouter;
import org.apache.cordova.Config;

public class ParticularCordovaActivity extends BaseActivity {

	private CordovaFragment mCordovaFragment;
	private CordovaButton button;
	private CordovaShowInfo mShowInfo;
	//	private PhotoUtil mPhotoUtil;
	private boolean isZXing = false;

	private CameraManager mCamera;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cordova_layout);
		IntentFilter filter = new IntentFilter();
		filter.addAction(CordovaShowUtils.FINISH_CORDOVA);
		registerReceiver(receiver, filter);

		findViews();
		bindDatas();
		setListeners();
		mCamera = new CameraManager(this);
	}

	private void findViews() {
		button = (CordovaButton) this.findViewById(R.id.particular_cordova_but);
		button.setVisibility(View.VISIBLE);
	}

	private void bindDatas() {
		Intent intent = getIntent();
		if (intent != null) {
			String shoInfo = intent.getStringExtra(CordovaShowUtils.CORDOVA_SHOW_INFO);
			mShowInfo = GsonUtil.getInstance().fromJson(shoInfo, CordovaShowInfo.class);
		}
		CordovaShowUtils mShowUtils = CordovaShowUtils.getInstance();
		FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
		ft.add(R.id.particular_cordova_layout, mCordovaFragment = new CordovaFragment()).commit();
		Config.init(this);
		String url = mShowUtils.getCordovaWebViewUrl(mShowInfo);
		if (!TextUtils.isEmpty(url)) {
			mCordovaFragment.loadUrl(url);
		}
	}

	private void setListeners() {
		super.bindListener();
		if (button != null) {
			button.setLeftBtnClickListener(onClickListener);
			button.setRightBtnClickListener(onClickListener);
			button.setFinishBtnClickListener(onClickListener);
			button.setReloadBtnClickListener(onClickListener);
		}
		mCordovaFragment.setOnClickOpenListener(controlType -> {
			if (controlType == JSControlType.Contacts) {//通讯录
				FePermissions.with(ParticularCordovaActivity.this)
						.rationaleMessage(getResources().getString(R.string.permission_rationale_contact))
						.permissions(new String[]{Manifest.permission.READ_CONTACTS})
						.requestCode(PermissionCode.CONTACTS)
						.request();
			}
			else if (controlType == JSControlType.Break) {
				finish();
			}
			else if (controlType == JSControlType.Record) {//录音
				FePermissions.with(this)
						.permissions(new String[]{Manifest.permission.RECORD_AUDIO})
						.rationaleMessage(getResources().getString(R.string.permission_rationale_record))
						.requestCode(PermissionCode.RECORD)
						.request();
			}
			else if (controlType == JSControlType.TakePhoto) {//拍照
				isZXing = false;
				FePermissions.with(this)
						.permissions(new String[]{Manifest.permission.CAMERA})
						.rationaleMessage(getResources().getString(cn.flyrise.feep.media.R.string.permission_rationale_camera))
						.requestCode(PermissionCode.CAMERA)
						.request();
			}
			else if (controlType == JSControlType.ZXing) {//二维码扫描
				isZXing = true;
				FePermissions.with(this)
						.permissions(new String[]{Manifest.permission.CAMERA})
						.rationaleMessage(getResources().getString(R.string.permission_rationale_camera))
						.requestCode(PermissionCode.CAMERA)
						.request();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mCamera.isExistPhoto()
				&& requestCode == CordovaContract.CordovaPresenters.PHOTO_RESULT
				&& resultCode == RESULT_OK) {//拍照获取一下地址
			if (data == null) data = new Intent();
			String imagePath = mCamera.getAbsolutePath();
			data.putExtra("photo_path", imagePath);
		}
		if (mCordovaFragment != null) {
			mCordovaFragment.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		FEHttpClient.cancel(this);
		unregisterReceiver(receiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mShowInfo != null) {
			FEUmengCfg.cordovaResume(this, mShowInfo.type);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mShowInfo != null) {
			FEUmengCfg.cordovaPause(this, mShowInfo.type);
		}
	}

	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (TextUtils.equals(intent.getAction(), CordovaShowUtils.FINISH_CORDOVA)) {
				ParticularCordovaActivity.this.finish();
			}
		}
	};

	private final View.OnClickListener onClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
				case R.id.button_left:
					mCordovaFragment.goBack();
					break;
				case R.id.button_right:
					mCordovaFragment.goForward();
					break;
				case R.id.button_finish:
					ParticularCordovaActivity.this.sendBroadcast(new Intent(CordovaShowUtils.FINISH_CORDOVA));
					break;
				case R.id.button_reload:
					mCordovaFragment.goReload();
					break;
				default:
					break;
			}
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			mCordovaFragment.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@PermissionGranted(PermissionCode.CONTACTS)
	public void onContactPermissionGranted() {
		Uri uri = Uri.parse("content://contacts/people");
		Intent intent = new Intent(Intent.ACTION_PICK, uri);
		startActivityForResult(intent, 0);
	}

	@PermissionGranted(PermissionCode.RECORD)
	public void onRecordPermissionGranted() {
		FRouter.build(this, "/media/recorder")
				.requestCode(CordovaContract.CordovaPresenters.RECORD_RESULT)
				.go();
	}

	@PermissionGranted(PermissionCode.CAMERA)
	public void onCameraPermissionGrated() {    // 打开相机
		if (isZXing) {//二维码扫描
			if (DevicesUtil.isCameraCanUsed(this)) {
				startActivityForResult(new Intent(this, CaptureActivity.class), CordovaContract.CordovaPresenters.SCANNING_QR_CODE);
			}
		}
		else {//拍照
			mCamera.start(CordovaContract.CordovaPresenters.PHOTO_RESULT);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
	}
}
