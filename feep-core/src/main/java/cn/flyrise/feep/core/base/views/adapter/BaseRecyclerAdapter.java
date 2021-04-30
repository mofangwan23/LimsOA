package cn.flyrise.feep.core.base.views.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author ZYP
 * @since 2016/7/11 11:46
 */
public abstract class BaseRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_NORMAL = 0;
    protected static final int TYPE_HEADER = 1;
    private static final int TYPE_FOOTER = 2;

    private int mHeaderViewLayoutId = -1;
    private int mFooterViewLayoutId = -1;

    public OnItemClickListener onItemClickListener;
    protected OnItemLongClickListener onItemLongClickListener;
    public OnHeadClickListener onHeadClickListener;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View headerView = LayoutInflater.from(parent.getContext()).inflate(mHeaderViewLayoutId, parent, false);
            return new HeaderViewHolder(headerView);
        }
        else if (viewType == TYPE_FOOTER) {
            View footerView = LayoutInflater.from(parent.getContext()).inflate(mFooterViewLayoutId, parent, false);
            return new FooterViewHolder(footerView);
        }

        return onChildCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            holder.itemView.setOnClickListener(v -> {
                if (onHeadClickListener != null) {
                    onHeadClickListener.onHeadClick(holder.itemView);
                }
            });
            return;
        }

        if (holder instanceof FooterViewHolder) {
            return;
        }

        if (mHeaderViewLayoutId == -1) {
            onChildBindViewHolder(holder, position);
        }
        else {
            onChildBindViewHolder(holder, position - 1);
        }
    }

    @Override
    public int getItemCount() {
        int itemCount = getDataSourceCount();
        if (this.mHeaderViewLayoutId != -1) {
            itemCount++;
        }

        if (this.mFooterViewLayoutId != -1) {
            itemCount++;
        }
        return itemCount;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && mHeaderViewLayoutId != -1) {
            return TYPE_HEADER;
        }

        if (mFooterViewLayoutId != -1) {
            if (mHeaderViewLayoutId != -1 && position == getDataSourceCount() + 1) {
                return TYPE_FOOTER;
            }

            if (mHeaderViewLayoutId == -1 && position == getDataSourceCount()) {
                return TYPE_FOOTER;
            }
        }

        return TYPE_NORMAL;
    }

    public abstract int getDataSourceCount();

    public abstract void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position);

    public abstract RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType);

    public void setHeaderView(int layoutId) {
        mHeaderViewLayoutId = layoutId;
    }

    public void setFooterView(int layoutId) {
        mFooterViewLayoutId = layoutId;
    }

    public void removeHeaderView() {
        if (mHeaderViewLayoutId != -1) {
            mHeaderViewLayoutId = -1;
            notifyDataSetChanged();
        }
    }

    public void removeFooterView() {
        if (mFooterViewLayoutId != -1) {
            mFooterViewLayoutId = -1;
            notifyDataSetChanged();
        }
    }

    public boolean hasHeaderView() {
        return this.mHeaderViewLayoutId != -1;
    }

    public boolean hasFooterView() {
        return this.mFooterViewLayoutId != -1;
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        this.onItemClickListener = clickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public void setOnHeadClickListener(OnHeadClickListener onHeadClickListener) {
        this.onHeadClickListener = onHeadClickListener;
    }

    private class FooterViewHolder extends RecyclerView.ViewHolder {
        FooterViewHolder(View itemView) {
            super(itemView);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, Object object);
    }

    public interface OnHeadClickListener {
        void onHeadClick(View view);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View view, Object object);
    }
}
