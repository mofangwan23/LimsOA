package cn.flyrise.feep.mobilekey;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.dialog.FEMaterialEditTextDialog.Builder;
import cn.flyrise.feep.core.services.IMobileKeyService;

/**
 * Created by klc on 2018/3/16.
 * 手机盾设置界面
 */

public class MoKeySettingActivity extends BaseActivity implements MokeySettingContract.IView {

	private RelativeLayout mRlModifyFePwd;
	private RelativeLayout mRlActive;
	private RelativeLayout mRlChangePw;
	private RelativeLayout mRlReset;
	private Button mBtLogout;

	private MokeySettingContract.IPresenter mPresenter;
	private final int CREATEPWD_CODE = 10001;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mobilekey_setting);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		super.toolBar(toolbar);
		toolbar.setTitle(R.string.mokey_setting);
		toolbar.setNavigationIcon(R.drawable.back_left_icon);
		toolbar.setNavigationOnClickListener(v -> finish());
	}

	@Override
	public void bindView() {
		super.bindView();
		mRlModifyFePwd = (RelativeLayout) findViewById(R.id.rlMoidfyFePw);
		mRlActive = (RelativeLayout) findViewById(R.id.rlActive);
		mRlChangePw = (RelativeLayout) findViewById(R.id.rlChangePw);
		mRlReset = (RelativeLayout) findViewById(R.id.rlReset);
		mBtLogout = (Button) findViewById(R.id.btLogout);
	}

	@Override
	public void bindData() {
		super.bindData();
		this.mPresenter = new MokeySettingPresenter(this);
		initLayout();
	}

	@Override
	public void bindListener() {
		super.bindListener();
		mRlModifyFePwd.setOnClickListener(v -> startActivity(new Intent(getContext(), MokeyModifyPwActivity.class)));
		mRlChangePw.setOnClickListener(v -> mPresenter.modifyPwd());
		mRlActive.setOnClickListener(v -> {
			Intent intent = new Intent(getContext(), MokeyModifyPwActivity.class);
			intent.putExtra("type", MokeyModifyPwActivity.TYPE_CREATE);
			startActivityForResult(intent, CREATEPWD_CODE);
		});
		mRlReset.setOnClickListener(v -> new Builder(this).setHint(getString(R.string.mokey_input_safepwd))
				.setPositiveButton(null, (dialog, input, check) -> mPresenter.reset(input))
				.setNegativeButton(null, null)
				.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
				.build()
				.show());
		mBtLogout.setOnClickListener(v -> new Builder(this).setHint(getString(R.string.mokey_input_safepwd))
				.setPositiveButton(null, (dialog, input, check) -> mPresenter.logout(input))
				.setNegativeButton(null, null)
				.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
				.build()
				.show());
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CREATEPWD_CODE && resultCode == RESULT_OK) {
			String pwd = data.getStringExtra("result");
			mPresenter.active(pwd);
		}
	}

	@Override
	public Context getContext() {
		return this;
	}

	@Override
	public void initLayout() {
		IMobileKeyService mobileKeyService = CoreZygote.getMobileKeyService();
		if (mobileKeyService.isNormal()) {
			mRlActive.setVisibility(View.GONE);
			mRlReset.setVisibility(View.VISIBLE);
			mRlChangePw.setVisibility(View.VISIBLE);
			mRlModifyFePwd.setVisibility(View.VISIBLE);
			mBtLogout.setVisibility(View.VISIBLE);
		}
		else if (mobileKeyService.isActivate()) {
			mRlReset.setVisibility(View.VISIBLE);
			mRlModifyFePwd.setVisibility(View.VISIBLE);
			mBtLogout.setVisibility(View.VISIBLE);
			mRlActive.setVisibility(View.GONE);
			mRlChangePw.setVisibility(View.GONE);
		}
		else {
			mRlActive.setVisibility(View.VISIBLE);
			mRlReset.setVisibility(View.GONE);
			mRlChangePw.setVisibility(View.GONE);
			mRlModifyFePwd.setVisibility(View.GONE);
			mBtLogout.setVisibility(View.GONE);
		}
	}


	@Override
	public void showMsg(int strId) {
		FEToast.showMessage(strId);
	}

}
