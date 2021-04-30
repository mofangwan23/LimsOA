package cn.flyrise.feep.meeting.old;


import cn.flyrise.feep.core.base.component.FEListContract;
import java.util.List;

/**
 * Created by KLC on 2016/12/27.
 */

public class MeetingListPresenter implements FEListContract.Presenter {

	private MeetingListRepository mRepository;
	private FEListContract.View<MeetingListItemBean> mView;

	private int mTotalNum = 0;
	private int mPageSize = 20;
	private int mPageNumber;
	private String searchKey;

	public MeetingListPresenter(FEListContract.View<MeetingListItemBean> view) {
		mRepository = new MeetingListRepository();
		this.mView = view;
	}

	public void onStart() {
		refreshListData();
	}

	@Override
	public void refreshListData() {
		mRepository.requestMeetingList(mPageNumber = 1, String.valueOf(mPageSize), searchKey,
				new FEListContract.LoadListCallback<MeetingListItemBean>() {
					@Override
					public void loadListDataSuccess(List<MeetingListItemBean> dataList, int totalPage) {
						mTotalNum = totalPage;
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
		this.searchKey = searchKey;
		this.refreshListData();
	}

	@Override
	public void loadMoreData() {
		mRepository.requestMeetingList(++mPageNumber, String.valueOf(mPageSize), searchKey,
				new FEListContract.LoadListCallback<MeetingListItemBean>() {
					@Override
					public void loadListDataSuccess(List<MeetingListItemBean> dataList, int totalPage) {
						mTotalNum = totalPage;
						mView.loadMoreListData(dataList);
					}

					@Override
					public void loadListDataError() {
						mPageNumber--;
						mView.loadMoreListFail();
					}
				});
	}

	@Override
	public boolean hasMoreData() {
		return mTotalNum > mPageNumber * mPageSize;
	}
}
