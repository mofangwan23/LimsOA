package cn.flyrise.feep.main.modules;

import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import java.util.NoSuchElementException;

/**
 * @author 社会主义接班人
 * @since 2018-09-07 16:45
 */
public class ApplicationLayout extends LinearLayout {


	private View mHeaderView;
	private View mContentView;
	private int mTouchSlop;

	private View mHeaderBarShrink;          // Header Bar 收缩的状态
	private View mHeaderBarExpand;          // Header Bar 展开的状态

	private View mHeaderContent;
	private View mHeaderContentMenu;        // 菜单
	private View mHeaderContentBackground;  // 中间的图片

	private int mHeaderHeight;              // 某时某刻 Header 的高度
	private int mHeaderMaxHeight;           // Header 的最大高度
	private int mHeaderMinHeight;           // Header 的最小高度
	private int mOriginHeaderHeight;

	private int mWidth;
	private VelocityTracker mVelocityTracker;       // 速度检测器    niubi

	private OnHeaderStatusChangeListener mHeaderStatusChangeListener;
	private ParentInterceptTouchEventCallback mInterceptTouchEventListener;

	private boolean enableScroll;

	public void setEnableScroll(boolean enableScroll) {
		this.enableScroll = enableScroll;

		postDelayed(() -> {
			if (enableScroll) {
				mHeaderView.getLayoutParams().height = mHeaderMaxHeight;
				smoothScroll(mHeaderHeight, mHeaderMaxHeight);//需求为每次进入，都重新展开快捷签到卡片
			}
			else {
				mHeaderBarExpand.setAlpha(1.0f);
				mHeaderView.getLayoutParams().height = mHeaderMinHeight;
				mHeaderView.requestLayout();
			}
		}, 320);
	}

	public void setParentInterceptTouchEventCallback(ParentInterceptTouchEventCallback callback) {
		this.mInterceptTouchEventListener = callback;
	}

	public void setOnHeaderStatusChangeListener(OnHeaderStatusChangeListener listener) {
		this.mHeaderStatusChangeListener = listener;
	}

	public ApplicationLayout(Context context) {
		this(context, null);
	}

