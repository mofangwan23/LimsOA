package cn.flyrise.feep.location.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.flyrise.feep.R;
import cn.flyrise.feep.location.bean.LocationSaveItem;

import static cn.flyrise.feep.core.common.utils.CommonUtil.isEmptyList;

public class LocationSaveAdapter extends RecyclerView.Adapter<LocationSaveAdapter.ViewHolder> {

    private List<LocationSaveItem> mItems;

    private Context mContext;

    private OnItemClickListener mItemCLickListener;

    private OnDeletedItemListener mDeletedItemListener;

    private int current = -1;

    public LocationSaveAdapter(Context context) {
        mContext = context;
    }

    public void setLocationSave(List<LocationSaveItem> items) {
        this.mItems = items;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemCLickListener = listener;
    }

    @SuppressLint("InflateParams")
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.location_save_item, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        LocationSaveItem item = mItems.get(position);
        holder.subject.setText(item.title);
        if (current == position) {
            holder.deletedIcon.setVisibility(View.VISIBLE);
        } else {
            holder.deletedIcon.setVisibility(View.GONE);
        }
        holder.frontView.setOnClickListener(v -> {
            current = -1;
            if (mItemCLickListener != null) {
                mItemCLickListener.onItem(item);
            }
            notifyDataSetChanged();
        });

        holder.frontView.setOnLongClickListener(v -> {//长按显示删除
            current = position;
            notifyDataSetChanged();
            return true;
        });

        holder.deletedIcon.setOnClickListener(v -> {
            if (mItems == null) {
                return;
            }
            if (mItems.contains(item)) {
                if (mItems.remove(item) && mDeletedItemListener != null) {
                    mDeletedItemListener.deleteItem(item);
                    current = -1;
                    notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return isEmptyList(mItems) ? 0 : mItems.size();
    }

    public interface OnItemClickListener {
        void onItem(LocationSaveItem poiItem);
    }

    public interface OnDeletedItemListener {
        void deleteItem(LocationSaveItem item);
    }

    public void setOnDeletedItemListener(OnDeletedItemListener deletedItem) {
        mDeletedItemListener = deletedItem;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView subject;    // 公司名称
        View frontView;
        ImageView deletedIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            subject = itemView.findViewById(R.id.myItemView_subject);
            deletedIcon = itemView.findViewById(R.id.delete_icon);
            frontView = itemView.findViewById(R.id.id_front);
        }
    }
}
