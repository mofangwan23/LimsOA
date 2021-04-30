package cn.flyrise.feep.main;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.R;
import cn.flyrise.feep.cordova.CordovaContract;
import cn.flyrise.feep.cordova.presenter.CordovaPresenter;
import cn.flyrise.feep.cordova.view.CordovaFragment;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEStatusBar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.network.TokenInject;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import cn.flyrise.feep.core.services.ILoginUserServices;
import cn.flyrise.feep.location.util.GpsHelper;
import cn.flyrise.feep.location.util.GpsStateUtils;
import cn.flyrise.feep.media.record.camera.CameraManager;
import cn.squirtlez.frouter.FRouter;
import com.amap.api.location.AMapLocationClient;

/**
 * 主界面同事圈
 */
public class AssociatesFragment extends CordovaFragment implements GpsStateUtils.GpsStateListener {

	private AMapLocationClient mLocationClient;

	private final String FEEP_UMENG = "AssociatesFragment";
	private long exitTimes = 0;
	private String mBlogURL;

//	private PhotoUtil mPhotoUtil;

	private CameraManager mCamera;

	private GpsStateUtils mGpsStateUtils;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		ILoginUserServices userServices = CoreZygote.getLoginUserServices();
		mWebView.getSettings().setDefaultTextEncodingName("utf-8");
		if (userServices != null) {
			String host = CoreZygote.getLoginUserServices().getServerAddress();
			mBlogURL = host + "/mdp/html/BLOG/listUI.html";
//			TokenInject.injectToken(mWebView, mBlogURL);
		}
		int paddingTop = 0;
		if (VERSION.SDK_INT == VERSION_CODES.KITKAT && !FEStatusBar.canModifyStatusBar(null)) {
			paddingTop = DevicesUtil.getStatusBarHeight(getActivity());
		}
		mFrameLayout.setPadding(0, paddingTop, 0, 0);
		loadBlogHome();
		initPermissions();
		mLocationClient = new AMapLocationClient(getContext());
		mLocationClient.startAssistantLocation(mWebView);

		mCamera = new CameraManager(getActivity());
	}

	private void initPermissions() {
		mGpsStateUtils = new GpsStateUtils(getContext(), this);
		mGpsStateUtils.requsetGpsIsState();
		FePermissions.with(this)
				.permissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION})
				.rationaleMessage(getResources().getString(com.hyphenate.chatui.R.string.permission_rationale_location))
				.requestCode(PermissionCode.LOCATION)
				.request();
	}

	@PermissionGranted(PermissionCode.LOCATION)
	public void onLocationPermissionGranted() {
		if (!mGpsStateUtils.gpsIsOpen()) mGpsStateUtils.openGPSSetting(getString(R.string.lbl_text_open_setting_gps));
		else if (GpsHelper.inspectMockLocation(getContext())) return;
		loadBlogHome();
	}

	@Override
	public void onResume() {
		super.onResume();
		FEUmengCfg.onFragmentResumeUMeng(FEEP_UMENG);
		CordovaPresenter.isResponseAble = true;
	}

	@Override
	public void onPause() {
		super.onPause();
		FEUmengCfg.onFragmentPauseUMeng(FEEP_UMENG);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (mCamera.isExistPhoto()
				&& requestCode == CordovaContract.CordovaPresenters.PHOTO_RESULT
				&& resultCode == Activity.RESULT_OK) {
			//拍照获取一下地址
			if (intent == null) intent = new Intent();
			String imagePath = mCamera.getAbsolutePath();
			intent.putExtra("photo_path", imagePath);
		}
		super.onActivityResult(requestCode, resultCode, intent);
	}

	@Override
	public void openRecord() {
		super.openRecord();
		FePermissions.with(AssociatesFragment.this)
				.permissions(new String[]{Manifest.permission.RECORD_AUDIO})
				.rationaleMessage(getResources().getString(R.string.permission_rationale_record))
				.requestCode(PermissionCode.RECORD)
				.request();
	}

	@Override
	public void openPhoto() {
		super.openPhoto();
		FePermissions.with(this)
				.permissions(new String[]{Manifest.permission.CAMERA})
				.rationaleMessage(getResources().getString(cn.flyrise.feep.media.R.string.permission_rationale_camera))
				.requestCode(PermissionCode.CAMERA)
				.request();
	}

	@PermissionGranted(PermissionCode.RECORD)
	public void onRecordPermissionGranted() {
		FRouter.build(getActivity(), "/media/recorder")
				.requestCode(CordovaContract.CordovaPresenters.RECORD_RESULT)
				.go();
	}

	@PermissionGranted(PermissionCode.CAMERA)
	public void onCameraPermissionGrated() {    // 打开相机
		mCamera.start(CordovaContract.CordovaPresenters.PHOTO_RESULT);
	}


	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mLocationClient != null) mLocationClient.stopAssistantLocation();
		CordovaPresenter.isResponseAble = false;
	}

	@Override
	public void setListener() {
		mWebView.setOnKeyListener((v, keyCode, event) -> {
			if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
				mWebView.goBack();
				exitTimes = System.currentTimeMillis();
				return true;
			}
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					if ((System.currentTimeMillis() - exitTimes) > 2000) {
						FEToast.showMessage(getResources().getString(R.string.list_exit));
						exitTimes = System.currentTimeMillis();
						return true;
					}
					else {
						CoreZygote.getApplicationServices().exitApplication();
						exitTimes = 0;
					}
				}
			}
			return true;
		});
	}


	public void loadBlogHome() {
		if (mWebView != null) {
			TokenInject.injectToken(mWebView, mBlogURL);
		}
	}

	public void requestFocus() {
		if (mWebView != null) {
			mWebView.requestFocus();
		}
	}


	@Override public void onGpsState(boolean isGpsState) {
		if (isGpsState) loadBlogHome();
	}

	@Override public void cancleDialog() {

	}

	@Override public void onDismiss() {

	}
}
