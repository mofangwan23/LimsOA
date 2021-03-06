package cn.flyrise.feep.more;

import static cn.flyrise.feep.core.common.utils.PreferencesUtils.FINGERPRINT_IDENTIFIER;
import static cn.flyrise.feep.core.common.utils.PreferencesUtils.LOGIN_GESTRUE_PASSWORD;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.flyrise.android.shared.model.user.UserInfo;
import cn.flyrise.feep.FEApplication;
import cn.flyrise.feep.K;
import cn.flyrise.feep.K.salary;
import cn.flyrise.feep.R;
import cn.flyrise.feep.auth.views.fingerprint.FingerprintNewUnLockActivity;
import cn.flyrise.feep.auth.views.gesture.GestureUnLockActivity;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.base.views.UISwitchButton;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.common.utils.NetworkUtil;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import cn.flyrise.feep.core.services.IMobileKeyService;
import cn.flyrise.feep.core.services.model.NetworkInfo;
import cn.flyrise.feep.fingerprint.FingerprintIdentifier;
import cn.flyrise.feep.mobilekey.MoKeySettingActivity;
import cn.flyrise.feep.userinfo.modle.UserInfoDetailItem;
import cn.flyrise.feep.userinfo.views.ModifyPasswordActivity;
import cn.flyrise.feep.utils.Patches;
import cn.flyrise.feep.utils.SafetyVerifyManager;
import cn.flyrise.feep.utils.SafetyVerifyManager.Callback;


/**
 * Created by Administrator on 2017-4-27.
 * ?????????????????????????????????????????????????????????~
 */
//public class AccountSecurityActivity extends BaseActivity implements EyeKeyRegisterListener, EyeKeyLoadingListener {
public class AccountSecurityActivity extends BaseActivity {

	private static final int CODE_FINGERPRINT_TO_GESTURE = 1256;
	private TextView mOpenGesturePassword;
	private View mFingerprintLayout;
	private UISwitchButton mFingerprintSwitchButton;
	private FingerprintIdentifier mFingerprintIdentifier;

	private SafetyVerifyManager mSafetyVerifyManager;
	protected FELoadingDialog mLoadingDialog;

	private RelativeLayout mRlMoKey;
	private ImageView mIvMoKey;

