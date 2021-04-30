package cn.flyrise.android.library.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.lang.ref.WeakReference;

import cn.flyrise.feep.R;

/**
 * <b>类功能描述：</b><div style="margin-left:40px;margin-top:-10px"> 开源的抽屉控件,详情可百度AndroidMiscWidgets </div>
 *
 * @author <a href="mailto:184618345@qq.com">017</a>
 * @version 1.0 </p> 修改时间：</br> 修改备注：</br>
 */
public class Panel extends LinearLayout {

    private static final String TAG = "Panel";

    private static final float MAXIMUM_MAJOR_VELOCITY = 200.0f;
    private static final float MAXIMUM_ACCELERATION = 2000.0f;
    private static final int MSG_ANIMATE = 1000;
    private static final int MSG_PREPARE_ANIMATE = 2000;
    private static final int ANIMATION_FRAME_DURATION = 1000 / 60;

    private Handler mHandler;
    private float mAnimatedAcceleration;
    private long mAnimationLastTime;
    private long mCurrentAnimationTime;
    private boolean mAnimating;

    private final int mMaximumMajorVelocity;
    private final int mMaximumAcceleration;

    private float lastRawX, lastRawY, curRawX, curRawY;
    private float lastEventTime, curEventTime;

    /**
     * Callback invoked when the panel is opened/closed.
     */
    public interface OnPanelListener {
        /**
         * Invoked when the panel becomes fully closed.
         */
        void onPanelClosed(Panel panel);

        /**
         * Invoked when the panel becomes fully opened.
         */
        void onPanelOpened(Panel panel);
    }

    private boolean mIsShrinking;
    private final int mPosition;
    private final int mDuration;
    private final boolean mLinearFlying;
    private final int mHandleId;
    private final int mContentId;

    private View mHandleView;

    private View mContent;
    private final Drawable mOpenedHandle;
    private final Drawable mClosedHandle;
    private float mTrackX;
    private float mTrackY;
    private float mVelocity;

    private OnPanelListener panelListener;

    public static final int TOP = 0;
    public static final int BOTTOM = 1;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;

    private enum State {
        ABOUT_TO_ANIMATE, ANIMATING, READY, TRACKING, FLYING, CLICK
    }

    private State mState;
    private final GestureDetector mGestureDetector;
    private int mContentHeight;
    private int mContentWidth;
    private final int mOrientation;
    private float mWeight;
    private final PanelOnGestureListener mGestureListener;
    private boolean mBringToFront;


    public Panel(Context context, AttributeSet attrs) {
        super(context, attrs);

        mHandler = new WeakHandler(this);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Panel);
        mDuration = a.getInteger(R.styleable.Panel_animationDuration, 750); // duration
        // defaults
        // to
        // 750
        // ms
        mPosition = a.getInteger(R.styleable.Panel_position, BOTTOM); // commonGroup
        // defaults
        // to
        // BOTTOM
        mLinearFlying = a.getBoolean(R.styleable.Panel_linearFlying, false); // linearFlying
        // defaults
        // to
        // false
        mWeight = a.getFraction(R.styleable.Panel_weight, 0, 1, 0.0f); // weight
        // defaults
        // to 0.0
        if (mWeight < 0 || mWeight > 1) {
            mWeight = 0.0f;
            Log.w(TAG, a.getPositionDescription() + ": weight must be > 0 and <= 1");
        }
        mOpenedHandle = a.getDrawable(R.styleable.Panel_openedHandle);
        mClosedHandle = a.getDrawable(R.styleable.Panel_closedHandle);

        RuntimeException e = null;
        mHandleId = a.getResourceId(R.styleable.Panel_handle, 0);
        if (mHandleId == 0) {
            e = new IllegalArgumentException(a.getPositionDescription() + ": The handle attribute is required and must refer to a valid child.");
        }
        mContentId = a.getResourceId(R.styleable.Panel_content, 0);
        if (mContentId == 0) {
            e = new IllegalArgumentException(a.getPositionDescription() + ": The content attribute is required and must refer to a valid child.");
        }
        a.recycle();

        final float density = getResources().getDisplayMetrics().density;
        mMaximumMajorVelocity = (int) (MAXIMUM_MAJOR_VELOCITY * density + 0.5f);
        mMaximumAcceleration = (int) (MAXIMUM_ACCELERATION * density + 0.5f);

