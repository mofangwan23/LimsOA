package com.hyphenate.chatui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chatui.R;
import com.hyphenate.easeui.utils.EaseSmileUtils;
import com.hyphenate.easeui.utils.EaseUserUtils;

import cn.flyrise.feep.core.base.views.adapter.FEListAdapter;
import cn.flyrise.feep.core.common.utils.DateUtil;

/**
 * Created by klc on 2017/3/17.
 */

public class ChatRecordSearchAdapter extends FEListAdapter<EMMessage> {

    private Context mContext;

    public ChatRecordSearchAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
        EMMessage message = dataList.get(position);
        EaseUserUtils.setUserNick(message.getFrom(), itemViewHolder.name);
        EaseUserUtils.setUserAvatar(mContext, message.getFrom(), itemViewHolder.avatar);
        itemViewHolder.time.setText(DateUtil.formatTimeForDetail(message.getMsgTime()));
        Spannable content = EaseSmileUtils.getSmallSmiledText(mContext,((EMTextMessageBody) message.getBody()).getMessage());
        itemViewHolder.message.setText(content);
        itemViewHolder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null)
                onItemClickListener.onItemClick(itemViewHolder.itemView, message);
        });
    }

    @Override
    public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.em_row_search_message, parent, false);
        return new ItemViewHolder(convertView);
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView message;
        TextView time;
        ImageView avatar;

        public ItemViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            message = (TextView) itemView.findViewById(R.id.message);
            time = (TextView) itemView.findViewById(R.id.time);
            avatar = (ImageView) itemView.findViewById(R.id.avatar);
        }
    }
}
