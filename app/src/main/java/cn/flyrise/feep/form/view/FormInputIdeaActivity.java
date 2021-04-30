//
// feep
//
// Created by ZhongYJ on 2012-02-10.
// Copyright 2011 flyrise. All rights reserved.
//

package cn.flyrise.feep.form.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.utility.DataStack;
import cn.flyrise.feep.commonality.EditableActivity;
import cn.flyrise.feep.commonality.manager.XunFeiVoiceInput;
import cn.flyrise.feep.commonality.util.CachePath;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseEditableActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.base.views.UISwitchButton;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.X;
import cn.flyrise.feep.core.common.X.FormRequestType;
import cn.flyrise.feep.core.common.utils.FileUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import cn.flyrise.feep.event.EventMessageDisposeSuccess;
import cn.flyrise.feep.form.FormAddsignActivity;
import cn.flyrise.feep.form.been.FormSendDo;
import cn.flyrise.feep.form.contract.FormInputContract;
import cn.flyrise.feep.form.presenter.FormInputPresenter;
import cn.flyrise.feep.form.widget.handWritting.FEWrittingCombo;
import cn.flyrise.feep.main.message.task.TaskMessageAdapter;
import cn.flyrise.feep.utils.Patches;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;

public class FormInputIdeaActivity extends BaseEditableActivity implements FormInputContract.View, EditableActivity {

	private EditText mIdeaEditText;
	private Button mIdeaButton;
	private Button wordsBnt;
	private Button submitBnt;
	private TextView mTvAttachemnt;
	private TextView formInputType;
	private View checkBoxLayout;
	private UISwitchButton checkBox;
	private FEWrittingCombo mWrittingCombo;                                                        // 手写控件
	private FEToolbar mToolBar;
	private UISwitchButton isWrittingCheckBox;
	private XunFeiVoiceInput mVoiceInput;

	private boolean isWait;
	private boolean isTrace;
	private boolean isReturnCurrentNode;
	private boolean isWritting = false;
	private boolean isCanReturnCurrentNode = false;
	private boolean isEdit = false;
	private FELoadingDialog mLoadingDialog;


