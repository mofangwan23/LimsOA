//
// ListViewWithoutScroll.java
// feep
//
// Created by lin yiqi on 2012-2-9.
// Copyright 2012 flyrise. All rights reserved.
//
package cn.flyrise.android.library.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;

import java.io.InputStream;

/**
 * @author <a href="mailto:184618345@qq.com">017</a>
 */
public class ListViewWithoutScroll extends LinearLayout {
    private Adapter adapter = null;
    private OnItemClickListener onItemClickListener = null;
    private OnItemLongClickListener onItemLongClickListener;
    private Drawable dividerLine;
    private int dividerLineHeight = 3;
    private boolean isFooterDividersEnabled = true;
    private boolean isHeaderDividersEnabled;
    private DataSetObserver dataSetObserver;

    public ListViewWithoutScroll (Context context) {
        super (context);
        dividerLine = dividerLine ();
    }

    /**
     * 允许通过XML的方式注册控件.
     *
     * @param context      context
     * @param attributeSet attribute set
     */
    public ListViewWithoutScroll (Context context, AttributeSet attributeSet) {
        super (context, attributeSet);
        dividerLine = dividerLine ();
        this.setOrientation (VERTICAL);
    }

    /**
     * 设置数据.
     *
     * @param adapter data adapter
     */
    public void setAdapter (Adapter adapter) {
        if (null != this.adapter) {
            this.adapter.unregisterDataSetObserver (dataSetObserver);
        }
        this.adapter = adapter;
        if (this.adapter != null) {
            dataSetObserver = new AdapterDataSetObserver ();
            this.adapter.registerDataSetObserver (dataSetObserver);
        }
        bindLinearLayout ();
    }

    /**
     * 绑定布局
     */
    private void bindLinearLayout () {
        final int count = adapter.getCount ();
        this.removeAllViews ();
        for (int i = 0; i < count; i++) {
            final View v = adapter.getView (i, null, this);
            final int j = i;
            // 为每个view绑定一个数据，可以是任何对象
            if (v == null) {
                continue;
            }
            v.setTag (i);
            v.setOnClickListener (v1 -> ListViewWithoutScroll.this.onItemClick (null, v1, j, adapter.getItemId (j)));
            v.setOnTouchListener ((v12, event) -> false);
            v.setOnLongClickListener (v13 -> {
                if (onItemLongClickListener != null) {
                    onItemLongClickListener.onItemLongClick (null, v13, j, adapter.getItemId (j));
                    return true;
                }
                return false;
            });
            if (i == 0 && isHeaderDividersEnabled) {
                final View divider = new View (getContext ());
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    divider.setBackground (dividerLine);
                }
                else {
                    divider.setBackgroundDrawable(dividerLine);
                }
                addView (divider, new LayoutParams (ViewGroup.LayoutParams.MATCH_PARENT, dividerLineHeight));
            }
            addView (v);
            if (i != count - 1 || isFooterDividersEnabled) {
                final View divider = new View (getContext ());
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    divider.setBackground (dividerLine);
                }
                else {
                    divider.setBackgroundDrawable(dividerLine);
                }
                addView (divider, new LayoutParams (ViewGroup.LayoutParams.MATCH_PARENT, dividerLineHeight));
            }
        }
    }

    /**
     * 分割线
     */
    private Drawable dividerLine () {
        final Bitmap bitmap = Bitmap.createBitmap (1, 4, Config.ARGB_4444);
        final Canvas cv = new Canvas (bitmap);
        cv.drawColor (Color.WHITE);
        final Paint p1 = new Paint ();
        p1.setStyle (Paint.Style.STROKE);// 设置画笔类型
        // p1.setStrokeCap(Paint.Cap.ROUND);// 设置端点处为圆点
        p1.setStrokeWidth (2);// 设置画笔粗细

        p1.setColor (0xffc5c4c1);
        cv.drawPoint (1, 1, p1);
        final Drawable drawable = new BitmapDrawable (getContext().getResources(),bitmap);
        return drawable;
    }

    /**
     * 设置分割线
     */
    public void setDivider (int resID) {
        final InputStream is = getContext ().getResources ().openRawResource (resID);
        dividerLine = Drawable.createFromStream (is, null);
    }

    /**
     * 设置分割线的高度
     */
    public void setDividerHeight (int dividerLineHeight) {
        this.dividerLineHeight = dividerLineHeight;
    }

    /**
     * 设置分割线
     */
    public void setDivider (Drawable divider) {
        dividerLine = divider;
    }

    /**
     * 启用或禁用页脚分割线(默认是启用-true)
     */
    public void setFooterDividersEnabled (boolean footerDividersEnabled) {
        this.isFooterDividersEnabled = footerDividersEnabled;
    }

    /**
     * 启用或禁用页头分割线。
     */
    public void setHeaderDividersEnabled (boolean headerDividersEnabled) {
        this.isHeaderDividersEnabled = headerDividersEnabled;
    }

    public void setOnItemClickListener (OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * 设置长按事件
     */
    public void setOnItemLongClickListener (OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    /**
     * @param arg0 null...
     * @param arg1 这个view,要获得Adapter要墙转换
     * @param arg2 commonGroup
     * @param arg3 这个是你设的哦
     */
    private void onItemClick (AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick (arg0, arg1, arg2, arg3);
        }
    }

    public Adapter getAdapter () {
        return adapter;
    }

    class AdapterDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged () {
            bindLinearLayout ();
        }

        @Override
        public void onInvalidated () {
            bindLinearLayout ();
        }

        public void clearSavedState () {
        }
    }
}
