package cn.flyrise.feep.auth.views.fingerprint;

import android.content.Intent;
import android.graphics.Color;
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
import cn.flyrise.feep.fingerprint.FingerprintIdentifier;
import cn.flyrise.feep.fingerprint.callback.BaseAuthenticationCallback;
import cn.flyrise.feep.protocol.FeepApplicationServices;

/**
 * @author ZYP
 * @since 2017-05-04 15:58
 */
public class FingerprintUnLockActivity extends BaseUnLockActivity {

	private FingerprintIdentifier mFingerprintIdentifier;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fingerprint_unlock);
	}

	@Override
	public void bindView() {
		super.bindView();
		mTvRetryPrompt.setVisibility(View.INVISIBLE); // 不显示
	}

	@Override
	public void bindData() {
		super.bindData();
		mFingerprintIdentifier = new FingerprintIdentifier(this, new BaseAuthenticationCallback() {
			@Override
			public void onAuthenticationHelp(int helpCode, String helpString) {
				mTvErrorPrompt.setTextColor(getResources().getColor(R.color.lock_pattern_password_title));
				mTvErrorPrompt.setText(helpString);
			}

			@Override
			public void onAuthenticationError(int errorCode, String errorString) {
				if(errorCode == 7){
					mTvErrorPrompt.setTextColor(Color.RED);
					mTvErrorPrompt.setText(getResources().getString(R.string.fp_txt_retry_more_use_pwd_login));
				}
			}

			@Override
			public void onAuthenticationFailed() {
				mTvErrorPrompt.setTextColor(getResources().getColor(R.color.lock_pattern_password_title));
				mTvErrorPrompt.setText(getResources().getString(R.string.fp_txt_fingerprint_not_match));
			}

			@Override
			public void onAuthenticationSucceeded() { // 验证成功
				if (isLockMainActivity) {
					FeepApplicationServices.sLastTimeUnlockSuccess = System.currentTimeMillis();
					CoreZygote.getApplicationServices().setHomeKeyState(IApplicationServices.HOME_PRESS_AND_UN_LOCKED);
				}
				else {
					setResult(1001);
				}
				finish();
			}
		});
	}

	@Override
	public void bindListener() {
		if (isAllowForgetPwd) {
			mTvForgetPwd.setVisibility(View.VISIBLE);
			mTvForgetPwd.setOnClickListener(view -> {   // 使用密码登陆
				SpUtil.put(PreferencesUtils.FINGERPRINT_IDENTIFIER, false);
				CoreZygote.getApplicationServices().exitApplication();
				Intent intent = new Intent();
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				intent.putExtra(BaseAuthActivity.EXTRA_FORGET_PASSWORD, true);
				intent.setClass(FingerprintUnLockActivity.this, NewLoginActivity.class);
				startActivity(intent);
				sendBroadcast(new Intent(FEMainActivity.CANCELLATION_LOCK_RECEIVER));
				finish();
			});
		}
		else {
			mTvForgetPwd.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mFingerprintIdentifier != null) {
			if (mFingerprintIdentifier.isFingerprintEnable()) {
				mFingerprintIdentifier.startAuthenticate();
			}
			else {  // 指纹失效，在设置关闭了指纹!
				FEToast.showMessage(getResources().getString(R.string.fp_txt_unable_use_pwd_login));
				SpUtil.put(PreferencesUtils.FINGERPRINT_IDENTIFIER, false);
				finish();
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mFingerprintIdentifier != null) {
			mFingerprintIdentifier.stopAuthenticate();
			mFingerprintIdentifier = null;
		}
	}
}
