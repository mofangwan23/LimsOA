/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hyphenate.chatui.group.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.core.base.views.adapter.FEListAdapter;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chatui.R;
import com.hyphenate.chatui.utils.image.ImageSynthesisFatcory;
import java.util.List;

public class GroupListAdapter extends FEListAdapter<EMGroup> {

	private Context mContext;

	public GroupListAdapter(Context context) {
		this.mContext = context;
		dataList = EMClient.getInstance().groupManager().getAllGroups();
	}

	@Override
	public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		ItemViewHolder viewHolder = (ItemViewHolder) holder;
		EMGroup group = dataList.get(position);
		if (!TextUtils.equals(group.getGroupId(), (String) viewHolder.mAvatar.getTag(R.id.avatar))) {
			new ImageSynthesisFatcory.Builder(mContext).setGroupId(group.getGroupId()).setImageView(viewHolder.mAvatar).builder();
			viewHolder.mAvatar.setTag(R.id.avatar, group.getGroupId());
		}
		viewHolder.mName.setText(group.getGroupName());
		viewHolder.itemView.setOnClickListener(v -> {
			if (onItemClickListener != null)
				onItemClickListener.onItemClick(viewHolder.itemView, group);
		});
	}

	@Override public long getItemId(int position) {
		return position;
	}

	@Override
	public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
		View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.em_row_group, parent, false);
		return new ItemViewHolder(convertView);
	}

	class ItemViewHolder extends RecyclerView.ViewHolder {

		View mView;
		ImageView mAvatar;
		TextView mName;

		public ItemViewHolder(View itemView) {
			super(itemView);
			this.mView = itemView;
			this.mAvatar = itemView.findViewById(R.id.avatar);
			this.mName = itemView.findViewById(R.id.name);
		}
	}

	@Override
	public void setDataList(List<EMGroup> showList) {
		super.setDataList(showList);
	}
}
