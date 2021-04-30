package cn.flyrise.feep.collaboration.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.text.TextUtils;

import cn.flyrise.android.protocol.entity.AssociationSendRequest;
import cn.flyrise.android.protocol.entity.CollaborationSendDoRequest;
import cn.flyrise.android.protocol.entity.DegreeOfEmergencyRequest;
import cn.flyrise.android.protocol.entity.DegreeOfEmergencyResponse;
import cn.flyrise.android.protocol.entity.WaitingSendDetailRequest;
import cn.flyrise.android.protocol.entity.WaitingSendDetailResponse;
import cn.flyrise.feep.core.common.X.AddressBookType;
import cn.flyrise.feep.core.common.X.CollaborationType;
import cn.flyrise.feep.core.common.X.RequestType;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.network.entry.AttachmentBean;
import cn.flyrise.android.protocol.model.Flow;
import cn.flyrise.android.protocol.model.FlowNode;
import cn.flyrise.feep.FEMainActivity;
import cn.flyrise.feep.K.collaboration;
import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.activity.WorkFlowActivity;
import cn.flyrise.feep.collaboration.matter.MatterListActivity;
import cn.flyrise.feep.collaboration.matter.model.Matter;
import cn.flyrise.feep.collaboration.model.Collaboration;
import cn.flyrise.feep.collaboration.utility.CollaborationDetailHelper;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.X;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.listener.OnProgressUpdateListenerImpl;
import cn.flyrise.feep.core.network.request.FileRequest;
import cn.flyrise.feep.core.network.request.FileRequestContent;
import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.core.network.uploader.UploadManager;
import cn.flyrise.feep.core.services.ILoginUserServices;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.event.EventMessageDisposeSuccess;
import cn.flyrise.feep.form.util.FormDataProvider;
import cn.flyrise.feep.media.attachments.bean.NetworkAttachment;
import cn.flyrise.feep.media.common.LuBan7;
import cn.flyrise.feep.media.common.AttachmentBeanConverter;
import cn.flyrise.feep.utils.Patches;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.greenrobot.eventbus.EventBus;

/**
 * Created by klc on 2017/5/3.
 */

public class NewCollaborationPresenter {

	/**
	 * 添加附件请求码
	 */
	protected final static int ADD_ATTACHMENT_REQUEST_CODE = 100;
	/**
	 * 添加流程请求码
	 */
	protected final static int ADD_FLOW_REQUEST_CODE = 200;
	/**
	 * 添加关联事项请求码
	 */
	protected final static int ADD_ASSOCAIATION_REQUEST_CODE = 300;

	protected NewCollaborationView mView;
	protected Collaboration mCollaboration;
	protected List<Matter> mSelectedMatter = new ArrayList<>();     // 选种的补充事项
	private String[] importValues;
	private boolean isNewImport;

	protected List<String> mLocalAttachments;              // 本地附件
	protected List<NetworkAttachment> mNetworkAttachments;  // 网络附件
	protected List<NetworkAttachment> mOriginNetworkAttachments;  //存储原来附件的网络附件

	private int sendType;

	public NewCollaborationPresenter(NewCollaborationView mView, boolean isNewImport) {
		this.mView = mView;
		this.mCollaboration = new Collaboration();
		this.isNewImport = isNewImport;

		this.mLocalAttachments = new ArrayList<>();
		this.mNetworkAttachments = new ArrayList<>();
	}

	public void loadData(Intent intent) {
		sendType = intent.getIntExtra(collaboration.EXTRA_FORM_TYPE, -1);
		switch (sendType) {
			case collaboration.EXTRA_FORM_TYPE_IM: // forIm;
				loadDataForIm(intent);
				break;
			case collaboration.EXTRA_FORM_TYPE_WAITSEND: //待发
				loadDataForCommit(intent);
				break;
			case collaboration.EXTRA_FORM_TYPE_WORKPLAN://计划
				loadDataFormWorkPlan(intent);
				break;
			case collaboration.EXTRA_FROM_TYPE_SCHEDULE:// 日程
				loadDataFromSchedule(intent);

		}
		if (isNewImport) {
			requestImportValues(new ResponseCallback<DegreeOfEmergencyResponse>() {
				@Override
				public void onCompleted(DegreeOfEmergencyResponse response) {
					if ("0".equals(response.getErrorCode())) {
						importValues = response.result;
						if (importValues.length > 0 && sendType != 101) {
							mView.setImportValue(importValues[0]);
						}
					}
				}
			});
		}
	}

