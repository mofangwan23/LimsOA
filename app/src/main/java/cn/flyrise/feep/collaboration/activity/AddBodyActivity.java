package cn.flyrise.feep.collaboration.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import cn.flyrise.android.protocol.entity.AssociationSendRequest;
import cn.flyrise.android.protocol.entity.CollaborationAddBodyRequest;
import cn.flyrise.android.protocol.entity.FormAddBodyRequest;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.matter.MatterListActivity;
import cn.flyrise.feep.collaboration.matter.model.Matter;
import cn.flyrise.feep.commonality.manager.XunFeiVoiceInput;
import cn.flyrise.feep.core.base.component.BaseEditableActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.X.CollaborationType;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.listener.OnProgressUpdateListenerImpl;
import cn.flyrise.feep.core.network.request.FileRequest;
import cn.flyrise.feep.core.network.request.FileRequestContent;
import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.core.network.uploader.UploadManager;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import cn.flyrise.feep.media.common.LuBan7;
import cn.flyrise.feep.utils.Patches;
import com.jakewharton.rxbinding.view.RxView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 补充正文界面
 * Created by klc on 2017/5/4.
 */
public class AddBodyActivity extends BaseEditableActivity {

	/**
	 * 添加附件请求码
	 */
	private final static int ADD_ATTACHMENT_REQUEST_CODE = 100;

	/**
	 * 补充事项
	 */
	private final static int ADD_MATTER_LIST = 101;

	/**
	 * 补充正文允许最大字数500
	 */
	private final static int numContentMax = 250;

	private EditText mEtContent;
	private Button mBtnVoice;
	private TextView tvNum;
	private TextView mTvAttachment;
	private TextView mTvAssociation;
	private FELoadingDialog mLoadingDialog;
	private Button mBtnConfirm;

