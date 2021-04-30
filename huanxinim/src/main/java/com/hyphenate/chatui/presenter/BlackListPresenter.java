package com.hyphenate.chatui.presenter;

import cn.flyrise.feep.core.base.component.FEListContract;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chatui.contract.BlackUserListContract;
import com.hyphenate.exceptions.HyphenateException;
import java.util.List;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by klc on 2017/3/13.
 * 黑名单界面P
 */

public class BlackListPresenter implements FEListContract.Presenter {

	private FEListContract.View<String> mView;
	private BlackUserListContract.IView mBlackView;

	public BlackListPresenter(FEListContract.View<String> mView, BlackUserListContract.IView mBlackView) {
		this.mView = mView;
		this.mBlackView = mBlackView;
	}

	@Override
	public void onStart() {
		refreshListData();
	}

	@Override
	public void refreshListData() {
		Observable.unsafeCreate((OnSubscribe<List<String>>) subscriber -> {
			List<String> blacklist = null;
			try {
				blacklist = EMClient.getInstance().contactManager().getBlackListFromServer();
			} catch (HyphenateException e) {
				e.printStackTrace();
			}
			subscriber.onNext(blacklist);
		}).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(f -> mView.refreshListData(f), e -> mView.refreshListFail());
	}

	@Override
	public void refreshListData(String searchKey) {
	}

	@Override
	public void loadMoreData() {
	}

	@Override
	public boolean hasMoreData() {
		return false;
	}

	public void removeOutBlacklist(final String tobeRemoveUser) {
		mView.showLoading(true);
		Observable
				.unsafeCreate((Observable.OnSubscribe<Boolean>) subscriber -> {
					try {
						EMClient.getInstance().contactManager().removeUserFromBlackList(tobeRemoveUser);
						subscriber.onNext(true);
					} catch (HyphenateException e) {
						subscriber.onError(e);
					}
				}).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(aBoolean -> {
					mBlackView.removeSuccess();
					mView.showLoading(false);
				}, exception -> {
					mBlackView.removeSuccess();
					mView.showLoading(false);
				});
	}
}