	private void loadDataForIm(Intent intent) {
		mCollaboration = new Collaboration();
		mCollaboration.setTrace(true);
		ArrayList<String> userIds = intent.getStringArrayListExtra("userIds");
		List<AddressBook> addressBooks = CommonUtil.isEmptyList(userIds) ? null
				: CoreZygote.getAddressBookServices().queryUserIds(userIds);
		if (CommonUtil.nonEmptyList(addressBooks)) {
			mCollaboration.flow = new Flow();
			ILoginUserServices userServices = CoreZygote.getLoginUserServices();
			StringBuilder nameBuilder = new StringBuilder();
			nameBuilder.append(userServices.getUserName()).append(",");
			int size = addressBooks.size();
			for (int i = 0; i < size - 1; i++) {
				nameBuilder.append(addressBooks.get(i).name).append(",");
			}
			nameBuilder.append(addressBooks.get(size - 1).name);
			mCollaboration.flow.setName(nameBuilder.toString());
			FlowNode rootNode = new FlowNode();
			rootNode.setGUID(UUID.randomUUID().toString());
			rootNode.setName(userServices.getUserName());
			rootNode.setValue(userServices.getUserId());
			rootNode.setType(AddressBookType.Staff);
			rootNode.setPopudom(X.NodePermission.Rollback);
			rootNode.setStatus(X.NodeState.Uncheck);
			List<FlowNode> subNodes = new ArrayList<>(addressBooks.size());
			for (AddressBook addressBook : addressBooks) {
				FlowNode node = new FlowNode();
				node.setGUID(UUID.randomUUID().toString());
				node.setName(addressBook.name);
				node.setValue(addressBook.userId);
				node.setType(AddressBookType.Staff);
				node.setPopudom(X.NodePermission.Rollback);
				node.setStatus(X.NodeState.Uncheck);
				subNodes.add(node);
			}
			rootNode.setSubnode(subNodes);
			mCollaboration.flow.setNodes(Collections.singletonList(rootNode));
			mView.displayView(mCollaboration);
			mView.setFileTextCount(getAttachmentCount());       // TODO Update 附件数量
			mView.setHasFlow(hasFlow());
		}
	}

	private void loadDataFromSchedule(Intent intent) {
		mCollaboration = new Collaboration();
		mCollaboration.title = intent.getStringExtra("title");
		mCollaboration.content = intent.getStringExtra("content");
		mCollaboration.setTrace(true);
		ArrayList<String> userIds = intent.getStringArrayListExtra("userIds");
		List<AddressBook> addressBooks = CommonUtil.isEmptyList(userIds) ? null
				: CoreZygote.getAddressBookServices().queryUserIds(userIds);
		if (CommonUtil.nonEmptyList(addressBooks)) {
			mCollaboration.flow = new Flow();
			ILoginUserServices userServices = CoreZygote.getLoginUserServices();
			StringBuilder nameBuilder = new StringBuilder();
			nameBuilder.append(userServices.getUserName()).append(",");
			int size = addressBooks.size();
			for (int i = 0; i < size - 1; i++) {
				nameBuilder.append(addressBooks.get(i).name).append(",");
			}
			nameBuilder.append(addressBooks.get(size - 1).name);
			mCollaboration.flow.setName(nameBuilder.toString());
			FlowNode rootNode = new FlowNode();
			rootNode.setGUID(UUID.randomUUID().toString());
			rootNode.setName(userServices.getUserName());
			rootNode.setValue(userServices.getUserId());
			rootNode.setType(AddressBookType.Staff);
			rootNode.setPopudom(X.NodePermission.Rollback);
			rootNode.setStatus(X.NodeState.Uncheck);
			List<FlowNode> subNodes = new ArrayList<>(addressBooks.size());
			for (AddressBook addressBook : addressBooks) {
				FlowNode node = new FlowNode();
				node.setGUID(UUID.randomUUID().toString());
				node.setName(addressBook.name);
				node.setValue(addressBook.userId);
				node.setType(AddressBookType.Staff);
				node.setPopudom(X.NodePermission.Rollback);
				node.setStatus(X.NodeState.Uncheck);
				subNodes.add(node);
			}
			rootNode.setSubnode(subNodes);
			mCollaboration.flow.setNodes(Collections.singletonList(rootNode));
		}
		mView.setHasFlow(hasFlow());
		mView.displayView(mCollaboration);
	}