	public ApplicationLayout(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ApplicationLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setOrientation(VERTICAL);
		this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		this.mVelocityTracker = VelocityTracker.obtain();

		ViewTreeObserver viewTreeObserver = getViewTreeObserver();
		viewTreeObserver.addOnPreDrawListener(new OnPreDrawListener() {
			@Override public boolean onPreDraw() {
				getViewTreeObserver().removeOnPreDrawListener(this);
				mWidth = getMeasuredWidth();
				if (mHeaderView == null || mContentView == null) {
					throw new NoSuchElementException("No such header or content view.");
				}

				View expandHeaderBar = mHeaderView.findViewById(R.id.view_header_bar_expand);
				if (expandHeaderBar == null) {
					throw new NoSuchElementException("Cannot find header bar in this view.");
				}

				int paddingTop = 0;
				if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT && !DevicesUtil.isSpecialDevice()) {
					paddingTop = DevicesUtil.getStatusBarHeight(getContext());
				}

				mOriginHeaderHeight = expandHeaderBar.getMeasuredHeight();
				mHeaderMinHeight = mOriginHeaderHeight + paddingTop;
				mHeaderMaxHeight = mHeaderView.getMeasuredHeight();
				mHeaderHeight = mHeaderMaxHeight;
				return true;
			}
		});
	}

	@Override protected void onFinishInflate() {
		super.onFinishInflate();
		if (getChildCount() != 2) {
			throw new IllegalArgumentException("This layout must be has two child: header and content.");
		}

		mHeaderView = getChildAt(0);
		mContentView = getChildAt(1);

		mHeaderBarShrink = mHeaderView.findViewById(R.id.view_header_bar_shrink);
		mHeaderBarExpand = mHeaderView.findViewById(R.id.view_header_bar_expand);

		mHeaderContent = mHeaderView.findViewById(R.id.view_header_content);
		mHeaderContentMenu = mHeaderView.findViewById(R.id.view_header_content_menu);
		mHeaderContentBackground = mHeaderView.findViewById(R.id.iv_header_content_background);
	}

	private int mLastInterceptY = 0;
	private int mLastY = 0;

	@Override public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (!enableScroll) return false;
		boolean isIntercepted = false;
		int motionY = (int) ev.getY();
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mLastInterceptY = motionY;
				mLastY = motionY;
				break;
			case MotionEvent.ACTION_MOVE:
				int deltaY = motionY - mLastInterceptY;
				if (Math.abs(deltaY) >= mTouchSlop) {
					// 处理拦截的逻辑
					// 如果 是最小状态，并且第一个是 0     不能拦截
					if (mHeaderHeight == mHeaderMinHeight) {
						isIntercepted = false;
						if (mInterceptTouchEventListener != null) {
							if (mInterceptTouchEventListener.isParentInterceptTouchEvent() && deltaY > 0) {
								isIntercepted = true;   // 父类拦截
							}
						}
					}
					else {
						isIntercepted = true;
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				mLastInterceptY = 0;
				break;
		}
		return isIntercepted;
	}

	@Override public boolean onTouchEvent(MotionEvent event) {
		if (!enableScroll) return false;
		mVelocityTracker.addMovement(event);
		int motionY = (int) event.getY();
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				break;
			case MotionEvent.ACTION_MOVE:
				int deltaY = motionY - mLastY;
				mHeaderHeight += deltaY;
				updateHeaderView();
				break;
			case MotionEvent.ACTION_UP:
				mVelocityTracker.computeCurrentVelocity(1000);
				int velocityY = (int) mVelocityTracker.getYVelocity();      // 获取 Y 方向上的加速度，
				// 向上为负数

				if (Math.abs(velocityY) >= 50) {
					if (velocityY < 0) {
						// 负数   上拉  快速关闭
						smoothScroll(mHeaderHeight, mHeaderMinHeight);
					}
					else {
						// 正数   下拉  快速打开
						smoothScroll(mHeaderHeight, mHeaderMaxHeight);
					}
				}
				else {
					// 判断是否已经过半，过半，展开或者收缩咯~
					if (mHeaderHeight >= mHeaderMaxHeight / 2) {
						// 展开
						smoothScroll(mHeaderHeight, mHeaderMaxHeight);
					}
					else {
						// 收缩
						smoothScroll(mHeaderHeight, mHeaderMinHeight);
					}
				}
				break;
		}
		this.mLastY = motionY;
		return true;
	}

	private void updateHeaderView() {
		if (mHeaderHeight < mHeaderMinHeight) {
			mHeaderHeight = mHeaderMinHeight;
		}
		else if (mHeaderHeight > mHeaderMaxHeight) {
			mHeaderHeight = mHeaderMaxHeight;
		}

		if (mHeaderHeight == mHeaderMaxHeight) {
			mHeaderBarShrink.setVisibility(View.GONE);
			if (mHeaderStatusChangeListener != null) {
				mHeaderStatusChangeListener.onHeaderStateChange(true);
			}
		}
		else if (mHeaderHeight == mHeaderMinHeight) {
			mHeaderBarExpand.setVisibility(View.GONE);
			if (mHeaderStatusChangeListener != null) {
				mHeaderStatusChangeListener.onHeaderStateChange(false);
			}
		}
		else {
			mHeaderBarShrink.setVisibility(View.VISIBLE);
			mHeaderBarExpand.setVisibility(View.VISIBLE);
		}

		float offset = mHeaderMaxHeight - mHeaderHeight;
		float _scale = offset / (mHeaderMaxHeight - mHeaderMinHeight);
		Log.i("scale", "" + _scale);
		mHeaderContentBackground.setScaleX(1 + _scale * 0.2f);
		mHeaderContentBackground.setScaleY(1 + _scale * 0.1f);

		float toAlpha = _scale;
		if (toAlpha > 0.0f) toAlpha += 0.3f;
		mHeaderBarExpand.setAlpha(1 - toAlpha);
		mHeaderBarShrink.setAlpha(toAlpha);
		mHeaderContentMenu.setAlpha(1 - toAlpha);
		mHeaderContentMenu.setTranslationX(-(mWidth * _scale));
		mHeaderContentMenu.setTranslationY(-((mHeaderMaxHeight - mHeaderMinHeight) * _scale));

		float x = (mHeaderHeight * 1.0f) / (mHeaderMaxHeight);
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mHeaderContent.getLayoutParams();
		params.topMargin = (int) (mHeaderMinHeight * x);
		mHeaderContent.requestLayout();

		// 更新啊
		mHeaderView.getLayoutParams().height = mHeaderHeight;
		mHeaderView.requestLayout();
	}

	private void smoothScroll(final int from, final int to) {
		final int frameCount = (int) (1000 / 1000f * 30 + 1);       // 计算帧数：设定1s 30帧
		final float partation = (to - from) / (float) frameCount;   // 计算每帧需要滚动的距离
		new Thread("ApplicationLayout") {
			@Override public void run() {
				for (int i = 0; i < frameCount; i++) {
					int h = 0;
					if (i == frameCount - 1) {
						h = to;
					}
					else {
						h = (int) (from + partation * i);
					}

					mHeaderHeight = h;
					post(new Runnable() {
						@Override public void run() {
							updateHeaderView();
						}
					});

					SystemClock.sleep(10);
				}
			}
		}.start();
	}

	public interface ParentInterceptTouchEventCallback {

		boolean isParentInterceptTouchEvent();
	}

	public interface OnHeaderStatusChangeListener {

		void onHeaderStateChange(boolean isExpand);
	}

}
