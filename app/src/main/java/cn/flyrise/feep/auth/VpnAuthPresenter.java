package cn.flyrise.feep.auth;

import static cn.flyrise.feep.auth.views.BaseAuthActivity.sExtraLogoUrl;

import android.app.NotificationManager;
import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.android.protocol.entity.LoginRequest;
import cn.flyrise.android.protocol.entity.LoginResponse;
import cn.flyrise.android.shared.bean.UserBean;
import cn.flyrise.android.shared.model.user.UserInfo;
import cn.flyrise.feep.FEApplication;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.processor.AddressBookDownloadServices;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.request.Request;
import cn.flyrise.feep.core.services.model.NetworkInfo;
import cn.flyrise.feep.core.services.model.UserKickPrompt;
import cn.flyrise.feep.core.watermark.WMStamp;
import cn.flyrise.feep.dbmodul.database.FeepOADataBase;
import cn.flyrise.feep.dbmodul.utils.UserInfoTableUtils;
import cn.flyrise.feep.location.service.LocationService;
import cn.flyrise.feep.mobilekey.MokeyProvider;
import cn.flyrise.feep.mobilekey.model.MokeyInfo;
import cn.flyrise.feep.protocol.FeepLoginUserServices;
import cn.flyrise.feep.protocol.FeepPathServices;
import cn.flyrise.feep.protocol.FeepRsaService;
import cn.trust.mobile.key.sdk.api.MoKeyEngine;
import com.google.gson.JsonSyntaxException;
import com.hyphenate.chatui.utils.FeepPushManager;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * @author ZYP
 * @since 2016-10-17 11:51
 */
public class VpnAuthPresenter implements AuthContract.AuthPresenter {

	// 历史遗留问题，无需放进 strings.xml
	private static final String NOT_ARROW_LOGIN_CN = "该设备不允许账户";
	private static final String NOT_ARROW_LOGIN_TW = "該設備不允許帳戶";

	private AuthContract.AuthView mAuthView;
	private UserBean mUserBean;
	private boolean isForgetPassword;
	private String mLastLoginName;

	//为了登陆的时候要强制校验手机盾加的
	private MokeyInfo mokeyInfo;
	private MokeyProvider mokeyProvider;
	private LoginResponse mLoginResponse;

	public VpnAuthPresenter(AuthContract.AuthView authView) {
		this(authView, false);
	}

	public VpnAuthPresenter(AuthContract.AuthView authView, boolean isForgetPassword) {
		this.mAuthView = authView;
		this.isForgetPassword = isForgetPassword;
	}

	public void initAuthPresenter(UserBean userBean) {
		this.mUserBean = userBean;
		this.mLastLoginName = userBean.getLoginName();

		if (this.isForgetPassword) {
			this.mUserBean.setPassword("");
			this.mUserBean.setAutoLogin(false);
//            this.mUserBean.setSavePassword(false);
			UserInfoTableUtils.insert(userBean);
		}

		if (mUserBean.isAutoLogin()
				&& mUserBean.isSavePassword()
				&& !TextUtils.isEmpty(mUserBean.getLoginName())
				&& !TextUtils.isEmpty(mUserBean.getPassword())) {    // 满足自动登录的条件
			executeLogin();
		}
	}

	public void setAuthView(AuthContract.AuthView authView) {
		this.mAuthView = authView;
	}

	public void notifyUserInfoChange() {
		this.mUserBean = UserInfoTableUtils.find();
	}

	public void startLogin(String loginName, String password) {
		final Context context = mAuthView.getContext();
		if (TextUtils.isEmpty(loginName)) {
			mAuthView.loginError(context.getResources().getString(R.string.login_username_empty));
			return;
		}

		if (TextUtils.isEmpty(password)) {
			mAuthView.loginError(context.getResources().getString(R.string.login_password_empty));
			return;
		}

		if (!TextUtils.equals(mLastLoginName, loginName)) {//登录前切换用户，清空极光Token
			CoreZygote.clearDevicesToken();
		}

		this.mUserBean.setLoginName(loginName);
		this.mUserBean.setPassword(password);

		if (TextUtils.isEmpty(mUserBean.getServerAddress())) {      // 用户未设置服务器地址和端口
//				|| TextUtils.isEmpty(mUserBean.getServerPort())) {      // 用户未设置服务器地址和端口
			FEToast.showMessage(context.getString(R.string.login_address_empty));
			mAuthView.uiDispatcher(AuthContract.AuthView.URL_CODE_NETIP_SETTING_ACTIVITY);
			return;
		}
		executeLogin();
	}

	public void executeLogin() {
		if (this.mUserBean.isVPN()) {   // 初始化 VPN 设置
			mAuthView.initVpnSetting();
			return;
		}
		executeLoginRequest();
	}