	private void loadDataForCommit(Intent intent) {
		mView.showLoading();
		String id = intent.getStringExtra("collaborationId");
		FEHttpClient.getInstance().post(new WaitingSendDetailRequest(id), new ResponseCallback<WaitingSendDetailResponse>() {
			@Override
			public void onCompleted(WaitingSendDetailResponse waitingSendDetailResponse) {
				mView.hideLoading();
				mCollaboration = waitingSendDetailResponse.result;
				mNetworkAttachments = AttachmentBeanConverter.convert(mCollaboration.getAttachmentList());
				if (!CommonUtil.isEmptyList(mNetworkAttachments)) {
					mOriginNetworkAttachments = new ArrayList<>();
					mOriginNetworkAttachments.addAll(mNetworkAttachments);
				}
				mSelectedMatter = CollaborationDetailHelper.relationItemToMatter(mCollaboration.getRelationList());
				mView.displayView(mCollaboration);
				mView.setFileTextCount(getAttachmentCount());
				mView.setHasFlow(hasFlow());
				mView.setAssociationCount(mSelectedMatter == null ? 0 : mSelectedMatter.size());
				mView.setImportValue(mCollaboration.important);
			}

			@Override
			public void onFailure(RepositoryException repositoryException) {
				super.onFailure(repositoryException);
				mView.hideLoading();
			}
		});
	}

	private void loadDataFormWorkPlan(Intent intent) {
		mCollaboration = new Collaboration();
		mCollaboration.title = intent.getStringExtra(collaboration.EXTRA_NEW_COLLABORATION_TITLE);
		mCollaboration.content = intent.getStringExtra(collaboration.EXTRA_NEW_COLLABORATION_CONTENT);
		ArrayList<AttachmentBean> netWorkAttachments = intent.getParcelableArrayListExtra(collaboration.EXTRA_NEW_COLLABORATION_ATTACHMENT);
		mNetworkAttachments = AttachmentBeanConverter.convert(netWorkAttachments);
		mView.setFileTextCount(getAttachmentCount());
		mView.displayView(mCollaboration);
	}

	public void handleConfirmBtn(Context context) {
		newCollaboration(context);
	}

	public void saveCollaboration(Context context) {
		final CollaborationSendDoRequest requestContent = new CollaborationSendDoRequest();
		if (mCollaboration.flow == null) {
			mCollaboration.flow = new Flow();
			final FlowNode ni = new FlowNode();
			ni.setName(CoreZygote.getLoginUserServices().getUserName());
			ni.setType(AddressBookType.Staff);
			ni.setValue(CoreZygote.getLoginUserServices().getUserId());
			ni.setGUID(UUID.randomUUID().toString());
			ni.setPopudom(X.NodePermission.Rollback);
			ni.setType(AddressBookType.Staff);
			ni.setStatus(X.NodeState.Uncheck);
			mCollaboration.flow.setNodes(new ArrayList<>());
			mCollaboration.flow.getNodes().add(ni);
			mCollaboration.flow.setGUID(UUID.randomUUID().toString());
			mCollaboration.flow.setName(CoreZygote.getLoginUserServices().getUserId());
		}
		requestContent.setRequestType(CollaborationType.TempStorage);
		if (TextUtils.isEmpty(mCollaboration.title)) {
			mCollaboration.title = "无标题";
		}
		if (TextUtils.isEmpty(mCollaboration.content)) {
			mCollaboration.content = "无内容";
		}
		checkSend(context, requestContent);
	}

