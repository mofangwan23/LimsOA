package cn.flyrise.feep.location.model;

import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.feep.commonality.bean.FEListInfo;
import cn.flyrise.feep.commonality.bean.FEListItem;
import cn.flyrise.feep.commonality.util.ListDataProvider;
import cn.flyrise.feep.core.common.X.RequestType;
import cn.flyrise.feep.location.contract.LocationHistoryRequstContract;
import java.util.ArrayList;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2018-1-24-9:44.
 */

public class LocationHistoryModel implements LocationHistoryRequstContract, ListDataProvider.OnListDataResponseListener {

	private int mPage = 1;
	private final ListDataProvider mProvider;

	private FEListInfo mCurrentItemInfo = new FEListInfo();
	private int mRequestType;
	private RequstListener mListener;
	private boolean isSignCalendar;//考勤日历

	public LocationHistoryModel(Context context, boolean isSignCalendar, int requestType, RequstListener listener) {
		this.isSignCalendar = isSignCalendar;
		this.mRequestType = requestType;
		this.mListener = listener;
		mProvider = new ListDataProvider(context);
		mProvider.setOnListResposeListener(this);
	}

	public void cancleRquestData() {
		if (mCurrentItemInfo != null) {
			mCurrentItemInfo.clearListDatas();
		}
		mPage = 1;
	}

	@Override
	public void request(String date, int page, String userId) {
		mProvider.request(mRequestType, date, page, userId);
	}

	public void request(String date, int page, String userId, int perPageNums) {
		mProvider.request(mRequestType, date, page, userId, perPageNums);
	}

	@Override
	public void request(String date, int page, String userId, int perPageNums, int sumId) {
		mProvider.request(mRequestType, date, page, userId, perPageNums, sumId);
	}

	@Override
	public void onSuccess(List<FEListItem> listItems, int totalNums, int requestType, boolean isSearch) {
		mCurrentItemInfo.setRequestType(requestType);
		mCurrentItemInfo.setTotalNums(totalNums);
		// 如果是历史记录的列表，需要重新组装列表
		if (requestType == RequestType.LocationHistory) {
			mListener.refreshNums(totalNums);
			if (listItems.size() != 0 && !isSignCalendar) {
				listItems = getLocationItem(listItems, mCurrentItemInfo.getListItems());
			}
		}
		if (mPage == 1) {
			mCurrentItemInfo.clearListDatas();
		}
		mCurrentItemInfo.addAllListItem(listItems);
		refreshListView();
	}

	@Override
	public void onFailure(Throwable error, String content, boolean isSearch) {
		if (mPage > 1) {
			mPage--;
		}
		refreshListView();
	}


	private List<FEListItem> getLocationItem(List<FEListItem> listItems, List<FEListItem> berfItems) {
		if (listItems == null || listItems.size() <= 0) {
			return null;
		}
		ArrayList<FEListItem> tempList = new ArrayList<>();

		FEListItem berfItem = (berfItems != null && berfItems.size() > 0) && mPage != 1 ? berfItems.get(berfItems.size() - 1) : null;

		FEListItem item;
		for (int i = 0; i < listItems.size(); i++) {
			item = listItems.get(i);
			if (isSetTempList(i, berfItem, item, listItems)) {
				setTempList(item, tempList);
			}
			tempList.add(item);
		}
		return tempList;
	}

	//是否添加“日期头”
	private boolean isSetTempList(int i, FEListItem berfItem, FEListItem item, List<FEListItem> listItems) {
		if (berfItem == null) {
			if (i == 0 || (!TextUtils.equals(listItems.get(i - 1).getDate(), item.getDate()) && !item.isOneDay())) {
				return true;
			}
			else if (!TextUtils.equals(listItems.get(i - 1).getDate(), item.getDate()) && !item.isOneDay()) {
				return true;
			}
		}
		else {
			if (i == 0) {
				if (!TextUtils.equals(berfItem.getDate(), item.getDate()) && !item.isOneDay()) {
					return true;
				}
			}
			else if (!TextUtils.equals(listItems.get(i - 1).getDate(), item.getDate()) && !item.isOneDay()) {
				return true;
			}
		}
		return false;
	}

	private void setTempList(FEListItem item, List<FEListItem> tempList) {
		final FEListItem two = new FEListItem();
		two.setOneDay();
		two.setDate(item.getDate());
		two.setWhatDay(item.getWhatDay());
		two.setImageHerf(item.getImageHerf());
		tempList.add(two);
	}

	/**
	 * 刷新列表需要做的事
	 */
	private void refreshListView() {
		if (LoadingHint.isLoading()) {
			LoadingHint.hide();
		}
		mListener.refreshHistoryData(mCurrentItemInfo, mPage);
	}

}
