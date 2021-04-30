/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-4-2
 */
package cn.flyrise.android.library.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

import java.util.ArrayList;

/**
 * <b>类功能描述: 一个边界可移动的布局容器,以后可能可以加入缩放~呵呵 </div>
 * 呵呵你 MB~
 * @author <a href="mailto:184618345@qq.com">017</a>
 * @version 1.0 </p> 修改时间：</br> 修改备注：</br>
 */
public class TransformLayout extends FrameLayout {
    public static final int LEFT = 1;
    public static final int TOP = 2;
    public static final int RIGHT = 3;
    public static final int BOTTOM = 4;
    private int borderLeft, borderTop, borderRight, borderBottom;
    private final ArrayList<ViewInfos> viewInfosList = new ArrayList<> ();

    public TransformLayout (Context context, AttributeSet attrs) {
        super (context, attrs);
    }

    public TransformLayout (Context context) {
        super (context);
    }

    @Override
    protected void onLayout (boolean changed, int l, int t, int r, int b) {
        super.onLayout (changed, l, t, r, b);
        reConfirmChileView ();
        transforma ();
    }

    private void transforma () {
        final int count = this.getChildCount ();
        for (int i = 0; i < count; i++) {
            final View child = this.getChildAt (i);
            final ViewInfos info = viewInfosList.get (i);
            // 以下用来协商Layout与View大小和位置的,也许可以用来解决ScrollView套ListView,WebView等问题
            child.measure (MeasureSpec.makeMeasureSpec (info.measuredWidth - borderLeft - borderRight, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec (info.measuredHeight - borderTop - borderBottom, MeasureSpec.EXACTLY));
            child.layout (info.left + borderLeft, info.top + borderTop, info.right - borderRight, info.botom - borderBottom);
        }
        if (listener != null) {
            listener.onTransform (borderLeft, borderTop, borderRight, borderBottom);
        }
        invalidate ();
    }

    private class ViewInfos {
        int left;
        int top;
        int right;
        int botom;
        int measuredWidth;
        int measuredHeight;

        public ViewInfos (int left, int top, int right, int botom, int measuredWidth, int measuredHeight) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.botom = botom;
            this.measuredWidth = measuredWidth;
            this.measuredHeight = measuredHeight;
        }
    }

    public void reConfirmChileView () {
        if (getChildCount () != viewInfosList.size ()) {
            viewInfosList.clear ();
            final int count = this.getChildCount ();
            for (int i = 0; i < count; i++) {
                final View child = this.getChildAt (i);
                viewInfosList.add (new ViewInfos (child.getLeft (), child.getTop (), child.getRight (), child.getBottom (), child.getMeasuredWidth (), child.getMeasuredHeight ()));
            }
        }
    }

    /**
     * 设置内容边距
     */
    public void setBorder (int left, int top, int right, int botom) {
        borderLeft = left;
        borderTop = top;
        borderRight = right;
        borderBottom = botom;
        transforma ();
    }

    /**
     * 交由动画设置边距
     */
    public void transformBorder (TranslateAnimation ta, int which) {
    }

    private TransformListener listener;

    public interface TransformListener {
        void onTransform(int left, int top, int right, int botom);
    }

    public void setTransformListener (TransformListener listener) {
        this.listener = listener;
    }
}
