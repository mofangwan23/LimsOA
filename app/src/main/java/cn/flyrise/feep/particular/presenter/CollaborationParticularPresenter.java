package cn.flyrise.feep.particular.presenter;

import static cn.flyrise.feep.core.common.X.RequestType.Sended;
import static cn.flyrise.feep.core.common.X.RequestType.ToDo;
import static cn.flyrise.feep.core.common.X.RequestType.ToDoDispatch;
import static cn.flyrise.feep.core.common.X.RequestType.ToDoNornal;
import static cn.flyrise.feep.core.common.X.RequestType.ToDoRead;
import static cn.flyrise.feep.core.common.X.RequestType.ToSend;
import static cn.flyrise.feep.utils.Patches.PATCH_APPLICATION_BUBBLE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import cn.flyrise.android.protocol.entity.BooleanResponse;
import cn.flyrise.android.protocol.entity.CollaborationDetailsResponse;
import cn.flyrise.android.protocol.entity.CollaborationSendDoRequest;
import cn.flyrise.android.protocol.entity.FormSendDoRequest;
import cn.flyrise.android.protocol.entity.RevocationRequest;
import cn.flyrise.android.protocol.model.Flow;
import cn.flyrise.android.protocol.model.FlowNode;
import cn.flyrise.android.protocol.model.SupplyContent;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.activity.AddBodyActivity;
import cn.flyrise.feep.collaboration.activity.CollaborationDisposeActivity;
import cn.flyrise.feep.collaboration.activity.TransmitActivity;
import cn.flyrise.feep.collaboration.activity.WorkFlowActivity;
import cn.flyrise.feep.collaboration.utility.CollaborationDetailHelper;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.X;
import cn.flyrise.feep.core.common.X.CollaborationType;
import cn.flyrise.feep.core.common.X.FormNode;
import cn.flyrise.feep.core.common.X.RequestType;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.callback.StringCallback;
import cn.flyrise.feep.core.network.entry.AttachmentBean;
import cn.flyrise.feep.core.network.request.RequestContent;
import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.core.services.model.UserKickPrompt;
import cn.flyrise.feep.form.FormCirculateActivity;
import cn.flyrise.feep.form.FormHandleActivity;
import cn.flyrise.feep.form.util.FormDataProvider;
import cn.flyrise.feep.form.view.FormInputIdeaActivity;
import cn.flyrise.feep.particular.ParticularActivity;
import cn.flyrise.feep.utils.Patches;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.json.JSONObject;

/**
 * @author ZYP
 * @since 2016-10-24 16:51
 */
public class CollaborationParticularPresenter extends ParticularPresenter {

	private CollaborationDetailsResponse mResponse;
	private String mBusinessId;
	private int mRequestType;
	private String mQueryString;

	private List<Integer> mPermissions;
	private FormDataProvider mFormDataProvider;

	CollaborationParticularPresenter(Builder builder) {
		super(builder);
		String title = mContext.getResources().getString(R.string.detail_title);
		mParticularView.setToolBarTitle(title);
		mPermissions = new ArrayList<>();
	}

	@Override
	public void start() {
		String messageId = mParticularIntent.getMessageId();
		if (!FunctionManager.hasPatch(PATCH_APPLICATION_BUBBLE)) {
			mBusinessId = mParticularIntent.getIntentBusinessId();
			mRequestType = mParticularIntent.getIntentRequestType();
			messageId = mParticularIntent.getIntentMessageId();
		}
		else {
			if (TextUtils.isEmpty(mBusinessId)) {
				mBusinessId = mParticularIntent.getBusinessId();
			}
			mRequestType = mParticularIntent.getListRequestType();

			int tempRequestType = mParticularIntent.getTempRequestType();
			if (tempRequestType == -1) {
				mParticularIntent.setTempRequestType(-1);
			}
		}

		mParticularView.showLoading();
		mParticularRepository
				.fetchCollaborationDetailString(mBusinessId, mRequestType, messageId)
				.start(new StringCallback() {
					@Override
					public void onCompleted(String s) {
						try {
							JSONObject properties = new JSONObject(s);
							JSONObject iq = properties.getJSONObject("iq");
							String query = iq.get("query").toString();
//                            mQueryString = query;

							CollaborationDetailsResponse response = GsonUtil.getInstance()
									.fromJson(query, CollaborationDetailsResponse.class);
							String errorCode = response.getErrorCode();
							if (TextUtils.equals(errorCode, "-95")) {
								mParticularView.fetchDetailError(response.getErrorMessage());
								return;
							}

							if (TextUtils.equals(errorCode, "-1")
									|| TextUtils.equals(errorCode, "-96")
									|| TextUtils.equals(errorCode, "100001")) {
								String errorMessage = TextUtils.equals(errorCode, "100001")
										? "您的账号在另一个地点登录，已被迫下线"
										: CommonUtil.getString(R.string.message_please_login_again);
								UserKickPrompt ukp = new UserKickPrompt(errorMessage, true);
								SpUtil.put(PreferencesUtils.USER_KICK_PROMPT, GsonUtil.getInstance().toJson(ukp));
								CoreZygote.getApplicationServices().reLoginApplication();
								return;
							}

							if (!TextUtils.isEmpty(response.getMobileFormUrl())) {
								mQueryString = query;
							}
							mParticularIntent.setTempRequestType(CommonUtil.parseInt(response.getRequestType()));
							handleResponse(response);
						} catch (Exception exp) {
							exp.printStackTrace();
						}
					}
				});
	}

