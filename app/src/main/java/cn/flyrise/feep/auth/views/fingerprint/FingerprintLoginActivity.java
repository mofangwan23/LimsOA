package cn.flyrise.feep.auth.views.fingerprint;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import cn.flyrise.feep.R;
import cn.flyrise.feep.auth.AuthContract;
import cn.flyrise.feep.auth.views.BaseThreeLoginActivity;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.fingerprint.FingerprintIdentifier;
import cn.flyrise.feep.fingerprint.callback.BaseAuthenticationCallback;

/**
 * @author ZYP
 * @since 2017-05-04 15:58
 */
public class FingerprintLoginActivity extends BaseThreeLoginActivity {

    private FingerprintIdentifier mFingerprintIdentifier;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint_login);
    }

    @Override public void bindView() {
        super.bindView();
    }

    @Override public void bindData() {
        super.bindData();
        mFingerprintIdentifier = new FingerprintIdentifier(this, new BaseAuthenticationCallback() {
            @Override public void onAuthenticationHelp(int helpCode, String helpString) {
                mTvErrorPrompt.setTextColor(getResources().getColor(R.color.lock_pattern_password_title));
                mTvErrorPrompt.setText(helpString);
            }

            @Override public void onAuthenticationError(int errorCode, String errorString) {
                if(errorCode == 7){
                    mTvErrorPrompt.setTextColor(Color.RED);
                    mTvErrorPrompt.setText(getResources().getString(R.string.fp_txt_retry_more_use_pwd_login));
                }
            }

            @Override public void onAuthenticationFailed() {
                mTvErrorPrompt.setTextColor(getResources().getColor(R.color.lock_pattern_password_title));
                mTvErrorPrompt.setText(R.string.fp_txt_fingerprint_not_match);
            }

            @Override public void onAuthenticationSucceeded() {
                mTvRetryPrompt.setVisibility(View.INVISIBLE);
                if (mUserBean != null && mUserBean.isVPN()) {
                    initVpnSetting();
                }
                else {
                    mAuthPresenter.executeLogin();
                }
            }
        });

        if (!mFingerprintIdentifier.isFingerprintEnable()) {    // 使用的指纹解锁，但在设置里关了，所以这是使用指纹是没用的。
            FEToast.showMessage(getString(R.string.fp_txt_unable_use_pwd_login));
            SpUtil.put(PreferencesUtils.FINGERPRINT_IDENTIFIER, false);
            uiDispatcher(AuthContract.AuthView.URL_CODE_LOGIN_ACTIVITY);
        }
    }

    @Override protected void onResume() {
        super.onResume();
        if (mFingerprintIdentifier != null && mFingerprintIdentifier.isFingerprintEnable()) {
            mFingerprintIdentifier.startAuthenticate();
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (mFingerprintIdentifier != null) {
            mFingerprintIdentifier.stopAuthenticate();
        }
    }
}
