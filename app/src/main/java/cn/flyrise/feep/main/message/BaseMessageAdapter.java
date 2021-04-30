package cn.flyrise.feep.main.message;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.flyrise.feep.core.base.views.adapter.BaseMessageRecyclerAdapter;
import java.util.ArrayList;
import java.util.List;

import cn.flyrise.feep.core.base.views.adapter.BaseRecyclerAdapter;
import cn.flyrise.feep.core.common.utils.CommonUtil;

/**
 * @author ZYP
 * @since 2017-03-30 14:48
 */
public abstract class BaseMessageAdapter<T> extends BaseMessageRecyclerAdapter {

    protected View mEmptyView;
    protected List<T> mDataSource;
    protected OnMessageClickListener<T> mMessageClickListener;

    public void setEmptyView(View emptyView) {
        this.mEmptyView = emptyView;
    }

    public void setDataSource(List<T> dataSource) {
        if (CommonUtil.isEmptyList(dataSource)) {
            dataSource = new ArrayList<>();
        }

        if (mEmptyView != null) {
            mEmptyView.setVisibility(CommonUtil.isEmptyList(dataSource) ? View.VISIBLE : View.GONE);
        }

        this.mDataSource = dataSource;
        this.notifyDataSetChanged();
    }

    public void addDataSource(List<T> dataSource) {
        if (CommonUtil.isEmptyList(dataSource)) {
            return;
        }

        if (CommonUtil.isEmptyList(mDataSource)) {
            mDataSource = new ArrayList<>(dataSource.size());
        }

        for (T t : dataSource) {
            if (mDataSource.contains(t)) {
                continue;
            }
            mDataSource.add(t);
        }
        this.notifyDataSetChanged();
    }

    public boolean needAddFooter(int totalSize) {
        return !CommonUtil.isEmptyList(mDataSource) && mDataSource.size() < totalSize;
    }

    public void setOnMessageClickListener(OnMessageClickListener<T> listener) {
        this.mMessageClickListener = listener;
    }

    @Override public int getDataSourceCount() {
        return CommonUtil.isEmptyList(mDataSource) ? 0 : mDataSource.size();
    }

    public interface OnMessageClickListener<T> {
        void onMessageClick(T t, int position);
    }

    @Override
    public int getLoadBackgroundColor() {
        return Color.parseColor("#00000000");
    }

}