	private void handleResponse(CollaborationDetailsResponse response) {
		if ("-95".equals(response.getErrorCode()) || "-99".equals(response.getErrorCode())) {
			mParticularView.fetchDetailError(response.getErrorMessage());
			return;
		}
		mResponse = response;
		setToolBarTitle(response.getType());
		if (!TextUtils.isEmpty(response.getId()) && !TextUtils.equals(response.getId(), mParticularIntent.getBusinessId())) {
			mParticularIntent.setTempBusinessId(response.getId());
		}

		if (mResponse.getType() == 0) {
			mResponse.setCurrentFlowNodeName(null);
		}

		displayHeadInformation(response.getSendUserID(),
				response.getSendUser(), response.getSendTime(),
				response.getTitle(), response.getCurrentFlowNodeName());
		fetchUserDetailInfo(response.getSendUserID());

		if (TextUtils.isEmpty(response.getMobileFormUrl())) {
			configParticularContent(response);
		}
		else {
			mParticularView.displayParticularContent(response.getContent(), false, response.getMobileFormUrl());
		}

		List<SupplyContent> supplyContents = response.getSupplyContents();
		if (CommonUtil.nonEmptyList(supplyContents)) {
			mParticularView.displayContentSupplement(supplyContents);
		}

		if (CommonUtil.nonEmptyList(response.getTrailContents())) {
			mParticularView.displayContentModify(response.getTrailContents());
		}

		List<AttachmentBean> attachments = new ArrayList<>();
		if (CommonUtil.nonEmptyList(response.getAttachments())) {
			attachments.addAll(response.getAttachments());
		}
		if (CommonUtil.nonEmptyList(supplyContents)) {                          // 将正文补充中的附件移动到附件列表
			for (SupplyContent supplyContent : supplyContents) {
				attachments.addAll(supplyContent.getAttachments());
			}
		}
		boolean hasAttachment = configAttachments(attachments);
		boolean hasReplies = configReplies(response.getReplies(),
				response.getType() == 0 && !mParticularIntent.isFromAssociate());
		configOriginalReplies(response.getOriginalReplies());

		FabVO fabVO = new FabVO();
		fabVO.hasAttachment = hasAttachment;
		fabVO.hasReply = hasReplies;
		fabVO.hasDuDu = hasDuDuFunction();
		if (fabVO.hasDuDu) {
			fabVO.duReplyUserIds = parseCollaborationAttendUser(response.getFlow());
		}
		mParticularView.configFloatingActionButton(fabVO);

		//设置右上角按钮的权限控制    - - 贼多if
		if (response.getType() == 0) {
			if (mRequestType == Sended && FunctionManager.hasPatch(Patches.PATCH_COLLABORATION_REVOCATION) && !response.isOver()) {
				mPermissions.add(R.id.action_revocation);
			}
			if (response.isCanTransmit() && FunctionManager.hasPatch(Patches.PATCH_COLLABORATION_TRANSMIT)) {
				mPermissions.add(R.id.action_transmit);
			}
			if (FunctionManager.hasPatch(Patches.PATCH_COLLABORATION_SUPPLEMENT)) {
				mPermissions.add(R.id.action_supplement);
			}
		}
		else if (response.getType() == 1) {
			if (mRequestType == Sended && FunctionManager.hasPatch(Patches.PATCH_FLOW_REVOCATION) && !response.isOver()) {
				mPermissions.add(R.id.action_revocation);
			}
			if (response.getIsSendRead() && FunctionManager.hasPatch(Patches.PATCH_CIRCULATE)) {
				mPermissions.add(R.id.action_circulate);
			}
			//1.发起表单后，流程未完结前发起人都可以对表单补充正文。
			//2.办理表单前，表单环节办理人不可以对表单补充正文。
			//3.办理表单后，流程未完结前表单环节办理人可以对表单补充正文。
			//4.流程完结后所有人不可以对表单补充正文。
			if (FunctionManager.hasPatch(Patches.PATCH_FLOW_SUPPLEMENT) && !response.isOver() && (
					mRequestType == RequestType.Done || mRequestType == Sended)) {
				mPermissions.add(R.id.action_supplement);
			}
		}
		//协同、表单肯定拥有查看流程的权限。
		mPermissions.add(R.id.action_view_flow);

		if (FunctionManager.hasPatch(Patches.PATCH_COLLECTIONS)) {
			mPermissions.add(TextUtils.isEmpty(mResponse.favoriteId) ? R.id.action_collection : R.id.action_collection_cancel);
		}

		if (mPermissions.size() == 1) {
			mParticularView.configToolBarRightText(mContext.getResources().getString(R.string.action_view_flow));
		}
		else {
			mParticularView.configToolBarRightText(mContext.getResources().getString(R.string.action_more));
		}
		configBottomMenu(response);
	}

