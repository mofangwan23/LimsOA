package com.hyphenate.chatui.retrieval;

import cn.flyrise.feep.core.base.component.FEListContract;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2018-05-04 19:52
 */
public class MoreRecordSearchPresenter implements FEListContract.Presenter {

	private FEListContract.View<ChatMessage> mView;
	private ChatMessagesRepository mRepository;

	public MoreRecordSearchPresenter(FEListContract.View<ChatMessage> mView) {
		this.mView = mView;
		this.mRepository = new ChatMessagesRepository();
	}

	@Override public void onStart() {

	}

	@Override public void refreshListData() {

	}

	@Override public void refreshListData(String searchKey) {
		mRepository.queryMessage(searchKey, -1)
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