	private List<Matter> mSelectedAssociation;     // 选种的补充事项
	private String mId;
	private int type; //来自表单或者是协同
	private String associationID;
	private String attachmentID;
	private XunFeiVoiceInput mVoiceInput;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.collaboration_addbody);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		super.toolBar(toolbar);
		toolbar.setTitle(R.string.add_body);
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
		mTvAssociation = (TextView) findViewById(R.id.tv_associate);
		mTvAttachment = (TextView) findViewById(R.id.tv_attachment);
		mBtnConfirm = (Button) findViewById(R.id.submit);
		mBtnVoice = (Button) findViewById(R.id.btVoice);
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
		mId = getIntent().getStringExtra(K.collaboration.Extra_Collaboration_ID);
		type = getIntent().getIntExtra("type", -1);
	}

	@Override
	public void bindListener() {
		super.bindListener();
		mBtnVoice.setOnClickListener(v -> {
			requestAudioPermission();
			mEtContent.requestFocus();
		});
		RxView.clicks(mBtnConfirm)
				.throttleFirst(1, TimeUnit.SECONDS)
				.subscribe(a -> checkSend(), exception -> exception.printStackTrace());

		mTvAttachment.setOnClickListener(
				v -> LuBan7.pufferGrenades(AddBodyActivity.this, mSelectedAttachments, null, ADD_ATTACHMENT_REQUEST_CODE));
		mTvAssociation.setOnClickListener(v -> {
			Intent intent = new Intent(AddBodyActivity.this, MatterListActivity.class);
			if (CommonUtil.nonEmptyList(mSelectedAssociation)) {
				intent.putExtra("selectedAssociation", mSelectedAssociation.toArray(new Matter[]{}));
			}
			startActivityForResult(intent, ADD_MATTER_LIST);
		});
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
		mVoiceInput.setOnRecognizerDialogListener(
				text -> XunFeiVoiceInput.setVoiceInputText(mEtContent, text, mEtContent.getSelectionStart()));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == ADD_ATTACHMENT_REQUEST_CODE) {
			if (data != null) {
				ArrayList<String> localAttachments = data.getStringArrayListExtra("extra_local_file");
				addSelectedAttachments(localAttachments);
				setFileTextCount(getSelectedAttachmentCount());
			}
		}
		else if (requestCode == ADD_MATTER_LIST) {
			if (data != null) {
				Parcelable[] parcelables = data.getParcelableArrayExtra("selectedAssociation");
				Matter[] associations = Arrays.copyOf(parcelables, parcelables.length, Matter[].class);
				mSelectedAssociation = Arrays.asList(associations);
				setAssociationCount(mSelectedAssociation.size());
			}
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

	public void showLoading() {
		hideLoading();
		mLoadingDialog = new FELoadingDialog.Builder(AddBodyActivity.this)
				.setCancelable(false)
				.create();
		mLoadingDialog.show();
	}

	public void showProgress(int progress) {
		if (mLoadingDialog != null) {
			mLoadingDialog.updateProgress(progress);
		}
	}

	public void hideLoading() {
		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}
	}

	public void setFileTextCount(int count) {
		mTvAttachment.setText(count == 0 ? getString(R.string.collaboration_attachment)
				: String.format(getString(R.string.collaboration_has_attachment), count));
	}

	public void setAssociationCount(int count) {
		mTvAssociation.setText(count == 0 ? getString(R.string.collaboration_matters)
				: String.format(getString(R.string.collaboration_has_matters), count));
	}

	private void toAddBody() {
		String content = mEtContent.getText().toString();
		attachmentID = UUID.randomUUID().toString();
		FileRequestContent filerequestcontent = new FileRequestContent();
		filerequestcontent.setAttachmentGUID(attachmentID);
		filerequestcontent.setFiles(CommonUtil.isEmptyList(mSelectedAttachments)
				? new ArrayList<>()
				: mSelectedAttachments);
		FileRequest fileRequest = new FileRequest();
		fileRequest.setFileContent(filerequestcontent);
		if (type == 0) {
			CollaborationAddBodyRequest requestContent = new CollaborationAddBodyRequest();
			requestContent.setId(mId);
			requestContent.setContent(content);
			requestContent.setRequestType(CollaborationType.AddBody + "");
			requestContent.setRelationFlow(associationID);
			requestContent.setAttachmentGUID(attachmentID);
			fileRequest.setRequestContent(requestContent);
		}
		else {
			FormAddBodyRequest requestContent = new FormAddBodyRequest();
			requestContent.setTaskId(mId);
			requestContent.setIdea(content);
			requestContent.setRelationFlow(associationID);
			requestContent.setAttachment(attachmentID);
			fileRequest.setRequestContent(requestContent);
		}
		new UploadManager(this)
				.fileRequest(fileRequest)
				.progressUpdateListener(new OnProgressUpdateListenerImpl() {
					@Override
					public void onPreExecute() {
						super.onPreExecute();
						showLoading();
					}

					@Override
					public void onProgressUpdate(long currentBytes, long contentLength, boolean done) {
						int progress = (int) (currentBytes * 100 / contentLength * 1.0F);
						showProgress(progress);
					}
				})
				.responseCallback(new ResponseCallback<ResponseContent>() {
					@Override
					public void onCompleted(ResponseContent responseContent) {
						hideLoading();
						final String errorCode = responseContent.getErrorCode();
						final String errorMessage = responseContent.getErrorMessage();
						if (!TextUtils.equals("0", errorCode)) {
							FEToast.showMessage(errorMessage);
							return;
						}
						FEToast.showMessage(R.string.message_operation_alert);
						setResult(RESULT_OK);
						finish();
					}

					@Override
					public void onFailure(RepositoryException repositoryException) {
						hideLoading();
					}
				})
				.execute();
	}

	private void uploadAssociation() {
		showLoading();
		associationID = UUID.randomUUID().toString();
		List<AssociationSendRequest.SendAssociation> sendList = new ArrayList<>();
		for (Matter association : mSelectedAssociation) {
			sendList.add(
					new AssociationSendRequest.SendAssociation(association.title, String.valueOf(association.matterType), association.id));
		}
		AssociationSendRequest request = new AssociationSendRequest(associationID, new AssociationSendRequest.Relationflow(sendList));
		FEHttpClient.getInstance().post(request, new ResponseCallback<ResponseContent>() {
			@Override
			public void onCompleted(ResponseContent response) {
				if (response.getErrorCode().equals(ResponseContent.OK_CODE)) {
					toAddBody();
				}
				else {
					onFailure(null);
				}
			}

			@Override
			public void onFailure(RepositoryException repositoryException) {
				super.onFailure(repositoryException);
				hideLoading();
			}
		});
	}

	private void checkSend() {
		if (TextUtils.isEmpty(mEtContent.getText().toString().trim())) {
			FEToast.showMessage(getString(R.string.addboy_input_content));
			return;
		}
		if (!CommonUtil.isEmptyList(mSelectedAssociation)) {
			uploadAssociation();
		}
		else {
			toAddBody();
		}
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

	/**
	 * 判断用户是否在此页面填写过东西
	 */
	private boolean isHasWrote() {
		String contentText = mEtContent.getText().toString();
		return !TextUtils.isEmpty(contentText)
				|| !CommonUtil.isEmptyList(mSelectedAssociation)
				|| getSelectedAttachmentCount() > 0;
	}

	// 本地附件相关

	private List<String> mSelectedAttachments;

	public void addSelectedAttachments(List<String> selectedAttachments) {
		if (mSelectedAttachments == null) {
			mSelectedAttachments = new ArrayList<>();
		}
		mSelectedAttachments.clear();
		mSelectedAttachments.addAll(selectedAttachments);
	}

	public int getSelectedAttachmentCount() {
		return CommonUtil.isEmptyList(mSelectedAttachments) ? 0 : mSelectedAttachments.size();
	}
}
