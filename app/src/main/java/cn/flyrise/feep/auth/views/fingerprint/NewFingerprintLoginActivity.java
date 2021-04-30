package cn.flyrise.feep.auth.views.fingerprint;

import android.os.Bundle;
import cn.flyrise.feep.R;
import cn.flyrise.feep.auth.AuthContract;
import cn.flyrise.feep.auth.views.BaseThreeLoginActivity;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.fingerprint.BiometricManager;

/**
 * @author ZYP
 * @since 2017-05-04 15:58
 */
public class NewFingerprintLoginActivity extends BaseThreeLoginActivity implements BiometricManager.OnFingerprintCheckedListener {

	private BiometricManager mBiometricManager;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fingerprint_new_login);
		mBiometricManager = new BiometricManager(this, this);
	}

	@Override
	public void bindView() {
		super.bindView();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mBiometricManager.restartBiometric();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mBiometricManager.cancelBiometric();
	}

	@Override
	public void bindListener() {
		super.bindListener();
		findViewById(R.id.fingerprintImg).setOnClickListener(v -> mBiometricManager.startBiometric());
		findViewById(R.id.tvGesturePrompt).setOnClickListener(v -> mBiometricManager.startBiometric());
	}

	private void succeededStartLogin() {//生物识别成功，开始登录
		if (mUserBean != null && mUserBean.isVPN()) {
			initVpnSetting();
		}
		else {
			mAuthPresenter.executeLogin();
		}
	}

	@Override
	public void onAuthenticationSucceeded() {
		succeededStartLogin();
	}

	@Override
	public void onFingerprintEnable(boolean isFingerprintEnable) {
		if (!isFingerprintEnable) {    // 使用的指纹解锁，但在设置里关了，所以这是使用指纹是没用的。
			FEToast.showMessage(getString(R.string.fp_txt_unable_use_pwd_login));
			SpUtil.put(PreferencesUtils.FINGERPRINT_IDENTIFIER, false);
			uiDispatcher(AuthContract.AuthView.URL_CODE_LOGIN_ACTIVITY);
		}
	}

	@Override
	public void onPasswordVerification() {
		startPasswordVerification();
	}
}
