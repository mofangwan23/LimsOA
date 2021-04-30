package cn.flyrise.feep.meeting7.ui.component;

import static cn.flyrise.feep.meeting7.ui.component.BaseRefreshHeader.STATE_DONE;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import java.util.ArrayList;
import java.util.List;

public class XRecyclerView extends RecyclerView {

	private static final int TYPE_REFRESH_HEADER = 10000;
	private static final int TYPE_FOOTER = 10001;
	private static final int HEADER_INIT_INDEX = 10002;

	private boolean isLoadingData = false;
	private boolean isNoMore = false;
	private WrapAdapter mWrapAdapter;

	private float mLastY = -1;
	private static final float DRAG_RATE = 3;

	private View mFooterView;
	private FooterViewCallback mFooterViewCallback;

	private ForwardLoadHeaderView mForwardLoadHeaderView;
	private LoadingListener mLoadingListener;

	private boolean pullRefreshEnabled = true;
	private boolean loadingMoreEnabled = true;

	private ArrayList<View> mHeaderViews = new ArrayList<>(2);
	private List<Integer> sHeaderTypes = new ArrayList<>(2);  // 每个header必须有不同的type,不然滚动的时候顺序会变化

	public XRecyclerView(Context context) {
		this(context, null);
	}

	public XRecyclerView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public XRecyclerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (pullRefreshEnabled) {
			mForwardLoadHeaderView = new ForwardLoadHeaderView(context);
		}
	}

	public boolean hasHeaderView() {
		return CommonUtil.nonEmptyList(mHeaderViews);
	}

	public void addHeaderView(View view) {
		if (mHeaderViews == null || sHeaderTypes == null)
			return;
		sHeaderTypes.add(HEADER_INIT_INDEX + mHeaderViews.size());
		mHeaderViews.add(view);
		if (mWrapAdapter != null) {
			mWrapAdapter.notifyDataSetChanged();
		}
	}

	public void removeHeaderView() {
		sHeaderTypes.clear();
		mHeaderViews.clear();
		final Adapter originalAdapter = mWrapAdapter.getOriginalAdapter();
		if (originalAdapter != null) {
			originalAdapter.notifyDataSetChanged();
		}
	}

	public void setFooterView(final View view, FooterViewCallback footerViewCallBack) {
		if (view == null || footerViewCallBack == null) {
			return;
		}
		mFooterView = view;
		this.mFooterViewCallback = footerViewCallBack;
	}

	public View getFooterView() {
		return mFooterView;
	}

	private View getHeaderViewByType(int itemType) {
		if (!isHeaderType(itemType)) return null;
		if (mHeaderViews == null) return null;
		return mHeaderViews.get(itemType - HEADER_INIT_INDEX);
	}

	private boolean isHeaderType(int itemViewType) {
		if (mHeaderViews == null || sHeaderTypes == null) return false;
		return mHeaderViews.size() > 0 && sHeaderTypes.contains(itemViewType);
	}

	private boolean isReservedItemViewType(int itemViewType) {
		return itemViewType == TYPE_REFRESH_HEADER || itemViewType == TYPE_FOOTER || sHeaderTypes.contains(itemViewType);
	}


	public void loadMoreComplete() {
		isLoadingData = false;
		if (mFooterViewCallback != null) {
			mFooterViewCallback.onLoadMoreComplete(mFooterView);
		}
	}

	public void setNoMore(boolean noMore) {
		isLoadingData = false;
		isNoMore = noMore;

		if (mFooterViewCallback != null) {
			mFooterViewCallback.onSetNoMore(mFooterView, noMore);
		}
	}

	public void refresh() {
		if (pullRefreshEnabled && mLoadingListener != null) {
			mForwardLoadHeaderView.setState(ForwardLoadHeaderView.STATE_REFRESHING);
			mLoadingListener.onRefresh();
		}
	}

	public void reset() {
		setNoMore(false);
		loadMoreComplete();
		refreshComplete();
	}

	public void refreshComplete() {
		if (mForwardLoadHeaderView != null)
			mForwardLoadHeaderView.refreshComplete();
//		setNoMore(false);
	}

	public void setRefreshHeader(ForwardLoadHeaderView refreshHeader) {
		mForwardLoadHeaderView = refreshHeader;
	}

	public void setPullRefreshEnabled(boolean enabled) {
		pullRefreshEnabled = enabled;
		if (mWrapAdapter != null) {
			mWrapAdapter.notifyDataSetChanged();
		}
	}

	public void setLoadingMoreEnabled(boolean enabled) {
		loadingMoreEnabled = enabled;
		if (mWrapAdapter != null) {
			mWrapAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void setAdapter(Adapter adapter) {
		mWrapAdapter = new WrapAdapter(adapter);
		super.setAdapter(mWrapAdapter);
	}

	@Override
	public Adapter getAdapter() {
		return mWrapAdapter == null ? null : mWrapAdapter.getOriginalAdapter();
	}

	private int getTotalHeadersCount() {
		return mWrapAdapter.getHeadersCount() + 1;
	}


	@Override
	public void onScrollStateChanged(int state) {
		super.onScrollStateChanged(state);
		if (state == RecyclerView.SCROLL_STATE_IDLE && mLoadingListener != null && !isLoadingData && loadingMoreEnabled) {
			LayoutManager layoutManager = getLayoutManager();
			int lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
			int adjAdapterItemCount = layoutManager.getItemCount() + getTotalHeadersCount();

			int status = STATE_DONE;

			if (mForwardLoadHeaderView != null) status = mForwardLoadHeaderView.getState();

			if (layoutManager.getChildCount() > 0
					&& lastVisibleItemPosition >= adjAdapterItemCount - 1
					&& adjAdapterItemCount >= layoutManager.getChildCount()
					&& !isNoMore
					&& status < ForwardLoadHeaderView.STATE_REFRESHING) {
				isLoadingData = true;

				if (mFooterViewCallback != null) {
					mFooterViewCallback.onLoadingMore(mFooterView);
				}
				mLoadingListener.onLoadMore();
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mLastY == -1) {
			mLastY = ev.getRawY();
		}
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mLastY = ev.getRawY();
				break;
			case MotionEvent.ACTION_MOVE:
				final float deltaY = ev.getRawY() - mLastY;
				mLastY = ev.getRawY();
				if (isOnTop() && pullRefreshEnabled) {
					if (mForwardLoadHeaderView == null)
						break;
					mForwardLoadHeaderView.onMove(deltaY / DRAG_RATE);
					if (mForwardLoadHeaderView.getVisibleHeight() > 0
							&& mForwardLoadHeaderView.getState() < ForwardLoadHeaderView.STATE_REFRESHING) {
						return false;
					}
				}
				break;
			default:
				mLastY = -1; // reset
				if (isOnTop() && pullRefreshEnabled) {
					if (mForwardLoadHeaderView != null && mForwardLoadHeaderView.releaseAction()) {
						if (mLoadingListener != null) {
							mLoadingListener.onRefresh();
						}
					}
				}
				break;
		}
		return super.onTouchEvent(ev);
	}

	private boolean isOnTop() {
		if (mForwardLoadHeaderView == null)
			return false;
		if (mForwardLoadHeaderView.getParent() != null) {
			return true;
		}
		else {
			return false;
		}
	}

	private class WrapAdapter extends RecyclerView.Adapter<ViewHolder> {

		private RecyclerView.Adapter adapter;

		public WrapAdapter(RecyclerView.Adapter adapter) {
			this.adapter = adapter;
		}

		public RecyclerView.Adapter getOriginalAdapter() {
			return this.adapter;
		}

		public boolean isHeader(int position) {
			if (mHeaderViews == null) return false;
			return position >= 1 && position < mHeaderViews.size() + 1;
		}

		public boolean isFooter(int position) {
			return loadingMoreEnabled ? position == getItemCount() - 1 : false;
		}

		public boolean isRefreshHeader(int position) {
			return position == 0;
		}

		public int getHeadersCount() {
			if (mHeaderViews == null)
				return 0;
			return mHeaderViews.size();
		}

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			if (viewType == TYPE_REFRESH_HEADER) {
				return new SimpleViewHolder(mForwardLoadHeaderView);
			}
			else if (isHeaderType(viewType)) {
				return new SimpleViewHolder(getHeaderViewByType(viewType));
			}
			else if (viewType == TYPE_FOOTER) {
				return new SimpleViewHolder(mFooterView);
			}
			return adapter.onCreateViewHolder(parent, viewType);
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
			if (isHeader(position) || isRefreshHeader(position)) {
				return;
			}
			int adjPosition = position - (getHeadersCount() + 1);
			int adapterCount;
			if (adapter != null) {
				adapterCount = adapter.getItemCount();
				if (adjPosition < adapterCount) {
					adapter.onBindViewHolder(holder, adjPosition);
				}
			}
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
			if (isHeader(position) || isRefreshHeader(position)) return;

			int adjPosition = position - (getHeadersCount() + 1);
			int adapterCount;
			if (adapter != null) {
				adapterCount = adapter.getItemCount();
				if (adjPosition < adapterCount) {
					if (payloads.isEmpty()) {
						adapter.onBindViewHolder(holder, adjPosition);
					}
					else {
						adapter.onBindViewHolder(holder, adjPosition, payloads);
					}
				}
			}
		}

		@Override
		public int getItemCount() {
			int adjLen = (loadingMoreEnabled ? 2 : 1);
			int headerCounts = getHeadersCount();
			return adapter != null
					? headerCounts + adapter.getItemCount() + adjLen
					: headerCounts + adjLen;
		}

		@Override
		public int getItemViewType(int position) {
			int adjPosition = position - (getHeadersCount() + 1);
			if (isRefreshHeader(position)) {
				return TYPE_REFRESH_HEADER;
			}
			if (isHeader(position)) {
				position = position - 1;
				return sHeaderTypes.get(position);
			}
			if (isFooter(position)) {
				return TYPE_FOOTER;
			}
			int adapterCount;
			if (adapter != null) {
				adapterCount = adapter.getItemCount();
				if (adjPosition < adapterCount) {
					int type = adapter.getItemViewType(adjPosition);
					if (isReservedItemViewType(type)) {
						throw new IllegalStateException("XRecyclerView require itemViewType in adapter should be less than 10000 ");
					}
					return type;
				}
			}
			return 0;
		}

		@Override
		public long getItemId(int position) {
			if (adapter != null && position >= getHeadersCount() + 1) {
				int adjPosition = position - (getHeadersCount() + 1);
				if (adjPosition < adapter.getItemCount()) {
					return adapter.getItemId(adjPosition);
				}
			}
			return -1;
		}


		@Override
		public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
			return adapter.onFailedToRecycleView(holder);
		}

		@Override
		public void unregisterAdapterDataObserver(AdapterDataObserver observer) {
			adapter.unregisterAdapterDataObserver(observer);
		}

		@Override
		public void registerAdapterDataObserver(AdapterDataObserver observer) {
			adapter.registerAdapterDataObserver(observer);
		}

		private class SimpleViewHolder extends RecyclerView.ViewHolder {

			public SimpleViewHolder(View itemView) {
				super(itemView);
			}
		}
	}

	public void setLoadingListener(LoadingListener listener) {
		mLoadingListener = listener;
	}

	public interface LoadingListener {

		void onRefresh();

		void onLoadMore();
	}

	public class SimpleLoadingListener implements LoadingListener {

		@Override public void onRefresh() {
		}

		@Override public void onLoadMore() {
		}
	}
}