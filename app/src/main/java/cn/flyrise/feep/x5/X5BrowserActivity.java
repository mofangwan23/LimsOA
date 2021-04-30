package cn.flyrise.feep.x5;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.FEStatusBar;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.x5.dispatcher.RequestDispatcher;
import cn.squirtlez.frouter.annotations.Route;

/**
 * @author 社会主义接班人
 * @since 2018-09-17 14:51
 */
@Route("/x5/browser")
public class X5BrowserActivity extends AppCompatActivity {

	private X5BrowserFragment fragment;
	private RequestDispatcher dispatcher;
	private boolean isNewForm;

	@Override protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dispatcher = URLTransferStation.getInstance().getDispatcher(getIntent());
		if (dispatcher.needIntercept()) {
			dispatcher.doIntercept(this);
			finish();
			return;
		}

		setContentView(R.layout.activity_x5_browser);
		initStatusBar();
		initPage();
	}

	private void initPage() {
		String homeLink = dispatcher.getHomeLink();
		Bundle args = new Bundle();
		args.putString("homeLink", homeLink);
		if(getIntent()!=null) {
			args.putBoolean("isNewForm", getIntent().getBooleanExtra("isNewForm",false));
		}

		fragment = new X5BrowserFragment();
		fragment.setArguments(args);
		getSupportFragmentManager().beginTransaction()
				.add(R.id.layoutFragmentContainer, fragment)
				.show(fragment)
				.commitAllowingStateLoss();

		findViewById(R.id.btnGoBack).setOnClickListener(view -> fragment.goBack());
		findViewById(R.id.btnForward).setOnClickListener(view -> fragment.goForward());
		findViewById(R.id.btnRefresh).setOnClickListener(view -> fragment.reload());
		findViewById(R.id.btnFinish).setOnClickListener(view -> finish());
	}

	private void initStatusBar() {
		if (VERSION.SDK_INT == VERSION_CODES.KITKAT) {
			if (FEStatusBar.canModifyStatusBar(getWindow())) {
				WindowManager.LayoutParams params = getWindow().getAttributes();
				params.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | params.flags);
				int statusBarHeight = DevicesUtil.getStatusBarHeight(this);
				View container = findViewById(R.id.layoutFragmentContainer);
				container.setPadding(0, statusBarHeight, 0, 0);
				FEStatusBar.setDarkStatusBar(this);
			}
			return;
		}

		if (VERSION.SDK_INT >= VERSION_CODES.M) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
			getWindow().setStatusBarColor(getResources().getColor(R.color.defaultColorPrimary));
			return;
		}

		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			if (FEStatusBar.canModifyStatusBar(getWindow())) {
				FEStatusBar.setDarkStatusBar(this);
				getWindow().setStatusBarColor(getResources().getColor(R.color.defaultColorPrimary));
				return;
			}
			getWindow().setStatusBarColor(Color.BLACK);
		}
	}

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (fragment != null) {
			fragment.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override public void onBackPressed() {
		if (fragment == null || !fragment.goBack()) {
			super.onBackPressed();
		}
	}
}
