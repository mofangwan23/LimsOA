package cn.flyrise.feep.particular.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import cn.flyrise.feep.collaboration.activity.NewCollaborationActivity;
import cn.flyrise.feep.schedule.NewScheduleActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.flyrise.android.protocol.entity.SendReplyRequest;
import cn.flyrise.android.protocol.entity.SendReplyResponse;
import cn.flyrise.android.protocol.entity.workplan.WorkPlanDetailResponse;
import cn.flyrise.android.protocol.model.User;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.X;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.core.network.uploader.UploadManager;

/**
 * @author ZYP
 * @author klc
 * @since 2016-10-24 16:51
 * @since 2018-05-09 11:03
 * msg: 增加计划转日程，协同。
 */
public class WorkPlanParticularPresenter extends ParticularPresenter {

	private WorkPlanDetailResponse mResponse;

	protected WorkPlanParticularPresenter(Builder builder) {
		super(builder);
		String title = mContext.getResources().getString(R.string.workplan_detail);
		mParticularView.setToolBarTitle(title);
	}

	@Override
	public void start() {
		mParticularView.showLoading();
		mParticularRepository
				.fetchWorkPlanDetail(mParticularIntent.getBusinessId(), mParticularIntent.getMessageId(),
						mParticularIntent.getRelatedUserId())
				.start(new ResponseCallback<WorkPlanDetailResponse>(mParticularView) {
					@Override
					public void onCompleted(WorkPlanDetailResponse response) {
						mParticularView.dismissLoading(null);
						if ("-95".equals(response.getErrorCode())) {
							mParticularView.fetchDetailError(response.getErrorMessage());
							return;
						}
						mResponse = response;
						HeadVO headVO = new HeadVO();
						headVO.title = response.getTitle();
						headVO.sendUserId = response.getSendUserID();
						headVO.sendUserName = response.getSendUser();
						headVO.receiverUsers = response.getReceiveUsers();
						headVO.copyToUsers = response.getCCUsers();
						headVO.noticeToUsers = response.getNoticeUsers();
						headVO.startTime = response.getStartTime();
						headVO.endTime = response.getEndTime();
						mParticularView.displayHeadInformation(headVO);
						fetchUserDetailInfo(response.getSendUserID());

						mParticularView.displayParticularContent(response.getContent(), true, null);
						boolean hasAttachment = configAttachments(response.getAttachments());
						boolean hasReplies = configReplies(response.getReplies(), true);

						FabVO fabVO = new FabVO();
						fabVO.hasAttachment = hasAttachment;
						fabVO.hasReply = hasReplies;
						fabVO.hasDuDu = hasDuDuFunction();
						if (fabVO.hasDuDu) {
							List<User> duduUser = new ArrayList<>();
							if(response.getReceiveUsers()!=null){
								duduUser.addAll(response.getReceiveUsers());
							}
							if(response.getCCUsers()!=null){
								duduUser.addAll(response.getCCUsers());
							}
							if(response.getNoticeUsers()!=null){
								duduUser.addAll(response.getNoticeUsers());
							}
							fabVO.duReplyUserIds = parseWorkPlanReceiver(duduUser);
						}
						mParticularView.configFloatingActionButton(fabVO);

						BottomMenuVO bottomMenuVO = null;
						String userID = CoreZygote.getLoginUserServices().getUserId();
						if (!TextUtils.equals(userID, response.getSendUserID())) {
							bottomMenuVO = new BottomMenuVO();
							bottomMenuVO.buttonText4 = mContext.getResources().getString(R.string.reply);
						}

						//设置右上角的事件
						mParticularView.configToolBarRightText(mContext.getResources().getString(R.string.action_more));

						mParticularView.configBottomMenu(bottomMenuVO);
					}
				});
	}

	@Override
	public void clickToReply(String replyId) {
		mParticularView.displayReplyView(false, replyId, null);
	}

	@Override
	protected int getReplyType() {
		return X.ReplyType.WorkPlan;
	}

	@Override
	public void handleBottomButton4(View view) {
		mParticularView.displayReplyView(false, null, mContext.getString(R.string.submit));
	}

	@Override
	public void handleBackButton() {

	}

	@Override public void toolBarRightTextClick(View view) {
		mParticularView.showWorkPlanMenu(view);
	}

	@Override
	public void handlePopMenu(int id, Context context) {
		if (id == R.id.action_2collaboration) {
			NewCollaborationActivity.startForWorkPlan((Activity) mContext, mResponse.getTitle(),
					mResponse.getContent(), mResponse.getAttachments());
		}
		else if (id == R.id.action_2schedule) {
			NewScheduleActivity.startActivityFromWorkPlan((Activity) mContext, mResponse.getTitle(), mResponse.getContent(),
					 mResponse.getAttachments());
		}
	}

	private String parseWorkPlanReceiver(List<User> receiverList) {
		if (CommonUtil.isEmptyList(receiverList)) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		int size = receiverList.size();
		for (int i = 0; i < size - 1; i++) {
			User user = receiverList.get(i);
			if (user == null ||
					TextUtils.equals(user.getId(), CoreZygote.getLoginUserServices().getUserId())) {
				continue;
			}
			builder.append(receiverList.get(i).getId()).append(",");
		}
		builder.append(receiverList.get(size - 1).getId());
		return builder.toString();
	}

	@Override
	public UploadManager executeBusinessReply(List<String> attachmentLists, String replyContent) {
		final String attachmentGUID = UUID.randomUUID().toString();
		final SendReplyRequest replyRequest = new SendReplyRequest();
		replyRequest.setContent(replyContent);
		replyRequest.setId(mResponse.getId());
		replyRequest.setAttachmentGUID(attachmentGUID);
		replyRequest.setReplyID("");
		replyRequest.setReplyType(X.ReplyType.WorkPlan);

		FEHttpClient.getInstance().post(replyRequest, new ResponseCallback<SendReplyResponse>() {
			@Override
			public void onCompleted(SendReplyResponse sendReplyResponse) {
				if (!TextUtils.equals(ResponseContent.OK_CODE, sendReplyResponse.getErrorCode())) {
					mParticularView.dismissLoading(mContext.getResources().getString(R.string.message_operation_fail));
					return;
				}
				mParticularView.replySuccess();
				Intent intent = new Intent("refreshWorkPlanList");
				intent.putExtra("planID", mResponse.getId());
				mContext.sendBroadcast(intent);
				start();
			}

			@Override
			public void onFailure(RepositoryException repositoryException) {
				mParticularView.dismissLoading(null);
			}
		});
		return null;
	}
}