	@Override
	public void handleBottomButton1(View view) {
		Intent intent = createIntent();
		int listRequestType = mParticularIntent.getListRequestType();
		if (ToDo == listRequestType
				|| ToDoDispatch == listRequestType
				|| ToDoNornal == listRequestType
				|| ToDoRead == listRequestType) {                          // 待办处理按钮
			if (mResponse.getType() == 0) {          // 协同处理
				CollaborationDisposeActivity.setData(mResponse.getFlow(), null);
				intent.putExtra("requestType", CollaborationType.DealWith);
				intent.setClass(mContext, CollaborationDisposeActivity.class);
			}
			else {                                                                                  // 表单处理
				if (mResponse.getNodeType() == FormNode.CopyTo && !mResponse.getNeedReply()) {        // 传阅
					final FormSendDoRequest sendDoRequest = getFormSendDoRequest(mResponse.getId());
					mFormDataProvider = new FormDataProvider(mContext, mResponse.getId(), null);
					if (!mFormDataProvider.isAllowSend) return;
					mFormDataProvider.isAllowSend = false;
					mFormDataProvider.submit(sendDoRequest);
					return;
				}
				intent.putExtra("requestTypeValue", X.FormRequestType.SendDo);
				if (mResponse.getFormHandleViewURL() == null || "".equals(mResponse.getFormHandleViewURL())) {
					intent.setClass(mContext, FormInputIdeaActivity.class);
					intent.putExtra("is_edit", mResponse.isEdit);
				}
				else {  // 含有必填项（需要从网页传过来的）
					intent.setClass(mContext, FormHandleActivity.class);
					intent.putExtra(K.form.TITLE_DATA_KEY, mContext.getResources().getString(R.string.form_input_idea_title));
					intent.putExtra(K.form.URL_DATA_KEY, mResponse.getFormHandleViewURL());
				}
			}
		}
		mParticularView.startIntent(intent);
	}

	@Override
	public void handleBottomButton2(View view) {
		String collaborationID = mResponse.getId();
		boolean isTrace = mResponse.getIsTrace();
		if (mResponse.getType() == 0) { // 协同跟踪
			final CollaborationSendDoRequest request = new CollaborationSendDoRequest();
			request.setRequestType(CollaborationType.ToggleState);
			request.setId(collaborationID);
			request.setFlow(mResponse.getFlow());
			request.setIsTrace(!isTrace);
			postTraceRequest((TextView) view, request);
		}
		else { // 表单的跟踪
			final FormSendDoRequest request = new FormSendDoRequest();
			request.setId(collaborationID);
			request.setDealType(FormNode.Normal);
			request.setTrace(isTrace);
			request.setRequestType(X.FormRequestType.ToggleState);
			postTraceRequest((TextView) view, request);
		}
	}

