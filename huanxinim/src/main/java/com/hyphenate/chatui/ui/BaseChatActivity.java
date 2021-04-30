package com.hyphenate.chatui.ui;

import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.common.FEStatusBar;
import cn.flyrise.feep.core.common.utils.DevicesUtil;

/**
 * @author ZYP
 * @since 2017-12-13 16:10
 */
public class BaseChatActivity extends BaseActivity {

	@Override protected void setupStatusBar() {
		if (DevicesUtil.isSpecialDevice()) {
			return;
		}

		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
			WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
			localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
			optionStatusBar();
			return;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			boolean setBarSuccess = false;
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			if (FEStatusBar.canModifyStatusBar(this.getWindow())) {
				setBarSuccess = optionStatusBar();
			}

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
				getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
				return;
			}

			if (setBarSuccess) {
				return;
			}

			getWindow().setStatusBarColor(statusBarColor());
		}
	}

}
