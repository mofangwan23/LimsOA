package cn.flyrise.feep.addressbook.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.views.adapter.BaseRecyclerAdapter;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.core.services.model.AddressBook;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-11-23 16:42
 */
public class OrganizationStructureRightAdapter extends BaseRecyclerAdapter {

	private final String mHost;
	private final Context mContext;
	private List<AddressBook> mAddressBooks;
	private OnItemClickListener mItemClickListener;

	public OrganizationStructureRightAdapter(Context context, String host) {
		this.mContext = context;
		this.mHost = host;
	}

	public void setAddressBooks(List<AddressBook> addressBooks) {
		this.mAddressBooks = addressBooks;
		this.notifyDataSetChanged();
	}

	public void addAddressBooks(List<AddressBook> addressBooks) {
		if (mAddressBooks == null) {
			mAddressBooks = new ArrayList<>();
		}

		mAddressBooks.addAll(addressBooks);
		this.notifyDataSetChanged();
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.mItemClickListener = listener;
	}

	@Override public int getDataSourceCount() {
		return CommonUtil.isEmptyList(mAddressBooks) ? 0 : mAddressBooks.size();
	}

	@Override
	public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		ViewHolder viewHolder = (ViewHolder) holder;
		AddressBook addressBook = mAddressBooks.get(position);
		String username = addressBook.name;
		viewHolder.tvUsername.setText(username);
		viewHolder.tvPosition.setText(addressBook.position);
		FEImageLoader.load(mContext, viewHolder.ivUserIcon, mHost + addressBook.imageHref, addressBook.userId, username);

		holder.itemView.setOnClickListener(v -> {
			if (mItemClickListener != null) {
				mItemClickListener.onItemClick(position, addressBook);
			}
		});
	}

	@Override public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_organization_persons, parent, false));
	}

	public class ViewHolder extends RecyclerView.ViewHolder {

		private TextView tvUsername;
		private TextView tvPosition;
		private ImageView ivUserIcon;

		public ViewHolder(View itemView) {
			super(itemView);
			tvUsername = itemView.findViewById(R.id.tvUserName);
			tvPosition = itemView.findViewById(R.id.tvPosition);
			ivUserIcon = itemView.findViewById(R.id.ivUserIcon);
		}
	}

	public interface OnItemClickListener {

		void onItemClick(int position, AddressBook addressBook);
	}

}
