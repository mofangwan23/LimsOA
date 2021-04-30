package cn.flyrise.feep.email.views;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

/**
 * @author ZYP
 * @since 2016/7/28 08:47
 */
public class TitleBarWebView extends WebView {

    private GestureDetectorCompat mGestureDetector;
    private boolean isDoubleTap;
    private int mTitleViewHeight;
    private int mNewTitleHeight;

    public TitleBarWebView(Context context) {
        this(context, null);
    }

    public TitleBarWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TitleBarWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mGestureDetector = new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onDoubleTap(MotionEvent e) {
                isDoubleTap = true;
                return false;
            }
        });
    }


    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        View title = getChildAt(0);
        mTitleViewHeight = title == null ? 0 : title.getMeasuredHeight();
    }

    /******************************************************************************/

    @Override public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        if (isDoubleTap) {
            isDoubleTap = false;
            return false;
        }

        if (MotionEventCompat.findPointerIndex(event, 0) == -1) {
            return super.onTouchEvent(event);
        }

        if (event.getPointerCount() >= 2) {
            requestDisallowInterceptTouchEvent(true);
        }
        else {
            requestDisallowInterceptTouchEvent(false);
        }

        return super.onTouchEvent(event);
    }

    @Override protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        requestDisallowInterceptTouchEvent(true);
    }

    /******************************************************************************/


//    @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
//        return super.onInterceptTouchEvent(ev);
//    }

//    private boolean touchInTitleBar;
//
//    @Override public boolean dispatchTouchEvent(MotionEvent me) {
//        boolean wasInTitle = false;
//        switch (me.getActionMasked()) {
//            case MotionEvent.ACTION_DOWN:
//                touchInTitleBar = (me.getY() <= visibleTitleHeight());
//                break;
//
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_CANCEL:
//                wasInTitle = touchInTitleBar;
//                touchInTitleBar = false;
//                break;
//        }
//        if (touchInTitleBar || wasInTitle) {
//            IView title = getChildAt(0);
//            if (title != null) {
//                me.offsetLocation(0, getScrollY());
//                return title.dispatchTouchEvent(me);
//            }
//        }
//
//        me.offsetLocation(0, -mTitleViewHeight);
//        return super.dispatchTouchEvent(me);
//    }
//
//    private int visibleTitleHeight() {
//        return mTitleViewHeight - getScrollY();
//    }
//
//    @Override protected void onScrollChanged(int l, int t, int oldl, int oldt) {
//        super.onScrollChanged(l, t, oldl, oldt);
//        mNewTitleHeight = t;
//        IView title = getChildAt(0);
//        if (title != null) {
//            title.offsetLeftAndRight(l - title.getLeft());
//            Log.i("diff = " + (mTitleViewHeight - mNewTitleHeight));
//        }
//    }
//
//    @Override protected void onDraw(Canvas c) {
//        c.save();
//
//        int tH = visibleTitleHeight();
//        if (tH > 0) {
//            int scrollX = getScrollX();
//            int scrollY = getScrollY();
//            c.clipRect(scrollX, scrollY + tH, scrollX + getWidth(), scrollY + getHeight());
//        }
//
//
//        if (mNewTitleHeight == 0) {
//            c.translate(0, mTitleViewHeight);
//        }
//        else if (mNewTitleHeight > mTitleViewHeight) {
//            c.translate(0, 0);
//        }
//        else {
//            c.translate(0, mTitleViewHeight - mNewTitleHeight);
//        }
//
//        super.onDraw(c);
//        c.restore();
//    }
}
