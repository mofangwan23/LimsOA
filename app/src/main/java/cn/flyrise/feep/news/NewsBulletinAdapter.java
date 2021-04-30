package cn.flyrise.feep.news;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.bean.FEListItem;
import cn.flyrise.feep.core.base.views.adapter.BaseRecyclerAdapter;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.core.common.utils.PixelUtil;
import cn.flyrise.feep.utils.RandomSources;
import java.util.List;

/**
 * @author ZYP
 * @since 2016-11-09 13:57
 */
public class NewsBulletinAdapter extends BaseRecyclerAdapter {

	private View mEmptyView;
	private List<FEListItem> mListItems;
	private OnItemClickListener mItemClickListener;

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.mItemClickListener = listener;
	}

	public void setEmptyView(View emptyView) {
		this.mEmptyView = emptyView;
	}

	public void setListItems(List<FEListItem> feListItems) {
		this.mListItems = feListItems;
		this.notifyDataSetChanged();
		this.mEmptyView.setVisibility(CommonUtil.isEmptyList(mListItems) ? View.VISIBLE : View.GONE);
	}

	public void addFEListItem(List<FEListItem> feListItems) {
		if (!CommonUtil.isEmptyList(feListItems)) {
			if (mListItems == null) this.mListItems = feListItems;
			else mListItems.addAll(feListItems);
			this.notifyDataSetChanged();
		}
		this.mEmptyView.setVisibility(CommonUtil.isEmptyList(mListItems) ? View.VISIBLE : View.GONE);
	}

	@Override public int getDataSourceCount() {
		return CommonUtil.isEmptyList(mListItems) ? 0 : mListItems.size();
	}

	@SuppressLint("RtlHardcoded")
	@Override public void onChildBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
		NewsBulletinViewHolder holder = (NewsBulletinViewHolder) viewHolder;
		FEListItem feListItem = mListItems.get(position);
		String category = feListItem.getCategory();
		if (!TextUtils.isEmpty(category)) {
			holder.tvCategory.setVisibility(View.VISIBLE);
			holder.tvCategory.setText(category);
			holder.tvCategory.setBackgroundResource(RandomSources.getSourceById(category));
		}

		String title = feListItem.getTitle();
		holder.tvTitle.setText(title);

		String content = feListItem.getContent();
		holder.tvContent.setVisibility(TextUtils.isEmpty(content) ? View.GONE : View.VISIBLE);
		holder.tvContent.setText(content);

		String sendUser = feListItem.getSendUser();
		holder.tvSendUser.setText(TextUtils.isEmpty(sendUser) ? feListItem.getMsgType() : sendUser);

		holder.ivMessageState.setVisibility(feListItem.isNews() ? View.VISIBLE : View.GONE);

		String viewCount = feListItem.getBadge();
		boolean hasViewCount = !TextUtils.isEmpty(viewCount);
		holder.vViewCount.setVisibility(hasViewCount ? View.VISIBLE : View.GONE);
		holder.tvViewCount.setText(viewCount);

		String sendTime = DateUtil.formatTimeForList(feListItem.getSendTime());
		holder.tvSendTime.setText(sendTime);
		if (!hasViewCount) {
			holder.vSendTime.setGravity(Gravity.RIGHT);
			holder.vSendTime.setPadding(0, 0, PixelUtil.dipToPx(16), 0);
		}

		holder.itemView.setOnClickListener(view -> {
			if (mItemClickListener != null) mItemClickListener.onItemClick(feListItem);
			if (feListItem.isNews()) {
				holder.ivMessageState.setVisibility(View.GONE);
				feListItem.setNews(false);
			}
		});
	}

	@Override
	public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
		return new NewsBulletinViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news_bulletin, parent, false));
	}

	public interface OnItemClickListener {

		void onItemClick(FEListItem feListItem);
	}

	public class NewsBulletinViewHolder extends RecyclerView.ViewHolder {

		public TextView tvCategory;
		public TextView tvTitle;
		public TextView tvContent;
		public TextView tvSendUser;
		private LinearLayout vSendTime;
		public TextView tvSendTime;
		private View vViewCount;
		public TextView tvViewCount;
		private View ivMessageState;

		NewsBulletinViewHolder(View itemView) {
			super(itemView);
			tvCategory = itemView.findViewById(R.id.tvCategory);
			tvTitle = itemView.findViewById(R.id.tvTitle);
			tvContent = itemView.findViewById(R.id.tvContent);
			tvSendUser = itemView.findViewById(R.id.tvSendUser);
			vSendTime = itemView.findViewById(R.id.layoutSendTime);
			tvSendTime = itemView.findViewById(R.id.tvSendTime);
			vViewCount = itemView.findViewById(R.id.layoutViewCount);
			tvViewCount = itemView.findViewById(R.id.tvViewCount);
			ivMessageState = itemView.findViewById(R.id.ivMessageState);
		}
	}
}