	private FEToolbar mToolbar;
	private UISwitchButton mFaceDiscrenSwitch;
//	private FaceRecognitionImpl mFaceRecognition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		}
		this.mFingerprintIdentifier = new FingerprintIdentifier(this);
		setContentView(R.layout.account_security_layout);
	}

	@Override
	public void bindView() {
		mToolbar = findViewById(R.id.toolBar);
		mOpenGesturePassword = findViewById(R.id.gesture_password_open);
		mFingerprintLayout = findViewById(R.id.layoutFingerprint);
		mFingerprintSwitchButton = findViewById(R.id.switchFingerprint);
		mSafetyVerifyManager = new SafetyVerifyManager(this);
		mRlMoKey = findViewById(R.id.rlMokey);
		mIvMoKey = findViewById(R.id.ivMokey);
		mFaceDiscrenSwitch = findViewById(R.id.switchFaceDiscren);
	}

	@Override
	public void bindData() {
		mToolbar.setBackgroundColor(Color.parseColor("#00000000"));
		mToolbar.setTitle(getResources().getString(R.string.reside_menu_item_security));
		mToolbar.setLineVisibility(View.GONE);
		mToolbar.setDarkMode();
		if (FunctionManager.hasPatch(Patches.PATCH_USER_INFO_MODIFY)) {
			findViewById(R.id.modify_password_layout).setVisibility(View.VISIBLE);
		}
		mFaceDiscrenSwitch.setChecked(false);
//		mFaceRecognition = new EyeKeyCheckManager();
//		NetworkInfo info = CoreZygote.getLoginUserServices().getNetworkInfo();
//		mFaceRecognition.isOpenFace(info.serverAddress + info.serverPort + CoreZygote.getLoginUserServices().getUserId()
//				, hasRegister -> mFaceDiscrenSwitch.setChecked(SpUtil.get(PreferencesUtils.LOGIN_FACE, false)));
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mOpenGesturePassword == null) {
			return;
		}
		boolean isCheckeds = SpUtil.get(LOGIN_GESTRUE_PASSWORD, false);
		mOpenGesturePassword.setText(isCheckeds
				? getResources().getString(R.string.gesture_password_open)
				: getResources().getString(R.string.gesture_password_off));

		if (mFingerprintIdentifier.isFingerprintEnable()) {
			mFingerprintLayout.setVisibility(View.VISIBLE);
		}
		else {
			mFingerprintLayout.setVisibility(View.GONE);
			SpUtil.put(PreferencesUtils.FINGERPRINT_IDENTIFIER, false);
		}

		mFingerprintSwitchButton.setChecked(SpUtil.get(PreferencesUtils.FINGERPRINT_IDENTIFIER, false));

		IMobileKeyService mokeyService = CoreZygote.getMobileKeyService();
		if (mokeyService != null) {
			mRlMoKey.setVisibility(View.VISIBLE);
			mIvMoKey.setVisibility(mokeyService.isNormal() ? View.GONE : View.VISIBLE);
		}
		else {
			mRlMoKey.setVisibility(View.GONE);
		}
	}

	@Override
	public void bindListener() {
		findViewById(R.id.modify_password_layout).setOnClickListener(v -> openModifyPassword());
		findViewById(R.id.hand_password_layout).setOnClickListener(v -> {
			boolean isSetUpGesture = SpUtil.get(LOGIN_GESTRUE_PASSWORD, false);
			if (isSetUpGesture) {                                                           // ???????????????????????????
				openPassWordTypeActivity();                                                 // ??????????????????
				return;
			}

			boolean isSetUpFingerprint = SpUtil.get(FINGERPRINT_IDENTIFIER, false);         // ?????????????????????????????????????????????????????????????????????
			if (isSetUpFingerprint) {
				Intent lockIntent = new Intent(AccountSecurityActivity.this, FingerprintNewUnLockActivity.class);
				lockIntent.putExtra("lockMainActivity", false);
				lockIntent.putExtra("allowForgetPwd", false);
				startActivityForResult(lockIntent, CODE_FINGERPRINT_TO_GESTURE);
				return;
			}

			mSafetyVerifyManager.startVerify(1024, new Callback() {            // ?????????????????????
				@Override
				public void onPreVerify() {
					showLoading();
				}

				@Override
				public void onVerifySuccess() {
					hideLoading();
					openPassWordTypeActivity();
				}

				@Override
				public void onVerifyFailed(boolean isPasswordVerify) {
					hideLoading();
					if (!NetworkUtil.isNetworkAvailable(AccountSecurityActivity.this)) {
						// ?????????????????????????????????????????????????????????????????????????????????????????????
						FEToast.showMessage(CommonUtil.getString(R.string.core_http_timeout));
						return;
					}
					FEToast.showMessage(getResources().getString(R.string.salary_pwd_verify_failed));
				}
			});
		});

		mFingerprintSwitchButton.setOnClickListener(v -> {
			mFingerprintSwitchButton.setChecked(true);

			boolean isSetUpFingerprint = SpUtil.get(FINGERPRINT_IDENTIFIER, false);     // ?????????????????????
			if (isSetUpFingerprint) {                                                   // ?????????????????????????????????????????????????????????)
				Intent lockIntent = new Intent(this, FingerprintNewUnLockActivity.class);
				lockIntent.putExtra("lockMainActivity", false);
				lockIntent.putExtra("allowForgetPwd", false);
				startActivityForResult(lockIntent, K.salary.fingerprint_verify_request_code);
				return;
			}

			SpUtil.put(PreferencesUtils.FINGERPRINT_IDENTIFIER, true);
			mFingerprintSwitchButton.setChecked(true);

			SpUtil.put(LOGIN_GESTRUE_PASSWORD, false);
			mOpenGesturePassword.setText(getResources().getString(R.string.gesture_password_off));
		});

		mRlMoKey.setOnClickListener(v -> startActivity(new Intent(AccountSecurityActivity.this, MoKeySettingActivity.class)));

		mFaceDiscrenSwitch.setOnClickListener(v -> {
			if (mFaceDiscrenSwitch.isChecked()) {
				FePermissions.with(AccountSecurityActivity.this)
						.permissions(new String[]{Manifest.permission.CAMERA})
						.rationaleMessage(getResources().getString(cn.flyrise.feep.media.R.string.permission_rationale_camera))
						.requestCode(PermissionCode.CAMERA)
						.request();
			}
			else {
				SpUtil.put(PreferencesUtils.LOGIN_FACE, false);
				NetworkInfo info = CoreZygote.getLoginUserServices().getNetworkInfo();
//				mFaceRecognition.deletePeopleName(info.serverAddress + info.serverPort + CoreZygote.getLoginUserServices().getUserId()
//						, () -> FEToast.showMessage("???????????????"));
			}
		});
	}

	@PermissionGranted(PermissionCode.CAMERA)
	public void onCameraPermissionGrated() {    // ????????????
		showLoading();
//		mFaceRecognition.startVerify(this, this);
	}

	private void openPassWordTypeActivity() {
		boolean isGestureUnLock = SpUtil.get(PreferencesUtils.LOGIN_GESTRUE_PASSWORD, false);       // ???????????????????????????
		if (isGestureUnLock) {                         // ?????????????????????
			Intent lockIntent = new Intent(this, GestureUnLockActivity.class);
			lockIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			lockIntent.putExtra("lockMainActivity", false);
			lockIntent.putExtra("allowForgetPwd", false);
			startActivityForResult(lockIntent, K.salary.gesture_verify_request_code);
		}
		else {
			openActivity(SetPassWordTypeActivity.class);
		}
	}

	private void openModifyPassword() {
		UserInfo userInfo = ((FEApplication) this.getApplication()).getUserInfo();
		if (userInfo == null) {
			return;
		}
		UserInfoDetailItem bean = new UserInfoDetailItem();
		bean.itemType = K.userInfo.DETAIL_LOGIN_PASSWORD;
		bean.title = getResources().getString(R.string.login_password);
		bean.content = userInfo.getPassword();

		Intent intent = new Intent(this, ModifyPasswordActivity.class);
		intent.putExtra("USER_BEAN", GsonUtil.getInstance().toJson(bean));
		startActivity(intent);
	}

	private void openActivity(Class<? extends Activity> activityClass) {
		startActivity(new Intent(this, activityClass));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == K.salary.gesture_verify_request_code && resultCode != 404) {         // ????????????????????????
			openActivity(SetPassWordTypeActivity.class);
		}
		else if (requestCode == salary.fingerprint_verify_request_code && resultCode == 1001) { // ?????????????????????????????????
			SpUtil.put(PreferencesUtils.FINGERPRINT_IDENTIFIER, false);
			mFingerprintSwitchButton.setChecked(false);
		}
		else if (requestCode == CODE_FINGERPRINT_TO_GESTURE && resultCode == 1001) {            // ??????????????????
			SpUtil.put(PreferencesUtils.FINGERPRINT_IDENTIFIER, false);
			mFingerprintSwitchButton.setChecked(false);
			openActivity(SetPassWordTypeActivity.class);                                        // ????????????????????????
		}
