package com.drag.framework;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import java.util.LinkedList;
import java.util.List;

public class DragGridView extends GridView {

	private boolean isMove = false;
	private boolean isShowDelete = false;

	private Animation createFastRotateAnimation() {
		Animation rotate = new RotateAnimation(-2.0f, 2.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

		rotate.setRepeatMode(Animation.REVERSE);
		rotate.setRepeatCount(Animation.INFINITE);
		rotate.setDuration(60);
		rotate.setInterpolator(new AccelerateDecelerateInterpolator());

		return rotate;
	}

	private int getItemViewCount() {
		// -1 to remove the DeleteZone from the loop
		return getChildCount() - 1;
	}

	public void animateMoveAllItems() {
		Animation rotateAnimation = createFastRotateAnimation();

		for (int i = 0; i < getItemViewCount(); i++) {
			View child = getChildAt(i);
			child.startAnimation(rotateAnimation);
		}
	}

	public void cancelAnimations() {
		for (int i = 0; i < getItemViewCount() - 2; i++) {
			View child = getChildAt(i);
			child.clearAnimation();
		}
	}

	/**
	 * DragGridView的item长按响应的时间， 默认是1500毫秒，也可以自行设置
	 */
	private long dragResponseMS = 1000;

	/**
	 * 是否可以拖拽，默认不可以
	 */
	private boolean isDrag = false;

	private int mDownX;
	private int mDownY;
	private int moveX;
	private int moveY;
	/**
	 * 正在拖拽的position
	 */
	private int mDragPosition;

	/**
	 * 刚开始拖拽的item对应的View
	 */
	private View mStartDragItemView = null;

	/**
	 * 用于拖拽的镜像，这里直接用一个ImageView
	 */
	private ImageView mDragImageView;

	/**
	 * 震动器
	 */
	private Vibrator mVibrator;

	private WindowManager mWindowManager;
	/**
	 * item镜像的布局参数
	 */
	private WindowManager.LayoutParams mWindowLayoutParams;

	/**
	 * 我们拖拽的item对应的Bitmap
	 */
	private Bitmap mDragBitmap;

	/**
	 * 按下的点到所在item的上边缘的距离
	 */
	private int mPoint2ItemTop;

	/**
	 * 按下的点到所在item的左边缘的距离
	 */
	private int mPoint2ItemLeft;

	/**
	 * DragGridView距离屏幕顶部的偏移量
	 */
	private int mOffset2Top;

	/**
	 * DragGridView距离屏幕左边的偏移量
	 */
	private int mOffset2Left;

	/**
	 * 状态栏的高度
	 */
	private int mStatusHeight;

	/**
	 * DragGridView自动向下滚动的边界值
	 */
	private int mDownScrollBorder;

	/**
	 * DragGridView自动向上滚动的边界值
	 */
	private int mUpScrollBorder;

	/**
	 * DragGridView自动滚动的速度
	 */
	private static final int speed = 20;

	private boolean mAnimationEnd = true;

	private DragGridBaseAdapter mDragAdapter;
	private int mNumColumns;
	private int mColumnWidth;
	private boolean mNumColumnsSet;
	private int mHorizontalSpacing;

	public void setMove(boolean isMove) {
		this.isMove = isMove;
	}

	public DragGridView(Context context) {
		this(context, null);
	}

	public DragGridView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DragGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		mStatusHeight = getStatusHeight(context); // 获取状态栏的高度

		if (!mNumColumnsSet) {
			mNumColumns = AUTO_FIT;
		}

	}

	private Handler mHandler = new Handler();

	// 用来处理是否为长按的Runnable
	private Runnable mLongClickRunnable = new Runnable() {
		@Override
		public void run() {
			if (isMove) {
				isDrag = true; // 设置可以拖拽
				mVibrator.vibrate(50); // 震动一下
				mStartDragItemView.setVisibility(View.INVISIBLE);// 隐藏该item
				// 根据我们按下的点显示item镜像
				createDragImage(mDragBitmap, mDownX, mDownY);
			}
		}
	};

