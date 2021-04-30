package cn.flyrise.feep.particular.presenter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import java.util.List;
import java.util.UUID;

import cn.flyrise.android.protocol.entity.MeetingInfoResponse;
import cn.flyrise.android.protocol.entity.MeetingReplyRequest;
import cn.flyrise.android.protocol.model.MeetingAttendUser;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.X;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.listener.OnProgressUpdateListenerImpl;
import cn.flyrise.feep.core.network.request.FileRequest;
import cn.flyrise.feep.core.network.request.FileRequestContent;
import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.core.network.uploader.UploadManager;

/**
 * @author ZYP
 * @since 2016-10-24 16:50
 */
public class MeetingParticularPresenter extends ParticularPresenter {

	private MeetingInfoResponse mResponse;
	private String mMeetingStatus;

	protected MeetingParticularPresenter(Builder builder) {
		super(builder);
		String title = mParticularIntent.isFromAssociate()
				? mContext.getResources().getString(R.string.associate_collaboration_title)
				: mContext.getResources().getString(R.string.meeting_detail);
		mParticularView.setToolBarTitle(title);
	}

	@Override
	public void start() {
		mParticularView.showLoading();
		mParticularRepository
				.fetchMeetingDetail(mParticularIntent.getBusinessId(), mParticularIntent.getMessageId())
				.start(new ResponseCallback<MeetingInfoResponse>(mParticularView) {
					@Override
					public void onCompleted(MeetingInfoResponse response) {
//                        mParticularView.dismissLoading(null);
						if ("-95".equals(response.getErrorCode())) {
							mParticularView.fetchDetailError(response.getErrorMessage());
							return;
						}

						mParticularView.configToolBarRightText(mContext.getResources().getString(R.string.meeting_detail_title));
						mResponse = response;
						displayHeadInformation(response.getSendUserID(), response.getSendUser(), response.getSendTime(),
								response.getTitle());
						fetchUserDetailInfo(response.getSendUserID());

						String url = FEHttpClient.getInstance().getHost() + response.getContent();
						fetchDetailContent(url);

						boolean hasAttachment = configAttachments(response.getAttachments());
						boolean hasReplies = configReplies(response.getReplies(), !mParticularIntent.isFromAssociate());

						FabVO fabVO = new FabVO();
						fabVO.hasReply = hasReplies;
						fabVO.hasAttachment = hasAttachment;
						fabVO.hasDuDu = hasDuDuFunction();
						if (fabVO.hasDuDu) {
							fabVO.duReplyUserIds = parseMeetingAttendUsers(response.getMeetingAttendUsers());
						}
						mParticularView.configFloatingActionButton(fabVO);

						BottomMenuVO bottomMenuVO = null;
						if (TextUtils.equals(response.getMeetingStatus(), "0")) {
							bottomMenuVO = new BottomMenuVO();
							bottomMenuVO.buttonText1 = mContext.getResources().getString(R.string.meeting_attend);
							bottomMenuVO.buttonText2 = mContext.getResources().getString(R.string.meeting_not_attend);
							bottomMenuVO.buttonText3 = mContext.getResources().getString(R.string.meeting_unknown);
						}
						mParticularView.configBottomMenu(bottomMenuVO);
					}

					@Override
					public void onFailure(RepositoryException repositoryException) {
						mParticularView.dismissLoading(null);
					}
				});
	}

	@Override
	public void handleBottomButton1(View view) {
		mMeetingStatus = "1";
		mParticularView.isMettingReply();
		mParticularView.displayReplyView(true, null, mContext.getResources().getString(R.string.meeting_attend));
	}

	@Override
	public void handleBottomButton2(View view) {
		mMeetingStatus = "2";
		mParticularView.isMettingReply();
		mParticularView.displayReplyView(true, null, mContext.getResources().getString(R.string.meeting_not_attend));
	}

	@Override
	public void handleBottomButton3(View view) {
		mMeetingStatus = "3";
		mParticularView.isMettingReply();
		mParticularView.displayReplyView(true, null, mContext.getResources().getString(R.string.meeting_unknown));
	}

	@Override
	public void handleBackButton() {

	}


