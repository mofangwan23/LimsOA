//
// feep
//
// Created by ZhongYJ on 2012-02-10.
// Copyright 2011 flyrise. All rights reserved.
//

package cn.flyrise.feep.form.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.flyrise.android.protocol.model.ReferenceItem;
import cn.flyrise.feep.R;
import cn.flyrise.feep.form.FormSendToDisposeActivity.ChooseSubNodeObjec;
import cn.flyrise.feep.form.been.FormSubNodeInfo;

public class FormSubNodeAdapter extends BaseAdapter {

	private final Context mContext;

	private List<FormSubNodeInfo> subNodes = new ArrayList<>();


	class ViewHolder {

		public View view;

		public TextView mName;

		public TextView mPosition;

		public ImageView mDelete;
	}


	public FormSubNodeAdapter(Context context, ChooseSubNodeObjec chooseSubNodeObjec) {
		mContext = context;
	}

	@Override
	public int getCount() {
		if (subNodes == null || subNodes.size() == 0) {
			return 0;
		}
		return subNodes.size();
	}

	@Override
	public FormSubNodeInfo getItem(int position) {
		return subNodes.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	// /**
	// * 获取列表item对应的选择状态
	// *
	// * @param position 列表item的索引
	// * @return true为选中，fasle为未选中
	// */
	// public boolean getItemState(int position) {
	// return mStates.get(position);
	// }

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			final LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(R.layout.form_person_listitem, null);
			holder = new ViewHolder();
			holder.view = convertView;
			holder.mName = (TextView) convertView.findViewById(R.id.form_node_two_name);
			holder.mPosition = (TextView) convertView.findViewById(R.id.form_node_two_position);
			holder.mDelete = (ImageView) convertView.findViewById(R.id.form_node_two_bnt);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (subNodes != null) {
			final FormSubNodeInfo subNodeInfo = subNodes.get(position);
			final ReferenceItem referenceItem = subNodeInfo.getReferenceItem();
			final String key = referenceItem.getKey();
			if (key == null) {
				holder.mDelete.setVisibility(View.GONE);
				holder.mName.setTextColor(0xffff0000);
				holder.mPosition.setTextColor(0xffff0000);
			}
			else {
				holder.mName.setTextColor(0xff000000);
				holder.mPosition.setTextColor(0xFF555555);
				holder.mDelete.setVisibility(View.VISIBLE);
			}
			final boolean state = subNodeInfo.isNeedAddState();
			final String value = referenceItem.getValue();
			holder.mName.setText(value == null ? "" : value);
			final String description = referenceItem.getDescription();
			if (description == null || description.length() == 0) {
				holder.mPosition.setVisibility(View.GONE);
			}
			else {
				holder.mPosition.setVisibility(View.VISIBLE);
				holder.mPosition.setText(description);
			}
			holder.mDelete.setImageResource(state ? R.drawable.action_add_fe : R.drawable.icon_wrong);
		}
		return convertView;
	}

	public void refreshDatas(List<FormSubNodeInfo> subNodes, String filterText) {
		if (TextUtils.isEmpty(filterText)) {
			this.subNodes = subNodes;
		}
		else {
			this.subNodes = new ArrayList<>();
			for (FormSubNodeInfo nodeInfo : subNodes) {
				ReferenceItem referenceItem = nodeInfo.getReferenceItem();
				final String value = referenceItem.getValue();
				if (value.contains(filterText)) {
					this.subNodes.add(nodeInfo);
				}
			}
		}
		notifyDataSetChanged();
	}

	/**
	 * 设置所有的节点状态为未选状态
	 */
	public void setAllNodeNoCheckState(List<FormSubNodeInfo> subNodeInfos) {
//		setNoCheckState(subNodeInfos);
		notifyDataSetChanged();
	}

//	/**
//	 * 设置所有的节点状态为未选状态
//	 */
//	private void setNoCheckState(List<FormSubNodeInfo> subNodeInfos) {
//		if (subNodeInfos != null) {
//			for (final FormSubNodeInfo subNodeInfo : subNodeInfos) {
//				if (!subNodeInfo.isNeedAddState()) {
//					subNodeInfo.setNeedAddState(true);
//				}
//			}
//		}
//	}

	/**
	 * 改变选择状态
	 */
	public void changeState(int position, boolean isChecked) {
		if (subNodes != null) {
			final FormSubNodeInfo subNodeInfo = subNodes.get(position);
			if (subNodeInfo == null) {
				return;
			}
			subNodeInfo.setNeedAddState(isChecked);
		}
		notifyDataSetChanged();
	}

}