	private File cacheFile;
	private FormInputContract.Presenter mPresenter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
		setContentView(R.layout.form_replymessage);
		initCheckBox();
		FileUtil.deleteFiles(CoreZygote.getPathServices().getSlateTempPath());
		FileUtil.deleteFiles(CoreZygote.getPathServices().getTempFilePath() + "/handwrittenFiles");
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		this.mToolBar = toolbar;
		toolbar.setNavigationOnClickListener(v -> {
			if (isHasWrote()) showExitDialog();
			else finish();
		});
	}

	@Override
	public void bindView() {
		super.bindView();
		mIdeaEditText = this.findViewById(R.id.content_voice_input_edit);
		mTvAttachemnt = this.findViewById(R.id.imagetextbuton_attac);
		mIdeaEditText.setHint(getResources().getString(R.string.form_input_idea_edittext_hint));
		mIdeaEditText.setEms(10);
		mIdeaEditText.setFreezesText(false);
		mWrittingCombo = this.findViewById(R.id.feWritingCombo);
		mIdeaButton = this.findViewById(R.id.voice_input_mic_bnt);
		checkBox = findViewById(R.id.form_idea_input_checkbox);
		checkBoxLayout = findViewById(R.id.form_idea_action_layout);
		submitBnt = findViewById(R.id.submit);
		wordsBnt = findViewById(R.id.form_dispose_word);
		isWrittingCheckBox = findViewById(R.id.form_idea_isWrittingCheckBox);
		isWrittingCheckBox.setChecked(false);
		checkBox.setChecked(false);
		formInputType = this.findViewById(R.id.form_input_type);
		final RelativeLayout isWrittingCheckBoxLayout = findViewById(R.id.form_writting_switch_layout);
		if (!checkIsWrittingSupported()) { // 检查服务器是否支持手写 不支持则隐藏手写功能开关
			isWrittingCheckBoxLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public void bindData() {
		mPresenter = new FormInputPresenter(this, this);
		mPresenter.getIntentData();
		mVoiceInput = new XunFeiVoiceInput(this);
		intentData();
		cacheFile = new File(CachePath.getCachePath(CachePath.FORM, mPresenter.getCollaborationID(), mPresenter.getRequstType()));
		initCache();
		if (FunctionManager.hasPatch(Patches.PATCH_FORM_INPUT_ATTACHMENT) && isEdit) {
			this.findViewById(R.id.attachemnt_line).setVisibility(View.VISIBLE);
			mTvAttachemnt.setVisibility(View.VISIBLE);
		}
	}

	private void intentData() {
		if (getIntent() == null) return;
		isCanReturnCurrentNode = getIntent().getBooleanExtra("isCanReturnCurrentNode", false);
		isEdit = TextUtils.equals("1", getIntent().getStringExtra("is_edit"));
	}

	//初始化选项框
	private void initCheckBox() {
		if (mPresenter.isSendDo()) {
			formInputType.setText(getString(R.string.form_input_idea_istrace));
		}
		else if (mPresenter.isReturn()) {
			if (isCanReturnCurrentNode) {
				formInputType.setText(getString(R.string.form_input_idea_isReturnCurrentNode));
			}
			else {
				checkBoxLayout.setVisibility(View.GONE);
			}
		}
		else if (mPresenter.isAddSign()) {
			formInputType.setText(getString(R.string.form_input_idea_iswait));
		}
	}

	@Override
	public void bindListener() {
		super.bindListener();
		checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
			if (mPresenter.isSendDo()) {
				isTrace = isChecked;
			}
			else if (mPresenter.isReturn()) {
				isReturnCurrentNode = isChecked;
			}
			else if (mPresenter.isAddSign()) {
				isWait = isChecked;
			}
		});
		submitBnt.setOnClickListener(v -> {
			mPresenter.sendElectSignature();
		});
		wordsBnt.setOnClickListener(v -> mPresenter.wordsDialog());

		isWrittingCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
			isWritting = !isWritting;
			if (isWritting) EnableHandwritting();
			else DisableHandWritting();
			//当选择了手写签批，暂时屏蔽掉话筒的点击事件，防止在画板删除的时候误点击到，影响体验
			if (isChecked) {
				mIdeaButton.setClickable(false);
				InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				if (inputMethodManager != null)
					inputMethodManager.hideSoftInputFromWindow(mIdeaEditText.getWindowToken(), InputMethodManager
							.HIDE_NOT_ALWAYS);
			}
			else {
				mIdeaButton.setClickable(true);
			}
		});
		// 内容语音输入按钮
		mIdeaButton.setOnClickListener(v -> {
			FePermissions.with(FormInputIdeaActivity.this)
					.permissions(new String[]{android.Manifest.permission.RECORD_AUDIO})
					.rationaleMessage(getResources().getString(R.string.permission_rationale_record))
					.requestCode(PermissionCode.RECORD)
					.request();
			mIdeaEditText.requestFocus();
		});
		// 语音输入识别结果监听事件
		mVoiceInput.setOnRecognizerDialogListener(text ->
				XunFeiVoiceInput.setVoiceInputText(mIdeaEditText, text, mIdeaEditText.getSelectionStart()));

		mTvAttachemnt.setOnClickListener(v -> mPresenter.selectedAttachment());
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

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mPresenter.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mWrittingCombo != null) {
			mWrittingCombo.recycleAllBitmaps();
			mWrittingCombo.removeAllViews();
			mWrittingCombo = null;
		}
		DataStack.getInstance().remove(FormAddsignActivity.PERSONKEY);
		EventBus.getDefault().unregister(this);
	}

	private void DisableHandWritting() {
		mWrittingCombo.setVisibility(View.GONE);
		mIdeaEditText.setVisibility(View.VISIBLE);
		wordsBnt.setVisibility(View.VISIBLE);
		mIdeaButton.setVisibility(View.VISIBLE);
	}

	private void EnableHandwritting() {
		mIdeaEditText.setVisibility(View.GONE);
		mIdeaButton.setVisibility(View.GONE);
		mWrittingCombo.setVisibility(View.VISIBLE);
		wordsBnt.setVisibility(View.GONE);
	}

	private boolean checkIsWrittingSupported() {
		return FunctionManager.hasModule(20);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mVoiceInput != null) mVoiceInput.dismiss();
	}

	//判断用户是否在此页面填写过东西
	private boolean isHasWrote() {
		return !TextUtils.isEmpty(mIdeaEditText.getText().toString());
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
	public int initCache() {
		if (cacheFile.exists()) {
			String json = FileUtil.readAll(cacheFile);
			FormSendDo sendDo = new Gson().fromJson(json, FormSendDo.class);
			if (mPresenter.getRequstType() == FormRequestType.SendDo) {
				checkBox.setChecked(sendDo.isTrace);
			}
			else if (mPresenter.getRequstType() == FormRequestType.Additional) {
				checkBox.setChecked(sendDo.isWait);
			}
			else if (mPresenter.getRequstType() == FormRequestType.Return) {
				checkBox.setChecked(sendDo.isReturn);
			}
			mIdeaEditText.setText(sendDo.content);
			mIdeaEditText.setSelection(sendDo.content.length());
			FEToast.showMessage("已恢复上次编辑状态");
			cacheFile.delete();
		}
		return -1;
	}

	@Override
	public void saveCache() {
		FormSendDo sendDo = new FormSendDo();
		sendDo.content = mIdeaEditText.getText().toString();
		sendDo.isTrace = isTrace;
		sendDo.isWait = isWait;
		sendDo.isReturn = isReturnCurrentNode;

		if (TextUtils.isEmpty(sendDo.content)) return;

		if (cacheFile.exists()) cacheFile.delete();
		try {
			cacheFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String json = new Gson().toJson(sendDo);
		FileUtil.writeData(cacheFile, json);
	}

	@Override
	public void showExitDialog() {
		new FEMaterialDialog.Builder(this)
				.setTitle(null)
				.setMessage(getString(cn.flyrise.feep.core.R.string.exit_edit_tig))
				.setPositiveButton(null, dialog -> {
					finish();
					if (cacheFile.exists()) cacheFile.delete();//删除缓存文件
				})
				.setNegativeButton(null, null)
				.build()
				.show();
	}

	@Override
	public void setToolarTitle(int requestType) {
		if (requestType == X.FormRequestType.Additional) {
			this.mToolBar.setTitle(getResources().getString(R.string.form_titleadd));
		}
		else if (requestType == X.FormRequestType.SendDo) {
			this.mToolBar.setTitle(getResources().getString(R.string.form_dispose));
		}
		else if (requestType == X.FormRequestType.Return) {
			this.mToolBar.setTitle(getResources().getString(R.string.form_return));
		}
	}

	@Override
	public boolean isWritting() {
		return isWritting;
	}

	@Override public boolean isWait() {
		return isWait;
	}

	@Override public boolean isTrace() {
		return isTrace;
	}

	@Override public boolean isReturnCurrentNode() {
		return isReturnCurrentNode;
	}

	@Override
	public void setIdeaEditText(String text) {
		mIdeaEditText.setText(text);
	}

	@Override
	public String getIdeaEditText() {
		return mIdeaEditText.getText().toString().trim();
	}

	@Override
	public void saveWrittingBitmap(String path, String bitmapName) {
		mWrittingCombo.saveBitmapToFile(path, bitmapName);
	}

	@Override
	public void setAttachmentTitle(String text) {
		mTvAttachemnt.setText(text);
	}

	@Override
	public void showLoading() {
		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}
		mLoadingDialog = new FELoadingDialog.Builder(this)
				.setLoadingLabel("发送中...")
				.setCancelable(true)
				.setOnDismissListener(this::finish)
				.create();
		mLoadingDialog.show();
	}

	@Override
	public void hideLoading() {
		if (mLoadingDialog != null) {
			mLoadingDialog.removeDismissListener();
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void eventRefreshList(EventMessageDisposeSuccess disposeSuccess) {
		hideLoading();
	}


}
