package cn.flyrise.feep.auth.views;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.KeyEvent;
import cn.flyrise.android.shared.bean.UserBean;
import cn.flyrise.android.shared.db.UserTable;
import cn.flyrise.feep.FEApplication;
import cn.flyrise.feep.FEMainActivity;
import cn.flyrise.feep.R;
import cn.flyrise.feep.auth.AuthContract;
import cn.flyrise.feep.auth.VpnAuthPresenter;
import cn.flyrise.feep.auth.server.setting.ServerSettingActivity;
import cn.flyrise.feep.auth.unknown.NewLoginActivity;
import cn.flyrise.feep.auth.views.fingerprint.NewFingerprintLoginActivity;
import cn.flyrise.feep.auth.views.gesture.GestureLoginActivity;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.FileUtil;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import cn.flyrise.feep.core.premission.PermissionSettingDialog;
import cn.flyrise.feep.dbmodul.utils.UserInfoTableUtils;
import cn.flyrise.feep.fingerprint.FingerprintIdentifier;
import cn.flyrise.feep.mobilekey.MokeyModifyPwActivity;
import cn.flyrise.feep.more.GuideActivity;
import cn.flyrise.feep.utils.FEUpdateVersionUtils;
import cn.squirtlez.frouter.annotations.ResultExtras;
import cn.squirtlez.frouter.annotations.Route;
import com.hyphenate.chatui.model.EmNotifierBean;
import com.hyphenate.easeui.EaseUiK;
import com.umeng.analytics.MobclickAgent;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * Update by ZYP
 */
@Route("/auth/splash")
@ResultExtras({"emNotifierBean"})
public class SplashActivity extends Activity implements AuthContract.AuthView {

