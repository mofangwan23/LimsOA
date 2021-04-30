package cn.flyrise.feep.robot.module;

import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.common.X.RequestType;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.robot.bean.FeSearchMessageItem;
import cn.flyrise.feep.robot.bean.RobotListDataItem;
import cn.flyrise.feep.robot.bean.RobotListRequest;
import cn.flyrise.feep.robot.bean.RobotListResponse;
import cn.flyrise.feep.robot.bean.RobotListTable;
import cn.flyrise.feep.robot.bean.RobotWorkPlanListItemBean;
import cn.flyrise.feep.robot.contract.RobotDataLoaderContract;
import java.util.ArrayList;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期：2017-7-25.
 * 请求计划列表
 */

public class FeWorkPlanLoadData implements RobotDataLoaderContract {

	private AddressBook addressBook;

	private FeSearchMessageListener listener;

	@Override
	public void setContext(Context context) {

	}

	@Override
	public void setListener(FeSearchMessageListener listener) {
		this.listener = listener;
	}

	@Override
	public void setRequestType(int requestType) {

	}

	@Override
	public void setAddressBoook(AddressBook addressBoook) {
		this.addressBook = addressBoook;
	}

	@Override
	public void requestMessageList(String searchKey) {

	}

	@Override
	public void requestWorkPlanList(final String userID) {
		RobotListRequest listRequest = new RobotListRequest();
		listRequest.requestType = RequestType.OthersWorkPlan + "";
		listRequest.page = String.valueOf(page);
		listRequest.perPageNums = String.valueOf(perPageNums);
		listRequest.id = userID;
		FEHttpClient.getInstance().post(listRequest, new ResponseCallback<RobotListResponse>() {
			@Override
			public void onCompleted(RobotListResponse listResponse) {
				if (TextUtils.equals(listResponse.getErrorCode(), "0")) {
					List<RobotWorkPlanListItemBean> data = changeDataToListItemBean(listResponse);
					setFeSearchMessageData(data);
				}
				else {
					if (listener != null) {
						listener.onError(DATA_ERROR);
					}
				}
			}
		});
	}

	private void setFeSearchMessageData(List<RobotWorkPlanListItemBean> data) {
		if (CommonUtil.isEmptyList(data)) {
			if (listener != null) {
				listener.onError(DATA_NULL);
			}
			return;
		}
		List<FeSearchMessageItem> feSearchMessageItems = new ArrayList<>();
		FeSearchMessageItem messageItem;
		for (RobotWorkPlanListItemBean itemBean : data) {
			if (itemBean == null) {
				continue;
			}
			messageItem = new FeSearchMessageItem();
			messageItem.sendUser = itemBean.sendUser;
			messageItem.sendUserId = itemBean.sendUserId;
			messageItem.sendTime = itemBean.sendTime;
			messageItem.isNew = itemBean.isNews;
			messageItem.title = itemBean.title;
			messageItem.status = itemBean.status;
			messageItem.messageId = itemBean.id;
			messageItem.BusinessId = itemBean.id;
			messageItem.moduleItemType = Func.Plan;
			if (addressBook != null) {
				messageItem.sendUserImg = addressBook.imageHref;
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

	public static ArrayList<RobotWorkPlanListItemBean> changeDataToListItemBean(RobotListResponse listResponse) {
		ArrayList<RobotWorkPlanListItemBean> list = new ArrayList<>();
		if (listResponse != null && listResponse.table != null) {
			RobotListTable listtable = listResponse.table;
			List<List<RobotListDataItem>> tableRows = listtable.tableRows;
			int lenght = tableRows.size();
			for (int i = 0; i < lenght; i++) {
				List<RobotListDataItem> dataItems = tableRows.get(i);
				RobotWorkPlanListItemBean itemBean = new RobotWorkPlanListItemBean();
				for (RobotListDataItem dataItem : dataItems) {
					String value = dataItem.value;
					switch (dataItem.name) {
						case "id":
							itemBean.id = value;
							break;
						case "isNews":
							itemBean.isNews = TextUtils.equals(value, "true");
							break;
						case "title":
							itemBean.title = value;
							break;
						case "sendUser":
							itemBean.sendUser = value;
							break;
						case "sendTime":
							itemBean.sendTime = value;
							break;
						case "sectionName":
							itemBean.sectionName = value;
							break;
						case "status":
							itemBean.status = value;
							break;
						case "UserId":
							itemBean.sendUserId = value;
							break;
					}
				}
				list.add(itemBean);
			}
		}
		return list;
	}

}
