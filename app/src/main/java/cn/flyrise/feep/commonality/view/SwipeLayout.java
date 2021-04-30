package cn.flyrise.feep.commonality.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import cn.flyrise.feep.core.common.FELog;

/**
 * @author ZYP
 * @since 2016/8/18 11:21
 */
public class SwipeLayout extends FrameLayout {

	private ViewDragHelper mViewDragHelper;
	private View mBackView;
	private View mFrontView;

	private int mRange;
	private int mViewWidth;
	private int mViewHeight;

	private boolean isOpen = false;

	public SwipeLayout(Context context) {
		this(context, null);
	}

	public SwipeLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mViewDragHelper = ViewDragHelper.create(this, mCallback);
	}

	//    @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
//        return mViewDragHelper.shouldInterceptTouchEvent(ev);
//    }
//
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		try {
			mViewDragHelper.processTouchEvent(event);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public void invalidate() {
		super.invalidate();

	}

	@Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		layoutContent(false);
	}

	private void layoutContent(boolean isOpen) {
		Rect rect = computeFrontRect(isOpen);
		mFrontView.layout(rect.left, rect.top, rect.right, rect.bottom);
		Rect backRect = computeBackRectViaFront(rect);
		mBackView.layout(backRect.left, backRect.top, backRect.right, backRect.bottom);
	}

	private Rect computeBackRectViaFront(Rect rect) {
		int left = rect.right;
		return new Rect(left, 0, left + mRange, mViewHeight);
	}

	private Rect computeFrontRect(boolean isOpen) {
		int left = 0;
		if (isOpen) {
			left = -mRange;
		}
		return new Rect(left, 0, left + mViewWidth, mViewHeight);
	}

	@Override protected void onFinishInflate() {
		super.onFinishInflate();
		mBackView = getChildAt(0);
		mFrontView = getChildAt(1);
	}

	@Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mRange = mBackView.getMeasuredWidth();
		mViewWidth = getMeasuredWidth();
		mViewHeight = getMeasuredHeight();
	}

	ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {

		@Override public boolean tryCaptureView(View child, int pointerId) {
			return false;
		}

		@Override public int getViewHorizontalDragRange(View child) {
			return mRange;
		}

		@Override
		public int clampViewPositionHorizontal(View child, int left, int dx) {
			if (child == mFrontView) {
				if (left > 0) {
					left = 0;
				}
				else if (left < -mRange) {
					left = -mRange;
				}
			}
			else if (child == mBackView) {
				if (left < (mViewWidth - mRange)) {
					left = mViewWidth - mRange;
				}
				else if (left > mViewWidth) {
					left = mViewWidth;
				}
			}
			return left;
		}

		@Override
		public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
			super.onViewPositionChanged(changedView, left, top, dx, dy);
			if (changedView == mFrontView) {
				mBackView.offsetLeftAndRight(dx);
			}
			else if (changedView == mBackView) {
				mFrontView.offsetLeftAndRight(dx);
			}
			dispatchDragEvent();
			invalidate();
		}

		@Override
		public void onViewReleased(View releasedChild, float xvel, float yvel) {
			super.onViewReleased(releasedChild, xvel, yvel);
			if (xvel == 0 && mFrontView.getLeft() < -mRange * 0.5f) {
				open();
			}
			else if (xvel < 0) {
				open();
			}
			else {
				close();
			}
		}
	};

	private void dispatchDragEvent() {
		Status lastStatus = status;
		status = updateStatus();
		if (lastStatus != status && onSwipeListener != null) {
			if (status == Status.Open) {
				onSwipeListener.onOpen(this);
			}
			else if (status == Status.Close) {
				onSwipeListener.onClose(this);
			}
			else if (status == Status.Swiping) {
				if (lastStatus == Status.Close) {
					onSwipeListener.onStartOpen(this);
				}
				else if (lastStatus == Status.Open) {
					onSwipeListener.onStartClose(this);
				}
			}
		}
	}

	private Status updateStatus() {
		int left = mFrontView.getLeft();
		if (left == 0) {
			return Status.Close;
		}
		else if (left == -mRange) {
			return Status.Open;
		}
		return Status.Swiping;
	}

	public void open() {
		FELog.i("-->>>>>adapter222：--open");
		if (mViewDragHelper.smoothSlideViewTo(mFrontView, -mRange, 0)) {
			ViewCompat.postInvalidateOnAnimation(this);
		}
		isOpen = true;
	}

	public void close() {
		FELog.i("-->>>>>adapter333：--close");
		if (mViewDragHelper.smoothSlideViewTo(mFrontView, 0, 0)) {
			ViewCompat.postInvalidateOnAnimation(this);
		}
		isOpen = false;
	}

	@Override
	public void computeScroll() {
		super.computeScroll();
		if (mViewDragHelper.continueSettling(true)) {
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}

	public int getRange() {
		return this.mRange;
	}

	public boolean isOpen() {
		return this.isOpen;
	}

	public enum Status {
		Open, Close, Swiping
	}

	private Status status = Status.Close;

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public interface OnSwipeListener {

		void onOpen(SwipeLayout swipeLayout);

		void onClose(SwipeLayout swipeLayout);

		void onStartOpen(SwipeLayout swipeLayout);

		void onStartClose(SwipeLayout swipeLayout);
	}

	public class OnSwipeListenerAdapter implements OnSwipeListener {

		@Override public void onOpen(SwipeLayout swipeLayout) {

		}

		@Override public void onClose(SwipeLayout swipeLayout) {

		}

		@Override public void onStartOpen(SwipeLayout swipeLayout) {

		}

		@Override public void onStartClose(SwipeLayout swipeLayout) {

		}
	}

	private OnSwipeListener onSwipeListener;

	public void setOnSwipeListener(OnSwipeListener onSwipeListener) {
		this.onSwipeListener = onSwipeListener;
	}
}
