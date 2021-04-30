package cn.flyrise.feep.addressbook.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.core.services.model.AddressBook;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-02-14 16:49
 */
public class MineAttentionAdapter extends RecyclerView.Adapter<MineAttentionAdapter.MineAttentionViewHolder> {

	private View mEmptyView;
	private List<AddressBook> mAttentions;
	private Context mContext;
	private String mHost;
	private OnItemClickListener mItemClickListener;

	public MineAttentionAdapter(Context context, String host) {
		this.mContext = context;
		this.mHost = host;
	}

	public void setEmptyView(View view) {
		this.mEmptyView = view;
	}

	public void setAttentions(List<AddressBook> attentions) {
		this.mAttentions = attentions;
		if (CommonUtil.isEmptyList(mAttentions)) {
			if (mEmptyView != null) {
				mEmptyView.setVisibility(View.VISIBLE);
			}
		}
		else {
			if (mEmptyView != null) {
				mEmptyView.setVisibility(View.GONE);
			}
		}
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.mItemClickListener = listener;
	}

	@Override public MineAttentionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_contact_mine_attention, parent, false);
		return new MineAttentionViewHolder(itemView);
	}

	@Override public void onBindViewHolder(MineAttentionViewHolder holder, int position) {
		final AddressBook addressBook = mAttentions.get(position);
		String deptName = TextUtils.isEmpty(addressBook.deptName) ? "" : addressBook.deptName + "-";
		holder.tvPosition.setText(deptName + addressBook.position);
		holder.tvUserName.setText(addressBook.name);
		FEImageLoader.load(mContext, holder.ivUserIcon, mHost + addressBook.imageHref,
				addressBook.userId, addressBook.name);

		holder.itemView.setOnClickListener(view -> {
			if (mItemClickListener != null) {
				mItemClickListener.onItemClick(addressBook);
			}
		});

	}

	@Override public int getItemCount() {
		return CommonUtil.isEmptyList(mAttentions) ? 0 : mAttentions.size();
	}

	public class MineAttentionViewHolder extends RecyclerView.ViewHolder {

		public View itemView;
		public ImageView ivUserIcon;
		public TextView tvUserName;
		public TextView tvPosition;

		public MineAttentionViewHolder(View itemView) {
			super(itemView);
			this.itemView = itemView;
			ivUserIcon = (ImageView) itemView.findViewById(R.id.ivUserIcon);
			tvUserName = (TextView) itemView.findViewById(R.id.tvUserName);
			tvPosition = (TextView) itemView.findViewById(R.id.tvUserPosition);
		}
	}

	public interface OnItemClickListener {

		void onItemClick(AddressBook addressBook);
	}


}
