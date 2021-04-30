package cn.flyrise.feep.addressbook.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.core.services.model.AddressBook;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZYP
 * @since 2016-12-12 10:01
 */
public class ContactSearchAdapter extends BaseContactAdapter {

	public ContactSearchAdapter(Context context) {
		super(context);
	}

	public void addContacts(List<AddressBook> contacts) {
		if (this.mContacts == null) {
			this.mContacts = new ArrayList<>();
		}
		this.mContacts.addAll(contacts);
		this.notifyDataSetChanged();
		if (mEmptyView != null) this.mEmptyView.setVisibility(CommonUtil.isEmptyList(mContacts) ? View.VISIBLE : View.GONE);
	}

	public void clearContacts() {
		if (mContacts != null) this.mContacts.clear();
		this.removeFooterView();
		this.notifyDataSetChanged();
		if (mEmptyView != null) this.mEmptyView.setVisibility(View.GONE);
	}

	@Override public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		ContactViewHolder contactViewHolder = (ContactViewHolder) holder;
		AddressBook contact = mContacts.get(position);

		FEImageLoader.load(mContext, contactViewHolder.ivUserIcon, mHostUrl + contact.imageHref,
				contact.userId, contact.name);

		contactViewHolder.tvUserName.setText(contact.name);
		contactViewHolder.tvLetter.setVisibility(View.GONE);
		contactViewHolder.tvUserPosition.setText(contact.deptName + " - " + contact.position);

		contactViewHolder.ivContactCheck.setVisibility(withSelect ? View.VISIBLE : View.GONE);
		if (withSelect) {
			if (getCannotSelectContacts().contains(contact)) {
				if (TextUtils.equals(contact.userId, mLoginUser)) {
					contactViewHolder.ivContactCheck.setVisibility(View.GONE);
				}
				else {
					contactViewHolder.ivContactCheck.setVisibility(View.VISIBLE);
					contactViewHolder.ivContactCheck.setImageResource(getCannotSelectContacts().contains(contact)
							? R.drawable.no_choice : R.drawable.shape_circle_grey_20);
				}
			}
			else {
				contactViewHolder.ivContactCheck.setVisibility(View.VISIBLE);
				contactViewHolder.ivContactCheck.setImageResource(getSelectedContacts().contains(contact)
						? R.drawable.node_current_icon : R.drawable.shape_circle_grey_20);
			}

			if (isExceptOwn) {
				if (TextUtils.equals(CoreZygote.getLoginUserServices().getUserId(), contact.userId)) {
					holder.itemView.setEnabled(false);
					contactViewHolder.ivContactCheck.setImageResource(R.drawable.shape_circle_grey_no_selected_20);
					holder.itemView.setBackgroundColor(Color.parseColor("#EDEDED"));
				}
				else {
					holder.itemView.setBackgroundColor(Color.parseColor("#ffffff"));
				}
			}
		}

		contactViewHolder.itemView.setOnClickListener(view -> {
			if (mItemClickListener != null) {
				if (getCannotSelectContacts().contains(contact)) {
					return;
				}
				mItemClickListener.onItemClick(contact, position);
			}
		});
	}
}
