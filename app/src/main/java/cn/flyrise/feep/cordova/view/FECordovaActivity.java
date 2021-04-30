package cn.flyrise.feep.cordova.view;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.view.CordovaButton;
import cn.flyrise.feep.cordova.utils.CordovaShowUtils;
import cn.flyrise.feep.core.common.AndroidBug5497Workaround;
import cn.flyrise.feep.core.common.CordovaShowInfo;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.FEStatusBar;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.squirtlez.frouter.annotations.Route;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewClient;
import org.apache.cordova.IceCreamCordovaWebViewClient;

/**
 * 类描述：继承CordovaActivity
 * @author 罗展健
 * @version FE6.0.3
 */
@Route("/cordova/old/page")
public class FECordovaActivity extends CordovaActivity {

	private CordovaButton button;
	private FELoadingDialog mLoadingDialog;
	private ImageView errorView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setupStatusBar();

		//加载一个显示错误的图片：
		errorView = new ImageView(this);
		errorView.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1.0F));
		errorView.setImageResource(R.drawable.web_error);
		errorView.setScaleType(ImageView.ScaleType.CENTER);
		this.root.addView(errorView);
		errorView.setVisibility(View.GONE);
		errorView.setOnClickListener(v -> {
			spinnerStart(null, null);
			goReload();
		});

		this.init();
		button = new CordovaButton(this);
		setListener();
		this.root.addView(button);

		View view = new View(this);
		view.setBackgroundColor(getResources().getColor(R.color.home_title_color));

		if ((Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT && FEStatusBar.canModifyStatusBar(getWindow()))
				|| (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.M)) {
			AndroidBug5497Workaround.assistActivity(this);
			int paddingTop = (int) getResources().getDimension(R.dimen.main_status_bar_height);
			view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, paddingTop));
			this.root.addView(view, 0);
		}

		String homeLink = getIntent().getStringExtra("homeLink");
		if (TextUtils.isEmpty(homeLink)) {
			CordovaShowInfo mShowInfo = null;
			CordovaShowUtils mShowUtils = CordovaShowUtils.getInstance();
			Intent intent = getIntent();
			if (intent != null) {
				String shoInfo = intent.getStringExtra(CordovaShowUtils.CORDOVA_SHOW_INFO);
				if (!TextUtils.isEmpty(shoInfo)) {
					mShowInfo = GsonUtil.getInstance().fromJson(shoInfo, CordovaShowInfo.class);
				}
			}
			homeLink = mShowUtils.getCordovaWebViewUrl(mShowInfo);
		}
		FELog.e("load url:" + homeLink);
		if (!TextUtils.isEmpty(homeLink)) {
			super.loadUrl(homeLink);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			try {
				CookieManager cookieManager = CookieManager.getInstance();
				cookieManager.setAcceptThirdPartyCookies(this.appView, true);
			} catch (Exception ex) {
			}
		}
	}

	private void setupStatusBar() {
		if (DevicesUtil.isSpecialDevice()) {
			return;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
			getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
			if (FEStatusBar.isXiaoMi()) {
				FEStatusBar.setMIUIStatusBarMode(getWindow(), true);
			}
			return;
		}

		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
			WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
			localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
			FEStatusBar.setDarkStatusBar(this);
			return;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			boolean setBarSuccess = false;
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			if (FEStatusBar.canModifyStatusBar(this.getWindow())) {
				WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
				localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
				setBarSuccess = FEStatusBar.setDarkStatusBar(this);
			}
			if (setBarSuccess) {
				return;
			}
			getWindow().setStatusBarColor(Color.parseColor("#000000"));
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		this.appView.removeAllViews();
		this.appView.destroy();
		FEHttpClient.cancel(this);
	}

	private void setListener() {
		if (button != null) {
			button.setLeftBtnClickListener(onClickListener);
			button.setRightBtnClickListener(onClickListener);
			button.setFinishBtnClickListener(onClickListener);
			button.setReloadBtnClickListener(onClickListener);
		}
	}

	private final View.OnClickListener onClickListener = view -> {
		switch (view.getId()) {
			case R.id.button_left:
				goBack();
				break;
			case R.id.button_right:
				goForward();
				break;
			case R.id.button_finish:
				finish();
				break;
			case R.id.button_reload:
				goReload();
				break;
			default:
				break;
		}
	};

	private void goBack() {
		if (super.appView == null) {
			return;
		}
		if (!appView.backHistory()) {
			finish();
		}
	}

	private void goForward() {
		if (super.appView == null) {
			return;
		}
		super.appView.goForward();
	}

	private void goReload() {
		if (super.appView == null) {
			return;
		}
		super.appView.reload();
	}

	@Override
	public void spinnerStart(String title, String message) {

		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}

		mLoadingDialog = new FELoadingDialog.Builder(this)
				.setLoadingLabel(CommonUtil.getString(R.string.core_loading_wait))
				.setCancelable(true)
				.create();
		mLoadingDialog.show();

	}

	@Override
	public void spinnerStop() {
		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}
	}

	@Override
	protected CordovaWebViewClient makeWebViewClient(CordovaWebView webView) {
		return new FECordovaWebViewClient(this, webView);
	}

	private class FECordovaWebViewClient extends IceCreamCordovaWebViewClient {

		private boolean isLoading;
		private boolean isError;

		FECordovaWebViewClient(CordovaInterface cordova, CordovaWebView view) {
			super(cordova, view);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			isLoading = true;
			isError = false;
		}


		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			if (isLoading && (errorCode == ERROR_HOST_LOOKUP || errorCode == ERROR_CONNECT || errorCode == ERROR_TIMEOUT)) {
				appView.setVisibility(View.GONE);
				errorView.setVisibility(View.VISIBLE);
				isError = true;
			}
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			isLoading = false;
			if (!isError) {
				appView.setVisibility(View.VISIBLE);
				errorView.setVisibility(View.GONE);
			}
			spinnerStop();
		}
	}
}
