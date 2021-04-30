package cn.flyrise.feep.form.presenter;


import android.text.TextUtils;

import cn.flyrise.feep.form.model.FormListRepository;
import cn.flyrise.feep.form.contract.FormListContract;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.flyrise.android.protocol.model.FormTypeItem;
import cn.flyrise.feep.commonality.bean.PageInfo;
import cn.flyrise.feep.core.base.component.FEListContract;

/**
 * Created by KLC on 2017/1/13.
 */

public class FormListPresenter implements FormListContract.Presenter {

	private FormListContract.View mView;
	private FormListRepository mRepository;
	final private int PageSize = 15;
	private String searchKey;
	private String formListID;
	private Map<String, PageInfo> mPageInfos;
	private PageInfo mNowPageInfo;

	public FormListPresenter(FormListContract.View view) {
		this.mView = view;
		this.mRepository = new FormListRepository();
		this.mPageInfos = new HashMap<>();
		this.mNowPageInfo = new PageInfo();
	}

	@Override
	public void onStart() {
		refreshListData();
		this.mPageInfos.put("root", mNowPageInfo);
	}

	@Override
	public void refreshListData() {
		mRepository.requestFormList(mNowPageInfo.mNowPage = 1, PageSize, searchKey, formListID,
				new FEListContract.LoadListCallback<FormTypeItem>() {
					@Override
					public void loadListDataSuccess(List<FormTypeItem> dataList, int totalNum) {
						mNowPageInfo.mTotalNum = totalNum;
						mView.refreshListData(dataList);
					}

					@Override
					public void loadListDataError() {
						mView.refreshListFail();
					}
				});
	}

	@Override
	public void refreshListData(String searchKey) {

	}

	@Override
	public void refreshListData(String searchKey, String formListID) {
		this.searchKey = searchKey;
		this.formListID = formListID;
		refreshListData();
	}

	@Override
	public void loadMoreData() {
		mRepository.requestFormList(++mNowPageInfo.mNowPage, PageSize, searchKey, formListID,
				new FEListContract.LoadListCallback<FormTypeItem>() {
					@Override
					public void loadListDataSuccess(List<FormTypeItem> dataList, int totalNum) {
						mNowPageInfo.mTotalNum = totalNum;
						mView.loadMoreListData(dataList);
					}

					@Override
					public void loadListDataError() {
						mNowPageInfo.mNowPage--;
						mView.loadMoreListFail();
					}
				});
	}

	@Override
	public void getListDataForFormID(String formListID) {
		this.formListID = formListID;
		List<FormTypeItem> data;
		if (TextUtils.isEmpty(formListID)) {
			data = mRepository.getLocalFormList(formListID);
			mView.refreshListData(data);
			mNowPageInfo = mPageInfos.get(formListID);
		}
		else {
			if (mPageInfos.containsKey(formListID)) {
				data = mRepository.getLocalFormList(formListID);
				mView.refreshListData(data);
				mNowPageInfo = mPageInfos.get(formListID);
			}
			else {
				this.mNowPageInfo = new PageInfo();
				this.mPageInfos.put(formListID, this.mNowPageInfo);
				refreshListData();
			}
		}
	}

	@Override
	public void onBackToParent() {
		if (TextUtils.isEmpty(formListID)) {
			mView.finish();
		}
		else {
			refreshListData(null, null);
		}
	}

	@Override
	public boolean hasMoreData() {
		return mNowPageInfo.mTotalNum > mNowPageInfo.mNowPage * 10;
	}
}
