package cn.flyrise.feep.collaboration.presenter;

import java.util.List;

import cn.flyrise.android.protocol.entity.WaitingSendListRequest;
import cn.flyrise.android.protocol.entity.WaitingSendListResponse;
import cn.flyrise.feep.collaboration.model.WaitingSend;
import cn.flyrise.feep.core.base.component.FEListContract;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;

/**
 * Created by klc on 2017/4/26.
 */

public class WaitingSendListPresenter implements FEListContract.Presenter {

	private FEListContract.View<WaitingSend> mView;

	public WaitingSendListPresenter(FEListContract.View<WaitingSend> mView) {
		this.mView = mView;
	}

	@Override
	public void onStart() {
		refreshListData();
	}

	@Override
	public void refreshListData() {
		FEHttpClient.getInstance().post(new WaitingSendListRequest(), new ResponseCallback<WaitingSendListResponse>() {
			@Override
			public void onCompleted(WaitingSendListResponse response) {
				if ("0".equals(response.getErrorCode())) {
					List<WaitingSend> dataList = response.result;
					mView.refreshListData(dataList);
				}
				else {
					mView.refreshListFail();
				}
			}

			@Override
			public void onFailure(RepositoryException repositoryException) {
				mView.refreshListFail();
			}
		});
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

}