	@Override
	public void handleBottomButton3(View view) {
		Intent intent = createIntent();
		int listRequestType = mParticularIntent.getListRequestType();
		if (mResponse.getType() == 0) {              // 协同加签
			if (listRequestType == ToDo
					|| ToDoDispatch == listRequestType
					|| ToDoNornal == listRequestType
					|| ToDoRead == listRequestType) {
				CollaborationDisposeActivity.setData(mResponse.getFlow(), mResponse.getCurrentFlowNodeGUID());
				intent.putExtra("requestType", CollaborationType.Additional);
				intent.setClass(mContext, CollaborationDisposeActivity.class);
			}
			else if (listRequestType == Sended) {
				intent.putExtra(WorkFlowActivity.SEND_BUTTON_KEY, true);
				intent.putExtra(WorkFlowActivity.COLLABORATIONID_INTENT_KEY, mResponse.getId());
				intent.putExtra(WorkFlowActivity.COLLABORATIONID_GUID, UUID.randomUUID().toString());

				WorkFlowActivity.setFunction(WorkFlowActivity.COLLABORATION_ADDSIGN);
				WorkFlowActivity.setInitData(mResponse.getFlow(), mResponse.getCurrentFlowNodeGUID());
				intent.setClass(mContext, WorkFlowActivity.class);
			}
		}
		else { // 表单加签跳转
			intent.putExtra("requestTypeValue", X.FormRequestType.Additional);
			if (mResponse.getFormHandleViewURL() == null || "".equals(mResponse.getFormHandleViewURL())) {
				intent.setClass(mContext, FormInputIdeaActivity.class);
				intent.putExtra("is_edit", mResponse.isEdit);
			}
			else {// 含有必填项
				intent.setClass(mContext, FormHandleActivity.class);
				intent.putExtra(K.form.TITLE_DATA_KEY, mContext.getResources().getString(R.string.form_input_idea_title));
				intent.putExtra(K.form.URL_DATA_KEY, mResponse.getFormHandleViewURL() + "&type=1");    // 加签需设置type为1
			}
		}
		mParticularView.startIntent(intent);
	}

	@Override
	public void handleBottomButton4(View view) {
		Intent intent = createIntent();
		if (mResponse.getType() == 0) {// 协同退回
			CollaborationDisposeActivity.setData(mResponse.getFlow(), null);
			intent.putExtra("requestType", CollaborationType.Return);
			intent.setClass(mContext, CollaborationDisposeActivity.class);
		}
		else {
			intent.putExtra("requestTypeValue", X.FormRequestType.Return);
			intent.putExtra("isCanReturnCurrentNode", mResponse.getIsCanReturnCurrentNode());
			intent.setClass(mContext, FormInputIdeaActivity.class);
			intent.putExtra("is_edit", mResponse.isEdit);
		}
		mParticularView.startIntent(intent);
	}

	@Override
	public void handleBackButton() {
		if (mResponse == null) {
			return;
		}
		if (mResponse.getNodeType() == FormNode.CopyTo) {        // 传阅
			final FormSendDoRequest sendDoRequest = getFormSendDoRequest(mResponse.getId());
			mFormDataProvider = new FormDataProvider(mContext, mResponse.getId(), null);
			if (!mFormDataProvider.isAllowSend) return;
			mFormDataProvider.isAllowSend = false;
			mFormDataProvider.submit(sendDoRequest);
		}
	}


	private FormSendDoRequest getFormSendDoRequest(String formID) {
		final FormSendDoRequest sendDoRequest = new FormSendDoRequest();
		sendDoRequest.setRequestType(X.FormRequestType.SendDo);
		sendDoRequest.setId(formID);
		sendDoRequest.setDealType(FormNode.Circulated);
		return sendDoRequest;
	}

