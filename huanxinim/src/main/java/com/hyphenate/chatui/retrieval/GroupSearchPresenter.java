package com.hyphenate.chatui.retrieval;

import cn.flyrise.feep.core.base.component.FEListContract;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2018-05-08 18:14
 */
public class GroupSearchPresenter implements FEListContract.Presenter {

	private FEListContract.View<GroupInfo> mView;
	private GroupRepository mRepository;

	public GroupSearchPresenter(FEListContract.View<GroupInfo> mView) {
		this.mView = mView;
		this.mRepository = new GroupRepository();
	}

	@Override public void onStart() {
	}

	@Override public void refreshListData() {
	}

	@Override public void refreshListData(String searchKey) {
		mRepository.queryGroupInfo(searchKey, -1)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(messages -> {
					mView.refreshListData(messages);
					mView.setCanPullUp(false);
				}, throwable -> mView.refreshListData(null));
	}

	@Override public void loadMoreData() {

	}

	@Override public boolean hasMoreData() {
		return false;
	}
}
