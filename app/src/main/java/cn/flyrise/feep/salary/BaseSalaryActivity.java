package cn.flyrise.feep.salary;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import cn.flyrise.android.shared.bean.UserBean;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.common.FEStatusBar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.NetworkUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.dbmodul.utils.UserInfoTableUtils;
import cn.flyrise.feep.utils.SafetyVerifyManager;
import java.text.DecimalFormat;

/**
 * @author ZYP
 * @since 2017-02-20 15:20
 */
public class BaseSalaryActivity extends BaseActivity implements SafetyVerifyManager.Callback {

	protected SafetyVerifyManager mSafetyVerifyManager;
	protected FELoadingDialog mLoadingDialog;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		}
	}

	@Override protected int statusBarColor() {
		return Color.TRANSPARENT;
	}

	@Override protected boolean optionStatusBar() {
		return FEStatusBar.setLightStatusBar(this);
	}

	@Override public void bindView() {
		mSafetyVerifyManager = new SafetyVerifyManager(this);
	}

	@Override public void onPreVerify() {
		showLoading();
	}

	@Override public void onVerifySuccess() {
		hideLoading();
	}

	@Override public void onVerifyFailed(boolean isInputPwd) {
		hideLoading();
		if (!NetworkUtil.isNetworkAvailable(this)) {    // 这里有毒
			FEToast.showMessage(CommonUtil.getString(R.string.core_http_timeout));
			return;
		}
		FEToast.showMessage(isInputPwd
				? getResources().getString(R.string.salary_pwd_verify_failed)
				: getResources().getString(R.string.core_data_get_error));
	}


	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == K.salary.gesture_verify_request_code) {
			if (resultCode == 404) {
				finish();
				return;
			}
			UserBean userBean = UserInfoTableUtils.find();
			mSafetyVerifyManager.verifyPassword(CommonUtil.toBase64Password(userBean.getPassword()), false, this);
		}
	}

	protected void showLoading() {
		this.hideLoading();
		mLoadingDialog = new FELoadingDialog.Builder(this)
				.setCancelable(true)
				.setLoadingLabel(getResources().getString(R.string.core_loading_wait))
				.setOnDismissListener(this::finish)
				.create();
		mLoadingDialog.show();
	}

	protected void hideLoading() {
		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}
	}

	@Override protected void onDestroy() {
		super.onDestroy();
		this.hideLoading();
	}

	public static String formatMonery(String value) {
		float monery = CommonUtil.parseFloat(value);
		if (monery == 0.0F) {
			return "0";
		}
		DecimalFormat mFormat = new DecimalFormat("###,###.00");
		return mFormat.format(monery);
	}
}
