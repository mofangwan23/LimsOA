package cn.flyrise.feep.main;

import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.android.protocol.entity.MessageListRequest;
import cn.flyrise.android.protocol.entity.MessageListResponse;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.request.MessagesResponse;
import cn.flyrise.feep.core.network.request.NoticesManageRequest;
import cn.flyrise.feep.main.message.MessageVO;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chatui.utils.IMHuanXinHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2017-04-01 13:40
 */
public class NewMainMessagePresenter implements NewMainMessageContract.IPresenter {

	private NewMainMessageContract.IView mNewMainMessageView;
	private Context mContext;
	private boolean isFirstQuery = true;
	private List<String> unReadCircleMessageIds = new ArrayList<>();

	NewMainMessagePresenter(Context context, NewMainMessageContract.IView newMainMessageView) {
		this.mContext = context;
		this.mNewMainMessageView = newMainMessageView;
	}

	@Override
	public void start() {
		if (isFirstQuery) {
			this.mNewMainMessageView.showLoading();
		}
		fetchMessageList();
		fetchConversationList();
	}

	@Override
	public void fetchMessageList() {
		Observable
				.unsafeCreate((Subscriber<? super List<MessageVO>> f) ->
						FEHttpClient.getInstance().post(new MessageListRequest(), new ResponseCallback<MessageListResponse>(mContext) {
							@Override
							public void onCompleted(MessageListResponse response) {
								if (response == null || !TextUtils.equals(response.getErrorCode(), "0")) {
									f.onError(new NullPointerException("Request message list failed."));
									return;
								}

								List<MessageVO> messageVOs = response.getResults();
								if (CommonUtil.isEmptyList(messageVOs)) {
									f.onError(new NullPointerException("Request message success, but not result."));
									return;
								}

								f.onNext(messageVOs);
							}

							@Override
							public void onFailure(RepositoryException repositoryException) {
								super.onFailure(repositoryException);
								f.onError(repositoryException == null
										? new NullPointerException("Request message failed.")
										: repositoryException.exception());
							}
						}))
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(messageVOs -> {
					if (isFirstQuery) {
						mNewMainMessageView.hideLoading();
					}
					isFirstQuery = false;
					mNewMainMessageView.onMessageLoadSuccess(messageVOs);
				}, exception -> {
					if (isFirstQuery) {
						mNewMainMessageView.hideLoading();
					}
					isFirstQuery = false;
				});
	}

	@Override
	public void fetchConversationList() {
		if (IMHuanXinHelper.getInstance().isSwitchUser()) {
			return;
		}
		getAllConversation();
	}

	@Override
	public void allMessageListRead(final String categorys) {
		NoticesManageRequest request = new NoticesManageRequest();
		request.setUserId(CoreZygote.getLoginUserServices().getUserId());
		request.setCategory(categorys);
		Observable
				.unsafeCreate(f ->
						FEHttpClient.getInstance().post(request, new ResponseCallback<MessagesResponse>(mContext) {
							@Override
							public void onCompleted(MessagesResponse response) {
								if (response == null || !TextUtils.equals(response.getErrorCode(), "0")) {
									f.onError(new NullPointerException());
									return;
								}
								f.onNext(response.getErrorCode());
							}

							@Override
							public void onFailure(RepositoryException repositoryException) {
								super.onFailure(repositoryException);
								f.onError(new NullPointerException());
							}
						})).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(object -> {
					//标记全部为已读
					if (TextUtils.equals(categorys, NewMainMessageContract.IView.ALL_MESSAGE_READ)) {
						EMClient.getInstance().chatManager().markAllConversationsAsRead();
						fetchConversationList();
					}
					fetchMessageList();
				}, throwable -> fetchMessageList());
	}

	@Override
	public void requestCircleMessageList(int totalCount, int pageNumber) {
		Observable
				.unsafeCreate((Subscriber<? super List<MessageVO>> f) -> {
					MessageListRequest request = new MessageListRequest();
					request.setCategory("2");
					request.setPerPageNums("20");
					request.setPage(String.valueOf(pageNumber));

					FEHttpClient.getInstance().post(request, new ResponseCallback<MessageListResponse>() {
						@Override
						public void onCompleted(MessageListResponse response) {
							if (!TextUtils.equals(response.getErrorCode(), "0")) {
								f.onError(new NullPointerException("circle message error"));
								return;
							}
							f.onNext(response.getResults());
						}

						@Override
						public void onFailure(RepositoryException repositoryException) {

						}
					});
				}).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(messageVOs -> {
					for (MessageVO messageVO : messageVOs) {
						if (unReadCircleMessageIds.size() >= totalCount) {
							mNewMainMessageView.onCircleMessageIdListSuccess(unReadCircleMessageIds);
							break;
						}
						else if (TextUtils.equals(messageVO.getReaded(), "false")) {
							unReadCircleMessageIds.add(messageVO.getMessageID());
						}
					}
				}, error -> {

				});
	}

	@Override
	public void circleMessageListRead(List<String> messageIds) {
		if (messageIds == null) return;
		Observable
				.unsafeCreate(f -> {
					final NoticesManageRequest reqContent = new NoticesManageRequest();
					reqContent.setMsgIds(messageIds);
					reqContent.setUserId(CoreZygote.getLoginUserServices().getUserId());
					FEHttpClient.getInstance().post(reqContent, new ResponseCallback<MessageListResponse>() {
						@Override
						public void onCompleted(MessageListResponse response) {
							if (!TextUtils.equals(response.getErrorCode(), "0")) {
								f.onError(new NullPointerException("cricle message error"));
								return;
							}
							f.onNext(null);
							f.onCompleted();
						}

						@Override
						public void onFailure(RepositoryException repositoryException) {
							f.onError(new NullPointerException("cricle message error"));
						}
					});
				}).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(f -> {
					mNewMainMessageView.onCircleMessageReadSuccess();
				}, error -> {

				});

	}

	private void getAllConversation() {
		Observable
				.unsafeCreate((OnSubscribe<List<EMConversation>>) subscriber -> {
					Collection<EMConversation> collection = EMClient.getInstance().chatManager().getAllConversations().values();
					List<EMConversation> conversationList = new ArrayList<>(collection);
					Collections.sort(conversationList, conversationSort);
					subscriber.onNext(conversationList);
				}).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(conversationLists -> mNewMainMessageView.onConversationLoadSuccess(conversationLists), exception -> {
					exception.printStackTrace();
				});
	}


	private Comparator conversationSort = (Comparator<EMConversation>) (lhs, rhs) -> {
		boolean lhsTop = !TextUtils.isEmpty(lhs.getExtField());
		boolean rhsTop = !TextUtils.isEmpty(rhs.getExtField());
		if (lhsTop && !rhsTop) {
			return -1;
		}
		if (!lhsTop && rhsTop) {
			return 1;
		}
		EMMessage lhsLastMsg = lhs.getLastMessage();
		EMMessage rhsLastMsg = rhs.getLastMessage();
		if (lhsLastMsg == null && rhsLastMsg == null) {
			return 0;
		}
		if (lhsLastMsg == null) {
			return 1;
		}
		if (rhsLastMsg == null) {
			return -1;
		}
		if (lhsLastMsg.getMsgTime() == rhsLastMsg.getMsgTime()) {
			return 0;
		}
		if (lhsLastMsg.getMsgTime() < rhsLastMsg.getMsgTime()) {
			return 1;
		}
		return -1;
	};


}
