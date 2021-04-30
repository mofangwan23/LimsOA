package cn.flyrise.feep.main.message.toberead;

import android.content.Intent;
import android.text.TextUtils;

import cn.flyrise.feep.core.base.views.adapter.BaseMessageRecyclerAdapter;
import com.dk.view.badge.BadgeUtil;

import cn.flyrise.feep.FEApplication;
import cn.flyrise.feep.commonality.util.IntentMessageDetail;
import cn.flyrise.feep.main.message.BaseMessageAdapter;
import cn.flyrise.feep.main.message.MessageListAdapter;
import cn.flyrise.feep.main.message.MessageVO;
import cn.flyrise.feep.main.message.MessageFragment;
import cn.flyrise.feep.meeting7.ui.MeetingDetailActivity;
import cn.flyrise.feep.notification.NotificationController;

/**
 * @author ZYP
 * @since 2017-03-30 17:05
 */
public class ToBeReadMessageFragment extends MessageFragment<MessageVO> {

	private boolean isRead; // true : 已阅; false : 待阅
	private boolean isSeparate;  // true : 待阅已阅新的机制，两者区分开来的
	private MessageListAdapter mAdapter;

	public static ToBeReadMessageFragment newInstance(boolean isRead) {
		ToBeReadMessageFragment instance = new ToBeReadMessageFragment();
		instance.setRead(isRead);
		return instance;
	}

	private void setRead(boolean isRead) {
		this.isRead = isRead;
	}

	public void setSeparate(boolean separate) {
		isSeparate = separate;
	}

	@Override
	protected void bindListener() {
		super.bindListener();
		mAdapter.setOnMessageClickListener((messageVO, position) -> {
			if (IntentMessageDetail.isShowMessage(messageVO, getActivity())) {            // 移除，添加
				if (TextUtils.equals(messageVO.getReaded(), "false")) {
					messageVO.setReaded();
					NotificationController.messageReaded(getActivity(), messageVO.getMessageID());
					((ToBeReadMessageActivity) getActivity()).removeReadMessage(messageVO);
					FEApplication feApplication = (FEApplication) getActivity().getApplicationContext();
					int num = feApplication.getCornerNum() - 1;
					BadgeUtil.setBadgeCount(getActivity(), num);//角标
					feApplication.setCornerNum(num);
				}
				new IntentMessageDetail(getActivity(), messageVO).startIntent();
			}
			else {
				mAdapter.updateMessageState(position);
				NotificationController.messageReaded(getActivity(), messageVO.getMessageID());
			}
		});

		this.mCurrentPage = 1;
		if (!isRead) {
			requestMessage(mCurrentPage, true);
		}
	}

	@Override
	public String getMessageTitle(Object clickObject) {
		return ((MessageVO) clickObject).getTitle();
	}

	public void refreshMessage() {
		mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(true));
		requestMessage(mCurrentPage = 1, true);
	}

	@Override
	protected boolean isNeedLoadMore() {
		return isRead || isSeparate;
	}

	@Override
	protected boolean isNeedShowRefresh() {
		return true;
	}

	@Override
	public BaseMessageAdapter<MessageVO> getMessageAdapter() {
		if (mAdapter == null) {
			mAdapter = new MessageListAdapter();
		}
		return mAdapter;
	}

	@Override
	public boolean isLoaderMore() {
		return mAdapter.needAddFooter(mTotalSize);
	}

	@Override
	public void requestMessage(int pageNumber, boolean isRefresh) {
		((ToBeReadMessageActivity) getActivity()).requestMessage(pageNumber, isRefresh, isRead);
	}

	@Override
	public void setTotalSize(int totalSize) {
		super.setTotalSize(totalSize);
		mIsLoading = false;
		if (isRead || isSeparate) {
			if (mAdapter.needAddFooter(totalSize)) {
				mAdapter.setLoadState(BaseMessageRecyclerAdapter.LOADING);
			}
			else {
				mAdapter.setLoadState(BaseMessageRecyclerAdapter.LOADING_COMPLETE);
			}
		}
	}

	public void onLoadFailed() {
		mIsLoading = false;
		if (mCurrentPage == 1) {
			hideRefreshLoading();
		}
		else {
			mCurrentPage--;
			scrollLastItem2Bottom();
		}
	}

	public void addMessage(MessageVO messageVO) {
		mAdapter.addMessageVO(messageVO);
	}

	public void removeMessage(MessageVO messageVO) {
		mAdapter.removeMessageVO(messageVO);
	}

	public int getDataSourceCount() {
		return mAdapter.getDataSourceCount();
	}

	public void setExtraCount(int extraCount) {
		mAdapter.setExtraCount(extraCount);
	}

}