	public void executeLoginRequest() {//封装一层，去获取加密公钥
//		String path = mUserBean.isHttps() ? "https" : "http" + "://" + mUserBean.getServerAddress() + ":" + mUserBean.getServerPort();
//		RsaManager.obtainRsaPuclicKey(path)// 获取移动端加密公钥
//				.subscribeOn(Schedulers.io())
//				.observeOn(AndroidSchedulers.mainThread())
//				.subscribe(result -> {
//					FELog.i("vpn-->publicKey:success:" + result);
//					CoreZygote.addRsaService(new FeepRsaService(result));
//					startExecuteLoginRequest();
//				}, throwable -> {
//					FELog.i("vpn-->publicKey:error");
		CoreZygote.addRsaService(new FeepRsaService(""));
//					startExecuteLoginRequest();
//				});
		startExecuteLoginRequest();
	}

	private void startExecuteLoginRequest() {
		FELog.i("vpn-->executeLoginRequest ...");
		mAuthView.showLoading();
		String encodePassword = CommonUtil.toBase64Password(mUserBean.getPassword());
		String serverAddress = this.mUserBean.getServerAddress();
		String serverPort = this.mUserBean.getServerPort();
		boolean isHttps = this.mUserBean.isHttps();

		StringBuilder urlBuilder = new StringBuilder(isHttps ? "https" : "http");
		urlBuilder.append("://").append(serverAddress);
		if (!TextUtils.isEmpty(serverPort)) urlBuilder.append(":").append(serverPort);

		SpUtil.put(PreferencesUtils.USER_IP, urlBuilder.toString());
		tryInitHttpClient(serverAddress, serverPort, isHttps);

		final Request<LoginRequest> request = LoginRequest.buildRequest(mUserBean.getLoginName(), encodePassword);
		request.getReqContent().deviceId = DevicesUtil.getDeviceUniqueId();

		FEHttpClient.getInstance().post(request, new ResponseCallback<LoginResponse>(mAuthView) {
			@Override
			public void onCompleted(LoginResponse loginResponse) {
				if (TextUtils.equals("1028", loginResponse.getErrorCode())) {
					mAuthView.toUpdate(loginResponse.getErrorMessage());
					return;
				}

				if (TextUtils.equals(loginResponse.getErrorCode(), "-98")) {
					String errorMessage = loginResponse.getErrorMessage();
					if (!TextUtils.isEmpty(errorMessage)) {
						if (errorMessage.contains(NOT_ARROW_LOGIN_CN) || errorMessage.contains(NOT_ARROW_LOGIN_TW)) {
							retryLoginAgain(request);
							return;
						}
					}
				}
				else if (TextUtils.equals(loginResponse.getErrorCode(), "100001")) {
					retryLoginAgain(request);
					return;
				}
				initMokey(loginResponse);
			}

			@Override
			public void onFailure(RepositoryException repositoryException) {
				Exception exception = repositoryException.exception();
				if (exception instanceof JsonSyntaxException || exception instanceof IllegalStateException) {
					retryLoginAgain(request);
				}
				else {
					handleLoginException(exception);
				}
			}
		});
	}

	private void retryLoginAgain(Request<LoginRequest> request) {
		request.getReqContent().deviceId = DevicesUtil.getDeviceAddress();
		FEHttpClient.getInstance().post(request, new ResponseCallback<LoginResponse>(mAuthView) {
			@Override
			public void onCompleted(LoginResponse loginResponse) {
				initMokey(loginResponse);
			}

			@Override
			public void onFailure(RepositoryException repositoryException) {
				Exception exception = repositoryException.exception();
				handleLoginException(exception);
			}
		});
	}

	private void tryInitHttpClient(String serverAddress, String serverPort, boolean isHttps) {
		try {
			FEHttpClient.getInstance();
		} catch (Exception exception) {
			new FEHttpClient.Builder(CoreZygote.getContext())
					.address(serverAddress)
					.port(serverPort)
					.isHttps(isHttps)
					.keyStore(CoreZygote.getPathServices().getKeyStoreFile())
					.build();
		}
	}


