package cn.flyrise.feep.auth.views.fingerprint;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import cn.flyrise.feep.FEMainActivity;
import cn.flyrise.feep.R;
import cn.flyrise.feep.auth.unknown.NewLoginActivity;
import cn.flyrise.feep.auth.views.BaseAuthActivity;
import cn.flyrise.feep.auth.views.BaseUnLockActivity;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.services.IApplicationServices;
import cn.flyrise.feep.fingerprint.BiometricManager;
import cn.flyrise.feep.protocol.FeepApplicationServices;

/**
 * @author ZYP
 * @since 2017-05-04 15:58
 */
public class FingerprintNewUnLockActivity extends BaseUnLockActivity implements BiometricManager.OnFingerprintCheckedListener {

	private BiometricManager mBiometricManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fingerprint_new_unlock);
		mBiometricManager = new BiometricManager(this, this);
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
		if (isAllowForgetPwd) {
			mTvForgetPwd.setVisibility(View.VISIBLE);
			mTvForgetPwd.setOnClickListener(view -> startPasswrodVerification());
		}
		else {
			mTvForgetPwd.setVisibility(View.GONE);
		}

		findViewById(R.id.fingerprintImg).setOnClickListener(v -> mBiometricManager.startBiometric());
		findViewById(R.id.gesturepwd_unlock_text).setOnClickListener(v -> mBiometricManager.startBiometric());
	}

	private void startPasswrodVerification() {
		SpUtil.put(PreferencesUtils.FINGERPRINT_IDENTIFIER, false);
		CoreZygote.getApplicationServices().exitApplication();
		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(BaseAuthActivity.EXTRA_FORGET_PASSWORD, true);
		intent.setClass(FingerprintNewUnLockActivity.this, NewLoginActivity.class);
		startActivity(intent);
		sendBroadcast(new Intent(FEMainActivity.CANCELLATION_LOCK_RECEIVER));
		finish();
	}

	@Override public void onAuthenticationSucceeded() {
		if (isLockMainActivity) {
			FeepApplicationServices.sLastTimeUnlockSuccess = System.currentTimeMillis();
			CoreZygote.getApplicationServices().setHomeKeyState(IApplicationServices.HOME_PRESS_AND_UN_LOCKED);
		}
		else {
			setResult(1001);
		}
		finish();
	}

	@Override public void onFingerprintEnable(boolean isFingerprintEnable) {
		if (!isFingerprintEnable) {    // 使用的指纹解锁，但在设置里关了，所以这是使用指纹是没用的。
			FEToast.showMessage(getResources().getString(R.string.fp_txt_unable_use_pwd_login));
			SpUtil.put(PreferencesUtils.FINGERPRINT_IDENTIFIER, false);
			finish();
		}
	}

	@Override
	public void onPasswordVerification() {
		startPasswrodVerification();
	}
}
