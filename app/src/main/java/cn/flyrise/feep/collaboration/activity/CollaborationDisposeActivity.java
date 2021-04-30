//
// CollaborationDisposeActivity.java
// feep
//
// Created by ZhongYJ on 2012-02-17.
// Copyright 2011 flyrise. All rights reserved.
//

package cn.flyrise.feep.collaboration.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.protocol.entity.CollaborationSendDoRequest;
import cn.flyrise.android.protocol.entity.ReferenceItemsRequest;
import cn.flyrise.android.protocol.entity.ReferenceItemsResponse;
import cn.flyrise.android.protocol.model.Flow;
import cn.flyrise.android.protocol.model.ReferenceItem;
import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.FEApplication;
import cn.flyrise.feep.FEMainActivity;
import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.model.CollaborationHoldData;
import cn.flyrise.feep.commonality.CommonWordsActivity;
import cn.flyrise.feep.commonality.EditableActivity;
import cn.flyrise.feep.commonality.manager.XunFeiVoiceInput;
import cn.flyrise.feep.commonality.util.CachePath;
import cn.flyrise.feep.core.base.component.BaseEditableActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.base.views.UISwitchButton;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.X.CollaborationType;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.FileUtil;
import cn.flyrise.feep.core.common.utils.LanguageManager;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
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
import cn.flyrise.feep.event.EventMessageDisposeSuccess;
import cn.flyrise.feep.form.util.FormDataProvider;
import cn.flyrise.feep.media.common.LuBan7;
import cn.flyrise.feep.utils.Patches;
import com.google.gson.Gson;
import com.jakewharton.rxbinding.view.RxView;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.greenrobot.eventbus.EventBus;

public class CollaborationDisposeActivity extends BaseEditableActivity implements EditableActivity {

	private final int FlOW_REQUEST_CODE = 201;         //跳转选人
	private final int ATTACHMENT_REQUEST_CODE = 202;   //添加附件

	private FEToolbar mToolBar;
	private EditText mEtOpinion;
	private Button mBtMic;
	private Button mBtCommonLanguage;
	private TextView mTvAttachment;
	private TextView mTvFlow;
	private UISwitchButton mSwbHideOpinion;
	private UISwitchButton mSwbTrace;

	//2018-5-4 加的加签等待 for FE7.0
	private UISwitchButton mSwbWait;
	//2018-3-8协同退回增加【退回到发起人】、【重新提交后直接返回本节点】
	private UISwitchButton mSwbReturnToStartNode;
	private UISwitchButton mSwbReturnToThisNode;
	private Button mBtSubmit;

	private boolean mFlowIsModify;
	private static Flow mFlow;
	private static String mCurrentFlowNodeGUID;
	private String mCollaborationID;
	private int requestType;
	private File cacheFile;
	private List<String> mSelectedLocalAttachments;     // 用户已选择的本地附件
	private XunFeiVoiceInput mVoiceInput;

