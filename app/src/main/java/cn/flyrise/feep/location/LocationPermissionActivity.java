package cn.flyrise.feep.location;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import cn.flyrise.feep.location.service.LocationService;

/**
 * Created by klc on 2017/4/7.
 * 解决在服务中无法请求权限的问题。
 */

public class LocationPermissionActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_permission);
		FePermissions.with(this)
				.permissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION})
				.rationaleMessage(getResources().getString(R.string.permission_rationale_location))
				.requestCode(PermissionCode.LOCATION)
				.request();
	}

	@PermissionGranted(PermissionCode.LOCATION)
	public void onLocationermissionGrated() {
		LocationService.startLocationService(this, LocationService.SETTASKCODE);
		finish();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults,
				(requestCode1, permissions1, grantResults1, deniedMessage) -> {
					LocationService.stopLocationService(LocationPermissionActivity.this);
					if (!this.isFinishing()) finish();
				});
	}
}
