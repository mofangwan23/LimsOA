package cn.flyrise.feep.commonality.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;
import cn.flyrise.feep.core.base.views.SwipeBackLayout;
import cn.flyrise.feep.core.common.FELog;

/**
 * @author ZYP
 * @since 2016-09-22 10:29
 * 主要用于嵌套在 ScrollView 中，在滑动过程中对事件拦截...
 */
public class TouchableWebView extends WebView {

	private float leftX = 0;
	private float leftY = 0;
	private SwipeBackLayout mSwipeBackLayout;

	private onTouchEventListener onTouchEventListener;

	public TouchableWebView(Context context) {
		this(context, null);
	}

	public TouchableWebView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TouchableWebView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void setOnTouchEventListener(onTouchEventListener onTouchEventListener) {
		this.onTouchEventListener = onTouchEventListener;
	}

	public void setSwipeBackLayout(SwipeBackLayout swipeBackLayout) {
		this.mSwipeBackLayout = swipeBackLayout;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (onTouchEventListener != null && onTouchEventListener.canIntercept(event)) {
			requestDisallowInterceptTouchEvent(true);
		}
		else if (event.getPointerCount() >= 2) {
			requestDisallowInterceptTouchEvent(true);
		}
		else if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (mSwipeBackLayout != null) mSwipeBackLayout.setAbleToSwipe(false);
			leftX = event.getX();
			leftY = event.getY();
		}
		else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			requestDisallowInterceptTouchEvent(Math.abs(event.getX() - leftX) > Math.abs(event.getY() - leftY));
		}
		else {
			if (mSwipeBackLayout != null) mSwipeBackLayout.setAbleToSwipe(true);
			requestDisallowInterceptTouchEvent(false);
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
		super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
		requestDisallowInterceptTouchEvent(true);
	}

	public interface onTouchEventListener {

		boolean canIntercept(MotionEvent event);
	}
}
