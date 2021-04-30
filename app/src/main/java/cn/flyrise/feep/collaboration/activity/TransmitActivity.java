package cn.flyrise.feep.collaboration.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.model.Collaboration;
import cn.flyrise.feep.collaboration.presenter.NewCollaborationView;
import cn.flyrise.feep.collaboration.presenter.TransmitPresenter;
import cn.flyrise.feep.commonality.manager.XunFeiVoiceInput;
import cn.flyrise.feep.core.base.component.BaseEditableActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.base.views.UISwitchButton;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import cn.flyrise.feep.utils.Patches;
import com.jakewharton.rxbinding.view.RxView;
import java.util.concurrent.TimeUnit;

/**
 * Created by klc on 2017/5/2.
 * 协同转发
 */

public class TransmitActivity extends BaseEditableActivity implements NewCollaborationView {


	/**
	 * 回复意见允许最大字数500
	 */
	private final static int numContentMax = 500;

	private TextView mTvTitle;
	private EditText mEtContent;
	private TextView tvNum;
	private Button mBtnVoice;
	private TextView mTvFlow;
	private TextView mTvAttachment;
	private TextView tvMatter;
	private UISwitchButton mbtTransmit;
	private Button mBtnConfirm;
	private FELoadingDialog mLoadingDialog;
	private TransmitPresenter mPresenter;
	private XunFeiVoiceInput mVoiceInput;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		supportRequestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY);
		setContentView(R.layout.collaboration_transmitoption);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		super.toolBar(toolbar);
		toolbar.setTitle(R.string.transmit);
		toolbar.setNavigationOnClickListener(v -> {
			if (isHasWrote()) {
				showExitDialog();
			}
			else {
				finish();
			}
		});
	}

	@Override
	public void bindView() {
		super.bindView();
		mEtContent = (EditText) findViewById(R.id.etContent);
		mBtnVoice = (Button) findViewById(R.id.btVoice);
		mTvTitle = (TextView) findViewById(R.id.tv_title);
		mTvFlow = (TextView) findViewById(R.id.tv_flow);
		mTvAttachment = (TextView) findViewById(R.id.tv_attachment);
		tvMatter = (TextView) findViewById(R.id.tv_association);
		mbtTransmit = (UISwitchButton) findViewById(R.id.btTransmit);
		mBtnConfirm = (Button) findViewById(R.id.btConfirm);
		mbtTransmit.setChecked(false);
		if (FunctionManager.hasPatch(Patches.PATCH_RELATED_MATTERS)) {
			findViewById(R.id.lv_association).setVisibility(View.VISIBLE);
		}
		tvNum = (TextView) findViewById(R.id.content_num);
		tvNum.setText(String.format(getResources().getString(R.string.words_can_input), numContentMax));
	}

	@Override
	public void bindData() {
		super.bindData();
		mVoiceInput = new XunFeiVoiceInput(this);
		mPresenter = new TransmitPresenter(this, false);
		mPresenter.loadData(getIntent());
	}

	@Override
	public void bindListener() {
		super.bindListener();
		mBtnVoice.setOnClickListener(v -> {
			requestAudioPermission();
			mEtContent.requestFocus();
		});
		mTvFlow.setOnClickListener(v -> mPresenter.flowClick(TransmitActivity.this, WorkFlowActivity.COLLABORATION_TRANSMIT));
		mTvAttachment.setOnClickListener(v -> mPresenter.attachmentClick(TransmitActivity.this));
		tvMatter.setOnClickListener(v -> mPresenter.associationClick(TransmitActivity.this));

		RxView.clicks(mBtnConfirm)
				.throttleFirst(1, TimeUnit.SECONDS)
				.subscribe(a -> {
					if (mPresenter.hasFlow()) {
						getViewValue();
						mPresenter.transmitCollaboration(TransmitActivity.this);
						return;
					}
					FEToast.showMessage(getString(R.string.collaboration_add_flow));
					mPresenter.flowClick(TransmitActivity.this, WorkFlowActivity.COLLABORATION_TRANSMIT);
				}, exception -> exception.printStackTrace());

		mEtContent.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Editable editable = mEtContent.getText();
				int len = editable.length();
				if (len <= numContentMax) {
					tvNum.setText(String.format(getResources().getString(R.string.words_can_input), (numContentMax - len)));
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		mVoiceInput.setOnRecognizerDialogListener(text -> {
			XunFeiVoiceInput.setVoiceInputText(mEtContent, text, mEtContent.getSelectionStart());
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mPresenter.onActivityResult(requestCode, resultCode, data);
	}

	private void requestAudioPermission() {
		FePermissions.with(this)
				.permissions(new String[]{android.Manifest.permission.RECORD_AUDIO})
				.rationaleMessage(getResources().getString(R.string.permission_rationale_record))
				.requestCode(PermissionCode.RECORD)
				.request();
	}

	@PermissionGranted(PermissionCode.RECORD)
	public void onRecordPermissionGranted() {
		if (mVoiceInput != null) mVoiceInput.show();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
	}


	private void getViewValue() {
		Collaboration collaboration = mPresenter.getCollaboration();
		collaboration.isChangeIdea = mbtTransmit.isChecked() ? "1" : "0";
		collaboration.setOption(mEtContent.getText().toString().trim());
	}

	public void showLoading() {
		hideLoading();
		mLoadingDialog = new FELoadingDialog.Builder(TransmitActivity.this)
				.setCancelable(false)
				.create();
		mLoadingDialog.show();
	}

	@Override
	public void showProgress(int progress) {
		if (mLoadingDialog != null) {
			mLoadingDialog.updateProgress(progress);
		}
	}

	@Override
	public void hideLoading() {
		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
		}
	}

	@Override
	public void displayView(Collaboration collaboration) {
		if (!TextUtils.isEmpty(collaboration.title)) {
			mTvTitle.setText(getString(R.string.title_hint) + collaboration.title);
		}
	}

	@Override
	public void setFileTextCount(int count) {
		mTvAttachment.setText(count == 0 ? getString(R.string.collaboration_attachment)
				: String.format(getString(R.string.collaboration_has_attachment), count));
	}

	@Override
	public void setHasFlow(boolean hasFlow) {
		mTvFlow.setText(!hasFlow ? R.string.collaboration_flow_not : R.string.collaboration_flow_yes);
	}

	@Override
	public void setAssociationCount(int count) {
		tvMatter.setText(count == 0 ? getString(R.string.collaboration_matters)
				: String.format(getString(R.string.collaboration_has_matters), count));
	}

	@Override
	public void setImportValue(String value) {

	}

	@Override
	public void hideSaveButton() {
	}

	@Override
	public void showImportDialog(String[] value) {

	}

	/**
	 * 判断用户是否在此页面填写过东西
	 */
	private boolean isHasWrote() {
		final String titleText = mTvTitle.getText().toString();
		return !TextUtils.isEmpty(titleText) || mPresenter.hasFlow() || mPresenter.hasFile();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isHasWrote()) {
				showExitDialog();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
		}
		if (mVoiceInput != null) mVoiceInput.dismiss();
	}
}
