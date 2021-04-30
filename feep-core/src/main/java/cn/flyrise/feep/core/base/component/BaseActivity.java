package cn.flyrise.feep.core.base.component;

import static cn.flyrise.feep.core.R.id.swipeBackLayout;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import cn.flyrise.feep.core.R;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.base.views.SwipeBackLayout;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.FEStatusBar;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.watermark.WMStamp;

/**
 * @author ZYP
 * @since 2017-03-01 09:48
 */
public abstract class BaseActivity extends AppCompatActivity {

	protected SwipeBackLayout mSwipeBackLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//兼容 Android 0 在 windowIsTranslucent = true的情况下报错
		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		}else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		setupStatusBar();
	}

	protected void setupStatusBar() {
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
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
			if (FEStatusBar.canModifyStatusBar(this.getWindow())) {
				WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
				localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
				setBarSuccess = optionStatusBar();
			}

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				getWindow().setStatusBarColor(Color.TRANSPARENT);
				getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
						| View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
				return;
			}

			if (setBarSuccess) {
				return;
			}

			getWindow().setStatusBarColor(statusBarColor());
		}
	}

	protected void toolBar(FEToolbar toolbar) {
	}

	protected int statusBarColor() {
		return Color.parseColor("#000000");
	}

	/**
	 * 不需要处理状态栏，please return false.
	 * 需要对状态栏进行处理：return FEStatusBar.setDarkStatusBar() or FEStatusBar.setLightStatusBar();
	 */
	protected boolean optionStatusBar() {
		return FEStatusBar.setDarkStatusBar(this);
	}
    @Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		initSwipeBackLayout();              // 初始化左滑
		initToolbar();                      // 初始化状态栏
		bindView();
		bindData();
		bindListener();
	}

	private void initSwipeBackLayout() {
		mSwipeBackLayout = (SwipeBackLayout) findViewById(swipeBackLayout);
		if (mSwipeBackLayout != null) {
			mSwipeBackLayout.setAbleToSwipe(isAbleToSwipe());
			mSwipeBackLayout.setShadowResourceLeft(R.drawable.core_drawer_shadow);
			mSwipeBackLayout.setSliderFadeColor(Color.TRANSPARENT);
			mSwipeBackLayout.setPanelSlideListener(new SwipeBackLayout.SwipeBackListener() {
				final int screenWidth = DevicesUtil.getScreenWidth();

				@Override
				public void onSwipe(View panel, float slideOffset) {
					View view = getCurrentFocus();
					if (view != null) {
						DevicesUtil.hideKeyboard(view);
					}
					int actualOffset = (int) (screenWidth * slideOffset);
					WMStamp.getInstance().update(BaseActivity.this, -actualOffset, 0);
				}

				@Override
				public void onSwipeOpen(View panel) {
					onSwipeOpened();
					finish();
					overridePendingTransition(0, R.anim.core_swipe_out_right);
				}

				@Override
				public void onSwipeClosed(View panel) {
				}
			});
		}
	}

	protected void onSwipeOpened() {
	}

	protected boolean isAbleToSwipe() {
		return true;
	}

	private void initToolbar() {
		FEToolbar toolbar = (FEToolbar) findViewById(R.id.toolBar);
		if (toolbar != null) {
			toolbar.setNavigationOnClickListener(v -> finish());
			toolBar(toolbar);
		}
	}

	public void bindView() {
	}

	public void bindData() {
	}

	public void bindListener() {
	}

	@Override
	public void startActivity(Intent intent) {
		DevicesUtil.tryCloseKeyboard(this);
		super.startActivity(intent);
	}

	@Override
	public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
		DevicesUtil.tryCloseKeyboard(this);
		super.startActivityForResult(intent, requestCode, options);
	}

	@Override
	public void finish() {
		DevicesUtil.tryCloseKeyboard(this);
		super.finish();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
	}
}
