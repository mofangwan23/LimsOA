package cn.flyrise.feep.main.message;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.views.LoadMoreRecyclerView;
import cn.flyrise.feep.core.base.views.adapter.BaseMessageRecyclerAdapter;

/**
 * @author ZYP
 * @since 2017-03-30 14:44
 */
public abstract class BaseMessageFragment<T> extends Fragment {

	protected BaseMessageAdapter<T> mAdapter;
	protected LoadMoreRecyclerView mRecyclerView;
	protected SwipeRefreshLayout mSwipeRefreshLayout;

	protected int mCurrentPage;
	protected boolean mIsLoading;
	protected int mTotalSize;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_approval_collaboration, container, false);
		mRecyclerView = (LoadMoreRecyclerView) view.findViewById(R.id.recyclerView);
		mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
		mSwipeRefreshLayout.setColorSchemeResources(R.color.defaultColorAccent);
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		mRecyclerView.setAdapter(mAdapter = getMessageAdapter());
		mRecyclerView.setBackgroundColor(Color.parseColor("#EBEBEB"));
		mAdapter.setEmptyView(view.findViewById(R.id.ivEmptyView));
		if (isNeedShowRefresh()) {
			mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(true));
		}
		bindListener();
		return view;
	}

	protected boolean isNeedShowRefresh() {
		return false;
	}

	protected void bindListener() {
		mSwipeRefreshLayout.setOnRefreshListener(() -> requestMessage(mCurrentPage = 1, true));

		mRecyclerView.setOnLoadMoreListener(() -> {
			if (!isNeedLoadMore()) return;
			if (!mIsLoading && isLoaderMore()) {
				mIsLoading = true;
				mAdapter.setLoadState(BaseMessageRecyclerAdapter.LOADING);
				requestMessage(++mCurrentPage, false);
			}
			else {
				mAdapter.setLoadState(BaseMessageRecyclerAdapter.LOADING_END);
			}
		});

		// 滚动到顶部
		final LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
		mRecyclerView.addOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				if (newState == RecyclerView.SCROLL_STATE_IDLE) {
					int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
					if (firstVisibleItemPosition == 0 && !recyclerView.canScrollVertically(-1)) {
						mSwipeRefreshLayout.setEnabled(true);
					}
					else {
						mSwipeRefreshLayout.setEnabled(false);
					}
				}
			}
		});
	}

	public void scrollLastItem2Bottom() {
		LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
		int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
		if (lastVisibleItem == mAdapter.getItemCount() - 1) {
			View footView = layoutManager.findViewByPosition(lastVisibleItem);
			if (footView != null) {
				int itemHeight = layoutManager.findViewByPosition(lastVisibleItem - 1).getHeight();
				int listViewHeight = mRecyclerView.getHeight();
				int itemCount = listViewHeight / itemHeight;
				int offest = listViewHeight % itemHeight;
				layoutManager.scrollToPositionWithOffset(mAdapter.getDataSourceCount() - itemCount, offest);
			}
		}
	}

	protected boolean isNeedLoadMore() {
		return true;
	}

	public void hideRefreshLoading() {
		mSwipeRefreshLayout.postDelayed(() -> mSwipeRefreshLayout.setRefreshing(false), 666);
	}

	public abstract BaseMessageAdapter<T> getMessageAdapter();

	public abstract void requestMessage(int pageNumber, boolean isRefresh);

	public abstract boolean isLoaderMore();

	public void setTotalSize(int totalSize) {
		this.mTotalSize = totalSize;
	}

}