	public boolean isMove() {
		return isMove;
	}


	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);

		if (adapter instanceof DragGridBaseAdapter) {
			mDragAdapter = (DragGridBaseAdapter) adapter;
		}
		else {
			throw new IllegalStateException("the adapter must be implements DragGridAdapter");
		}
	}

	@Override
	public void setNumColumns(int numColumns) {
		super.setNumColumns(numColumns);
		mNumColumnsSet = true;
		this.mNumColumns = numColumns;
	}

	@Override
	public void setColumnWidth(int columnWidth) {
		super.setColumnWidth(columnWidth);
		mColumnWidth = columnWidth;
	}

	@Override
	public void setHorizontalSpacing(int horizontalSpacing) {
		super.setHorizontalSpacing(horizontalSpacing);
		this.mHorizontalSpacing = horizontalSpacing;
	}

	/**
	 * 若设置为AUTO_FIT，计算有多少列
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (mNumColumns == AUTO_FIT) {
			int numFittedColumns;
			if (mColumnWidth > 0) {
				int gridWidth = Math.max(MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight(), 0);
				numFittedColumns = gridWidth / mColumnWidth;
				if (numFittedColumns > 0) {
					while (numFittedColumns != 1) {
						if (numFittedColumns * mColumnWidth + (numFittedColumns - 1) * mHorizontalSpacing > gridWidth) {
							numFittedColumns--;
						}
						else {
							break;
						}
					}
				}
				else {
					numFittedColumns = 1;
				}
			}
			else {
				numFittedColumns = 2;
			}
			mNumColumns = numFittedColumns;
		}

//		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}

	/**
	 * 设置响应拖拽的毫秒数，默认是1500毫秒
	 */
	public void setDragResponseMS(long dragResponseMS) {
		this.dragResponseMS = dragResponseMS;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mDownX = (int) ev.getX();
				mDownY = (int) ev.getY();

				// 根据按下的X,Y坐标获取所点击item的position
				mDragPosition = pointToPosition(mDownX, mDownY);

				if (mDragPosition == AdapterView.INVALID_POSITION) {
					return super.dispatchTouchEvent(ev);
				}

				// 使用Handler延迟dragResponseMS执行mLongClickRunnable
				mHandler.postDelayed(mLongClickRunnable, dragResponseMS);
				// 根据position获取该item所对应的View
				mStartDragItemView = getChildAt(mDragPosition - getFirstVisiblePosition());
				// 下面这几个距离大家可以参考我的博客上面的图来理解下
				mPoint2ItemTop = mDownY - mStartDragItemView.getTop();
				mPoint2ItemLeft = mDownX - mStartDragItemView.getLeft();

				mOffset2Top = (int) (ev.getRawY() - mDownY);
				mOffset2Left = (int) (ev.getRawX() - mDownX);

				// 获取DragGridView自动向上滚动的偏移量，小于这个值，DragGridView向下滚动
				mDownScrollBorder = getHeight() / 5;
				// 获取DragGridView自动向下滚动的偏移量，大于这个值，DragGridView向上滚动
				mUpScrollBorder = getHeight() * 4 / 5;

				// 开启mDragItemView绘图缓存
				mStartDragItemView.setDrawingCacheEnabled(true);
				// 获取mDragItemView在缓存中的Bitmap对象
				mDragBitmap = Bitmap.createBitmap(mStartDragItemView.getDrawingCache());
				// 这一步很关键，释放绘图缓存，避免出现重复的镜像
				mStartDragItemView.destroyDrawingCache();
				break;
			case MotionEvent.ACTION_MOVE:
				int moveX = (int) ev.getX();
				int moveY = (int) ev.getY();

				// 如果我们在按下的item上面移动，只要不超过item的边界我们就不移除mRunnable
				if (!isTouchInItem(mStartDragItemView, moveX, moveY)) {
					mHandler.removeCallbacks(mLongClickRunnable);
				}
				break;
			case MotionEvent.ACTION_UP:
				isMove = false;
				mHandler.removeCallbacks(mLongClickRunnable);
				mHandler.removeCallbacks(mScrollRunnable);
				break;
			default:
				break;
		}
		return super.dispatchTouchEvent(ev);
	}

	/**
	 * 是否点击在GridView的item上面
	 */
	private boolean isTouchInItem(View dragView, int x, int y) {
		if (dragView == null) {
			return false;
		}
		int leftOffset = dragView.getLeft();
		int topOffset = dragView.getTop();
		if (x < leftOffset || x > leftOffset + dragView.getWidth()) {
			return false;
		}

		return !(y < topOffset || y > topOffset + dragView.getHeight());

	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (isDrag && mDragImageView != null) {
			switch (ev.getAction()) {
				case MotionEvent.ACTION_MOVE:
					isShowDelete = true;
					moveX = (int) ev.getX();
					moveY = (int) ev.getY();

					// 拖动item
					onDragItem(moveX, moveY);
					break;
				case MotionEvent.ACTION_UP:
					onStopDrag();
					if (isShowDelete) {
						isShowDelete = false;
						mDragAdapter.setIsShowDelete(isShowDelete);
					}
					isDrag = false;
					break;
			}
			return true;
		}
		return super.onTouchEvent(ev);
	}

	/**
	 * 创建拖动的镜像
	 * @param downX 按下的点相对父控件的X坐标
	 * @param downY 按下的点相对父控件的X坐标
	 */
	private void createDragImage(Bitmap bitmap, int downX, int downY) {
		mWindowLayoutParams = new WindowManager.LayoutParams();
		mWindowLayoutParams.format = PixelFormat.TRANSLUCENT; // 图片之外的其他地方透明
		mWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
		mWindowLayoutParams.x = downX - mPoint2ItemLeft + mOffset2Left;
		mWindowLayoutParams.y = downY - mPoint2ItemTop + mOffset2Top - mStatusHeight;
		mWindowLayoutParams.alpha = 0.55f; // 透明度
		mWindowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		mWindowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

		mDragImageView = new ImageView(getContext());
		mDragImageView.setImageBitmap(bitmap);
		mWindowManager.addView(mDragImageView, mWindowLayoutParams);
	}

	/**
	 * 从界面上面移动拖动镜像
	 */
	private void removeDragImage() {
		if (mDragImageView != null) {
			mWindowManager.removeView(mDragImageView);
			mDragImageView = null;
		}
	}

	/**
	 * 拖动item，在里面实现了item镜像的位置更新，item的相互交换以及GridView的自行滚动
	 */
	private void onDragItem(int moveX, int moveY) {
		mWindowLayoutParams.x = moveX - mPoint2ItemLeft + mOffset2Left;
		mWindowLayoutParams.y = moveY - mPoint2ItemTop + mOffset2Top - mStatusHeight;
		mWindowManager.updateViewLayout(mDragImageView, mWindowLayoutParams); // 更新镜像的位置
		onSwapItem(moveX, moveY);

		// GridView自动滚动
		mHandler.post(mScrollRunnable);
	}

	/**
	 * 当moveY的值大于向上滚动的边界值，触发GridView自动向上滚动
	 * 当moveY的值小于向下滚动的边界值，触发GridView自动向下滚动
	 * 否则不进行滚动
	 */
	private Runnable mScrollRunnable = new Runnable() {

		@Override
		public void run() {
			int scrollY;
			if (getFirstVisiblePosition() == 0 || getLastVisiblePosition() == getCount() - 1) {
				mHandler.removeCallbacks(mScrollRunnable);
			}

			if (moveY > mUpScrollBorder) {
				scrollY = speed;
				mHandler.postDelayed(mScrollRunnable, 25);
			}
			else if (moveY < mDownScrollBorder) {
				scrollY = -speed;
				mHandler.postDelayed(mScrollRunnable, 25);
			}
			else {
				scrollY = 0;
				mHandler.removeCallbacks(mScrollRunnable);
			}

			smoothScrollBy(scrollY, 10);
		}
	};

	/**
	 * 交换item,并且控制item之间的显示与隐藏效果
	 */
	private void onSwapItem(int moveX, int moveY) {
		// 获取我们手指移动到的那个item的position
		final int tempPosition = pointToPosition(moveX, moveY);
		// 假如tempPosition 改变了并且tempPosition不等于-1,则进行交换
		if (tempPosition != mDragPosition && tempPosition != AdapterView.INVALID_POSITION && mAnimationEnd) {
			mDragAdapter.reorderItems(mDragPosition, tempPosition);
			mDragAdapter.setHideItem(tempPosition);

			final ViewTreeObserver observer = getViewTreeObserver();
			observer.addOnPreDrawListener(new OnPreDrawListener() {

				@Override
				public boolean onPreDraw() {
					observer.removeOnPreDrawListener(this);
					animateReorder(mDragPosition, tempPosition);
					mDragPosition = tempPosition;
					return true;
				}
			});

		}
	}

	/**
	 * 创建移动动画
	 */
	private AnimatorSet createTranslationAnimations(View view, float startX, float endX, float startY, float endY) {
		ObjectAnimator animX = ObjectAnimator.ofFloat(view, "translationX", startX, endX);
		ObjectAnimator animY = ObjectAnimator.ofFloat(view, "translationY", startY, endY);
		AnimatorSet animSetXY = new AnimatorSet();
		animSetXY.playTogether(animX, animY);
		return animSetXY;
	}

	/**
	 * item的交换动画效果
	 */
	private void animateReorder(final int oldPosition, final int newPosition) {
		boolean isForward = newPosition > oldPosition;
		List<Animator> resultList = new LinkedList<>();
		if (isForward) {
			for (int pos = oldPosition; pos < newPosition; pos++) {
				View view = getChildAt(pos - getFirstVisiblePosition());
				System.out.println(pos);

				if ((pos + 1) % mNumColumns == 0) {
					resultList.add(createTranslationAnimations(view, -view.getWidth() * (mNumColumns - 1), 0, view.getHeight(), 0));
				}
				else {
					resultList.add(createTranslationAnimations(view, view.getWidth(), 0, 0, 0));
				}
			}
		}
		else {
			for (int pos = oldPosition; pos > newPosition; pos--) {
				View view = getChildAt(pos - getFirstVisiblePosition());
				if ((pos + mNumColumns) % mNumColumns == 0) {
					resultList.add(createTranslationAnimations(view, view.getWidth() * (mNumColumns - 1), 0, -view.getHeight(), 0));
				}
				else {
					resultList.add(createTranslationAnimations(view, -view.getWidth(), 0, 0, 0));
				}
			}
		}

		AnimatorSet resultSet = new AnimatorSet();
		resultSet.playTogether(resultList);
		resultSet.setDuration(300);
		resultSet.setInterpolator(new AccelerateDecelerateInterpolator());
		resultSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				mAnimationEnd = false;
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				mAnimationEnd = true;
			}
		});
		resultSet.start();
	}

	/**
	 * 停止拖拽我们将之前隐藏的item显示出来，并将镜像移除
	 */
	private void onStopDrag() {
		View view = getChildAt(mDragPosition - getFirstVisiblePosition());
		if (view != null) {
			view.setVisibility(View.VISIBLE);
		}
		mDragAdapter.setHideItem(-1);
		removeDragImage();
		if (mDragCompletedListener != null) {
			this.mDragCompletedListener.onDragComleted();
		}
	}

	private OnDragCompletedListener mDragCompletedListener;

	public void setOnDragCompletedListener(OnDragCompletedListener listener) {
		this.mDragCompletedListener = listener;
	}

	public interface OnDragCompletedListener {

		void onDragComleted();
	}

	/**
	 * 获取状态栏的高度
	 */
	private static int getStatusHeight(Context context) {
		int statusHeight = 0;
		Rect localRect = new Rect();
		((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
		statusHeight = localRect.top;
		if (0 == statusHeight) {
			Class<?> localClass;
			try {
				localClass = Class.forName("com.android.internal.R$dimen");
				Object localObject = localClass.newInstance();
				int i5 = Integer.parseInt(localClass.getField("status_bar_height").get(localObject).toString());
				statusHeight = context.getResources().getDimensionPixelSize(i5);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return statusHeight;
	}

}
