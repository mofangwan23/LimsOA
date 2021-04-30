package com.drop;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 新建：陈冕;
 * 日期： 2018-5-7-14:05.
 */

public class WaterDropSwipRefreshLayout extends SwipeRefreshLayout {

	private boolean isDropCover = false;

	public WaterDropSwipRefreshLayout(Context context) {
		super(context);
	}

	public WaterDropSwipRefreshLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}

	public void setMoveDrop(boolean isDropCover) {
		this.isDropCover = isDropCover;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return !isDropCover && super.onInterceptTouchEvent(ev);
	}
}
