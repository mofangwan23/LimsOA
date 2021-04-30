package cn.flyrise.feep.particular.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.protocol.entity.AddressBookResponse;
import cn.flyrise.android.protocol.entity.SendReplyRequest;
import cn.flyrise.android.protocol.model.AddressBookItem;
import cn.flyrise.feep.collection.CollectionFolderActivity;
import cn.flyrise.feep.collection.CollectionFolderFragment;
import cn.flyrise.feep.collection.FavoriteRepository;
import cn.flyrise.feep.collection.bean.CollectionEvent;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.network.entry.AttachmentBean;
import cn.flyrise.android.protocol.model.MeetingAttendUser;
import cn.flyrise.android.protocol.model.Reply;
import cn.flyrise.android.protocol.model.User;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.callback.StringCallback;
import cn.flyrise.feep.core.network.listener.OnProgressUpdateListenerImpl;
import cn.flyrise.feep.core.network.request.FileRequest;
import cn.flyrise.feep.core.network.request.FileRequestContent;
import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.core.network.uploader.UploadManager;
import cn.flyrise.feep.particular.ParticularActivity;
import cn.flyrise.feep.particular.ParticularContract;
import cn.flyrise.feep.particular.ParticularIntent;
import cn.flyrise.feep.particular.repository.ParticularRepository;
import cn.flyrise.feep.media.common.AttachmentBeanConverter;
import java.util.List;
import java.util.UUID;
import org.greenrobot.eventbus.EventBus;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2016-10-20 11:06
 */
public abstract class ParticularPresenter implements ParticularContract.IPresenter {

	public static final int PARTICULAR_NEWS = 1;
	public static final int PARTICULAR_ANNOUNCEMENT = 2;
	public static final int PARTICULAR_MEETING = 3;
	public static final int PARTICULAR_COLLABORATION = 4;
	public static final int PARTICULAR_WORK_PLAN = 5;

	protected ParticularRepository mParticularRepository;
	protected final Context mContext;
	protected final ParticularIntent mParticularIntent;
	protected final ParticularContract.IView mParticularView;
	protected FavoriteRepository mRepository;

	protected ParticularPresenter(Builder builder) {
		this.mContext = builder.context;
		this.mParticularView = builder.particularView;
		this.mParticularIntent = builder.particularIntent;
		this.setParticularRepository(new ParticularRepository());
	}

	public abstract void start();

	public void setParticularRepository(ParticularRepository repository) {
		this.mParticularRepository = repository;
	}

	@Override
	public UploadManager executeBusinessReply(List<String> attachmentLists, String replyContent) {
		return null;
	}

	@Override
	public void fetchUserDetailInfo(String userId) {
		mParticularRepository.fetchUserDetailInfo(userId)
				.start(new ResponseCallback<AddressBookResponse>(mParticularView) {
					@Override
					public void onCompleted(AddressBookResponse addressBookResponse) {
						List<AddressBookItem> items = addressBookResponse.getItems();
						if (!CommonUtil.isEmptyList(items)) {
							mParticularView.configSendUserContactInfo(items.get(0));
						}
					}

					@Override
					public void onFailure(RepositoryException repositoryException) {
					}
				});
	}

	@Override
	public void toolBarRightTextClick(View view) {
	}

	@Override
	public ParticularIntent getParticularIntent() {
		return this.mParticularIntent;
	}

	@Override
	public void clickToReply(String replyId) {
		mParticularView.displayReplyView(true, replyId, null);
	}

	protected void fetchDetailContent(String url) {
		mParticularRepository
				.fetchDetailContent(url)
				.start(new StringCallback(mParticularView) {
					@Override
					public void onCompleted(String content) {
						mParticularView.displayParticularContent(content, false, null);
					}

					@Override
					public void onFailure(RepositoryException repositoryException) {
						mParticularView.dismissLoading(null);
					}
				});
	}

