package com.hyphenate.chatui.retrieval;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.core.base.views.adapter.FEListAdapter;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import com.hyphenate.chatui.R;

/**
 * @author ZYP
 * @since 2018-05-08 18:14
 */
public class GroupSearchAdapter extends FEListAdapter<GroupInfo> {

	private Context mContext;

	public GroupSearchAdapter(Context context) {
		this.mContext = context;
	}

	@Override public void onChildBindViewHolder(ViewHolder holder, int position) {
		ItemViewHolder vHolder = (ItemViewHolder) holder;
		GroupInfo groupInfo = dataList.get(position);

		vHolder.tvConversationName.setText(groupInfo.conversationName);
		if (TextUtils.isEmpty(groupInfo.content)) {
			vHolder.tvMessage.setVisibility(View.GONE);
		}
		else {
			vHolder.tvMessage.setVisibility(View.VISIBLE);
			vHolder.tvMessage.setText(groupInfo.content);
		}
		FEImageLoader.load(mContext, vHolder.ivAvatar, groupInfo.imageRes);
		vHolder.itemView.setOnClickListener(v -> {
			if (onItemClickListener != null)
				onItemClickListener.onItemClick(vHolder.itemView, groupInfo);
		});
	}

	@Override public ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
		View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.em_row_group_search, parent, false);
		return new ItemViewHolder(convertView);
	}

	private class ItemViewHolder extends RecyclerView.ViewHolder {

		ImageView ivAvatar;
		TextView tvMessage;
		TextView tvConversationName;

		public ItemViewHolder(View itemView) {
			super(itemView);
			tvConversationName = itemView.findViewById(R.id.name);
			tvMessage = itemView.findViewById(R.id.message);
			ivAvatar = itemView.findViewById(R.id.avatar);
		}
	}
}
