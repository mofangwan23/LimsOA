package com.hyphenate.chatui.group.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.views.adapter.FEListAdapter;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chatui.R;
import com.hyphenate.chatui.group.model.EmUserItem;
import com.hyphenate.easeui.EaseUiK;
import java.util.ArrayList;
import java.util.List;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by klc on 2017/3/15.
 */

public class GroupUserManageListAdapter extends FEListAdapter<EmUserItem> {

	private Context mContext;
	private List<EmUserItem> mSelectUsers;
	private int mListType;
	private EMGroup mGroup;

	public GroupUserManageListAdapter(Context mContext, String groupId) {
		this.mContext = mContext;
		this.mSelectUsers = new ArrayList<>();
		this.mGroup = EMClient.getInstance().groupManager().getGroup(groupId);
	}

	public void setListType(int listType) {
		this.mListType = listType;
	}

	@Override
	public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		ContactViewHolder viewHolder = (ContactViewHolder) holder;
		EmUserItem user = dataList.get(position);
		CoreZygote.getAddressBookServices().queryUserDetail(user.userId)
				.subscribe(userInfo -> {
					if (userInfo != null) {
						String host = CoreZygote.getLoginUserServices().getServerAddress();
						FEImageLoader.load(mContext, viewHolder.ivUserIcon, host + userInfo.imageHref, user.userId, userInfo.name);
						viewHolder.tvUserName.setText(userInfo.name);
					}
					else {
						FEImageLoader.load(mContext, viewHolder.ivUserIcon, R.drawable.ease_default_avatar);
						viewHolder.tvUserName.setText(user.userId);
					}
				}, error -> {
					FEImageLoader.load(mContext, viewHolder.ivUserIcon, R.drawable.ease_default_avatar);
					viewHolder.tvUserName.setText(user.userId);
				});

		if (mListType == EaseUiK.EmUserList.em_userList_delete_code) {
			viewHolder.checkBox.setVisibility(View.VISIBLE);
			viewHolder.checkBox.setChecked(user.isCheck);
			viewHolder.iv_admin.setVisibility(View.GONE);
			viewHolder.iv_black.setVisibility(View.GONE);
			viewHolder.iv_mute.setVisibility(View.GONE);
		}
		else if (mListType == EaseUiK.EmUserList.em_userList_manage_code) {
			viewHolder.checkBox.setVisibility(View.GONE);
			viewHolder.iv_admin.setVisibility(user.isAdmin ? View.VISIBLE : View.GONE);
			viewHolder.iv_mute.setVisibility(user.isMute ? View.VISIBLE : View.GONE);
			viewHolder.iv_black.setVisibility(user.isBlack ? View.VISIBLE : View.GONE);
		}
		else {
			viewHolder.checkBox.setVisibility(View.GONE);
			viewHolder.iv_admin.setVisibility(View.GONE);
			viewHolder.iv_black.setVisibility(View.GONE);
			viewHolder.iv_mute.setVisibility(View.GONE);
		}
		viewHolder.tv_owner.setVisibility(user.userId.equals(mGroup.getOwner()) ? View.VISIBLE : View.GONE);
		viewHolder.ivUserIcon.setOnClickListener(v -> {
			if (onItemClickListener != null) {
				onItemClickListener.onItemClick(v, user);
			}
		});
		if (mListType != EaseUiK.EmUserList.em_userList_manage_code) {
			viewHolder.view.setOnClickListener(v -> {
				if (mListType == EaseUiK.EmUserList.em_userList_delete_code) {
					user.isCheck = !user.isCheck;
					if (user.isCheck) {
						mSelectUsers.add(user);
					}
					else {
						mSelectUsers.remove(user);
					}
					viewHolder.checkBox.setChecked(user.isCheck);
				}
				if (onItemClickListener != null) {
					onItemClickListener.onItemClick(viewHolder.view, user);
				}
			});
		}
		else {
			viewHolder.view.setOnLongClickListener(v -> {
				if (onItemLongClickListener != null) {
					onItemLongClickListener.onItemLongClick(viewHolder.view, user);
				}
				return true;
			});
		}
		if (mGroup.getOwner().equals(user.userId)) {
			viewHolder.checkBox.setVisibility(View.GONE);
			viewHolder.view.setOnClickListener(null);
		}
	}

	@Override
	public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
		View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.em_item_emuser, parent, false);
		return new ContactViewHolder(convertView);
	}

	private class ContactViewHolder extends RecyclerView.ViewHolder {

		View view;
		ImageView ivUserIcon;
		TextView tvUserName;
		CheckBox checkBox;
		TextView tv_owner;
		ImageView iv_black;
		ImageView iv_mute;
		ImageView iv_admin;

		ContactViewHolder(View itemView) {
			super(itemView);
			view = itemView;
			ivUserIcon = (ImageView) itemView.findViewById(R.id.ivUserIcon);
			tvUserName = (TextView) itemView.findViewById(R.id.tvUserName);
			checkBox = (CheckBox) itemView.findViewById(R.id.ivContactCheck);
			tv_owner = (TextView) itemView.findViewById(R.id.tv_owner);
			iv_black = (ImageView) itemView.findViewById(R.id.iv_black);
			iv_mute = (ImageView) itemView.findViewById(R.id.iv_mute);
			iv_admin = (ImageView) itemView.findViewById(R.id.iv_admin);
		}
	}

	public List<EmUserItem> getSelectUser() {
		return this.mSelectUsers;
	}
}