	private void newCollaboration(Context context) {
		if (mCollaboration.content == null) {
			mCollaboration.content = "";
		}
		else {
			mCollaboration.content = mCollaboration.content + "<br>";
		}
		mCollaboration.content = mCollaboration.content + context.getString(R.string.fe_from_android_mobile);
		mCollaboration.flow.getNodes().get(0).setStatus(X.NodeState.Checked);// 所有流程发出去了,根节点都被处理,很奇怪干嘛这个处理不在服务器做
		final CollaborationSendDoRequest requestContent = new CollaborationSendDoRequest();
		requestContent.setRequestType(CollaborationType.SendDo);
		checkSend(context, requestContent);
	}

	protected void uploadAssociation(Context context, CollaborationSendDoRequest requestContent) {
		mView.showLoading();
		if (TextUtils.isEmpty(mCollaboration.relationflow)) {
			mCollaboration.relationflow = UUID.randomUUID().toString();
		}
		List<AssociationSendRequest.SendAssociation> sendList = new ArrayList<>();
		for (Matter association : mSelectedMatter) {
			sendList.add(
					new AssociationSendRequest.SendAssociation(association.title, String.valueOf(association.matterType), association.id));
		}
		AssociationSendRequest request = new AssociationSendRequest(mCollaboration.relationflow,
				new AssociationSendRequest.Relationflow(sendList));
		FEHttpClient.getInstance().post(request, new ResponseCallback<ResponseContent>() {
			@Override
			public void onCompleted(ResponseContent response) {
				if (response.getErrorCode().equals(ResponseContent.OK_CODE)) {
					sendContent(context, requestContent);
				}
				else {
					onFailure(null);
				}
			}

			@Override
			public void onFailure(RepositoryException repositoryException) {
				super.onFailure(repositoryException);
				mView.hideLoading();
			}
		});
	}

	void checkSend(Context context, CollaborationSendDoRequest requestContent) {
		if (!CommonUtil.isEmptyList(mSelectedMatter) && FunctionManager.hasPatch(Patches.PATCH_RELATED_MATTERS)) {
			uploadAssociation(context, requestContent);
		}
		else {
			sendContent(context, requestContent);
		}
	}

