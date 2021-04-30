package cn.flyrise.feep.commonality.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import cn.flyrise.feep.core.base.views.adapter.FEListAdapter;

import cn.flyrise.android.library.view.addressbooklistview.been.AddressBookListItem;
import cn.flyrise.android.protocol.model.AddressBookItem;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.image.loader.FEImageLoader;

public class TheContactSearchListViewAdapter extends FEListAdapter<AddressBookListItem> {

	private OnItemContentClickListener onItemContentClickListener;

	public void setItemContentClickListener(
			OnItemContentClickListener itemContentClickListener) {
		this.onItemContentClickListener = itemContentClickListener;
	}

	@Override
	public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		ItemViewHolder viewHolder = (ItemViewHolder) holder;
		final AddressBookItem userInfo = dataList.get(position).getAddressBookItem();
		if (userInfo == null) {
			return;
		}
		String host = CoreZygote.getLoginUserServices().getServerAddress();
		FEImageLoader
				.load(CoreZygote.getContext(), viewHolder.imgIcon, host + userInfo.getImageHref(), userInfo.getId(), userInfo.getName());
		viewHolder.iconTv.setText("");
		viewHolder.mName.setText(userInfo.getName());
		viewHolder.mPosition.setText(userInfo.getPosition());
		final String tel = userInfo.getTel();
		if (!TextUtils.isEmpty(tel)) {
			viewHolder.mPhone.setVisibility(View.VISIBLE);
			viewHolder.mPhone.setText(tel);
			viewHolder.mCallBnt.setVisibility(View.VISIBLE);
		}
		else {
			viewHolder.mPhone.setVisibility(View.GONE);
			viewHolder.mCallBnt.setVisibility(View.INVISIBLE);
		}
		viewHolder.mCallBnt.setOnClickListener(v -> {
			if (onItemContentClickListener != null) {
				onItemContentClickListener.onPhoneClick(tel);
			}
		});
		viewHolder.itemView.setOnClickListener(v -> {
			if (onItemClickListener != null) {
				onItemClickListener.onItemClick(v, userInfo);
			}
		});
		viewHolder.bottom.setVisibility(position == getItemCount() - 1 ? View.GONE : View.VISIBLE);
	}

	@Override
	public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.thecontact_search_listitem, null);
		return new ItemViewHolder(view);
	}

	private class ItemViewHolder extends RecyclerView.ViewHolder {

		ImageView mCallBnt;
		TextView mName;
		TextView mPosition;
		TextView mPhone;
		ImageView imgIcon;
		TextView iconTv;
		TextView bottom;

		public ItemViewHolder(View itemView) {
			super(itemView);
			imgIcon = (ImageView) itemView.findViewById(R.id.the_contact_search_item_icon);
			iconTv = (TextView) itemView.findViewById(R.id.the_contact_item_icon_tv);
			mPhone = (TextView) itemView.findViewById(R.id.address_list_item_phone);
			mPosition = (TextView) itemView.findViewById(R.id.address_list_item_position);
			mName = (TextView) itemView.findViewById(R.id.address_list_item_name);
			mCallBnt = (ImageView) itemView.findViewById(R.id.address_list_item_callbnt);
			bottom = (TextView) itemView.findViewById(R.id.address_list_item_bottom);
		}
	}

	public interface OnItemContentClickListener {

		void onPhoneClick(String tel);
	}

}