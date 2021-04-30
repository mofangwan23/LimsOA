package cn.flyrise.feep.more;

/**
 * 设置密码登录的类型
 */

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.FEMainActivity;
import cn.flyrise.feep.R;
import cn.flyrise.feep.auth.views.gesture.CreateGesturePasswordActivity;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.base.views.UISwitchButton;

public class SetPassWordTypeActivity extends BaseActivity {

    private final String FEEP_UMENG = "SetPassWordTypeActivity";
    private UISwitchButton img_gesture_passwrod;
    private TextView tv_gestrue_password;
    private RelativeLayout reset_password_layout;
    private boolean isCheckeds = false;
    public static final String RESET_PASSWORD = "reset_password";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_password_login_type);
    }

    @Override
    protected void toolBar(FEToolbar toolbar) {
        toolbar.setTitle(R.string.gestrue_password_title);
    }

    @Override
    public void bindView() {
        img_gesture_passwrod = (UISwitchButton) this.findViewById(R.id.image_gettrue_password);
        tv_gestrue_password = (TextView) this.findViewById(R.id.tv_gettrue_password);
        reset_password_layout = (RelativeLayout) this.findViewById(R.id.to_reset_password_layout);
    }


    @Override
    public void bindListener() {
        super.bindListener();
        img_gesture_passwrod.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isCheckeds = isChecked;
                downButtom();
            }
        });
        // 重置手势密码
        reset_password_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCheckeds) {
                    final Intent intent = new Intent(SetPassWordTypeActivity.this, CreateGesturePasswordActivity.class);
                    intent.putExtra(RESET_PASSWORD, true);
                    startActivity(intent);
                }
                else {
                    FEToast.showMessage(getResources().getString(R.string.lbl_text_));
                }
            }
        });
    }

    private void downButtom() {
        if (!isCheckeds) {
//            gesture = false;
            reset_password_layout.setVisibility(View.INVISIBLE);
            tv_gestrue_password.setText(getResources().getString(R.string.off_gestrue_password));
            sendBroadcast(new Intent(FEMainActivity.CANCELLATION_LOCK_RECEIVER));
        }
        else {
//            gesture = true;
            reset_password_layout.setVisibility(View.VISIBLE);
            tv_gestrue_password.setText(getResources().getString(R.string.reset_password));
            final Intent intent = new Intent(this, CreateGesturePasswordActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        FEUmengCfg.onActivityResumeUMeng(this, FEEP_UMENG);
        isCheckeds = SpUtil.get(PreferencesUtils.LOGIN_GESTRUE_PASSWORD, false);
        if (!isCheckeds) {
            img_gesture_passwrod.setChecked(false);
            reset_password_layout.setVisibility(View.INVISIBLE);
            tv_gestrue_password.setText(getResources().getString(R.string.off_gestrue_password));
        }
        else {
            img_gesture_passwrod.setChecked(true);
            reset_password_layout.setVisibility(View.VISIBLE);
            tv_gestrue_password.setText(getResources().getString(R.string.reset_password));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        FEUmengCfg.onActivityPauseUMeng(this, FEEP_UMENG);
        SpUtil.put(PreferencesUtils.LOGIN_GESTRUE_PASSWORD, isCheckeds);
        if (isCheckeds) {
            SpUtil.put(PreferencesUtils.FINGERPRINT_IDENTIFIER, false);
        }
    }

}
