package com.drop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import cn.flyrise.feep.core.base.views.BadgeView;
import cn.flyrise.feep.core.common.FELog;


public class WaterDrop extends RelativeLayout {

	private Paint mPaint = new Paint();
	private BadgeView mBadgeView;
	private DropCover.OnDragCompeteListener mOnDragCompeteListener;
	private boolean mHolderEventFlag;
	private boolean enable = true;

	public WaterDrop(Context context) {
		super(context);
		init();
	}

	public WaterDrop(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public void setText(String str) {
		mBadgeView.setText(str);
	}

	public String getText() {
		return mBadgeView.getText().toString();
	}

	public void setTextSize(int size) {
		mBadgeView.setTextSize(size);
	}

	@SuppressLint("NewApi")
	private void init() {
		mPaint.setAntiAlias(true);
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		mBadgeView = new BadgeView(getContext());
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mBadgeView.setLayoutParams(params);
		addView(mBadgeView);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (CoverManager.getInstance().isNull()) return false;
		ViewGroup parent = getScrollableParent();
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (mOnDragCompeteListener != null) mOnDragCompeteListener.onDownDrag(true);
				if (!enable) return false;
				mHolderEventFlag = !CoverManager.getInstance().isRunning();
				FELog.i("-->>>>flage:" + mHolderEventFlag);
				if (mHolderEventFlag) {
					if (parent != null) parent.requestDisallowInterceptTouchEvent(true);
					CoverManager.getInstance().start(this, mOnDragCompeteListener);
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (mHolderEventFlag) CoverManager.getInstance().update(event.getRawX(), event.getRawY());
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				if (mOnDragCompeteListener != null) mOnDragCompeteListener.onDownDrag(false);
				if (mHolderEventFlag) {
					if (parent != null) parent.requestDisallowInterceptTouchEvent(false);
					CoverManager.getInstance().finish(this, event.getRawX(), event.getRawY());
				}
				break;
		}

		return true;
	}

	private ViewGroup getScrollableParent() {
		View target = this;
		while (true) {
			View parent;
			try {
				parent = (View) target.getParent();
			} catch (Exception e) {
				return null;
			}
			if (parent == null) return null;
			if (isViewGroup(parent)) return (ViewGroup) parent;
			target = parent;
		}
	}

	private boolean isViewGroup(View parent) {//跨过父控件拖动
		return parent instanceof ListView
				|| parent instanceof ScrollView
				|| parent instanceof GridView
				|| parent instanceof RecyclerView;
	}


	public void setOnDragCompeteListener(DropCover.OnDragCompeteListener onDragCompeteListener) {
		mOnDragCompeteListener = onDragCompeteListener;
	}

	//设置是否可以拖拽气泡
	public void setTouchEnable(boolean enable) {
		this.enable = enable;
	}
}