	private void postTraceRequest(TextView textView, RequestContent requestContent) {
		FEHttpClient.getInstance().post(requestContent, new ResponseCallback<ResponseContent>() {
			@Override
			public void onCompleted(ResponseContent responseContent) {
				try {
					final String result = responseContent.getErrorCode();
					if (TextUtils.equals(result, "0")) {
						boolean isTrace = mResponse.getIsTrace();
						mParticularView.dismissLoading(mContext.getResources().getString(R.string.message_operation_alert));
						textView.setText(mContext.getResources().getString(isTrace ? R.string.trace : R.string.cancel_trace));
						mResponse.setIsTrace(isTrace ? "0" : "1");
						mParticularView.dismissLoading(mContext.getResources().getString(R.string.message_operation_alert));
					}
					else if (!"-1".equals(result) && !"-96".equals(result)) {
						mParticularView.dismissLoading(responseContent.getErrorMessage());
					}
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(RepositoryException repositoryException) {
				mParticularView.dismissLoading(null);
			}
		});
	}

	private Intent createIntent() {
		Intent intent = new Intent();
		intent.putExtra("collaborationID", mResponse.getId());
		intent.putExtra("currentFlowNodeGUID", mResponse.getCurrentFlowNodeGUID());
		if (mResponse.getType() == 1) {
			int dealType = mResponse.getNodeType();
			intent.putExtra("dealTypeValue", dealType);
		}
		return intent;
	}

	private void configParticularContent(CollaborationDetailsResponse response) {
		if (response.getType() == 0) {
			mParticularView.displayParticularContent(response.getContent(), true, null);
		}
		else {
			String content = response.getContent();
			if (!TextUtils.isEmpty(content) && content.endsWith("<zidingyibiaodan>")) {
				content = content.replace("<zidingyibiaodan>", "");
			}
			String url = FEHttpClient.getInstance().getHost() + content;
			fetchDetailContent(url);
		}
	}

	@Override
	public void toolBarRightTextClick(View view) {
		if (FunctionManager.hasPatch(Patches.PATCH_COLLECTIONS)) {
			if (mRequestType == 1 || mRequestType == 4) {
				mPermissions.remove((Integer) R.id.action_collection);
				mPermissions.remove((Integer) R.id.action_collection_cancel);
				mPermissions.add(TextUtils.isEmpty(mResponse.favoriteId) ? R.id.action_collection : R.id.action_collection_cancel);
			}
		}

		if (mRequestType == 0) {
			mPermissions.remove((Integer) R.id.action_collection);
			mPermissions.remove((Integer) R.id.action_collection_cancel);
		}

		if (mPermissions.size() != 1) {
			mParticularView.showCollaborationMenu(view, mPermissions);
		}
		else {
			CollaborationDetailHelper.showFlowActivity(mContext, mResponse);
		}
	}

	@Override
	protected int getReplyType() {
		return X.ReplyType.Collaboration;
	}

	@Override
	public void handlePopMenu(int id, Context context) {
		super.handlePopMenu(id, context);
		switch (id) {
			case R.id.action_revocation:    // 撤销
				String message;
				if (mResponse.getType() == 0) {
					message = context.getString(R.string.revocation_collaboration);
				}
				else {
					message = context.getString(R.string.revocation_flow);
				}
				mParticularView.showConfirmDialog(message, dialog -> revocation());
				break;
			case R.id.action_transmit:      // 转发
				Intent transmit = new Intent(context, TransmitActivity.class);
				transmit.putExtra(K.collaboration.Extra_Collaboration_ID, mResponse.getId());
				context.startActivity(transmit);
				break;
			case R.id.action_circulate:     // 传阅
				Intent circulate = new Intent(context, FormCirculateActivity.class);
				circulate.putExtra(K.form.EXTRA_ID, mResponse.getId());
				circulate.putExtra(K.form.CURRENT_NODE_ID, mResponse.getId());
				context.startActivity(circulate);
				break;
			case R.id.action_supplement:    // 补充正文
				Intent addIntent = new Intent(context, AddBodyActivity.class);
				addIntent.putExtra(K.collaboration.Extra_Collaboration_ID, mResponse.getId());
				addIntent.putExtra("type", mResponse.getType());
				((Activity) context).startActivityForResult(addIntent, ParticularActivity.CODE_OPEN_ADDBODY);
				break;
			case R.id.action_view_flow:     // 查看流程
				CollaborationDetailHelper.showFlowActivity(context, mResponse);
				break;
		}
	}

	private void revocation() {
		mParticularView.showLoading();
		FEHttpClient.getInstance()
				.post(new RevocationRequest(mResponse.getId(), mResponse.getType()), new ResponseCallback<BooleanResponse>() {
					@Override
					public void onCompleted(BooleanResponse booleanResponse) {
						if (booleanResponse.isSuccess) {
							mParticularView.dismissLoading(CommonUtil.getString(R.string.revocation_success));
							mParticularView.finishViewWithResult(null);
						}
						else {
							if (TextUtils.isEmpty(booleanResponse.getErrorMessage())) {
								onFailure(null);
							}
							else {
								mParticularView.dismissLoading(booleanResponse.getErrorMessage());
							}
						}
					}

					@Override
					public void onFailure(RepositoryException repositoryException) {
						super.onFailure(repositoryException);
						mParticularView.dismissLoading(CommonUtil.getString(R.string.revocation_error));
					}
				});
	}

	private void configBottomMenu(CollaborationDetailsResponse response) {
		if (mParticularIntent.isFromAssociate()) {
			mParticularView.configBottomMenu(null);
			return;
		}

		BottomMenuVO bottomMenuVO = new BottomMenuVO();
		switch (mParticularIntent.getListRequestType()) {
			case ToSend:         // 待发
				bottomMenuVO.buttonText1 = mContext.getResources().getString(R.string.collaboration_send);
				break;
			case ToDo:           // 待办
			case ToDoDispatch:
			case ToDoNornal:
			case ToDoRead:
				if (mResponse.getNodeType() == FormNode.CopyTo) {
					break;
				}
				else {
					bottomMenuVO.buttonText1 = response.getType() == 1
							? mContext.getResources().getString(R.string.collaboration_send_do)
							: mContext.getResources().getString(R.string.deal);
				}
				if (response.getIsAddsign()) {
					bottomMenuVO.buttonText3 = mContext.getResources().getString(R.string.add);    // 有加签权限
				}
				if (response.getIsReturn()) {
					bottomMenuVO.buttonText4 = mContext.getResources().getString(R.string.back);    // 有退回权限
				}
				break;
			case Sended:         // 已发
				bottomMenuVO.buttonText2 = response.getIsTrace()
						? mContext.getResources().getString(R.string.cancel_trace)
						: mContext.getResources().getString(R.string.trace);
				if (response.getType() == 0
						&& response.getIsAddsign() && !response.isOver()) {
					bottomMenuVO.buttonText3 = mContext.getResources().getString(R.string.add);    // 有加签权限并且是协同未办理结束的，流程在已发列表里面只显示跟踪按钮而已。
				}
				break;
			default:
				bottomMenuVO.buttonText2 = response.getIsTrace()
						? mContext.getResources().getString(R.string.cancel_trace)
						: mContext.getResources().getString(R.string.trace);
				break;

		}
		mParticularView.configBottomMenu(bottomMenuVO);
	}

	private void setToolBarTitle(int typeValue) {
		if (mParticularIntent.isFromAssociate()) {
			mParticularView.setToolBarTitle(mContext.getResources().getString(R.string.associate_collaboration_title));
		}
		else if (typeValue == 0) {
			mParticularView.setToolBarTitle(mContext.getResources().getString(R.string.collaboration_detail_title));
		}
		else if (typeValue == 1) {
			mParticularView.setToolBarTitle(mContext.getResources().getString(R.string.form_detail_title));
		}
	}

	private String parseCollaborationAttendUser(Flow flow) {
		if (flow == null) {
			return "";
		}

		if (CommonUtil.isEmptyList(flow.getNodes())) {
			return "";
		}

		Set<String> userIds = parseNodes(flow.getNodes());
		if (CommonUtil.isEmptyList(userIds)) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (String userId : userIds) {
			if (userId.equals(CoreZygote.getLoginUserServices().getUserId())) {
				continue;
			}
			sb.append(userId).append(",");
		}

		int len = sb.length();
		if (len > 0) {
			return sb.substring(0, len - 1);
		}
		else {
			return "";
		}
	}

	private Set<String> parseNodes(List<FlowNode> flowNodes) {
		if (CommonUtil.isEmptyList(flowNodes)) {
			return null;
		}

		Set<String> nodes = new HashSet<>();
		for (FlowNode node : flowNodes) {
			nodes.add(node.getValue());
			Set<String> subNodes = parseNodes(node.getSubnode());
			if (subNodes != null) {
				nodes.addAll(subNodes);
			}
		}
		return nodes;
	}

	public String getQueryString() {
		if (TextUtils.isEmpty(mResponse.getMobileFormUrl())) {
			return null;
		}
		return mQueryString;
	}

	protected String getBusinessId() {
		return mResponse.taskId;//协同这边的用详情返回来的TaskID
	}

	protected String getFavoriteId() {
		return mResponse.favoriteId;
	}

	protected String getAddType() {
		return mRequestType + "";
	}

	protected String getRemoveType() {
		return mRequestType + "";
	}

	protected void setFavoriteId(String favoriteId) {
		mResponse.favoriteId = favoriteId;
	}

	protected String getUserId() {
		return mResponse.getSendUserID();
	}

	protected String getSendTime() {
		return mResponse.getSendTime();
	}

	protected String getTitle() {
		return mResponse.getTitle();
	}
}
