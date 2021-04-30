/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-12-31 上午10:50:10
 */

package cn.flyrise.feep.report;

import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import cn.flyrise.feep.core.base.views.adapter.FEListAdapter;
import cn.flyrise.android.protocol.model.ReportListItem;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.PixelUtil;

/**
 * 类功能描述：</br>
 * @author 钟永健
 * @version 1.0</br> 修改时间：2012-12-31</br> 修改备注：</br>
 * @author klc
 * @version 2.0</br> 修改时间：2017-1-17</br> 修改备注：</br>
 */
class ReportListAdapter extends FEListAdapter<ReportListItem> {

    @Override
    public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemViewHolder viewHolder = (ItemViewHolder) holder;
        ReportListItem item = dataList.get(position);
        viewHolder.view.setMinimumHeight(PixelUtil.dipToPx(60));
        viewHolder.tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        viewHolder.tvName.setPadding(PixelUtil.dipToPx(10), 0, 0, 0);
        viewHolder.ivHead.setVisibility(View.VISIBLE);
        viewHolder.ivArrow.setVisibility(View.VISIBLE);
        viewHolder.view.setBackgroundResource(R.drawable.listview_item_bg);
        viewHolder.tvName.setText(item.getReportName());
        viewHolder.ivHead.setVisibility(View.GONE);
        viewHolder.view.setOnClickListener(v -> {
            if (onItemClickListener != null)
                onItemClickListener.onItemClick(viewHolder.view, item);
        });
    }

    @Override
    public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.form_list_item, null);
        return new ItemViewHolder(view);
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        View view;
        ImageView ivHead;
        TextView tvName;
        ImageView ivArrow;

        public ItemViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            this.ivHead = (ImageView) itemView.findViewById(R.id.form_list_item_headimage);
            this.tvName = (TextView) itemView.findViewById(R.id.form_list_item_name);
            this.ivArrow = (ImageView) itemView.findViewById(R.id.form_list_item_arrow);
        }

    }
}
