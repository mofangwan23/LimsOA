package cn.flyrise.feep.workplan7.presenter;


import cn.flyrise.android.protocol.entity.ListRequest;
import cn.flyrise.android.protocol.entity.ListResponse;
import cn.flyrise.android.protocol.entity.RelatedUserRequest;
import cn.flyrise.android.protocol.entity.RelatedUserResponse;
import cn.flyrise.android.protocol.model.User;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.X.RequestType;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.workplan7.contract.WorkPlanSearchListContract;
import cn.flyrise.feep.workplan7.util.WorkPlanDataManager;
import java.util.ArrayList;
import java.util.List;

import cn.flyrise.feep.core.base.component.FEListContract;
import cn.flyrise.feep.workplan7.model.WorkPlanListItemBean;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by KLC on 2017/1/13.
 */

public class WorkPlanSearchListPresenter implements WorkPlanSearchListContract.Presenter {

	private final int PageSize = 20;

	private FEListContract.View<WorkPlanListItemBean> mView;

	private ArrayList<User> subordinates;
	private int totalNum;
	private int nowPage;
	private String userId;
	private String searchKey;

	@Override
	public void onStart() {
	}

	public WorkPlanSearchListPresenter(FEListContract.View<WorkPlanListItemBean> view) {
		this.mView = view;
		requestSubordinate();
	}

	@Override
	public void refreshListData() {
		refreshListData(userId);
	}

	@Override
	public void searchData(String searchKey) {
		this.searchKey = searchKey;
		requestData(CoreZygote.getLoginUserServices().getUserId(), searchKey, nowPage, PageSize)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(listResponse -> {
					List<WorkPlanListItemBean> data = WorkPlanDataManager.changeDataToListItemBean(listResponse);
					totalNum = Integer.valueOf(listResponse.getTotalNums());
					mView.refreshListData(data);
				}, throwable -> mView.refreshListFail());
	}

	@Override public List<User> getSubordinate() {
		return subordinates;
	}

	@Override
	public void refreshListData(String userID) {
		this.totalNum = 0;
		this.nowPage = 1;
		this.userId = userID;
		requestData(userID, searchKey, nowPage, PageSize)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(listResponse -> {
					List<WorkPlanListItemBean> data = WorkPlanDataManager.changeDataToListItemBean(listResponse);
					totalNum = Integer.valueOf(listResponse.getTotalNums());
					mView.refreshListData(data);
				}, throwable -> mView.refreshListFail());
	}

	@Override
	public void loadMoreData() {
		requestData(userId, searchKey, ++nowPage, PageSize)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(listResponse -> {
					List<WorkPlanListItemBean> data = WorkPlanDataManager.changeDataToListItemBean(listResponse);
					totalNum = Integer.valueOf(listResponse.getTotalNums());
					mView.loadMoreListData(data);
				}, throwable -> mView.loadMoreListFail());
	}

	private rx.Observable<ListResponse> requestData(String userID, String searchKey, int nowPage, int pageSize) {
		return rx.Observable.create(new OnSubscribe<ListResponse>() {
			@Override public void call(Subscriber<? super ListResponse> subscriber) {
				final ListRequest listRequest = new ListRequest();
				listRequest.setRequestType(RequestType.OthersWorkPlan);
				listRequest.setPage(String.valueOf(nowPage));
				listRequest.setPerPageNums(String.valueOf(pageSize));
				listRequest.setId(userID);
				listRequest.setSearchKey(searchKey);
				FEHttpClient.getInstance().post(listRequest, new ResponseCallback<ListResponse>() {
					@Override public void onCompleted(ListResponse listResponse) {
						if (listResponse.getErrorCode().equals("0"))
							subscriber.onNext(listResponse);
						else
							subscriber.onError(new Exception("get workPlan list error"));
					}

					@Override public void onFailure(RepositoryException repositoryException) {
						super.onFailure(repositoryException);
						subscriber.onError(new Exception("get workPlan list error"));
					}
				});
			}
		});
	}

	public boolean hasMoreData() {
		return totalNum > nowPage * PageSize;
	}

	private void requestSubordinate() {
		final RelatedUserRequest relatedUserRequest = new RelatedUserRequest();
		relatedUserRequest.setRequestType("0");
		FEHttpClient.getInstance().post(relatedUserRequest, new ResponseCallback<RelatedUserResponse>(this) {
			@Override
			public void onCompleted(RelatedUserResponse relatedUserResponse) {
				if ("0".equals(relatedUserResponse.getErrorCode())) {
					subordinates = relatedUserResponse.getUsers();
				}
			}
		});
	}
}
