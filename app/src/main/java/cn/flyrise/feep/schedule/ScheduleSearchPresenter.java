package cn.flyrise.feep.schedule;

import android.text.TextUtils;
import cn.flyrise.android.protocol.entity.schedule.AgendaResponseItem;
import cn.flyrise.android.protocol.entity.schedule.ScheduleSearchRequest;
import cn.flyrise.android.protocol.entity.schedule.ScheduleSearchResponse;
import cn.flyrise.feep.core.base.component.FEListContract;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;

/**
 * @author ZYP
 * @since 2018-05-11 11:02
 */
public class ScheduleSearchPresenter implements FEListContract.Presenter {

	private static final int PAGE_SIZE = 20;
	private FEListContract.View<AgendaResponseItem> mSearchView;

	private int mTotalCount = 0;
	private int mPageNumber;
	private String mSearchKey;

	public ScheduleSearchPresenter(FEListContract.View<AgendaResponseItem> view) {
		this.mSearchView = view;
	}

	@Override public void onStart() {
		mSearchView.showLoading(true);
		refreshListData();
	}

	@Override public void refreshListData() {
		ScheduleSearchRequest request = new ScheduleSearchRequest();
		request.keyword = mSearchKey;
		request.pageNumber = String.valueOf(mPageNumber = 1);
		request.pageSize = String.valueOf(PAGE_SIZE);
		FEHttpClient.getInstance().post(request, new ResponseCallback<ScheduleSearchResponse>() {
			@Override public void onCompleted(ScheduleSearchResponse response) {
				if (response != null && TextUtils.equals(response.getErrorCode(), "0")) {
					mTotalCount = response.totalNums;
					mSearchView.refreshListData(response.items);
					mSearchView.setCanPullUp(hasMoreData());
				}
				mSearchView.showLoading(false);
			}

			@Override public void onFailure(RepositoryException repository) {
				mSearchView.showLoading(false);
			}
		});
	}

	@Override public void refreshListData(String searchKey) {
		this.mSearchKey = searchKey;
		this.refreshListData();
	}

	@Override public void loadMoreData() {
		ScheduleSearchRequest request = new ScheduleSearchRequest();
		request.keyword = mSearchKey;
		request.pageNumber = String.valueOf(mPageNumber++);
		request.pageSize = String.valueOf(PAGE_SIZE);
		FEHttpClient.getInstance().post(request, new ResponseCallback<ScheduleSearchResponse>() {
			@Override public void onCompleted(ScheduleSearchResponse response) {
				if (response != null && TextUtils.equals(response.getErrorCode(), "0")) {
					mTotalCount = response.totalNums;
					mSearchView.loadMoreListData(response.items);
					return;
				}

				mSearchView.loadMoreListFail();
			}

			@Override public void onFailure(RepositoryException repository) {
				FELog.e("Schedule search faile by this keyword: " + mSearchKey
						+ ". Error: " + repository.exception().getMessage());
				repository.exception().printStackTrace();
				mSearchView.loadMoreListFail();
				mPageNumber--;
			}
		});

	}

	@Override public boolean hasMoreData() {
		return mTotalCount > mPageNumber * PAGE_SIZE;
	}
}
