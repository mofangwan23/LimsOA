package cn.flyrise.feep.location.assistant;

import android.Manifest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.NetworkUtil;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import cn.flyrise.feep.location.util.GpsHelper;
import cn.flyrise.feep.location.util.GpsStateUtils;

/**
 * 新建：陈冕;
 * 日期： 2018-8-7-17:47.
 * 急速打开启动页
 */
public abstract class SignInPermissionsActivity extends BaseActivity implements GpsStateUtils.GpsStateListener {

	private GpsStateUtils mGpsStateUtils;
	private int netWork = 1012;
	private Handler mHandle = new Handler(Looper.getMainLooper()) {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == netWork) { networkError(); }
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mGpsStateUtils = new GpsStateUtils(this);
		mGpsStateUtils.requsetGpsIsState();
		FePermissions.with(this)
				.permissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION})
				.rationaleMessage(getResources().getString(com.hyphenate.chatui.R.string.permission_rationale_location))
				.requestCode(PermissionCode.LOCATION)
				.request();
	}

	@PermissionGranted(PermissionCode.LOCATION)
	public void onLocationPermissionGranted() {
		if (!mGpsStateUtils.gpsIsOpen()) {
			mGpsStateUtils.openGPSSetting(getString(R.string.lbl_text_open_setting_gps));
		}
		else if (!GpsHelper.inspectMockLocation(this)) {
			permissionsSuccess();
		}
		else {
			finish();
		}
	}

	public void netWork() {
		new Thread(() -> { if (!NetworkUtil.ping()) mHandle.sendEmptyMessage(netWork); }).start();
	}

	@Override
	public void onGpsState(boolean isGpsState) {
		if (isGpsState) {
			FEToast.showMessage(getString(R.string.location_check_sign_in));
		}
		finish();
	}

	@Override
	public void cancleDialog() {
		finish();
	}

	@Override
	public void onDismiss() {
		finish();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults,
				(requestCode1, permissions1, grantResults1, deniedMessage) -> {
					if (!this.isFinishing()) finish();
				});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mHandle.removeMessages(netWork);
	}

	public abstract void permissionsSuccess();//一堆权限获取成功，可以开始签到了

	public abstract void networkError();//网络废了
}
