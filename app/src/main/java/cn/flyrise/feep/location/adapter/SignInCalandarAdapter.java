package cn.flyrise.feep.location.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.bean.FEListItem;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.views.adapter.BaseRecyclerAdapter;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.location.ShowNetPhotoActivity;
import cn.flyrise.feep.location.bean.LocationDetailItem;
import cn.flyrise.feep.location.widget.SignInCalendarBubbleView;
import java.util.List;

public class SignInCalandarAdapter extends BaseRecyclerAdapter {

	private final Context context;
	private List<FEListItem> items;

	public SignInCalandarAdapter(Context context) {
		this.context = context;
	}

	public void refreshAdapter(List<FEListItem> items) {
		this.items = items;
		notifyDataSetChanged();
	}

	@Override
	public int getDataSourceCount() {
		return CommonUtil.isEmptyList(items) ? 0 : items.size();
	}

	@Override
	public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		ViewHodler viewHodler = (ViewHodler) holder;
		viewHodler.convertView.setOnClickListener(null);// 清空点击事件，不给点击
		final FEListItem item = items.get(position);
		viewHodler.message_layout.setVisibility(View.VISIBLE);
		viewHodler.name.setText(item.getName());
		final String imgUrl = item.getImageHerf();
		if (imgUrl == null || "".equals(imgUrl)) {// 如果图片为空，说明不是现场签到的
			viewHodler.address.setText(item.getAddress());
			viewHodler.address.setVisibility(View.VISIBLE);
			viewHodler.liveImage.setVisibility(View.GONE);
		}
		else {
			viewHodler.liveImage.setVisibility(View.VISIBLE);
			String sImgUrl = item.getSguid();
			if (!TextUtils.isEmpty(imgUrl)) {
				String fImageUrl = CoreZygote.getLoginUserServices().getServerAddress() + sImgUrl;
				FEImageLoader.load(context, viewHodler.liveImage, fImageUrl, R.drawable.default_error);
			}
			if (item.getPdesc() == null || "".equals(item.getPdesc()) || isNoDescription(item.getPdesc())) {
				viewHodler.address.setText(item.getAddress());
			}
			else {
				viewHodler.address.setText(item.getPdesc());
			}
			// 点击查看现场签到图片
			viewHodler.convertView.setOnClickListener(v -> openLocationPhoton(item));
		}
		viewHodler.time.setVisibility(View.VISIBLE);
		viewHodler.time.setText(item.getTime());
		viewHodler.mLine.setVisibility(items.size() - 1 == position ? View.INVISIBLE : View.VISIBLE);
		viewHodler.mLayoutBubble.setBackgroundDrawable(new SignInCalendarBubbleView());
	}

	@Override
	public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHodler(View.inflate(context, R.layout.location_history_item, null));
	}

	private class ViewHodler extends RecyclerView.ViewHolder {

		private TextView time;
		private TextView name;
		private TextView address;
		private RelativeLayout message_layout;
		private RelativeLayout mLayoutBubble;
		ImageView liveImage;
		private View convertView;
		private View mLine;

		ViewHodler(View itemView) {
			super(itemView);
			convertView = itemView;
			time = itemView.findViewById(R.id.item_time);
			name = itemView.findViewById(R.id.location_name);
			address = itemView.findViewById(R.id.location_address);
			message_layout = itemView.findViewById(R.id.message_layout);
			mLayoutBubble = itemView.findViewById(R.id.layout_content);
			liveImage = itemView.findViewById(R.id.location_image);
			mLine = itemView.findViewById(R.id.line);
		}
	}

	private boolean isNoDescription(String content) {
		return context.getResources().getString(R.string.onsite_no_description).equals(content);
	}

	private void openLocationPhoton(FEListItem item) {
		String realImagePath = item.getGuid();
		if (TextUtils.isEmpty(realImagePath)) {
			realImagePath = item.getSguid();
		}

		final Intent intent = new Intent(context, ShowNetPhotoActivity.class);
		LocationDetailItem photoItem = new LocationDetailItem();
		photoItem.title = item.getName();
		photoItem.address = item.getAddress();
		photoItem.describe = item.getPdesc();
		photoItem.iconUrl = CoreZygote.getLoginUserServices().getServerAddress() + realImagePath;
		photoItem.date = item.getDate() + " " + item.getTime();
		intent.putExtra("location_detail_data", GsonUtil.getInstance().toJson(photoItem));
		context.startActivity(intent);
	}
}
