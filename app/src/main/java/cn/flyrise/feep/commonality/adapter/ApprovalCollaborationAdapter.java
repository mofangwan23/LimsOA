package cn.flyrise.feep.commonality.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.bean.FEListItem;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.views.adapter.BaseMessageRecyclerAdapter;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2016/8/16 13:32
 */
public class ApprovalCollaborationAdapter extends BaseMessageRecyclerAdapter {

	private View mEmptyView;
	private List<FEListItem> mApprovalLists;
	private OnApprovalItemClickListener mApprovalItemClickListener;
	private String mHost;

	public ApprovalCollaborationAdapter() {
		mHost = CoreZygote.getLoginUserServices().getServerAddress();
	}

	public void setEmptyView(View emptyView) {
		this.mEmptyView = emptyView;
	}

	public void setApprovalLists(List<FEListItem> approvalLists) {
		if (CommonUtil.isEmptyList(approvalLists)) {
			approvalLists = new ArrayList<>();
		}
		this.mApprovalLists = approvalLists;
		if (mEmptyView != null) {
			mEmptyView.setVisibility(CommonUtil.isEmptyList(mApprovalLists) ? View.VISIBLE : View.GONE);
		}
		this.notifyDataSetChanged();
	}

	public void addApprovalLists(List<FEListItem> approvalLists) {
		if (CommonUtil.isEmptyList(approvalLists)) {
			return;
		}

		if (CommonUtil.isEmptyList(approvalLists)) {
			mApprovalLists = new ArrayList<>(approvalLists.size());
		}

		mApprovalLists.addAll(approvalLists);
		if (mEmptyView != null) {
			mEmptyView.setVisibility(CommonUtil.isEmptyList(mApprovalLists) ? View.VISIBLE : View.GONE);
		}
		this.notifyDataSetChanged();
	}

	public boolean needAddFooter(int totalSize) {
		return !CommonUtil.isEmptyList(mApprovalLists) && mApprovalLists.size() < totalSize;
	}

	public boolean removeMessage(String messageId) {
		if (CommonUtil.isEmptyList(mApprovalLists)) return false;
		FEListItem item = null;
		int position = -1;
		for (int i = 0; i < mApprovalLists.size(); i++) {
			item = mApprovalLists.get(i);
			if (item != null && TextUtils.equals(messageId, item.getId())) {
				position = i;
				break;
			}
		}
		if (position < 0) return false;
		if (mApprovalLists.remove(item)) {
			notifyItemRemoved(position);
			return true;
		}
		return false;
	}

	public void setOnApprovalItemClickListener(OnApprovalItemClickListener listener) {
		this.mApprovalItemClickListener = listener;
	}

	public boolean hasLevelItem() {
		if (!CommonUtil.isEmptyList(mApprovalLists)) {
			for (FEListItem listItem : mApprovalLists) {
				if (!TextUtils.isEmpty(listItem.getLevel())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int getDataSourceCount() {
		return CommonUtil.isEmptyList(mApprovalLists) ? 0 : mApprovalLists.size();
	}

	@Override
	public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		ApprovalViewHolder approvalHolder = (ApprovalViewHolder) holder;
		final FEListItem listItem = mApprovalLists.get(position);

		approvalHolder.itemView.setOnClickListener(v -> {
			if (mApprovalItemClickListener != null) {
				mApprovalItemClickListener.onApprovalItemClick(listItem);
			}
		});

		if (TextUtils.isEmpty(listItem.getLevel())) {
			approvalHolder.tvTitle.setTextColor(Color.BLACK);
		}
		else {
			approvalHolder.tvTitle.setTextColor(Color.RED);
		}
		approvalHolder.tvTitle.setText(listItem.getTitle());
		approvalHolder.tvAuthor.setText(listItem.getSendUser());

		if (TextUtils.isEmpty(listItem.getSendUserId())) {
			approvalHolder.ivAvatar.setVisibility(View.GONE);
		}
		else {
			CoreZygote.getAddressBookServices().queryUserDetail(listItem.getSendUserId())
					.subscribe(it -> {
						if (it != null) {
							approvalHolder.ivAvatar.setVisibility(View.VISIBLE);
							FEImageLoader.load(CoreZygote.getContext(), approvalHolder.ivAvatar
									, mHost + it.imageHref, listItem.getSendUserId(), listItem.getSendUser());
						}
						else {
							approvalHolder.ivAvatar.setVisibility(View.GONE);
						}
					}, error -> {
						approvalHolder.ivAvatar.setVisibility(View.GONE);
					});
		}

		if (TextUtils.isEmpty(listItem.getSendUser())) {
			approvalHolder.tvDate.setVisibility(View.GONE);
		}
		else {
			approvalHolder.tvDate.setVisibility(View.VISIBLE);
			approvalHolder.tvDate.setText(DateUtil.formatTimeForList(listItem.getSendTime()));
		}

		approvalHolder.tvImportant.setVisibility(View.VISIBLE);
		approvalHolder.tvImportant.setText(listItem.getImportant());
		if (TextUtils.equals(listItem.getImportant(), "特急")) {
			approvalHolder.tvImportant.setBackgroundResource(R.drawable.bg_approval_search_important_extra_urgen);
			approvalHolder.tvImportant.setTextColor(Color.parseColor("#FF3B2F"));
		}
		else if (TextUtils.equals(listItem.getImportant(), "急件") || TextUtils.equals(listItem.getImportant(), "加急")) {
			approvalHolder.tvImportant.setBackgroundResource(R.drawable.bg_approval_search_important_urgen);
			approvalHolder.tvImportant.setTextColor(Color.parseColor("#F28149"));
		}
		else {
			approvalHolder.tvImportant.setVisibility(View.GONE);
		}
	}

	@Override
	public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_approval_item, parent, false);
		return new ApprovalViewHolder(itemView);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getLoadBackgroundColor() {
		return Color.parseColor("#ffffff");
	}

	public class ApprovalViewHolder extends RecyclerView.ViewHolder {

		TextView tvTitle;
		TextView tvAuthor;
		TextView tvDate;
		ImageView ivAvatar;
		TextView tvImportant;

		ApprovalViewHolder(View itemView) {
			super(itemView);
			tvTitle = itemView.findViewById(R.id.tvApprovalItemTitle);
			tvAuthor = itemView.findViewById(R.id.tvApprovalItemAuthor);
			tvDate = itemView.findViewById(R.id.tvApprovalItemDate);
			ivAvatar = itemView.findViewById(R.id.ivApprovalAvatar);
			tvImportant = itemView.findViewById(R.id.tvApprovalImportant);
		}
	}

	public interface OnApprovalItemClickListener {

		void onApprovalItemClick(FEListItem listItem);
	}

}
