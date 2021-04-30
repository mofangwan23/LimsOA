package cn.flyrise.feep.core.base.component;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import cn.flyrise.feep.core.R;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.AndroidBug5497Workaround;
import cn.flyrise.feep.core.common.FEStatusBar;
import cn.flyrise.feep.core.common.utils.DevicesUtil;

/**
 * @author klc
 * @since 2017-12-11 15:08
 */
public abstract class NotTranslucentBarActivity extends BaseActivity {

	@Override
	protected void toolBar(FEToolbar toolbar) {
		super.toolBar(toolbar);
		if ((Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT && FEStatusBar.canModifyStatusBar(getWindow()))
				|| (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.M)) {
			AndroidBug5497Workaround.assistActivity(this);
			int statusBarHeight = DevicesUtil.getStatusBarHeight(this);
			toolbar.setPadding(0, statusBarHeight, 0, 0);
		}
	}

	@Override
	protected void setupStatusBar() {
		if (DevicesUtil.isSpecialDevice()) {
			return;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
			getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimary));
			//垃圾小米...6.0不支持原生的方法。
			if (FEStatusBar.isXiaoMi()) {
				FEStatusBar.setMIUIStatusBarMode(getWindow(), true);
			}
			return;
		}
		super.setupStatusBar();

	}

}