	protected void displayHeadInformation(String sendUserId, String sendUserName, String sendTime, String title) {
		HeadVO headVO = new HeadVO();
		headVO.title = title;
		headVO.sendUserId = sendUserId;
		headVO.sendUserName = sendUserName;
		headVO.sendTime = sendTime;
		mParticularView.displayHeadInformation(headVO);
	}

	protected void displayHeadInformation(String sendUserId, String sendUserName, String sendTime, String title, String nodeName) {
		HeadVO headVO = new HeadVO();
		headVO.title = title;
		headVO.sendUserId = sendUserId;
		headVO.sendUserName = sendUserName;
		headVO.sendTime = sendTime;
		headVO.nowNodeName = nodeName;
		mParticularView.displayHeadInformation(headVO);
	}

	protected boolean configAttachments(List<AttachmentBean> attachments) {
		if (CommonUtil.isEmptyList(attachments)) {
			mParticularView.displayAttachment(null);
			return false;
		}
		mParticularView.displayAttachment(AttachmentBeanConverter.convert(attachments));
		return true;
	}

	protected boolean configReplies(List<Reply> replies, boolean showReplyButton) {
		if (CommonUtil.isEmptyList(replies)) return false;
		mParticularView.displayReplyList(replies, showReplyButton);
		return true;
	}

	protected boolean configOriginalReplies(List<Reply> replies) {
		if (CommonUtil.isEmptyList(replies)) return false;
		mParticularView.displayOriginalReplyList(replies);
		return true;
	}

	protected boolean hasDuDuFunction() {
		return FunctionManager.hasModule(45);
	}

