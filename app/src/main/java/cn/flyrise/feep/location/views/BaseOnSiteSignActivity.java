package cn.flyrise.feep.location.views;

import android.Manifest;
import android.support.annotation.NonNull;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;

/**
 * 新建：陈冕;
 * 日期： 2018-7-31-9:12.
 * 权限要丢到父类java类中，不然获取不到权限状态
 */
public abstract class BaseOnSiteSignActivity extends BaseActivity {

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
	}

	public void permissionLocation() {
		FePermissions.with(this)
				.requestCode(PermissionCode.LOCATION)
				.rationaleMessage(getString(R.string.permission_rationale_location))
				.permissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION})
				.request();
	}

	public void permissionCamera() {
		FePermissions.with(this)
				.permissions(new String[]{(Manifest.permission.CAMERA)})
				.rationaleMessage(getString(R.string.permission_rationale_camera))
				.requestCode(PermissionCode.CAMERA)
				.request();
	}


	@PermissionGranted(PermissionCode.LOCATION)
	public void onLocationPermissionGranted() {
		onLocaPermission();
	}

	@PermissionGranted(PermissionCode.CAMERA)
	public void onCameraPermissionGranted() {
		onCameraPermission();
	}

	public abstract void onLocaPermission();

	public abstract void onCameraPermission();
}
