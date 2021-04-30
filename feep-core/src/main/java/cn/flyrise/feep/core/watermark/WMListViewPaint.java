package cn.flyrise.feep.core.watermark;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * @author ZYP
 * @since 2017-09-06 15:15
 */
public class WMListViewPaint<T> extends WMAbstractPaint<T, ListView> {

	public WMListViewPaint(T target, ListView dependView, WMCanvasCreator<T> creator, String text) {
		super(target, dependView, creator, text);
	}

	@Override public int measureWaterMarkContainerHeight() {
		ListAdapter adapter = mDependView.getAdapter();
		int count = adapter.getCount();
		if (count == 0) {
			return 0;
		}

		int visiblePosition = mDependView.getFirstVisiblePosition();
		visiblePosition = count > 1 ? visiblePosition + 1 : visiblePosition;
		View visibleView = adapter.getView(visiblePosition, null, mDependView); // 这里 +1 预防取到不存在的 head view.
		try {
			visibleView.measure(0, 0);
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		return visibleView.getMeasuredHeight() * count;
	}

	@Override public void dispatchScrollEvent() {
		mDependView.setOnScrollListener(new AbsListView.OnScrollListener() {
			ViewGroup container = getWaterMarkContainer();

			@Override public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				int top = getScrollY(view);
				container.scrollTo(0, top);
			}
		});
	}

	int getScrollY(AbsListView view) {
		View c = view.getChildAt(0);
		if (c == null) {
			return 0;
		}
		int firstVisiblePosition = view.getFirstVisiblePosition();
		int top = c.getTop();
		return -top + firstVisiblePosition * c.getHeight();
	}
}
