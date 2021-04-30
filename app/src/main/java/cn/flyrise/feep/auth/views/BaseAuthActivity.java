package cn.flyrise.feep.auth.views;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import cn.flyrise.android.shared.bean.UserBean;
import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.FEMainActivity;
import cn.flyrise.feep.R;
import cn.flyrise.feep.auth.AuthContract;
import cn.flyrise.feep.auth.login.setting.LoginSettingActivity;
import cn.flyrise.feep.auth.unknown.NewLoginActivity;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.dialog.FEMaterialDialog.Builder;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.dbmodul.utils.UserInfoTableUtils;
import cn.flyrise.feep.mobilekey.MokeyModifyPwActivity;
import com.sangfor.ssl.BaseMessage;
import com.sangfor.ssl.LoginResultListener;
import com.sangfor.ssl.SFException;
import com.sangfor.ssl.SangforAuthManager;
import com.sangfor.ssl.common.ErrorCode;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author ZYP
 * @since 2016-10-19 13:19
 */
public abstract class BaseAuthActivity extends BaseActivity implements AuthContract.AuthView, LoginResultListener {

	private final int CREATEPWD_CODE = 10001;

	public static final String EXTRA_FORGET_PASSWORD = "extra_forget_password";
	public static final String sLogoFolder = "logoimage";                                // 保存图片的文件夹名称
	public static final String sLogoFileName = sLogoFolder + ".jpg";                    // 保存图片的文件名称
	public static final String sExtraLogoUrl = "service_logo_url";                      // 保存图片下载前的地址，用于比较图片是否更新

	protected UserBean mUserBean;
	private boolean isVpnInit = false;
	private boolean loginVpn = false;

