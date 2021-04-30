package cn.flyrise.feep.core.watermark;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.PixelUtil;

/**
 * @author ZYP
 * @since 2017-09-06 15:15
 */
public class WMRecyclerViewPaint<T> extends WMAbstractPaint<T, RecyclerView> {

	private LinearLayoutManager mLayoutManager;
	private WMScrollListener mScrollListener;

	public WMRecyclerViewPaint(T target, RecyclerView dependView, WMCanvasCreator<T> creator, String text) {
		super(target, dependView, creator, text);
		this.mLayoutManager = (LinearLayoutManager) dependView.getLayoutManager();
	}

	@Override public int measureWaterMarkContainerHeight() {
		FELog.i("measureWaterMarkContainerHeight()");
		Adapter adapter = mDependView.getAdapter();
		int itemCount = adapter.getItemCount();
		if (itemCount == 0) {
			return 0;
		}

		Object tag = mDependView.getTag();
		if (tag != null && TextUtils.equals(tag.toString(), "FeiLaoMian")) {
			return PixelUtil.dipToPx(72) * itemCount;
		}

		int visiblePosition = mLayoutManager.findFirstVisibleItemPosition();
		View visibleView = mLayoutManager.findViewByPosition(visiblePosition);
		return visibleView.getHeight() * itemCount;
	}

	@Override public void dispatchScrollEvent() {
		if (mScrollListener == null) {
			mScrollListener = new WMScrollListener(getWaterMarkContainer());
		}

		mDependView.removeOnScrollListener(mScrollListener);
		mDependView.addOnScrollListener(mScrollListener);
	}

	private class WMScrollListener extends OnScrollListener {

		private ViewGroup container;

		public WMScrollListener(ViewGroup container) {
			this.container = container;
		}

		@Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
			if (dy != 0) {
				container.scrollBy(dx, dy);
			}
			else {
				int scrollDistance = getScrollDistance();
				container.scrollTo(0, scrollDistance);
			}
		}

		@Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
			if (newState == RecyclerView.SCROLL_STATE_IDLE) {
				int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();
				if (firstVisibleItemPosition == 0 && !recyclerView.canScrollVertically(-1)) {
					container.scrollTo(0, 0);
					return;
				}

				int lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
				int totalItemCount = recyclerView.getAdapter().getItemCount();
				if (lastVisibleItem == (totalItemCount - 1)) {
					container.scrollTo(0, container.getHeight() - recyclerView.getHeight());
				}
			}
		}
	}

	private int getScrollDistance() {
		FELog.i("getScrollDistance()");
		try {
			int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();
			View firstVisibleItem = mLayoutManager.getChildAt(0);
			int firstItemHeight = firstVisibleItem.getMeasuredHeight();
			return (firstVisibleItemPosition + 1) * firstItemHeight;
		} catch (Exception exp) {
			exp.printStackTrace();
			return 0;
		}
	}
}