	private void initMokey(final LoginResponse loginResponse) {
		if (!TextUtils.equals(loginResponse.getErrorCode(), "0")) {                     // 登录失败
			mAuthView.loginError(loginResponse.getErrorMessage());
			return;
		}

		if (loginResponse.getFeVersion() < 65) {  // 非 65 服务器
			mAuthView.loginError(String.format(mAuthView.getContext().getResources().getString(R.string.login_version_error)
					, loginResponse.getFeVersion()));
			return;
		}
		mLoginResponse = loginResponse;
		mokeyInfo = loginResponse.getMobileKeyMenu();
		//没有手机盾参数，让他直接登录
		if (mokeyInfo == null || TextUtils.isEmpty(mokeyInfo.getKeyID())) {
			handleLoginResult(loginResponse);
			return;
		}
		CoreZygote.addMobileKeyService(mokeyInfo);
		//获取手机盾的一个key状态
		mAuthView.hideLoading();
		mokeyProvider = new MokeyProvider(mAuthView.getContext());
		mokeyProvider.getKeyState().subscribe(errorCode -> {
			mokeyInfo.setKeyExist(errorCode == MoKeyEngine.SUCCESS || errorCode == MoKeyEngine.ERROR_LOCAL_KEY_EXIST);
			if (!mokeyInfo.isCompleState()) {
				handleLoginResult(loginResponse);
				return;
			}
			if (!mokeyInfo.isActivate()) {
				mAuthView.openMokeySafePwdActivity();
				return;
			}
			if (mokeyInfo.isKeyExist()) {
				verifyMokey();
				return;
			}
			mAuthView.loginError(mAuthView.getContext().getString(R.string.mokey_verify_error));
		}, throwable -> mAuthView.loginError(throwable.getMessage()));
	}

	@Override
	public void activieMokey(String pwd) {
		mokeyProvider.active().subscribe(errorCode -> {
			if (errorCode == 0) {
				mokeyInfo.setKeyExist(true);
				mokeyInfo.setActivate(true);
				mokeyProvider.sendActiveState(pwd);
				handleLoginResult(mLoginResponse);
			}
			else {
				mAuthView.loginError(mAuthView.getContext().getString(R.string.mokey_active_error));
			}
		}, throwable -> mAuthView.loginError(mAuthView.getContext().getString(R.string.mokey_active_error)));
	}

	public void updateRememberPwd(boolean isRemember) {
		mUserBean.setSavePassword(isRemember);
	}

	public void updateAutoLogin(boolean isAutoLogin) {
		mUserBean.setAutoLogin(isAutoLogin);
	}

	private void verifyMokey() {
		mokeyProvider.userSign().subscribe(errorCode -> {
			if (errorCode == 0) {
				handleLoginResult(mLoginResponse);
			}
			else {
				mAuthView.loginError(mAuthView.getContext().getString(R.string.mokey_verify_error));
			}
		}, throwable -> mAuthView.loginError(mAuthView.getContext().getString(R.string.mokey_verify_error)));
	}

