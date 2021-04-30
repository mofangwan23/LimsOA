package cn.flyrise.feep.auth.views;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import cn.flyrise.android.protocol.model.CaptureReturnData;
import cn.flyrise.feep.R;
import cn.flyrise.feep.auth.NetIPSettingContract;
import cn.flyrise.feep.auth.NetIPSettingPresenter;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.base.views.UISwitchButton;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.cookie.PersistentCookieJar;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import cn.flyrise.feep.utils.ParseCaptureUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import java.io.InputStream;
import javax.net.ssl.HttpsURLConnection;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;

/**
 * @author ZYP
 * @since 2016-10-18 10:38
 */
public class NetIPSettingActivity extends BaseActivity implements NetIPSettingContract.View, TextWatcher {

	public static final String KEY_STORE_NAME = "FEkey";
	public static final int SCANNING_QR_CODE = 10009;

	private Button mBtnSubmit;
	private FEToolbar mToolBar;
	private FELoadingDialog mLoadingDialog;
	private EditText mEtServerAddress;
	private EditText mEtServerPort;

	private UISwitchButton mChkRemember;
	private UISwitchButton mChkAutoLogin;
	private UISwitchButton mChkHttps;
	private UISwitchButton mChkVpn;

	private ViewGroup mVpnLabelLayout;
	private TextView mTvVpnAddress;
	private TextView mTvVpnPort;
	private TextView mTvVpnAccount;
	private TextView mTvVpnPassword;
	private NetIPSettingContract.Presenter mSettingPresenter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		mSettingPresenter = new NetIPSettingPresenter(this);
		mSettingPresenter.initNetIPSettingPresenter();
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		super.toolBar(toolbar);
		this.mToolBar = toolbar;
		this.mToolBar.setTitle(R.string.setting_title);
		this.mToolBar.setRightText(getResources().getString(R.string.setting_ok));
		this.mToolBar.setRightTextClickListener(v -> {
			if (TextUtils.equals(mToolBar.getRightText(), getResources().getString(R.string.setting_ok))) {
				saveUserInput();
				return;
			}
			setResult(200);
			finish();
		});
	}

	@Override
	public void bindView() {
		mBtnSubmit = (Button) findViewById(R.id.btnIPSubmit);
		mEtServerAddress = (EditText) findViewById(R.id.etServerAddress);
		mEtServerPort = (EditText) findViewById(R.id.etServerPort);
		mChkRemember = (UISwitchButton) findViewById(R.id.chkRemember);
		mChkAutoLogin = (UISwitchButton) findViewById(R.id.chkAutoLogin);
		mChkHttps = (UISwitchButton) findViewById(R.id.chkHttps);
		mChkVpn = (UISwitchButton) findViewById(R.id.chkVpn);

		mVpnLabelLayout = (ViewGroup) findViewById(R.id.layoutVpn);
		mTvVpnAddress = (TextView) findViewById(R.id.tvVpnAddress);
		mTvVpnPort = (TextView) findViewById(R.id.tvVpnPort);
		mTvVpnAccount = (TextView) findViewById(R.id.tvVpnAccount);
		mTvVpnPassword = (TextView) findViewById(R.id.tvVpnPassword);
	}

	@Override
	public void bindListener() {
		findViewById(R.id.tvQCodeScan).setOnClickListener(v -> FePermissions.with(NetIPSettingActivity.this)
				.permissions(new String[]{Manifest.permission.CAMERA})
				.rationaleMessage(getResources().getString(R.string.permission_rationale_camera))
				.requestCode(PermissionCode.CAMERA)
				.request());

		mBtnSubmit.setOnClickListener(v -> saveUserInput());

		mChkVpn.setOnClickListener(v -> {
			if (mChkVpn.isChecked()) {
				showVpnSettingDialog();
			}
			else {
				mVpnLabelLayout.setVisibility(View.GONE);
			}
		});

		mChkAutoLogin.setOnClickListener(v -> {
			if (mChkAutoLogin.isChecked()) {
				mChkRemember.setChecked(true);
			}
		});

		mChkRemember.setOnClickListener(v -> {
			if (!mChkRemember.isChecked()) {
				mChkAutoLogin.setChecked(false);
			}
		});

		mEtServerAddress.addTextChangedListener(this);
		mEtServerPort.addTextChangedListener(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		CaptureReturnData crd = ParseCaptureUtils.parseData(data, requestCode);
		if (crd == null) {
			return;
		}
		mEtServerAddress.setText(TextUtils.isEmpty(crd.getIp()) ? "" : crd.getIp());
		mEtServerPort.setText(TextUtils.isEmpty(crd.getPort()) ? "" : crd.getPort());
		mChkHttps.setChecked(crd.isHttps());
		mChkVpn.setChecked(crd.isOpenVpn());
		if (crd.isOpenVpn()) {
			displayVpnLayout(crd.getVpnAddress(), crd.getVpnPort(), crd.getVpnName(), crd.getVpnPassword());
		}
		else {
			displayVpnLayout("", "", "", "");
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
	}

	@PermissionGranted(PermissionCode.CAMERA)
	public void onCameraPermissionGranted() {
		if (DevicesUtil.isCameraCanUsed(NetIPSettingActivity.this)) {
			startActivityForResult(new Intent(NetIPSettingActivity.this, CaptureActivity.class), SCANNING_QR_CODE);
		}
	}

	@Override
	public void showLoading() {
		if (mLoadingDialog == null) {
			mLoadingDialog = new FELoadingDialog.Builder(this)
					.setCancelable(true)
					.setOnDismissListener(new FELoadingDialog.OnDismissListener() {
						@Override
						public void onDismiss() {
							try {
								FEHttpClient.cancel(this);
							} catch (Exception ex) {
								ex.printStackTrace();
							} finally {
								mToolBar.getRightTextView().setText(getResources().getString(R.string.setting_ok));
								mBtnSubmit.setText(getResources().getString(R.string.collaboration_recorder_ok));
							}
						}
					})
					.create();
		}
		mLoadingDialog.show();
		mToolBar.getRightTextView().setText(getResources().getString(R.string.setting_cancel));
		mBtnSubmit.setText(getResources().getString(R.string.collaboration_recorder_cancel));
	}

	@Override
	public void onSettingSuccess() {
		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}

		OkHttpClient okhttpClient = FEHttpClient.getInstance().getOkHttpClient();
		if (mChkHttps.isChecked()) {
			HttpsURLConnection.setDefaultSSLSocketFactory(okhttpClient.sslSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(okhttpClient.hostnameVerifier());
//			Glide.get(this).register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(okhttpClient));
		}
		final CookieJar cookieJar = okhttpClient.cookieJar();
		if (cookieJar != null && cookieJar instanceof PersistentCookieJar) {
			((PersistentCookieJar) cookieJar).clear();
		}

		FEToast.showMessage(getResources().getString(R.string.message_operation_alert));
		setResult(200);
		finish();
	}

	@Override
	public void onSettingFailed(String failedMessage) {
		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}
		if (!TextUtils.isEmpty(failedMessage)) {
			FEToast.showMessage(failedMessage);
		}
		mToolBar.getRightTextView().setText(getResources().getString(R.string.setting_ok));
		mBtnSubmit.setText(getResources().getString(R.string.collaboration_recorder_ok));
	}

	@Override
	public Context getContext() {
		return this;
	}

	@Override
	public void initUserSetting(String serverAddress, String port,
			boolean isRemember, boolean isAutoLogin, boolean isHttps, boolean isVpn,
			String vpnAddress, String vpnPort, String vpnAccount, String vpnPassword) {
		mEtServerAddress.setText(serverAddress);

		if (TextUtils.equals(port, "80")) {
			port = "";
		}
		mEtServerPort.setText(port);

		mChkRemember.setChecked(isRemember);
		mChkAutoLogin.setChecked(isAutoLogin);
		mChkHttps.setChecked(isHttps);
		mChkVpn.setChecked(isVpn);

		if (isVpn) {
			mVpnLabelLayout.setVisibility(android.view.View.VISIBLE);
			mTvVpnAddress.setText(vpnAddress);
			mTvVpnPort.setText(vpnPort);
			mTvVpnAccount.setText(vpnAccount);
			mTvVpnPassword.setText(vpnPassword);
		}
	}

	public void showVpnSettingDialog() {
		new VpnSettingFragment()
				.setVpnAddress(mTvVpnAddress.getText().toString())
				.setVpnPort(mTvVpnPort.getText().toString())
				.setVpnUsername(mTvVpnAccount.getText().toString())
				.setVpnPassword(mTvVpnPassword.getText().toString())
				.setOnCompletedListener(new VpnSettingFragment.OnCompleteListener() {
					@Override
					public void onCompleted(String address, String port, String username, String password) {
						mChkVpn.setChecked(true);
						displayVpnLayout(address, port, username, password);
					}
				})
				.setOnCancelListener(new VpnSettingFragment.OnCancelListener() {
					@Override
					public void onCancel() {
						mChkVpn.setChecked(false);
					}
				})
				.show(getSupportFragmentManager(), "100");
	}

	public void displayVpnLayout(String address, String port, String username, String password) {
		mVpnLabelLayout.setVisibility(mChkVpn.isChecked() ? android.view.View.VISIBLE : android.view.View.GONE);
		mTvVpnAddress.setText(address);
		mTvVpnPort.setText(port);
		mTvVpnAccount.setText(username);
		mTvVpnPassword.setText(password);
	}

	public void saveUserInput() {
		DevicesUtil.tryCloseKeyboard(this);
		String serverAddress = mEtServerAddress.getText().toString().trim();
		String serverPort = mEtServerPort.getText().toString().trim();

		boolean isRemember = mChkRemember.isChecked();
		boolean isAutoLogin = mChkAutoLogin.isChecked();
		boolean isHttps = mChkHttps.isChecked();
		boolean isVpn = mChkVpn.isChecked();
//		if (TextUtils.isEmpty(serverPort)) {
//			if (isHttps) {
//				serverPort = DEFULAT_SSL_PORT;
//			}
//			else {
//				serverPort = DEFULAT_PORT;
//			}
//		}
		if (isVpn) {
			String vpnServerAddress = mTvVpnAddress.getText().toString();
			String vpnServerPost = mTvVpnPort.getText().toString();
			String vpnUsername = mTvVpnAccount.getText().toString();
			String vpnPassword = mTvVpnPassword.getText().toString();
			mSettingPresenter.saveUserSetting(serverAddress, serverPort,
					isRemember, isAutoLogin, isHttps, isVpn,
					vpnServerAddress, vpnServerPost, vpnUsername, vpnPassword);
			return;
		}

		mSettingPresenter.saveUserSetting(serverAddress, serverPort, isRemember, isAutoLogin, isHttps);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		int softHeight = SpUtil.get(DevicesUtil.SOFT_INPUT_HEIGHT, 0);
		if (softHeight == 0) {
			DevicesUtil.getSupportSoftInputHeight(NetIPSettingActivity.this);
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
	}
}
