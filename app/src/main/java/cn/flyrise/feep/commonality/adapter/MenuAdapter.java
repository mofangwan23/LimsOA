//
// LoadingImage.java
// feep
//
// Created by ZhongYJ on 2012-3-6.
// Copyright 2011 flyrise. All rights reserved.
//

package cn.flyrise.feep.commonality.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.function.AppTopMenu;
import com.drop.DropCover;
import com.drop.WaterDrop;

import java.util.List;

import cn.flyrise.android.library.view.ResizeTextView;
import cn.flyrise.android.protocol.model.BadgeCount;
import cn.flyrise.feep.FEApplication;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.bean.MenuInfo;
import cn.flyrise.feep.core.common.X.MainMenu;

public class MenuAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater mInflater;

	private BadgeCount mBadgeCount;
	private List<AppTopMenu> mMenuInfoList;

	private int mCurrentPosition = 0;
	private boolean hasUnreadApplicationMessage = false;

	private boolean mobileKeyIsActive = true;

	private DropCover.OnDragCompeteListener mListener;

	public MenuAdapter(Context context, List<AppTopMenu> menuInfoLis, DropCover.OnDragCompeteListener listener) {
		mContext = context;
		mMenuInfoList = menuInfoLis;
		this.mListener = listener;
		mInflater = LayoutInflater.from(mContext);
	}

	@Override
	public int getCount() {
		return mMenuInfoList == null ? 0 : mMenuInfoList.size();
	}

	@Override
	public Object getItem(int position) {
		return mMenuInfoList == null ? null : mMenuInfoList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void setCurrentPosition(int position) {
		this.mCurrentPosition = position;
		notifyDataSetChanged();
	}

	public void setBadgeCount(BadgeCount badgeCount) {
		this.mBadgeCount = badgeCount;
		notifyDataSetChanged();
	}

	public void setUnreadApplicationMessage(boolean unreadApplicationMessage) {
		this.hasUnreadApplicationMessage = unreadApplicationMessage;
		notifyDataSetChanged();
	}

	public void setMobileKeyIsActive(boolean mobileKeyIsActive) {
		this.mobileKeyIsActive = mobileKeyIsActive;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.menu_popupwindow_item, null);
			convertView.setBackgroundResource(R.drawable.menu_item_background_selected);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}

		AppTopMenu menuInfo = mMenuInfoList.get(position);
		if (mCurrentPosition == position) {
			holder.ivModuleIcon.setBackgroundResource(menuInfo.selectedIcon);
			holder.tvModuleName.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
		}
		else {
			holder.ivModuleIcon.setBackgroundResource(menuInfo.normalIcon);
			holder.tvModuleName.setTextColor(mContext.getResources().getColor(R.color.text_menu_text_color));
		}

		holder.waterDrop.setTouchEnable(mCurrentPosition == position);//显示消息界面才允许拖动气泡
		holder.tvModuleName.setText(menuInfo.menu);

		if (menuInfo.type == MainMenu.Mine) {
			boolean hasNewVersion = ((FEApplication) mContext.getApplicationContext()).hasNewVersion();
			holder.ivRedBadge.setVisibility(hasNewVersion || !mobileKeyIsActive ? View.VISIBLE : View.GONE);
			holder.waterDrop.setVisibility(View.GONE);
		}
		else if (menuInfo.type == MainMenu.Application) {
			holder.ivRedBadge.setVisibility(hasUnreadApplicationMessage ? View.VISIBLE : View.GONE);
			holder.waterDrop.setVisibility(View.GONE);
		}
		else if (menuInfo.type == MainMenu.Message) {
			displayUnreadBadgeCount(holder.waterDrop);
			holder.ivRedBadge.setVisibility(View.GONE);
		}
		else if (menuInfo.type == MainMenu.Associate){
			displayUnreadCircleCount(holder.ivRedBadge);
			holder.waterDrop.setVisibility(View.GONE);
		}else {
			holder.ivRedBadge.setVisibility(View.GONE);
			holder.waterDrop.setVisibility(View.GONE);
		}

		return convertView;
	}

	private void displayUnreadBadgeCount(WaterDrop mDrop) {
		if (mBadgeCount == null || !mBadgeCount.isMessageType()) {
			mDrop.setVisibility(View.GONE);
			return;
		}

		int badgeCount = mBadgeCount.getTotalCount();
		if (badgeCount == 0) {
			mDrop.setVisibility(View.GONE);
			return;
		}

		mDrop.setText(badgeCount <= 99 ? badgeCount + "" : "99+");
		mDrop.setVisibility(View.VISIBLE);
		mDrop.setOnDragCompeteListener(mListener);
	}

	private void displayUnreadCircleCount(ImageView ivRedBadge){
		if (mBadgeCount == null || !mBadgeCount.isMessageType()) {
			ivRedBadge.setVisibility(View.GONE);
			return;
		}
		int badgeCount = mBadgeCount.getCircleNums();
		if (badgeCount <= 0) {
			ivRedBadge.setVisibility(View.GONE);
		}else {
			ivRedBadge.setVisibility(View.VISIBLE);
		}
//		mDrop.setText(badgeCount <= 99 ? badgeCount + "" : "99+");
//		mDrop.setVisibility(View.VISIBLE);
//		mDrop.setOnDragCompeteListener(mListener);
	}

	public String getMenu(int position) {
		return mMenuInfoList == null ? null : mMenuInfoList.get(position).type;
	}


	private static class ViewHolder {

		ResizeTextView tvModuleName;
		ImageView ivModuleIcon;
		ImageView ivRedBadge;

		WaterDrop waterDrop;

		public ViewHolder(View convertView) {
			tvModuleName = convertView.findViewById(R.id.menu_item_text);
			ivModuleIcon = convertView.findViewById(R.id.menu_item_image);
			ivRedBadge = convertView.findViewById(R.id.num_icon_bg);
			waterDrop = convertView.findViewById(R.id.badge_view);
		}
	}
}