	private void handleLoginResult(final LoginResponse loginResponse) {

		FEApplication application = (FEApplication) mAuthView.getContext().getApplicationContext();
		application.setGroupVersion(loginResponse.isGroupVersion());

		SpUtil.put(PreferencesUtils.NINEPOINT_SET_PASSWORD, "");

		String accessToken = loginResponse.getAccessToken();                            // 保存用户 Token，仅在位置上报时使用
		SpUtil.put(PreferencesUtils.USER_ACCESSTOKEN,
				TextUtils.isEmpty(accessToken) ? "" : accessToken);

		WMStamp.getInstance().setWaterMark(loginResponse.getUserName(), loginResponse.getDepartment());

		String userId = loginResponse.getUserID();
		String preUserId = SpUtil.get(PreferencesUtils.USER_ID, "");
		FELog.i("UserId = " + userId + ", PreUserId = " + preUserId);

		// 不同用户，清理账号安全设置
		if (!TextUtils.equals(userId, preUserId)) {
			SpUtil.put(PreferencesUtils.LOGIN_GESTRUE_PASSWORD, false);
			SpUtil.put(PreferencesUtils.FINGERPRINT_IDENTIFIER, false);
			SpUtil.put(PreferencesUtils.LOGIN_FACE, false);

			//清除旧机制的数据库内容
			FeepOADataBase.clearTables();
		}
		SpUtil.put(PreferencesUtils.USER_ID, userId + "");

		((FeepPathServices) CoreZygote.getPathServices()).setUserId(userId);

//		FeepPushManager.deleteAlias();                //登录成功删除Alias值，防止离线消息冲突
		FeepPushManager.reInitAllalias();             // 设置 JPush Alias

		UserInfo userInfo = saveUserInfo(loginResponse, userId, loginResponse.getUserName());
		FeepLoginUserServices loginUserServices = new FeepLoginUserServices();
		loginUserServices.setFeVersion(loginResponse.getFeVersion());
		loginUserServices.setUserName(userInfo.getUserName());
		loginUserServices.setUserId(userId);
		loginUserServices.setAccessToken(loginResponse.getAccessToken());
		loginUserServices.setSmallVersion(loginResponse.getSmallVersion());

		NetworkInfo networkInfo = new NetworkInfo();
		networkInfo.serverAddress = mUserBean.getServerAddress();
		networkInfo.serverPort = mUserBean.getServerPort();
		networkInfo.isHttps = mUserBean.isHttps();
		loginUserServices.setNetworkInfo(networkInfo);
		CoreZygote.addLoginUserServices(loginUserServices);

		saveLockUserHref();
		if (!TextUtils.isEmpty(mLastLoginName) && !TextUtils.equals(mLastLoginName, userInfo.getLoginName())) { // 切换用户，清空状态栏
			((NotificationManager) mAuthView.getContext().getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
		}

		SpUtil.put(K.preferences.address_frist_common, false);//常用联系人加载防重复标识

		String logoUrl = userInfo.getUrl() + loginResponse.getLogoUrl();
		SpUtil.put(sExtraLogoUrl, logoUrl);

		// 登录成功后，启动一下定位服务。
//		LocationService.startLocationService(mAuthView.getContext(), LocationService.REQUESTCODE);

		//清楚掉一些缓存文件
		// FileUtil.deleteFolderFile(CoreZygote.getPathServices().getBaseTempPath(), false);

		// 启动通讯录下载服务
		AddressBookDownloadServices.start(mAuthView.getContext());

		mAuthView.hideLoading();
		mAuthView.loginSuccess();
	}

	private UserInfo saveUserInfo(LoginResponse loginResponse, String userId, String userName) {
		final UserInfo userInfo = new UserInfo();
		userInfo.setUserID(userId);
		userInfo.setUserName(userName);
		userInfo.setDepartment(loginResponse.getDepartment());
		userInfo.setUserPost(loginResponse.getUserPost());
		userInfo.setBottomMenu(loginResponse.getBottomMenu());

		userInfo.setLoginName(mUserBean.getLoginName());
		userInfo.setPassword(mUserBean.getPassword());
		userInfo.setServerAddress(mUserBean.getServerAddress());
		userInfo.setServerPort(mUserBean.getServerPort());
		userInfo.setServerHttpsPort(mUserBean.getHttpsPort());
		userInfo.setImid(loginResponse.getImid());
		userInfo.setHttps(mUserBean.isHttps());
		userInfo.setSavePassword(mUserBean.isSavePassword());
		userInfo.setAvatarUrl(loginResponse.getHeadUrl());
		userInfo.setVpn(mUserBean.isVPN());
		((FEApplication) mAuthView.getContext().getApplicationContext()).setUserInfo(userInfo);

		mUserBean.setUserID(userId);
		mUserBean.setUserName(userName);
		UserInfoTableUtils.insert(mUserBean);
		FEHttpClient.getInstance().bindWebViewCookie();
		return userInfo;
	}

	private void saveLockUserHref() {
		final UserInfo userGesturesLoginInfo = ((FEApplication) mAuthView.getContext().getApplicationContext()).getUserInfo();
		if (userGesturesLoginInfo != null) {
			String userHref = userGesturesLoginInfo.getAvatarUrl();
			if (!TextUtils.isEmpty(userHref)) {
				userGesturesLoginInfo.setAvatarUrl(userHref);
			}
		}
		final String userInfo = GsonUtil.getInstance().toJson(userGesturesLoginInfo);
		SpUtil.put(PreferencesUtils.NINEPOINT_USER_INFO, userInfo);
	}

	public void tryToShowUserKickDialog() {
		String data = SpUtil.get(PreferencesUtils.USER_KICK_PROMPT, "");
		if (TextUtils.isEmpty(data)) {
			return;
		}

		UserKickPrompt ukp = GsonUtil.getInstance().fromJson(data, UserKickPrompt.class);
		if (ukp == null) {
			return;
		}

		String prompt = ukp.getUserKickPrompt();
		if (ukp.isUserKick()) {
			ukp.setIsUserKick(false);
			ukp.setUserKickPrompt("");
			SpUtil.put(PreferencesUtils.USER_KICK_PROMPT, GsonUtil.getInstance().toJson(ukp));
			if (!TextUtils.isEmpty(prompt)) {
				new FEMaterialDialog.Builder(mAuthView.getContext()).setTitle(null).setMessage(prompt).setPositiveButton(null, null).build()
						.show();
			}
		}
	}

	private void handleLoginException(Throwable exception) {
		String errorMessage = null;
		if (exception != null) {
			if (exception instanceof ConnectException || exception instanceof UnknownHostException) {// 连接服务器失败，可能是没联网
				errorMessage = CommonUtil.getString(R.string.core_http_failure);
			}
			else if (exception instanceof SocketTimeoutException) {                                 // 连接超时，可能网络太渣
				errorMessage = CommonUtil.getString(R.string.core_http_timeout);
			}

			if (exception instanceof JsonSyntaxException || exception instanceof IllegalStateException) {
				FELog.e("devicesId = " + DevicesUtil.getDeviceUniqueId());
				errorMessage = CommonUtil.getString(R.string.auth_mac_check_failed) + DevicesUtil.getDeviceUniqueId();
			}
		}
		mAuthView.loginError(errorMessage);
	}

	public UserBean getUserBean() {
		return mUserBean;
	}
}