	public void sendContent(Context context, CollaborationSendDoRequest requestContent) {
		if (mCollaboration.flow != null) {
			mCollaboration.flow.setGUID(UUID.randomUUID().toString());
		}
		if (TextUtils.isEmpty(mCollaboration.attachmentGUID)) {
			mCollaboration.attachmentGUID = UUID.randomUUID().toString();
		}

		requestContent.setId(mCollaboration.id == null ? "" : mCollaboration.id);
		requestContent.setAttachmentGUID(mCollaboration.attachmentGUID);
		requestContent.setRelationFlow(mCollaboration.relationflow);
		requestContent.setContent(mCollaboration.content);
		requestContent.setFlow(mCollaboration.flow);
		requestContent.setImportanceKey(mCollaboration.important);
		requestContent.setImportanceValue(mCollaboration.important);
		requestContent.setIsTrace(mCollaboration.isTrace());
		requestContent.setTitle(mCollaboration.title);

		final FileRequestContent filerequestcontent = new FileRequestContent();
		filerequestcontent.setAttachmentGUID(mCollaboration.attachmentGUID);
		filerequestcontent.setFiles(CommonUtil.isEmptyList(mLocalAttachments) ? new ArrayList<>() : mLocalAttachments);
		if (sendType == collaboration.EXTRA_FORM_TYPE_WORKPLAN)
			filerequestcontent.setCopyFileIds(getNeedCopyFileIdDs());
		else
			filerequestcontent.setDeleteFileIds(getDeleteFileIDs());

		final FileRequest fileRequest = new FileRequest();
		fileRequest.setRequestContent(requestContent);
		fileRequest.setFileContent(filerequestcontent);

		final ArrayList<Integer> typeList = new ArrayList<>();  // 在新建协同完成后，返回的时候待办的气泡会消失，所以待办也一起刷新
		typeList.add(RequestType.Sended);
		typeList.add(RequestType.ToSend);
		typeList.add(RequestType.ToDo);

		new UploadManager(context)
				.fileRequest(fileRequest)
				.progressUpdateListener(new OnProgressUpdateListenerImpl() {
					@Override
					public void onProgressUpdate(long currentBytes, long contentLength, boolean done) {
						int progress = (int) (currentBytes * 100 / contentLength * 1.0F);
						mView.showProgress(progress);
					}

					@Override
					public void onPreExecute() {
						super.onPreExecute();
						mView.showLoading();
					}
				})
				.responseCallback(new ResponseCallback<ResponseContent>() {
					@Override
					public void onCompleted(ResponseContent responseContent) {
						mView.hideLoading();
						final String errorCode = responseContent.getErrorCode();
						final String errorMessage = responseContent.getErrorMessage();
						if (!TextUtils.equals("0", errorCode)) {
							FEToast.showMessage(errorMessage);
							return;
						}
						FEToast.showMessage(R.string.message_operation_alert);
						EventMessageDisposeSuccess success = new EventMessageDisposeSuccess();
						success.isRefresh = true;
						EventBus.getDefault().post(success);
						context.startActivity(FormDataProvider.buildIntent(context, FEMainActivity.class));
					}

					@Override
					public void onFailure(RepositoryException repositoryException) {
						mView.hideLoading();
					}
				})
				.execute();
	}


	private void requestImportValues(ResponseCallback<DegreeOfEmergencyResponse> responseCallback) {
		DegreeOfEmergencyRequest request = new DegreeOfEmergencyRequest();
		FEHttpClient.getInstance().post(request, responseCallback);
	}

	public void importClick() {
		if (importValues == null || importValues.length == 0) {
			mView.showLoading();
			requestImportValues(new ResponseCallback<DegreeOfEmergencyResponse>() {
				@Override
				public void onCompleted(DegreeOfEmergencyResponse response) {
					mView.hideLoading();
					if ("0".equals(response.getErrorCode())) {
						importValues = response.result;
						mView.showImportDialog(importValues);
					}
				}

				@Override
				public void onFailure(RepositoryException repositoryException) {
					super.onFailure(repositoryException);
					mView.hideLoading();
				}
			});
		}
		else {
			mView.showImportDialog(importValues);
		}
	}


	public void attachmentClick(Activity activity) {
		LuBan7.pufferGrenades(activity, mLocalAttachments, mNetworkAttachments, ADD_ATTACHMENT_REQUEST_CODE);
	}

	public void flowClick(Activity activity, int function) {
		final Intent intent = new Intent(activity, WorkFlowActivity.class);
		WorkFlowActivity.setInitData(mCollaboration.flow, null);
		WorkFlowActivity.setFunction(function);
		activity.startActivityForResult(intent, ADD_FLOW_REQUEST_CODE);
	}

