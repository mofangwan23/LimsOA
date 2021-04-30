package cn.flyrise.feep.form;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import cn.flyrise.android.protocol.entity.BooleanResponse;
import cn.flyrise.android.protocol.entity.SendReaderRequest;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.utils.ContactsIntent;
import cn.flyrise.feep.commonality.manager.XunFeiVoiceInput;
import cn.flyrise.feep.core.base.component.BaseEditableActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.base.views.UISwitchButton;
import cn.flyrise.feep.core.common.DataKeeper;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import cn.flyrise.feep.core.services.model.AddressBook;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by klc on 2017/5/8.
 * 表单传阅界面
 */
public class FormCirculateActivity extends BaseEditableActivity {

	public final static int PERSON_REQUEST_CODE = 100;

	/**
	 * 意见允许最大字数
	 */
	private final static int numTitleMax = 2000;

	private EditText mEtContent;
	private TextView mTvContentNum;
	private Button mBtnVoice;
	private TextView mTvPerson;
	private UISwitchButton mBtMobileRemind;
//	private UISwitchButton mBtEmailRemind;

	private Button mBtnConfirm;
	private FELoadingDialog mLoadingDialog;
	private List<AddressBook> receiverPersons;
	private String formId;
	private XunFeiVoiceInput mVoiceInput;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.form_circulate);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		super.toolBar(toolbar);
		toolbar.setTitle(R.string.circulate);
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
		mTvContentNum = (TextView) findViewById(R.id.title_num);
		mBtnVoice = (Button) findViewById(R.id.btVoice);
		mTvPerson = (TextView) findViewById(R.id.tv_person);
		mBtnConfirm = (Button) findViewById(R.id.btConfirm);
		mBtMobileRemind = (UISwitchButton) findViewById(R.id.btMobileRemind);
//		mBtEmailRemind = (UISwitchButton) findViewById(R.id.btEmailRemind);
		mBtMobileRemind.setChecked(false);
//		mBtEmailRemind.setChecked(false);
		mTvContentNum.setText(String.format(getResources().getString(R.string.words_can_input), numTitleMax));
	}

	@Override
	public void bindData() {
		super.bindData();
		mVoiceInput = new XunFeiVoiceInput(this);
		formId = getIntent().getStringExtra(K.form.EXTRA_ID);
		this.receiverPersons = new ArrayList<>();
	}

	@Override
	public void bindListener() {
		super.bindListener();
		mBtnVoice.setOnClickListener(v -> {
			requestAudioPermission();
			mEtContent.requestFocus();
		});
		mTvPerson.setOnClickListener(v -> addFlow(receiverPersons, PERSON_REQUEST_CODE));
		mBtnConfirm.setOnClickListener(v -> doConfirm());
		mEtContent.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Editable editable = mEtContent.getText();
				int len = editable.length();
				if (len <= numTitleMax) {
					mTvContentNum.setText(String.format(getResources().getString(R.string.words_can_input), numTitleMax - len));
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

	private boolean isHasWrote() {
		final String contentText = mEtContent.getText().toString();
		return !TextUtils.isEmpty(contentText) || !CommonUtil.isEmptyList(receiverPersons);
	}

	private void addFlow(List<AddressBook> persons, int requestCode) {
		if (CommonUtil.nonEmptyList(persons)) {
			DataKeeper.getInstance().keepDatas(requestCode, persons);
		}
		new ContactsIntent(FormCirculateActivity.this)
				.targetHashCode(requestCode)
				.requestCode(requestCode)
				.userCompanyOnly()
				.title(getString(R.string.circulate_select_person))
				.withSelect()
				.open();
	}


	public void showLoading() {
		hideLoading();
		mLoadingDialog = new FELoadingDialog.Builder(FormCirculateActivity.this)
				.setCancelable(false)
				.create();
		mLoadingDialog.show();
	}

	public void hideLoading() {
		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PERSON_REQUEST_CODE) {     // 主送人
			receiverPersons = (List<AddressBook>) DataKeeper.getInstance().getKeepDatas(PERSON_REQUEST_CODE);
			mTvPerson.setText(CommonUtil.isEmptyList(receiverPersons) ? getString(R.string.circulate_person)
					: String.format(getString(R.string.circulate_person_has_select), receiverPersons.size()));
		}
	}

	@Override
	public void finish() {
		super.finish();
		DataKeeper.getInstance().removeKeepData(PERSON_REQUEST_CODE);
		if (receiverPersons != null) {
			receiverPersons.clear();
		}
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

	private void doConfirm() {
		if (CommonUtil.isEmptyList(receiverPersons)) {
			FEToast.showMessage(getString(R.string.circulate_select_person_hint));
			addFlow(receiverPersons, PERSON_REQUEST_CODE);
			return;
		}
		showLoading();
		StringBuilder receivers = new StringBuilder();
		for (AddressBook addressBook : receiverPersons) {
			receivers.append(addressBook.userId).append(",");
		}
		receivers.deleteCharAt(receivers.length() - 1);
//		SendReaderRequest request = new SendReaderRequest(receivers.toString(), formId,
//				mEtContent.getText().toString(),
//				mBtEmailRemind.isChecked() ? "1" : "0", mBtMobileRemind.isChecked() ? "1" : "0");
		SendReaderRequest request = new SendReaderRequest(receivers.toString(), formId,
				mEtContent.getText().toString(),
				"0", mBtMobileRemind.isChecked() ? "1" : "0");
		FEHttpClient.getInstance().post(request, new ResponseCallback<BooleanResponse>() {
			@Override
			public void onCompleted(BooleanResponse response) {
				if ("0".equals(response.getErrorCode()) && response.isSuccess) {
					FEToast.showMessage(getString(R.string.circulate_send_success));
					hideLoading();
					finish();
				}
				else {
					onFailure(null);
				}
			}

			@Override
			public void onFailure(RepositoryException repositoryException) {
				super.onFailure(repositoryException);
				FEToast.showMessage(getString(R.string.circulate_send_error));
				hideLoading();
			}
		});
	}
}
