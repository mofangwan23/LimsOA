package cn.flyrise.feep.core.base.views;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import cn.flyrise.feep.core.R;
import cn.flyrise.feep.core.base.views.adapter.BaseRecyclerAdapter;


/**
 * RecyclerView跟SwipeRefreshLayout封装
 * Created by klc on 2016/8/23.
 */
public class PullAndLoadMoreRecyclerView extends LinearLayout {

    private Context context;
    private LoadMoreRecyclerView loadMoreRecyclerView;
    private SwipeRefreshLayout refreshLayout;
    private LoadMoreListener loadMoreListener;
    private RefreshListener refreshListener;
    private BaseRecyclerAdapter adapter;

    public PullAndLoadMoreRecyclerView(Context context) {
        super(context);
        initLayout(context);
        setListener();
        this.context = context;
    }

    public PullAndLoadMoreRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout(context);
        setListener();
        this.context = context;
    }

    private void initLayout(Context mContext) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.core_pull_refresh_recyclerview, this);
        loadMoreRecyclerView = (LoadMoreRecyclerView) findViewById(R.id.recyclerView);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        loadMoreRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        loadMoreRecyclerView.setItemAnimator(new DefaultItemAnimator());
        refreshLayout.setColorSchemeResources(R.color.core_default_accent_color);
    }

    public void setGridLayout(int spanCount) {
        loadMoreRecyclerView.setLayoutManager(new GridLayoutManager(context, spanCount));
    }

    private void setListener() {
        loadMoreRecyclerView.setOnLoadMoreListener(() -> {
            if (loadMoreListener != null && this.adapter.hasFooterView()) {
                loadMoreListener.loadMore();
            }
        });

        refreshLayout.setOnRefreshListener(() -> {
            if (refreshListener != null)
                refreshListener.refresh();
        });

    }

    public void setAdapter(BaseRecyclerAdapter adapter) {
        this.adapter = adapter;
        loadMoreRecyclerView.setAdapter(adapter);
    }

    public void scrollLastItem2Bottom() {
        loadMoreRecyclerView.scrollLastItem2Bottom(adapter);
    }

    public void scroll2Top() {
        loadMoreRecyclerView.smoothScrollToPosition(0);
    }

    public void addFootView() {
        this.adapter.setFooterView(R.layout.core_refresh_bottom_loading);
    }

    public void removeFootView() {
        this.adapter.removeFooterView();
    }

    public void setLoadMoreListener(LoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }

    public void setRefreshListener(RefreshListener refreshListener) {
        this.refreshListener = refreshListener;
    }

    public boolean isLoading() {
        return refreshLayout.isRefreshing();
    }

    public interface LoadMoreListener {
        void loadMore();
    }

    public interface RefreshListener {
        void refresh();
    }

    public void setRefreshing(boolean refreshing) {
        this.refreshLayout.setRefreshing(refreshing);
    }

    public void setCanRefresh(boolean canRefresh) {
        if (canRefresh)
            this.refreshLayout.setEnabled(true);
        else
            this.refreshLayout.setEnabled(false);
    }

    public void setColorSchemeResources(int resources) {
        this.refreshLayout.setColorSchemeResources(resources);
    }

    @Override
    public void setVisibility(int visibility) {
        this.loadMoreRecyclerView.setVisibility(visibility);
    }

    public LoadMoreRecyclerView getLoadMoreRecyclerView() {
        return loadMoreRecyclerView;
    }

    public SwipeRefreshLayout getRefreshLayout() {
        return refreshLayout;
    }

    //监听滚动中事件
    public void setOnScrollStateTouchListener(LoadMoreRecyclerView.OnScrollStateTouchScroll scrollStateTouchScroll) {
        loadMoreRecyclerView.setOnScrollStateTouchListener(scrollStateTouchScroll);
    }
}