        if (e != null) {
            throw e;
        }
        mOrientation = (mPosition == TOP || mPosition == BOTTOM) ? VERTICAL : HORIZONTAL;
        setOrientation(mOrientation);
        mState = State.READY;
        mGestureListener = new PanelOnGestureListener();
        mGestureDetector = new GestureDetector(mGestureListener);
        mGestureDetector.setIsLongpressEnabled(false);

        // i DON'T really know why i need this...
        setBaselineAligned(false);
    }

    /**
     * Sets the grammarResultListener that receives a notification when the panel becomes open/close.
     *
     * @param onPanelListener The grammarResultListener to be notified when the panel is opened/closed.
     */
    public void setOnPanelListener(OnPanelListener onPanelListener) {
        panelListener = onPanelListener;
    }

    /**
     * Gets Panel's mHandleView
     *
     * @return Panel's mHandleView
     */
    public View getHandle() {
        return mHandleView;
    }

    /**
     * Gets Panel's mContent
     *
     * @return Panel's mContent
     */
    public View getContent() {
        return mContent;
    }

    /**
     * Sets the acceleration curve for panel's animation.
     *
     * @param i The interpolator which defines the acceleration curve
     */
    public void setInterpolator(Interpolator i) {
    }

    /**
     * Set the opened state of Panel.
     *
     * @param open    True if Panel is to be opened, false if Panel is to be closed.
     * @param animate True if use animation, false otherwise.
     * @return True if operation was performed, false otherwise.
     */
    public boolean setOpen(boolean open, boolean animate) {
        if (mState == State.READY && isOpen() ^ open) {
            mIsShrinking = !open;
            if (animate) {
                mState = State.ABOUT_TO_ANIMATE;
                if (!mIsShrinking) {
                    // this could make flicker so we test mState in
                    // dispatchDraw()
                    // to see if is equal to ABOUT_TO_ANIMATE
                    mContent.setVisibility(VISIBLE);
                }
                final long now = SystemClock.uptimeMillis();
                mAnimationLastTime = now;
                mCurrentAnimationTime = now + ANIMATION_FRAME_DURATION;
                mAnimating = true;
                mHandler.removeMessages(MSG_ANIMATE);
                mHandler.removeMessages(MSG_PREPARE_ANIMATE);
                mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_PREPARE_ANIMATE), mCurrentAnimationTime);
            }
            else {
                mContent.setVisibility(open ? VISIBLE : GONE);
                postProcess();
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the opened status for Panel.
     *
     * @return True if Panel is opened, false otherwise.
     */
    public boolean isOpen() {
        return mContent.getVisibility() == VISIBLE;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHandleView = findViewById(mHandleId);
        if (mHandleView == null) {
            final String name = getResources().getResourceEntryName(mHandleId);
            throw new RuntimeException("Your Panel must have a child IView whose id attribute is 'R.id." + name + "'");
        }
        mHandleView.setClickable(true);
        mHandleView.setOnTouchListener(touchListener);
        // mHandleView.setOnClickListener(clickListener);

        mContent = findViewById(mContentId);
        if (mContent == null) {
            final String name = getResources().getResourceEntryName(mHandleId);
            throw new RuntimeException("Your Panel must have a child IView whose id attribute is 'R.id." + name + "'");
        }

        // reposition children
        removeView(mHandleView);
        removeView(mContent);
        if (mPosition == TOP || mPosition == LEFT) {
            addView(mContent);
            addView(mHandleView);
        }
        else {
            addView(mHandleView);
            addView(mContent);
        }

        if (mClosedHandle != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mHandleView.setBackground(mClosedHandle);
            }
            else {
                mHandleView.setBackgroundDrawable(mClosedHandle);
            }
        }
        mContent.setClickable(true);
        mContent.setVisibility(GONE);
        if (mWeight > 0) {
            final ViewGroup.LayoutParams params = mContent.getLayoutParams();
            if (mOrientation == VERTICAL) {
                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            }
            else {
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            }
            mContent.setLayoutParams(params);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        final ViewParent parent = getParent();
        if (parent != null && parent instanceof FrameLayout) {
            mBringToFront = true;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mWeight > 0 && mContent.getVisibility() == VISIBLE) {
            final View parent = (View) getParent();
            if (parent != null) {
                if (mOrientation == VERTICAL) {
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) (parent.getHeight() * mWeight), MeasureSpec.EXACTLY);
                }
                else {
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec((int) (parent.getWidth() * mWeight), MeasureSpec.EXACTLY);
                }
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mContentWidth = mContent.getWidth();
        mContentHeight = mContent.getHeight();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        // String name = getResources().getResourceEntryName(getId());
        // Log.d(TAG, name + " ispatchDraw " + mState);
        // this is why 'mState' was added:
        // avoid flicker before animation start
        if (mState == State.ABOUT_TO_ANIMATE && !mIsShrinking) {
            int delta = mOrientation == VERTICAL ? mContentHeight : mContentWidth;
            if (mPosition == LEFT || mPosition == TOP) {
                delta = -delta;
            }
            if (mOrientation == VERTICAL) {
                canvas.translate(0, delta);
            }
            else {
                canvas.translate(delta, 0);
            }
        }
        if (mState == State.TRACKING || mState == State.FLYING || mState == State.CLICK) {
            canvas.translate(mTrackX, mTrackY);
        }
        if (listener != null) {
            listener.onScroll(mContent.getWidth(), mContent.getHeight(), mTrackX, mTrackY);
        }
        super.dispatchDraw(canvas);
    }

    private float ensureRange(float v, int min, int max) {
        v = Math.max(v, min);
        v = Math.min(v, max);
        return v;
    }

    OnTouchListener touchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (mAnimating) {
                // we are animating
                return true;// 动画中不响应onTouch事件
            }
            final int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                if (mBringToFront) {
                    bringToFront();
                }
            }

            if (!mGestureDetector.onTouchEvent(event)) {
                if (action == MotionEvent.ACTION_UP) {
                    // tup up after scrolling

                    final long now = SystemClock.uptimeMillis();
                    mAnimationLastTime = now;
                    mCurrentAnimationTime = now + ANIMATION_FRAME_DURATION;
                    mAnimating = true;
                    mHandler.removeMessages(MSG_ANIMATE);
                    mHandler.removeMessages(MSG_PREPARE_ANIMATE);
                    mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_PREPARE_ANIMATE), mCurrentAnimationTime);
                }
            }
            return false;
        }
    };

    public boolean initChange() {
        if (mState != State.READY) {
            // we are animating or just about to animate
            return false;
        }
        mState = State.ABOUT_TO_ANIMATE;
        mIsShrinking = mContent.getVisibility() == VISIBLE;
        if (!mIsShrinking) {
            // this could make flicker so we test mState in dispatchDraw()
            // to see if is equal to ABOUT_TO_ANIMATE
            mContent.setVisibility(VISIBLE);
        }
        return true;
    }

    private void postProcess() {
        if (mIsShrinking && mClosedHandle != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mHandleView.setBackground(mClosedHandle);
            }
            else {
                mHandleView.setBackgroundDrawable(mClosedHandle);
            }
        }
        else if (!mIsShrinking && mOpenedHandle != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mHandleView.setBackground(mOpenedHandle);
            }
            else {
                mHandleView.setBackgroundDrawable(mOpenedHandle);
            }
        }
        // invoke grammarResultListener if any
        if (panelListener != null) {
            if (mIsShrinking) {
                panelListener.onPanelClosed(Panel.this);
            }
            else {
                panelListener.onPanelOpened(Panel.this);
            }
        }
    }

    class PanelOnGestureListener implements OnGestureListener {
        float scrollY;
        float scrollX;

        @Override
        public boolean onDown(MotionEvent e) {
            scrollX = scrollY = 0;
            lastRawX = curRawX = lastRawY = curRawY = -1;
            lastEventTime = curEventTime = -1;
            initChange();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            mState = State.FLYING;

            float velocityX2, velocityY2;
            if (lastRawX == -1 && lastRawY == -1) // 见onScroll方法
            {
                velocityX2 = (curRawX - e1.getRawX()) / (curEventTime - e1.getEventTime()) * 1000; // px/s
                velocityY2 = (curRawY - e1.getRawY()) / (curEventTime - e1.getEventTime()) * 1000;
            }
            else {
                velocityX2 = (curRawX - lastRawX) / (curEventTime - lastEventTime) * 1000;
                velocityY2 = (curRawY - lastRawY) / (curEventTime - lastEventTime) * 1000;
            }

            mVelocity = mOrientation == VERTICAL ? velocityY2 : velocityX2;

            if (Math.abs(mVelocity) > 50) {
                if (mVelocity > 0) {
                    mAnimatedAcceleration = mMaximumAcceleration;
                }
                else {
                    mAnimatedAcceleration = -mMaximumAcceleration;
                }

                final long now = SystemClock.uptimeMillis();
                mAnimationLastTime = now;
                mCurrentAnimationTime = now + ANIMATION_FRAME_DURATION;
                mAnimating = true;
                mHandler.removeMessages(MSG_ANIMATE);
                mHandler.removeMessages(MSG_PREPARE_ANIMATE);
                mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE), mCurrentAnimationTime);
                return true;
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // not used
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mState = State.TRACKING;
            float tmpY = 0, tmpX = 0;
            if (mOrientation == VERTICAL) {
                scrollY -= distanceY;
                if (mPosition == TOP) {
                    tmpY = ensureRange(scrollY, -mContentHeight, 0);
                }
                else {
                    tmpY = ensureRange(scrollY, 0, mContentHeight);
                }
            }
            else {
                scrollX -= distanceX;
                if (mPosition == LEFT) {
                    tmpX = ensureRange(scrollX, -mContentWidth, 0);
                }
                else {
                    tmpX = ensureRange(scrollX, 0, mContentWidth);
                }
            }

            if (tmpX != mTrackX || tmpY != mTrackY) {
                mTrackX = tmpX;
                mTrackY = tmpY;
                // invalidate(); //放在此导致极快速滑动至touch区域外界面不刷新（mTrackX、mTrackY均为0）
            }
            invalidate();

            lastRawX = curRawX;
            lastRawY = curRawY;
            lastEventTime = curEventTime;
            curRawX = e2.getRawX();
            curRawY = e2.getRawY();
            curEventTime = e2.getEventTime();
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            // not used
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // not used
            return false;
        }
    }

    private void prepareAnimation() {

        switch (mPosition) {
            case LEFT:
                if (mIsShrinking) {
                    mVelocity = -mMaximumMajorVelocity;
                    mAnimatedAcceleration = -mMaximumAcceleration;

                }
                else {
                    mVelocity = mMaximumMajorVelocity;
                    mAnimatedAcceleration = mMaximumAcceleration;
                    if (mTrackX == 0 && mState == State.ABOUT_TO_ANIMATE) {
                        mTrackX = -mContentWidth;
                    }
                }
                break;
            case RIGHT:
                if (mIsShrinking) {
                    mVelocity = mMaximumMajorVelocity;
                    mAnimatedAcceleration = mMaximumAcceleration;
                }
                else {
                    mVelocity = -mMaximumMajorVelocity;
                    mAnimatedAcceleration = -mMaximumAcceleration;

                    if (mTrackX == 0 && mState == State.ABOUT_TO_ANIMATE) {
                        mTrackX = mContentWidth;
                    }
                }
                break;
            case TOP:
                if (mIsShrinking) {
                    mVelocity = -mMaximumMajorVelocity;
                    mAnimatedAcceleration = -mMaximumAcceleration;
                }
                else {
                    mVelocity = mMaximumMajorVelocity;
                    mAnimatedAcceleration = mMaximumAcceleration;

                    if (mTrackX == 0 && mState == State.ABOUT_TO_ANIMATE) {
                        mTrackY = -mContentHeight;
                    }
                }
                break;
            case BOTTOM:
                if (mIsShrinking) {
                    mVelocity = mMaximumMajorVelocity;
                    mAnimatedAcceleration = mMaximumAcceleration;
                }
                else {
                    mVelocity = -mMaximumMajorVelocity;
                    mAnimatedAcceleration = -mMaximumAcceleration;

                    if (mTrackX == 0 && mState == State.ABOUT_TO_ANIMATE) {
                        mTrackY = mContentHeight;
                    }
                }
                break;
        }

        if (mState == State.TRACKING) {
            if (mIsShrinking) {
                if ((mOrientation == VERTICAL && Math.abs(mTrackY) < mContentHeight / 2) || (mOrientation == HORIZONTAL && Math.abs(mTrackX) < mContentWidth / 2)) {
                    mVelocity = -mVelocity;
                    mAnimatedAcceleration = -mAnimatedAcceleration;
                    mIsShrinking = !mIsShrinking;
                }
            }
            else {
                if ((mOrientation == VERTICAL && Math.abs(mTrackY) > mContentHeight / 2) || (mOrientation == HORIZONTAL && Math.abs(mTrackX) > mContentWidth / 2)) {
                    mVelocity = -mVelocity;
                    mAnimatedAcceleration = -mAnimatedAcceleration;
                    mIsShrinking = !mIsShrinking;
                }
            }
        }
        if (mState != State.FLYING && mState != State.TRACKING) {
            mState = State.CLICK;
        }
    }

    private void doAnimation() {

        if (mAnimating) {
            final long now = SystemClock.uptimeMillis();
            final float t = (now - mAnimationLastTime) / 1000.0f; // ms -> s
            final float v = mVelocity; // px/s
            final float a = mAnimatedAcceleration; // px/s/s
            mVelocity = v + (a * t); // px/s
            mAnimationLastTime = now;

            switch (mPosition) {
                case LEFT:
                    mTrackX = mTrackX + (v * t) + (0.5f * a * t * t); // px
                    if (mTrackX > 0) {
                        mTrackX = 0;
                        mState = State.READY;
                        mAnimating = false;
                    }
                    else if (mTrackX < -mContentWidth) {
                        mTrackX = -mContentWidth;
                        mContent.setVisibility(GONE);
                        mState = State.READY;
                        mAnimating = false;
                    }
                    break;
                case RIGHT:
                    mTrackX = mTrackX + (v * t) + (0.5f * a * t * t);
                    if (mTrackX < 0) {
                        mTrackX = 0;
                        mState = State.READY;
                        mAnimating = false;
                    }
                    else if (mTrackX > mContentWidth) {
                        mTrackX = mContentWidth;
                        mContent.setVisibility(GONE);
                        mState = State.READY;
                        mAnimating = false;
                    }
                    break;
                case TOP:
                    mTrackY = mTrackY + (v * t) + (0.5f * a * t * t);
                    if (mTrackY > 0) {
                        mTrackY = 0;
                        mState = State.READY;
                        mAnimating = false;
                    }
                    else if (mTrackY < -mContentHeight) {
                        mTrackY = -mContentHeight;
                        mContent.setVisibility(GONE);
                        mState = State.READY;
                        mAnimating = false;
                    }
                    break;
                case BOTTOM:
                    mTrackY = mTrackY + (v * t) + (0.5f * a * t * t);
                    if (mTrackY < 0) {
                        mTrackY = 0;
                        mState = State.READY;
                        mAnimating = false;
                    }
                    else if (mTrackY > mContentHeight) {
                        mTrackY = mContentHeight;
                        mContent.setVisibility(GONE);
                        mState = State.READY;
                        mAnimating = false;
                    }
                    break;
            }

            invalidate();
            if (!mAnimating) {
                postProcess();
                return;
            }
            mCurrentAnimationTime += ANIMATION_FRAME_DURATION;
            mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE), mCurrentAnimationTime);

        }
    }

    private SlidingScrollListener listener;

    public interface SlidingScrollListener {
        void onScroll(int contentWidth, int contentHight, float trackX, float trackY);
    }

    public void setSlidingScrollListener(SlidingScrollListener listener) {
        this.listener = listener;
    }


    private static class WeakHandler extends Handler {
        private WeakReference<View> mView;

        public WeakHandler(View view) {
            this.mView = new WeakReference<>(view);
        }

        @Override public void handleMessage(Message msg) {
            Panel view = (Panel) mView.get();
            if (view != null) {
                switch (msg.what) {
                    case MSG_ANIMATE:
                        view.doAnimation();
                        break;
                    case MSG_PREPARE_ANIMATE:
                        view.prepareAnimation();
                        view.doAnimation();
                        break;
                }
            }
        }
    }

    public void destroy() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }
}
