package cn.flyrise.feep.collaboration.matter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.flyrise.android.protocol.entity.AssociationKnowledgeListRequest;
import cn.flyrise.android.protocol.entity.AssociationListResponse;
import cn.flyrise.android.protocol.entity.MatterListRequest;
import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.matter.adpater.MatterListAdapter;
import cn.flyrise.feep.collaboration.matter.model.DirectoryNode;
import cn.flyrise.feep.collaboration.matter.model.Matter;
import cn.flyrise.feep.collaboration.matter.model.MatterPageInfo;
import cn.flyrise.feep.core.base.views.LoadMoreRecyclerView;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import java.util.List;

/**
 * Created by klc on 2017/5/12.
 */
public class MatterListFragment extends Fragment {

	private boolean isLoading;
	private static final int PAGE_SIZE = 20;
	private LoadMoreRecyclerView mRecyclerView;
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private Handler mHandler = new Handler(Looper.getMainLooper());

	private MatterPageInfo mPageInfo;
	private int mMatterType;
	private DirectoryNode node;
	private List<Matter> mSelectedAssociations;
	private MatterListAdapter mAdapter;

	public static MatterListFragment newInstance(int matterType) {
		Bundle args = new Bundle();
		MatterListFragment fragment = new MatterListFragment();
		fragment.setMatterType(matterType);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View contentView = inflater.inflate(R.layout.fragment_matter_list, container, false);
		initViewsAndListener(contentView);
		return contentView;
	}

	public void setMatterType(int matterType) {
		this.mMatterType = matterType;
	}

	public void setKnowledgePageInfo(DirectoryNode node, MatterPageInfo pageInfo) {
		this.node = node;
		this.mPageInfo = pageInfo;
		if (pageInfo.currentPage != 0) {
			mAdapter.setAssociationList(mPageInfo.dataList);
		}
		else {
			showLoading();
			requestList(++pageInfo.currentPage);
		}
	}

	private void initViewsAndListener(View contentView) {
		View emptyView = contentView.findViewById(R.id.ivEmptyView);
		if (mMatterType == MatterListActivity.MATTER_KNOWLEDGE) {
			TextView tvHint = emptyView.findViewById(R.id.empty_hint);
			tvHint.setText(R.string.common_empty_file_hint);
		}
		mSwipeRefreshLayout = (SwipeRefreshLayout) contentView.findViewById(R.id.swipeRefreshLayout);
		mSwipeRefreshLayout.setColorSchemeResources(R.color.login_btn_defulit);
		mRecyclerView = (LoadMoreRecyclerView) contentView.findViewById(R.id.recyclerView);
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		mRecyclerView.setAdapter(mAdapter = new MatterListAdapter());
		mAdapter.setSelectedAssociations(mSelectedAssociations);
		mAdapter.setEmptyView(emptyView);
		mAdapter.setOnItemClickListener(new MatterListAdapter.OnAssociationCheckChangeListener() {
			@Override
			public void onAssociationAdd(Matter association) {
				MatterListActivity activity = (MatterListActivity) getActivity();
				activity.addSelectedAssociation(association);
			}

			@Override
			public void onAssociationDelete(Matter deletedAssociation) {
				MatterListActivity activity = (MatterListActivity) getActivity();
				activity.removeSelectedAssociation(deletedAssociation);
			}
		});

		mRecyclerView.setOnLoadMoreListener(() -> {
			if (!isLoading && hasMoreData()) {
				isLoading = true;
				requestList(++mPageInfo.currentPage);
			}
			else {
				mAdapter.removeFooterView();
			}
		});

		mSwipeRefreshLayout.setOnRefreshListener(() -> {
			if (mPageInfo != null) {
				requestList(mPageInfo.currentPage = 1);
			}
			else {
				mSwipeRefreshLayout.setRefreshing(false);
			}
		});

		if (mMatterType != MatterListActivity.MATTER_KNOWLEDGE) {
			showLoading();
			mPageInfo = new MatterPageInfo();
			requestList(mPageInfo.currentPage = 1);
		}
	}

	public void showLoading() {
		mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(true));
	}

	public boolean hasMoreData() {
		return mPageInfo.hasMore;
	}

	public void deleteAssociation(Matter association) {
		mAdapter.deleteSelectedAssociation(association);
	}

	public void addAssociation(Matter association) {
		mAdapter.addSelectedAssociation(association);
	}

	public void setSelectedAssociations(List<Matter> associations) {
		this.mSelectedAssociations = associations;
	}

	public void notifyDataSetChange() {
		this.mAdapter.notifyDataSetChanged();
	}

	private void requestList(int page) {
		if (mMatterType == MatterListActivity.MATTER_KNOWLEDGE) {
			AssociationKnowledgeListRequest request = new AssociationKnowledgeListRequest(node.id, node.attr, "",
					String.valueOf(mPageInfo.currentPage), PAGE_SIZE + "");
			FEHttpClient.getInstance().post(request, responseCallback);
		}
		else {
			MatterListRequest request = new MatterListRequest("", page, PAGE_SIZE, mMatterType);
			FEHttpClient.getInstance().post(request, responseCallback);
		}
	}


	public ResponseCallback responseCallback = new ResponseCallback<AssociationListResponse>() {
		@Override
		public void onCompleted(AssociationListResponse response) {
			isLoading = false;
			mHandler.postDelayed(() -> mSwipeRefreshLayout.setRefreshing(false), 1000);
			AssociationListResponse.Result result = response.getResult();
			if (result != null) {
				mPageInfo.hasMore = result.getTotalPage() > mPageInfo.currentPage;
				List<Matter> associationList = result.getAssociationList();
				if (CommonUtil.nonEmptyList(associationList)) {
					for (int i = 0, n = associationList.size(); i < n; i++) {
						associationList.get(i).matterType = mMatterType;
					}
				}

				if (mPageInfo.currentPage == 1) {
					mAdapter.setAssociationList(associationList);
					mPageInfo.dataList = associationList;
				}
				else {
					mAdapter.addAssociationList(associationList);
				}
				if (hasMoreData()) {
					mAdapter.setFooterView(cn.flyrise.feep.core.R.layout.core_refresh_bottom_loading);
				}
				else {
					mAdapter.removeFooterView();
				}
			}
		}

		@Override
		public void onFailure(RepositoryException repositoryException) {
			mRecyclerView.scrollLastItem2Bottom(mAdapter);
			isLoading = false;
			mHandler.postDelayed(() -> mSwipeRefreshLayout.setRefreshing(false), 1000);
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
		}
	}
}