package cn.flyrise.feep.robot.module;

import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.robot.bean.FeSearchMessageItem;
import cn.flyrise.feep.robot.bean.RobotFEListItem;
import cn.flyrise.feep.robot.contract.RobotDataLoaderContract;
import java.util.ArrayList;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期：2017-7-24.
 * 请求审批列表
 */

public class FeMessageLoadData implements RobotDataLoaderContract {

	private String searchKey;
	private RobotListDataProvider provider;
	private int mRequestType;

	private FeSearchMessageListener listener;

	private AddressBook addressBook;

	@Override
	public void setContext(Context context) {
		this.provider = new RobotListDataProvider(context);
		this.provider.setOnListResposeListener(responseListener);
	}

	@Override
	public void setListener(FeSearchMessageListener listener) {
		this.listener = listener;
	}

	@Override
	public void setRequestType(int requestType) {
		this.mRequestType = requestType;
	}

	@Override
	public void setAddressBoook(AddressBook addressBoook) {
		this.addressBook = addressBoook;
	}

	@Override
	public void requestMessageList(String searchKey) {
		this.searchKey = searchKey;
		if (mRequestType == -1) {
			return;
		}
		provider.request(mRequestType, page, searchKey);
	}

	@Override
	public void requestWorkPlanList(String userID) {

	}

	private RobotListDataProvider.OnListDataResponseListener responseListener = new RobotListDataProvider.OnListDataResponseListener() {
		@Override
		public void onFailure(Throwable error, String content, boolean isSearch) {
			if (listener != null) {
//                listener.onError(DATA_ERROR);
			}
		}

		@Override
		public void onSuccess(List<RobotFEListItem> listItems, int totalNums, int requestType, boolean isSearch) {
			if (TextUtils.isEmpty(searchKey)) {
				listener.onError(DATA_ERROR);
			}
			else {
				setFeSearchMessageData(listItems);
			}
		}
	};

	private void setFeSearchMessageData(List<RobotFEListItem> listItems) {
		if (CommonUtil.isEmptyList(listItems)) {
			if (listener != null) {
				listener.onError(DATA_NULL);
			}
			return;
		}
		List<FeSearchMessageItem> feSearchMessageItems = new ArrayList<>();
		FeSearchMessageItem messageItem;
		for (RobotFEListItem itemBean : listItems) {
			if (itemBean == null) {
				continue;
			}
			messageItem = new FeSearchMessageItem();
			messageItem.sendUser = itemBean.getSendUser();
			messageItem.sendTime = itemBean.getSendTime();
			messageItem.sendUserImg = itemBean.getSendUserImg();
			messageItem.isNew = itemBean.isNews();
			messageItem.title = itemBean.getTitle();
			messageItem.messageId = itemBean.getId();
			messageItem.BusinessId = itemBean.getId();
			messageItem.ListRequestType = mRequestType;
			messageItem.moduleItemType = mRequestType;
			if (addressBook != null) {
				messageItem.sendUserId = addressBook.userId;
				if (TextUtils.isEmpty(itemBean.getSendUserImg()) || TextUtils.isEmpty(itemBean.getSendUser())) {
					messageItem.sendUserId = addressBook.imageHref;
					messageItem.sendUser = addressBook.name;
				}
			}
			feSearchMessageItems.add(messageItem);
		}
		if (feSearchMessageItems.size() == 0) {
			if (listener != null) {
				listener.onError(DATA_NULL);
			}
			return;
		}
		if (listener != null) {
			listener.onRobotModuleItem(feSearchMessageItems);
		}
	}
}
