package cn.flyrise.feep.collaboration.matter.presenter;

import java.util.List;

import cn.flyrise.android.protocol.entity.AssociationKnowledgeListRequest;
import cn.flyrise.android.protocol.entity.MatterListRequest;
import cn.flyrise.android.protocol.entity.AssociationListResponse;
import cn.flyrise.feep.collaboration.matter.MatterListActivity;
import cn.flyrise.feep.collaboration.matter.model.Matter;
import cn.flyrise.feep.core.base.component.FEListContract;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;

/**
 * Created by klc on 2017/5/22.
 */

public class MatterListPresenter implements FEListContract.Presenter {

	private final int mPageSize = 20;

	private FEListContract.View<Matter> mView;

	private int mTotalPage = 0;
	private int mPageNumber;
	private String searchKey;
	private int type;
	private String folderID;
	private String folderAttr;

	public MatterListPresenter(FEListContract.View<Matter> mView, int type) {
		this.mView = mView;
		this.type = type;
	}

	public MatterListPresenter(FEListContract.View<Matter> mView, int type, String folderID, String folderAttr) {
		this.mView = mView;
		this.type = type;
		this.folderID = folderID;
		this.folderAttr = folderAttr;
	}

	@Override
	public void onStart() {
		refreshListData();
	}

	@Override
	public void refreshListData() {
		mPageNumber = 1;
		request();
	}

	@Override
	public void refreshListData(String searchKey) {
		this.searchKey = searchKey;
		this.refreshListData();
	}

	@Override
	public void loadMoreData() {
		mPageNumber++;
		request();
	}

	@Override
	public boolean hasMoreData() {
		return mTotalPage > mPageNumber;
	}


	private void request() {
		if (type == MatterListActivity.MATTER_KNOWLEDGE) {
			AssociationKnowledgeListRequest request = new AssociationKnowledgeListRequest(folderID, folderAttr, searchKey,
					String.valueOf(mPageNumber), String.valueOf(mPageSize));
			FEHttpClient.getInstance().post(request, responseCallback);
		}
		else {
			MatterListRequest request = new MatterListRequest(searchKey, mPageNumber, mPageSize, type);
			FEHttpClient.getInstance().post(request, responseCallback);
		}
	}

	private ResponseCallback<AssociationListResponse> responseCallback = new ResponseCallback<AssociationListResponse>() {
		@Override
		public void onCompleted(AssociationListResponse response) {
			AssociationListResponse.Result result = response.getResult();
			if (result != null) {
				mTotalPage = result.getTotalPage();
				List<Matter> associationList = result.getAssociationList();
				if (CommonUtil.nonEmptyList(associationList)) {
					for (int i = 0, n = associationList.size(); i < n; i++) {
						associationList.get(i).matterType = type;
					}
				}
				if (mPageNumber == 1) {
					mView.refreshListData(associationList);
				}
				else {
					mView.loadMoreListData(associationList);
				}
			}
		}

		@Override
		public void onFailure(RepositoryException repositoryException) {
			super.onFailure(repositoryException);
			if (mPageNumber == 1) {
				mView.refreshListFail();
			}
			else {
				mPageNumber--;
				mView.loadMoreListFail();
			}
		}
	};
}
