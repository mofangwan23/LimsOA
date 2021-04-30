package cn.flyrise.feep.main.message.other;

import android.text.TextUtils;
import cn.flyrise.android.protocol.entity.MessageListRequest;
import cn.flyrise.android.protocol.entity.MessageListResponse;
import cn.flyrise.feep.FEApplication;
import cn.flyrise.feep.commonality.util.IntentMessageDetail;
import cn.flyrise.feep.cordova.utils.CordovaShowUtils;
import cn.flyrise.feep.core.base.views.adapter.BaseMessageRecyclerAdapter;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.main.message.BaseMessageAdapter;
import cn.flyrise.feep.main.message.MessageFragment;
import cn.flyrise.feep.main.message.MessageListAdapter;
import cn.flyrise.feep.main.message.MessageVO;
import cn.flyrise.feep.notification.NotificationController;
import com.dk.view.badge.BadgeUtil;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-03-31 16:42
 */
public class SystemMessageFragment extends MessageFragment<MessageVO> {

	private String mCategory;
	public static final String PAGE_SIZE = "20";
	private MessageListAdapter mAdapter;

	public static SystemMessageFragment newInstance(String category) {
		SystemMessageFragment instance = new SystemMessageFragment();
		instance.setCategory(category);
		return instance;
	}

	private void setCategory(String category) {
		this.mCategory = category;
	}

	@Override
	protected void bindListener() {
		super.bindListener();
		mAdapter.setOnMessageClickListener((messageVO, position) -> {
			if (IntentMessageDetail.isShowMessage(messageVO, getActivity())) {
				if (TextUtils.equals(messageVO.getReaded(), "false")) {
					mAdapter.updateMessageState(position);
					NotificationController.messageReaded(getActivity(), messageVO.getMessageID());

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
		requestMessage(mCurrentPage = 1, true);
	}

	@Override
	public String getMessageTitle(Object clickObject) {
		return ((MessageVO) clickObject).getTitle();
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
		mIsLoading = true;
		MessageListRequest request = new MessageListRequest();
		request.setCategory(mCategory);
		request.setPerPageNums(PAGE_SIZE);
		request.setPage(String.valueOf(pageNumber));

		FEHttpClient.getInstance().post(request, new ResponseCallback<MessageListResponse>() {
			@Override
			public void onCompleted(MessageListResponse response) {
				mIsLoading = false;
				mAdapter.setLoadState(BaseMessageRecyclerAdapter.LOADING_COMPLETE);
				hideRefreshLoading();
				if (!TextUtils.equals(response.getErrorCode(), "0")) {
					this.onFailure(null);
					return;
				}

				setTotalSize(CommonUtil.parseInt(response.getTotalNums()));
				List<MessageVO> messageVOs = response.getResults();
				if (isRefresh) {
					mAdapter.setDataSource(messageVOs);
				}
				else {
					mAdapter.addDataSource(messageVOs);
				}

				String notificationMessageId = getActivity() == null || getActivity().getIntent() == null ? ""
						: getActivity().getIntent().getStringExtra(CordovaShowUtils.MSGID);

				if (!TextUtils.isEmpty(notificationMessageId)) {
					for (MessageVO messageVO : messageVOs) {
						if (TextUtils.equals(messageVO.getMessageID(), notificationMessageId)) {
							if (TextUtils.equals(messageVO.getReaded(), "false")) {
								NotificationController.messageReaded(getActivity(), messageVO.getMessageID());
								if (IntentMessageDetail.isShowMessage(messageVO, getActivity())) {
									new IntentMessageDetail(getActivity(), messageVO).startIntent();
								}
								messageVO.setReaded();
								mAdapter.notifyDataSetChanged();
								break;
							}
						}
					}
				}
			}

			@Override
			public void onFailure(RepositoryException repositoryException) {
				mIsLoading = false;
				hideRefreshLoading();
				if (mCurrentPage > 1) {
					mCurrentPage--;
				}
				mAdapter.setLoadState(BaseMessageRecyclerAdapter.LOADING_COMPLETE);
			}
		});
	}

}