	protected boolean isShowToast = true;//提示应用退到后台，防止被替换
	private SangforAuthManager mSFManager = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mUserBean = UserInfoTableUtils.find();
	}

	protected FELoadingDialog mLoadingDialog;
	protected AuthContract.AuthPresenter mAuthPresenter;

	@Override
	protected void onResume() {
		super.onResume();
		FEUmengCfg.onActivityResumeUMeng(this, "LoginActivity");
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 404 && mAuthPresenter != null) {
//			if (loginVpn) {
//				SangforAuth sfAuth = SangforAuth.getInstance();
//				if (sfAuth != null) sfAuth.vpnLogout();
//				loginVpn = false;
//			}
			mUserBean = UserInfoTableUtils.find();
//			initSangVpn(mUserBean.isVPN());
			startVPNInitAndLogin(mUserBean.isVPN());
			mAuthPresenter.notifyUserInfoChange();
			if (mUserBean.isVPN()) {
				FEHttpClient.cancelHttpClient();
			}
		}
		if (requestCode == CREATEPWD_CODE && resultCode == RESULT_OK) {
			String pwd = data.getStringExtra("result");
			mAuthPresenter.activieMokey(pwd);
		}
	}

	@Override
	public void showLoading() {
		if (mLoadingDialog == null) {
			mLoadingDialog = new FELoadingDialog.Builder(this)
					.setCancelable(true)
					.setOnDismissListener(() -> {
						try {
							FEHttpClient.cancel(BaseAuthActivity.this);
						} catch (Exception ex) {
							ex.printStackTrace();
						} finally {
							loginError(null);
						}
					})
					.setLoadingLabel(getString(R.string.login_ing))
					.create();
		}
		mLoadingDialog.show();
	}

	@Override
	public void hideLoading() {
		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}
	}

	@Override
	public void uiDispatcher(int urlCode) {
		if (urlCode == AuthContract.AuthView.URL_CODE_NETIP_SETTING_ACTIVITY) {
			startActivityForResult(new Intent(this, LoginSettingActivity.class), 404);
		}
		else if (urlCode == AuthContract.AuthView.URL_CODE_LOGIN_ACTIVITY) {
			Intent intent = new Intent(this, NewLoginActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			intent.putExtra(EXTRA_FORGET_PASSWORD, true);
			startActivity(intent);
			sendBroadcast(new Intent(FEMainActivity.CANCELLATION_LOCK_RECEIVER));
		}
	}

	@Override
	public void initVpnSetting() {
		FELog.i("vpn-->InitVpnSetting ...");
		if (loginVpn) {
			mAuthPresenter.executeLoginRequest();
			return;
		}
		startVPNInitAndLogin(mUserBean.isVPN());
	}

	//vpn初始登录统一接口
	private void startVPNInitAndLogin(boolean isVpnOpen) {
		if (!isVpnOpen || isFinishing()) return;
		if (!isVpnInit) initLoginParms();
		showLoading();//开启登录进度框
		loginVpn = false;
		try {
			//依据登录方式调用相应的登录接口
			String vpnUrl = "https://" + mUserBean.getVpnAddress()
					+ ":" + (TextUtils.isEmpty(mUserBean.getVpnPort()) ? "443" : mUserBean.getVpnPort());
			//该接口做了两件事：1.vpn初始化；2.用户名/密码主认证过程
			mSFManager.startPasswordAuthLogin(getApplication(), this, VPNMode.EASYAPP,
					new URL(vpnUrl), mUserBean.getVpnUsername(), mUserBean.getVpnPassword());
		} catch (SFException e) {
			interceptVpnErrorMessage("VPN登录异常");
			hideLoad();
			FELog.i("-->>>vpn-SFException:%s", e);
		} catch (MalformedURLException e) {
			interceptVpnErrorMessage("VPN登录异常");
			e.printStackTrace();
			hideLoad();
			FELog.i("-->>>vpn-SFException:%s", e);
		}
	}

	//vpn初始化登录参数
	private void initLoginParms() {
		mSFManager = SangforAuthManager.getInstance();// 1.构建SangforAuthManager对象
		try {
			mSFManager.setLoginResultListener(this);// 2.设置VPN认证结果回调
			isVpnInit = true;
		} catch (SFException e) {
			isVpnInit = false;
			FELog.i("-->>>vpn-SFException:%s", e);
			interceptVpnErrorMessage("VPN初始化异常");
		}
		mSFManager.setAuthConnectTimeOut(8);//3.设置登录超时时间，单位为秒
	}

	//vpn登录失败回调接口
	@Override
	public void onLoginFailed(ErrorCode errorCode, String errorStr) {
		if (interceptVpnErrorMessage(errorStr)) return;
		hideLoad();
		loginVpn = false;
		loginError("VPN登录失败：" + (TextUtils.isEmpty(errorStr) ? "" : errorStr));
	}

	//vpn登录进行中回调接口
	@Override
	public void onLoginProcess(int nextAuthType, BaseMessage message) {
		//停止登录进度框
//		cancelWaitingProgressDialog();
//		// 存在多认证, 需要进行下一次认证
//		Toast.makeText(this, getString(R.string.str_next_auth) +
//				SFUtils.getAuthTypeDescription(nextAuthType), Toast.LENGTH_SHORT).show();
//		SangforAuthDialog sfAuthDialog = new SangforAuthDialog(this);
//		createAuthDialog(sfAuthDialog, nextAuthType, message);
//		mDialog.show();
	}

	//VPN登录成功回调
	@Override
	public void onLoginSuccess() {
		loginVpn = true;
		mAuthPresenter.executeLoginRequest();// 认证成功后即可开始访问资源
	}

	@Override
	public void loginError(String errorMessage) {
		try {//VPN在界面销毁后还会回调，导致崩溃
			hideLoad();
			if (TextUtils.isEmpty(errorMessage) || this.isFinishing()) return;
			if (TextUtils.equals(errorMessage, "无效请求：null")) {
				errorMessage = getString(R.string.core_http_network_exception);
			}
			boolean isInputPasswordError = TextUtils.equals(getString(R.string.login_input_error), errorMessage);
			boolean isDevicesError = errorMessage.contains(CommonUtil.getString(R.string.auth_mac_check_failed));
			FEMaterialDialog.Builder builder = new FEMaterialDialog.Builder(this)
					.setMessage(errorMessage)
					.setCancelable(false)
					.setPositiveButton(isInputPasswordError ? getString(R.string.login_reset_password) : null, dialog -> {
						if (isInputPasswordError) resetPassword();
					});

			if (isDevicesError) {
				builder.setNeutralButton(R.string.copy_device_id, dialog -> {
					ClipboardManager cmb = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
					cmb.setText(DevicesUtil.getDeviceUniqueId());
				});
			}
			builder.build().show();
		} catch (Exception e) {

		}
	}

	private void hideLoad() {
		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}
	}


	protected boolean interceptVpnErrorMessage(String errorMessage) {
		return false;
	}


	@Override
	public void openMokeySafePwdActivity() {
		new Builder(this)
				.setMessage(R.string.mokey_hint_must_active)
				.setNegativeButton(null, dialog -> loginError(getString(R.string.mokey_error_no_active_for_login)))
				.setPositiveButton(null, dialog -> {
					Intent intent = new Intent(getContext(), MokeyModifyPwActivity.class);
					intent.putExtra("type", MokeyModifyPwActivity.TYPE_CREATE);
					startActivityForResult(intent, CREATEPWD_CODE);
				}).build().show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}
		if (mAuthPresenter != null) {
			mAuthPresenter = null;
		}
	}

	@Override
	public Context getContext() {
		return this;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			new FEMaterialDialog.Builder(this)
					.setTitle(R.string.dialog_default_title)
					.setMessage(R.string.login_exit)
					.setPositiveButton(null, dialog -> {
						isShowToast = false;
						CoreZygote.getApplicationServices().exitApplication();
						try {
							SangforAuthManager.getInstance().vpnLogout();
						} catch (Exception ex) {
						}
					})
					.setNegativeButton(R.string.dialog_default_cancel_button_text, null)
					.build()
					.show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	protected void resetPassword() {

	}

	protected void vpnError() {

	}


}
