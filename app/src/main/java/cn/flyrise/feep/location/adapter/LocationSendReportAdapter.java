package cn.flyrise.feep.location.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.views.adapter.BaseMessageRecyclerAdapter;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.location.bean.SignPoiItem;
import com.amap.api.services.core.PoiItem;
import java.util.ArrayList;
import java.util.List;

public class LocationSendReportAdapter extends BaseMessageRecyclerAdapter {

	private List<SignPoiItem> mItems;
	private SignPoiItem mSelectedItem;

	public void setPoiItems(List<SignPoiItem> items) {
		mSelectedItem = CommonUtil.isEmptyList(items) ? null : items.get(0);
		this.mItems = items;
		notifyDataSetChanged();
	}

	public void addPoiItems(List<SignPoiItem> items) {
		if (CommonUtil.isEmptyList(items)) return;
		if (mItems == null) mItems = new ArrayList<>();
		mItems.addAll(items);
		notifyDataSetChanged();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getDataSourceCount() {
		return CommonUtil.isEmptyList(mItems) ? 0 : mItems.size();
	}

	@Override
	public void onChildBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
		ViewHolder holder = (ViewHolder) viewHolder;
		final SignPoiItem signPoiItem = mItems.get(position);
		final PoiItem poiItem = signPoiItem.poiItem;

		if (mSelectedItem != null && mSelectedItem.poiItem != null && signPoiItem.poiItem != null
				&& TextUtils.equals(mSelectedItem.poiItem.getPoiId(), signPoiItem.poiItem.getPoiId())) {
			holder.report.setVisibility(View.VISIBLE);
		}
		else {
			holder.report.setVisibility(mSelectedItem == null && position == 0 ? View.VISIBLE : View.INVISIBLE);
		}

		holder.subject.setText(poiItem.getTitle());
		holder.contact.setText(poiItem.getSnippet());
		holder.favorite.setText(poiItem.getDistance() + " m");

		holder.frontView.setOnClickListener((v) -> {
			mSelectedItem = signPoiItem;
			notifyDataSetChanged();
		});
	}

	@Override
	public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.poi_item_location, parent, false));
	}

	private static class ViewHolder extends RecyclerView.ViewHolder {

		TextView subject;    // 公司名称
		TextView contact;    // 地址
		TextView favorite;   // 距离
		ImageView report;    // 图标
		View frontView;

		public ViewHolder(View convertView) {
			super(convertView);
			subject = convertView.findViewById(R.id.myItemView_subject);
			contact = convertView.findViewById(R.id.myItemView_contact);
			favorite = convertView.findViewById(R.id.myItemView_favorite);
			report = convertView.findViewById(R.id.myItemView_report);
			frontView = convertView.findViewById(R.id.id_front);
		}
	}

	public PoiItem getSelectedPoiItem() {
		return mSelectedItem == null ? null : mSelectedItem.poiItem;
	}

}
