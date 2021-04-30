package cn.flyrise.feep.location.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.location.bean.LocationSaveItem;
import java.util.ArrayList;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2017-11-6-15:35.
 */

public class LocationCustomModifyAdapter extends RecyclerView.Adapter<LocationCustomModifyAdapter.ViewHodler> {

	private List<LocationSaveItem> poiItems;
	private OnLocationCustomListener mListener;
	private final List<RadioButton> mCheckBoxs = new ArrayList<>();

	public LocationCustomModifyAdapter(List<LocationSaveItem> poiItems) {
		this.poiItems = poiItems;
	}

	@Override
	public ViewHodler onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_custom_item, parent, false);
		return new ViewHodler(view);
	}

	@Override
	public void onBindViewHolder(ViewHodler holder, int position) {
		LocationSaveItem poiItem = poiItems.get(position);
		if (poiItem == null) return;
		holder.mTvTitle.setText(poiItem.title);
		holder.mTvContent.setText(poiItem.content);
		holder.mCheckBox.setChecked(poiItem.isCheck);
		holder.mButtomLine.setVisibility(position == poiItems.size() - 1 ? View.VISIBLE : View.GONE);

		if (poiItem.isCheck) mCheckBoxs.add(holder.mCheckBox);

		holder.mTvModify.setOnClickListener(v -> {
			if (mListener != null) mListener.customModify(position, poiItem.isCheck);
		});
		holder.mTvDelete.setOnClickListener(v -> {
			if (mListener != null) mListener.customDelete(position, poiItem.isCheck);
		});
		holder.mCheckBox.setOnClickListener(v -> {
			mCheckBoxs.add(holder.mCheckBox);
			setCheckedItem(poiItem.poiId, holder);
		});
	}

	private void setCheckedItem(String key, ViewHodler holder) {
		for (RadioButton checkBox : mCheckBoxs) {
			if (checkBox == null) {
				continue;
			}
			checkBox.setChecked(false);
		}
		for (LocationSaveItem item : poiItems) {
			item.isCheck = TextUtils.equals(item.poiId, key);
		}
		holder.mCheckBox.setChecked(true);
	}

	@Override
	public int getItemCount() {
		return poiItems == null ? 0 : poiItems.size();
	}

	class ViewHodler extends RecyclerView.ViewHolder {

		private TextView mTvTitle;
		private TextView mTvContent;
		private TextView mTvModify;
		private TextView mTvDelete;
		private RadioButton mCheckBox;
		private View mButtomLine;

		ViewHodler(View itemView) {
			super(itemView);
			mTvTitle = itemView.findViewById(R.id.location_title);
			mTvContent = itemView.findViewById(R.id.location_context);
			mTvModify = itemView.findViewById(R.id.modify_button);
			mTvDelete = itemView.findViewById(R.id.delete_button);
			mCheckBox = itemView.findViewById(R.id.radio_btn);
			mButtomLine = itemView.findViewById(R.id.buttomLine);
		}
	}

	public void setAdapterItem(List<LocationSaveItem> poiItems) {
		this.poiItems = poiItems;
		notifyDataSetChanged();
	}

	public List<LocationSaveItem> getPoiItems() {
		return poiItems == null ? new ArrayList<>() : poiItems;
	}

	public interface OnLocationCustomListener {

		void customModify(int position, boolean isCheck);//当前修改的Item

		void customDelete(int position, boolean isCheck);//当前删除的Item

	}

	public void setOnLocationCustomListener(OnLocationCustomListener listener) {
		this.mListener = listener;
	}

}
