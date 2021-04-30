/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-4-19 下午2:09:11
 */

package cn.flyrise.android.library.view.pulltorefreshlistview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.internal.LoadingLayout;

import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.PixelUtil;

/**
 * 类功能描述：</br>
 *
 * @author 钟永健
 * @version 1.0</br> 修改时间：2013-4-19</br> 修改备注：</br>
 */
public class FEPullToRefreshListView extends PullToRefreshListView {

    private AdapterView.OnItemClickListener adapterViewItemClickListener;
    private OnItemClickListener myItemClickListener;
    private OnItemLongClickListener myItemLongClickListener;

    private ListAdapter adapter;

    public FEPullToRefreshListView(Context context) {
        super(context);
        setMode(Mode.PULL_FROM_END);
        init();
    }

    public FEPullToRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setMode(Mode.PULL_FROM_END);
        init();
    }

    public FEPullToRefreshListView(Context context, Mode mode, AnimationStyle style) {
        super(context, mode, style);
        init();
    }

    private void init() {
        setCacheColorHint(0);
        setShowIndicator(false);// 默认不显示拖动提示
        getRefreshableView().setBackgroundColor(getResources().getColor(R.color.all_background_color));
        getRefreshableView().setDivider(getResources().getDrawable(R.drawable.spacer_medium));
        setOverScrollMode(ListView.OVER_SCROLL_NEVER);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setScrollBarSize(PixelUtil.dipToPx(2));
        }
    }

    /**
     * 不显示分割线
     */
    public void setDiverHide() {
        getRefreshableView().setDividerHeight(0);
        getRefreshableView().setDivider(null);
    }

    /**
     * 判断是否在加载中
     *
     * @return
     */
    public boolean isLoading() {
        return getState() == State.REFRESHING;
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        this.adapter = adapter;
        super.setAdapter(adapter);
    }

    public ListAdapter getAdapter() {
        return adapter;
    }

    /*************************** 设置item长按事件 ***********************************/
    /**
     * 设置item长按事件
     */
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.myItemLongClickListener = listener;
        getRefreshableView().setOnItemLongClickListener(systemItemLongClickListener);
    }

    /**
     * 系统的item长按事件
     */
    private final AdapterView.OnItemLongClickListener systemItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            return myItemLongClickListener != null && myItemLongClickListener.onItemLongClick(FEPullToRefreshListView.this, view, position - 1, id);
        }
    };

    /**
     * item点击事件
     */
    public interface OnItemLongClickListener {
        boolean onItemLongClick(FEPullToRefreshListView parent, View view, int position, long id);
    }

    /*****************************************************************************/

    /*************************** 设置item点击事件 ***********************************/
    /**
     * 设置item的点击事件
     */
    @Override
    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        adapterViewItemClickListener = listener;
        super.setOnItemClickListener(systemItemClickListener);
    }

    /**
     * 设置item的点击事件
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        myItemClickListener = listener;
        super.setOnItemClickListener(systemItemClickListener);
    }

    /**
     * 系统的item点击事件
     */
    private final AdapterView.OnItemClickListener systemItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (adapterViewItemClickListener != null) {
                adapterViewItemClickListener.onItemClick(parent, view, position - 1, id);
            }
            if (myItemClickListener != null) {
                myItemClickListener.onItemClick(FEPullToRefreshListView.this, view, position - 1, id);
            }
        }
    };

    /**
     * item点击事件
     */
    public interface OnItemClickListener {
        void onItemClick(FEPullToRefreshListView parent, View view, int position, long id);
    }

    /*****************************************************************************/

    @Override
    public void setOnTouchListener(OnTouchListener listener) {
        getRefreshableView().setOnTouchListener(listener);
    }

    public void setCacheColorHint(int color) {
        getRefreshableView().setCacheColorHint(color);
    }

    public void setScrollingCacheEnabled(boolean isEnabled) {
        getRefreshableView().setScrollingCacheEnabled(isEnabled);
    }

    @Override
    protected LoadingLayout createLoadingLayout(Context context, Mode mode, TypedArray attrs) {
        final LoadingLayout layout = new HandLoadingLayout(context, mode, Orientation.VERTICAL, attrs);
        layout.setVisibility(View.INVISIBLE);
        return layout;
    }
}
