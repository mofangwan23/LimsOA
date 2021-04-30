package cn.flyrise.feep.core.base.views;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import cn.flyrise.feep.core.base.views.adapter.BaseRecyclerAdapter;
import cn.flyrise.feep.core.common.FELog;

/**
 * @author ZYP
 * @since 2016/7/11 11:26
 */
public class LoadMoreRecyclerView extends RecyclerView {

	private OnLoadMoreListener mLoadMoreListener;

	private OnScrollStateTouchScroll mScrollStateTouchListener;

	public LoadMoreRecyclerView(Context context) {
		this(context, null);
	}

	public LoadMoreRecyclerView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LoadMoreRecyclerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		super.addOnScrollListener(new OnScrollListener() {
//			boolean isSlidingToLast = false;

			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				if (newState == RecyclerView.SCROLL_STATE_IDLE) {
					//获取最后一个完全显示的ItemPosition
//					int lastVisibleItem = getLastVisiblePosition();
//					int totalItemCount = recyclerView.getAdapter().getItemCount();
					FELog.i("-->>>>>滚动到底部：" + canScrollVertically(1));
					// 判断是否滚动到底部
//					if (lastVisibleItem == (totalItemCount - 1) && isSlidingToLast) {
					if (!canScrollVertically(1)) {
						FELog.i("-->>>>>滚动到底部：呵呵呵呵呵");
						//加载更多功能的代码
						if (mLoadMoreListener != null) {
							mLoadMoreListener.loadMore();
						}
					}
				}

				if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
					if (mScrollStateTouchListener != null) {
						mScrollStateTouchListener.scrollStateTouchScroll();
					}
				}
			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
//				isSlidingToLast = dy > 0;
			}
		});
	}

//	public int getLastVisiblePosition() {
//		int position;
//		LayoutManager layoutManager = getLayoutManager();
//		if (layoutManager instanceof LinearLayoutManager) {
//			position = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
//		}
//		else if (layoutManager instanceof GridLayoutManager) {
//			position = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
//		}
//		else if (layoutManager instanceof StaggeredGridLayoutManager) {
//			StaggeredGridLayoutManager sgLayoutManager = (StaggeredGridLayoutManager) layoutManager;
//			int[] lastPositions = sgLayoutManager.findLastVisibleItemPositions(new int[sgLayoutManager.getSpanCount()]);
//			position = getMaxPosition(lastPositions);
//		}
//		else {
//			position = layoutManager.getItemCount() - 1;
//		}
//		return position;
//	}

//	private int getMaxPosition(int[] positions) {
//		int maxPosition = Integer.MIN_VALUE;
//		for (int i = 0, len = positions.length; i < len; i++) {
//			maxPosition = Math.max(maxPosition, positions[i]);
//		}
//		return maxPosition;
//	}


	public void setOnLoadMoreListener(OnLoadMoreListener loadMoreListener) {
		this.mLoadMoreListener = loadMoreListener;
	}

	public void scrollLastItem2Bottom(BaseRecyclerAdapter adapter) {
		if (adapter == null) return;
		LinearLayoutManager layoutManager = (LinearLayoutManager) this.getLayoutManager();
		if (layoutManager == null) return;
		int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
		if (lastVisibleItem != adapter.getItemCount() - 1) return;
		View footView = layoutManager.findViewByPosition(lastVisibleItem);
		if (footView == null || layoutManager.findViewByPosition(lastVisibleItem - 1) == null) return;
		int itemHeight = layoutManager.findViewByPosition(lastVisibleItem - 1).getHeight();
		int listViewHeight = getHeight();
		int itemCount = listViewHeight / itemHeight;
		int offest = listViewHeight % itemHeight;
		layoutManager.scrollToPositionWithOffset(adapter.getDataSourceCount() - itemCount, offest);
	}

	//监听滚动中事件
	public void setOnScrollStateTouchListener(OnScrollStateTouchScroll scrollStateTouchScroll) {
		this.mScrollStateTouchListener = scrollStateTouchScroll;
	}

	public interface OnLoadMoreListener {

		void loadMore();
	}

	public interface OnScrollStateTouchScroll {

		void scrollStateTouchScroll();
	}
}
