package cn.flyrise.feep.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import cn.flyrise.android.shared.model.user.UserInfo;
import cn.flyrise.feep.FEApplication;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.auth.views.fingerprint.FingerprintNewUnLockActivity;
import cn.flyrise.feep.auth.views.gesture.GestureUnLockActivity;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.function.Module;
import cn.flyrise.feep.more.AccountSecurityActivity;
import cn.flyrise.feep.salary.BaseSalaryActivity;
import cn.flyrise.feep.salary.SalaryDataSources;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2017-09-22 11:35
 */
public class SafetyVerifyManager {

	private Activity mActivity;
	private Callback mCallback;
	private int mRequestCode;
	private AlertDialog mVerifyDialog;

	public SafetyVerifyManager(Activity activity) {
		this.mActivity = activity;
		if (mActivity == null) {
			throw new NullPointerException("The target activity must not be null.");
		}
	}

	public interface Callback {

		/**
		 * 开始校验之前，用于显示 Loading
		 */
		void onPreVerify();

		/**
		 * 校验成功
		 */
		void onVerifySuccess();

		/**
		 * 校验失败
		 */
		void onVerifyFailed(boolean isPasswordVerify);
	}

	@SuppressWarnings("all") public void startVerify(int requestCode, Callback callback) {
		this.mCallback = callback;
		this.mRequestCode = requestCode;
		boolean showVerify = mActivity.getIntent().getBooleanExtra(K.salary.show_verify_dialog, true);
		if (showVerify) {                                                                               // 开始进行校验
			if (SpUtil.get(PreferencesUtils.LOGIN_GESTRUE_PASSWORD, false)) {                           // 执行手势解锁
				startGestureVerify();
			}
			else if (SpUtil.get(PreferencesUtils.FINGERPRINT_IDENTIFIER, false)) {                      // 执行指纹解锁
				startFingerprintVerify();
			}
			else {                                                                                      // 执行密码解锁
				startPasswordVerify();
			}
		}
		else {                                                                                          // 不需要进行任何校验
			if (callback != null) {
				callback.onVerifySuccess();
			}
		}
	}

	private void startGestureVerify() {
		Intent lockIntent = newIntent(GestureUnLockActivity.class);
		lockIntent.putExtra("isSalary", true);
		mActivity.startActivityForResult(lockIntent, mRequestCode);
	}

	private void startFingerprintVerify() {
		Intent lockIntent = newIntent(FingerprintNewUnLockActivity.class);
		mActivity.startActivityForResult(lockIntent, mRequestCode);
	}

	private Intent newIntent(Class clazz) {
		Intent lockIntent = new Intent(mActivity, clazz);
		lockIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		lockIntent.putExtra("lockMainActivity", false);
		lockIntent.putExtra("allowForgetPwd", false);
		return lockIntent;
	}

	private void startPasswordVerify() {
		View dialogView = View.inflate(mActivity, R.layout.dialog_salary_pwd_verify, null);
		TextView textView = (TextView) dialogView.findViewById(R.id.tvLabel);
		textView.setText(getVerifyDialogTitle());

		final EditText etPassword = (EditText) dialogView.findViewById(R.id.etPassword);
		etPassword.setFocusable(true);
		etPassword.setFocusableInTouchMode(true);
		etPassword.requestFocus();

		etPassword.setOnEditorActionListener((v, actionId, event) -> {
			String text = etPassword.getText().toString().trim();
			if (TextUtils.isEmpty(text)) {
				FEToast.showMessage(CommonUtil.getString(R.string.login_password_empty));
				return true;
			}
			verifyPassword(CommonUtil.toBase64Password(text), true);
			return false;
		});

		dialogView.findViewById(R.id.tvConfirm).setOnClickListener(v -> {
			String text = etPassword.getText().toString().trim();
			if (TextUtils.isEmpty(text)) {
				FEToast.showMessage(CommonUtil.getString(R.string.login_password_empty));
				return;
			}
			verifyPassword(CommonUtil.toBase64Password(text), true);
		});

		mVerifyDialog = new AlertDialog.Builder(mActivity)
				.setView(dialogView)
				.setCancelable(true)
				.setOnCancelListener(dialog -> {
					dialog.dismiss();
					mActivity.finish();
				})
				.create();
		mVerifyDialog.setCanceledOnTouchOutside(true);
		mVerifyDialog.show();

		Observable.timer(500, TimeUnit.MILLISECONDS)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(time -> DevicesUtil.showKeyboard(etPassword));
	}

	private void verifyPassword(String password, boolean isPasswordVerify) {
		verifyPassword(password, isPasswordVerify, mCallback);
	}

	public void verifyPassword(String password, boolean isPasswordVerify, Callback callback) {
		if (callback != null) {
			callback.onPreVerify();
		}

		Module salaryModule = FunctionManager.findModule(Func.Salary);
		if (salaryModule != null) {
			SalaryDataSources.verifyPassword(password)                                              // 校验密码
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(code -> {
						if (code == 1) {
							if (mVerifyDialog != null && mVerifyDialog.isShowing()) {
								mVerifyDialog.dismiss();
							}

							if (callback != null) {
								callback.onVerifySuccess();
							}
						}
						else {
							if (callback != null) {
								callback.onVerifyFailed(isPasswordVerify);
							}
						}
					}, exception -> {
						exception.printStackTrace();
						if (callback != null) {
							callback.onVerifyFailed(isPasswordVerify);
						}
					});
		}
		else {
			FEApplication application = (FEApplication) CoreZygote.getContext();
			UserInfo userInfo = application.getUserInfo();
			String userPwd = CommonUtil.toBase64Password(userInfo.getPassword());

			if (TextUtils.equals(userPwd, password)) {
				if (mVerifyDialog != null && mVerifyDialog.isShowing()) {
					mVerifyDialog.dismiss();
				}
				if (callback != null) {
					callback.onVerifySuccess();
				}
			}
			else {
				callback.onVerifyFailed(isPasswordVerify);
			}
		}
	}

	private String getVerifyDialogTitle() {
		if (mActivity instanceof BaseSalaryActivity) {
			return CommonUtil.getString(R.string.salary_verify_dialog_title);
		}
		else if (mActivity instanceof AccountSecurityActivity) {
			return CommonUtil.getString(R.string.reside_menu_item_security);
		}
		return CommonUtil.getString(R.string.login_password_empty);
	}
}
