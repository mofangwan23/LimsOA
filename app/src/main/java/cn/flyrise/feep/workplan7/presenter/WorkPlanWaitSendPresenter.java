package cn.flyrise.feep.workplan7.presenter;

import cn.flyrise.android.protocol.entity.workplan.WorkPlanWaitSendDelRequest;
import cn.flyrise.android.protocol.entity.workplan.WorkPlanWaitSendListRequest;
import cn.flyrise.android.protocol.entity.workplan.WorkPlanWaitSendListResponse;
import cn.flyrise.feep.core.base.component.FEListContract;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.workplan7.model.WorkPlanWaitSend;
import java.util.List;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * author : klc
 * data on 2018/5/7 17:33
 * Msg :
 */
public class WorkPlanWaitSendPresenter implements FEListContract.Presenter {

	private FEListContract.View<WorkPlanWaitSend> mView;
	private int pageSize = 20;
	private int nowPage;
	private int totalPage;

	public WorkPlanWaitSendPresenter(FEListContract.View<WorkPlanWaitSend> mView) {
		this.mView = mView;
	}

	@Override
	public void onStart() {
		refreshListData();
	}

	@Override
	public void refreshListData() {
		nowPage = 1;
		getWorkPlanWaitSend(nowPage)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(workPlanWaitSends -> mView.refreshListData(workPlanWaitSends), throwable -> mView.refreshListData(null));
	}

	@Override public void refreshListData(String searchKey) {

	}


	@Override
	public void loadMoreData() {
		nowPage++;
		getWorkPlanWaitSend(nowPage)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(workPlanWaitSends -> mView.loadMoreListData(workPlanWaitSends), throwable -> {
					mView.loadMoreListFail();
					nowPage--;
				});
	}

	@Override
	public boolean hasMoreData() {
		return totalPage > nowPage;
	}

	public void delete(String msgIds) {
		mView.showLoading(true);
		FEHttpClient.getInstance().post(new WorkPlanWaitSendDelRequest(msgIds), new ResponseCallback<ResponseContent>() {
			@Override public void onCompleted(ResponseContent responseContent) {
				mView.showLoading(false);
				if (responseContent.getErrorCode().equals("0")) {
					refreshListData();
				}
				else {
					FEToast.showMessage("删除失败");
				}
			}

			@Override public void onFailure(RepositoryException repositoryException) {
				super.onFailure(repositoryException);
				mView.showLoading(false);
				FEToast.showMessage("删除失败");
			}
		});
	}

	private Observable<List<WorkPlanWaitSend>> getWorkPlanWaitSend(int nowPage) {
		return Observable.create(new OnSubscribe<List<WorkPlanWaitSend>>() {
			@Override public void call(Subscriber<? super List<WorkPlanWaitSend>> subscriber) {
				WorkPlanWaitSendListRequest request = new WorkPlanWaitSendListRequest(String.valueOf(nowPage), String.valueOf(pageSize));
				FEHttpClient.getInstance().post(request, new ResponseCallback<WorkPlanWaitSendListResponse>() {
					@Override public void onCompleted(WorkPlanWaitSendListResponse response) {
						if (response.getErrorCode().equals("0")) {
							totalPage = response.data.totalPage;
							subscriber.onNext(response.data.rows);
						}
						else {
							subscriber.onError(new Throwable(response.getErrorMessage()));
						}
					}

					@Override public void onFailure(RepositoryException repositoryException) {
						super.onFailure(repositoryException);
						subscriber.onError(repositoryException.exception());
					}
				});
			}
		});
	}

}