	private final int CREATEPWD_CODE = 10001;
	private static final String EXTRA_DELAY_TIME = "extra_delay_time";
	private AuthContract.AuthPresenter mPresenter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
			finish();
			return;
		}
		setContentView(R.layout.activity_splash);
		fixUserDataBefore201611();
		FePermissions.with(SplashActivity.this)
				.permissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE})
				.rationaleMessage(getResources().getString(R.string.permission_msg_base_info))
				.requestCode(PermissionCode.BASE)
				.request();
	}

	/**
	 * 兼容 2016-11 之前的版本。新版本数据库结构发生了变动，已经没有 UserTable 这个表。
	 */
	private void fixUserDataBefore201611() {
		UserTable table = new UserTable(this);
		UserBean userBean = table.find(1);
		if (userBean == null || TextUtils.isEmpty(userBean.getUserID())) {
			return;
		}

		try {
			UserInfoTableUtils.insert(userBean);
			table.delete(1);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				table.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults,
				(requestCode1, permissions1, grantResults1, deniedMessage) -> new PermissionSettingDialog.Builder(SplashActivity.this)
						.setCancelable(false)
						.setMessage(getResources().getString(R.string.permission_msg_request_failed_base))
						.setNeutralText(getResources().getString(R.string.permission_text_go_setting))
						.setPositiveText(getResources().getString(R.string.permission_text_i_know))
						.setPositiveListener(v -> finish())
						.setNeutralListener(v -> {
							Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
							intent.setData(Uri.fromParts("package", getPackageName(), null));
							startActivity(intent);
							finish();
						})
						.build()
						.show());
	}

	@PermissionGranted(PermissionCode.BASE)
	public void onBasePermissionGranted() {
		initUmengAnalytics();
		int delayTime = SpUtil.get(EXTRA_DELAY_TIME, 0);
		if (delayTime == 0) {                                                           // 首次登录
			Observable
					.unsafeCreate(f -> {
						try {
							FELog.i("Start to delete base folder, init env.");
							FileUtil.deleteFolderFile(CoreZygote.getPathServices().getBasePath(), false);
							FELog.i("Delete env success, turn to login page.");
						} catch (Exception exp) {
							exp.printStackTrace();
						}
						f.onNext(200);
						f.onCompleted();
					})
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(obj -> {
						startActivity(new Intent(SplashActivity.this, GuideActivity.class));
						overridePendingTransition(R.anim.welcome_screen_enteranim, R.anim.welcome_screen_exitanim);
						finish();
					}, exception -> {
						exception.printStackTrace();
						finish();
					});
		}
		else {
			// 如果没有设置 IP 和 地址，也算第一次登录
			UserBean userBean = UserInfoTableUtils.find();
			if (userBean != null
					&& TextUtils.isEmpty(userBean.getServerAddress())) {
//					|| TextUtils.isEmpty(userBean.getServerPort()))) {
				startActivity(new Intent(SplashActivity.this, ServerSettingActivity.class));
				overridePendingTransition(R.anim.welcome_screen_enteranim, R.anim.welcome_screen_exitanim);
				finish();
				return;
			}

			emNotifierIntent(getIntent());

//			if (SpUtil.get(PreferencesUtils.LOGIN_FACE, false)) {//人脸识别登录
////				Intent intent = new Intent(SplashActivity.this, FaceLoginActivity.class);
////				startActivity(intent);
////				finish();
////				return;
////			}
			if (SpUtil.get(PreferencesUtils.LOGIN_GESTRUE_PASSWORD, false)) {   // 手势登录...
				delayLogin(GestureLoginActivity.class);
			}
			else {
				if (SpUtil.get(PreferencesUtils.FINGERPRINT_IDENTIFIER, false))  // 使用指纹登录
					fingerprintLogin();
				else
					startLogin();
			}
		}
	}

	private void fingerprintLogin(){ // 使用指纹登录
		FingerprintIdentifier fingerprintIdentifier = new FingerprintIdentifier(this);
		if( fingerprintIdentifier.isFingerprintEnable()){
//			delayLogin(FingerprintLoginActivity.class);
			delayLogin(NewFingerprintLoginActivity.class);
		}else{
			startLogin();
		}
	}

	private void startLogin(){
		Observable.timer(1, TimeUnit.SECONDS)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(result -> tryAutoLogin(), exception -> exception.printStackTrace());
	}

	private void emNotifierIntent(Intent intent) {
		if (intent == null || intent.getExtras() == null) return;
		int type = intent.getExtras().getInt(EaseUiK.EmChatContent.emChatType, 0);
		if (type != EaseUiK.EmChatContent.em_chatType_single && type != EaseUiK.EmChatContent.em_chatType_group) return;
		String chatId = intent.getExtras().getString(EaseUiK.EmChatContent.emChatID);
		if (TextUtils.isEmpty(chatId)) return;
		EmNotifierBean bean = new EmNotifierBean();
		bean.emChatID = chatId;
		bean.emChatType = type;
		FEApplication.setEmNotifierBean(bean);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
	}

	private void initUmengAnalytics() {
		MobclickAgent.setDebugMode(false);
		MobclickAgent.openActivityDurationTrack(false);
		MobclickAgent.setSessionContinueMillis(1000);
		MobclickAgent.setCatchUncaughtExceptions(true);
		MobclickAgent.updateOnlineConfig(this);
	}

	private void delayLogin(Class<? extends Activity> activityClass) {
		Observable.timer(1, TimeUnit.SECONDS)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(time -> {
					startActivity(new Intent(SplashActivity.this, activityClass));
					finish();
					overridePendingTransition(R.anim.welcome_screen_enteranim, R.anim.welcome_screen_exitanim);
				}, exception -> {
					exception.printStackTrace();
					finish();
				});
	}

	/**
	 * 尝试进行自动登录
	 */
	private void tryAutoLogin() {
		final UserBean userBean = UserInfoTableUtils.find();
		if (userBean == null) {                                             // 不存在用户
			delayLogin(NewLoginActivity.class);
			return;
		}

		if (!userBean.isAutoLogin()) {                                      // 非自动登录
			delayLogin(NewLoginActivity.class);
			return;
		}

		if (!userBean.isSavePassword()) {                                   // 未保存密码
			delayLogin(NewLoginActivity.class);
			return;
		}

		if (userBean.isVPN()) {                                            // 开启vpn的情况
			delayLogin(NewLoginActivity.class);
			return;
		}

		String username = userBean.getLoginName();
		String password = userBean.getPassword();
		if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {   // 账号或密码为空，不满足自动登录的条件 GG
			delayLogin(NewLoginActivity.class);
			return;
		}
		mPresenter = new VpnAuthPresenter(this);
		mPresenter.initAuthPresenter(userBean);
	}

	@Override
	public void loginSuccess() {
		Intent intent = new Intent(SplashActivity.this, FEMainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
		finish();
	}

	@Override
	public void loginError(String errorMessage) {
		delayLogin(NewLoginActivity.class);
	}

	@Override public void toUpdate(String errorMessage) {
		delayLogin(NewLoginActivity.class);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void hideLoading() {

	}

	@Override
	public void uiDispatcher(int urlCode) {
	}

	@Override
	public Context getContext() {
		return SplashActivity.this;
	}

	@Override
	public void initVpnSetting() {
	}

	@Override
	public void openMokeySafePwdActivity() {
		new FEMaterialDialog.Builder(this)
				.setMessage("当前模式必须激活手机盾")
				.setNegativeButton(null, dialog -> loginError("未激活手机盾，无法登录")).setPositiveButton(null, dialog -> {
			Intent intent = new Intent(getContext(), MokeyModifyPwActivity.class);
			intent.putExtra("type", MokeyModifyPwActivity.TYPE_CREATE);
			startActivityForResult(intent, CREATEPWD_CODE);
		}).build().show();
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CREATEPWD_CODE && resultCode == RESULT_OK) {
			String pwd = data.getStringExtra("result");
			mPresenter.activieMokey(pwd);
		}
	}

//	@Override protected void onResume() {
//		super.onResume();
////		JPushInterface.onResume(getApplicationContext());
//	}
//
//	@Override protected void onPause() {
//		super.onPause();
////		JPushInterface.onPause(getApplicationContext());
//	}
}
