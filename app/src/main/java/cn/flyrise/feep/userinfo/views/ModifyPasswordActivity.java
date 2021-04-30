package cn.flyrise.feep.userinfo.views;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.userinfo.contract.ModifyPasswordContract;
import cn.flyrise.feep.userinfo.modle.UserInfoDetailItem;
import cn.flyrise.feep.userinfo.presenter.ModifyPasswordPresenter;
import cn.flyrise.feep.userinfo.widget.ModifyPasswordEiditext;

/**
 * cm on 2017-4-25.
 * 修改密码
 */
public class ModifyPasswordActivity extends BaseActivity implements ModifyPasswordContract.View {

	private UserInfoDetailItem mBean;

	private Button mConfirm;

	private ModifyPasswordEiditext mMeUsed;//原密码
	private ModifyPasswordEiditext mMeOne;//第一次输入
	private ModifyPasswordEiditext mMeTow;//再次输入

	private ModifyPasswordContract.presenter mPresenter;

	private Handler mHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.modify_password_layout);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		super.toolBar(toolbar);
		if (toolbar != null) {
			toolbar.setTitle(getResources().getString(R.string.modify_password_title));
		}
	}

	@Override
	public void bindView() {
		super.bindView();
		mConfirm = this.findViewById(R.id.submit);
		mMeUsed = this.findViewById(R.id.used_me);
		mMeOne = this.findViewById(R.id.one_me);
		mMeTow = this.findViewById(R.id.tow_me);
		mMeUsed.setTitle(getResources().getString(R.string.modify_login_password));
		mMeOne.setTitle(getResources().getString(R.string.modify_new_password));
		mMeTow.setTitle(getResources().getString(R.string.modify_confirm_password));
		mMeUsed.setHint(getResources().getString(R.string.password_used_hind));
		mMeOne.setHint(getResources().getString(R.string.password_modify_hind));
		mMeTow.setHint(getResources().getString(R.string.modify_confirm_hind));
	}

	@Override
	public void bindData() {
		super.bindData();
		mPresenter = new ModifyPasswordPresenter(this);
		initIntent();
		mHandler.postDelayed(() -> {
			InputMethodManager inputManager = (InputMethodManager) mMeUsed.getContext().getSystemService(INPUT_METHOD_SERVICE);
			assert inputManager != null;
			inputManager.showSoftInput(mMeUsed, 0);
		}, 360);
	}

	private void initIntent() {
		if (getIntent() == null) {
			return;
		}
		String text = getIntent().getStringExtra("USER_BEAN");
		if (TextUtils.isEmpty(text)) {
			return;
		}
		mBean = GsonUtil.getInstance().fromJson(text, UserInfoDetailItem.class);
	}

	@Override
	public void bindListener() {
		super.bindListener();
		mConfirm.setOnClickListener(v -> {
			if (!isPasswordDataCorrect()) {
				return;
			}
			mPresenter.successModifyPassword(getBeanConent(), mMeOne.getContent());
		});
	}

	private boolean isPasswordDataCorrect() {
		if (isDataNull()) {
			FEToast.showMessage(getResources().getString(R.string.input_password));
			return false;
		}
		if (!isPasswordSuccess()) {
			mMeUsed.setError(getResources().getString(R.string.password_used_error));
			return false;
		}
		if (isNewPasswordAndUsedPasswordIdentical()) {
			mMeOne.setError(getResources().getString(R.string.password_modify_error));
			return false;
		}
		if (!TextUtils.equals(mMeTow.getContent(), mMeOne.getContent())) {
			mMeTow.setError(getResources().getString(R.string.modify_confirm_error));
			return false;
		}
		return true;
	}

	private String getBeanConent() {
		return mBean == null ? "" : mBean.content;
	}

	//数据为空
	private boolean isDataNull() {
		return TextUtils.isEmpty(getBeanConent())
				|| TextUtils.isEmpty(mMeUsed.getContent())
				|| TextUtils.isEmpty(mMeOne.getContent())
				|| TextUtils.isEmpty(mMeTow.getContent());
	}

	//密码错误
	private boolean isPasswordSuccess() {
		return TextUtils.equals(mMeUsed.getContent(), getBeanConent());
	}

	//新密码不能和原密码相同
	private boolean isNewPasswordAndUsedPasswordIdentical() {
		return TextUtils.equals(getBeanConent(), mMeOne.getContent());
	}


	@Override
	public void finishModify() {
		finish();
	}

	@Override
	public void showLoading() {
		LoadingHint.show(ModifyPasswordActivity.this);
	}

	@Override
	public void hideLoading() {
		LoadingHint.hide();
	}

	@Override
	public void inputError(String errorText) {
		if (mMeTow != null) mMeTow.setError(errorText);
	}
}
