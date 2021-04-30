package com.handmark.pulltorefresh.library;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import java.util.HashSet;

public class LoadingLayoutProxy implements ILoadingLayout {

    private final HashSet<ILoadingLayout> mLoadingLayouts;

    LoadingLayoutProxy() {
        mLoadingLayouts = new HashSet<>();
    }

    /**
     * This allows you to add extra LoadingLayout instances to this proxy. This
     * is only necessary if you keep your own instances, and want to have them
     * included in any
     * {@link PullToRefreshBase#createLoadingLayoutProxy(boolean, boolean)
     * createLoadingLayoutProxy(...)} calls.
     *
     * @param layout - LoadingLayout to have included.
     */
    public void addLayout(ILoadingLayout layout) {
        if (null != layout) {
            mLoadingLayouts.add(layout);
        }
    }

    public void removeAllLayout() {
        if (mLoadingLayouts != null && mLoadingLayouts.size() > 0) {
            mLoadingLayouts.clear();
        }
    }

    @Override
    public void setLastUpdatedLabel(CharSequence label) {
        for (ILoadingLayout layout : mLoadingLayouts) {
            layout.setLastUpdatedLabel(label);
        }
    }

    @Override
    public void setLoadingDrawable(Drawable drawable) {
        for (ILoadingLayout layout : mLoadingLayouts) {
            layout.setLoadingDrawable(drawable);
        }
    }

    @Override
    public void setRefreshingLabel(CharSequence refreshingLabel) {
        for (ILoadingLayout layout : mLoadingLayouts) {
            layout.setRefreshingLabel(refreshingLabel);
        }
    }

    @Override
    public void setPullLabel(CharSequence label) {
        for (ILoadingLayout layout : mLoadingLayouts) {
            layout.setPullLabel(label);
        }
    }

    @Override
    public void setReleaseLabel(CharSequence label) {
        for (ILoadingLayout layout : mLoadingLayouts) {
            layout.setReleaseLabel(label);
        }
    }

    public void setTextTypeface(Typeface tf) {
        for (ILoadingLayout layout : mLoadingLayouts) {
            layout.setTextTypeface(tf);
        }
    }
}