	@Override
	public void handlePopMenu(int id, Context context) {

	}

	@Override
	public void toolBarRightTextClick(View view) {
		MeetingAttendUserVO attendUserVO = new MeetingAttendUserVO();
		attendUserVO.master = mResponse.getMaster();
		attendUserVO.attendNumber = mResponse.getMeetingAttendNumber();
		attendUserVO.notAttendNumber = mResponse.getMeetingNotAttendNumber();
		attendUserVO.considerNumber = mResponse.getMeetingConsiderNumber();
		attendUserVO.notDealNumber = mResponse.getMeetingNotDealNumber();
		attendUserVO.meetingAttendUser = mResponse.getMeetingAttendUsers();
		attendUserVO.signInNumber = mResponse.getMeetingSignNumber();
		mParticularView.showMeetingAttendUserInfo(view, attendUserVO);
	}

	@Override
	protected int getReplyType() {
		return X.ReplyType.Meeting;
	}

	private String parseMeetingAttendUsers(List<MeetingAttendUser> meetingAttendUsers) {
		if (CommonUtil.isEmptyList(meetingAttendUsers)) {
			return "";
		}

		StringBuilder builder = new StringBuilder();
		int size = meetingAttendUsers.size();
		for (int i = 0; i < size - 1; i++) {
			MeetingAttendUser meetingAttendUser = meetingAttendUsers.get(i);
			if (meetingAttendUser == null ||
					TextUtils.equals(CoreZygote.getLoginUserServices().getUserId(),
							meetingAttendUser.getMeetingAttendUserID())) {
				continue;
			}
			builder.append(meetingAttendUsers.get(i).getMeetingAttendUserID()).append(",");
		}
		builder.append(meetingAttendUsers.get(size - 1).getMeetingAttendUserID());
		return builder.toString();
	}

	@Override
	public UploadManager executeBusinessReply(List<String> attachmentLists, String replyContent) {
		String guid = UUID.randomUUID().toString();
		FileRequest fileRequest = new FileRequest();
		if (attachmentLists != null && attachmentLists.size() > 0) {
			final FileRequestContent fileContent = new FileRequestContent();
			fileContent.setAttachmentGUID(guid);
			fileContent.setFiles(attachmentLists);
			fileRequest.setFileContent(fileContent);
		}

		MeetingReplyRequest replyRequest = new MeetingReplyRequest();
		replyRequest.setId(mResponse.getId());
		replyRequest.setMeetingId(mResponse.getMeeting_join_id());
		replyRequest.setMeetingStatus(mMeetingStatus);
		replyRequest.setMeetingContent(replyContent);
		replyRequest.setMeetingAnnex(guid);
		replyRequest.setRequestType("10");
		fileRequest.setRequestContent(replyRequest);

		UploadManager uploadManager = new UploadManager(mContext)
				.fileRequest(fileRequest)
				.progressUpdateListener(new OnProgressUpdateListenerImpl() {
					@Override
					public void onPreExecute() {
						mParticularView.showLoading();
					}

					@Override
					public void onProgressUpdate(long currentBytes, long contentLength, boolean done) {
						int progress = (int) (currentBytes * 100 / contentLength * 1.0F);
						mParticularView.showLoadingWithProgress(progress);
					}

					@Override
					public void onFailExecute(Throwable ex) {
						mParticularView.dismissLoading(mContext.getResources().getString(R.string.message_operation_fail));
					}
				})
				.responseCallback(new ResponseCallback<ResponseContent>() {
					@Override
					public void onCompleted(ResponseContent responseContent) {
						if (!TextUtils.equals(ResponseContent.OK_CODE, responseContent.getErrorCode())) {
							mParticularView.dismissLoading(mContext.getResources().getString(R.string.message_operation_fail));
							return;
						}
						mParticularView.replySuccess();
						Intent intent = new Intent();
						intent.putExtra("status", mMeetingStatus);
						mParticularView.finishViewWithResult(intent);
					}

					@Override
					public void onFailure(RepositoryException repositoryException) {
						mParticularView.dismissLoading(null);
					}
				});
		uploadManager.execute();
		return uploadManager;
	}
}
