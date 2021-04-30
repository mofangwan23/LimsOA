package com.handmark.pulltorefresh.library.internal;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.handmark.pulltorefresh.library.ILoadingLayout;

/**
 * @author ZYP
 * @since 2017-05-16 11:55
 */
public class SimpleLoadingLayout extends LinearLayout implements ILoadingLayout {

    public SimpleLoadingLayout(Context context) {
        this(context, null);
    }

    public SimpleLoadingLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleLoadingLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, cn.flyrise.feep.core.R.layout.core_refresh_bottom_loading, this);
    }

    @Override public void setLastUpdatedLabel(CharSequence label) {

    }

    @Override public void setLoadingDrawable(Drawable drawable) {

    }

    @Override public void setPullLabel(CharSequence pullLabel) {

    }

    @Override public void setRefreshingLabel(CharSequence refreshingLabel) {

    }

    @Override public void setReleaseLabel(CharSequence releaseLabel) {

    }

    @Override public void setTextTypeface(Typeface tf) {

    }
}