	@Override
	public UploadManager executeCommentReply(List<String> attachments, String replyContent, String replyId) {
		final String guid = UUID.randomUUID().toString();
		final FileRequest fileRequest = new FileRequest();
		if (attachments != null && attachments.size() > 0) {
			final FileRequestContent fileContent = new FileRequestContent();
			fileContent.setAttachmentGUID(guid);
			fileContent.setFiles(attachments);
			fileRequest.setFileContent(fileContent);
		}
		final SendReplyRequest replyRequest = new SendReplyRequest();
		replyRequest.setAttachmentGUID(guid);
		replyRequest.setContent(replyContent);
		replyRequest.setId(mParticularIntent.getBusinessId());
		replyRequest.setReplyID(replyId);
		replyRequest.setReplyType(getReplyType());
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
						mParticularView.dismissLoading(mContext.getResources().getString(R.string.reply_failure));
					}
				})
				.responseCallback(new ResponseCallback<ResponseContent>(mParticularView) {
					@Override
					public void onCompleted(ResponseContent responseContent) {
						if (!ResponseContent.OK_CODE.equals(responseContent.getErrorCode())) {
							mParticularView.dismissLoading(mContext.getResources().getString(R.string.reply_failure));
							return;
						}
						mParticularView.replySuccess();
						start();
					}

					@Override
					public void onFailure(RepositoryException repositoryException) {
						mParticularView.dismissLoading(null);
					}
				});
		uploadManager.execute();
		return uploadManager;
	}

	@Override
	public void handleBottomButton1(View view) {
	}

	@Override
	public void handleBottomButton2(View view) {
	}

	@Override
	public void handleBottomButton3(View view) {
	}

	@Override
	public void handleBottomButton4(View view) {
	}

	@Override public void handlePopMenu(int id, Context context) {
		switch (id) {
			case R.id.action_collection:        // 添加收藏
				Intent intent = new Intent(context, CollectionFolderActivity.class);
				intent.putExtra("mode", CollectionFolderFragment.MODE_SELECT);
				((Activity) mContext).startActivityForResult(intent, ParticularActivity.CODE_SELECT_COLLECTION_FOLDER);
				break;
			case R.id.action_collection_cancel: // 移除收藏
				removeFromFavoriteFolder();
				break;
		}
	}

	@Override public void addToFavoriteFolder(String favoriteId, String favoriteName) {
		// 添加
		if (mRepository == null) {
			mRepository = new FavoriteRepository();
		}

		LoadingHint.show(mContext);
		mRepository
				.addToFolder(favoriteId, getBusinessId(), getAddType(), getTitle(), getUserId(), getSendTime())
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(result -> {
					LoadingHint.hide();
					if (result.errorCode == 0) {
						FEToast.showMessage("添加成功");
						EventBus.getDefault().post(new CollectionEvent(200));
						setFavoriteId(favoriteId);  // 记得更新一下状态
						return;
					}
					FEToast.showMessage(result.errorMessage);
				}, exception -> {
					LoadingHint.hide();
					FEToast.showMessage("添加收藏失败，请稍后重试！");
				});
	}

	protected void removeFromFavoriteFolder() {
		if (mRepository == null) {
			mRepository = new FavoriteRepository();
		}

		LoadingHint.show(mContext);
		mRepository.removeFromFolder(getFavoriteId(), getBusinessId(), getRemoveType())
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(result -> {
					LoadingHint.hide();
					if (result.errorCode == 0) {
						FEToast.showMessage("取消成功");
						EventBus.getDefault().post(new CollectionEvent(200));
						setFavoriteId("");  // 记得更新一下状态
						return;
					}
					FEToast.showMessage(result.errorMessage);
				}, exception -> {
					LoadingHint.hide();
					FEToast.showMessage("取消收藏失败，请稍后重试！");
				});
	}

	protected String getBusinessId() {
		return null;
	}

	protected String getFavoriteId() {
		return null;
	}

	protected void setFavoriteId(String favoriteId) { }

	protected String getAddType() {
		return null;
	}

	protected String getRemoveType() {
		return null;
	}

	protected String getUserId() {
		return null;
	}

	protected String getSendTime() {
		return null;
	}

	protected String getTitle() {
		return null;
	}


	protected abstract int getReplyType();

	public static class Builder {

		private Context context;
		private ParticularContract.IView particularView;
		private ParticularIntent particularIntent;

		public Builder(Context context) {
			this.context = context;
		}

		public Builder setParticularView(ParticularContract.IView particularView) {
			this.particularView = particularView;
			return this;
		}

		public Builder setStartIntent(Intent intent) {
			this.particularIntent = new ParticularIntent(particularView.getContext(), intent);
			return this;
		}


		public ParticularPresenter build() {
			if (particularIntent.getParticularType() == -1) {
				throw new IllegalArgumentException("You must set the particular type before start this activity.");
			}

			ParticularPresenter presenter = null;
			switch (particularIntent.getParticularType()) {
				case PARTICULAR_NEWS:
				case PARTICULAR_ANNOUNCEMENT:
					presenter = new NewsParticularPresenter(this);
					break;
				case PARTICULAR_MEETING:
					presenter = new MeetingParticularPresenter(this);
					break;
				case PARTICULAR_COLLABORATION:
					presenter = new CollaborationParticularPresenter(this);
					break;
				case PARTICULAR_WORK_PLAN:
					presenter = new WorkPlanParticularPresenter(this);
					break;
			}
			return presenter;
		}
	}

	public class HeadVO {

		public String sendUserId;
		public String sendUserName;
		public String sendTime;
		public String title;
		public String startTime;
		public String endTime;
		public String nowNodeName;
		public List<User> receiverUsers;
		public List<User> copyToUsers;
		public List<User> noticeToUsers;
	}

	public class BottomMenuVO {

		public String buttonText1;
		public String buttonText2;
		public String buttonText3;
		public String buttonText4;
	}

	public class FabVO {

		public boolean hasAttachment;
		public boolean hasReply;            // In News / Announcement this field is related news.
		public boolean hasDuDu;
		public String duReplyUserIds;
	}

	public class MeetingAttendUserVO {

		public String master;
		public String attendNumber;
		public String notAttendNumber;
		public String considerNumber;
		public String notDealNumber;
		public String signInNumber;
		public List<MeetingAttendUser> meetingAttendUser;
	}
}
