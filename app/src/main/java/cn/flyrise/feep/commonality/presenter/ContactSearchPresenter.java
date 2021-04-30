package cn.flyrise.feep.commonality.presenter;

import cn.flyrise.android.library.view.addressbooklistview.been.AddressBookListItem;
import cn.flyrise.android.protocol.entity.AddressBookRequest;
import cn.flyrise.android.protocol.entity.AddressBookResponse;
import cn.flyrise.android.protocol.model.AddressBookItem;
import cn.flyrise.feep.core.base.component.FEListContract;
import cn.flyrise.feep.core.base.component.FEListContract.View;
import cn.flyrise.feep.core.common.X.AddressBookFilterType;
import cn.flyrise.feep.core.common.X.AddressBookType;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by klc on 2017/9/6.
 * 联系人搜索P
 */

public class ContactSearchPresenter implements FEListContract.Presenter {

	private FEListContract.View<AddressBookListItem> mView;

	private int mTotalNum = 0;
	private int mPageSize = 15;
	private int mPageNumber;
	private String searchKey;

	public ContactSearchPresenter(View<AddressBookListItem> mView) {
		this.mView = mView;
	}

	@Override
	public void onStart() {

	}

	@Override
	public void refreshListData() {
		mPageNumber = 1;
		requestSearchData();
	}

	@Override
	public void refreshListData(String searchKey) {
		this.searchKey = searchKey;
		refreshListData();
	}

	@Override
	public void loadMoreData() {
		mPageNumber++;
		requestSearchData();
	}

	@Override
	public boolean hasMoreData() {
		return mTotalNum > mPageNumber * mPageSize;
	}

	private void requestSearchData() {
		final AddressBookRequest request = new AddressBookRequest();
		request.setCurrentDeptID("");
		request.setDataSourceType(AddressBookType.Staff);
		request.setFilterType(AddressBookFilterType.Register);
		request.setIsCurrentDept(false);
		request.setPage(String.valueOf(mPageNumber));
		request.setPerPageNums(String.valueOf(mPageSize));
		request.setParentItemID("");
		request.setParentItemType(AddressBookType.Staff);
		request.setSearchKey(searchKey);
		request.setSearchUserID("");
		FEHttpClient.getInstance().post(request, new ResponseCallback<AddressBookResponse>() {
			@Override
			public void onCompleted(AddressBookResponse responseContent) {
				if (responseContent != null && "0".equals(responseContent.getErrorCode())) {
					if (mPageNumber == 1) {
						mTotalNum = Integer.valueOf(responseContent.getTotalNums());
						mView.refreshListData(createListData(responseContent));
					}
					else {
						mTotalNum = Integer.valueOf(responseContent.getTotalNums());
						mView.loadMoreListData(createListData(responseContent));
					}
				}
				else {
					onFailure(null);
				}
			}

			@Override
			public void onFailure(RepositoryException repositoryException) {
				if (mPageNumber == 1) {
					mView.refreshListFail();
				}
				else {
					mPageNumber--;
					mView.loadMoreListFail();
				}
			}
		});
	}


	private ArrayList<AddressBookListItem> createListData(AddressBookResponse listResponse) {
		final ArrayList<AddressBookListItem> listDatas = new ArrayList<>();
		List<AddressBookItem> listItems;
		try {
			listItems = listResponse.getItems();
			if (listItems != null) {
				for (final AddressBookItem item : listItems) {
					final AddressBookListItem listData = new AddressBookListItem();
					listData.setAddressBookItem(item);
					listDatas.add(listData);
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return listDatas;
	}
}