	public void associationClick(Activity activity) {
		Intent intent = new Intent(activity, MatterListActivity.class);
		if (CommonUtil.nonEmptyList(mSelectedMatter)) {
			intent.putExtra("selectedAssociation", mSelectedMatter.toArray(new Matter[]{}));
		}
		activity.startActivityForResult(intent, ADD_ASSOCAIATION_REQUEST_CODE);
	}

	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case ADD_ATTACHMENT_REQUEST_CODE:// 添加附件返回来的结果
				if (data != null) {
					mLocalAttachments = data.getStringArrayListExtra("extra_local_file");
					mNetworkAttachments = data.getParcelableArrayListExtra("extra_network_file");
					mView.setFileTextCount(getAttachmentCount());
					return true;
				}
			case ADD_FLOW_REQUEST_CODE:
				// 添加流程返回来的结果
				if (resultCode == Activity.RESULT_OK) {
					mCollaboration.flow = WorkFlowActivity.getResult();
					WorkFlowActivity.setResultData(null);
					mView.setHasFlow(hasFlow());
				}
				return true;
			case ADD_ASSOCAIATION_REQUEST_CODE:
				if (data != null) {
					Parcelable[] parcelables = data.getParcelableArrayExtra("selectedAssociation");
					Matter[] associations = Arrays.copyOf(parcelables, parcelables.length, Matter[].class);
					mSelectedMatter = Arrays.asList(associations);
					mView.setAssociationCount(mSelectedMatter.size());
				}
				return true;
		}
		return false;
	}

	public boolean hasFile() {
		return getAttachmentCount() != 0;
	}

	public boolean hasFlow() {
		Flow flow = mCollaboration.flow;
		if (flow != null) {
			if (flow.getNodes() != null && flow.getNodes().size() != 0) {
				FlowNode flownode = flow.getNodes().get(0);
				return !(flownode == null || flownode.getSubnode() == null || flownode.getSubnode().size() == 0);
			}
			else {
				return false;
			}
		}
		return false;
	}

	public boolean hasAssociation() {
		return !CommonUtil.isEmptyList(mSelectedMatter);
	}

//	private String getDeleteFileIDs() {
//		if (CommonUtil.isEmptyList(mCollaboration.getAttachmentList())) {
//			return "";
//		}
//		List<NetworkAttachment> intersection = new ArrayList<>(AttachmentBeanConverter.convert(mCollaboration.getAttachmentList()));
//		if (!CommonUtil.isEmptyList(mNetworkAttachments)) {
//			intersection.removeAll(mNetworkAttachments);
//		}
//		StringBuilder deleteFile = new StringBuilder();
//		if (CommonUtil.nonEmptyList(intersection)) {
//			for (NetworkAttachment attachment : intersection) {
//				deleteFile.append(attachment.getId()).append(",");
//			}
//		}
//		if (deleteFile.length() > 0) {
//			deleteFile.deleteCharAt(deleteFile.length() - 1);
//		}
//		return deleteFile.toString();
//	}

	private List<String> getDeleteFileIDs() {
		if (CommonUtil.isEmptyList(mOriginNetworkAttachments)) {
			return null;
		}
		if (!CommonUtil.isEmptyList(mNetworkAttachments)) {
			mOriginNetworkAttachments.removeAll(mNetworkAttachments);
		}
		List<String> deleteFiles = new ArrayList<>();
		if (CommonUtil.nonEmptyList(mOriginNetworkAttachments)) {
			for (NetworkAttachment attachment : mOriginNetworkAttachments) {
				deleteFiles.add(attachment.getId());
			}
		}
		return deleteFiles;
	}

	private String getNeedCopyFileIdDs() {
		if (CommonUtil.isEmptyList(mNetworkAttachments)) {
			return null;
		}
		StringBuilder ids = new StringBuilder();
		for (NetworkAttachment attachment : mNetworkAttachments) {
			ids.append(attachment.getId()).append(",");
		}
		ids.deleteCharAt(ids.length() - 1);
		return ids.toString();
	}

	public Collaboration getCollaboration() {
		return mCollaboration;
	}

	protected int getAttachmentCount() {
		int totalCount = 0;
		if (CommonUtil.nonEmptyList(mLocalAttachments)) {
			totalCount += mLocalAttachments.size();
		}

		if (CommonUtil.nonEmptyList(mNetworkAttachments)) {
			totalCount += mNetworkAttachments.size();
		}

		return totalCount;
	}
}
