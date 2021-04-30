package com.hyphenate.chatui.retrieval;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.views.adapter.FEListAdapter;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.core.services.model.AddressBook;
import com.hyphenate.chatui.R;
import com.hyphenate.easeui.utils.EaseSmileUtils;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2018-05-04 19:51
 */
public class MoreRecordSearchAdapter extends FEListAdapter<ChatMessage> {

	private Context mContext;

	public MoreRecordSearchAdapter(Context context) {
		this.mContext = context;
	}

	@Override public void onChildBindViewHolder(ViewHolder holder, int position) {
		ItemViewHolder vHolder = (ItemViewHolder) holder;
		ChatMessage message = dataList.get(position);

		vHolder.tvConversationName.setText(message.conversationName);
		vHolder.tvMessage.setText(EaseSmileUtils.getSmallSmiledText(mContext, message.content));
		fillUserAvatar(vHolder.ivAvatar, message);
		vHolder.itemView.setOnClickListener(v -> {
			if (onItemClickListener != null)
				onItemClickListener.onItemClick(vHolder.itemView, message);
		});
	}

	@Override public ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
		View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.em_row_search_message, parent, false);
		return new ItemViewHolder(convertView);
	}

	private void fillUserAvatar(ImageView imageView, ChatMessage chatMessage) {
		if (chatMessage.isGroup) {
			FEImageLoader.load(mContext, imageView, chatMessage.imageRes);
		}
		else {
			CoreZygote.getAddressBookServices().queryUserDetail(chatMessage.conversationId)
					.subscribe(addressBook -> {
						if (addressBook == null) {
							FEImageLoader.load(mContext, imageView, chatMessage.imageRes);
						}
						else {
							FEImageLoader.load(mContext, imageView,
									CoreZygote.getLoginUserServices().getServerAddress() + addressBook.imageHref,
									addressBook.userId, addressBook.name);
						}
					}, error -> {
						FEImageLoader.load(mContext, imageView, chatMessage.imageRes);
					});

		}
	}


	private class ItemViewHolder extends RecyclerView.ViewHolder {

		ImageView ivAvatar;
		TextView tvTime;
		TextView tvMessage;
		TextView tvConversationName;

		public ItemViewHolder(View itemView) {
			super(itemView);
			tvConversationName = itemView.findViewById(R.id.name);
			tvMessage = itemView.findViewById(R.id.message);
			ivAvatar = itemView.findViewById(R.id.avatar);

			tvTime = itemView.findViewById(R.id.time);
			tvTime.setVisibility(View.GONE);
		}
	}
}
