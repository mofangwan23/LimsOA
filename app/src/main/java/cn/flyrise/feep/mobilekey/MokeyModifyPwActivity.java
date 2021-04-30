package cn.flyrise.feep.mobilekey;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.protocol.entity.mokey.MokeyModifyFePwRequest;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.userinfo.widget.ModifyPasswordEiditext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by klc on 2018/3/16.
 * 修改手机盾与FE的校验密码
 */

public class MokeyModifyPwActivity extends BaseActivity {

	private final String REGEX_PASSWORD = "^[\\w_]{6,16}";

	public static final int TYPE_CREATE = 0X001;
	public static final int TYPE_MODIFY = 0X002;

	private FEToolbar mToolBar;
	protected ModifyPasswordEiditext mOriginal;//原密码
	protected ModifyPasswordEiditext mFirstPwd;//第一次输入
	protected ModifyPasswordEiditext mSecondPwd;//再次输入

	private Button mConfirm;
	private int type;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.modify_password_layout);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		super.toolBar(toolbar);
		this.mToolBar = toolbar;
	}

	@Override
	public void bindView() {
		super.bindView();
		mConfirm = (Button) this.findViewById(R.id.submit);
		mOriginal = (ModifyPasswordEiditext) this.findViewById(R.id.used_me);
		mFirstPwd = (ModifyPasswordEiditext) this.findViewById(R.id.one_me);
		mSecondPwd = (ModifyPasswordEiditext) this.findViewById(R.id.tow_me);
		mOriginal.setTitle(getResources().getString(R.string.modify_login_password));
		mFirstPwd.setTitle(getResources().getString(R.string.modify_new_password));
		mSecondPwd.setTitle(getResources().getString(R.string.modify_confirm_password));
		mOriginal.setHint(getResources().getString(R.string.password_used_hind));
		mFirstPwd.setHint(getResources().getString(R.string.password_modify_hind));
		mSecondPwd.setHint(getResources().getString(R.string.modify_confirm_hind));
	}

	@Override
	public void bindData() {
		type = getIntent().getIntExtra("type", TYPE_MODIFY);
		ModifyPasswordEiditext etInput;
		if (type == TYPE_CREATE) {
			mToolBar.setTitle(R.string.mokey_set_safe_pwd);
			etInput = mFirstPwd;
			mOriginal.setVisibility(View.GONE);
		}
		else {
			mToolBar.setTitle(R.string.mokey_modify_safe_pwd);
			etInput = mOriginal;
		}
		new Handler().postDelayed(() -> {
			InputMethodManager inputManager = (InputMethodManager) etInput.getContext().getSystemService(INPUT_METHOD_SERVICE);
			assert inputManager != null;
			inputManager.showSoftInput(mOriginal, 0);
		}, 360);
	}

	@Override
	public void bindListener() {
		super.bindListener();
		mConfirm.setOnClickListener(v -> {
			if (!checkInput()) {
				return;
			}
			if (type == TYPE_CREATE) {
				returnPwd();
			}
			else {
				toModify();
			}
		});
	}


	private boolean checkInput() {
		if ((mOriginal.getVisibility() == View.VISIBLE && checkNull(mOriginal)) || checkNull(mFirstPwd) || checkNull(mSecondPwd)) {
			return false;
		}
		if (mOriginal.getVisibility() == View.VISIBLE && mOriginal.getContent().equals(mFirstPwd.getContent())) {
			mSecondPwd.setError(getString(R.string.password_modify_error));
			return false;
		}
		if (!mFirstPwd.getContent().equals(mSecondPwd.getContent())) {
			mSecondPwd.setError(getResources().getString(R.string.modify_confirm_error));
			return false;
		}
		Pattern pattern = Pattern.compile(REGEX_PASSWORD);
		Matcher matcher = pattern.matcher(mSecondPwd.getContent());
		if (!matcher.matches()) {
			mSecondPwd.setError(getResources().getString(R.string.modify_password_error));
			return false;
		}
		return true;
	}

	private boolean checkNull(ModifyPasswordEiditext eiditext) {
		if (TextUtils.isEmpty(eiditext.getContent())) {
			eiditext.setError(getString(R.string.mokey_must_input_hint));
			return true;
		}
		return false;
	}

	private void toModify() {
		LoadingHint.show(this);
		String oldPw = CommonUtil.getMD5(mOriginal.getContent());
		String newPw = CommonUtil.getMD5(mSecondPwd.getContent());
		FEHttpClient.getInstance().post(new MokeyModifyFePwRequest(oldPw, newPw), new ResponseCallback<ResponseContent>() {
			@Override
			public void onCompleted(ResponseContent responseContent) {
				if (responseContent.getErrorCode().equals("0")) {
					FEToast.showMessage(R.string.mokey_modify_pwd_success);
					LoadingHint.hide();
					finish();
				}
				else {
					onFailure(null);
				}
			}

			@Override
			public void onFailure(RepositoryException repositoryException) {
				super.onFailure(repositoryException);
				FEToast.showMessage(R.string.mokey_modify_pwd_error);
				LoadingHint.hide();
			}
		});
	}

	private void returnPwd() {
		Intent intent = new Intent();
		intent.putExtra("result", mSecondPwd.getContent());
		setResult(RESULT_OK, intent);
		finish();
	}

}
