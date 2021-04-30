package cn.flyrise.feep.main.message.toberead;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.hyphenate.easeui.model.Message;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.flyrise.android.protocol.entity.MessageListRequest;
import cn.flyrise.android.protocol.entity.MessageListResponse;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.main.message.BaseMessageActivity;
import cn.flyrise.feep.main.message.MessageConstant;
import cn.flyrise.feep.main.message.MessageVO;
import cn.squirtlez.frouter.annotations.Route;

/**
 * @author ZYP
 * @since 2017-03-30 14:40 FE 666 版本：待阅消息
 */
@Route("/message/toberead")
public class ToBeReadMessageActivity extends BaseMessageActivity {

	private boolean needRefresh;
	private static final int[] TAB_ICONS = {R.drawable.icon_msg_unread_selector, R.drawable.icon_msg_read_selector};
	public static final String PAGE_SIZE = "20";
	private boolean isLoading = false;                      // 是否在加载
	private boolean isFirstLoad = true;

	private ToBeReadMessageFragment mReadMessageFragment;   // 已阅
	private ToBeReadMessageFragment mUnReadMessageFragment; // 待阅


	public static void start(Activity activity) {
		Intent intent = new Intent(activity, ToBeReadMessageActivity.class);
		activity.startActivity(intent);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		toolbar.setTitle(R.string.message_nofity_title);
	}

	@Override
	protected List<Fragment> getFragments() {
		return Arrays.asList(mUnReadMessageFragment = ToBeReadMessageFragment.newInstance(false),
				mReadMessageFragment = ToBeReadMessageFragment.newInstance(true));
	}

	@Override
	protected List<String> getTabTexts() {
		return Arrays.asList("待阅", "已阅");
	}

	@Override
	protected int getTabIcon(int position) {
		return TAB_ICONS[position];
	}

	@Override
	protected void onTabClick(String name) {
		if (needRefresh) {
			mReadMessageFragment.refreshMessage();
			needRefresh = false;
		}
	}

	public void requestMessage(int pageNumber, boolean isRefresh, boolean isRead) {
		if (isLoading) {
			return;
		}

		isLoading = true;
		MessageListRequest request = new MessageListRequest();
		request.setCategory(MessageConstant.NOTIFY);
		request.setPerPageNums(PAGE_SIZE);
		request.setPage(String.valueOf(pageNumber));
		if (isRefresh) {
			request.setMsgNums("0");
		}
		else {
			request.setMsgNums(isRead ? String.valueOf(mReadMessageFragment.getDataSourceCount())
					: String.valueOf(mUnReadMessageFragment.getDataSourceCount()));
		}
		request.setMessageType(isRead ? MessageListRequest.MESSAGETYPE_READ : MessageListRequest.MESSAGETYPE_UNREAD);

		FEHttpClient.getInstance().post(request, new ResponseCallback<MessageListResponse>() {
			@Override
			public void onCompleted(MessageListResponse response) {
				isLoading = false;
				if (!TextUtils.equals(response.getErrorCode(), "0")) {
					this.onFailure(null);
					return;
				}
				if (response.getMessageType() == -1) {
					filterMessageList(CommonUtil.parseInt(response.getTotalNums()), response.getResults(), isRefresh);
				}
				else {
					setMessageToFragment(CommonUtil.parseInt(response.getTotalNums()), response.getResults(), isRefresh, isRead);
					if (isFirstLoad) {
						requestMessage(pageNumber, isRefresh, !isRead);
						isFirstLoad = false;
					}
				}
			}

			@Override
			public void onFailure(RepositoryException repositoryException) {
				isLoading = false;
				mReadMessageFragment.onLoadFailed();
				mUnReadMessageFragment.onLoadFailed();
			}
		});
	}

	public void removeReadMessage(MessageVO messageVO) {
		new Handler().postDelayed(() -> mUnReadMessageFragment.removeMessage(messageVO), 200);
		needRefresh = true;
	}

	private void filterMessageList(int totalSize, List<MessageVO> messageVOs, boolean isRefresh) {
		mReadMessageFragment.setExtraCount(mUnReadMessageFragment.getDataSourceCount());
		mReadMessageFragment.setTotalSize(totalSize);
		mUnReadMessageFragment.setTotalSize(totalSize);

		List<MessageVO> readMessages = new ArrayList<>();
		List<MessageVO> unReadMessages = new ArrayList<>();
		for (MessageVO messageVO : messageVOs) {
			if (TextUtils.equals(messageVO.getReaded(), "false")) {
				unReadMessages.add(messageVO);
			}
			else {
				readMessages.add(messageVO);
			}
		}

		if (isRefresh) {
			mReadMessageFragment.getMessageAdapter().setDataSource(readMessages);
			mUnReadMessageFragment.getMessageAdapter().setDataSource(unReadMessages);
		}
		else {
			mReadMessageFragment.getMessageAdapter().addDataSource(readMessages);
			mUnReadMessageFragment.getMessageAdapter().addDataSource(unReadMessages);
		}

		mReadMessageFragment.hideRefreshLoading();
		mUnReadMessageFragment.hideRefreshLoading();
	}

	private void setMessageToFragment(int totalSize, List<MessageVO> messageVOs, boolean isRefresh, boolean isRead) {
		ToBeReadMessageFragment fragment = isRead ? mReadMessageFragment : mUnReadMessageFragment;
		fragment.setSeparate(true);
		if (isRefresh) {
			fragment.getMessageAdapter().setDataSource(messageVOs);
		}
		else {
			fragment.getMessageAdapter().addDataSource(messageVOs);
		}
		fragment.setTotalSize(totalSize);

		fragment.hideRefreshLoading();
	}
}
