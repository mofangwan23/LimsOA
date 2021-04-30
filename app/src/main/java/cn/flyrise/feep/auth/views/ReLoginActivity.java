package cn.flyrise.feep.auth.views;

import android.app.Activity;
import android.content.Intent;
import cn.flyrise.feep.auth.unknown.NewLoginActivity;
import cn.flyrise.feep.auth.views.fingerprint.NewFingerprintLoginActivity;
import cn.flyrise.feep.auth.views.gesture.GestureLoginActivity;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.fingerprint.FingerprintIdentifier;

/**
 * @author klc
 * @since 2017-12-14 15:41
 */
public class ReLoginActivity extends BaseActivity {

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Class<? extends Activity> reloginActivityClass = null;
        boolean isGestureLogin = SpUtil.get(PreferencesUtils.LOGIN_GESTRUE_PASSWORD, false);
        if (isGestureLogin) {
            reloginActivityClass = GestureLoginActivity.class;
        }

        if (reloginActivityClass == null) {
            boolean isFingerprintLogin = SpUtil.get(PreferencesUtils.FINGERPRINT_IDENTIFIER, false);
            FingerprintIdentifier identifier = new FingerprintIdentifier(this);
            if (isFingerprintLogin && identifier.isFingerprintEnable()) {
                reloginActivityClass = NewFingerprintLoginActivity.class;
//                reloginActivityClass = FingerprintLoginActivity.class;
            }
        }

        if (reloginActivityClass == null) {
            reloginActivityClass = NewLoginActivity.class;
        }

        Intent intent = new Intent(this, reloginActivityClass);
        startActivity(intent);
        finish();
    }
}
