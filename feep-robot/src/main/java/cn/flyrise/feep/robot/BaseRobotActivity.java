package cn.flyrise.feep.robot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.X;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.common.utils.NetworkUtil;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import cn.squirtlez.frouter.FRouter;

/**
 * 新建：陈冕;
 * 日期： 2018-7-27-15:14.
 * 动态权限使用kotlin会异常
 */
@SuppressLint("Registered")
public abstract class BaseRobotActivity extends BaseActivity {

	private String operationType;

	@SuppressLint("HandlerLeak")
	protected Handler mHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FePermissions.with(this)
				.permissions(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE})
				.rationaleMessage(getString(R.string.permission_rationale_record))
				.requestCode(PermissionCode.RECORD)
				.request();
		new Thread(() -> { if (!NetworkUtil.ping()) mHandler.post(this::networkError); }).start();
	}

	public void intentSchedule() {
		if (FunctionManager.isNative(X.Func.Schedule)) {
			FePermissions.with(this)
					.requestCode(PermissionCode.CALENDAR)
					.rationaleMessage(getString(R.string.permission_rationale_calendar))
					.permissions(new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR})
					.request();
		}
		else {
			FRouter.build(this, "/x5/browser")
					.withInt("moduleId", Func.Schedule)
					.go();
		}
	}

	public void operationLocationSign(String operationType) {
		this.operationType = operationType;
		FePermissions.with(this)
				.rationaleMessage(getString(R.string.permission_rationale_location))
				.permissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION})
				.requestCode(PermissionCode.LOCATION)
				.request();
	}

	@PermissionGranted(PermissionCode.LOCATION)
	public void onLocationPermissionGranted() {
		if (TextUtils.equals(Robot.operation.searchType, operationType))
			FRouter.build(this, "/location/search").go();
		else
			FRouter.build(this, "/location/main").go();
	}

	@PermissionGranted(PermissionCode.CALENDAR)
	public void onCalendarPermissionGranted() {
		FRouter.build(this, "/schedule/native").go();
	}


	@PermissionGranted(PermissionCode.RECORD)
	public void onRecordPermissionGranted() {
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
	}

	public abstract void networkError();
}