//		else if (requestCode == EyeKeyCheckManager.REQUEST_DETECT) {
//			if (resultCode == RESULT_CANCELED) {
//				FEToast.showMessage("????????????????????????");
//				mFaceDiscrenSwitch.setChecked(false);
//				return;
//			}
//
//			if (data == null) {
//				FEToast.showMessage("????????????");
//				mFaceDiscrenSwitch.setChecked(false);
//				return;
//			}
//			NetworkInfo info = CoreZygote.getLoginUserServices().getNetworkInfo();
//			mFaceRecognition.registerResult(
//					new EyeFaceBuilder.Builder(this)
//							.setData(data)
//							.setEyeKeyOAOnly(info.serverAddress + info.serverPort)
//							.setUserId(CoreZygote.getLoginUserServices().getUserId())
//							.setLoadingListener(this)
//							.setRegisterListener(this)
//							.build());
//		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.hideLoading();
		if (mFingerprintIdentifier != null) {
			mFingerprintIdentifier.stopAuthenticate();
		}
	}

	protected void showLoading() {
		this.hideLoading();
		mLoadingDialog = new FELoadingDialog.Builder(this)
				.setCancelable(true)
				.setLoadingLabel(getResources().getString(R.string.core_loading_wait))
				.setOnDismissListener(this::finish)
				.create();
		mLoadingDialog.show();
	}

	protected void hideLoading() {
		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}
	}

//	@Override
//	public void registerSuccess() {
//		mFaceDiscrenSwitch.setVisibility(View.VISIBLE);
//		mFaceDiscrenSwitch.setChecked(true);
//	}

//	@Override
//	public void registerFailure() {
//		mFaceDiscrenSwitch.setChecked(false);
//	}
//
//	@Override
//	public void loadingShow() {
//		showLoading();
//	}
//
//	@Override
//	public void loadingHint() {
//		hideLoading();
//	}
}
