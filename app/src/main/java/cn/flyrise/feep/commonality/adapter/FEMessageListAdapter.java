/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-3-16 上午11:32:09
 */
package cn.flyrise.feep.commonality.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.flyrise.feep.core.base.views.adapter.FEListAdapter;
import cn.flyrise.android.library.utility.SubTextUtility;
import cn.flyrise.feep.core.common.X.RequestType;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.bean.FEListItem;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.core.common.utils.PixelUtil;
import cn.flyrise.feep.utils.RandomSources;

/**
 * 类功能描述：</br>
 *
 * @author cm
 * @version 1.0</br> 修改时间：2015-11-11</br> 修改备注：</br>
 */
public class FEMessageListAdapter extends FEListAdapter<FEListItem>{

    public boolean removeMessage(String messageId) {
        if (CommonUtil.isEmptyList(dataList)) return false;
        FEListItem item = null;
        int position = -1;
        for (int i = 0; i < dataList.size(); i++) {
            item = dataList.get(i);
            if (item != null && TextUtils.equals(messageId, item.getId())) {
                position = i;
                break;
            }
        }
        if (position < 0) return false;
        if (dataList.remove(item)) {
            notifyItemRemoved(position);
            return true;
        }
        return false;
    }

    @Override
    public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemViewHolder viewHolder = (ItemViewHolder) holder;
        viewHolder.view.setBackgroundResource(R.drawable.listview_item_bg);
        FEListItem listItem = dataList.get(position);
        String titles;
        if (SubTextUtility.isTextBook(listItem.getTitle())) {
            titles = SubTextUtility.subTextString(listItem.getTitle());
        }
        else {
            titles = listItem.getTitle();
        }
        if (!TextUtils.isEmpty(titles)) {
            viewHolder.mTitle.setText(titles);
        }
        if (!TextUtils.isEmpty(listItem.getCategory())) {
            viewHolder.icon_name.setVisibility(View.VISIBLE);
            viewHolder.icon_name.setText(listItem.getCategory());
            viewHolder.icon_name.setBackgroundResource(RandomSources.getSourceById(listItem.getCategory()));
        }
        else {
            viewHolder.icon_name.setVisibility(View.GONE);
        }
        if (listItem.getRequestType() == RequestType.System) {
            viewHolder.mTitle.setTextColor(0xff999999);
            viewHolder.view.setBackgroundResource(R.drawable.listview_item_disenable_bg);
        }
        else {
            viewHolder.mTitle.setTextColor(0xff333333);
        }
        if (!TextUtils.isEmpty(listItem.getSendUser())) {
            viewHolder.mName.setText(listItem.getSendUser());
        }
        else {
            viewHolder.mName.setText(listItem.getMsgType());
        }
        if (!TextUtils.isEmpty(listItem.getContent())) {
            viewHolder.mContent.setVisibility(View.VISIBLE);
            viewHolder.mContent.setText(listItem.getContent());
        }
        else {
            viewHolder.mContent.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(listItem.getBadge())) {
            viewHolder.mNum.setText(listItem.getBadge());
        }
        else {
            viewHolder.mTimeLayout.setGravity(Gravity.RIGHT);
            viewHolder.mTimeLayout.setPadding(0, 0, PixelUtil.dipToPx(16), 0);
            viewHolder.mNumLayout.setVisibility(View.GONE);
        }
        String sendDate = DateUtil.strToString(listItem.getSendTime());
        viewHolder.mTime.setText(sendDate);
        viewHolder.view.setOnClickListener(v -> {
            if (onItemClickListener != null)
                onItemClickListener.onItemClick(viewHolder.view, listItem);
        });
    }

    @Override
    public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ItemViewHolder(inflater.inflate(R.layout.fe_list_item, null));
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView icon_name;
        TextView mTitle;
        TextView mContent;
        TextView mName;
        TextView mTime;
        TextView mNum;
        LinearLayout mNumLayout;
        LinearLayout mTimeLayout;

        public ItemViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            icon_name = (TextView) itemView.findViewById(R.id.category_name);
            mTitle = (TextView) itemView.findViewById(R.id.fe_list_item_title);
            mName = (TextView) itemView.findViewById(R.id.fe_list_item_name);
            mTime = (TextView) itemView.findViewById(R.id.fe_list_item_time);
            mContent = (TextView) itemView.findViewById(R.id.fe_list_item_content);
            mNum = (TextView) itemView.findViewById(R.id.fe_list_item_nums);
            mNumLayout = (LinearLayout) itemView.findViewById(R.id.fe_list_item_nums_layout);
            mTimeLayout = (LinearLayout) itemView.findViewById(R.id.bottom_content_layout);
        }

    }
}
