package cn.flyrise.feep.report;


import java.util.List;

import cn.flyrise.feep.core.base.component.FEListContract;
import cn.flyrise.android.protocol.model.ReportListItem;

/**
 * Created by KLC on 2017/1/13.
 */

class ReportListPresenter implements FEListContract.Presenter {

	private FEListContract.View<ReportListItem> mView;
	private ReportListRepository mRepository;
	final private int PageSize = 20;
	private int mNowPage;
	private int mTotalNum;

	ReportListPresenter(FEListContract.View<ReportListItem> view) {
		this.mView = view;
		this.mRepository = new ReportListRepository();
	}

	@Override
	public void onStart() {
		refreshListData();
	}

	@Override
	public void refreshListData() {
		mRepository.requestReportList(mNowPage = 1, PageSize, new FEListContract.LoadListCallback<ReportListItem>() {
			@Override
			public void loadListDataSuccess(List<ReportListItem> dataList, int totalNum) {
				mTotalNum = totalNum;
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
	public void loadMoreData() {
		mRepository.requestReportList(++mNowPage, PageSize, new FEListContract.LoadListCallback<ReportListItem>() {
			@Override
			public void loadListDataSuccess(List<ReportListItem> dataList, int totalNum) {
				mTotalNum = totalNum;
				mView.loadMoreListData(dataList);
			}

			@Override
			public void loadListDataError() {
				mView.loadMoreListFail();
			}
		});
	}

	public boolean hasMoreData() {
		return mTotalNum > mNowPage * 10;
	}
}
