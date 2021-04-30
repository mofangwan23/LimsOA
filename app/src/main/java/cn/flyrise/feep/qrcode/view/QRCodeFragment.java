package cn.flyrise.feep.qrcode.view;

import android.Manifest;
import android.Manifest.permission;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.protocol.entity.MeetingSignInResponse.MeetingSign;
import cn.flyrise.feep.R;
import cn.flyrise.feep.auth.views.CaptureActivity;
import cn.flyrise.feep.auth.views.LoginZxingActivity;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import cn.flyrise.feep.qrcode.QRResultContract;
import cn.flyrise.feep.qrcode.QRResultPresenter;

/**
 * Created by klc on 2018/3/26.
 */

public class QRCodeFragment extends Fragment implements QRResultContract.IView {


	private QRResultContract.IPresenter mQRPresenter;

	private final int SCANNING_QR_CODE = 10001;
	private final int SETTING_CODE = 10002;
	private MeetingSignInDialog signInDialog;

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mQRPresenter = new QRResultPresenter(getActivity(), this);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SCANNING_QR_CODE && resultCode == Activity.RESULT_OK) {
			String result = data.getExtras().getString("result");
			mQRPresenter.handleCode(result);
		}
	}

	@Override
	public void startScanActivity() {
		FePermissions.with(this)
				.permissions(new String[]{Manifest.permission.CAMERA})
				.rationaleMessage(getResources().getString(R.string.permission_rationale_camera))
				.requestCode(PermissionCode.CAMERA)
				.request();
	}

	@Override
	public void requestLocationPermission() {
		FePermissions.with(this)
				.permissions(new String[]{permission.ACCESS_FINE_LOCATION})
				.rationaleMessage(getResources().getString(R.string.permission_rationale_location))
				.requestCode(PermissionCode.LOCATION)
				.request();
	}

	@Override
	public void showLoading() {
		LoadingHint.show(getActivity());
	}

	@Override
	public void hideLoading() {
		LoadingHint.hide();
	}

	@Override
	public void showOpenGpsDialog() {
		new FEMaterialDialog.Builder(getActivity())
				.setMessage(getResources().getString(R.string.lockpattern_setting_button_text))
				.setNegativeButton(null, null)
				.setPositiveButton(
						getResources().getString(R.string.lockpattern_setting_button_text),
						dialog -> startSettingActivity())
				.build().show();
	}

	@Override
	public void startSettingActivity() {
		Intent intent = new Intent(
				Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivityForResult(intent, SETTING_CODE); // 设置完成后返回到原来的界面
	}

	@Override
	public void showMeetingSignDialog(MeetingSign meetingSign) {
		signInDialog = new MeetingSignInDialog.Builder()
				.setTitle(meetingSign.title)
				.setStartTime(meetingSign.startTime)
				.setEndTime(meetingSign.endTime)
				.setAddress(meetingSign.meetingPlace)
				.setMeetingMaster(meetingSign.meetingMaster)
				.setSignTime(meetingSign.signTime)
				.setSignType(meetingSign.signType)
				.create();
		signInDialog.show(getFragmentManager(), "meeting");
	}

	@Override public void startLoginZXAciivity(String url) {
		LoginZxingActivity.Companion.startActivity(getActivity(), url);
	}


	@PermissionGranted(PermissionCode.CAMERA)
	public void onCameraPermissionGranted() {
		startActivityForResult(new Intent(getActivity(), CaptureActivity.class), SCANNING_QR_CODE);
	}

	@PermissionGranted(PermissionCode.LOCATION)
	public void onLocationPermissionGranted() {
		mQRPresenter.checkGpsOpen();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
			@NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (signInDialog != null) {
			signInDialog.dismiss();
		}
	}
}
