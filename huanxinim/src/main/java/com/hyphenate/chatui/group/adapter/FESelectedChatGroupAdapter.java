package com.hyphenate.chatui.group.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hyphenate.chat.EMGroup;
import com.hyphenate.chatui.R;

import java.util.List;

import cn.flyrise.feep.core.common.utils.CommonUtil;

/**
 * 新建：陈冕;
 * 日期： 2018-3-9-13:59.
 */

public class FESelectedChatGroupAdapter extends RecyclerView.Adapter<FESelectedChatGroupAdapter.ViewHodler> {

    private List<EMGroup> emGroups;
    private OnSelectedItemListener mListener;

    public FESelectedChatGroupAdapter(List<EMGroup> emGroups, OnSelectedItemListener mListener) {
        this.emGroups = emGroups;
        this.mListener = mListener;
    }

    @Override
    public ViewHodler onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.em_selected_chat_group_item_dialog,parent,false);
        return new ViewHodler(view);
    }

    @Override
    public void onBindViewHolder(ViewHodler holder, int position) {
        EMGroup emGroup = emGroups.get(position);
        if (emGroup == null) {
            return;
        }
        holder.groupName.setText(emGroup.getGroupName());
        holder.groupName.setOnClickListener(v -> {
            if (mListener == null) {
                return;
            }
            mListener.selectedItem(emGroup.getGroupId());
        });
    }

    @Override
    public int getItemCount() {
        return CommonUtil.isEmptyList(emGroups) ? 0 : emGroups.size();
    }

    class ViewHodler extends RecyclerView.ViewHolder {
        private TextView groupName;

        ViewHodler(View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.group_name);
        }
    }

    public interface OnSelectedItemListener {
        void selectedItem(String groudId);
    }

}
