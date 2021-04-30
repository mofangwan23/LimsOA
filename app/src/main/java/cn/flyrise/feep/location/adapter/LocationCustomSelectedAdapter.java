package cn.flyrise.feep.location.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.location.bean.LocationSaveItem;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2017-11-6-15:35.
 */

public class LocationCustomSelectedAdapter extends RecyclerView.Adapter<LocationCustomSelectedAdapter.ViewHodler> {

	private List<LocationSaveItem> poiItems;

	private int selected = -1;
	private OnClickItemListener listener;

	public LocationCustomSelectedAdapter(List<LocationSaveItem> poiItems, int selected, OnClickItemListener listener) {
		this.poiItems = poiItems;
		this.selected = selected < 0 ? 0 : selected;
		this.listener = listener;
	}

	@Override
	public ViewHodler onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_custom_selected_item, parent, false);
		return new ViewHodler(view);
	}

	@Override
	public void onBindViewHolder(ViewHodler holder, int position) {
		LocationSaveItem poiItem = poiItems.get(position);
		if (poiItem == null) return;
		holder.mTvTitle.setText(poiItem.title);
		holder.mTvContent.setText(poiItem.content);
		holder.mCheckBox.setVisibility(selected == position ? View.VISIBLE : View.INVISIBLE);
		holder.mTvDefault.setVisibility(poiItem.isCheck ? View.VISIBLE : View.GONE);
		holder.mItem.setOnClickListener(v -> {
			setCheckedItem(position);
			if (listener != null) listener.onClickItem(poiItem);
		});
	}

	public void setCheckedItem(int key) {
		selected = key;
		notifyDataSetChanged();
	}

	@Override
	public int getItemCount() {
		return poiItems == null ? 0 : poiItems.size();
	}

	class ViewHodler extends RecyclerView.ViewHolder {

		private TextView mTvTitle;
		private TextView mTvContent;
		private TextView mTvDefault;
		private ImageView mCheckBox;
		private View mItem;

		ViewHodler(View itemView) {
			super(itemView);
			mItem = itemView;
			mTvTitle = itemView.findViewById(R.id.location_title);
			mTvContent = itemView.findViewById(R.id.location_context);
			mTvDefault = itemView.findViewById(R.id.location_context_hint);
			mCheckBox = itemView.findViewById(R.id.radio_btn);
		}
	}

	public void setAdapterItem(List<LocationSaveItem> poiItems, int selected) {
		this.selected = selected;
		this.poiItems = poiItems;
		notifyDataSetChanged();
	}

	public LocationSaveItem getSelectedItem() {
		if (CommonUtil.isEmptyList(poiItems)) return null;
		if (selected >= 0 && selected < poiItems.size()) {
			return poiItems.get(selected);
		}
		return null;
	}

	public interface OnClickItemListener {

		void onClickItem(LocationSaveItem saveItem);
	}

}
