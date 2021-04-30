package cn.flyrise.feep.commonality;

import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.feep.commonality.bean.FEListItem;
import cn.flyrise.feep.commonality.util.ListDataProvider;
import cn.flyrise.feep.core.common.X.RequestType;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.utils.Patches;
import java.util.List;

/**
 * Created by KLC on 2017/1/5.
 */

class MessageSearchPresenter implements MessageSearchContract.Presenter {

	private int mTotalNum = 0;
	private int mPageSize = 20;
	private int mPageNumber;
	private String searchKey;
	private String lastSearchKey;

	private ListDataProvider provider;
	private MessageSearchActivity view;
	private int requestType;

	MessageSearchPresenter(MessageSearchActivity view, Context context, int requestType) {
		this.view = view;
		this.provider = new ListDataProvider(context);
		this.provider.setPerPageNums(mPageSize);
		this.requestType = requestType;
		this.provider.setOnListResposeListener(responseListener);
	}

	public void refreshListData(String searchKey) {
		this.searchKey = searchKey;
		refreshListData();
	}

	@Override
	public void onStart() {

	}

	@Override
	public void refreshListData() {
		provider.request(requestType, mPageNumber = 1, searchKey);
	}

	@Override
	public void loadMoreData() {
		provider.request(requestType, ++mPageNumber, searchKey);
	}

	@Override
	public void cancelLoad() {
		searchKey = null;
	}

	@Override
	public boolean hasMoreData() {
		if (FunctionManager.hasPatch(Patches.PATCH_NO_REFRESH_LIST)
				&& requestType == RequestType.ToDo) {
			return mTotalNum > mPageSize;
		}
		return mTotalNum > mPageNumber * mPageSize;
	}

	private ListDataProvider.OnListDataResponseListener responseListener = new ListDataProvider.OnListDataResponseListener() {
		@Override
		public void onFailure(Throwable error, String content, boolean isSearch) {
			if (mPageNumber == 1) {
//				if (!TextUtils.equals(lastSearchKey, searchKey)) {
				view.refreshListFail();
				view.setCanPullUp(hasMoreData());
//				}
			}
			else {
				view.loadMoreListFail();
				mPageNumber--;
			}
		}

		@Override
		public void onSuccess(List<FEListItem> listItems, int totalNums, int requestType, boolean isSearch) {
			if (!CommonUtil.isEmptyList(listItems) && listItems.get(listItems.size() - 1) != null) {
				view.messageId = listItems.get(listItems.size() - 1).getId();
			}
			if (!TextUtils.isEmpty(searchKey)) {
				mTotalNum = totalNums;
				if (mPageNumber == 1) {
					view.refreshListData(listItems);
					lastSearchKey = searchKey;
				}
				else {
					view.loadMoreListData(listItems);
				}
			}
		}
	};
}
