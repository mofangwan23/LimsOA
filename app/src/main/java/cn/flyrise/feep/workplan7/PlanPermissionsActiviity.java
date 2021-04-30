package cn.flyrise.feep.workplan7;

import android.support.annotation.NonNull;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;

/**
 * Create by cm132 on 2018/12/4.
 * Describe:
 */
public abstract class PlanPermissionsActiviity extends BaseActivity{

	protected void permissionRecord(){
		FePermissions.with(this)
				.permissions(new String[]{android.Manifest.permission.RECORD_AUDIO})
				.rationaleMessage(getString(R.string.permission_rationale_record))
				.requestCode(PermissionCode.RECORD)
				.request();
	}

	@PermissionGranted(PermissionCode.RECORD)
	public void onRecordPermissionGranted() {
		onRecordPermissionsResult();
	}

	@Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
	}

	public abstract void onRecordPermissionsResult();
}