	/**
	 * 静态方法传值
	 */
	public static void setData(Flow flow, String currentFlowNodeGUID) {
		mFlow = flow;
		mCurrentFlowNodeGUID = currentFlowNodeGUID;
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		this.mToolBar = toolbar;
		toolbar.setNavigationOnClickListener(v -> {
			if (isHasWrote())
				showExitDialog();
			else
				finish();
		});
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.collaboration_moreaction);
		setEditViewWithLanguage();
	}

	@Override
	public void bindView() {
		super.bindView();
		mEtOpinion = findViewById(R.id.etOpinion);
		mBtMic = findViewById(R.id.btMic);
		mBtCommonLanguage = findViewById(R.id.btCommonLanguage);
		mTvAttachment = findViewById(R.id.tvAttachment);
		mTvFlow = findViewById(R.id.tvFlow);
		mTvAttachment = findViewById(R.id.tvAttachment);
		mSwbHideOpinion = findViewById(R.id.swbHideOpinion);
		mSwbTrace = findViewById(R.id.swbTrace);
		mSwbWait = findViewById(R.id.swbWait);
		mSwbReturnToStartNode = findViewById(R.id.btBackToSender);
		mSwbReturnToThisNode = findViewById(R.id.btReturnToThisNode);
		mBtSubmit = this.findViewById(R.id.submit);
		mSwbHideOpinion.setChecked(false);
		mSwbTrace.setChecked(false);
		mSwbReturnToStartNode.setChecked(false);
		mSwbReturnToThisNode.setChecked(false);
	}

	@Override
	public void bindData() {
		super.bindData();
		mVoiceInput = new XunFeiVoiceInput(this);
		getIntentData();
		if (requestType == CollaborationType.Return) {// 退回
			mToolBar.setTitle(R.string.collaboration_back);
		}
		else if (requestType == CollaborationType.Additional) {// 加签
			mToolBar.setTitle(R.string.collaboration_add);
			findViewById(R.id.lyFlow).setVisibility(View.VISIBLE);
//			findViewById(R.id.rlWait).setVisibility(View.VISIBLE);
			mTvFlow.setText(getString(R.string.collaboration_add_person_not));
			mSwbWait.setChecked(true);
		}
		else {
			mToolBar.setTitle(R.string.collaboration_deal);
		}
		cacheFile = new File(CachePath.getCachePath(CachePath.COLLABORATION, mCollaborationID, requestType));

		if (FunctionManager.hasPatch(Patches.PATH_COLLABORATION_SENDBACK) && requestType == CollaborationType.Return) {
			findViewById(R.id.rlBackToStartNode).setVisibility(View.VISIBLE);
			findViewById(R.id.rlReturnToThisNode).setVisibility(View.VISIBLE);
		}

		initCache();
	}

	/**
	 * 获取通过intent传过来的Intent
	 */
	private void getIntentData() {
		final Intent intent = getIntent();
		if (intent != null) {
			mCollaborationID = intent.getStringExtra("collaborationID");
			final int type = intent.getIntExtra("requestType", 1);
			if (type == CollaborationType.DealWith) {
				requestType = CollaborationType.DealWith;
			}
			else if (type == CollaborationType.Additional) {
				requestType = CollaborationType.Additional;
			}
			else if (type == CollaborationType.Return) {
				requestType = CollaborationType.Return;
			}
		}
	}


	@Override
	public void bindListener() {
		super.bindListener();
		mBtMic.setOnClickListener(v -> FePermissions.with(CollaborationDisposeActivity.this)
				.permissions(new String[]{android.Manifest.permission.RECORD_AUDIO})
				.rationaleMessage(getResources().getString(R.string.permission_rationale_record))
				.requestCode(PermissionCode.RECORD)
				.request());
		mBtCommonLanguage.setOnClickListener(v -> wordsDialog());
		mTvFlow.setOnClickListener(v -> chooseFlow());
		mTvAttachment.setOnClickListener(v -> LuBan7.pufferGrenades(CollaborationDisposeActivity.this,
				mSelectedLocalAttachments, null, ATTACHMENT_REQUEST_CODE));
		RxView.clicks(mBtSubmit)
				.throttleFirst(1, TimeUnit.SECONDS)
				.subscribe(a -> {
					if (requestType == CollaborationType.DealWith) {
						createSendDialog(this.getString(R.string.collaboration_really_deal));
					}
					else if (requestType == CollaborationType.Return) {
						createSendDialog(mSwbReturnToStartNode.isChecked() ?
								getString(R.string.collaboration_sure_return_to_startnode) : getString(R.string.collaboration_really_back));
					}
					else if (requestType == CollaborationType.Additional) {// 加签
						if (mFlowIsModify) {
							submit();
							return;
						}
						new FEMaterialDialog.Builder(this)
								.setTitle(null)
								.setMessage(this.getString(R.string.collaboration_add_message))
								.setPositiveButton(this.getString(R.string.dialog_button_yes), dialog -> chooseFlow())
								.setNegativeButton(this.getString(R.string.dialog_button_no), null)
								.build()
								.show();
					}
				}, Throwable::printStackTrace);
		mVoiceInput.setOnRecognizerDialogListener(text -> {
			mEtOpinion.requestFocus();
			int selection = mEtOpinion.getSelectionStart();
			XunFeiVoiceInput.setVoiceInputText(mEtOpinion, text, selection);
		});
	}

	@Override
	public int initCache() {
		if (cacheFile.exists()) {
			String json = FileUtil.readAll(cacheFile);
			CollaborationHoldData holdData = new Gson().fromJson(json, CollaborationHoldData.class);
			if (holdData != null) {
				mEtOpinion.setText(holdData.content);
				mEtOpinion.setSelection(holdData.content.length());
				mFlow = holdData.flow;
				mFlowIsModify = holdData.isAddSigned;
				setFlowChange();
				List<String> filePath = holdData.attachmentPath;
				if (!CommonUtil.isEmptyList(filePath)) {
					for (String path : filePath) {
						File file = new File(path);
						if (file.exists()) {
							addLocalAttachments(file.getPath());
						}
					}
				}
				setAttachmentSize();
				mSwbHideOpinion.setChecked(holdData.hideOpinion);
				mSwbTrace.setChecked(holdData.trace);
				mSwbWait.setChecked(holdData.isWait);
				mSwbReturnToStartNode.setChecked(holdData.isBackToStartNode);
				mSwbReturnToThisNode.setChecked(holdData.isReturnToThisNodeAfterHandle);
				FEToast.showMessage(getString(R.string.collaboration_restored_status));
			}
			cacheFile.delete();
		}
		return 0;
	}

	@Override
	public void saveCache() {
		CollaborationHoldData sendDo = new CollaborationHoldData();
		sendDo.content = mEtOpinion.getText().toString();
		if (TextUtils.isEmpty(sendDo.content) && CommonUtil.isEmptyList(mSelectedLocalAttachments) && !mFlowIsModify) {
			return;
		}
		if (CommonUtil.nonEmptyList(mSelectedLocalAttachments)) {
			sendDo.attachmentPath = new ArrayList<>();
			sendDo.attachmentPath.addAll(mSelectedLocalAttachments);
		}
		sendDo.flow = mFlow;
		sendDo.isAddSigned = mFlowIsModify;
		sendDo.hideOpinion = mSwbHideOpinion.isChecked();
		sendDo.trace = mSwbTrace.isChecked();
		sendDo.isWait = mSwbWait.isChecked();
		sendDo.isBackToStartNode = mSwbReturnToStartNode.isChecked();
		sendDo.isReturnToThisNodeAfterHandle = mSwbReturnToThisNode.isChecked();
		if (cacheFile.exists()) {
			cacheFile.delete();
		}
		try {
			cacheFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String json = new Gson().toJson(sendDo);
		FileUtil.writeData(cacheFile, json);
	}

	/**
	 * 根据当前语言设置语音输入按钮的显示与隐藏
	 */
	private void setEditViewWithLanguage() {
		mBtCommonLanguage
				.setVisibility(LanguageManager.getCurrentLanguage() == LanguageManager.LANGUAGE_TYPE_CN ?
						View.VISIBLE : View.GONE);
	}

	/**
	 * 选择流程
	 */
	private void chooseFlow() {

		final Intent intent = new Intent(CollaborationDisposeActivity.this, WorkFlowActivity.class);
		requestType = CollaborationType.Additional;
		WorkFlowActivity.setFunction(WorkFlowActivity.COLLABORATION_ADDSIGN);
		WorkFlowActivity.setInitData(mFlow, mCurrentFlowNodeGUID);
		CollaborationDisposeActivity.this.startActivityForResult(intent, FlOW_REQUEST_CODE);
	}

	/**
	 * 常用语对话框
	 */
	private void wordsDialog() {
		final FEApplication application = (FEApplication) getApplication();
		String[] commonWords = application.getCommonWords();
		if (commonWords != null) {
			showCommonWordDialog(CommonWordsActivity.convertCommonWord(commonWords));
			return;
		}

		LoadingHint.show(this);
		final ReferenceItemsRequest commonWordsReq = new ReferenceItemsRequest();
		commonWordsReq.setRequestType(ReferenceItemsRequest.TYPE_COMMON_WORDS);

		FEHttpClient.getInstance().post(commonWordsReq, new ResponseCallback<ReferenceItemsResponse>(this) {
			@Override
			public void onCompleted(ReferenceItemsResponse responseContent) {
				LoadingHint.hide();
				final List<ReferenceItem> items = responseContent.getItems();
				if ("-98".equals(responseContent.getErrorCode())) {
					application.setCommonWords(getResources().getStringArray(R.array.words));
				}
				else {
					application.setCommonWords(CommonWordsActivity.convertCommonWords(items));
				}
				showCommonWordDialog(CommonWordsActivity.convertCommonWord(application.getCommonWords()));
			}

			@Override
			public void onFailure(RepositoryException repositoryException) {
				LoadingHint.hide();
				FEToast.showMessage(getResources().getString(R.string.lbl_retry_operator));
			}
		});

	}

	private void showCommonWordDialog(final String[] commonWords) {
		new FEMaterialDialog
				.Builder(this)
				.setCancelable(true)
				.setTitle(getResources().getString(R.string.common_language))
				.setItems(commonWords, (dialog, view, position) -> {
					dialog.dismiss();
					mEtOpinion.setText(commonWords[position]);
				})
				.setPositiveButton(getResources().getString(R.string.lbl_text_edit),
						dialog -> startActivity(new Intent(CollaborationDisposeActivity.this, CommonWordsActivity.class)))
				.setNegativeButton(null, null)
				.build()
				.show();
	}

	/**
	 * 提交
	 */
	private void submit() {
		final String GUID = UUID.randomUUID().toString();
		final String content = mEtOpinion.getText().toString() + getString(R.string.fe_from_android_mobile);
		if (!contentSizeIsAccord(this, content)) {
			return;
		}
		final FileRequest filerequest = new FileRequest();
		final FileRequestContent filerequestcontent = new FileRequestContent();
		final CollaborationSendDoRequest requestContent = new CollaborationSendDoRequest();
		requestContent.setRequestType(requestType);
		requestContent.setId(mCollaborationID);
		requestContent.setContent(content);
		requestContent.setFlow(mFlow);
		requestContent.setAttachmentGUID(GUID);
		requestContent.setIsHidden(mSwbHideOpinion.isChecked());
		requestContent.setIsTrace(mSwbTrace.isChecked());
		requestContent.setIsWait(mSwbWait.isChecked());
		requestContent.setReturnToStartNode(mSwbReturnToStartNode.isChecked());
		requestContent.setReturnToThisNode(mSwbReturnToThisNode.isChecked());
		requestContent.setAttitude(CollaborationDisposeActivity.this.getString(R.string.read));

		filerequestcontent.setAttachmentGUID(GUID);
		filerequestcontent.setFiles(CommonUtil.isEmptyList(mSelectedLocalAttachments)
				? new ArrayList<>()
				: mSelectedLocalAttachments);

		filerequest.setRequestContent(requestContent);
		filerequest.setFileContent(filerequestcontent);

		new UploadManager(this)
				.fileRequest(filerequest)
				.progressUpdateListener(new OnProgressUpdateListenerImpl() {
					@Override
					public void onPreExecute() {
						LoadingHint.show(CollaborationDisposeActivity.this);
					}

					@Override
					public void onProgressUpdate(long currentBytes, long contentLength, boolean done) {
						int progress = (int) (currentBytes * 100 / contentLength * 1.0F);
						LoadingHint.showProgress(progress);
					}
				})
				.responseCallback(new ResponseCallback<ResponseContent>() {
					@Override
					public void onCompleted(ResponseContent responseContent) {
						LoadingHint.hide();
						final String errorCode = responseContent.getErrorCode();
						final String errorMessage = responseContent.getErrorMessage();
						if (!TextUtils.equals("0", errorCode)) {
							FEToast.showMessage(errorMessage);
							return;
						}

						//删除缓存文件
						if (cacheFile.exists()) {
							cacheFile.delete();
						}
						FEToast.showMessage(getResources().getString(R.string.message_operation_alert));
						EventBus.getDefault().post(new EventMessageDisposeSuccess());
						startActivity(FormDataProvider.buildIntent(CollaborationDisposeActivity.this, FEMainActivity.class));

					}

					@Override
					public void onFailure(RepositoryException repositoryException) {
						LoadingHint.hide();
					}
				})
				.execute();
	}


	/**
	 * 创建点击发送后弹出的对话框
	 */
	private void createSendDialog(String message) {
		new FEMaterialDialog.Builder(this)
				.setTitle(null)
				.setMessage(message)
				.setPositiveButton(null, dialog -> submit())
				.setNegativeButton(null, null)
				.build()
				.show();
	}

	/*--*****************************************************************--**/

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case FlOW_REQUEST_CODE:
				if (resultCode == RESULT_OK) {
					mFlow = WorkFlowActivity.getResult();// 加签后返回的节点
					WorkFlowActivity.setResultData(null);
					mFlowIsModify = WorkFlowActivity.hasModify();
					setFlowChange();
				}
				break;
			case ATTACHMENT_REQUEST_CODE:
				if (data != null) {
					mSelectedLocalAttachments = data.getStringArrayListExtra("extra_local_file");
					setAttachmentSize();
				}
				break;
			default:
				break;
		}
	}


	private void setFlowChange() {
		mTvFlow.setText(mFlowIsModify ?
				R.string.collaboration_add_person_yes : R.string.collaboration_add_person_not);
	}

	private void setAttachmentSize() {
		int attachmentCount = getAttachmentCount();
		if (attachmentCount <= 0) {
			mTvAttachment.setText(getString(R.string.collaboration_attachment));
		}
		else {
			mTvAttachment.setText(getString(R.string.collaboration_attachment) + "（" + attachmentCount + "）");
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mFlow = null;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mVoiceInput != null) mVoiceInput.dismiss();
		FEUmengCfg.onActivityPauseUMeng(this, FEUmengCfg.CollaborationDispose);
	}

	@Override
	protected void onResume() {
		super.onResume();
		FEUmengCfg.onActivityResumeUMeng(this, FEUmengCfg.CollaborationDispose);
	}

	private boolean contentSizeIsAccord(Context context, String content) {
		if (TextUtils.isEmpty(content)) {
			return false;
		}
		if (content.getBytes().length > 3800) {
			FEToast.showMessage(context.getResources().getString(R.string.dispose_content_size));
			return false;
		}
		return true;
	}


	/**
	 * 判断用户是否在此页面填写过东西
	 */
	private boolean isHasWrote() {
		final String titleText = mEtOpinion.getText().toString();
		return !TextUtils.isEmpty(titleText) || getAttachmentCount() > 0;
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
	public void showExitDialog() {
		new FEMaterialDialog.Builder(this)
				.setTitle(null)
				.setMessage(getString(cn.flyrise.feep.core.R.string.exit_edit_tig))
				.setPositiveButton(null, dialog -> {
					finish();
					//删除缓存文件
					if (cacheFile.exists()) {
						cacheFile.delete();
					}
				})
				.setNegativeButton(null, null)
				.build()
				.show();
	}

	private int getAttachmentCount() {
		return CommonUtil.isEmptyList(mSelectedLocalAttachments) ? 0 : mSelectedLocalAttachments.size();
	}

	private void addLocalAttachments(String attachmentPath) {
		if (mSelectedLocalAttachments == null) {
			mSelectedLocalAttachments = new ArrayList<>();
		}
		mSelectedLocalAttachments.add(attachmentPath);
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

}
