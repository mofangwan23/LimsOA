package cn.flyrise.feep.robot.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.robot.R;
import cn.flyrise.feep.robot.entity.WhatCanSayItem;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2017-12-6-11:34.
 */

public class WhatCanSayItemAdapter extends RecyclerView.Adapter<WhatCanSayItemAdapter.ViewHolder> {

	private List<WhatCanSayItem> whatCanSayItems = new ArrayList<>();

	private OnMoresListener listener;

	public WhatCanSayItemAdapter(List<WhatCanSayItem> sayItems, OnMoresListener listener) {
		whatCanSayItems.add(null);
		this.whatCanSayItems.addAll(sayItems);
		this.listener = listener;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.robot_more_item_layout, parent, false));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		if (position == 0) {
			holder.mTvHeadTitle.setVisibility(View.VISIBLE);
			holder.mLayoutContent.setVisibility(View.GONE);
		}
		else {
			holder.mTvHeadTitle.setVisibility(View.GONE);
			holder.mLayoutContent.setVisibility(View.VISIBLE);
			WhatCanSayItem whatCanSayItem = whatCanSayItems.get(position);
			if (whatCanSayItem == null) {
				return;
			}
			holder.mImgIcon.setImageResource(getMoreIcon(whatCanSayItem.id));
			holder.mTvTitle.setText(whatCanSayItem.title);
			holder.mTvContent.setText(whatCanSayItem.content);
			holder.mLayoutContent.setOnClickListener(v -> {
				if (listener != null) {
					listener.more(whatCanSayItem.title, whatCanSayItem.mores);
				}
			});
		}
	}

	@Override
	public int getItemCount() {
		return CommonUtil.isEmptyList(whatCanSayItems) ? 0 : whatCanSayItems.size();
	}

	private int getMoreIcon(String id) {
		if (TextUtils.isEmpty(id)) {
			return R.drawable.robot_more_say_1;
		}

		StringBuilder sbType = new StringBuilder("robot_more_");
		sbType.append(id);
//		R.drawable drawable = new R.drawable();
//		int weatherType;
//		try {
//			Field field = R.drawable.class.getField(sbType.toString());
//			weatherType = (Integer) field.get(drawable);
//		} catch (Exception e) {
//			weatherType = R.drawable.robot_more_say_1;
//		}

		Context context = CoreZygote.getContext();
		if(context==null){
			return R.drawable.robot_more_say_1;
		}
		return context.getResources().getIdentifier(sbType.toString(), "drawable", context.getPackageName());

	}

	class ViewHolder extends RecyclerView.ViewHolder {

		TextView mTvTitle;
		TextView mTvContent;
		TextView mTvHeadTitle;
		ImageView mImgIcon;

		RelativeLayout mLayoutContent;

		public ViewHolder(View itemView) {
			super(itemView);
			mTvTitle = itemView.findViewById(R.id.title);
			mTvContent = itemView.findViewById(R.id.content);
			mTvHeadTitle = itemView.findViewById(R.id.head_title);
			mImgIcon = itemView.findViewById(R.id.img_icon);

			mLayoutContent = itemView.findViewById(R.id.content_layout);
		}
	}

	public interface OnMoresListener {

		void more(String title, List<String> mores);

	}

}
